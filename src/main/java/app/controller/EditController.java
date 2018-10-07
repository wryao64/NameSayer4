package app.controller;

import app.*;
import app.meme.UserMemeProfile;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EditController implements Initializable {
    private static final String LIST_FILE = Main.COMPOSITE_LOCATION + "/mylist.txt";

    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();
    private NamesDatabase _namesDB = new NamesDatabase();
    private UserMemeProfile user;

    @FXML private ListView<Name> selectedNamesList;
    @FXML private TextField nameInput;
    @FXML private Label title;

    /**
     * EditController constructor from a single string (for coming from Welcome screen)
     * @param name A name to add as a string
     */
    public EditController(String name){
        Name nameToAdd = createName(name);
        if(nameToAdd != null){
            _selectedNames.add(nameToAdd);
        }
    }

    /**
     * EditController constructor from file of names (for coming from Welcome screen)
     * @param file A text file with names
     */
    public EditController(File file){
        openFile(file);
    }

    /**
     * EditController constructor with a list of names
     * @param names A list of names
     */
    public EditController(List<Name> names){
        for(Name nameToAdd : names){
            _selectedNames.add(nameToAdd);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        Label tempLabel = new Label("No Names To Practice :(");
        tempLabel.setId("temp-label");
        selectedNamesList.setPlaceholder(tempLabel);
        selectedNamesList.getItems().addAll(_selectedNames);
        _selectedNames.addListener((ListChangeListener<Name>) c -> {
            selectedNamesList.setItems(_selectedNames);
        });
    }

    @FXML
    private void addButtonPress(){
        String nameStr = nameInput.getText().trim();
        if(nameStr == null || nameStr.isEmpty()) {
            DialogGenerator.showErrorMessage("Name is blank.");
        } else if(_selectedNames.contains(new Name(nameStr, null))) {
            DialogGenerator.showErrorMessage("Name has already been added.");
        } else {
            addName(nameStr);
        }
    }

    private void addName(String name){
        Name nameToAdd = createName(name);
        if (nameToAdd != null) {
            _selectedNames.add(nameToAdd);
            nameInput.setText(""); // clear TextField
        }
    }

    @FXML
    private void removeButtonPress() {
        // get the selected item and pass it to the list of selected names
        Name selectedName = selectedNamesList.getSelectionModel().getSelectedItem();
        if(selectedName == null){
            DialogGenerator.showErrorMessage("No name selected.");
        } else {
            _selectedNames.remove(selectedName);
        }
    }

    @FXML
    private void shuffleButtonPress() {
        Collections.shuffle(_selectedNames);
    }

    @FXML
    private void backButtonPress() {
        if(_selectedNames.isEmpty()){
            DialogGenerator.showErrorMessage("Please select at least one name.");
        } else {
            goToNameDisplay(_selectedNames);
        }
    }

    private void goToNameDisplay(List<Name> selectedNames){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(selectedNames));
            Parent nameView = loader.load();
            _stage.setScene(new Scene(nameView));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearButtonPress(){
        _selectedNames.clear();
    }

    @FXML
    private void uploadButtonPress(){
        // opens window to choose file
        FileChooser fc = new FileChooser();
        configureFileChooser(fc);
        File file = fc.showOpenDialog(_stage);
        if (file != null) {
            openFile(file);
        }
    }

    private void configureFileChooser(final FileChooser fc) {
        fc.setTitle("Open Name File");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text File (*.txt)", "*.txt")
        );
    }

    private void openFile(File file) {
        // load in names from file
        try{
            String line;
            BufferedReader bf = new BufferedReader(new FileReader(file));
            while ((line = bf.readLine()) != null) {
                Name newName = createName(line);
                if(newName != null){
                    _selectedNames.add(newName);
                }
            }
            DialogGenerator.showOkMessage("File Loaded","Names from " + file.getName() + " have been read in.");
        } catch(IOException e){
            DialogGenerator.showErrorMessage("Could not read file :(");
        }
    }

    /**
     * createName creates a Name object. There is a prompt if the part of the Name is not in the database
     * asking the user what they want to do ?????
     *
     * TODO: PARTS OF THIS SHOULD BE REFACTORED TO THE NAME CLASS
     * TODO: (right now it works ok as this is the only time the app makes names)
     *
     * @param nameStr The name to create a Name object from
     * @return a Name object with the name and audio file setup, null if Name not setup
     */
    private Name createName(String nameStr){
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

                trimAudio(name);
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
                createConcatTextFile(namesInDatabase);

                // TODO:check if not already a composite name of the same type

                // TODO: normalise audio

                // trims silence for each individual part of the name
                for (String namePart : namesInDatabase) {
                    name = new Name(namePart, _namesDB.getFile(namePart));
                    String trimmedAudioStr = getTrimmedAudioLocation(namePart);

                    trimAudio(name);
                    // changes the Name object to reference the trimmed audio
                    name = new Name(namePart, new File(trimmedAudioStr));
                }

                // TODO: add silence between names -- I don't think we need to

                // do the concat TODO: multithread this
                String cmd = "ffmpeg -f concat -safe 0 -i " + LIST_FILE + " -c copy " + output;
                ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
                try {
                    Process process = builder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private String listAsLine(List<String> list){
        String line = "";
        for(String str : list){
            line = line + str;
        }
        return line;
    }

    /**
     * Gets rid of the silences on either end of the name audio
     * @param name
     */
    private void trimAudio(Name name) {
        String trimmedAudioStr = getTrimmedAudioLocation(name.toString());

        //TODO: multithread this
        String trimCmd = "ffmpeg -hide_banner -i " + name.getDBRecording().toString() +
                " -af silenceremove=1:0:-55dB:1:5:-55dB:0:peak " + trimmedAudioStr;

        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", trimCmd);
        try {
            Process process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createConcatTextFile(List<String> list){
        try {
            PrintWriter writer = new PrintWriter(LIST_FILE, "UTF-8");
//            System.out.println("LIST FILE IS: " + LIST_FILE); // for testing
            for(String str : list){
                String fileStr = getTrimmedAudioLocation(str);
//                writer.println("file " + "'" + _namesDB.getFile(str).getAbsolutePath() + "'");
                writer.println("file " + "'" + fileStr + "'");
            }
            writer.close();
        } catch (FileNotFoundException|UnsupportedEncodingException e) {
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
}
