package com.carbonaudit.model;

/**
 * Entidad principal que representa a la organización
 * de la que se realiza el estudio de la huella de carbono
 */
public class Empresa {

    // ========== CAMPOS DE CLASE =======

    private int idEmpresa;
    private String nombreSocial;
    private String cif;
    private String telefono;
    private String email;
    private String sector;
    private Direccion direccion; // FK hacia la tabla DIRECCION

    // ======== CONSTRUCTORES =============

    public Empresa() {
    }

    public Empresa(String nombreSocial, String cif, Direccion direccion) {
        this.nombreSocial = nombreSocial;
        this.cif = cif;
        this.direccion = direccion;
    }

    // ========= GETTERS Y SETTERS ========

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreSocial() {
        return nombreSocial;
    }

    public void setNombreSocial(String nombreSocial) {
        this.nombreSocial = nombreSocial;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}