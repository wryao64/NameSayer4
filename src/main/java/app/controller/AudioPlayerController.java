package app.controller;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AudioPlayerController {
    protected boolean _buttonDisabled = false;

    public abstract void setButtonDisable();
}
