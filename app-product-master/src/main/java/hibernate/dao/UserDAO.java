package hibernate.dao;

import hibernate.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * UserDAO class - data access object for User objects. Extends abstract class GenericDAO.
 *
 * @author Karl Labrador
 * @author Lars-Håvard Holter Bråten
 */
public class UserDAO extends GenericDAO<User, Integer> {
    public UserDAO(EntityManager entityManager) {
        super(entityManager);
        setEntityClass(User.class);
    }

    /**
     * Finds a user by their username and uuid
     * @param username The user's username
     * @param uuid The user's UUID
     * @return the User object if there is a result, null if none
     */
    public User getUser(String username, String uuid) {
        TypedQuery<User> tq = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.uuid = :uuid", User.class);
        tq.setParameter("username", username);
        tq.setParameter("uuid", uuid);
        //tq.unwrap(org.hibernate.query.Query.class).setTimeout(100);

        User user = null;

        try {
            if (tq.getResultList().size() == 1) {
                user = tq.getSingleResult();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return user;
    }
}
