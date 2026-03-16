package com.carbonaudit.model;

import java.math.BigDecimal;

/**
 * Representa el registro de consumo energético de un departamento.
 * Es la base para los cálculos de Alcance 1 y 2.
 */
public class ConsumoMensual {

    private int idConsumo;
    private BigDecimal cantidad;
    private int mes;
    private int anio;

    // RELACIONES COMPOSICION
    private Departamento departamento;
    private FactorEmision factorEmision;

    public ConsumoMensual() {}

    // Constructor útil para la lógica de negocio
    public ConsumoMensual(BigDecimal cantidad, int mes, int anio, Departamento departamento, FactorEmision factorEmision) {
        this.cantidad = cantidad;
        this.mes = mes;
        this.anio = anio;
        this.departamento = departamento;
        this.factorEmision = factorEmision;
    }


    // ==== GETTERS Y SETTERS ====

    public int getIdConsumo() { return idConsumo; }
    public void setIdConsumo(int idConsumo) { this.idConsumo = idConsumo; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    public FactorEmision getFactorEmision() { return factorEmision; }
    public void setFactorEmision(FactorEmision factorEmision) { this.factorEmision = factorEmision; }

    @Override
    public String toString() {
        return "Consumo[" + anio + "-" + mes + "]: " + cantidad + " " +
                (factorEmision != null ? factorEmision.getUnidad() : "");
    }

    // ======== METODOS DE DOMINIO ==========
    /**
     * Calcula la huella de CO2 de este registro específico.
     * Multiplica la cantidad por el valor del factor asociado.
     */
    public BigDecimal calcularEmision() {
        if (cantidad != null && factorEmision != null && factorEmision.getValorFactor() != null) {
            return cantidad.multiply(factorEmision.getValorFactor());
        }
        return BigDecimal.ZERO;
    }
}