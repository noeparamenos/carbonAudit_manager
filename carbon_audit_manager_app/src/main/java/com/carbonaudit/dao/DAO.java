package com.carbonaudit.dao;

import java.util.List;
import java.util.Optional;

/**
 * Obliga a todos nuestros DAOs específicos a que tengan las mismas operaciones CRUD
 * Usando Genéricos (T para la Entidad, K para el tipo de dato del ID
 * @param <T> Entidad
 * @param <K> ID (El id en la bd)
 */
public interface DAO<T, K>{

    T create(T entity);

    // Usamos Optional porque una búsqueda por ID podría no encontrar nada
    Optional<T> findById(K id);

    List<T> findAll();

    void update(T entity);

    void delete(K id);
}
