package com.carbonaudit.model;

/**
 * Segmentación de la empresa para el análisis de impacto ambiental Por departamentos
 * Permite que una empresa tenga apartamentos en diversas localizaciones
 */
public class Departamento {

    private int idDepartamento;
    private String nombre;
    private String descripcion;

    // Indica si para este departamento se deben calcular las emisiones de commuting (Alcance 3)
    private boolean incluirAlcance3;

    private Direccion direccion; // FK a DIRECCION
    private Empresa empresa;   // FK a EMPRESA

    // ======== CONSTRUCTORES ==============
    public Departamento() {
        // Por defecto activamos el alcance 3
        // No es necesario según la legislación actual. Se podrá editar desde la UI
        this.incluirAlcance3 = true;
    }

    // Constructor para facilitar la creación de nuevos departamentos
    public Departamento(String nombre, Direccion direccion, Empresa empresa) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.empresa = empresa;
        this.incluirAlcance3 = true;
    }

    // ==== GETTERS Y SETTERS ====

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isIncluirAlcance3() { return incluirAlcance3; }
    public void setIncluirAlcance3(boolean incluirAlcance3) { this.incluirAlcance3 = incluirAlcance3; }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

}