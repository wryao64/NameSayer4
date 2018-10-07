package app.meme;

import app.DialogGenerator;
import app.Main;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of user's meme collection (rewards system)
 * Memes are shamelessly stolen from Reddit among other sources.
 * hi brother cheers from iraq.
 * Now this is epic.
 * BOTTOM TEXT.
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

        // choose a starter meme (Charmander btw)
        memes.add(getFreshRandomMeme());
        freshestMemeIndex = 0;
    }

    public void tryDropMeme(){
        // check not outta memes
        if(!(memes.size() == memesFolder.listFiles().length) ) {
            // roll a random number
            double chance = Math.random();
            if (memeDropRate > chance) {
                // winner winner chicken dinner
                memeDropRate = INITIAL_MEME_DROP_RATE; // resetti spagetti
                memes.add(getFreshRandomMeme());
                freshestMemeIndex++;
                DialogGenerator.showOkMessage("You got a new meme", "Hey there buddy you actually just won a meme. Go reward yourself and check it out :)");
            } else {
                // hold this L feelsBadMan
                memeDropRate = memeDropRate + MEME_GAINS_RATE; // go gym get big
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
