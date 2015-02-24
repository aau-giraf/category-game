package dk.aau.cs.giraf.train;

import android.widget.ImageButton;

import java.util.ArrayList;

import dk.aau.cs.giraf.train.opengl.game.Station;

public class StationList {
    public ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();

    public ArrayList<StationConfiguration> getStations() {
        return new ArrayList<StationConfiguration>(this.stations);
    }

    public void setStationConfigurations(ArrayList<StationConfiguration> sConfigurations) {
        this.stations.clear();

        for (StationConfiguration station : sConfigurations) {
            this.stations.add(station);
        }
    }

    public void removeStation(int index){
        stations.remove(index);
    }

    public void removeStation(StationConfiguration station){
        stations.remove(station);
    }

    public void receivePictograms(int[] pictogramIds, int selectedstation) {
        int i = this.stations.get(selectedstation).getAcceptPictograms().size();
        for (int id : pictogramIds) {
            if(i < 6){
                this.stations.get(selectedstation).addAcceptPictogram(id);
                i++;
            }
        }
    }

    public void receivePictograms(int pictogramId, int selectedStation, int selectedAcceptPictogram, boolean category){
        if(!category){
            this.stations.get(selectedStation).changeAcceptPictogram(selectedAcceptPictogram,pictogramId);
        }
        else{
            this.stations.get(selectedStation).setCategory(pictogramId);
        }
    }
}
