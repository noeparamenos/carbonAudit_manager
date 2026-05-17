package com.carbonaudit.service;

import com.carbonaudit.dao.DireccionDAO;
import com.carbonaudit.dao.EmpleadoDAO;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empleado;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de gestión de empleados.
 * Centraliza el acceso a EmpleadoDAO y DireccionDAO para que ningún Controlador
 * los llame directamente, manteniendo la separación Controller → Service → DAO.
 */
public class ServicioGestionEmpleado {

    private final EmpleadoDAO  empleadoDAO;
    private final DireccionDAO direccionDAO;

    public ServicioGestionEmpleado() {
        this.empleadoDAO  = new EmpleadoDAO();
        this.direccionDAO = new DireccionDAO();
    }

    // =========== CRUD EMPLEADO ==============

    public Empleado crearEmpleado(Empleado empleado) {
        return empleadoDAO.create(empleado);
    }

    public void actualizarEmpleado(Empleado empleado) {
        empleadoDAO.update(empleado);
    }

    public void darDeBajaEmpleado(int idEmpleado, LocalDate fecha) {
        empleadoDAO.darDeBaja(idEmpleado, fecha);
    }

    public List<Empleado> getEmpleadosDepartamento(int idDepartamento) {
        return empleadoDAO.findAllByDepartamento(idDepartamento);
    }

    // =========== PERSISTENCIA DE COORDENADAS Y DISTANCIA ==============

    /** Persiste las coordenadas geocodificadas de una dirección. */
    public void persistirCoordenadas(Direccion dir) {
        direccionDAO.update(dir);
    }

    /**
     * Persiste en BD la distancia al trabajo calculada y las coordenadas geocodificadas
     * de ambas direcciones (empleado y departamento).
     * Se llama desde el callback onSucceeded del hilo asíncrono de cálculo de distancia.
     */
    public void persistirResultadoDistancia(Empleado empleado) {
        empleadoDAO.update(empleado);
        if (empleado.getDepartamento().getDireccion() != null) {
            direccionDAO.update(empleado.getDepartamento().getDireccion());
        }
    }
}