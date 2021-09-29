package hibernate.api;

import hibernate.model.Album;
import hibernate.model.ImageData;
import hibernate.model.User;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * DataAPITest class does usage tests for the DataAPI and tests most of the DAOs available.
 * Uses an in-memory H2 database to perform test operations against.
 *
 * @author Mats Sollid Eide
 * @author Arvid Kirkbakk
 * @author Karl Labrador
 */
class DataAPITest {
    private static DataAPI api;
    private static User testUser;

    /**
     * Sets up the test. Here a user is created once and used throughout the tests.
     */
    @BeforeAll
    public static void setup() {
        api = new DataAPI();
        testUser = api.createUser("TestUser", "TestUUID");
    }

    /**
     * Tests if the retrieved user object is equal to the test user (testUser).
     * Gets user by a User ID.
     * Should fail if they are not equal.
     */
    @Test
    void testGetUserByIdAgainstTestUser() {
        try {
            User existingUser = api.getUser(1);
            assertEquals(testUser, existingUser);
        } catch (Exception e){
            fail("Exception caught when getting user by ID: " + e.getMessage());
        }
    }

    /**
     * Tests if the retrieved user object is equal to the test user (testUser).
     * Gets user by a Username and UUID.
     * Should fail if they are not equal.
     */
    @Test
    void testGetUserByUsernameAndUUIDAgainstTestUser() {
        try{
            User existingUser = api.getUser("TestUser", "TestUUID");
            assertEquals(testUser, existingUser);
        }catch (Exception e){
            fail("Exception caught when getting user by username and UUID: " + e.getMessage());
        }
    }

    /**
     * Tests if the retrieved number of total users in the system equals to 1.
     * The test user (testUser) should be the only user in the database.
     * Should fail if it is not equal to 1.
     */
    @Test
    void testGetAllUsersShouldEqualToOne() {
        try {
            List<User> allUsers = api.getAllUsers();
            assertEquals(1, allUsers.size());
        } catch (Exception e){
            fail("Exception caught when testing getAllUsers: " + e.getMessage());
        }
    }

    /**
     * Tests if it is possible to change the username of the test user, and then save.
     * It then verifies if the change has been made. Reverts back to the original test user's
     * username before starting other tests.
     */
    @Test
    void testSetUsernameAndSaveThenVerify() {
        try {
            // Try changing the test user's username
            testUser.setUsername("NewUsername");
            api.saveUser(testUser);
            assertEquals("NewUsername", testUser.getUsername());

            // Try lookup the user based on new username
            assertNotNull(api.getUser("NewUsername", "TestUUID"));

            // Change back to its previous username
            testUser.setUsername("TestUser");
            api.saveUser(testUser);
            assertEquals("TestUser", testUser.getUsername());
        }catch (Exception e){
            fail("Caught exception performing namechange test: " + e.getMessage());
        }
    }

    /**
     * Tests user creation and deletion. It creates a user and checks if the total number of users equals to 2.
     * It then proceeds to delete the user and verifies that the number of users has been reduced to 1.
     */
    @Test
    void testCreateAndDeleteUser() {
        try {
            // Create user and check that number of users has grown by 1
            User tempUser = api.createUser("UserToDelete", "UserToDeleteUUID");
            assertEquals(2, api.getAllUsers().size());

            // Delete the newly created user
            api.deleteUser(tempUser);

            // Check if the number of users is 2
            assertEquals(1, api.getAllUsers().size());
        }catch (Exception e){
            fail("Exception caught while creating/deleting user: " + e.getMessage());
        }
    }

    /**
     * Tests image creation and deletion. It creates an image and checks if the total number of images equals to 1.
     * It then proceeds to delete the image and verifies that the number of images has been reduced to 1.
     */
    @Test
    void testCreateAndDeleteImage() {
        try{
            // Add image
            ImageData testImage = new ImageData();
            testImage.setPath("C:\\User\testuser\\Desktop\\testpath.png");
            api.saveImage(testUser, testImage);

            assertEquals(1, testUser.getImages().size());

            // Delete image
            api.deleteImage(testImage);

            assertEquals(0, testUser.getImages().size());
        } catch (Exception e){
            fail("Exception caught: " + e.getMessage() + e);
        }
    }

    /**
     * Tests album creation and deletion. It creates an image and checks if the total number of albums equals to 1.
     * It then proceeds to delete the album and verifies that the number of albums has been reduced to 0.
     * While an album has been created, it also tests manipulation operations and verifies if changes have been made.
     */
    @Test
    void testCreateAndDeleteAlbum() {
        try {
            // Create album
            Album newAlbum = api.createAlbum(testUser);
            api.setAlbumTitle(newAlbum, "TestAlbum");

            // Add image
            ImageData testImage = new ImageData();
            testImage.setPath("C:\\User\testuser\\Desktop\\testpath.png");
            api.saveImage(testUser, testImage);

            // Save image to album
            api.addImages(newAlbum, testImage);

            assertEquals(1, testUser.getAlbums().size());
            assertEquals(1, newAlbum.getImages().size());
            assertEquals("TestAlbum", newAlbum.getTitle());

            // Manipulate album
            api.setAlbumTitle(newAlbum, "CoolAlbum");
            assertEquals("CoolAlbum", newAlbum.getTitle());

            // Delete image and album
            api.deleteImage(testImage);
            api.deleteAlbum(newAlbum);

            // Check that there are no albums
            assertEquals(0, testUser.getAlbums().size());
        } catch (Exception e){
            fail("Exception caught when creating/getting album: " + e.toString() + e.getMessage());
        }
    }
}