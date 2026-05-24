package com.carbonaudit.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato CRUD genérico que deben implementar todos los DAOs de la aplicación.
 *
 * El uso de genéricos permite definir las operaciones una sola vez y reutilizarlas
 * para cualquier entidad, manteniendo la seguridad de tipos en tiempo de compilación.
 *
 * @code CommutingEmpleadoDAO no implementa esta interfaz porque
 * su clave primaria es compuesta (id_empleado, mes, anio), lo que no encaja
 * en el parámetro genérico simple  K
 *
 * @param <T> Tipo de la entidad gestionada
 * @param <K> Tipo de la clave primaria
 */
public interface DAO<T, K> {

    /**
     * Persiste una nueva entidad en la base de datos.
     *
     * @param entity entidad a insertar
     * @return la misma entidad con el {@code id} asignado por la base de datos
     */
    T create(T entity);

    /**
     * Busca una entidad por su clave primaria.
     *
     * Devuelve {@code Optional} en lugar de {@code T} para representar
     * explícitamente que el registro puede no existir, evitando comprobaciones
     * de {@code null} en la capa de servicio.
     *
     * @param id clave primaria del registro
     * @return {@code Optional} con la entidad si existe, vacío si no se encuentra
     */
    Optional<T> findById(K id);

    /**
     * Recupera todas las entidades de la tabla.
     *
     * @return lista con todas las entidades; vacía si no hay registros
     */
    List<T> findAll();

    /**
     * Actualiza los campos de un registro existente usando el id de la entidad.
     *
     * @param entity entidad con los nuevos valores (debe tener el id)
     */
    void update(T entity);

    /**
     * Elimina el registro con la clave primaria indicada.
     *
     * Para EmpleadoDAO esta operación realiza un hard-delete físico.
     * Para registrar la baja de un empleado preservando su historial usar
     * EmpleadoDAO#darDeBaja (soft-delete).
     *
     * @param id clave primaria del registro a eliminar
     */
    void delete(K id);
}
