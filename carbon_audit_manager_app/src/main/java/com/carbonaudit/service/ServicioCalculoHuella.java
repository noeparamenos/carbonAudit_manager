package com.carbonaudit.service;



import com.carbonaudit.model.*;
import com.carbonaudit.dao.*; // Importas tus DAOs listos
import com.carbonaudit.service.external.IServicioGeografico;

import java.math.BigDecimal;

public class ServicioCalculoHuella {


    private final IServicioGeografico geoService;
    private final DireccionDAO direccionDao;
    private final FactorEmisionDAO factorDao;

    // Constructor (Inyección de dependencias)
    public ServicioCalculoHuella(IServicioGeografico geoService, DireccionDAO direccionDao, FactorEmisionDAO factorDao) {
        this.geoService = geoService;
        this.direccionDao = direccionDao;
        this.factorDao = factorDao;
    }

    // =========== COMMUTING DE EMPLEADOS ==============

    /**
     * Valida que todos los campos esten presentes para evitar NullPoniterException
     * @param emp a validar los datos
     */
    private void validarDatosEmpleado(Empleado emp) {
        // 1. Validaciones de seguridad
        if (emp.getDistanciaTrabajo() == null) {
            throw new IllegalStateException("El empleado no tiene calculada la distancia al trabajo. Ejecuta calcularYAsignarDistanciaCommuting primero.");
        }
        if (emp.getMedioTransporte() == null || emp.getMedioTransporte().getValorFactor() == null) {
            throw new IllegalStateException("El empleado no tiene un medio de transporte o factor de emisión válido asignado.");
        }
    }

    /**
     * Procesa una dirección: busca coordenadas y la guarda en DB.
     * @param dir de la que queremos obtener las coordenadas
     * @return La direccion con las coordenadas actualizadas
     */
    public Direccion registrarDireccion(Direccion dir) throws Exception {
        // 1. Usamos el servicio externo para completar lat/lon
        geoService.completarCoordenadas(dir);

        // Persistir la direccion en la BD
        direccionDao.create(dir);
        return dir;
    }

    /**
     * Calcula la distancia real entre la casa del empleado y su departamento de trabajo,
     * actualiza el campo km
     *
     * @param empleado El empleado a procesar.
     * @throws Exception Si faltan datos o falla la conexión con el servicio de mapas.
     */
    public void AsignarDistanciaCommuting(Empleado empleado) throws Exception {

        validarDatosEmpleado(empleado);

        // Datos de direcciones
        Direccion dirEmpleado = empleado.getDireccion();
        Direccion dirDepartamento = empleado.getDepartamento().getDireccion();

        // Llamamos a la API de Geocoding si la dirección aún no tiene coordenadas.
        if (dirEmpleado.getLatitud() == null || dirEmpleado.getLongitud() == null) {
            geoService.completarCoordenadas(dirEmpleado);
        }
        if (dirDepartamento.getLatitud() == null || dirDepartamento.getLongitud() == null) {
            geoService.completarCoordenadas(dirDepartamento);
        }

        // Calculo y actualizacion de km al trabajo
        BigDecimal distanciaEnKm = geoService.calcularDistancia(dirEmpleado, dirDepartamento);
        empleado.setDistanciaTrabajo(distanciaEnKm);
    }

    /**
     * Calcula y devuelve la huella generada por un empleado al mes en su transporte al trabajo.
     * Resultado expresado en kgCO2e.
     * @param emp Empleado del que se quiere calcular el commuting mensual
     */
    public BigDecimal getCommutingMensual(Empleado emp) throws Exception {

        validarDatosEmpleado(emp);

        // KM totales realizados en un mes (Ida y vuelta * días)
        BigDecimal kmTotalesMes = emp.getDistanciaTrabajo()
                .multiply(new BigDecimal("2"))
                .multiply(new BigDecimal(emp.getDiasPresenciales()));

        // 3. Resultado: km * factor_conversion
        BigDecimal emisiones = kmTotalesMes.multiply(emp.getMedioTransporte().getValorFactor());

        // 4. Redondeo a 2 decimales para la vista del usuario
        return emisiones.setScale(2, java.math.RoundingMode.HALF_UP);
    }


}