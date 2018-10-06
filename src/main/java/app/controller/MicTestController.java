package app.controller;

import app.Main;
import app.Name;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MicTestController implements Initializable {
    private Stage _stage;
    private Timeline timeline;
    private List<Name> _names;

    @FXML
    ProgressBar bar;

    @FXML
    Button testButton;

    @FXML
    Button backButton;

    public MicTestController(List<Name> names){
        _names = names; // hold onto information about names to pass back
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();

        // set up for the progress bar
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(bar.progressProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(5),
                        new KeyValue(bar.progressProperty(), 1)
                )
        );
    }

    @FXML
    private void testButtonPressed() {
        Thread thread = new Thread(new TestRecording());
        thread.start();
        timeline.playFromStart();

        testButton.setDisable(true);
        backButton.setDisable(true);
    }

    @FXML
    private void backButtonPress() {
        // go back to Welcome page
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            NameDisplayController controller = new NameDisplayController(_names);
            loader.setController(controller);
            Parent nameDisplay = loader.load();
            _stage.setScene(new Scene(nameDisplay));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Recording is a class that creates a recording saved as UNSAVED_RECORDING_FILENAME
     */
    private class TestRecording extends Task<Void> {
        private String _fileName;
        @Override
        protected Void call() {
            try {
                // make a directory in the user location for this name if there isnt one
                File directory = new File(Main.RECORDING_LOCATION + "/");
                _fileName = directory.toString() + "/test" +  Main.AUDIO_FILETYPE;

                File recording = new File(_fileName);
                // Safety check delete any previous recording with the same name
                if(recording.exists()){
                    recording.delete();
                }

                // do the recording
                String cmd = "ffmpeg -f alsa -i default -t 5 " + _fileName;
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
                // immediately plays back the test recording
                Media media = new Media(new File(_fileName).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                timeline.playFromStart();

                testButton.setDisable(false);
                backButton.setDisable(false);
            });
        }
    }
}
