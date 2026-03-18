package com.carbonaudit.test;

import com.carbonaudit.dao.ConsumoMensualDAO;
import com.carbonaudit.dao.DireccionDAO;
import com.carbonaudit.dao.EmpresaDAO;
import com.carbonaudit.dao.FactorEmisionDAO;
import com.carbonaudit.model.ConsumoMensual;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empresa;
import com.carbonaudit.model.FactorEmision;

import java.math.BigDecimal;

public class MainTestDao {
    public static void main(String[] args) {
        // 1. Instanciamos los DAOs necesarios
        DireccionDAO direccionDAO = new DireccionDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();
        FactorEmisionDAO factorDAO = new FactorEmisionDAO();
        ConsumoMensualDAO consumoDAO = new ConsumoMensualDAO();

        try {
            System.out.println("--- INICIANDO TEST DE PERSISTENCIA ---");

            //  Dirección
            Direccion dir = new Direccion();
            dir.setCalle("Calle Innovación");
            dir.setNumero(101);
            dir.setCiudad("Madrid");
            dir.setCodigoPostal("24248");
            dir = direccionDAO.create(dir);
            System.out.println("✅ Dirección creada con ID: " + dir.getIdDireccion());

            // 3. TEST: Crear Empresa vinculada
            Empresa emp = new Empresa();
            emp.setNombreSocial("EcoCorp S.A.");
            emp.setCif("A12345678");
            emp.setDireccion(dir); // Pasamos el objeto con el ID ya generado
            emp = empresaDAO.create(emp);
            System.out.println("✅ Empresa 'EcoCorp' creada con ID: " + emp.getIdEmpresa());

            // 4. TEST: Crear un Factor de Emisión (Electricidad)
            FactorEmision luz = new FactorEmision();
            luz.setNombre("Electricidad Mix España");
            luz.setUnidad("kWh");
            luz.setValorFactor(new BigDecimal("0.25900")); // kgCO2e/kWh
            luz.setAlcance(2);
            luz = factorDAO.create(luz);
            System.out.println("✅ Factor de Emisión creado: " + luz.getNombre());

            // 5. TEST: Forzar error de validación (Mes inexistente)
            System.out.println("--- Probando validador de mes ---");
            try {
                ConsumoMensual error = new ConsumoMensual();
                error.setMes(13); // Esto debería lanzar IllegalArgumentException
                consumoDAO.create(error);
            } catch (IllegalArgumentException e) {
                System.out.println("✅ El validador funciona: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("ERROR EN EL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}