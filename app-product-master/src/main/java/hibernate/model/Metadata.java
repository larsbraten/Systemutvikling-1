package hibernate.model;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import javax.persistence.*;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * Metadata Class to retrieve specific metadata from an image file.
 * General usage is calling the static method {@link #generate}.
 *
 * @author Karl Labrador
 * @author Arvid Kirkbakk
 * @author Lars-Håvard Bråten
 */
@Embeddable
public class Metadata {
    private static Logger logger = Logger.getLogger(Metadata.class);

    @Column(name = "latitude", nullable = true)
    private double latitude;

    @Column(name = "longitude", nullable = true)
    private double longitude;

    @Column(name = "height", nullable = true)
    private int height;

    @Column(name = "width", nullable = true)
    private int width;

    @Column(name = "make", nullable = true)
    private String make;

    @Column(name = "model", nullable = true)
    private String model;

    @Column(name = "captured", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date captured;

    /**
     * Reads metadata from the specified absolute filepath and generates a Metadata object with available data
     * @param filepath Absolute path to an image file
     * @return A Metadata object if it successfully reads metadata, null if it catches an exception
     */
    public static Metadata generate(String filepath) {
        File file = new File(filepath);

        try {
            com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(file);

            GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            ExifDirectoryBase exifdb = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);

            // Variables with default values
            double tmp_latitude = 0.0;
            double tmp_longitude = 0.0;
            int tmp_height = 0;
            int tmp_width = 0;
            String tmp_make = "";
            String tmp_model = "";
            Date tmp_captured = null;

            // Geolocation
            try {
                GeoLocation geolocation = gps.getGeoLocation();

                if (geolocation != null) {
                    tmp_latitude = geolocation.getLatitude();
                    tmp_longitude = geolocation.getLongitude();

                    logger.info("Geolocation called and returned latitude=" + (geolocation.getLatitude() + ", longitude=" + geolocation.getLongitude()));
                }
            } catch (NullPointerException ex) {
                logger.warn("Exception caught: " + Arrays.toString(ex.getStackTrace()));
            }

            // Height & Width
            FileInputStream fis = new FileInputStream(file);
            FilterInputStream filter = new BufferedInputStream(fis);

            FileType filetype = FileTypeDetector.detectFileType(filter);
            logger.info("Filetype detected: " + filetype);
            
            if (filetype == FileType.Jpeg) {
                JpegDirectory jpegdir = metadata.getFirstDirectoryOfType(JpegDirectory.class);

                if (jpegdir.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT) != null) {
                    tmp_height = jpegdir.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
                }

                if (jpegdir.getInteger(JpegDirectory.TAG_IMAGE_WIDTH) != null) {
                    tmp_width = jpegdir.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
                }
            }
            else if (filetype == FileType.Png) {
                PngDirectory pngdir = metadata.getFirstDirectoryOfType(PngDirectory.class);

                if (pngdir.getInteger(PngDirectory.TAG_IMAGE_HEIGHT) != null) {
                    tmp_height = pngdir.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
                }

                if (pngdir.getInteger(PngDirectory.TAG_IMAGE_WIDTH) != null) {
                    tmp_width = pngdir.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
                }
            }

            // Make, Model & Capture Date
            try {
                if (exifdb.getString(ExifDirectoryBase.TAG_MAKE) != null) {
                    tmp_make = exifdb.getString(ExifDirectoryBase.TAG_MAKE);
                }

                if (exifdb.getString(ExifDirectoryBase.TAG_MODEL) != null) {
                    tmp_model = exifdb.getString(ExifDirectoryBase.TAG_MODEL);
                }

                // Date
                if (exifdb.getDate(ExifDirectoryBase.TAG_DATETIME) != null) {
                    tmp_captured = exifdb.getDate(ExifDirectoryBase.TAG_DATETIME);
                }
            } catch (NullPointerException ex) {
                logger.warn("Exception caught: " + Arrays.toString(ex.getStackTrace()));
            }

            Metadata res = new Metadata();
            res.setLatitude(tmp_latitude);
            res.setLongitude(tmp_longitude);
            res.setHeight(tmp_height);
            res.setWidth(tmp_width);
            res.setMake(tmp_make);
            res.setModel(tmp_model);
            res.setCaptured(tmp_captured);

            logger.info("Metadata generation result: " + res.toString());

            return res;
        } catch (ImageProcessingException | IOException ex) {
            logger.warn("Exception caught: " + Arrays.toString(ex.getStackTrace()));
        }

        return null;
    }

    /**
     * Get method for latitude
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get method for longitude
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Get method for height
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get method for width
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get method for device make
     * @return make
     */
    public String getMake() {
        return make;
    }

    /**
     * Get method for device model
     * @return model
     */
    public String getModel() {
        return model;
    }

    /**
     * Get method for Date object
     * @return Date
     */
    public Date getCaptured() {
        return captured;
    }

    /**
     * Set method for latitude
     * @param latitude latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Set method for longitude
     * @param longitude longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Set method for height
     * @param height height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Set method for width
     * @param width width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Set method for make
     * @param make make
     */
    public void setMake(String make) {
        this.make = make;
    }

    /**
     * Set method for model
     * @param model model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Set method for captured
     * @param captured captured
     */
    public void setCaptured(Date captured) {
        this.captured = captured;
    }

    /**
     * toString method
     * @return String with variable values
     */
    public String toString() {
        return String.format("[metadata] latitude=%f, longitude=%f, height=%d, width=%d, make=%s, model=%s, captured=%s", getLatitude(), getLongitude(), getHeight(), getWidth(), getMake(), getModel(), getCaptured());
    }
}
