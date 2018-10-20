package app;

import app.controller.Controller;
import app.controller.NameDisplayController;
import javafx.fxml.Initializable;

import java.io.File;
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
    private boolean _isBadQuality;
    private int _nextRecordingIndex;

    public Name(String name, File dbRecording) {
        _name = name;
        _dbRecording = dbRecording;
        _userRecordings = new ArrayList<File>();
        _nextRecordingIndex = 0; // will have to deal with already saved recordings
        _isBadQuality = false; // default quality to good
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

    public boolean dbRecordingExists(){
        if(_dbRecording != null && _dbRecording.exists()){
            return true;
        } else {
            return false;
        }
    }

    public boolean playDBRecording(Controller controller){
        if(dbRecordingExists()){
            AudioPlayer ap = new AudioPlayer(_dbRecording);
            Thread thread = new Thread(ap);
            thread.start();

            ap.setOnSucceeded(e -> {
                controller.setButtonDisable();
            });
        } else {
            return false;
        }
        return true;
    }

    public File getDBRecording(){
        return _dbRecording;
    }

    public void addUserRecording(File recordingFile){
        _userRecordings.add(recordingFile);
        _nextRecordingIndex++;
    }

    public File getLatestUserRecording(){
        return _userRecordings.get(_userRecordings.size() - 1);
    }

    public int getNextRecordingIndex(){
        return _nextRecordingIndex;
    }

    public List<File> getAllUserRecordings(){
        return _userRecordings;
    }

    public void toggleQuality(){
        _isBadQuality = !_isBadQuality;
    }

    public boolean isBadQuality(){
        return _isBadQuality;
    }

}
