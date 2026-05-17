package com.carbonaudit.service;

import com.carbonaudit.dao.ResponsableDAO;
import com.carbonaudit.model.Responsable;

import java.util.List;
import java.util.Optional;

public class ServicioGestionResponsable {

    private final ResponsableDAO responsableDAO;

    public ServicioGestionResponsable() {
        this.responsableDAO = new ResponsableDAO();
    }

    // =========== CONSULTAS ==============

    public List<Responsable> getAllActivos() {
        return responsableDAO.findAllActivos();
    }

    public Optional<Responsable> getActivoByDepartamento(int idDepartamento) {
        return responsableDAO.findActivoByDepartamento(idDepartamento);
    }

    public List<Responsable> getAllByDepartamento(int idDepartamento) {
        return responsableDAO.findAllByDepartamento(idDepartamento);
    }

    // =========== CRUD RESPONSABLE ==============

    public void crearResponsable(Responsable responsable)    { responsableDAO.create(responsable); }
    public void actualizarResponsable(Responsable responsable) { responsableDAO.update(responsable); }
}