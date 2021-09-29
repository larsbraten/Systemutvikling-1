package hibernate.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for the GenericDAO class
 *
 * @author Karl Labrador
 *
 * @param <T> Entity Class
 * @param <ID> Data type of the primary identifier
 */
public interface DAO<T, ID extends Serializable> {
    // Actions
    Optional<T> find(ID id);
    Optional<T> save(T entity);
    List<T> findAll();
    void delete(T entity);
}
