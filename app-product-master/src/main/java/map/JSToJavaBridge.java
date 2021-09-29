package map;

import org.apache.log4j.Logger;
import java.util.Arrays;


/**
 * Class used to communicate between JavaScript and Java. Each method is called by the JavaScript.
 * Then this class will call on relevant methods in the {@code Map} class.
 *
 * @author Robin Vold
 * @see Map
 */
public class JSToJavaBridge {
    private final static Logger logger = Logger.getLogger(JSToJavaBridge.class);

    /**
     * Loggermethod supposed to be used by the JavaScript. Will log info using {@code log4j}.
     * @param loggerInfoMessage The string that needs to be logged. */
    public void loggerInfo(String loggerInfoMessage){
        logger.info(loggerInfoMessage);
    }

    /**
     * Loggermethod supposed to be used by the JavaScript. Will log debug information using {@code log4j}.
     * @param loggerDebugMessage The string that needs to be logged. */
    public void loggerDebug(String loggerDebugMessage){
        logger.debug(loggerDebugMessage);
    }

    /**
     * Loggermethod supposed to be used by the JavaScript. Will log error information using {@code log4j}.
     * @param loggerErrorMessage The string that needs to be logged. */
    public void loggerError(String loggerErrorMessage){
        logger.error(loggerErrorMessage);
    }

    /**
     * Method run by the JavaScript when the user clicks a marker/image
     * @param imgPath The image path of the clicked image*/
    public void clickedMarker(String imgPath){
        Map.displayClickedImage(imgPath);
    }
}
