package app.audio;

import java.io.File;

/**
 * RepeatAudioPlayer is a class that plays 2 audio files,
 * one after the other repeatedly a specified number of times
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