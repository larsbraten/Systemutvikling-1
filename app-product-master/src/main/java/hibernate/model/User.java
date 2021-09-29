package hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * User class that stores data about the user
 * It has a composition based approach and contains ImageData objects that belongs
 * to the user.
 *
 * Any changes made to a User object will have to be pushed in order for the data to
 * persist. Remember to use the saveUser method in the DataAPI to push changes. See {@link hibernate.api.DataAPI#saveUser}.
 *
 * @author Karl Labrador
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"username", "uuid"})}
)
@NamedQueries({
        @NamedQuery(name="User.count", query="SELECT COUNT(u) FROM User u"),
        @NamedQuery(name="User.findAll", query="SELECT u FROM User u")
})
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid", insertable = false, updatable = false)
    private int userid;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Basic(optional = false)
    @Column(name = "created", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("imageid DESC")
    private List<ImageData> images = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("albumid DESC")
    private List<Album> albums = new ArrayList<>();

    /**
     * Get method for userid
     * @return int userid
     */
    public int getUserID() {
        return userid;
    }

    /**
     * Set method for userid
     * @param userid the user's userid
     */
    public void setUserID(int userid) {
        this.userid = userid;
    }

    /**
     * Get method for username
     * @return String username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set method for username
     * @param username the user's username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get method for UUID
     * @return String uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set method for UUID
     * @param uuid the user's UUID
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get method for created, that represents when the User object was originally created and stored to persist in the database
     * @return Date created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Set method for created
     * @param created a Date object
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Get method to retrieve all the images that belongs to the user.
     * It is allowed to directly manipulate the List object after getting it
     * with this method. Remember to use the saveUser method to push changes to
     * the database. See {@link hibernate.api.DataAPI#saveUser}.
     *
     * @return A List object that consists of ImageData objects
     */
    public List<ImageData> getImages() {
        return images;
    }

    /**
     * Get method to retrieve all the images grouped by tags.
     * @return Returns a HashMap where the tag is the key, and value is a List.
     */
    public HashMap<String, List<ImageData>> getImagesByTags() {
        HashMap<String, List<ImageData>> hashMap = new HashMap<String, List<ImageData>>();

        images.forEach(image -> {
            List<String> tags = image.getTags();

            tags.forEach(tag -> {
               if (!hashMap.containsKey(tag)) {
                   List<ImageData> list = new ArrayList<ImageData>();
                   list.add(image);

                   hashMap.put(tag, list);
               }
               else {
                   hashMap.get(tag).add(image);
               }
            });
        });

        return hashMap;
    }

    /**
     * Set method for images
     * @param images a List object that contains ImageData objects
     */
    public void setImages(List<ImageData> images) {
        this.images = images;
    }

    /**
     * This method allows the developer to manually create an ImageData object, and push it to the array
     * via this method. Remember to use saveUser method to push changes to the database. See {@link hibernate.api.DataAPI#saveUser}.
     *
     * @param image an ImageData object
     */
    public void addImage(ImageData image) {
        image.setUser(this);
        images.add(image);
    }

    /**
     * This method allows the developer to manually create an ImageData object, and push it to the array
     * via this method. Remember to use saveUser method to push changes to the database. See {@link hibernate.api.DataAPI#saveUser}.
     *
     * @param image an ImageData object
     */
    public void deleteImage(ImageData image) {
        images.remove(image);
        image.setUser(null);
    }

    /**
     * Adds an album to the user's albums
     * @param album the album object to be added
     */
    public void addAlbum(Album album){
        album.setUser(this);
        albums.add(album);
    }

    /**
     * Deletes an album from the user
     * @param album the album object to be deleted
     */
    public void deleteAlbum(Album album){
        albums.remove(album);
        album.setUser(null);
    }

    /**
     * Gets all the user's albums
     * @return A List containing the album objects
     */
    public List<Album> getAlbums(){
        return albums;
    }

    /**
     * toString method
     * @return String
     */
    public String toString() {
        return String.format("[user] userid=%d, username=%s, uuid=%s, images=%s, albums=%s", getUserID(), getUsername(), getUuid(), getImages(), getAlbums());
    }
}