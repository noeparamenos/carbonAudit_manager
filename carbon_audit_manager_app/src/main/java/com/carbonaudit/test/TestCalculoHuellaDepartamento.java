package com.carbonaudit.test;

import com.carbonaudit.dao.*;
import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioCalculoHuella;
import com.carbonaudit.service.external.ServicioGeograficoORS;

import java.math.BigDecimal;

/**
 * Test autocontenido del cálculo de huella de carbono total de un departamento.
 *
 * Flujo:
 * 1. Crea datos (empresa, departamento, empleados, consumos, commuting)
 * 2. Ejecuta cálculos de huella
 * 3. Verifica resultados manualmente
 * 4. Limpia toda la información de la BD (en orden inverso de FK)
 */
public class TestCalculoHuellaDepartamento {

    public static void main(String[] args) {
        try {

            System.out.println("=========TEST: Cálculo de Huella de Carbono - Departamento=============\n");

            // Inicializar DAOs Para la creación y consulta en la BD
            DireccionDAO direccionDAO = new DireccionDAO();
            EmpresaDAO empresaDAO = new EmpresaDAO();
            DepartamentoDAO departamentoDAO = new DepartamentoDAO();
            FactorEmisionDAO factorDAO = new FactorEmisionDAO();
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            ConsumoMensualDAO consumoDAO = new ConsumoMensualDAO();
            CommutingEmpleadoDAO commutingDAO = new CommutingEmpleadoDAO();

            // Servicio Para cálculo de la huella
            ServicioCalculoHuella servicioHuella = new ServicioCalculoHuella(new ServicioGeograficoORS());

            System.out.println("Factores de emisión anteriores eliminados");

            // --------------------------------------------------------
            // 1: CREAR DATOS DE PRUEBA

            System.out.println("------FASE 1: Creando datos de prueba... ----------\n");

            // Crear dirección de la empresa
            Direccion dirEmpresa = new Direccion("Calle Principal", 100, "Madrid", "28001");
            dirEmpresa = direccionDAO.create(dirEmpresa);
            System.out.println("Dirección empresa creada:" + dirEmpresa.getCalle() + " - Nº " + dirEmpresa.getNumero());

            // Crear empresa
            Empresa empresa = new Empresa("TechCorp S.L.", "A12345678", dirEmpresa);
            empresa.setTelefono("915555555");
            empresa.setEmail("info@techcorp.com");
            empresa.setSector("Tecnología");
            empresa = empresaDAO.create(empresa);
            System.out.println("Empresa creada: " + empresa.getNombreSocial());

            // Crear departamento con la misma direccion de la empresa
            Departamento departamento = new Departamento("Desarrollo", dirEmpresa, empresa);
            departamento.setDescripcion("Equipo de desarrollo de software");
            departamento.setIncluirAlcance3(true);
            departamento = departamentoDAO.create(departamento);
            System.out.println("Departamento creado: " + departamento.getNombre());
            System.out.println("   - Scope 3 habilitado: " + departamento.isIncluirAlcance3());


            // Crear factor de emisión para electricidad (Scope 2)
            FactorEmision factorElectricidad = new FactorEmision("Electricidad Mix España", "kWh", new BigDecimal("0.275"), 2);
            factorElectricidad = factorDAO.create(factorElectricidad);
            System.out.println("Factor emisión creado: "+ factorElectricidad.getNombre());

            // Crear factor de emisión para transporte (Scope 3)
            FactorEmision factorCocheGasolina = new FactorEmision("Coche Gasolina", "km", new BigDecimal("0.137"), 3);
            factorCocheGasolina = factorDAO.create(factorCocheGasolina);
            System.out.println("Factor emisión creado: " + factorCocheGasolina.getNombre());

            // Crear dirección de empleado
            Direccion dirEmpleado = new Direccion("Calle Casa", 10, "Fuenlabrada", "28944");
            dirEmpleado = direccionDAO.create(dirEmpleado);
            System.out.println("Dirección empleado creada: " + dirEmpleado.getCalle()+ "- Nª " + dirEmpleado.getNumero() + ")");

            // Crear empleado
            Empleado empleado = new Empleado("Juan García", factorCocheGasolina, 20, dirEmpleado, departamento);
            empleado = empleadoDAO.create(empleado);
            System.out.println("Empleado creado:" + empleado.getNombre());

            // Crear consumo mensual (factura de electricidad)
            ConsumoMensual consumo = new ConsumoMensual(
                    new BigDecimal("500"),
                    3,
                    2024,
                    departamento,
                    factorElectricidad
            );

            consumo = consumoDAO.create(consumo);
            System.out.println("Consumo creado: " + factorElectricidad.getNombre() + "---" + consumo.getMes() + "/" + consumo.getAnio());

            // Crear registro de commuting (Scope 3)
            CommutingEmpleado commuting = new CommutingEmpleado(
                    empleado,
                    factorCocheGasolina,
                    new BigDecimal("25"),
                    20,
                    3,
                    2024
            );

            commutingDAO.create(commuting);
            System.out.println("OK - Commuting creado: " + empleado.getNombre() + " - " +
                    commuting.getDistanciaDiariaKm() + " km/día × " + commuting.getDiasPresencialesMes() + " días");

            System.out.println();

            // -------------------------------------------------------------
            // FASE 2: CALCULAR HUELLA Y VERIFICAR

            System.out.println();
            System.out.println("----FASE 2: Calculando huella...\n");

            int mes = 3;
            int anio = 2024;

            // Cálculo manual
            BigDecimal scope2 = new BigDecimal("500").multiply(new BigDecimal("0.275"));  // 137.5 kgCO2e
            BigDecimal scope3 = new BigDecimal("25")
                    .multiply(new BigDecimal("2"))           // Ida y vuelta
                    .multiply(new BigDecimal("20"))          // Días presenciales
                    .multiply(new BigDecimal("0.137"));      // Factor: 137 kgCO2e

            //Redondeos
            scope2 = scope2.setScale(2, java.math.RoundingMode.HALF_UP);
            scope3 = scope3.setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal totalEsperado = scope2.add(scope3).setScale(2, java.math.RoundingMode.HALF_UP);

            // Cálculo mediante servicio
            BigDecimal huellaTotal = servicioHuella.getHuellaTotalDepartamentoMes(departamento, mes, anio);

            System.out.println(" - Resultados del cálculo:");
            System.out.println("    Scope 2 (Electricidad): " + scope2 + " kgCO2e");
            System.out.println("    Scope 3 (Commuting):    " + scope3 + " kgCO2e");
            System.out.println("   -------------------------");
            System.out.println("   TOTAL ESPERADO:         " + totalEsperado + " kgCO2e");
            System.out.println("   TOTAL CALCULADO:        " + huellaTotal + " kgCO2e");
            System.out.println();

            // Verificación
            if (huellaTotal.equals(totalEsperado)) {
                System.out.println("  OK: Los cálculos coinciden");
            } else {
                System.out.println(" !! ERROR: Los cálculos no coinciden");
                System.out.println("   Diferencia: " + huellaTotal.subtract(totalEsperado));
            }

            System.out.println();

            // ------------------------------------------------------
            // FASE 3: LIMPIAR DATOS (orden inverso de FK)

            System.out.println("-----------FASE 3: Limpiando datos de prueba... --------\n");

            // 1. Borrar commutings
            commutingDAO.delete(empleado.getIdEmpleado(), mes, anio);
            System.out.println("Commuting eliminado");

            // 2. Borrar consumos
            consumoDAO.delete(consumo.getIdConsumo());
            System.out.println("Consumo eliminado");

            // 3. Borrar empleado
            empleadoDAO.delete(empleado.getIdEmpleado());
            System.out.println("Empleado eliminado");

            // 4. Borrar departamento
            departamentoDAO.delete(departamento.getIdDepartamento());
            System.out.println("Departamento eliminado");

            // 5. Borrar empresa
            empresaDAO.delete(empresa.getIdEmpresa());
            System.out.println("Empresa eliminada");

            // 6. Borrar direcciones
            direccionDAO.delete(dirEmpresa.getIdDireccion());
            direccionDAO.delete(dirEmpleado.getIdDireccion());
            System.out.println("Direcciones eliminadas");

            // 7. Borrar factores de emisión (los que creamos)
            factorDAO.delete(factorElectricidad.getIdFactor());
            factorDAO.delete(factorCocheGasolina.getIdFactor());
            System.out.println("Factores de emisión eliminados");

            System.out.println();
            System.out.println("------------------- TEST COMPLETADO OK ---------------------");

        } catch (Exception e) {
            System.err.println(" !! ERROR EN EL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}