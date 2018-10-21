package app;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NameProcessor {
    private String listFile;

    private NamesDatabase _namesDB = new NamesDatabase();

    /**
     * createName creates a Name object. There is a prompt if the part of the Name is not in the database
     * asking the user what they want to do ?????
     *
     * @param nameStr The name to create a Name object from
     * @return a Name object with the name and audio file setup, null if Name not setup
     */
    public Name createName(String nameStr){
        listFile = Main.COMPOSITE_LOCATION + "/";
        List<String> namesInDatabase = new ArrayList<>();
        List<String> namesNotInDatabase = new ArrayList<>();
        boolean createName = true;

        String[] nameParts = nameStr.split("[\\s-]+"); // split name by spaces and hyphens

        // Add parts to relevant list depending if in database
        for(int i = 0; i < nameParts.length; i++){
            String namePart = nameParts[i];
            if(_namesDB.checkExists(namePart)){
                namesInDatabase.add(namePart);
            } else {
                namesNotInDatabase.add(namePart);
            }
        }

        if(namesNotInDatabase.size() > 0){
            // Build up the error message regarding missing names
            String message = namesNotInDatabase.get(0);
            if(namesNotInDatabase.size() == 1){
                message = message + " is";
            } else {
                for(int i = 1; i < namesNotInDatabase.size(); i++){
                    message = message + ", " + namesNotInDatabase.get(i);
                }
                message = message + " are";
            }
            message = message + " not in the database. Would you still like to create the name?";
            createName = DialogGenerator.showOptionsDialog("Missing names in database", message, "Yes", "No");
        }

        if(createName){
            Name name;
            if(namesInDatabase.size() == 1){
                // just a single database name so no need for audio concat
                name = new Name(nameStr, _namesDB.getFile(nameStr));

                AudioProcessTask aPTask = new AudioProcessTask(name);
                new Thread(aPTask).start();

                // changes the Name object to reference the trimmed audio
                String trimmedAudioStr = getTrimmedAudioLocation(name.toString());
                name = new Name(nameStr, new File(trimmedAudioStr));

                // load in any previous quality
                File badQualityFile = new File(Main.QUALITY_FILE);
                if(badQualityFile.exists()){
                    getQuality(badQualityFile, name);
                }
            } else {
                // multiple database names, do the concat
                String output = Main.COMPOSITE_LOCATION + "/" + listAsLine(namesInDatabase) + Main.AUDIO_FILETYPE;

                // create a text file of the names to add
                listFile += listAsLine(namesInDatabase) + ".txt";
                createConcatTextFile(namesInDatabase);

                AudioProcessTask aPTask = new AudioProcessTask(namesInDatabase, output);
                new Thread(aPTask).start();

                name = new Name(nameStr, new File(output));
            }

            // Load in any previous user recordings
            File userDir = new File(Main.RECORDING_LOCATION + "/" + name.toString());
            if(userDir.exists()){
                // add in all user recordings that match naming convention
                File[] listOfUserRecordings = userDir.listFiles();
                for(int j = 0; j < listOfUserRecordings.length; j++) {
                    File userRecording = listOfUserRecordings[j];
                    String format = ".*\\/" + name + "[0-9]+" + Main.AUDIO_FILETYPE + "$";
                    if(userRecording.toString().matches(format)){
                        name.addUserRecording(userRecording);
                    }
                }
            }

            return name;

        } else {
            return null;
        }
    }

    private String listAsLine(List<String> list){
        String line = "";
        for(String str : list){
            line = line + str;
        }
        return line;
    }

    private void createConcatTextFile(List<String> list){
        try {
            PrintWriter writer = new PrintWriter(listFile, "UTF-8");
            for(String str : list){
                String fileStr = getTrimmedAudioLocation(str);
                writer.println("file " + "'" + fileStr + "'");
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the location of the trimmed name audio
     * @param name
     * @return the trimmed audio location string
     */
    private String getTrimmedAudioLocation(String name) {
        String trimmedAudioStr = Main.COMPOSITE_LOCATION + "/" + name + Main.AUDIO_FILETYPE;
        return trimmedAudioStr;
    }

    /**
     * Checks if the name is marked as bad quality and changes the quality button accordingly
     * @param badQualityFile
     * @param name
     */
    private void getQuality(File badQualityFile, Name name) {
        try {
            // get the quality file as a list of strings
            Path qualityPath = badQualityFile.toPath();
            List<String> qualityLines = new ArrayList<>(Files.readAllLines(qualityPath, StandardCharsets.UTF_8));

            // check if the name is bad quality
            for (int k = 0; k < qualityLines.size(); k++) {
                String line = qualityLines.get(k);
                if(line.equals(name.getDBRecording().getName())) {
                    name.toggleQuality();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AudioProcessTask extends Task<Void> {
        boolean _composite = false;
        Name _name;
        List<String> _nameList;
        String _outputLocStr;


        // for single name
        public AudioProcessTask(Name name) {
            _name = name;
        }

        // for composite name
        public AudioProcessTask(List<String> nameList, String outputLocStr) {
            _composite = true;
            _nameList = nameList;
            _outputLocStr = outputLocStr;
        }

        @Override
        protected Void call() throws Exception {
            if (!_composite) {
                String originalStr = _name.getDBRecording().toString();
                String trimmedAudioStr = getTrimmedAudioLocation(_name.toString());

                this.normaliseAudio(originalStr, trimmedAudioStr);

                // trims the silence
                String trimCmd = "ffmpeg -y -hide_banner -i " + trimmedAudioStr +
                        " -af silenceremove=1:0:-55dB:1:5:-55dB:0 " + trimmedAudioStr;
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", trimCmd);
                Process process = builder.start();
                process.waitFor();
            } else {
                String trimCmd = "";

                // trims all parts of the name in the same command
                for (String namePart : _nameList) {
                    _name = new Name(namePart, _namesDB.getFile(namePart));
                    String trimmedAudioStr = getTrimmedAudioLocation(namePart);

                    this.normaliseAudio(_name.getDBRecording().toString(), trimmedAudioStr);

                    String trimCmdPart = "ffmpeg -y -hide_banner -i " + trimmedAudioStr +
                            " -af silenceremove=1:0:-55dB:1:5:-55dB:0 " + trimmedAudioStr;

                    if (trimCmd != "") {
                        trimCmd = trimCmd + " && " + trimCmdPart;
                    } else {
                        trimCmd = trimCmdPart;
                    }
                }

                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", trimCmd);
                Process process = builder.start();
                process.waitFor();

                // concatenates all parts of the name together
                String cmd = "ffmpeg -y -f concat -safe 0 -i " + listFile + " -c copy " + _outputLocStr;
                builder = new ProcessBuilder("/bin/bash", "-c", cmd);
                process = builder.start();
                process.waitFor();
            }

            return null;
        }

        private void normaliseAudio(String originalStr, String normStr) {
            int targetVol = 0;

            // finds the mean volume of the audio
            String normCmd = "ffmpeg -i " + originalStr + " -filter:a volumedetect -f null /dev/null 2>&1 | grep mean_volume";
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", normCmd);
            Process process = null;
            try {
                process = builder.start();
                process.waitFor();

                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String meanVol = br.readLine();

                // finds the value of mean volume of the audio from given output
                int start = meanVol.indexOf(':') + 1;
                int end = meanVol.indexOf('.');
                meanVol = meanVol.substring(start, end).trim();

                // finds the difference between the target volume and audio volume
                int diff = targetVol - Integer.parseInt(meanVol);

                // changes the volume of the audio
                String volumeCmd = "ffmpeg -y -i " + originalStr + " -filter:a \"volume=" + diff + "dB\" " + normStr;
                builder = new ProcessBuilder("/bin/bash", "-c", volumeCmd);
                process = builder.start();
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
