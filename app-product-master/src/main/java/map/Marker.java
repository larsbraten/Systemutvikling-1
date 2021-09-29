package map;

import hibernate.model.ImageData;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;


/**
 * Class used to representing the marker on the map.
 * @author Robin Vold
 */
public class Marker {
    private final static Logger logger = Logger.getLogger(Marker.class);
    private List<ImageData> imageData;
    private boolean containsMultipel;

    /**
     * Constructor for a marker
     * @param imageData The first ImageData-object connected to this marker
     * @throws IllegalArgumentException if the given ImageData object is not valid*/
    public Marker(ImageData imageData) throws IllegalArgumentException{
        if(imageData == null || imageData.getMetadata() == null
                || imageData.getMetadata().getLatitude() > 90.0
                || imageData.getMetadata().getLatitude() < 0.0
                || imageData.getMetadata().getLongitude() > 180.0
                || imageData.getMetadata().getLongitude() < 0.0){
            throw new IllegalArgumentException("Needs to be valid ImageData object");
        }
        this.imageData = new ArrayList<>();
        this.containsMultipel = false;
        this.imageData.add(imageData);
    }

    /**
     * Adds a ImageData object to the marker. It also sets {@code containsMutipel} to {@code true}.
     * @param imgData The new ImageData-object connected to this marker
     * @return Returns {@code true} or {@code false} whenever the ImageData object was added or not. */
    public boolean addImage(ImageData imgData){
        if(imgData == null || imgData.getMetadata() == null
                || imgData.getMetadata().getLatitude() > 90.0
                || imgData.getMetadata().getLatitude() < 0.0
                || imgData.getMetadata().getLongitude() > 180.0
                || imgData.getMetadata().getLongitude() < 0.0){
            return false;
        }
        boolean imageDataAdded = imageData.add(imgData);
        if(!containsMultipel){
            containsMultipel = imageDataAdded;
        }
        return imageDataAdded;
    }


    /**
     * @return  {@code true} if the marker contains multiple ImageData-objects og {@code false} if not*/
    public boolean containsMultipel() {
        return containsMultipel;
    }

    /**
     * @return Returns the compare ImageData-object, which is the first ImageData-object*/
    public ImageData getCompareImage(){
        return this.imageData.get(0);
    }

    /**
     * @return Returns a array of strings containing image-path(s), latitude and longitude. */
    public String[] toJSMarkerFormat(){
        logger.debug("Marker parsed to string");
        if(!containsMultipel) {
            return new String[]{this.imageData.get(0).getPath(), Double.toString(this.imageData.get(0).getMetadata().getLatitude()), Double.toString(this.imageData.get(0).getMetadata().getLongitude())};
        }
        else{
            String[] collectionMarker = new String[this.imageData.size()+2];
            collectionMarker[0] = Double.toString(this.imageData.get(0).getMetadata().getLatitude());
            collectionMarker[1] = Double.toString(this.imageData.get(0).getMetadata().getLongitude());
            for(int i=2;i<collectionMarker.length;i++){
                collectionMarker[i] = this.imageData.get(i-2).getPath();
            }
            return collectionMarker;
        }
    }


    /**
     * @return Returns the field-values in a String format */
    @Override
    public String toString() {
        return "Marker{" +
                "imageData=" + this.imageData +
                ", containsMultipel=" + containsMultipel +
                '}';
    }
}
