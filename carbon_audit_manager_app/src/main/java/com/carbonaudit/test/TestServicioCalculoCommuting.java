package com.carbonaudit.test;

import com.carbonaudit.model.*;
import com.carbonaudit.service.ServicioCalculoHuella;
import com.carbonaudit.service.external.ServicioGeograficoORS;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestServicioCalculoCommuting {
    public static void main(String[] args) {
        try {

            ServicioGeograficoORS servicioGeografico = new ServicioGeograficoORS();
            ServicioCalculoHuella calculoService     = new ServicioCalculoHuella();

            Direccion direccionEmpl = new Direccion("Calle La Laguna", 6, "La Mata del Paramo", "24008");
            Direccion direccionDep  = new Direccion("Calle Manuel Llaneza", 3, "Oviedo", "33010");

            servicioGeografico.completarCoordenadas(direccionDep);
            servicioGeografico.completarCoordenadas(direccionEmpl);
            System.out.println("Coordenadas departamento: " + direccionDep.getLatitud() + " " + direccionDep.getLongitud());
            System.out.println("Coordenadas empleado: " + direccionEmpl.getLatitud() + " " + direccionEmpl.getLongitud());

            Departamento departamento = new Departamento("RRHH", direccionDep, new Empresa());
            FactorEmision factorEmision = new FactorEmision("Coche Gasolina Medio", "km", new BigDecimal("0.13700"), 3);
            Empleado empleado = new Empleado("Pepe", LocalDate.now(), factorEmision, 20, direccionEmpl, departamento);

            // Calcular distancia vía geoService y asignar al empleado
            empleado.setDistanciaTrabajo(servicioGeografico.calcularDistancia(direccionEmpl, direccionDep));


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