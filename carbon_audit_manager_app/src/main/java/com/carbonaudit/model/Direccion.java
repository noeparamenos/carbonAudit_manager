package com.carbonaudit.model;

import java.math.BigDecimal;

/**
 * Representa una ubicación física para el cálculo de movilidad y logística.
 */
public class Direccion {

    // ========= CAMPOS DE CASE ============

    private int idDireccion; //id único

    // Información de la dirección a especificar
    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String provincia;

    // Información para calcular rutas
    private BigDecimal latitud;
    private BigDecimal longitud;

    // ==== CONSTRUCTORES ======

    public Direccion() {}

    public Direccion(String calle, String ciudad, String codigoPostal) {
        this.calle = calle;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
    }

    // ==== GETTERS Y SETTERS ===================

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public int getIdDireccion() { return idDireccion; }
    public void setIdDireccion(int idDireccion) { this.idDireccion = idDireccion; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }

    @Override
    public String toString() {
        return calle + ", " + ciudad + " (" + codigoPostal + ")";
    }


}