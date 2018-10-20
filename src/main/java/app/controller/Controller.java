package app.controller;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class Controller implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public abstract void setButtonDisable();
}
