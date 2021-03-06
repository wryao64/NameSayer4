package app.controller;

import app.Main;
import app.name.Name;
import app.pet.UserPetCollection;
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

public class ImageViewerController implements Initializable {
    @FXML ImageView imageView;
    @FXML Label title;
    @FXML Button previousButton;
    @FXML Button nextButton;
    @FXML Label aboutText;

    private SimpleIntegerProperty _currentIndex;
    private UserPetCollection _user;
    private List<Name> _namesToPassBack;

    public ImageViewerController(UserPetCollection user, List<Name> names){
        _user = user;
        _currentIndex = new SimpleIntegerProperty(user.getLatestIndex());
        _namesToPassBack = names;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // bind _currentIndex to change the pet and stuff
        _currentIndex.addListener((observable, oldValue, newValue) -> {
            showMeme();
            nextButtonDisablerCheck();
            previousButtonDisableCheck();
            title.setText("Cute animals");
        });

        // Initial setup
        showMeme();
        nextButtonDisablerCheck();
        previousButtonDisableCheck();

        if(_currentIndex.intValue() == 0){
            title.setText("Here's a cute animal picture!");
            aboutText.setText("More pictures will randomly drop when you record, listen and practise more names!");
        } else {
            title.setText("Here's your latest picture!");
            aboutText.setText("");
        }
    }

    @FXML
    private void backButtonPress(){
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("NameDisplay.fxml"));
            loader.setController(new NameDisplayController(_namesToPassBack));
            Parent nameView = loader.load();
            Main.getStage().setScene(new Scene(nameView));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void next(){
        _currentIndex.set(_currentIndex.intValue() + 1);
    }

    @FXML
    private void previous(){
        _currentIndex.set(_currentIndex.intValue() - 1);
    }

    // show a pet on the ImageView
    private void showMeme(){
        imageView.setImage(_user.getPicAtIndex(_currentIndex.intValue()));
    }

    private void nextButtonDisablerCheck(){
        if(_currentIndex.intValue() == _user.getLatestIndex()){
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
    }

    private void previousButtonDisableCheck(){
        if(_currentIndex.intValue() == 0){
            previousButton.setDisable(true);
        } else {
            previousButton.setDisable(false);
        }
    }

}
