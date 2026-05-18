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
        Validador.validarValorFactor(factor.getValorFactor());
        Validador.validarAlcance(factor.getAlcance());
        factorDAO.create(factor);
    }

    public void actualizarFactor(FactorEmision factor) {
        Validador.validarValorFactor(factor.getValorFactor());
        Validador.validarAlcance(factor.getAlcance());
        factorDAO.update(factor);
    }
    public void eliminarFactor(int idFactor)         { factorDAO.delete(idFactor); }
}