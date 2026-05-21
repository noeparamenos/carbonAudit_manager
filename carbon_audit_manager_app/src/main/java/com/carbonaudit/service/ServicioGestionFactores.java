package com.carbonaudit.service;

import com.carbonaudit.dao.FactorEmisionDAO;
import com.carbonaudit.model.FactorEmision;
import com.carbonaudit.util.Validador;

import java.util.List;

public class ServicioGestionFactores {

    private final FactorEmisionDAO factorDAO;

    public ServicioGestionFactores() {
        this.factorDAO = new FactorEmisionDAO();
    }

    // =========== CONSULTAS ==============

    public List<FactorEmision> getFactores()                     { return factorDAO.findAll(); }
    public List<FactorEmision> getFactoresPorAlcance(int alcance) { return factorDAO.findByAlcance(alcance); }

    // =========== CRUD FACTOR ==============

    public void crearFactor(FactorEmision factor) {
        if (factor.getNombre() == null || factor.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del factor es obligatorio.");
        if (factor.getUnidad() == null || factor.getUnidad().isBlank())
            throw new IllegalArgumentException("La unidad es obligatoria.");
        Validador.validarValorFactor(factor.getValorFactor());
        Validador.validarAlcance(factor.getAlcance());
        factorDAO.create(factor);
    }

    public void actualizarFactor(FactorEmision factor) {
        if (factor.getNombre() == null || factor.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del factor es obligatorio.");
        if (factor.getUnidad() == null || factor.getUnidad().isBlank())
            throw new IllegalArgumentException("La unidad es obligatoria.");
        Validador.validarValorFactor(factor.getValorFactor());
        Validador.validarAlcance(factor.getAlcance());
        factorDAO.update(factor);
    }
    public void eliminarFactor(int idFactor)         { factorDAO.delete(idFactor); }
}