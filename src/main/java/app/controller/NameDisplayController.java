package app.controller;

import app.Main;
import app.Name;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NameDisplayController implements Initializable {
    private Stage _stage;
    private List<Name> _nameList;
    private Name _selectedName = new Name("Placeholder", "Placeholder");

    public NameDisplayController(List<Name> nameList) {
        _nameList = nameList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
    }

    @FXML
    private void practiseButtonPress() {
        // redirect to Record page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Record.fxml"));
            loader.setController(new RecordController(_selectedName, _nameList));
            Parent recordView = loader.load();
            _stage.setScene(new Scene(recordView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToWelcome() {
        // go back to Welcome page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Welcome.fxml"));
            Parent welcomeView = loader.load();
            _stage.setScene(new Scene(welcomeView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editButtonPress() {
        // go back to List page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("List.fxml"));
            loader.setController(new ListController());
            Parent listView = loader.load();
            _stage.setScene(new Scene(listView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
