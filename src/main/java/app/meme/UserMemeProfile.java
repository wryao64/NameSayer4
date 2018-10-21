package app.meme;

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
 * Keeps track of user's meme collection (rewards system)
 * Memes are shamelessly stolen from Reddit among other sources.
 */
public class UserMemeProfile {
    private static final double INITIAL_MEME_DROP_RATE = 0.03;
    private static final double MEME_GAINS_RATE = 0.03;
    private static final String MEMES_FOLDER = Main.ASSETS_LOCATION + "/.memes";

    private int freshestMemeIndex;
    private List<File> memes;
    private double memeDropRate;
    private File memesFolder;

    public UserMemeProfile(){
        memes = new ArrayList<>();
        memeDropRate = INITIAL_MEME_DROP_RATE;
        memesFolder = new File(MEMES_FOLDER);

        // get a meme to start with
        memes.add(getFreshRandomMeme());
        freshestMemeIndex = 0;
    }

    public void tryDropMeme(){
        // check there are still memes
        if(!(memes.size() == memesFolder.listFiles().length) ) {
            // roll a random number
            double chance = Math.random();
            if (memeDropRate > chance) {
                // user has won a meme
                memeDropRate = INITIAL_MEME_DROP_RATE;
                memes.add(getFreshRandomMeme());
                freshestMemeIndex++;
                Notifications.create()
                        .position(Pos.BOTTOM_CENTER)
                        .darkStyle()
                        .title("Meme reward!")
                        .text("Congrats you just won a meme!")
                        .showConfirm();
            } else {
                memeDropRate = memeDropRate + MEME_GAINS_RATE;
            }
        }
    }

    private File getFreshRandomMeme(){
        // go to the meme folder and get a meme image file
        File[] memesFiles = memesFolder.listFiles();

        // get a random file, making sure folder is an image and not a previous meme
        File file;
        while(true){
            // (int)(Math.random() * ((max - min) + 1)) + min gets random int in a range
            int randIndex = (int)(Math.random() * ((memesFiles.length - 1 - 0) + 1)) + 0;
            file = memesFiles[randIndex];
            try {
                // check it is an image and fresh
                if( isImage( file ) && !memes.contains( file ) ) {
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

    public Image getMemeAtIndex(int index){
        return new Image( memes.get( index ).toURI().toString() );
    }

    public int getFreshestMemeIndex(){
        return freshestMemeIndex;
    }
}
