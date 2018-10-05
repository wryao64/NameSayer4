package app.controller;

import app.Main;
import app.Name;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WelcomeController {
    private Stage _stage;

    @FXML
    TextField nameInput;

    public void initialize() {
        _stage = Main.getStage();
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
        if(nameInput.getText() != null && !nameInput.getText().trim().isEmpty()){
            List<Name> name = new ArrayList<>();
            name.add(new Name(nameInput.getText(), null));
            goToList(name);
        } else {
            // TODO: show error message that no input...
            System.out.println("Error no input");
        }
    }

    private void configureFileChooser(final FileChooser fc) {
        fc.setTitle("Open Name File");
        fc.setInitialDirectory(new File(Main.ASSETS_LOCATION));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text File", "*.txt")
        );
    }

    private void openFile(File file) {
        try {
            // load in names from file
            BufferedReader bf = new BufferedReader(new FileReader(file));

            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
        } catch(IOException e) {
            System.out.println("Could not open file");
        }
        System.out.println("Should go to next screen");
    }

    private void goToList(List<Name> names){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(names));
            Parent nameView = loader.load();
            _stage.setScene(new Scene(nameView));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
