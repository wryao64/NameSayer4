package app;

import app.pet.UserPetCollection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static final String AUDIO_FILETYPE = ".wav";
    public static final String ASSETS_LOCATION = new File("assets").getAbsolutePath();
    public static final String DATABASE_LOCATION = new File(ASSETS_LOCATION + "/names").getAbsolutePath();
    public static final String RECORDING_LOCATION = new File(ASSETS_LOCATION + "/user").getAbsolutePath();
    public static final String TEMP_LOCATION = new File(ASSETS_LOCATION + "/temp").getAbsolutePath();
    public static final String QUALITY_FILE = new File("badQualityRecordings.txt").getAbsolutePath();

    private static Stage _stage;

    private static UserPetCollection _user;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return _stage;
    }

    public static UserPetCollection getUser() {
            return _user;
    }

    @Override
    public void start(Stage primaryStage) {
        _stage = primaryStage;
        _stage.setOnCloseRequest(event -> onClose());
        setUpEnvironment();

        primaryStage.setTitle("NameSayer");

        // set-up welcome screen
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("controller/Welcome.fxml"));
            Parent layout = loader.load();
            primaryStage.setScene(new Scene(layout));
        } catch (IOException e){
            e.printStackTrace();
        }

        primaryStage.show();

        // create the user to track their memes :)
        _user = new UserPetCollection();
    }

    private void setUpEnvironment(){
        File assets = new File(ASSETS_LOCATION);
        File dbDirectory = new File(DATABASE_LOCATION);
        File userDirectory = new File(RECORDING_LOCATION);
        File tempDirectory = new File(TEMP_LOCATION);
        File quality = new File(QUALITY_FILE);

        if(!assets.exists()){
            assets.mkdir();
        }

        if(!dbDirectory.exists()){
            dbDirectory.mkdir();
        }

        if(!userDirectory.exists()){
            userDirectory.mkdir();
        }

        if(!tempDirectory.exists()){
            tempDirectory.mkdir();
        }

        if(!quality.exists()){
            try {
                quality.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Any cleanup when the program exits
     */
    private void onClose(){
        // Delete the temp files
        File dir = new File(TEMP_LOCATION);
        for(File file: dir.listFiles()){
            file.delete();
        }
    }
}