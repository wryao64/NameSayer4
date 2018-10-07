package app.controller;

import app.AudioCapture;
import app.Main;
import app.Name;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class MicTestController implements Initializable {
    private Stage _stage;
    private List<Name> _names;

    private boolean _capturing = false;
    private Task _audioCapTask;

    @FXML
    ProgressBar bar;

    @FXML
    Button startButton;

    @FXML
    Button stopButton;

    @FXML
    Button backButton;

    public MicTestController(List<Name> names){
        _names = names; // hold on to information about names to pass back
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        stopButton.setDisable(true);
    }

    @FXML
    private void startButtonPress() {
        // start capturing audio
        if (!_capturing) {
            startButton.setDisable(true);
            stopButton.setDisable(false);
            backButton.setDisable(true);

            AudioCapture _audioCapture = new AudioCapture();
            _audioCapTask = _audioCapture.callACTask();

            _audioCapTask.valueProperty().addListener((o, oldVal, newVal) -> {
                bar.progressProperty().setValue((double) newVal / 100);
            });

            new Thread(_audioCapTask).start();
            _capturing = true;
        }
    }

    @FXML
    private void stopButtonPress() {
        // stop capturing audio
        if (_capturing) {
            startButton.setDisable(false);
            stopButton.setDisable(true);
            backButton.setDisable(false);

            _audioCapTask.cancel();
            bar.setProgress(0);
            _capturing = false;
        }
    }

    @FXML
    private void backButtonPress() {
        // go back to NameDisplay page
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
}
