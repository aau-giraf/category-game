package dk.aau.cs.giraf.categorygame;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.Settings;

public class ConfigurationList {

    private Profile currentProfile = null;
    private Activity caller;
    private ProfileController profileController;
    private ArrayList<GameConfiguration> listOfConfiguration = new ArrayList<GameConfiguration>();
    private static final String TAG = ConfigurationList.class.getName();


    public ConfigurationList(Activity a, Profile c){
        this.profileController = new ProfileController(a.getApplicationContext());
        this.currentProfile = c;
        this.caller = a;
        this.GetSavedSettings();
        this.getPremadeGames();
    }

    public void update(Profile c){
        this.listOfConfiguration.clear();
        this.currentProfile = c;
        this.GetSavedSettings();
        this.getPremadeGames();
    }

    public void addConfiguration(GameConfiguration g){
        this.listOfConfiguration.add(g);
    }

    public ArrayList<GameConfiguration> getGameconfiguration(){
        return this.listOfConfiguration;
    }

    public void removeConfiguration(GameConfiguration g){
        this.listOfConfiguration.remove(g);
    }


    public void GetSavedSettings() {
        Log.d(this.TAG, "Getting saved settings for currentProfile");
        Settings setting;
        setting = this.currentProfile.getNewSettings();
        //Hack to check if settings is empty, there is no method for this.
        if(setting.toJSON().equals("{}")){
            Log.d(this.TAG, "No setting saved, nothing to parse.");
            return;
        }


        //Parse configurations
        String configurations = setting.getSetting(this.caller.getApplicationContext(), "getGames");
        Log.d(this.TAG, configurations);
        this.listOfConfiguration.addAll(parseConfigurations(configurations));

    }


    private void getPremadeGames() {
        Log.d(this.TAG, "Trying to parse premade games");
        //Read the games into a string
        String gamesString = Utility.readRawFileToString(this.caller, R.raw.premade_games);

        //Parse the games
        ArrayList<GameConfiguration> gameConfigurations = this.parseConfigurations(gamesString);
        int numberOfGames = gameConfigurations.size();
        /*Check if the games created already exists in the users list of games
        Since we do not want to add them twice
         */
        int numberOfGamesDeleted = 0;
        Iterator<GameConfiguration> iterator = gameConfigurations.iterator();
        gameConfigurations = new ArrayList<GameConfiguration>();
        while (iterator.hasNext()) {
            GameConfiguration g = iterator.next();
            if (gameExists(g)) {
                //Remove the game from the list.
                Log.d(this.TAG, "Premade game already exists: " + g.getGameName());
                iterator.remove();
                //Log.d(this.TAG, "Size of premadeconfigs: " + gameConfigurations.size());
                numberOfGamesDeleted++;

            }
            else{
                gameConfigurations.add(g);
            }

        }

        //Check if all games hasn't been removed
        if (numberOfGames != numberOfGamesDeleted) {
            //Get the games from the iterator
            Log.d(this.TAG, "Adding premade games");
            this.listOfConfiguration.addAll(gameConfigurations);
        }
        else{
            Log.d(this.TAG, "All premade games already exists");
        }


    }

    private boolean gameExists(GameConfiguration gameConfiguration) {
        //Check for gameName
        for (GameConfiguration gC : this.listOfConfiguration) {
            if (gameConfiguration.getGameName().equals(gC.getGameName())) {
                return true;
            }
        }
        return false;
    }


    private ArrayList<GameConfiguration> parseConfigurations(String configurations) {
        /*Saved games are on the form Gm-30-104@11618.1760.529&105@105.1762.6368
        where:
        Gm is the game name,
        - is a sperator due to db settings implementation
        30 is the distance betweenstations
        104 is the category id of a station
        @ seperates category id from acceptPictogram ids
        11618.1760.529 is acceptPictograms
        & seperates stations
        _ seperates other game configurations
         */

        String[] configs = configurations.split("_");
        ArrayList<GameConfiguration> games = new ArrayList<GameConfiguration>();
        for (int i = 0; i < configs.length; i++) {
            GameConfiguration gC =  null;
            String[] parts = configs[i].split("-");
            String gameName = parts[0];
            int distanceBetweenStations = Integer.parseInt(parts[1]);

            //Instantiate gC object
            gC = new GameConfiguration(gameName, distanceBetweenStations);

            String[] stations = parts[2].split("&");
            for(int j = 0; j < stations.length; j++) {
                //split into category and accepted pictograms
                Log.d(this.TAG, "Number of stations to parse: " + stations.length);
                String[] stationParts = stations[j].split("@");
                long categoryID = Long.parseLong(stationParts[0]);
                StationConfiguration sC = new StationConfiguration(categoryID);
                //Split into array of accepted pictograms
                String[] acceptedPictogramsStrings = stationParts[1].split("\\.");
                //Iterate through accepted pictograms

                for(int k = 0; k < acceptedPictogramsStrings.length; k++) {
                    sC.addAcceptPictogram(Long.parseLong(acceptedPictogramsStrings[k]));
                }

                gC.addStation(sC);
            }

            games.add(gC);
        }
        return games;
    }

    public void SaveSettings() {
        /*Saved games are on the form Gm-30-104@11618.1760.529&105@105.1762.6368
        where:
        Gm is the game name,
        - is a sperator due to db settings implementation
        30 is the distance betweenstations
        104 is the category id of a station
        @ seperates category id from acceptPictogram ids
        11618.1760.529 is acceptPictograms
        & seperates stations
        _ seperates other game configurations
         */
        Log.d(this.TAG, "Saving settings for currentProfile");
        Settings s = new Settings();

        if (this.listOfConfiguration.size() > 0) {
            String gameConfigurations = "";
            for (GameConfiguration gc : this.listOfConfiguration) {
                //Iterate through all game configurations
                gameConfigurations += gc.getGameName() + "-";
                gameConfigurations += Integer.toString(gc.getDistanceBetweenStations()) + "-";

                //Iterate through all stations in a configuration
                for (StationConfiguration station : gc.getStations()) {
                    gameConfigurations += Long.toString(station.getCategory()) + "@";

                    for (long acceptPictogram : station.getAcceptPictograms()) {

                        gameConfigurations += Long.toString(acceptPictogram) + ".";
                    }
                    gameConfigurations = gameConfigurations.substring(0, gameConfigurations.length() - 1);

                    gameConfigurations += "&";
                }
                gameConfigurations = gameConfigurations.substring(0, gameConfigurations.length() - 1);

                gameConfigurations += "_";


            Log.d(this.TAG, gameConfigurations);
        }
            gameConfigurations = gameConfigurations.substring(0, gameConfigurations.length()-1);
            s.createSetting(this.caller.getApplicationContext(), "getGames", gameConfigurations);
        }

        this.currentProfile.setNewSettings(s);
        Log.d(this.TAG, s.toJSON());
        this.profileController.modify(currentProfile);

    }

}
