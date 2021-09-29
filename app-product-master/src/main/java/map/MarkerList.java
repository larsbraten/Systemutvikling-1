package map;

import hibernate.model.ImageData;
import hibernate.model.Metadata;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;


/**
 * A class containing static methods used to editing Marker and ImageData -lists, and generate JavaScript readable arrays.
 * @author Robin Vold
 * @see Marker
 * @see Map*/
public class MarkerList {
    private final static Logger logger = Logger.getLogger(MarkerList.class);
    private static List<String[]> multipleImgMarkerList = new ArrayList<>();
    private static List<String[]>  singleImgMarkerArray = new ArrayList<>();
    private static boolean removedNoneGPSImg = false;



    /**Accessor method to get the list with the marker information for single markers
     * @return The list with string arrays with marker information. If the list is empty, there are none singel markers*/
    public static List<String[]>  getSingleImgMarkerArray(){
        return singleImgMarkerArray;
    }

    /**Accessor method to get the list with the marker information for multi-markers
     * @return The list with string arrays with marker information. If the list is empty, there are none multi-markers*/
    public static List<String[]> getMultipleImgMarkerList(){
        return multipleImgMarkerList;
    }


    /**
     * Method to filter a list of {@code Marker}-objects
     * Filters them into two lists, that is accessed with {@code getMultipleImgMarkerList} and {@code getSingleImgMarkerArray}
     * @param markerList list with markers that is desired to be filtered.
     * @return returns {@code true} if the elements is filtered, or {@code false} if the argument is empty or {@code null}. */
    public static boolean filterJSMarkers(List<Marker> markerList){
        if(markerList == null || markerList.isEmpty()){
            return false;
        }
        for(Marker m : markerList){
            if(m.containsMultipel()){
                multipleImgMarkerList.add(m.toJSMarkerFormat());
            }
            else {
                singleImgMarkerArray.add(m.toJSMarkerFormat());
            }
        }
        logger.debug("Filtered markerlist");
        return true;
    }




    /**
     * Method to remove any {@code ImageDate}-objects that does not contain metadata, or invalid position data.
     * @param imageData The list where every {@code ImageData}-object will be checked if it is valid for the map.
     * @return Returns a list with only valid {@code ImageData}-objects. */
    public static List<ImageData> removeNoneGPSImg(List<ImageData> imageData){
        List<ImageData> imgData = new ArrayList<>(imageData.size());
        for(int i=0;i<imageData.size();i++){
            Metadata metadata = imageData.get(i).getMetadata();
            boolean bool = (metadata != null) && (metadata.getLatitude() < 90.0 && metadata.getLatitude() > 0.0) && (metadata.getLongitude() < 180.0 && metadata.getLongitude() > 0.0);
            if(bool){
                imgData.add(imageData.get(i));
            }
        }
        removedNoneGPSImg = true;
        logger.debug("Removed none-gps images.");
        return imgData;
    }



    /**
     * Method that transforms a {@code ImageData}-list to a list containing {@code Marker}-objects
     * @param imgData List containing {@code ImageData}-objects
     * @return Returns a unsorted list containing {@code Marker}-objects, or an empty list if the received list is empty or null. */
    public static List<Marker> transformToMarkerList(List<ImageData> imgData){
        if(imgData == null || imgData.isEmpty()){
            return new ArrayList<>();
        }
        if(!removedNoneGPSImg){
            imgData = removeNoneGPSImg(imgData);
        }
        boolean[] loopList = new boolean[imgData.size()];
        List<Marker> markers = new ArrayList<>(imgData.size());
        int counter = imgData.size();

        for(int i=0;i<counter;i++) {
            if (loopList[i]) {
                markers.add(null);
            }else {
                try{
                    Marker marker = new Marker(imgData.get(i));
                    loopList[i] = true;
                    double[] latlong1 = {marker.getCompareImage().getMetadata().getLatitude(), marker.getCompareImage().getMetadata().getLongitude()};
                    for(int j=i+1;j<counter;j++){
                        if(!loopList[j]) {
                            double[] latlong2 = {imgData.get(j).getMetadata().getLatitude(), imgData.get(j).getMetadata().getLongitude()};
                            double value = 0.005;
                            boolean isCloseLat = latlong1[0]-latlong2[0]<value && latlong2[0]-latlong1[0]<value;
                            boolean isCloseLong = latlong1[1]-latlong2[1]<value && latlong2[1]-latlong1[1]<value;
                            if(isCloseLat && isCloseLong){
                                marker.addImage(imgData.get(j));
                                loopList[j] = true;
                            }
                        }
                    }
                    markers.add(marker);
                }catch (IllegalArgumentException ex){
                    logger.error(ex.getMessage());
                    markers.add(null);
                    loopList[i] = true;
                }
            }
        }
        while(markers.remove(null));
        logger.debug("Transformed ImageDataList to List containing Markers-objects");
        return markers;
    }


    /**
     * Method parsing Java list of strings to JavaScript readable array, with both {@code string} and {@code number} values
     * @param list Java list of strings
     * @return Returns a String that will be interpreted as an array in JavaScript*/
    public static String transformToJavascriptArraySingle(List<String[]> list) {
        logger.debug("Parsing Java array to JS array");
        StringBuffer sb = new StringBuffer();
        sb.append("[");

        for (String[] str : list) {
            str[0] = str[0].replace("\\","\\\\");  //Necessary for the backslashes to join the transfer to JS
            sb.append("[");
            sb.append("\"").append(str[0]).append("\"").append(", ");
            sb.append(str[1]).append(", ");
            sb.append(str[2]).append("]").append(", ");
        }

        if (sb.length() > 1) {
            sb.replace(sb.length() - 2, sb.length(), "");
        }

        sb.append("]");
        return sb.toString();
    }



    /**
     * Method parsing Java list of strings to JavaScript readable array, with both {@code string} and {@code number} values
     * @param list Java list of strings
     * @return Returns a String that will be interpreted as an array in JavaScript*/
    public static String transformToJavascriptArrayMultiple(List<String[]> list) {
        logger.debug("Parsing Java array to JS array");
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        list.forEach(strArr -> {
            sb.append("[");
            sb.append(strArr[0]).append(", ").append(strArr[1]).append(", ");
            for(int i=2;i<strArr.length;i++){
                strArr[i] = strArr[i].replace("\\","\\\\");
                sb.append("\"").append(strArr[i]).append("\"").append(", ");

            }
            sb.replace(sb.length() - 2, sb.length(), "");
            sb.append("]").append(", ");
        });
        if (sb.length() > 1) {
            sb.replace(sb.length() - 2, sb.length(), "");
        }

        sb.append("]");
        return sb.toString();
    }

}
