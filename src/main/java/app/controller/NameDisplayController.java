package app.controller;

import app.*;
import app.meme.UserMemeProfile;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class NameDisplayController extends Controller {

    private Stage _stage;
    private UserMemeProfile _user;

    private List<Name> _nameList;
    private SimpleIntegerProperty _selectedNameIndex;
    private Name _selectedName; // this changes when _selectedNameIndex changes
    private NamesDatabase _namesDB; // just build it once :) namesDB checking could be refactored to names class

    @FXML private ComboBox nameComboBox;
    @FXML private ListView<File> userRecordings;
    @FXML private Spinner<Integer> repeatSpinner;
    @FXML private Button qualityFlagButton;
    @FXML private Button memeButton;
    @FXML private Button setupButton;
    @FXML private Button listenButton;
    @FXML private Button practiseButton;
    @FXML private Button compareButton;
    @FXML private Button testMicButton;
    @FXML private Button listenUserButton;

    @Override
    public void setButtonDisable() {
        _buttonDisabled = !_buttonDisabled;
        memeButton.setDisable(_buttonDisabled);
        setupButton.setDisable(_buttonDisabled);
        listenButton.setDisable(_buttonDisabled);
        practiseButton.setDisable(_buttonDisabled);
        compareButton.setDisable(_buttonDisabled);
        testMicButton.setDisable(_buttonDisabled);
        listenUserButton.setDisable(_buttonDisabled);
    }

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
            setQualityFlagButtonText();
            Main.getUser().tryDropMeme();
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
        setQualityFlagButtonText();
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
        Main.getUser().tryDropMeme();
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
            Main.getUser().tryDropMeme();
            File selectedUserRecording = userRecordings.getSelectionModel().getSelectedItem();
            if(selectedUserRecording == null){
                // default to latest recording if none selected
                selectedUserRecording = _selectedName.getLatestUserRecording();
            }

            // Balance the audio volume
            RepeatAudioPlayer rap = new RepeatAudioPlayer(_selectedName.getDBRecording(), selectedUserRecording,
                    repeatSpinner.getValue());
            Thread thread = new Thread(rap);
            thread.start();
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
        this.setButtonDisable();
        Main.getUser().tryDropMeme();
        if(!_selectedName.playDBRecording(this)){
            DialogGenerator.showErrorMessage("There are no names in the database matching \""
                    + _selectedName.toString() + "\"");
        }
    }

    @FXML
    private void listenUserRecording(){
        File selectedRecording = userRecordings.getSelectionModel().getSelectedItem();
        if(selectedRecording == null) {
            DialogGenerator.showOkMessage("No recording selected", "Please select a recording to listen to.");
        } else {
            this.setButtonDisable();

            AudioPlayer ap = new AudioPlayer(selectedRecording);
            Thread thread = new Thread(ap);
            thread.start();

            ap.setOnSucceeded(e -> {
                this.setButtonDisable();
            });
        }
    }

    @FXML
    private void qualityButtonPress(){
        try {
            _selectedName.toggleQuality();
            setQualityFlagButtonText();

            // load in quality file
            Path qualityPath = new File(Main.QUALITY_FILE).toPath();
            List<String> quality = new ArrayList<>(Files.readAllLines(qualityPath, StandardCharsets.UTF_8));

            if(_selectedName.isBadQuality()){
                // record onto the quality file
                quality.add(_selectedName.getDBRecording().getName());
            } else {
                // remove from the quality file if exists
                for (int i = 0; i < quality.size(); i++) {
                    if(quality.get(i).equals(_selectedName.getDBRecording().getName())){
                        quality.remove(i);
                    }
                }
            }

            // write to quality file
            Collections.sort(quality);
            Files.write(qualityPath, quality, StandardCharsets.UTF_8);
        } catch(IOException e){
            DialogGenerator.showErrorMessage("Error writing to quality file");
        }
    }

    @FXML
    private void memeButtonPress(){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("MemeViewer.fxml"));
            loader.setController(new MemeViewerController(_user,_nameList));
            Parent memeView = loader.load();
            _stage.setScene(new Scene(memeView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void setQualityFlagButtonText(){
        NamesDatabase namesDB = new NamesDatabase();
        if(!namesDB.checkExists(_selectedName.toString())){
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
}
