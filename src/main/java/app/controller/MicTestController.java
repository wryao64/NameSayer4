package app.controller;

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

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Some audio capture code is from:
 * https://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
 *
 * Algorithm used for converting raw audio to double is from:
 * https://stackoverflow.com/questions/3899585/microphone-level-in-java
 */
public class MicTestController implements Initializable {
    private Stage _stage;
    private List<Name> _names;

    private TargetDataLine _line;
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

            setUpDataLine();

            _audioCapTask = new AudioCaptureTask(_line);
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

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        int frameSize = 4;
        float frameRate = 44100;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits,
                channels, frameSize, frameRate, bigEndian);
        return format; //TODO: check these values with Catherine's slides
    }

    /**
     * Set up data line of the mic
     */
    private void setUpDataLine() {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // Checks if the system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Error: Line not supported");
            return;
        }

        try {
            _line = (TargetDataLine) AudioSystem.getLine(info);
            _line.open(format, 6000);
            _line.start();   // start capturing
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Class that handles capturing audio from the system's microphone.
     */
    private class AudioCaptureTask extends Task<Double>{
        private TargetDataLine targetLine;

        public AudioCaptureTask(TargetDataLine dataLine) {
            targetLine = dataLine;
        }

        @Override
        protected Double call() throws Exception {
            byte tempBuffer[] = new byte[6000];

            try {
                while (!isCancelled()) {
                    targetLine.read(tempBuffer, 0, tempBuffer.length);
                    double value = calculateRMSLevel(tempBuffer);
                    updateValue(value);
                }
                updateValue(0.0);
                targetLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0.0;
        }

        /**
         * Algorithm that converts the input data from the mic to a double value
         * @param audioData a byte array of the audio data
         * @return RMS level of raw audioData
         */
        public double calculateRMSLevel(byte[] audioData) {
            long lSum = 0;
            for(int i = 0; i < audioData.length; i++)
                lSum = lSum + audioData[i];

            double dAvg = lSum / audioData.length;
            double sumMeanSquare = 0d;

            for(int j = 0; j < audioData.length; j++)
                sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

            double averageMeanSquare = sumMeanSquare / audioData.length;

            return (Math.pow(averageMeanSquare,0.5d) + 0.5);
        }
    }

}
