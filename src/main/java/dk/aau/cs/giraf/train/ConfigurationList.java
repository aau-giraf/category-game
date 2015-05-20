package dk.aau.cs.giraf.train;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.dblib.models.Settings;

public class ConfigurationList {

    private Profile currentProfile = null;
    private Activity caller;
    private Context context;
    private Helper helper;
    private ProfileController profileController;
    private ArrayList<GameConfiguration> listOfConfiguration = new ArrayList<GameConfiguration>();
    private static final String TAG = ConfigurationList.class.getName();
    //Strong constants used in the class
    public final String getGames = "getGames";
    public final String game = "game";
    public final String gameID = "gameID";
    public final String profileID = "profileID";
    public final String guardianID = "guardianID";
    public final String distanceBetweenStations = "distanceBetweenStations";
    public final String stations = "stations";
    public final String category = "category";
    public final String acceptPitograms = "acceptPitograms";


    public ConfigurationList(Activity a, Profile c, Context context){
        this.profileController = new ProfileController(context);
        this.helper = new Helper(context);
        this.currentProfile = c;
        this.caller = a;
        this.context = context;
        this.GetSavedSettings();
    }

    public void update(Profile c){
        this.listOfConfiguration.clear();
        this.currentProfile = c;
        this.GetSavedSettings();
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
        String configurations = setting.getSetting(this.context, "getGames");
        Log.d(this.TAG, configurations);
        this.listOfConfiguration = this.parseConfigurations(configurations);

    }

    private ArrayList<GameConfiguration> parseConfigurations(String configurations) {
        String[] configs = configurations.split("\n");
        ArrayList<GameConfiguration> games = new ArrayList<GameConfiguration>();
        for (int i = 0; i < configs.length; i++) {
            GameConfiguration gC =  null;
            String[] parts = configs[i].split("-");
            String gameName = parts[0];
            int distanceBetweenStations = Integer.parseInt(parts[1]);

            //Instantiate gC object
            gC = new GameConfiguration(gameName, distanceBetweenStations);

            String[] stations = parts[2].split(";");
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

                    gameConfigurations += ";";
                }
                gameConfigurations = gameConfigurations.substring(0, gameConfigurations.length() - 1);

                gameConfigurations += "\n";


            Log.d(this.TAG, gameConfigurations);
        }
            gameConfigurations = gameConfigurations.substring(0, gameConfigurations.length()-1);
            s.createSetting(this.context, "getGames", gameConfigurations);
        }

        this.currentProfile.setNewSettings(s);
        Log.d(this.TAG, s.toJSON());
        this.profileController.modify(currentProfile);

    }

   /* public void loadPredefinedGames() {
        //Load predefined categories

        new ArrayList<Integer>(Arrays.asList(1,2,3,5,8,13,21));

        StationConfiguration station1 = new StationConfiguration(2);
        //Add pictograms to station
        ArrayList<Long> acceptedpictogramsStation1 = new ArrayList<Long>(Arrays.asList(ids));
        for (long acceptedpictogram : acceptedpictogramsStation1) {
            station1.addAcceptPictogram(acceptedpictogram);
        }

        StationConfiguration station2 = new StationConfiguration(2);
        //Add pictograms to station
        ArrayList<Long> acceptedpictogramsStation2 = new ArrayList<Long>(Arrays.asList(ids));
        for (long acceptedpictogram : acceptedpictogramsStation2) {
            station2.addAcceptPictogram(acceptedpictogram);
        }

        StationConfiguration station3 = new StationConfiguration(2);
        //Add pictograms to station
        ArrayList<Long> acceptedpictogramsStation3 = new ArrayList<Long>(Arrays.asList(ids));
        for (long acceptedpictogram : acceptedpictogramsStation3) {
            station3.addAcceptPictogram(acceptedpictogram);
        }
        StationConfiguration station4 = new StationConfiguration(2);
        //Add pictograms to station
        ArrayList<Long> acceptedpictogramsStation4 = new ArrayList<Long>(Arrays.asList(ids));
        for (long acceptedpictogram : acceptedpictogramsStation3) {
            station4.addAcceptPictogram(acceptedpictogram);
        }

        GameConfiguration gameConfiguration = null;

    }*/


    private void makePremadeGames() {

        ArrayList<StationConfiguration> stationConfigurationsGame1 = new ArrayList<StationConfiguration>();

        ArrayList<Long> categoriesForGame1 = new ArrayList<Long>();

        for (long category : categoriesForGame1){
            StationConfiguration stationConfiguration = new StationConfiguration(category);
            //for ()
        }


    }

    private GameConfiguration makeGameconfiguration(String name, int distanceBetweenStations, ArrayList<StationConfiguration> stations) {
        GameConfiguration gameConfiguration = new GameConfiguration(name, distanceBetweenStations);
        gameConfiguration.setStations(stations);
        return gameConfiguration;





    }

    private StationConfiguration makeStation(long category, ArrayList<Long> acceptedpictograms ) {
        ArrayList<StationConfiguration> stationConfigurations = new ArrayList<StationConfiguration>();
        StationConfiguration stationConfiguration = new StationConfiguration(category);
        for (long acceptedpictogram : acceptedpictograms) {
            stationConfiguration.addAcceptPictogram(acceptedpictogram);

        }
        return stationConfiguration;
    }

}
