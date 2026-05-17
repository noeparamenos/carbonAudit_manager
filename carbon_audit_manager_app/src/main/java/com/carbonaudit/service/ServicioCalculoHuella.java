package com.carbonaudit.service;

import com.carbonaudit.dao.CommutingEmpleadoDAO;
import com.carbonaudit.dao.ConsumoMensualDAO;
import com.carbonaudit.dao.DepartamentoDAO;
import com.carbonaudit.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicioCalculoHuella {

    private final ConsumoMensualDAO    consumoDAO;
    private final CommutingEmpleadoDAO commutingDAO;
    private final DepartamentoDAO      departamentoDAO;

    public ServicioCalculoHuella() {
        this.consumoDAO      = new ConsumoMensualDAO();
        this.commutingDAO    = new CommutingEmpleadoDAO();
        this.departamentoDAO = new DepartamentoDAO();
    }

    // =========== COMMUTING DE EMPLEADO (cálculo puro, sin persistencia) ==============

    /**
     * Calcula la huella generada por un empleado al mes en su transporte al trabajo.
     * Requiere que {@code emp.getDistanciaTrabajo()} ya esté asignada.
     * Resultado expresado en kgCO2e.
     */
    public BigDecimal getCommutingMensual(Empleado emp) {
        if (emp.getDistanciaTrabajo() == null
                || emp.getMedioTransporte() == null
                || emp.getMedioTransporte().getValorFactor() == null) {
            throw new IllegalStateException("El empleado no tiene distancia al trabajo o medio de transporte válido.");
        }
        BigDecimal kmTotalesMes = emp.getDistanciaTrabajo()
                .multiply(new BigDecimal("2"))
                .multiply(new BigDecimal(emp.getDiasPresenciales()));
        return kmTotalesMes.multiply(emp.getMedioTransporte().getValorFactor())
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =========== ACCESO A CONSUMOS (encapsula el DAO para que el controlador no lo toque) ==============
    // Estos métodos evitan que la capa Controller llame al DAO directamente,
    // manteniendo la separación de capas: Controller → Service → DAO.

    /** Consumos de un departamento para un mes concreto. */
    public List<ConsumoMensual> getConsumosMensuales(int idDepartamento, int mes, int anio) {
        return consumoDAO.getConsumosDepartamentoMes(idDepartamento, mes, anio);
    }

    public void crearConsumo(ConsumoMensual consumo)      { consumoDAO.create(consumo); }
    public void actualizarConsumo(ConsumoMensual consumo) { consumoDAO.update(consumo); }
    public void eliminarConsumo(int idConsumo)            { consumoDAO.delete(idConsumo); }

    /** Consumos de una empresa para un mes concreto (todos sus departamentos). */
    public List<ConsumoMensual> getConsumosMensualesEmpresa(int idEmpresa, int mes, int anio) {
        return consumoDAO.getConsumosByEmpresaMes(idEmpresa, mes, anio);
    }

    /** Consumos de una empresa para un año completo (todos sus departamentos, los 12 meses). */
    public List<ConsumoMensual> getConsumosAnualesEmpresa(int idEmpresa, int anio) {
        return consumoDAO.getConsumosByEmpresaAnio(idEmpresa, anio);
    }

    /**
     * Suma las emisiones de una lista de consumos sin incluir commuting (Scope 1 + 2).
     * Centraliza el cálculo del total para que no quede lógica de negocio en el Controlador.
     */
    public BigDecimal getTotalConsumos(List<ConsumoMensual> consumos) {
        return consumos.stream()
                .map(ConsumoMensual::calcularEmision)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =========== CÁLCULO DE HUELLA POR DEPARTAMENTO ==============

    /**
     * Calcula la huella de carbono total de un departamento para un mes y año específico.
     * Suma Scope 1+2 (consumos) y opcionalmente Scope 3 (commuting) si es true para ese departamento.
     *
     * @param departamento Departamento del cual calcular la huella
     * @param mes         Mes del período (1-12)
     * @param anio        Año del período
     * @return Total de emisiones en kgCO2e con 2 decimales
     */
    public BigDecimal getHuellaTotalDepartamentoMes(Departamento departamento, int mes, int anio) {
        BigDecimal totalEmisiones = BigDecimal.ZERO;

        // SCOPE 1 + SCOPE 2: Consumos del departamento (energía, combustible, etc.)
        List<ConsumoMensual> consumos = consumoDAO.getConsumosDepartamentoMes(
                departamento.getIdDepartamento(), mes, anio);

        for (ConsumoMensual consumo : consumos) {
            BigDecimal emisionConsumo = consumo.calcularEmision();
            if (emisionConsumo != null) {
                totalEmisiones = totalEmisiones.add(emisionConsumo); //Sumamos cada consumo mensual al total
            }
        }

        // SCOPE 3: Commuting de empleados (solo si el departamento lo incluye)
        if (departamento.isIncluirAlcance3()) {
            List<CommutingEmpleado> commutings = commutingDAO.getCommutingsDepartamentoMes(
                    departamento.getIdDepartamento(), mes, anio);

            for (CommutingEmpleado commuting : commutings) {
                BigDecimal emisionCommuting = calcularEmisionCommuting(commuting);
                if (emisionCommuting != null) {
                    totalEmisiones = totalEmisiones.add(emisionCommuting); //Sumamos los commutings de cada empleado
                }
            }
        }

        // Redondeo final a 2 decimales
        return totalEmisiones.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la emisión de carbono para un registro de commuting específico.
     * Fórmula: distancia_diaria_km × 2 (ida y vuelta) × días_presenciales × factor_emisión
     *
     * @param commuting Registro de commuting del empleado
     * @return Emisión en kgCO2e
     */
    private BigDecimal calcularEmisionCommuting(CommutingEmpleado commuting) {
        // Comprobamos que exitan los requisitos para el cálculo
        if (commuting.getDistanciaDiariaKm() == null ||
            commuting.getMedioTransporte() == null ||
            commuting.getMedioTransporte().getValorFactor() == null) {
            return BigDecimal.ZERO;
        }
        //km total Ida y vuelta
        BigDecimal distanciaIda = commuting.getDistanciaDiariaKm();
        BigDecimal distanciaIdaVuelta = distanciaIda.multiply(new BigDecimal("2"));

        // Emisión mensual
        BigDecimal kmTotalesMes = distanciaIdaVuelta.multiply(new BigDecimal(commuting.getDiasPresencialesMes()));
        BigDecimal emision = kmTotalesMes.multiply(commuting.getMedioTransporte().getValorFactor());

        return emision.setScale(2, RoundingMode.HALF_UP); //Redondeo
    }

    // =========== CÁLCULO DE HUELLA POR EMPRESA ==============

    /**
     * Calcula la huella total de una empresa sumando todos sus departamentos para un mes y año.
     *
     * @param empresa Empresa de la que calcular la huella
     * @param mes     Mes del período (1-12)
     * @param anio    Año del período
     * @return Total de emisiones en kgCO2e con redondedo con 2 decimales
     */
    public BigDecimal getHuellaTotalEmpresaMes(Empresa empresa, int mes, int anio) {
        // Recupera los departamentos de la empresa
        List<Departamento> departamentos = departamentoDAO.findAllByEmpresa(empresa.getIdEmpresa());

        BigDecimal totalEmpresa = BigDecimal.ZERO;
        for (Departamento departamento : departamentos) {
            //Añade el consumo total de cada departamento para el mes indicado
            totalEmpresa = totalEmpresa.add(getHuellaTotalDepartamentoMes(departamento, mes, anio));
        }
        return totalEmpresa.setScale(2, RoundingMode.HALF_UP); 
    }

    // =========== DESGLOSE POR SCOPE ==============

    /**
     * Calcula la huella de un departamento en un mes desglosada por Scope (1, 2 y 3).
     *
     * @param departamento Departamento del que calcular la huella
     * @param mes          Mes del período (1-12)
     * @param anio         Año del período
     * @return Mapa con clave = scope (1, 2 o 3) y valor = kgCO2e de ese scope para el mes indicado
     */
    public Map<Integer, BigDecimal> getHuellaPorScope(Departamento departamento, int mes, int anio) {
        Map<Integer, BigDecimal> resultado = new HashMap<>();
        resultado.put(1, BigDecimal.ZERO);
        resultado.put(2, BigDecimal.ZERO);
        resultado.put(3, BigDecimal.ZERO);

        // Scope 1 y 2: según el alcance del factor de cada consumo
        List<ConsumoMensual> consumos = consumoDAO.getConsumosDepartamentoMes(
                departamento.getIdDepartamento(), mes, anio);
        for (ConsumoMensual consumo : consumos) {
            // Extraer el alcance al que pertenece el consumo
            int scope = consumo.getFactorEmision().getAlcance();
            // Añadir al total del alcance la emisión de este consumo
            resultado.merge(scope, consumo.calcularEmision(), BigDecimal::add);
        }

        // Scope 3: commuting (solo si está habilitado)
        if (departamento.isIncluirAlcance3()) {
            List<CommutingEmpleado> commutings = commutingDAO.getCommutingsDepartamentoMes(
                    departamento.getIdDepartamento(), mes, anio);
            for (CommutingEmpleado commuting : commutings) {
                // Añadir la emisión de cada commuting de cada empleado del departamento
                resultado.merge(3, calcularEmisionCommuting(commuting), BigDecimal::add);
            }
        }

        resultado.replaceAll((scope, valor) -> valor.setScale(2, RoundingMode.HALF_UP));
        return resultado;
    }

    // =========== CÁLCULO ANUAL ==============

    /**
     * Calcula la huella anual de un departamento sumando los 12 meses del año.
     *
     * @param departamento Departamento del que calcular la huella
     * @param anio         Año del cálculo
     * @return Total de emisiones anuales en kgCO2e con 2 decimales
     */
    public BigDecimal getHuellaAnualDepartamento(Departamento departamento, int anio) {
        BigDecimal totalAnual = BigDecimal.ZERO;
        for (int mes = 1; mes <= 12; mes++) {
            //suma la huella de cada mes del departamento
            totalAnual = totalAnual.add(getHuellaTotalDepartamentoMes(departamento, mes, anio));
        }
        return totalAnual.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la huella anual de una empresa sumando todos sus departamentos en los 12 meses.
     *
     * @param empresa Empresa de la que calcular la huella
     * @param anio    Año del cálculo
     * @return Total de emisiones anuales en kgCO2e con 2 decimales
     */
    public BigDecimal getHuellaAnualEmpresa(Empresa empresa, int anio) {
        List<Departamento> departamentos = departamentoDAO.findAllByEmpresa(empresa.getIdEmpresa());

        BigDecimal totalAnual = BigDecimal.ZERO;
        for (Departamento departamento : departamentos) {
            //Suma la huella anual de cada departamento
            totalAnual = totalAnual.add(getHuellaAnualDepartamento(departamento, anio));
        }
        return totalAnual.setScale(2, RoundingMode.HALF_UP);
    }

    // =========== VARIACIÓN MENSUAL ==============

    /**
     * Calcula la variación de emisiones de un departamento respecto al mes anterior.
     * Un valor positivo indica más emisiones, negativo indica mejora.
     *
     * @param departamento Departamento a comparar
     * @param mes          Mes actual (1-12)
     * @param anio         Año actual
     * @return Diferencia en kgCO2e (actual - anterior)
     */
    public BigDecimal getVariacionMensual(Departamento departamento, int mes, int anio) {
        // Obtener el mes anterior
        int mesAnterior = (mes == 1) ? 12 : mes - 1;
        int anioAnterior = (mes == 1) ? anio - 1 : anio;
        
        // huellas de ambos meses
        BigDecimal huellaActual = getHuellaTotalDepartamentoMes(departamento, mes, anio);
        BigDecimal huellaAnterior = getHuellaTotalDepartamentoMes(departamento, mesAnterior, anioAnterior);
        
        // Redondeo
        return huellaActual.subtract(huellaAnterior).setScale(2, RoundingMode.HALF_UP);
    }

}