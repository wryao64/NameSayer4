package app;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

public class DialogGenerator {

    /**
     * Creations an option dialog
     * @param title Title of dialog
     * @param message Message to display to user
     * @param option1 String message for option 1
     * @param option2 String message for option 2
     * @return true if option1 is selected, false is option2 is selected
     */
    public static boolean showOptionsDialog(String title, String message, String option1, String option2){
        ButtonType option1Button = new ButtonType(option1);
        ButtonType option2Button = new ButtonType(option2);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, option1Button, option2Button);
        alert.setTitle(title);
        alert.setHeaderText(null);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("../resources/css/styles.css");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(option2Button) == option1Button){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates an error dialog
     * @param message Message to display to the user
     */
    public static void showErrorMessage(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("../resources/css/styles.css");

        alert.showAndWait();
    }

    /**
     * Creates an ok dialog
     * @param title Title of window
     * @param message Message to display to user
     */
    public static void showOkMessage(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("../resources/css/styles.css");

        alert.showAndWait();
    }
}
