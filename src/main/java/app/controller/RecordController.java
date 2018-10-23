package app.controller;

import app.audio.AudioCapture;
import app.DialogGenerator;
import app.Main;
import app.audio.AudioPlayer;
import app.name.Name;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RecordController implements Initializable {
    private Stage _stage;
    private String _fileName;
    private File _recordingFile;
    private Task _audioCapTask;
    private boolean _fileSaved = false;
    private boolean _buttonDisabled = false;

    private Name _currentName;
    private List<Name> _selectedNames;

    @FXML private Label nameLabel;
    @FXML private Button listenButton;
    @FXML private Button recordButton;
    @FXML private Button saveButton;
    @FXML private Button playButton;
    @FXML private Button backButton;
    @FXML ProgressBar bar;

    public void togglePlayButtons() {
        _buttonDisabled = !_buttonDisabled;
        recordButton.setDisable(_buttonDisabled);
        backButton.setDisable(_buttonDisabled);

        // always disable play/save if no recording yet
        if(!_recordingFile.exists()){
            playButton.setDisable(true);
            saveButton.setDisable(true);
        } else {
            playButton.setDisable(_buttonDisabled);
            saveButton.setDisable(_buttonDisabled);
        }

        // always disable listen button if no db recording
        if(!_currentName.dbRecordingExists()){
            listenButton.setDisable(true);
        } else {
            listenButton.setDisable(_buttonDisabled);
        }
    }

    public RecordController(Name name, List<Name> nameList) {
        _currentName = name;
        _selectedNames = nameList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        nameLabel.setText(_currentName.toString());
        setupCurrentFile();

        // disable the listen button if there is no database recording
        if(!_currentName.dbRecordingExists()){
            listenButton.setDisable(true);
        }

        // disable the play/save buttons as there is no recording file yet
        playButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void setupCurrentFile(){
        // setup for file location stuff:
        File directory = new File(Main.RECORDING_LOCATION + "/" + _currentName.toString());
        if(!directory.exists()){
            directory.mkdir();
        }

        // set fields correctly
        _fileName = directory.toString()+ "/" + _currentName.toString() + _currentName.getNextRecordingIndex() + Main.AUDIO_FILETYPE;
        _recordingFile = new File(_fileName);

        // safety check delete any previous recording with the same name
        if(_recordingFile.exists()){
            _recordingFile.delete();
        }
    }

    @FXML
    private void listenButtonPress(){
        Main.getUser().tryDropPetPic();
        togglePlayButtons();
        AudioPlayer ap = new AudioPlayer(_currentName.getDBRecording());
        ap.setOnSucceeded(e -> this.togglePlayButtons());
        Thread thread = new Thread(ap);
        thread.start();
    }

    @FXML
    private void recordButtonPress(){
        // If there is already a recording, check the user has not saved.
        giveChanceToSave();

        _fileSaved = false;
        togglePlayButtons();
        AudioCapture _audioCapture = new AudioCapture();
        _audioCapTask = _audioCapture.callACTask();

        _audioCapTask.valueProperty().addListener((o, oldVal, newVal) -> {
            bar.progressProperty().setValue((double) newVal / 100);
        });

        new Thread(_audioCapTask).start();

        Thread thread = new Thread(new Recording());
        thread.start();
    }

    @FXML
    private void playButtonPress(){
        if(_recordingFile.exists()){
            togglePlayButtons();
            AudioPlayer ap = new AudioPlayer(_recordingFile);
            ap.setOnSucceeded(e -> this.togglePlayButtons());
            Thread thread = new Thread(ap);
            thread.start();
        } else {
            DialogGenerator.showErrorMessage("No recording found to play.");
        }
    }

    @FXML
    private void saveButtonPress(){
        _currentName.addUserRecording(_recordingFile);

        // give indication that the file is saved
        Notifications.create()
                .position(Pos.BOTTOM_CENTER)
                .darkStyle()
                .title("Saved recording")
                .text("Recording has been saved")
                .showConfirm();

        _fileSaved = true;

        // prepare another file
        _fileName = Main.RECORDING_LOCATION +
                "/" + _currentName.toString() +
                "/" + _currentName.toString() + _currentName.getNextRecordingIndex() + Main.AUDIO_FILETYPE;
        _recordingFile = new File(_fileName);

        // disable the buttons again
        playButton.setDisable(true);
        saveButton.setDisable(true);
    }

    /**
     * Checks if there is an unsaved recording.
     * Gives the user a chance to save the recording if it is unsaved.
     */
    private void giveChanceToSave(){
        // check there is no unsaved file
        if(!_fileSaved && _recordingFile.exists()){
            boolean saveUnsaved = DialogGenerator.showOptionsDialog("Unsaved recording",
                    "There is still a recording that has not been saved. Would you like to save it?",
                    "Yes", "No");
            if(saveUnsaved) {
                _currentName.addUserRecording(_recordingFile);
            } else {
                _recordingFile.delete();
            }
        }
    }

    @FXML
    private void backButtonPress() {

        giveChanceToSave();

        // go back to list page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(_selectedNames, _currentName));
            Parent listView = loader.load();
            _stage.setScene(new Scene(listView));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recording is a class that creates a recording saved as UNSAVED_RECORDING_FILENAME
     */
    private class Recording extends Task<Void> {

        @Override
        protected Void call() {
            try {
                // do the recording
                String cmd = "ffmpeg -f alsa -i default -t 5 \"" + _fileName + "\"";
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
                Process process = builder.start();

                Thread.sleep(5000);

            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {
            Platform.runLater(() -> {
                _audioCapTask.cancel();
                bar.setProgress(0);

                // Enable the buttons:
                togglePlayButtons();
            });
        }
    }
}
