package app.controller;

import app.DialogGenerator;
import app.Main;
import app.Name;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NameDisplayController implements Initializable {
    private Stage _stage;
    private List<Name> _nameList;
    private Name _selectedName;
    private int _selectedNameIndex;

    @FXML Label nameLabel;


    public NameDisplayController(List<Name> nameList) {
        _nameList = nameList;
        if(_nameList.size() > 0){
            _selectedName = _nameList.get(0);
            _selectedNameIndex = 0;
        } else {
            // deal with no selected name
            // right now this wont happen because of the GUI design... if the list forces user to pick a name
            System.out.println("Make the editing force the user to pick a name so this wont happen :)");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        onChangeSelectedName();
    }

    @FXML
    private void backButtonPress(){
        if(_selectedNameIndex > 0){
            _selectedNameIndex--;
            onChangeSelectedName();
        } else {
            DialogGenerator.showErrorMessage("This is the first name, can't go back.");
        }
    }

    @FXML
    private void nextButtonPress(){
        if(_selectedNameIndex < _nameList.size() - 1){
            _selectedNameIndex++;
            onChangeSelectedName();
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
        _selectedName.playDBRecording();
    }

    private void onChangeSelectedName(){
        _selectedName = _nameList.get(_selectedNameIndex);
        nameLabel.setText(_selectedName.toString());
    }
}
