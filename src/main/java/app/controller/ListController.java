package app.controller;

import app.Main;
import app.Name;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class ListController implements Initializable {
    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();

    @FXML
    private ListView<Name> selectedNamesList;

    public ListController(List<Name> names){
        _selectedNames.addAll(names);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        selectedNamesList.setPlaceholder(new Label("No Names To Practice :("));
        selectedNamesList.getItems().addAll(_selectedNames);
    }

    @FXML
    private void removeButtonPress() {
        System.out.print("remove not yet implemented");
//        // get the selected item and pass it to the list of selected names
//        Name selectedName = selectedNamesList.getSelectionModel().getSelectedItem();
//
//        if(selectedName == null){
//            Alert noSelectionAlert = new Alert(Alert.AlertType.ERROR, "No name selected");
//            noSelectionAlert.showAndWait();
//        } else {
//            _selectedNames.remove(selectedName);
//            selectedNamesList.setItems(_selectedNames);
//        }
    }

    @FXML
    private void practiseButtonPress() {
        System.out.println("Practice not yet implemented");
//        if(_selectedNames.isEmpty()){
//            Alert noSelectionAlert = new Alert(Alert.AlertType.ERROR, "No names selected");
//            noSelectionAlert.showAndWait();
//        } else {
//            // get the selected items and pass it onto the new scene
//            try {
//                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
//                loader.setController(new NameDisplayController(_selectedNames));
//                Parent nameDisplay = loader.load();
//                _stage.setScene(new Scene(nameDisplay));
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @FXML
    private void shuffleButtonPressed() {
        Collections.shuffle(_selectedNames);
    }
}
