package app.controller;

import app.Main;
import app.Name;
import app.NamesDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EditController implements Initializable {
    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();
    private NamesDatabase _namesDB;

    @FXML private ListView<Name> selectedNamesList;
    @FXML private TextField nameInput;

    public EditController(List<Name> names){
        for(Name nameToAdd : names){
            // TODO: Setup names inputted properly with concat...
            _selectedNames.add(nameToAdd);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        _namesDB = new NamesDatabase();

        selectedNamesList.setPlaceholder(new Label("No Names To Practice :("));
        selectedNamesList.getItems().addAll(_selectedNames);
    }

    @FXML
    private void addButtonPress(){
        // TODO: add the name to list of stuff to practice
        String nameStr = nameInput.getText();
        if(nameInput.getText() != null && !nameInput.getText().trim().isEmpty()) {
            Name nameToAdd = createName(nameStr);
            _selectedNames.add(nameToAdd);
            selectedNamesList.setItems(_selectedNames);
            nameInput.setText(""); // clear TextField
        } else {
            // TODO: Set alert error message
            System.out.println("Error: name is blank");
        }
    }

    @FXML
    private void removeButtonPress() {
        // get the selected item and pass it to the list of selected names
        Name selectedName = selectedNamesList.getSelectionModel().getSelectedItem();
        if(selectedName == null){
            Alert noSelectionAlert = new Alert(Alert.AlertType.ERROR, "No name selected");
            noSelectionAlert.showAndWait();
        } else {
            _selectedNames.remove(selectedName);
            selectedNamesList.setItems(_selectedNames);
        }
    }

    @FXML
    private void shuffleButtonPress() {
        Collections.shuffle(_selectedNames);
    }

    @FXML
    private void backButtonPress() {
        // go back to NameDisplay page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(_selectedNames));
            Parent nameView = loader.load();
            _stage.setScene(new Scene(nameView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Name createName(String nameStr){
        return new Name(nameStr, _namesDB.getFile(nameStr));
    }
}
