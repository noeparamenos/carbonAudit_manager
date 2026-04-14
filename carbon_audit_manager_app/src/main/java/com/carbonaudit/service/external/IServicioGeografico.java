package com.carbonaudit.service.external;

import com.carbonaudit.model.Direccion;
import java.math.BigDecimal;

/**
 * Interfaz para servicios de geolocalización y rutas.
 * Define los métods necesarios para interactuar con APIs externas (ej OpenRouteService, google...)
 */
public interface IServicioGeografico {

    /**
     * Obtiene y asigna la latitud y longitud a un objeto Direccion.
     * * @param direccion El objeto direccion a completar.
     * @throws Exception Si la dirección no se encuentra o hay errores de red.
     */
    void completarCoordenadas(Direccion direccion) throws Exception;

    /**
     * Calcula la distancia real por carretera entre dos direcciones
     * * @param origen Dirección de residencia del empleado.
     * @param destino Dirección del departamento o sede de la empresa.
     * @return Distancia en kilómetros expresada en BigDecimal para máxima precisión.
     * @throws Exception Si no se puede calcular la ruta.
     */
    BigDecimal calcularDistancia(Direccion origen, Direccion destino) throws Exception;
}