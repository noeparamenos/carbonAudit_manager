package com.carbonaudit.test;

import com.carbonaudit.dao.*;
import com.carbonaudit.model.*;

import java.math.BigDecimal;
import java.util.Optional;

public class MainTestDao {


    public static void main(String[] args) {
        // 1. Instanciamos los DAOs necesarios
        DireccionDAO direccionDAO = new DireccionDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();
        FactorEmisionDAO factorDAO = new FactorEmisionDAO();
        ConsumoMensualDAO consumoDAO = new ConsumoMensualDAO();
        DepartamentoDAO departamentoDAO =  new DepartamentoDAO();
        EmpleadoDAO empleadoDAO = new EmpleadoDAO();

        try {
            System.out.println("--- INICIANDO TEST DE PERSISTENCIA ---");

            //  Dirección
            Direccion direccion = new Direccion();
            direccion.setCalle("Calle Innovación");
            direccion.setNumero(101);
            direccion.setCiudad("Madrid");
            direccion.setCodigoPostal("24248");
            direccion = direccionDAO.create(direccion);
            System.out.println("Dirección creada con ID: " + direccion.getIdDireccion());


            // Crear Empresa
            Empresa empresa = new Empresa();
            empresa.setNombreSocial("Noe S.A.");
            empresa.setCif("A123457778");
            empresa.setDireccion(direccion); // Pasamos el objeto con el ID ya generado
            empresa = empresaDAO.create(empresa);
            System.out.println("Empresa "+empresa.getNombreSocial()+"creada con ID: " + empresa.getIdEmpresa());

            // Crear Departamento
            Departamento departamento  = new Departamento();

            departamento.setDireccion(direccion);
            departamento.setNombre("RR");
            departamento.setEmpresa(empresa);
            departamento = departamentoDAO.create(departamento);
            System.out.println("Departamento "+departamento.getNombre()+" de la empresa "+empresa.getNombreSocial()
                    +"creado con id "+departamento.getIdDepartamento());

            // Crear un Factor de Emisión (Electricidad)
            FactorEmision factorEmision = new FactorEmision();

            factorEmision.setNombre("Gas Natural");
            factorEmision.setUnidad("kWh");
            factorEmision.setValorFactor(new BigDecimal("0.25900")); // kgCO2e/kWh
            factorEmision.setAlcance(3);
            factorEmision = factorDAO.create(factorEmision);
            System.out.println("Factor de Emisión creado: " + factorEmision.getNombre());


            // Crear Empleado
            Empleado empleado = new Empleado();
            empleado.setNombre("Pedro Piqueras");
            empleado.setDireccion(direccion);
            empleado.setMedioTransporte(factorEmision);
            empleado.setDepartamento(departamento);
            empleado.setDiasPresenciales(20);
            empleado = empleadoDAO.create(empleado);
            System.out.println("Empleado "+empleado.getNombre()+" creado con id: "+empleado.getIdEmpleado());


            // TEST: Forzar error de validación (Mes inexistente)
            System.out.println("--- Probando validador de mes ---");
            try {
                ConsumoMensual error = new ConsumoMensual();
                error.setMes(13); // Esto debería lanzar IllegalArgumentException
                consumoDAO.create(error);
            } catch (IllegalArgumentException e) {
                System.out.println("El validador funciona: " + e.getMessage());
            }

            // Borrado

            empleadoDAO.delete(empleado.getIdEmpleado());
            factorDAO.delete(factorEmision.getIdFactor());
            departamentoDAO.delete(departamento.getIdDepartamento());
            empresaDAO.delete(empresa.getIdEmpresa());
            direccionDAO.delete(direccion.getIdDireccion());

        } catch (Exception e) {
            System.err.println("ERROR EN EL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}