package app.controller;

import app.DialogGenerator;
import app.Main;
import app.Name;
import app.NamesDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EditController implements Initializable {
    private static final String LIST_FILE = Main.COMPOSITE_LOCATION + "/mylist.txt";

    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();
    private NamesDatabase _namesDB;

    @FXML private ListView<Name> selectedNamesList;
    @FXML private TextField nameInput;

    public EditController(List<Name> names){
        for(Name nameToAdd : names){
            // TODO: Setup names inputted properly with concat...
            _selectedNames.add(nameToAdd);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _stage = Main.getStage();
        _namesDB = new NamesDatabase();

        selectedNamesList.setPlaceholder(new Label("No Names To Practice :("));
        selectedNamesList.getItems().addAll(_selectedNames);
    }

    @FXML
    private void addButtonPress(){
        // TODO: add the name to list of stuff to practice
        String nameStr = nameInput.getText();
        if(nameInput.getText() != null && !nameInput.getText().trim().isEmpty()) {
            Name nameToAdd = createName(nameStr.trim());
            if(nameToAdd != null){
                _selectedNames.add(nameToAdd);
                selectedNamesList.setItems(_selectedNames);
                nameInput.setText(""); // clear TextField
            }
        } else {
            DialogGenerator.showErrorMessage("Name is blank.");
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
            selectedNamesList.setItems(_selectedNames);
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

    /**
     * createName creates a Name object. There is a prompt if the part of the Name is not in the database
     * asking the user what they want to do ?????
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
            if(namesInDatabase.size() == 1){
                return new Name(nameStr, _namesDB.getFile(nameStr));
            } else {
                // create filename for concated audio
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

                return new Name(nameStr, new File(output));
            }
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
