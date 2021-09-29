package utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hibernate.model.ImageData;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDFTest that does usage tests for the PDF utility class
 *
 * @author Mats Sollid Eide
 */
class PDFTest {
    private static ArrayList<ImageData> images = new ArrayList<>();
    private static ImageData imageData = new ImageData();

    /**
     * Reset to clear imageData and images variables.
     */
    @BeforeEach
    void reset(){
        imageData.setPath("");
        images.clear();
    }

    /**
     * Generates a PDF that consists of test images.
     * Should fail if the PDF.generate method throws an exception or fails.
     */
    @Test
    void testGenerate() {

        String imageDirectory = System.getProperty("user.dir") + "/src/test/resources/TestImages/";
        String[] imageFilenames = {
                "000090025.jpg",
                "000090029.jpg",
                "000090031.jpg",
                "MetadataTest.JPG"
        };

        for(String file : imageFilenames){
            var imgData = new ImageData();
            imgData.setPath(imageDirectory + file);
            images.add(imgData);
        }

        try {
            PDF.generate(images, PDF.Mode.DYNAMIC);
        }
        catch(Exception e){
            fail("Failed to generate PDF: " + e.getMessage());
        }
    }

    /**
     * Generates a PDF that consists of the test images, saves the pdf to a location.
     * Should fail if the PDF.generate method throws an exception, fails or it cannot save the file to the location.
     */
    @Test
    void testGenerateWithPath(){
        String imageDirectory = System.getProperty("user.dir") + "/src/test/resources/TestImages/";
        String[] imageFilenames = {
                "000090025.jpg",
                "000090029.jpg",
                "000090031.jpg",
                "MetadataTest.JPG"
        };

        for(String file : imageFilenames){
            var imgData = new ImageData();
            imgData.setPath(imageDirectory + file);
            images.add(imgData);
        }

        try {
            PDF.generate(images, System.getProperty("user.dir") + "/target/", "PDFTest.pdf", PDF.Mode.DYNAMIC);
        }
        catch(Exception e) {
            fail("Failed to generate path-specified PDF: " + e.getMessage());
        }
    }
}