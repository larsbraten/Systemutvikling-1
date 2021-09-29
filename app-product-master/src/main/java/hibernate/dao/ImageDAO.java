package hibernate.dao;

import hibernate.model.ImageData;

import javax.persistence.EntityManager;

/**
 * ImageDAO class - data access object for ImageData objects. Extends abstract class GenericDAO.
 *
 * @author Karl Labrador
 * @author Lars-Håvard Holter Bråten
 */
public class ImageDAO extends GenericDAO<ImageData, Integer> {
    public ImageDAO(EntityManager entityManager) {
        super(entityManager);
        setEntityClass(ImageData.class);
    }
}
