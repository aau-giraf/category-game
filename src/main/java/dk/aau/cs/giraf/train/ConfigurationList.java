package dk.aau.cs.giraf.train;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import dk.aau.cs.giraf.dblib.models.Profile;

public class ConfigurationList {

    private Profile currentProfile = null;
    private Activity caller;

    private ArrayList<GameConfiguration> listOfConfiguration = new ArrayList<GameConfiguration>();
    /*Internal storage for all configurations*/
    private ArrayList<GameConfiguration> listOfAllConfiguration = new ArrayList<GameConfiguration>();

    public ConfigurationList(Activity a, Profile c){
        currentProfile = c;
        caller = a;
        loadAllConfigurations();
    }

    public void update(Profile c){
        listOfConfiguration.clear();
        listOfAllConfiguration.clear();

        currentProfile = c;
        loadAllConfigurations();
    }

    public void addConfiguration(GameConfiguration g){
        listOfConfiguration.add(g);
        listOfAllConfiguration.add(g);
    }

    public ArrayList<GameConfiguration> getGameconfiguration(){
        return listOfConfiguration;
    }

    public void removeConfiguration(GameConfiguration g){
        listOfConfiguration.remove(g);
        listOfAllConfiguration.remove(g);
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
            try{
                int gameID = Integer.parseInt(game[0]);
                int guardianID = Integer.parseInt(game[1]);
                int childID = Integer.valueOf(game[2]);
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

                if(this.currentProfile.getId() == gameConf.getChildId() || this.currentProfile.getId() == gameConf.getGuardianID()){
                    this.listOfConfiguration.add(gameConf);
                }
                this.listOfAllConfiguration.add(gameConf);
            } catch (NumberFormatException e){

            }
        }
    }

    public void saveAllConfigurations(String saveFilePath) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = caller.openFileOutput(saveFilePath, Context.MODE_PRIVATE);
            for (GameConfiguration game : this.listOfAllConfiguration) {
                fos.write(game.writeConfiguration().getBytes());
            }
        } catch(FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
    }
}
