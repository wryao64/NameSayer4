package app.controller;

import app.*;
import app.audio.AudioPlayer;
import app.audio.RepeatAudioPlayer;
import app.pet.UserPetCollection;
import app.name.Name;
import app.name.NamesDatabase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private UserPetCollection _user;

    private List<Name> _nameList;
    private SimpleIntegerProperty _selectedNameIndex;
    private Name _selectedName; // this changes when _selectedNameIndex changes
    private NamesDatabase _namesDB = new NamesDatabase();
    private boolean _buttonDisabled = false;

    @FXML private ComboBox nameComboBox;
    @FXML private ListView<File> userRecordings;
    @FXML private Spinner<Integer> repeatSpinner;
    @FXML private Button qualityFlagButton;
    @FXML private Button breakButton;
    @FXML private Button setupButton;
    @FXML private Button listenButton;
    @FXML private Button practiseButton;
    @FXML private Button compareButton;
    @FXML private Button testMicButton;
    @FXML private Button listenUserButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    public NameDisplayController(List<Name> nameList) {
        // namesList should always have at least 1 item enforced by the GUI design
        _nameList = nameList;
        _selectedNameIndex = new SimpleIntegerProperty(0);
        _selectedName = _nameList.get(_selectedNameIndex.intValue());
    }

    public NameDisplayController(List<Name> nameList, Name currentName) {
        _nameList = nameList;
        _selectedNameIndex = new SimpleIntegerProperty((_nameList.indexOf(currentName)));
        _selectedName = _nameList.get(_selectedNameIndex.intValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        _user = Main.getUser();

        nameComboBox.getItems().addAll(_nameList);

        // bind _selectedName to change with the index changing
        _selectedNameIndex.addListener((observable, oldValue, newValue) -> {
            nameComboBox.getSelectionModel().select(newValue.intValue());
            _selectedName = _nameList.get(newValue.intValue());
            fetchUserRecordings();
            checkButtons();
            _user.tryDropPetPic();
        });

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

        // initial setup
        nameComboBox.getSelectionModel().select(_selectedNameIndex.intValue());
        fetchUserRecordings();
        checkButtons();
    }

    private void checkButtons(){
        setQualityFlagButtonText();
        nextButtonCheck();
        prevButtonCheck();
        listenButtonCheck();
        listenUserButtonCheck();
    }

    private void togglePlayingButtons(){
        _buttonDisabled = !_buttonDisabled;
        compareButton.setDisable(_buttonDisabled);

        if(_buttonDisabled){
            listenButton.setDisable(_buttonDisabled);
            listenUserButton.setDisable(_buttonDisabled);
        } else {
            listenButtonCheck();
            listenUserButtonCheck();
        }
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
        _user.tryDropPetPic();
        // redirect to RecordController page
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

    /**
     * Compares the user recordings and the database one by playing them repeatedly
     */
    @FXML
    private void compareButtonPress(){
        if(!_selectedName.dbRecordingExists()){
            DialogGenerator.showErrorMessage("There are no names in the database matching \""
                    + _selectedName.toString() + "\"");
        } else if(userRecordings.getItems().size() == 0) {
            DialogGenerator.showErrorMessage("There are no practise recordings for " + _selectedName.toString());
        } else {
            File selectedUserRecording = userRecordings.getSelectionModel().getSelectedItem();
            if(selectedUserRecording == null){
                // default to latest recording if none selected
                selectedUserRecording = _selectedName.getLatestUserRecording();
            }
            togglePlayingButtons();
            // TODO: Balance the audio volume
            RepeatAudioPlayer rap = new RepeatAudioPlayer(_selectedName.getDBRecording(), selectedUserRecording,
                    repeatSpinner.getValue());
            rap.setOnSucceeded(e -> togglePlayingButtons());
            Thread thread = new Thread(rap);
            thread.start();
            _user.tryDropPetPic();
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
    private void listenButtonPress(){
        togglePlayingButtons();
        AudioPlayer ap = new AudioPlayer(_selectedName.getDBRecording());
        ap.setOnSucceeded(e -> this.togglePlayingButtons());
        Thread thread = new Thread(ap);
        thread.start();
        _user.tryDropPetPic();
    }

    private void listenButtonCheck(){
        if(!_selectedName.dbRecordingExists()){
            listenButton.setDisable(true);
            listenButton.setText("Unable to listen");
        } else {
            listenButton.setDisable(false);
            listenButton.setText("Listen");
        }
    }

    @FXML
    private void listenUserRecording(){
        File selectedRecording = userRecordings.getSelectionModel().getSelectedItem();
        if(selectedRecording == null) {
            selectedRecording = _selectedName.getLatestUserRecording();
        }

        this.togglePlayingButtons();
        AudioPlayer ap = new AudioPlayer(selectedRecording);
        ap.setOnSucceeded(e -> this.togglePlayingButtons());
        Thread thread = new Thread(ap);
        thread.start();
    }

    private void listenUserButtonCheck(){
        if(_selectedName.getAllUserRecordings().size() == 0){
            listenUserButton.setDisable(true);
        } else {
            listenUserButton.setDisable(false);
        }
    }

    @FXML
    private void qualityButtonPress(){
        _selectedName.toggleQuality();
        setQualityFlagButtonText();
    }

    @FXML
    private void breakButtonPress(){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("ImageViewer.fxml"));
            loader.setController(new ImageViewerController(_user,_nameList));
            Parent memeView = loader.load();
            _stage.setScene(new Scene(memeView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void setQualityFlagButtonText(){
        if(!_namesDB.checkExists(_selectedName.toString())){
            qualityFlagButton.setText("Cannot mark quality");
            qualityFlagButton.setDisable(true);
        } else {
            qualityFlagButton.setDisable(false);
            if(_selectedName.isBadQuality()) {
                qualityFlagButton.setTooltip(new Tooltip("The name is currently marked as bad quality"));
                qualityFlagButton.setText("Mark as Good");
            } else {
                qualityFlagButton.setTooltip(new Tooltip("The name is currently marked as good quality"));
                qualityFlagButton.setText("Mark as Bad");
            }
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
        Label tempLabel = new Label("No practise recordings");
        tempLabel.setId("temp-label");
        userRecordings.setPlaceholder(tempLabel);
    }

    // Deals with checking if the previous button should be disabled
    private void prevButtonCheck(){
        if(_selectedNameIndex.intValue() == 0) {
            prevButton.setDisable(true);
        } else {
            prevButton.setDisable(false);
        }
    }

    // Deals with checking if the next button should be disabled
    private void nextButtonCheck(){
        if(_selectedNameIndex.intValue() == _nameList.size() - 1) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
    }
}
