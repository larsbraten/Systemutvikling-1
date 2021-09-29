package hibernate.api;

import hibernate.model.Album;
import hibernate.model.ImageData;
import hibernate.model.User;

import java.util.List;

/**
 * API Interface for data accessing
 *
 * @author Karl Labrador
 * @author Arvid Kirkbakk
 * @author Mats Sollid Eide
 */
public interface API {
    // Users
    User getUser(int userid);
    User getUser(String username, String uuid);
    List<User> getAllUsers();
    User createUser(User user);
    User createUser(String username, String uuid);
    User saveUser(User user);
    void deleteUser(User user);

    // Images
    ImageData getImage(int imageid);
    List<ImageData> getImages(User user);
    ImageData saveImage(User user, ImageData imagedata);
    void deleteImage(ImageData imagedata);

    // Album
    Album createAlbum(User user);
    Album getAlbum(int albumid);
    List<Album> getAlbums(User user);
    void addImages(Album album, ImageData image);
    void addImages(Album album, List<ImageData> images);
    Album saveAlbum(Album album);
    void deleteAlbum(Album album);
    void setAlbumTitle(Album album, String title);
}
