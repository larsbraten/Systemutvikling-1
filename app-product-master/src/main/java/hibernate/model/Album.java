package hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an album.
 * Each album consists of a list of ImageIDs and a title.
 *
 * Each album has a single user. Each user can have multiple albums.
 * An album must always have a user and cannot exist on its own.
 *
 * @author Arvid Kirkbakk
 * @author Mats Sollid Eide
 */
@Entity
@Table(name = "albums")
@NamedQueries({
        @NamedQuery(name="Album.count", query="SELECT COUNT(a) FROM Album a"),
        @NamedQuery(name="Album.findAll", query="SELECT a FROM Album a")
})

public class Album implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "albumid", insertable = false, updatable = false)
    private int albumid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @OneToMany(fetch = FetchType.LAZY) //Unsure about FetchType/additional arguments
    private List<ImageData> images = new ArrayList<>();


    /**
     * Gets the unique ID of the album
     * @return The album ID
     */
    public int getAlbumID(){
        return albumid;
    }

    /**
     * Gets the title for the album
     * @return The album title
     */
    public String getTitle(){
        return title;
    }

    /**
     * Sets a new album title
     * @param newTitle new album title
     */
    public void setTitle(String newTitle){
        this.title = newTitle;
    }

    /**
     * Gets the album's user
     * @return The user object which the album belongs to
     */
    public User getUser(){
        return user;
    }

    /**
     * Sets a user for the album
     * @param user The user object which will keep the album
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets all the images in the album
     * @return A List with ImageData objects
     */
    public List<ImageData> getImages(){
        return images;
    }

    /**
     * Adds an image to the album
     * @param image An ImageData object
     */
    public void addImage(ImageData image){
        images.add(image);
    }

    /**
     * Adds a List of images to the album
     * @param images A List containing ImageData
     */
    public void addImages(List<ImageData> images){
        this.images.addAll(images);
    }

    /**
     * Removes an image from the album
     * @param image The ImageData object to be removed
     */
    public void removeImage(ImageData image){
        images.remove(image);
    }

    /**
     * Clears all the ImageData objects from the album
     */
    public void clear(){
        images.clear();
    }
}
