package app.controller;

import app.Main;
import app.Name;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
        // do the file uploading
        System.out.println("Uploading file not yet implemented");
    }

    @FXML
    private void submitButtonPress(){
        if(nameInput.getText() != null && !nameInput.getText().trim().isEmpty()){
            List<String> name = new ArrayList<String>();
            name.add(nameInput.getText());
            goToList(name);
        } else {
            // show error message that no input...
            System.out.println("Error no input");
        }
    }

    @FXML
    private void testMicButtonPressed() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("MicTest.fxml"));
            loader.setController(new MicTestController());
            Parent testMic = loader.load();
            _stage.setScene(new Scene(testMic));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void goToList(List<String> names){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
//            loader.setController(new NameDisplayController(names));
            loader.setController(new NameDisplayController(new ArrayList<Name>())); // placeholder list of names
            Parent nameView = loader.load();
            _stage.setScene(new Scene(nameView));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
