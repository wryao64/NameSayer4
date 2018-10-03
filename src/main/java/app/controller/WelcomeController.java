package app.controller;

import app.Main;
import app.Name;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WelcomeController {
    @FXML
    TextField nameInput;

    @FXML
    private void uploadButtonPress(){
        // do the file uploading
        System.out.println("Uploading file not yet implemented");
    }

    @FXML
    private void submitButtonPress(){
        List<Name> names = new ArrayList<Name>();
        if(nameInput.getText() != null){
            names.add(new Name(nameInput.getText(), "FILENAME HERE"));
            goToList(names);
        } else {
            // show error message that no input...
        }
    }

    private void goToList(List<Name> names){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("List.fxml"));
            loader.setController(new ListController(names));
            Parent listView = loader.load();
            Main.getStage().setScene(new Scene(listView));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
