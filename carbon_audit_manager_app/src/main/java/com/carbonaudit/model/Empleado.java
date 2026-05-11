package com.carbonaudit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa a los empleados de la empresa con sus datos de movilidad integrados.
 */
public class Empleado {

    private int idEmpleado;
    private String nombre;
    private LocalDate fechaAlta;
    private LocalDate fechaBaja;  // null = activo en la empresa

    // Datos de movilidad (Alcance 3)
    private BigDecimal distanciaTrabajo;
    private FactorEmision medioTransporte;
    private int diasPresenciales;

    // Relaciones
    private Direccion direccion;  // Residencia
    private Departamento departamento;   // Departamento

    // ======== CONSTRUCTORES ===========

    public Empleado() {
        this.diasPresenciales = 20;
        this.fechaAlta = LocalDate.now();
    }

    public Empleado(String nombre, LocalDate fechaAlta, FactorEmision medioTransporte,
                    int diasPresenciales, Direccion direccion, Departamento departamento) {
        this.nombre = nombre;
        this.fechaAlta = fechaAlta;
        this.medioTransporte = medioTransporte;
        this.diasPresenciales = diasPresenciales;
        this.direccion = direccion;
        this.departamento = departamento;
    }

    // ==== GETTERS Y SETTERS ====


    public boolean isActivo() { return fechaBaja == null; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }

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