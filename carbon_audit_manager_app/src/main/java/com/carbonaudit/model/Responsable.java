package com.carbonaudit.model;

import java.time.LocalDate;

/**
 * Representa el historial de responsabilidad de sostenibilidad de un empleado
 * sobre un departamento específico.
 * Es de utilidad para auditorias y cumplimiento de estándares
 */
public class Responsable {

    private int idAsignacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin; // Null si sigue en el cargo actualmente

    // FKs
    private Departamento departamento;
    private Empleado encargado;

    // ======= CONSTRUCTORES ============
    public Responsable() {
    }


    public Responsable(LocalDate fechaInicio, Departamento departamento, Empleado encargado) {
        this.fechaInicio = fechaInicio;
        this.departamento = departamento;
        this.encargado = encargado;
    }

    // ==== GETTERS Y SETTERS ====

    public int getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(int idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Empleado getEncargado() {
        return encargado;
    }

    public void setEncargado(Empleado encargado) {
        this.encargado = encargado;
    }

    /**
     * Métod de utilidad para saber si el responsable está activo
     *
     * @return True si el empleado es aún responsable del departamento.
     * False si ya finalizo su periodo de responsabilidad
     */
    public boolean isActivo() {
        return fechaFin == null;
    }

    @Override
    public String toString() {
        return "Responsable: " + encargado + " en Dept: " + departamento +
                " (Desde: " + fechaInicio + ")";
    }
}