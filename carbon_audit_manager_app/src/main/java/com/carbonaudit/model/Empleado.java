package com.carbonaudit.model;

import java.math.BigDecimal;

/**
 * Representa a los empleados de la empresa con sus datos de movilidad integrados.
 */
public class Empleado {

    private int idEmpleado;
    private String nombre;

    // Datos de movilidad (Alcance 3)
    private BigDecimal distanciaTrabajo;
    private FactorEmision medioTransporte;
    private int diasPresenciales;

    // Relaciones
    private Direccion direccion;  // Residencia
    private Departamento departamento;   // Departamento

    // ======== CONSTRUCTORES ===========

    public Empleado() {
        this.diasPresenciales = 20; //Por defecto trabaja en presencial
    }

    // Constructor completo para facilitar la creación desde formularios
    public Empleado(String nombre, BigDecimal distanciaTrabajo, FactorEmision medioTransporte,
                    int diasPresenciales, Direccion direccion, Departamento departamento) {
        this.nombre = nombre;
        this.distanciaTrabajo = distanciaTrabajo;
        this.medioTransporte = medioTransporte;
        this.diasPresenciales = diasPresenciales;
        this.direccion = direccion;
        this.departamento = departamento;
    }

    public Empleado(String nombre, FactorEmision medioTransporte, int diasPresenciales, Direccion direccion, Departamento departamento) {
        this.nombre = nombre;
        this.medioTransporte = medioTransporte;
        this.diasPresenciales = diasPresenciales;
        this.direccion = direccion;
        this.departamento = departamento;
    }

    // ==== GETTERS Y SETTERS ====


    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getDistanciaTrabajo() { return distanciaTrabajo; }
    public void setDistanciaTrabajo(BigDecimal distanciaTrabajo) { this.distanciaTrabajo = distanciaTrabajo; }

    public FactorEmision getMedioTransporte() { return medioTransporte; }
    public void setMedioTransporte(FactorEmision medioTransporte) { this.medioTransporte = medioTransporte; }

    public int getDiasPresenciales() { return diasPresenciales; }
    public void setDiasPresenciales(int diasPresenciales) { this.diasPresenciales = diasPresenciales; }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    @Override
    public String toString() {
        return nombre + " (ID: " + idEmpleado + ")";
    }
}