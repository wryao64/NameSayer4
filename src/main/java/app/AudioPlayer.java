package app;

import javafx.concurrent.Task;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * AudioPlayer is a class that plays an audio file to be used on a different thread
 * Adapted from: https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java
 */
public class AudioPlayer extends Task<Void> {
    private File _audioFile;

    public AudioPlayer(File audioFile){
        _audioFile = audioFile;
    }

    @Override
    protected Void call() {
        playAudioFile(_audioFile);
        return null;
    }

    protected void playAudioFile(File audioFile){
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            int BUFFER_SIZE = 128000;
            AudioFormat audioFormat = null;
            SourceDataLine sourceLine = null;

            audioFormat = audioStream.getFormat();

            sourceLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceLine.open(audioFormat);
            sourceLine.start();

            int nBytesRead = 0;
            byte[] abData = new byte[BUFFER_SIZE];
            while (nBytesRead != -1) {
                try {
                    nBytesRead =
                            audioStream.read(abData, 0, abData.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (nBytesRead >= 0) {
                    int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
                }
            }
            sourceLine.drain();
            sourceLine.close();
        } catch (LineUnavailableException |IOException|UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    protected File getAudioFile() {
        return _audioFile;
    }
}