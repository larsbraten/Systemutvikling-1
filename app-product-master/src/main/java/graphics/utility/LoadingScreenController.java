package graphics.utility;

import graphics.launcher.MainStage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoadingScreenController class.
 *
 * @author Karl Labrador
 */
public class LoadingScreenController implements Initializable {
    private static Logger logger = Logger.getLogger(LoadingScreenController.class);

    @FXML
    private Pane rootPane;

    /**
     * Initializes the controller
     * @param url URL
     * @param resourceBundle ResourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Starting LoadingScreen");
        new LoadingScreen().start();
    }

    /**
     * LoadingScreen.
     * Handles loading screen operations.
     */
    class LoadingScreen extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);

                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    MainStage mainStage = new MainStage();

                    try {
                        mainStage.start(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    rootPane.getScene().getWindow().hide();
                    logger.info("LoadingScreen hidden");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
