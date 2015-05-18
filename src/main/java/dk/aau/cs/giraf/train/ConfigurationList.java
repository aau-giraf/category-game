package dk.aau.cs.giraf.train;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    public final String gameName = "gameName";
    public final String gameID = "gameID";
    public final String childID = "childID";
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
        //Try to parse the saved configurations
        try {
            GameConfiguration gC;
            StationConfiguration sC;
            //Get games configurations and parse to json
            String games = setting.getSetting(this.context, this.getGames);
            //Convert it to a JSONArray
            JSONArray jGameConfigurations = new JSONArray(games);
            //Parse jsonArray to list of game configurations objects
            for (int i = 0; i < jGameConfigurations.length(); i++) {
                JSONObject jgC = jGameConfigurations.getJSONObject(i);
                gC = new GameConfiguration(jgC.getString(this.gameName), jgC.getLong(this.gameID), jgC.getLong(this.childID), jgC.getLong(this.guardianID), jgC.getInt(this.distanceBetweenStations));

                //Load stations into gameconfiguration object
                JSONArray jStations = jgC.getJSONArray(this.stations);
                for (int j = 0; j < jStations.length(); j++) {
                    sC = new StationConfiguration(jStations.getJSONObject(j).getLong(this.category));
                    //Load pictograms associated with the station
                    JSONArray jAceptedPictograms = jStations.getJSONObject(j).getJSONArray(this.acceptPitograms);
                    for (int k = 0; k < jAceptedPictograms.length(); k++) {
                        long pictogramID = jAceptedPictograms.getLong(k);
                        Log.d(this.TAG, "PictogramID: " + pictogramID);
                        sC.addAcceptPictogram(pictogramID);
                    }
                    gC.addStation(sC);
                }
                this.listOfConfiguration.add(gC);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void SaveSettings() {
        Log.d(this.TAG, "Saving settings for currentProfile");
        Settings s = new Settings();


        //Create a json string with all the game configurations
        String stringOfGameConfigurations = new JSONArray(this.listOfConfiguration).toString();

        //Add the json string to the profile's setting
        s.createSetting(this.context, this.getGames, stringOfGameConfigurations);

        this.currentProfile.setNewSettings(s);
        this.profileController.modify(currentProfile);

    }

}
