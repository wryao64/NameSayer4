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
 * The pictures of pets are sent in from our friends!
 */
public class UserPetCollection {
    private static final double INITIAL_DROP_RATE = 0.03;
    private static final double GAIN_RATE = 0.03;
    private static final String IMAGE_FOLDER = Main.ASSETS_LOCATION + "/.pets";

    private int latestIndex;
    private List<File> pets;
    private double dropRate;
    private File imageFolder;

    public UserPetCollection(){
        pets = new ArrayList<>();
        dropRate = INITIAL_DROP_RATE;
        imageFolder = new File(IMAGE_FOLDER);

        // get a pet to start with
        pets.add(getFreshPetPicture());
        latestIndex = 0;
    }

    public void tryDropPetPic(){
        // check there are still pet photos
        if(!(pets.size() == imageFolder.listFiles().length) ) {
            // roll a random number
            double chance = Math.random();
            if (dropRate > chance) {
                // user has won a new image
                dropRate = INITIAL_DROP_RATE;
                pets.add(getFreshPetPicture());
                latestIndex++;
                Notifications.create()
                        .position(Pos.BOTTOM_CENTER)
                        .darkStyle()
                        .title("Cute animal picture!")
                        .text("You got a new cute animal picture. Take a break to check it out.")
                        .showConfirm();
            } else {
                dropRate = dropRate + GAIN_RATE;
            }
        }
    }

    private File getFreshPetPicture(){
        // go to the folder and get an image file
        File[] memesFiles = imageFolder.listFiles();

        // get a random file, making sure folder is an image and previously won
        File file;
        while(true){
            // (int)(Math.random() * ((max - min) + 1)) + min gets random int in a range
            int randIndex = (int)(Math.random() * ((memesFiles.length - 1 - 0) + 1)) + 0;
            file = memesFiles[randIndex];
            try {
                if( isImage( file ) && !pets.contains( file ) ) {
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
        return new Image( pets.get( index ).toURI().toString() );
    }

    public int getLatestIndex(){
        return latestIndex;
    }
}
