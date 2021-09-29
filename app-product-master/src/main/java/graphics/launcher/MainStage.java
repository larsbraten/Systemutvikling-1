package graphics.launcher;

import graphics.utility.AppLayoutController;
import hibernate.model.ImageData;
import hibernate.model.Metadata;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utility.PDF;

/**
 * @author Karl Labrador, Eivind Berger-Nilsen
 * Initilizes and launches aplication. Also contains inner class
 * for user-query/dialog methods.
 */

public class MainStage extends Application {

    private static final Logger LOGGER = LogManager.getLogger(MainStage.class);
    private static Stage mainStage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        mainStage = primaryStage;
        primaryStage.setTitle("Flagship");
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("app-icon-temp.png").openStream()));
        Parent root = FXMLLoader.load(getClass().getResource("/views/AppLayout2.fxml"));
        primaryStage.setFullScreenExitHint("Press ESC or upper right menu-icon to exit.");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("ESC"));
        AppLayoutController.getRequestFullScreenProperty().addListener((observable, oldValue, newValue) -> {
            primaryStage.setFullScreen(newValue);
        });
        Scene mainScene = new Scene(root);
        mainScene.getStylesheets().add("Styling.css");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(810);
        primaryStage.setMinHeight(400);
        LOGGER.info("Success! loading window...");
        mainScene.setRoot(root);
        LOGGER.info("Starting program. Loading graphical elements...");
        primaryStage.show();

    }

    /**
     * @author Eivind Berger_Nilsen
     * UserPrompter-class contains user-dialog windows utilized in this application.
     * This class were declared as inner-class in the launcher-class for so ownership of the
     * dialog could easily be passed to the primary stage. This prevents dialogs from minimizing the main
     * window once visible in fullscreen mode.
     */

    public static class UserPrompter {

        private UserPrompter(){}

        /**
         * Displays a information alert to the user.
         * @param title the title.
         * @param header the header.
         * @param message the message.
         */

        public static void showMessage(String title, String header, String message){
            Alert programMessage = new Alert(Alert.AlertType.INFORMATION);
            programMessage.initOwner(mainStage);
            programMessage.setTitle(title);
            programMessage.setHeaderText(header);
            programMessage.setContentText(message);
            programMessage.initModality(Modality.APPLICATION_MODAL);
            programMessage.show();
        }

        /**
         * Prompts user for a folder destination and prints inputted files.
         * @param files to print.
         * @return true if success.
         */

        public static boolean printPDF(Collection<File> files){
            File userInput = queryDirectoryDestination("Please provide a suitable path and filename.");
            if(userInput != null){
                LOGGER.info("Printing PDF: " + userInput.getName() + " to path " + userInput.getAbsolutePath());
                List<ImageData> imageData = files
                    .stream()
                    .peek(file -> LOGGER.info("Printing to PDF: " + file.getAbsolutePath()))
                    .map(file -> {
                        ImageData newImageData = new ImageData();
                        newImageData.setPath(file.getAbsolutePath());
                        return newImageData;
                    })
                    .collect(Collectors.toList());

                PDF.generate(imageData, userInput.getParent() + "\\", userInput.getName() + ".PDF", PDF.Mode.CROPPED);
            }
            return false;
        }

        /**
         * Query user for a folder, and return the file within it with format: .jpeg, .jpg, .bmp, .gif, .png
         * @return Image files.
         */

        public static Set<File> queryDirectory(){
            try {
                return Arrays.stream(new DirectoryChooser().showDialog(mainStage).listFiles())
                    .filter(file -> {
                        String suffix = file.getAbsolutePath();
                        return file.isFile() && (suffix.contains(".jpeg") || suffix.contains("jpg") || suffix.contains("bmp") ||
                            suffix.contains(".gif") || suffix.contains(".png"));
                    }).collect(Collectors.toSet());
            }catch (NullPointerException e){
                LOGGER.info("Folder-selection aborted.");
            }
            return null;
        }

        /**
         * Query user for a selection of files and returns: .jpeg, .jpg, .bmp, .gif, .png
         * @return Image files.
         */

        public static Set<File> queryFiles(){
            try {
                return new FileChooser().showOpenMultipleDialog(mainStage)
                    .stream()
                    .filter(file -> {
                        String suffix = file.getAbsolutePath().toLowerCase();
                        return file.isFile() && (suffix.contains(".jpeg") || suffix.contains("jpg") || suffix.contains("bmp") ||
                            suffix.contains(".gif") || suffix.contains(".png"));
                    }).collect(Collectors.toSet());
            }catch (NullPointerException e){
                LOGGER.info("File-selection aborted.");
            }
            return null;
        }

        /**
         * Queries user for a inputted string while displaying a custom message.
         * @param message The message to display.
         * @return The inputted string.
         */

        public static String queryName(String message){
            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.initModality(Modality.APPLICATION_MODAL);
            textInputDialog.initOwner(mainStage);
            textInputDialog.setTitle(message);
            textInputDialog.setContentText(message);
            textInputDialog.showAndWait();
            return textInputDialog.getResult();
        }

        /**
         * Queries user for destination folder.
         * @param title
         * @return
         */

        public static File queryDirectoryDestination(String title){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("PDF-document", "PDF"));
            return fileChooser.showSaveDialog(mainStage);
        }

        public static String queryOverWritePermission(){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Overwrite existing album?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Album already exits.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(mainStage);
            alert.showAndWait();
            return alert.getResult().getText();
        }
    }
}
