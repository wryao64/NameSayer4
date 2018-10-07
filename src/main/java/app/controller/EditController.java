package app.controller;

import app.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditController implements Initializable {
    private static final String LIST_FILE = Main.COMPOSITE_LOCATION + "/mylist.txt";

    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();
    private NamesDatabase _namesDB;

    @FXML private ListView<Name> selectedNamesList;
    @FXML private TextField nameInput;

    public EditController(List<Name> names){
        for(Name nameToAdd : names){
            _selectedNames.add(nameToAdd);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        _namesDB = new NamesDatabase();

        selectedNamesList.setPlaceholder(new Label("No Names To Practice :("));
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
            Name nameToAdd = createName(nameStr);
            if (nameToAdd != null) {
                _selectedNames.add(nameToAdd);
                nameInput.setText(""); // clear TextField
            }
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
            try {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
                loader.setController(new NameDisplayController(_selectedNames));
                Parent nameView = loader.load();
                _stage.setScene(new Scene(nameView));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                openFile(file);
                DialogGenerator.showOkMessage("File Loaded","Names from " + file.getName() + " have been read in.");
            } catch(IOException e){
                DialogGenerator.showErrorMessage("Could not read file :(");
            }
        }
    }

    private void configureFileChooser(final FileChooser fc) {
        fc.setTitle("Open Name File");
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text File (*.txt)", "*.txt")
        );
    }

    private void openFile(File file) throws IOException{
        // load in names from file
        BufferedReader bf = new BufferedReader(new FileReader(file));

        String line;
        while ((line = bf.readLine()) != null) {
            Name newName = createName(line);
            if(newName != null){
                _selectedNames.add(newName);
            }
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


                // load in any previous quality
                File badQualityFile = new File(Main.QUALITY_FILE);
                if(badQualityFile.exists()){
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
            } else {
                // multiple database names, do the concat
                String output = Main.COMPOSITE_LOCATION + "/" + listAsLine(namesInDatabase) + Main.AUDIO_FILETYPE;

                // create a text file of the names to add
                createConcatTextFile(namesInDatabase);

                // TODO:check if not already a composite name of the same type

                // TODO: normalise audio

                // TODO: trim silence

                // TODO: add silence between names

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

    private String listAsLine(List<String> list){
        String line = "";
        for(String str : list){
            line = line + str;
        }
        return line;
    }

    private void createConcatTextFile(List<String> list){
        try {
            PrintWriter writer = new PrintWriter(LIST_FILE, "UTF-8");
            System.out.println("LIST FILE IS: " + LIST_FILE);
            for(String str : list){
                writer.println("file " + "'" + _namesDB.getFile(str).getAbsolutePath() + "'");
            }
            writer.close();
        } catch (FileNotFoundException|UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
