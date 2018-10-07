package app.controller;

import app.AudioCapture;
import app.DialogGenerator;
import app.Main;
import app.Name;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RecordController implements Initializable {
    private Stage _stage;

    private boolean _capturing = false;
    private Task _audioCapTask;

    private Name _currentName;
    private List<Name> _selectedNames;

    @FXML
    private Label nameLabel;

    @FXML
    private Button listenButton;

    @FXML
    private Button recordButton;

//    @FXML
//    private Button stopButton;

    @FXML
    private Button backButton;

    @FXML
    ProgressBar bar;

    public RecordController(Name name, List<Name> nameList) {
        _currentName = name;
        _selectedNames = nameList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        nameLabel.setText(_currentName.toString());
//        stopButton.setDisable(true);
    }

    @FXML
    private void listenButtonPress(){
        Main.getUser().tryDropMeme();
        if(!_currentName.playDBRecording()){
            DialogGenerator.showOkMessage("Name not in database",
                    "There is nothing in the database matching \"" + _currentName.toString() + "\"");
        }
    }

    @FXML
    private void recordButtonPress(){
        AudioCapture _audioCapture = new AudioCapture();
        _audioCapTask = _audioCapture.callACTask();

        _audioCapTask.valueProperty().addListener((o, oldVal, newVal) -> {
            bar.progressProperty().setValue((double) newVal / 100);
        });

        new Thread(_audioCapTask).start();
        _capturing = true;

        Thread thread = new Thread(new Recording());
        thread.start();

        listenButton.setDisable(true);
        recordButton.setDisable(true);
//        stopButton.setDisable(false);
        backButton.setDisable(true);
    }

    // can't stop recording
//    @FXML
//    private void stopButtonPress() {
//        _audioCapTask.cancel();
//        bar.setProgress(0);
//        _capturing = false;
//
//        recordButton.setDisable(false);
//        stopButton.setDisable(true);
//        backButton.setDisable(false);
//    }

    @FXML
    private void backButtonPress() {
        // go back to List page
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
        private String _fileName;
        @Override
        protected Void call() {
            try {
                // make a directory in the user location for this name if there isnt one
                File directory = new File(Main.RECORDING_LOCATION + "/" + _currentName.toString());
                if(!directory.exists()){
                    directory.mkdir();
                }

                _fileName = directory.toString()+ "/" + _currentName.toString() + _currentName.getNextRecordingIndex() + Main.AUDIO_FILETYPE;

                File recording = new File(_fileName);
                // Safety check delete any previous recording with the same name
                if(recording.exists()){
                    recording.delete();
                }

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
                _capturing = false;

                // See if user wants to listen to the recording
                boolean hearRecording = DialogGenerator.showOptionsDialog("Play Recording", "Would you like to listen to the recording?", "Yes", "No");
                if (hearRecording) {
                    Media media = new Media(new File(_fileName).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();

                    // See if user wants to save the recording
                    boolean save = DialogGenerator.showOptionsDialog("Save Recording", "Would you like to save this recording?", "Yes", "No");
                    mediaPlayer.stop();

                    File recording = new File(_fileName);
                    if(save) {
                        // add it to the name
                        _currentName.addUserRecording(recording);
                    } else {
                        // cleanup the recorded file
                        recording.delete();
                    }
                } else {
                    // See if user wants to save the recording
                    boolean save = DialogGenerator.showOptionsDialog("Save Recording", "Would you like to save this recording?", "Yes", "No");

                    File recording = new File(_fileName);
                    if (save) {
                        // add it to the name
                        _currentName.addUserRecording(recording);
                    } else {
                        // cleanup the recorded file
                        recording.delete();
                    }
                }
                listenButton.setDisable(false);
                recordButton.setDisable(false);
                backButton.setDisable(false);
            });
        }
    }
}
