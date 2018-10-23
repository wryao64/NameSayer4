package app.name;

import app.Main;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents the Names Database loaded in from the database location
 */
public class NamesDatabase {
    private HashMap<String,File> _namesDB;

    public boolean checkExists(String name){
        return _namesDB.containsKey(name.toLowerCase());
    }

    public File getFile(String name){
        return _namesDB.get(name.toLowerCase());
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
                _namesDB.put(name.toLowerCase(), nameFile);
            }
        }
    }

    private List<String> getAllNames(){
        return new ArrayList<>(_namesDB.keySet());
    }

    /**
     * Gets suggested names for autocomplete functionality to show given a string
     * which should be the start of a name.
     * @param name The string the user has typed up to (the start of a name)
     * @return A list of matching names as a string that begin with the input
     */
    public List<String> getSuggestedNames(String name) {
        int latestIndex = findLatestSpaceInString(name);
        final String latestName;
        if(latestIndex == 0 ){
            latestName = name;
        } else {
            latestName = name.substring(latestIndex+1);
        }

        List<String> matchingNames = getAllNames();

        // filter suggestions
        matchingNames = matchingNames
                .stream()
                .filter(s -> s.toLowerCase().startsWith(latestName.toLowerCase()))
                .collect(Collectors.toList());

        List<String> suggestions = new ArrayList<>();

        for(String str : matchingNames){
            suggestions.add(str.substring(0,1).toUpperCase() + str.substring(1));
        }

        // add on the previous names
        if(latestIndex != 0){
            String prevName = name.substring(0, latestIndex + 1);
            for(int i = 0; i < suggestions.size(); i++){
                suggestions.set(i, prevName + suggestions.get(i));
            }
        }

        Collections.sort(suggestions);
        return suggestions;
    }

    private int findLatestSpaceInString(String str){
        int index = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == ' ' || str.charAt(i) == '-'){
                index = i;
            }
        }
        return index;
    }
}
