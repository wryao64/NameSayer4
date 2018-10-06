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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NameDisplayController implements Initializable {
    private Stage _stage;
    private List<Name> _nameList;
    private SimpleIntegerProperty _selectedNameIndex;
    private Name _selectedName; // this changes when _selectedNameIndex changes

    @FXML
    private ComboBox nameComboBox;


    public NameDisplayController(List<Name> nameList) {
        // namesList should always have at least 1 item enforced by the GUI design
        _nameList = nameList;
        _selectedNameIndex = new SimpleIntegerProperty(0);

        // bind _selectedName to change with the index changing
        _selectedNameIndex.addListener((observable, oldValue, newValue) -> {
            nameComboBox.getSelectionModel().select(newValue.intValue());
            _selectedName = _nameList.get(newValue.intValue());
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        nameComboBox.getItems().addAll(_nameList);
        nameComboBox.getSelectionModel().select(0);
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
        _selectedName.playDBRecording();
    }

    @FXML
    private void comboBoxChange(){
        _selectedNameIndex.set(nameComboBox.getSelectionModel().getSelectedIndex());
    }
}
