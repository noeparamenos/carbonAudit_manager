package com.carbonaudit.test;

import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioCalculoHuella;
import com.carbonaudit.service.external.IServicioGeografico;
import com.carbonaudit.service.external.ServicioGeograficoORS;

import java.math.BigDecimal;

public class TestServicioCalculoCommuting {
    public static void main(String[] args) {
        try {

            IServicioGeografico servicioGeografico = new ServicioGeograficoORS();
            Empleado empleado;
            Departamento departamento;
            Direccion direccionEmpl;
            Direccion direccionDep;
            FactorEmision factorEmision;

            ServicioCalculoHuella calculoService = new ServicioCalculoHuella(servicioGeografico);

            direccionEmpl = new Direccion("Calle La Laguna", 6, "La Mata del Paramo", "24008");
            direccionDep = new Direccion("Calle Manuel Llaneza", 3, "Oviedo", "33010");

            // Asignacion de posicionamiento
            servicioGeografico.completarCoordenadas(direccionDep);
            servicioGeografico.completarCoordenadas(direccionEmpl);
            System.out.println("Coordenadas departamento: " + direccionDep.getLatitud() + " " + direccionDep.getLongitud());
            System.out.println("Coordenadas empleado: " + direccionEmpl.getLatitud() + " " + direccionEmpl.getLongitud());


            departamento = new Departamento("RRHH", direccionDep, new Empresa());

            factorEmision = new FactorEmision("Coche Gasolina Medio", "km", new BigDecimal("0.13700"), 3);

            empleado = new Empleado("Pepe", factorEmision, 20, direccionEmpl, departamento);


            calculoService.AsignarDistanciaTrabajo(empleado);


            // test geolocalizacion
            System.out.println("Calculando distancia entre casa y oficina...");
            System.out.println("Distancia detectada: " + empleado.getDistanciaTrabajo() + " km");

            // test calculo Commuting
            BigDecimal huellaMensual = calculoService.getCommutingMensual(empleado);

            System.out.println("Huella para " + empleado.getDiasPresenciales() + " días: " + huellaMensual + " kgCO2e");


        } catch (Exception e) {
            System.err.println("ERROR EN EL TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }
}