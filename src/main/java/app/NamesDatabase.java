package app;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Names Database loaded in from the database location
 */
public class NamesDatabase {
    private HashMap<String,File> _namesDB;

    public boolean checkExists(String name){
        return _namesDB.containsKey(name);
    }

    public File getFile(String name){
        return _namesDB.get(name);
    }

    public NamesDatabase(){
        initialise();
    }

    /**
     * Set-up the database by loading in the names in the database (audio files) and storing them
     * in a HashMap with the key being the name and the value being the file
     */
    private void initialise(){
        // setup database
        _namesDB = new HashMap<>();
        File namesDir = new File(Main.DATABASE_LOCATION);
        File[] listOfNames = namesDir.listFiles();

        // for now names have no spaces and only alphabetical characters
        String patternString = "_([A-z]+)" + Main.AUDIO_FILETYPE + "$";
        Pattern pattern = Pattern.compile(patternString);

        for (int i = 0; i < listOfNames.length; i++) {

            File nameFile = listOfNames[i];
            Matcher matcher = pattern.matcher(nameFile.getName());
            while(matcher.find()){
                String name = matcher.group(1);

                // TODO: Deal with duplicate names

                // TODO: Quality handling might go here (if its database related not really...)

                _namesDB.put(name, nameFile);
            }
        }
//        testPrintDatabase(); // For testing
    }

    // TODO: delete later when testing not needed
    private void testPrintDatabase(){
        System.out.println("\n======================================================");
        System.out.println("\nTest Printing Database:\n");
        for (String name : _namesDB.keySet()){
            System.out.println(name + " : " + _namesDB.get(name).getName());
        }
        System.out.println("======================================================\n");
    }
}
