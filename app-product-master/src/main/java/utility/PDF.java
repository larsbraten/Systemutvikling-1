package utility;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import hibernate.model.ImageData;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


/**
 * PDF Generator Class
 *
 * @author Arvid Kirkbakk
 * @author Mats Sollid Eide
 * @author Karl Labrador
 */
public class PDF {
    private static final Logger logger = Logger.getLogger(PDF.class);

    // Maximum image size allowed in the document
    private static final int MAX_IMAGE_WIDTH = 1000, MAX_IMAGE_HEIGHT = 720;

    private static final int BIG_THRESHOLD = 400, SMALL_THRESHOLD = 200;
    private static final float EDGE_MARGIN = 4;

    private static FileOutputStream fos;
    private static PdfWriter writer;
    private static PdfDocument pdf;
    private static Document doc;

    /**
     * Specifies the mode for the PDF generation
     */
    public enum Mode{
        /**
         * Each image is printed its own A4 page
         */
        A4,

        /**
         * Each page in the PDF document is cropped to exactly match the size of each image
         */
        CROPPED,

        /**
         * Each A4 page in the document will feature multiple images if enough space is available
         */
        DYNAMIC
    }

    /**
     * Generates a PDF in a given directory.
     * Calls the appropriate private method based on the given mode.
     *
     * @param images A list of the ImageData objects that will appear in the generated PDF
     * @param destFolder The path for the destination folder of the generated PDF
     * @param destFilename The filename for the generated PDF. Filetype must be included.
     * @param mode One of the modes in the Mode enum.
     * @return The path to the generated PDF
     */
    public static String generate(List<ImageData> images, String destFolder, String destFilename, Mode mode){
        logger.info("generate() was called with " + images.size() + " images, folder: " +
               destFolder + ", filename: " + destFilename + ", Mode: " + mode);
        switch (mode){
            case A4: return generateA4(images, destFolder, destFilename);
            case CROPPED: return generateCropped(images, destFolder, destFilename);
            case DYNAMIC: return generateDynamic(images, destFolder, destFilename);
        }
        // Never reached since all Mode values are handled
        return null;
    }

    /**
     * Default generate method if the mode is not specified.
     * @param images A list of the ImageData objects that will appear in the generated PDF
     * @param destFolder The path for the destination folder of the generated PDF
     * @param destFilename The filename for the generated PDF. Filetype must be included.
     * @return The path to the generated PDF
     */
    public static String generate(List<ImageData> images, String destFolder, String destFilename){
        return generate(images, destFolder, destFilename, Mode.A4);
    }

    /**
     * Generates a PDF with a default name in a default directory.
     * Calls the appropriate private method based on the given mode.
     *
     * @param images A list of the ImageData objects that will appear in the generated PDF
     * @param mode One of the modes in the Mode enum.
     * @return The path to the generated PDF
     */
    public static String generate(List<ImageData> images, Mode mode){
        logger.info("generate() was called with " + images.size() + " images, Mode: " + mode);
        String[] dest = getDefaultDestination();
        return generate(images, dest[0], dest[1], mode);
    }


    /**
     * Called when a destination path for the PDF is not specified
     * @return A String array containing {Folder path, filename} for the destination of a generated PDF
     */
    private static String[] getDefaultDestination(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss_dd.MM.yyyy");

        // Windows Path
        String destFolder = "C:/Users/" + System.getProperty("user.name") + "/Documents/Generated PDFs/";
        String destFilename = "Album " + dtf.format(java.time.LocalDateTime.now()) + ".pdf";

        return new String[]{destFolder, destFilename};
    }

    /**
     * Initializes the document
     */
    private static void initDocument(String destination, PageSize pageSize) throws FileNotFoundException {
        logger.info("Initializing document ...");
        fos = new FileOutputStream(destination);
        writer = new PdfWriter(fos);
        pdf = new PdfDocument(writer);
        doc = new Document(pdf, pageSize);
    }

    /**
     * Creates a directory at the specified path, if that directory does not already exist
     *
     * @param dirName The full path to the to-be-made directory
     * @return True if the directory was created successfully or already exists. False if it failed to create the directory
     */
    private static boolean makeDirectory(String dirName){
        File directory = new File(dirName);
        if(!directory.exists()) {
            logger.info("Creating directory: " + dirName);
            return directory.mkdir();
        }
        logger.info("Found existing directory at: " + dirName);
        return true;
    }

    //******************************PRIVATE METHODS FOR THE DIFFERENT MODES*********************************\\

    /**
     * PDF generator which constant A4-sized pages. Images will be layed out to fit each page as best as possible
     *
     * @param images
     * @param destFolder
     * @param destFilename
     * @return The full destination path as a String, null if a try block fails
     */
    private static String generateA4(List<ImageData> images, String destFolder, String destFilename){
        logger.info("Generating A4  PDF with " + images.size() + " images to path: " + destFolder + destFilename);

        if (destFolder.equals("") || destFilename.equals("")) {
            throw new IllegalArgumentException("Cannot generate PDF document without a target folder and filename");
        }

        makeDirectory(destFolder);
        try{
            initDocument(destFolder + destFilename, PageSize.A4);
        }catch(FileNotFoundException e){
            return null;
        }

        doc.setMargins(EDGE_MARGIN, EDGE_MARGIN, EDGE_MARGIN, EDGE_MARGIN);

        int currentPage = 1;
        for (ImageData imageData : images) {
            try {
                Image img = new Image(ImageDataFactory.create(imageData.getPath()));
                logger.info("Created Image object");

                // Scales images to line up with th edge margin
                img.scaleToFit(PageSize.A4.getWidth() - 2 * EDGE_MARGIN, PageSize.A4.getHeight() - 2 * EDGE_MARGIN);
                img.setFixedPosition(currentPage, (PageSize.A4.getWidth()-img.getImageScaledWidth())/2,
                        (PageSize.A4.getHeight() - img.getImageScaledHeight())/2);
                doc.add(img);
                logger.info("Added image to document");

                // Adds a new page if the final page has not been reached
                if (currentPage != images.size()-1) {
                    doc.add(new AreaBreak());
                    logger.info("Added new page to" +doc);
                }
            } catch (MalformedURLException ex){
                logger.error("Exception caught", ex);
            }
            currentPage++;
        }

        logger.info("All images have been added, closing document ...");
        doc.close();
        return destFolder + destFilename;
    }


    /**
     * Generates a PDF document that contains all images passed to the method.
     * Each page is formatted to match the image size
     *
     * @param images a List object that contains ImageData objects
     * @param destFolder a destination folder as a String
     * @param destFilename a destination filename as a String
     * @return The full destination path as a String, null if a try block fails
     */
    private static String generateCropped(List<ImageData> images, String destFolder, String destFilename) {
        logger.info("Start PDF generation of cropped images to path: "+destFolder+destFilename);
        if (destFolder.equals("") || destFilename.equals("")) {
            throw new IllegalArgumentException("Cannot generate PDF document without a target folder and filename");
        }

        makeDirectory(destFolder);

        // Initializes the document with a tiny first page
        try{
            initDocument(destFolder + destFilename, new PageSize(new Rectangle(0.01f, 0.01f)));
        }catch (FileNotFoundException e){
            return null;
        }

        // Sets all margins to 0 to avoid visible edges around the images
        doc.setMargins(0, 0, 0, 0);

        // Loops through the images and adds them to the document one by one
        for (ImageData imgData : images){
            try {
                // Constructs an Itext Image object
                var img = new Image(ImageDataFactory.create(imgData.getPath()));
                logger.info("Constructed Itext Image" );

                // Scales the image within the limits while keeping the original aspect ratio
                if(img.getImageWidth() > MAX_IMAGE_WIDTH || img.getImageHeight() > MAX_IMAGE_HEIGHT) {
                    float scaleFactor = Math.min(MAX_IMAGE_WIDTH / img.getImageWidth(), MAX_IMAGE_HEIGHT / img.getImageHeight());
                    img.scale(scaleFactor, scaleFactor);
                }

                // Creates a new page with the same size as the image, then adds the image to the page
                doc.add(new AreaBreak(new PageSize(img.getImageScaledWidth(), img.getImageScaledHeight())));
                doc.add(img);
                logger.info("Added page and inserted");

            } catch (MalformedURLException ex) {
                logger.error("Bad filename in an ImageData object", ex);
            }
        }

        logger.info("All images have been added, closing document ...");
        doc.close();
        return destFolder + destFilename;
    }

    /**
     * Generates a PDF with a constant page size, and tries to place the images where they fit best
     * @param images a List object that contains ImageData objects
     * @param destFolder a destination folder as a String
     * @param destFilename a destination filename as a String
     * @return The full destination path as a String, null if a try block fails
     */
    private static String generateDynamic(List<ImageData> images, String destFolder, String destFilename){
        if (destFolder.equals("") || destFilename.equals("")) {
            throw new IllegalArgumentException("Cannot generate PDF document without a target folder and filename");
        }

        logger.info("Starting generation of PDF with dynamicly placed images ...");

        makeDirectory(destFolder);
        try {
            initDocument(destFolder + destFilename, PageSize.A4);
        }catch (FileNotFoundException e){
            return null;
        }

        // Specifies the width of each column, large numbers are scaled down
        // The length of each array equals the number of columns in the table
        float[] tableWidths2 = {1000, 1000};
        float[] tableWidths3 = {1000, 1000, 1000};
        // Generate tables
        Table table2 = new Table(tableWidths2);
        Table table3 = new Table(tableWidths3);

        logger.info("Converting ImageData to iText Image objects ...");
        List<Image> itxtImages = images
                .stream()
                .map(imageData -> {
                    try {
                        return new Image(ImageDataFactory.create(imageData.getPath()));
                    } catch (MalformedURLException ex) {
                        logger.error("Exception caught", ex);
                        return null;
                    }
                }).collect(Collectors.toList());

        for(Image img : itxtImages){
            logger.info("Placing image ...");
            if(img.getImageWidth() > BIG_THRESHOLD){
                doc.add(img);
            }
            else if(img.getImageWidth() > SMALL_THRESHOLD){
                table2.addCell(img);
            }
            else{
                table3.addCell(img);
            }
        }
        // Table cleanup
        removeBorder(table2);
        removeBorder(table3);

        doc.add(table2);
        doc.add(table3);

        logger.info("All images have been added, closing document ...");
        doc.close();
        return destFolder + destFilename;
    }

    // Method to remove borders from each cell in Table
    private static void removeBorder(Table table) {
        logger.info("Removing cell borders for image tables ..." );
        for (IElement iElement : table.getChildren()) {
            ((Cell)iElement).setBorder(Border.NO_BORDER);
        }
    }
}
