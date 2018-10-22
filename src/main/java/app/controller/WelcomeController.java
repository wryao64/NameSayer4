package app.controller;

import app.DialogGenerator;
import app.Main;
import app.name.NamesDatabase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;

public class WelcomeController {
    private Stage _stage;

    @FXML TextField nameInput;

    public void initialize() {
        _stage = Main.getStage();

        NamesDatabase namesDB = new NamesDatabase();

        // setup autocomplete
        TextFields.bindAutoCompletion(nameInput,
                textField -> namesDB.getSuggestedNames(nameInput.getText()));
    }

    @FXML
    private void uploadButtonPress(){
        // opens window to choose file
        FileChooser fc = new FileChooser();
        configureFileChooser(fc);
        File file = fc.showOpenDialog(_stage);
        if (file != null) {
            openFile(file);
        }
    }

    @FXML
    private void submitButtonPress(){
        if(nameInput.getText().trim() != null && !nameInput.getText().trim().isEmpty()){
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Edit.fxml"));
                loader.setController(new EditController(nameInput.getText().trim()));
                Parent editView = loader.load();
                _stage.setScene(new Scene(editView));
            } catch (IOException e){
                DialogGenerator.showErrorMessage("Error processing name");
            }
        } else {
            DialogGenerator.showErrorMessage("Error: Name not inputted.");
        }
    }

    private void configureFileChooser(final FileChooser fc) {
        fc.setTitle("Open Name File");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text File", "*.txt")
        );
    }

    private void openFile(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Edit.fxml"));
            loader.setController(new EditController(file));
            Parent editView = loader.load();
            _stage.setScene(new Scene(editView));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
