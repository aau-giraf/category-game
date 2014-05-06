package dk.aau.cs.giraf.train;

import android.app.Activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * Created by Aleksander on 05-05-14.
 */
public class ConfigurationList {

    private Profile childProfile = null;
    private Activity caller;

    public ArrayList<GameConfiguration> listOfConfiguration = new ArrayList<GameConfiguration>();

    public ConfigurationList(Activity a, Profile c){
        childProfile = c;
        caller = a;
        loadAllConfigurations();
    }

    private void loadAllConfigurations() {
        FileInputStream fis = null;
        StringWriter sWriter = new StringWriter(1024);

        try {
            fis = this.caller.getApplicationContext().openFileInput(MainActivity.SAVEFILE_PATH);

            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and append to string
                sWriter.append((char) content);
            }
        } catch(FileNotFoundException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sWriter.close();
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        this.splitConfigurations(sWriter.toString());
    }

    private void splitConfigurations(String data) {
        String[] configurations = data.split("\n");

        // For each configuration
        for (int i = 0; i < configurations.length; i++) {

            String[] parts = configurations[i].split(";");
            String[] game = parts[0].split(",");

            int gameID = Integer.parseInt(game[0]);
            int guardianID = Integer.parseInt(game[1]);
            long childID = Long.valueOf(game[2]);
            String gameName = game[3];
            int TempdistanceBetweenStations = Integer.parseInt(game[4]);
            ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();

            // For each station
            for (int k = 1; k < parts.length; k++) {
                StationConfiguration station = new StationConfiguration();
                String[] stationParts = parts[k].split(",");

                station.setCategory((Integer.parseInt(stationParts[0])));

                // For each accept pictogram of station
                for (int n = 1; n < stationParts.length; n++) {
                    station.addAcceptPictogram(Integer.parseInt(stationParts[n]));
                }
                stations.add(station);
            }

            GameConfiguration gameConf = new GameConfiguration(gameName, gameID, childID, guardianID, TempdistanceBetweenStations);
            gameConf.setStations(stations);

            if(this.childProfile.getId() != gameConf.getChildId()){
                this.listOfConfiguration.add(gameConf);
            }
        }
    }
}
