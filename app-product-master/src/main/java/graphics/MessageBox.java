package graphics;

import javafx.scene.control.Alert;

/**
 * MessageBox class to present messages in a nice box to the client. Can only be used from within a JavaFX stage.
 *
 * @author Karl Labrador
 */
public class MessageBox {
    public static void main(String[] args) {
        error("Error", "The application stumbled upon a problem!");
    }

    /**
     * A generic alert method that creates an alert box
     * @param title the title of the message
     * @param message the message
     * @param alertType the Alert Type
     */
    public static void alert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Displays a message box with the error alert type
     * @param title the title of the message
     * @param message the message
     */
    public static void error(String title, String message) {
        alert(title, message, Alert.AlertType.ERROR);
    }

    /**
     * Displays a message box with the information alert type
     * @param title the title of the message
     * @param message the message
     */
    public static void info(String title, String message) {
        alert(title, message, Alert.AlertType.INFORMATION);
    }

    /**
     * Displays a message box with the confirmation alert type
     * @param title the title of the message
     * @param message the message
     */
    public static void confirmation(String title, String message) {
        alert(title, message, Alert.AlertType.CONFIRMATION);
    }

    /**
     * Displays a message box with the warning alert type
     * @param title the title of the message
     * @param message the message
     */
    public static void warning(String title, String message) {
        alert(title, message, Alert.AlertType.WARNING);
    }
}
