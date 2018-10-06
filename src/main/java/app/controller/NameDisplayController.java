package app.controller;

import app.DialogGenerator;
import app.Main;
import app.Name;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class NameDisplayController implements Initializable {
    private Stage _stage;
    private List<Name> _nameList;
    private SimpleIntegerProperty _selectedNameIndex;
    private Name _selectedName; // this changes when _selectedNameIndex changes

    @FXML private ComboBox nameComboBox;

    @FXML private ListView<File> userRecordings;

    public NameDisplayController(List<Name> nameList) {
        // namesList should always have at least 1 item enforced by the GUI design
        _nameList = nameList;
        _selectedNameIndex = new SimpleIntegerProperty(0);
        _selectedName = _nameList.get(0);

        // bind _selectedName to change with the index changing
        _selectedNameIndex.addListener((observable, oldValue, newValue) -> {
            nameComboBox.getSelectionModel().select(newValue.intValue());
            _selectedName = _nameList.get(newValue.intValue());
            fetchUserRecordings();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        nameComboBox.getItems().addAll(_nameList);
        nameComboBox.getSelectionModel().select(0);
        fetchUserRecordings();
        // setup user recordings to show by last modified date
        userRecordings.setCellFactory(lv -> new ListCell<File>(){
            @Override
            protected void updateItem(File userRecording, boolean empty) {
                super.updateItem(userRecording, empty);
                if(empty){
                    setText(null);
                } else {
                    long ms = userRecording.lastModified();
                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/Y HH:mm:ss");
                    setText(sdf.format(ms));
                }
            }
        });
    }

    @FXML
    private void backButtonPress(){
        if(_selectedNameIndex.get() > 0){
            _selectedNameIndex.set(_selectedNameIndex.get() - 1);
        } else {
            DialogGenerator.showErrorMessage("This is the first name, can't go back.");
        }
    }

    @FXML
    private void nextButtonPress(){
        if(_selectedNameIndex.get() < _nameList.size() - 1){
            _selectedNameIndex.set(_selectedNameIndex.get() + 1);
        } else {
            DialogGenerator.showErrorMessage("This is the last name, can't go next.");
        }
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
    private void editButtonPress() {
        // go back to List page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Edit.fxml"));
            loader.setController(new EditController(_nameList));
            Parent editView = loader.load();
            _stage.setScene(new Scene(editView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void testMicButtonPress() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("MicTest.fxml"));
            loader.setController(new MicTestController(_nameList));
            Parent testMicView = loader.load();
            _stage.setScene(new Scene(testMicView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void playTestingButtonPress(){
        // the database recordings need to be setup correctly
        // right now some of the names aren't actually in the database and won't play
        System.out.println("Currently selected to play: " + _selectedName.toString());
        if(!_selectedName.playDBRecording()){
            DialogGenerator.showOkMessage("Name not in database",
                    "There is nothing in the database matching \"" + _selectedName.toString() + "\"");
        }
    }

    @FXML
    private void comboBoxChange(){
        _selectedNameIndex.set(nameComboBox.getSelectionModel().getSelectedIndex());
    }

    private void fetchUserRecordings(){
        List<File> userRecordingsList = _selectedName.getAllUserRecordings();

        // Sort the files by date modified in descending order
        Collections.sort(userRecordingsList, (f1, f2) -> {
            // change the sign so Files are in descending order
            return (-1)*Long.compare(f1.lastModified(), f2.lastModified());
        });

        userRecordings.getItems().clear();
        userRecordings.getItems().addAll(userRecordingsList);
        userRecordings.setPlaceholder(new Label("No recordings for " + _selectedName.toString()));
    }
}
