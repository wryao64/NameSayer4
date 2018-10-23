package app.controller;

import app.*;
import app.pet.UserPetCollection;
import app.name.Name;
import app.name.NameProcessor;
import app.name.NamesDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.net.URL;
import java.util.*;

public class EditController implements Initializable {

    private Stage _stage;
    private ObservableList<Name> _selectedNames = FXCollections.observableArrayList();

    private NamesDatabase _namesDB = new NamesDatabase();
    private UserPetCollection _user;

    @FXML private ListView<Name> selectedNamesList;
    @FXML private TextField nameInput;
    @FXML private Label title;
    @FXML private Button removeButton;

    /**
     * EditController constructor from a single string (for coming from Welcome screen)
     * @param name A name to add as a string
     */
    public EditController(String name){
        NameProcessor np = new NameProcessor();
        Name nameToAdd = np.createName(name);
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
        Label tempLabel = new Label("No Names To Practise :(");
        tempLabel.setId("temp-label");
        selectedNamesList.setPlaceholder(tempLabel);
        selectedNamesList.getItems().addAll(_selectedNames);
        _selectedNames.addListener((ListChangeListener<Name>) c -> {
            selectedNamesList.setItems(_selectedNames);
        });

        // disable remove button until a name on the list is selected
        removeButton.setDisable(true);
        selectedNamesList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> checkRemoveButton());

        // setup autocomplete
        TextFields.bindAutoCompletion(nameInput,
                textField -> _namesDB.getSuggestedNames(nameInput.getText()));
    }

    @FXML
    private void addButtonPress(){
        String nameStr = nameInput.getText().trim();
        if(nameStr == null || nameStr.isEmpty()) {
            DialogGenerator.showErrorMessage("Name is blank.");
        } else if(_selectedNames.contains(new Name(nameStr, null))) {
            DialogGenerator.showErrorMessage(nameStr + " has already been added.");
        } else {
            addName(nameStr);
        }
        nameInput.setText(""); // clear TextField
    }

    private void addName(String name){
        NameProcessor np = new NameProcessor();
        Name nameToAdd = np.createName(name);
        if (nameToAdd != null) {
            _selectedNames.add(nameToAdd);
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

    private void checkRemoveButton() {
        if(selectedNamesList.getSelectionModel().getSelectedItem() != null) {
            removeButton.setDisable(false);
        } else {
            removeButton.setDisable(true);
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
                // make sure no duplicate names are added
                if(! _selectedNames.contains( new Name( line, null) ) ){
                    addName(line);
                }
            }
            DialogGenerator.showOkMessage("File Loaded","Names from " + file.getName() + " have been read in.");
        } catch(IOException e){
            DialogGenerator.showErrorMessage("Could not read file :(");
        }
    }
}
