package map;

import hibernate.model.ImageData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;
import java.util.List;

/**
 * Class used to representing the map. It uses a webview node to access a HTML file that runs OSM with leaflet.
 * This class communicates directly with the HTML and JavaScript files. However the JavaScript communicates through the {@code JSToJavaBridge}-object.
 * @author Robin Vold
 * @see JSToJavaBridge
 * @see MarkerList
 */
public class Map {
    private static WebView webView = new WebView();
    private static WebEngine webEngine = webView.getEngine();
    private static List<ImageData> imageDataList = null;
    private final static Logger logger = Logger.getLogger(Map.class);
    private final static JSToJavaBridge jSToJavaBridge = new JSToJavaBridge();
    private final static ObjectProperty<ImageData> FULL_IMAGE_REQUEST = new SimpleObjectProperty<>();


    /**
     * Method used to initialize the map
     * @return Returns a {@code WebView}-node that the {@code AppLayoutController} will place in the desired container. */
    public static WebView initializeMap() {
        logger.info("Initializing map");
        String pathToHTML = String.valueOf(Map.class.getResource("/mapResources/OSMLeaflet.html"));
        logger.debug("Path to HTML-map: " + pathToHTML);
        webEngine.load(pathToHTML);

        webEngine.getLoadWorker().stateProperty().addListener(
                (ChangeListener) (observable, oldValue, newValue) -> {
                    System.out.println(newValue);
                    if (newValue == Worker.State.SUCCEEDED) {
                        //document finished loading
                        logger.debug("HTML loaded");
                        runScriptCommands();
                    }
                }
        );

        //Prints stacktrace to the logger. If we had time, this method should also return null if the HTML-doc was unloadable. Then the AppLayoutController should have a
        // if-test checking if it recieves null. In that case a message should be printed to the user.
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                logger.error("Failed to load HTML, this is the given stack trace: " + newExc.getMessage());
            }
        });

        return webView;
    }


    /**
     * Method that runs initialization JS commands. These include adding the JS-bridge to the Script, and adding the map to the Webview
     * @see JSToJavaBridge*/
    private static void runScriptCommands() {
        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("jSToJavaBridge", jSToJavaBridge);
        logger.debug("Added jSToJavaBridge to the JS");
        webEngine.executeScript("addMap()");
    }


    /**Method used for adding markers to the map.
     * @param imageData A List containing the {@code ImageData}-objects that is desired to be placed on the map.
     * */
    public static void addMarkers(List<ImageData> imageData) {
        logger.info("Adding markers");
        imageDataList = MarkerList.removeNoneGPSImg(imageData);
        List<Marker> markers = MarkerList.transformToMarkerList(imageDataList);
        MarkerList.filterJSMarkers(markers);
        List<String[]>  dataArr = MarkerList.getSingleImgMarkerArray();
        List<String[]> dataMultiList = MarkerList.getMultipleImgMarkerList();

        String command = "addMarkers(" + MarkerList.transformToJavascriptArraySingle(dataArr) + ", " + MarkerList.transformToJavascriptArrayMultiple(dataMultiList) + ")";
        webEngine.executeScript(command);
    }



    /**Method ran by the {@code JSToJavaBridge} when the user clicks an image.
     * @param imgPath The path to the clicked image on the users computer.
     * @see JSToJavaBridge*/
    public static void displayClickedImage(String imgPath) {
        logger.debug("Click with image path: "+imgPath+" received in Java");
        imgPath = imgPath.replace("/","\\");
        ImageData imageData = null;
        for(int i=0;i<imageDataList.size();i++){
            if(imageDataList.get(i).getPath().trim().equals(imgPath.trim())){
                imageData = imageDataList.get(i);
                break;
            }
        }
        FULL_IMAGE_REQUEST.setValue(imageData);
        FULL_IMAGE_REQUEST.setValue(null);
    }


    /**Accessor method for the FULL_IMAGE_REQUEST
     * @return Returns a the generic ObjectProperty with ImageData. */
    public static ObjectProperty<ImageData> getFullImageRequest(){
        return FULL_IMAGE_REQUEST;
    }
}

