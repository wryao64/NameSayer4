package app.name;

import app.audio.AudioPlayer;
import app.DialogGenerator;
import app.Main;
import app.controller.AudioPlayerController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
            return this.toString().toLowerCase().equals(obj.toString().toLowerCase());
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

    public boolean playDBRecording(AudioPlayerController controller){
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

        // load in quality file to write to
        Path qualityPath = new File(Main.QUALITY_FILE).toPath();
        List<String> quality = null;
        try {
            quality = new ArrayList<>(Files.readAllLines(qualityPath, StandardCharsets.UTF_8));

            if (_isBadQuality) {
                // record onto the quality file
                quality.add(this.getDBRecording().getName());
            } else {
                // remove from the quality file if exists
                for (int i = 0; i < quality.size(); i++) {
                    if (quality.get(i).equals(this.getDBRecording().getName())) {
                        quality.remove(i);
                    }
                }
            }

            // write to quality file
            Collections.sort(quality);
            Files.write(qualityPath, quality, StandardCharsets.UTF_8);

        } catch (IOException e1) {
            DialogGenerator.showErrorMessage("Could not write to quality file");
        }
    }

    public boolean isBadQuality(){
        return _isBadQuality;
    }

}
