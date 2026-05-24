package com.carbonaudit.service;

import com.carbonaudit.dao.DepartamentoDAO;
import com.carbonaudit.model.Departamento;
import com.carbonaudit.util.Validador;

import java.util.List;

public class ServicioGestionDepartamento {

    private final DepartamentoDAO departamentoDAO;

    public ServicioGestionDepartamento() {
        this.departamentoDAO = new DepartamentoDAO();
    }

    // =========== CRUD DEPARTAMENTO ==============

    public List<Departamento> getDepartamentosEmpresa(int idEmpresa) {
        return departamentoDAO.findAllByEmpresa(idEmpresa);
    }

    public void crearDepartamento(Departamento departamento) {
        if (departamento.getNombre() == null || departamento.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del departamento es obligatorio.");
        if (departamento.getEmpresa() == null)
            throw new IllegalArgumentException("El departamento debe estar vinculado a una empresa.");
        Validador.validarDireccion(departamento.getDireccion());
        departamentoDAO.create(departamento);
    }

    public void actualizarDepartamento(Departamento departamento) {
        if (departamento.getNombre() == null || departamento.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del departamento es obligatorio.");
        Validador.validarDireccion(departamento.getDireccion());
        departamentoDAO.update(departamento);
    }
    public void eliminarDepartamento(int idDepartamento)        { departamentoDAO.delete(idDepartamento); }
}