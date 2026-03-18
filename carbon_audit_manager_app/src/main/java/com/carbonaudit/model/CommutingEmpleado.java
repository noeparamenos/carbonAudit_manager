package com.carbonaudit.model;

import java.math.BigDecimal;

/**
 * Registra la actividad de desplazamiento (commuting) de un empleado
 * en un mes y año específicos. Fundamental para el Alcance 3.
 */
public class CommutingEmpleado {

    // RELACIONES COMO OBJETOS (Composición)
    private Empleado empleado;       // Quién se desplaza
    private FactorEmision medioTransporte; // Qué medio usa (Coche, Bus, etc.)

    // Datos del un mes concreto
    private BigDecimal distanciaDiariaKm;
    private int diasPresencialesMes;
    private int mes;
    private int anio;

    // ============ CONSTRUCTORES ===============

    public CommutingEmpleado() {}

    public CommutingEmpleado(Empleado empleado, FactorEmision medioTransporte,
                             BigDecimal distanciaDiariaKm, int diasPresencialesMes,
                             int mes, int anio) {
        this.empleado = empleado;
        this.medioTransporte = medioTransporte;
        this.distanciaDiariaKm = distanciaDiariaKm;
        this.diasPresencialesMes = diasPresencialesMes;
        this.mes = mes;
        this.anio = anio;
    }

    // ==== GETTERS Y SETTERS ====

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public FactorEmision getMedioTransporte() { return medioTransporte; }
    public void setMedioTransporte(FactorEmision medioTransporte) { this.medioTransporte = medioTransporte; }

    public BigDecimal getDistanciaDiariaKm() { return distanciaDiariaKm; }
    public void setDistanciaDiariaKm(BigDecimal distanciaDiariaKm) { this.distanciaDiariaKm = distanciaDiariaKm; }

    public int getDiasPresencialesMes() { return diasPresencialesMes; }
    public void setDiasPresencialesMes(int diasPresencialesMes) { this.diasPresencialesMes = diasPresencialesMes; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    @Override
    public String toString() {
        return "Commuting " + (empleado != null ? empleado.getNombre() : "ID:" ) +
                " [" + mes + "/" + anio + "]";
    }
}