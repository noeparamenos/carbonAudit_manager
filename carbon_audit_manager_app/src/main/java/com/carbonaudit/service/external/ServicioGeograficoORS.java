package com.carbonaudit.service.external;

import com.carbonaudit.model.Direccion;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


/**
 * Implementación del servicio geográfico utilizando la API de OpenRouteService (ORS).
 * Se encarga de obtener las Coordenadas de una dirección y distancias entre direcciones
 */
public class ServicioGeograficoORS implements IServicioGeografico {

    private final String apiKey;
    private final HttpClient httpClient;
    private static final String BASE_URL = "https://api.openrouteservice.org";

    public ServicioGeograficoORS() {
        // Cargamos la clave desde el archivo .env
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("ORS_API_KEY");
        this.httpClient = HttpClient.newHttpClient();
    }
    /**
     * Convierte una dirección de texto en coordenadas GPS.
     * Y se las asinga al los campos del objeto direccion pasado por parámetro
     * @param dir Direccion de la que se quieren calcular las coordenadas
     */
    @Override
    public void completarCoordenadas(Direccion dir) throws Exception {
        // Extrae los datos de la direccion y los formateamos
        String textoDireccion = String.format("%s %d, %s, %s",
                dir.getCalle(),
                dir.getNumero(),
                dir.getCiudad(),
                dir.getCodigoPostal());
        String direccionCodificada = URLEncoder.encode(textoDireccion, StandardCharsets.UTF_8);

        // Construccion de la URL del endpoint de Geocoding
        String url = BASE_URL + "/geocode/search?api_key=" + apiKey + "&text=" + direccionCodificada + "&limit=1";
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        // Preparacion y envio de la peticion GET
        HttpResponse<String> respuesta = httpClient.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 200) {
            // Parseo de la respuesta usando GSON
            JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            JsonArray features = json.getAsJsonArray("features");

            if (features.size() > 0) {
                //  ORS devuelve las coordenadas en orden [1.Longitud, 2.Latitud]
                JsonArray coords = features.get(0).getAsJsonObject()
                        .getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates");

                // Asignamos las cordenadas a la direccion (inyeccion por parámetro)
                dir.setLongitud(coords.get(0).getAsBigDecimal()); //1.Longitud
                dir.setLatitud(coords.get(1).getAsBigDecimal()); //2. Latitud
            } else {
                throw new Exception("No se encontraron coordenadas para la dirección: " + textoDireccion);
            }
        } else {
            throw new Exception("Error en la API de Geocoding (Status: " + respuesta.statusCode() + ")");
        }
    }

    /**
     * Calcula la distancia por carretera entre 2 puntos
     * @param origen Direccion del empleado
     * @param destino Dirección del departamento o sede de la empresa.
     * @return La distancia entre dos puntos en km con 2 decimales(ej. 12.30)
     * @throws Exception
     */
    @Override
    public BigDecimal calcularDistancia(Direccion origen, Direccion destino) throws Exception {
        // Verificamos que la direccion contiene posicionamiento
        if (origen.getLatitud() == null || destino.getLatitud() == null) {
            throw new Exception("Las direcciones deben tener coordenadas para calcular la ruta.");
        }

        // Endpoint de rutas de ORS (driving-car)
        // formato: &start=long,lat&end=long,lat
        String url = String.format("%s/v2/directions/driving-car?api_key=%s&start=%s,%s&end=%s,%s",
                BASE_URL, apiKey,
                origen.getLongitud(), origen.getLatitud(),
                destino.getLongitud(), destino.getLatitud());

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        // Preparacion y envio de la peticion GET
        HttpResponse<String> respuesta = httpClient.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            // Accedemos a la distancia en metros
            double metros = json.getAsJsonArray("features")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("properties")
                    .getAsJsonObject("summary")
                    .get("distance").getAsDouble();

            // Convertimos metros a Km (Metros / 1000) con 2 decimales
            return BigDecimal.valueOf(metros)
                    .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        } else {
            throw new Exception("Error en la API de Rutas (Status: " + respuesta.statusCode() + ")");
        }
    }
}