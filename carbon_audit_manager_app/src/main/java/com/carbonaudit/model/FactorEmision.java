package com.carbonaudit.model;

import java.math.BigDecimal;

/**
 * Contiene los coeficientes oficiales para transformar consumos en kgCO2e.
 * Basado en el estándar GHG Protocol.
 */
public class FactorEmision {

    private int idFactor;
    private String nombre;      // Ej: "Electricidad Mix España", "Diesel B7"
    private String unidad;      // Ej: "kWh", "km", "litros"
    private BigDecimal valorFactor; // DECIMAL(10,5) para alta precisión
    private int alcance;        // 1, 2 o 3

    // ============= CONSTRUCTORES =============================

    public FactorEmision() {}

    // Constructor para inicializar factores rápidamente
    public FactorEmision(String nombre, String unidad, BigDecimal valorFactor, int alcance) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.valorFactor = valorFactor;
        this.alcance = alcance;
    }

    // ==== GETTERS Y SETTERS ====

    public int getIdFactor() { return idFactor; }
    public void setIdFactor(int idFactor) { this.idFactor = idFactor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getValorFactor() { return valorFactor; }
    public void setValorFactor(BigDecimal valorFactor) { this.valorFactor = valorFactor; }

    public int getAlcance() { return alcance; }
    public void setAlcance(int alcance) { this.alcance = alcance; }

    @Override
    public String toString() {
        return nombre + " (" + valorFactor + " kgCO2e/" + unidad + ")";
    }
}