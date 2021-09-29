package hibernate.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Abstract DAO class that covers basic methods that apply to all DAOs
 *
 * @author Karl Labrador
 * @author Lars-Håvard Holter Bråten
 *
 * @param <T> Entity Class
 * @param <ID> Data type of the primary identifier
 */
public abstract class GenericDAO<T, ID extends Serializable> implements DAO<T, ID> {
    @PersistenceContext
    protected EntityManager entityManager;

    private Class<T> entityClass;

    /**
     * Basic constructor
     * @param entityManager JPA entityManager
     */
    public GenericDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Sets the entityClass value
     * @param entityClass the Class of the entity
     */
    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Finds data by ID
     * @param id The ID of the entity
     * @return Optional object
     */
    public Optional<T> find(ID id) {
        try {
            return Optional.ofNullable(entityManager.find(entityClass, id));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Finds all data
     * @return a List object that contains User objects if there are results
     */
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        Query nq = entityManager.createNamedQuery(entityClass.getSimpleName() + ".findAll");
        return nq.getResultList();
    }

    /**
     * Saves the state of the persistence object
     * @param entity The entity/persistence object
     * @return The entity/persistence object in its current state
     */
    public Optional<T> save(T entity) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(entity);
            entityManager.getTransaction().commit();

            return Optional.ofNullable(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Deletes an entity/object from the database
     * @param entity The entity/persistence object
     */
    public void delete(T entity) {
        try {
            entityManager.getTransaction().begin();
            entityManager.remove(entity);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
