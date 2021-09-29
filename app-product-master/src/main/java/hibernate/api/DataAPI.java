package hibernate.api;

import hibernate.dao.AlbumDAO;
import hibernate.dao.ImageDAO;
import hibernate.dao.UserDAO;
import hibernate.model.Album;
import hibernate.model.ImageData;
import hibernate.model.User;
import org.apache.log4j.Logger;
import utility.Device;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;

/**
 * DataAPI Class, an API layer for data access objects
 *
 * @author Karl Labrador
 * @author Lars-Håvard Holter Bråten
 * @author Arvid Kirkbakk
 * @author Mats Sollid Eide
 */
public class DataAPI implements API {
    private static EntityManagerFactory entityManagerFactory;
    private final static Logger logger = Logger.getLogger(DataAPI.class);
    private EntityManager entityManager;
    private UserDAO userDAO;
    private ImageDAO imageDAO;
    private AlbumDAO albumDAO;

    /**
     * Constructor for DataAPI. Initializes the API by creating new instances of the relevant DAOs.
     */
    public DataAPI() {
        entityManagerFactory = Persistence.createEntityManagerFactory("app-product");
        entityManager = entityManagerFactory.createEntityManager();

        userDAO = new UserDAO(entityManager);
        imageDAO = new ImageDAO(entityManager);
        albumDAO = new AlbumDAO(entityManager);

        logger.info("Initialized DataAPI");
    }

    // Login

    /**
     * A method to automatically get a User object. It will check if the user exists based on username and UUID, if not, a user will be created.
     * @return a User object or null if something wrong happened
     */
    public User login() {
        String username = Device.getUsername();
        String uuid = Device.getUUID();

        // Check if user exists
        User user = getUser(username, uuid);
        if (user != null) {
            return user;
        }

        // User doesn't exist so it will be created
        return createUser(username, uuid);
    }

    // Users

    /**
     * Finds a User object based on the user's ID
     * @param userid the user's ID
     * @return a User object retrieved from the database
     */
    public User getUser(int userid) {
        Optional<User> optionalUser = userDAO.find(userid);
        logger.info("Retrieved user with userid");
        return optionalUser.orElse(null);
    }

    /**
     * Finds a User object based on the user's username and UUID
     * @param username the user's ID
     * @param uuid the user's UUID
     * @return a User object retrieved from the database
     */
    public User getUser(String username, String uuid) {
        logger.info("Retrieved user with username and UUID");
        return userDAO.getUser(username, uuid);
    }

    /**
     * Takes the User object argument and pushes it to the database
     * @param user A User object created beforehand
     * @return The User object that was pushed
     */
    public User createUser(User user) {
        if (user == null || (user.getUsername().equals("") || user.getUuid().equals(""))) {
            return null;
        }

        logger.info("Created User with a User object");
        return userDAO.save(user).orElse(null);
    }

    /**
     * Creates a User object and sends it to another method to push it to the database
     * @param username the user's username
     * @param uuid the user's UUID
     * @return The User object that was pushed
     */
    public User createUser(String username, String uuid) {
        if (username.equals("") && uuid.equals("")) {
            throw new IllegalArgumentException("Username and UUID cannot be empty");
        }

        User user = new User();

        user.setUsername(username);
        user.setUuid(uuid);

        Optional<User> optionalUser = userDAO.save(user);
        logger.info("Created User with username " + user.getUsername() + " and UUID " + user.getUuid());

        return optionalUser.orElse(null);
    }

    /**
     * Automatically attempts to create a new user based on the user's OS username and UUID
     * @return A User object retrieved from the database
     */
    public User createUserAuto() {
        return createUser(Device.getUsername(), Device.getUUID());
    }

    /**
     * Finds all User objects stored in the database
     * @return a List object consisting of User objects
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Pushes and saves changed made to the User object to the database. It is important that
     * this is run after changes have been made to the User object, in order for the changes to
     * persist.
     *
     * It is possible to perform multiple manipulations on the User object, as JPA/Hibernate will
     * push the changes in bulk to the database. Not running saveUser will result in changes not being
     * saved!
     *
     * @param user the User object with changes made that needs to be pushed to the database
     */
    public User saveUser(User user) {
        Optional<User> optionalUser = userDAO.save(user);
        logger.info("Saved User State");
        return optionalUser.orElse(null);
    }

    /**
     * Deletes a User object from the database
     * @param user the User object to be deleted
     */
    public void deleteUser(User user) {
        logger.info("Deleted user with username " + user.getUsername());
        userDAO.delete(user);
    }

    // Images
    /**
     * Retrieves an ImageData object based on the image's ImageID
     * @param imageid the image's ImageID
     * @return An ImageData object retrieved from the database
     */
    public ImageData getImage(int imageid) {
        Optional<ImageData> optionalImage = imageDAO.find(imageid);
        return optionalImage.orElse(null);
    }

    /**
     * Retrieves ImageData objects from the database based on who the images belong to.
     * It is not necessary to use this method in order to get the user's images.
     *
     * You can use .getImages() directly on the User object. See {@link hibernate.model.User#getImages}.
     *
     * @param user a User object retrieved from the database
     * @return A List object that consists of ImageData objects
     */
    public List<ImageData> getImages(User user) {
        return ((user != null) ? user.getImages() : null);
    }

    /**
     * Saves an ImageData object directly to the database.
     * @param user a User object retrieved from the database
     * @param imagedata an ImageData object
     * @return the ImageData object in an updated state
     */
    public ImageData saveImage(User user, ImageData imagedata) {
        if (user != null) {
            imagedata.setUser(user);
        }

        imagedata.getUser().addImage(imagedata);
        Optional<ImageData> optionalImage = imageDAO.save(imagedata);

        return optionalImage.orElse(null);
    }

    /**
     * Alias method for saveImage
     * @param imagedata an ImageData object
     * @return the result of {@link #saveImage(User, ImageData)}
     */
    public ImageData saveImage(ImageData imagedata) {
        return saveImage(null, imagedata);
    }

    /**
     * Deletes an ImageData object from the database
     * @param imagedata an ImageData object
     */
    public void deleteImage(ImageData imagedata) {
        imagedata.getUser().deleteImage(imagedata);
    }


    // Albums

    /**
     * Creates a new album object under a given user
     * @param user The user object the album belongs to
     * @return The newly created album object
     */
    public Album createAlbum(User user){
        Album album = new Album();
        user.addAlbum(album);
        album.setUser(user);

        Optional<Album> optionalAlbum = albumDAO.save(album);
        logger.info("Created an album");

        return optionalAlbum.orElse(null);
    }

    /**
     * Gets the album object correpsonding to the given album ID
     * @param albumid The ID of the album to be retrieved
     * @return The corresponding album, or null if no match is found
     */
    public Album getAlbum(int albumid) {
        Optional<Album> optionalAlbum = albumDAO.find(albumid);
        return optionalAlbum.orElse(null);
    }

    /**
     * Gets all the albums linked to a user
     * The change is not written to the database until saveAlbum() is called
     * @param user The user object whose albums will be retrieved
     * @return A List containing the user's album
     */
    public List<Album> getAlbums(User user) {
        return ((user != null) ? user.getAlbums() : null);
    }

    /**
     * Adds a single image to an album
     * The change is not written to the database until saveAlbum() is called
     *
     * @param album The album the image will be added to
     * @param image An ImageData object
     */
    public void addImages(Album album, ImageData image) {
        album.addImage(image);
        logger.info("Added image to album");
    }

    /**
     * Adds a list of images to an album
     * The change is not written to the database until saveAlbum() is called
     *
     * @param album The album the image will be added to
     * @param images A List of ImageData objects
     */
    public void addImages(Album album, List<ImageData> images) {
        album.addImages(images);
        logger.info("Added " + images.size() + " images to album.");
    }

    /**
     * Sets a title for an album
     * The change is not written to the database until saveAlbum() is called
     *
     * @param album The album to be titled
     * @param title The title
     */
    public void setAlbumTitle(Album album, String title) {
        album.setTitle(title);
    }

    /**
     * Removes an album from its associated user
     * The change is not written to the database until saveAlbum() is called
     *
     * @param album The album to be deleted
     */
    @Override
    public void deleteAlbum(Album album){
        album.getUser().deleteAlbum(album);
        logger.info("Deleted album.");
    }

    /**
     * Saves an album to the database
     * @param album The album to be saved
     * @return The saved album object
     */
    public Album saveAlbum(Album album) {
        Optional<Album> optionalAlbum = albumDAO.save(album);
        logger.info("Saved album to database.");

        return optionalAlbum.orElse(null);
    }
}
