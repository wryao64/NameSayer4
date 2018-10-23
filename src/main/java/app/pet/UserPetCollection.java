package app.pet;

import app.Main;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import org.controlsfx.control.Notifications;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of user's pet photo collection (rewards system)
 * The pictures of _pets are sent in from our friends!
 */
public class UserPetCollection {
    private static final double INITIAL_DROP_RATE = 0.03;
    private static final double GAIN_RATE = 0.03;
    private static final String IMAGE_FOLDER = Main.ASSETS_LOCATION + "/.pets";

    private int _latestIndex;
    private List<File> _pets;
    private double _dropRate;
    private File _imageFolder;

    public UserPetCollection(){
        _pets = new ArrayList<>();
        _dropRate = INITIAL_DROP_RATE;
        _imageFolder = new File(IMAGE_FOLDER);

        // get a pet to start with
        _pets.add(getFreshPetPicture());
        _latestIndex = 0;
    }

    public void tryDropPetPic(){
        // check there are still pet photos
        if(!(_pets.size() == _imageFolder.listFiles().length) ) {
            // roll a random number
            double chance = Math.random();
            if (_dropRate > chance) {
                // user has won a new image
                _dropRate = INITIAL_DROP_RATE;
                _pets.add(getFreshPetPicture());
                _latestIndex++;
                Notifications.create()
                        .position(Pos.BOTTOM_CENTER)
                        .darkStyle()
                        .title("Cute animal picture!")
                        .text("You got a new cute animal picture. Take a break to check it out.")
                        .showConfirm();
            } else {
                _dropRate = _dropRate + GAIN_RATE;
            }
        }
    }

    private File getFreshPetPicture(){
        // go to the folder and get an image file
        File[] memesFiles = _imageFolder.listFiles();

        // get a random file, making sure folder is an image and previously won
        File file;
        while(true){
            // (int)(Math.random() * ((max - min) + 1)) + min gets random int in a range
            int randIndex = (int)(Math.random() * ((memesFiles.length - 1 - 0) + 1)) + 0;
            file = memesFiles[randIndex];
            try {
                if( isImage( file ) && !_pets.contains( file ) ) {
                    break;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private boolean isImage(File file) throws IOException{
        return (ImageIO.read(file) != null);
    }

    public Image getPicAtIndex(int index){
        return new Image( _pets.get( index ).toURI().toString() );
    }

    public int getLatestIndex(){
        return _latestIndex;
    }
}
