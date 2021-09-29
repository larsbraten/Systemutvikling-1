package hibernate.dao;

import hibernate.model.Album;
import javax.persistence.EntityManager;

/**
 * AlbumDAO class - data access object for Album objects. Extends abstract class GenericDAO.
 *
 * @author Arvid Kirkbakk
 * @author Mats Sollid Eide
 */
public class AlbumDAO extends GenericDAO<Album, Integer>{
    public AlbumDAO(EntityManager entityManager){
        super(entityManager);
        setEntityClass(Album.class);
    }
}
