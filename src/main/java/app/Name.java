package app;

import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a name.
 * Names have recordings associated with them which are files on disk
 */
public class Name {
    private String _name;
    private File _dbRecording;
    private List<File> _userRecordings;
    private int _quality; // will deal with quality later...
    private int _nextRecordingIndex;

    public Name(String name, File dbRecording) {
        _name = name;
        _dbRecording = dbRecording;
        _userRecordings = new ArrayList<File>();
        _nextRecordingIndex = 0; // will have to deal with already saved recordings
        _quality = 0; // for now 0 is unset quality
    }


    /**
     * Override equals to consider names with the same string value (name) equal
     * @param obj The object being compared with
     * @return true if the name is the same, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(! (obj instanceof Name)){
            return false;
        } else {
            return this.toString().equals(obj.toString());
        }
    }

    @Override
    public String toString() {
        return _name;
    }

    public boolean playDBRecording(){
        /* Adapted from: https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java */
        if(_dbRecording != null && _dbRecording.exists()){
            Thread thread = new Thread(new AudioPlayer(_dbRecording));
            thread.start();
        } else {
            return false;
        }
        return true;
    }

    public void addUserRecording(File recordingFile){
        _userRecordings.add(recordingFile);
        _nextRecordingIndex++;
    }

    public int getNextRecordingIndex(){
        return _nextRecordingIndex;
    }

    public void playLatestUserRecording(){
        System.out.println("playing recording" + _userRecordings.get(_userRecordings.size() - 1));
    }

    public List<File> getAllUserRecordings(){
        return _userRecordings;
    }

    public void setQuality(int quality){
        _quality = quality;
    }

    public int getQuality(){
        return _quality;
    }

}
