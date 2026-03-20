package com.carbonaudit.service.external;

import com.carbonaudit.model.Direccion;
import java.math.BigDecimal;

public class TestGeolocalizacion {
    public static void main(String[] args) {
        // 1. Instanciamos el servicio (carga .env con la key del API automáticamente)
        IServicioGeografico geoService = new ServicioGeograficoORS();

        try {
            System.out.println("--- PRUEBA 1: GEOLOCALIZACIÓN ---");
            // Simulamos una dirección (Asegúrate de que tu POJO Direccion tenga estos campos)
            String[][] direcciones = {
                    {"Calle de España", "15", "Madrid", "28001"},
                    {"Calle de Atocha", "10", "Madrid", "28012"},
                    {"Avenida de la Transición Española", "13", "Alcobendas", "28108"}
            };

            for (String[] d : direcciones) {
                Direccion dir = new Direccion(d[0], Integer.parseInt(d[1]), d[2], d[3]);
                geoService.completarCoordenadas(dir);
                System.out.println("Validada: " + d[0] + " -> " + dir.getLatitud() + ", " + dir.getLongitud());
            }

            // Calle, Numero, Ciudad, CP
            Direccion casaEmpleado = new Direccion("Calle del Pez", 10, "Madrid", "28004");

            System.out.println("Buscando coordenadas para: " + casaEmpleado.getCalle() + " " + casaEmpleado.getNumero());

            geoService.completarCoordenadas(casaEmpleado);

            System.out.println("Resultado -> Latitud: " + casaEmpleado.getLatitud() +
                    " | Longitud: " + casaEmpleado.getLongitud());

            System.out.println("\n--- PRUEBA 2: CÁLCULO DE RUTA (COMMUTING) ---");
            // Creamos una segunda dirección (la oficina)
            Direccion oficina = new Direccion("Paseo de la Castellana", 200, "Madrid", "28046");
            geoService.completarCoordenadas(oficina);

            // Calculamos la distancia por carretera
            BigDecimal distanciaKm = geoService.calcularDistancia(casaEmpleado, oficina);

            System.out.println("Distancia real por carretera: " + distanciaKm + " km");
            System.out.println("Prueba superada con éxito. Datos listos para auditoría.");

        } catch (Exception e) {
            System.err.println("ERROR EN LA PRUEBA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}