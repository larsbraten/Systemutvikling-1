package metadata;

import hibernate.model.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MetadataTest class that does usage test for the Metadata class.
 *
 * @author Mats Sollid Eide
 * @author Karl Labrador
 */
class MetadataTest {
    private String filepath;

    /**
     * Sets up the test by making ready the test image.
     */
    @BeforeEach
    public void testRetrieveTestResource() {
        try {
            URL res = getClass().getResource("/TestImages/MetadataTest.JPG");
            this.filepath = Paths.get(res.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException ex) {
            fail("Filepath was not successfully retrieved with this message: " + ex.getMessage());
        }
    }

    /**
     * Tests if the Metadata class is able to retrieve the metadata from the test image.
     * Should fail if the data retrieved is not equal to expected data.
     */
    @Test
    void testRetrieveSimpleData() {
        if (filepath == null) {
            fail("Filepath was not successfully retrieved to perform test");
        }

        Metadata metadata = Metadata.generate(filepath);
        assertNotNull(metadata);

        assertEquals(2448, metadata.getHeight());
        assertEquals(3264, metadata.getWidth());
        assertEquals("Apple", metadata.getMake());
        assertEquals("iPhone 5", metadata.getModel());
        assertNotNull(metadata.getCaptured());
    }
}