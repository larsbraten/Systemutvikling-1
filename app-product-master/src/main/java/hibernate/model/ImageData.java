package hibernate.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "images")
@NamedQueries({
        @NamedQuery(name = "ImageData.count", query = "SELECT COUNT(i) FROM ImageData i"),
        @NamedQuery(name = "ImageData.findAll", query = "SELECT i FROM ImageData i")
})
public class ImageData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageid", insertable = false, updatable = false)
    private int imageid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "path", insertable = true, updatable = true)
    private String path;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tags",
            joinColumns=@JoinColumn(name = "imageid", referencedColumnName = "imageid")
    )
    @Column(name="tag")
    private List<String> tags = new ArrayList<String>();

    @Basic(optional = false)
    @Column(name = "created", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Embedded
    private Metadata metadata;

    /**
     * Get method for imageid
     * @return the imageID as an integer
     */
    public int getImageID() {
        return imageid;
    }

    /**
     * Set method for imageid
     * @param imageid the image's ID
     */
    public void setImageID(int imageid) {
        this.imageid = imageid;
    }

    /**
     * Get method for the User object that owns the ImageData object
     * @return the User object that the image is referring to
     */
    public User getUser() {
        return user;
    }

    /**
     * Set method to set the User that owns the ImageData object
     * @param user a User object
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get method for the path of the image
     * @return the absolute path of the image
     */
    public String getPath() {
        return path;
    }

    /**
     * Set method for the path of the image
     * @param path the absolute path of the image
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get method for created, that represents when the ImageData object was originally created and stored to persist in the database
     * @return A Date object
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
     * Get method for image tags
     * @return a List that consists of strings
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Set method for image tags
     * @param tags A List object that consists of strings
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Get method for metadata
     * @return a Metadata object
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Set method for metadata
     * @param metadata a Metadata object
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Equals method that overrides the default equals method
     * @param o An Object that is hopefully an ImageData object
     * @return true if the objects are equal based on imageID, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ImageData)) {
            return false;
        }

        ImageData img = (ImageData) o;

        return (imageid == img.getImageID());
    }

    /**
     * toString method
     * @return String
     */
    public String toString() {
        return String.format("[imagedata] imageid=%d, userid=%d, path=%s, tags=%s%n, metadata=%s", getImageID(), getUser().getUserID(), getPath(), getTags(), getMetadata());
    }
}
