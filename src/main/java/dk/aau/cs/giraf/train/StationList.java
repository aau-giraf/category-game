package dk.aau.cs.giraf.train;

import java.util.ArrayList;

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

    public void receivePictograms(long[] pictogramIds, int selectedstation) {
        int i = this.stations.get(selectedstation).getAcceptPictograms().size();
        for (long id : pictogramIds) {
            if(i < 6){
                this.stations.get(selectedstation).addAcceptPictogram(id);
                i++;
            }
        }
    }

    public void receivePictograms(long pictogramId, int selectedStation, int selectedAcceptPictogram, boolean category){
        if(!category){
            this.stations.get(selectedStation).changeAcceptPictogram(selectedAcceptPictogram,pictogramId);
        }
        else{
            this.stations.get(selectedStation).setCategory(pictogramId);
        }
    }
}
