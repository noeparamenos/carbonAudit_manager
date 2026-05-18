package com.carbonaudit.service;

import com.carbonaudit.dao.DireccionDAO;
import com.carbonaudit.dao.EmpleadoDAO;
import com.carbonaudit.model.Direccion;
import com.carbonaudit.model.Empleado;
import com.carbonaudit.service.external.IServicioGeografico;
import com.carbonaudit.util.Validador;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de gestión de empleados.
 * Centraliza el acceso a EmpleadoDAO y DireccionDAO para que ningún Controlador
 * los llame directamente, manteniendo la separación Controller → Service → DAO.
 */
public class ServicioGestionEmpleado {

    private final EmpleadoDAO         empleadoDAO;
    private final DireccionDAO        direccionDAO;
    private final IServicioGeografico geoService;

    /** Constructor completo — requerido cuando se necesite calcular distancias vía API. */
    public ServicioGestionEmpleado(IServicioGeografico geoService) {
        this.empleadoDAO  = new EmpleadoDAO();
        this.direccionDAO = new DireccionDAO();
        this.geoService   = geoService;
    }

    /** Constructor sin geoService — para contextos de solo lectura/escritura sin cálculo de distancias. */
    public ServicioGestionEmpleado() {
        this(null);
    }

    // =========== CRUD EMPLEADO ==============

    public Empleado crearEmpleado(Empleado empleado) {
        Validador.validarDiasPresenciales(empleado.getDiasPresenciales());
        Validador.validarDireccion(empleado.getDireccion());
        return empleadoDAO.create(empleado);
    }

    public void actualizarEmpleado(Empleado empleado) {
        Validador.validarDiasPresenciales(empleado.getDiasPresenciales());
        Validador.validarDireccion(empleado.getDireccion());
        empleadoDAO.update(empleado);
    }

    public void darDeBajaEmpleado(int idEmpleado, LocalDate fecha) {
        empleadoDAO.darDeBaja(idEmpleado, fecha);
    }

    public List<Empleado> getEmpleadosDepartamento(int idDepartamento) {
        return empleadoDAO.findAllByDepartamento(idDepartamento);
    }

    public int countEmpleadosDepartamento(int idDepartamento) {
        return empleadoDAO.countByDepartamento(idDepartamento);
    }

    // =========== GEOCODIFICACIÓN Y DISTANCIA ==============

    /** Persiste las coordenadas geocodificadas de una dirección. */
    public void persistirCoordenadas(Direccion dir) {
        direccionDAO.update(dir);
    }

    /**
     * Geocodifica las direcciones del empleado y su departamento, calcula la distancia
     * al trabajo vía la API geográfica y persiste el resultado en BD.
     * Se ejecuta desde el hilo daemon de JavaFX Task; no toca la UI.
     */
    public void calcularYPersistirDistancia(Empleado empleado) throws Exception {
        Direccion dirEmpleado     = empleado.getDireccion();
        Direccion dirDepartamento = empleado.getDepartamento().getDireccion();

        geoService.completarCoordenadas(dirEmpleado);
        geoService.completarCoordenadas(dirDepartamento);

        empleado.setDistanciaTrabajo(geoService.calcularDistancia(dirEmpleado, dirDepartamento));

        empleadoDAO.update(empleado);
        if (dirDepartamento != null) {
            direccionDAO.update(dirDepartamento);
        }
    }
}