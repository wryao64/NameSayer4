package app;

import javafx.concurrent.Task;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * AudioPlayer is a class that plays an audio file to be used on a different thread
 * Adapted from: https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java
 */
public class RepeatAudioPlayer extends AudioPlayer {
    private File _audioFile2;

    private int _repeat;

    public RepeatAudioPlayer(File audioFile1, File audioFile2, int repeat){
        super(audioFile1);
        _audioFile2 = audioFile2;
        _repeat = repeat;
    }

    @Override
    protected Void call() {
        for(int i = 0; i < _repeat; i++){
            playAudioFile(this.getAudioFile());
            playAudioFile(_audioFile2);
        }
        return null;
    }
}