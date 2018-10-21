package app.controller;

import app.Main;
import app.name.Name;
import app.dog.UserPetCollection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MemeViewerController implements Initializable {
    @FXML ImageView imageView;
    @FXML Label title;
    @FXML Button previousButton;
    @FXML Button nextButton;
    @FXML Label aboutText;

    private SimpleIntegerProperty currentMemeIndex;
    private UserPetCollection _user;
    private List<Name> namesToPassBack;

    public MemeViewerController(UserPetCollection user, List<Name> names){
        _user = user;
        currentMemeIndex = new SimpleIntegerProperty(user.getLatestIndex());
        namesToPassBack = names;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // bind currentMemeIndex to change the dog and stuff
        currentMemeIndex.addListener((observable, oldValue, newValue) -> {
            showMeme();
            nextButtonDisablerCheck();
            previousButtonDisableCheck();
            title.setText("Viewing memes!");
        });

        // Initial setup
        showMeme();
        nextButtonDisablerCheck();
        previousButtonDisableCheck();

        if(currentMemeIndex.intValue() == 0){
            title.setText("Here's a starter dog!");
            aboutText.setText("More memes will randomly drop when you record, listen and practise more names!");
        } else {
            title.setText("Here's your fresh dog!");
            aboutText.setText("");
        }
    }

    @FXML
    private void backButtonPress(){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(namesToPassBack));
            Parent nameView = loader.load();
            Main.getStage().setScene(new Scene(nameView));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void next(){
        currentMemeIndex.set(currentMemeIndex.intValue() + 1);
    }

    @FXML
    private void previous(){
        currentMemeIndex.set(currentMemeIndex.intValue() - 1);
    }

    // show a dog on the ImageView
    private void showMeme(){
        imageView.setImage(_user.getPicAtIndex(currentMemeIndex.intValue()));
    }

    private void nextButtonDisablerCheck(){
        if(currentMemeIndex.intValue() == _user.getLatestIndex()){
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
    }

    private void previousButtonDisableCheck(){
        if(currentMemeIndex.intValue() == 0){
            previousButton.setDisable(true);
        } else {
            previousButton.setDisable(false);
        }
    }

}
