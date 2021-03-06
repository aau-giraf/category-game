package dk.aau.cs.giraf.train;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * This class contains all information of a game configuration.
 * It contains the id of the child associated with it, aswell as the guardian id, and a list of stations.
 *
 * @author Nicklas Andersen
 * @see StationConfiguration
 */
public class GameConfiguration implements Parcelable {

    private String gameName;
    private int  distanceBetweenStations;
    private ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();
	
	public GameConfiguration(String gameName, int distanceBetweenStations) {
		this.gameName = gameName;
		this.distanceBetweenStations = distanceBetweenStations;
	}


    public int getDistanceBetweenStations(){
        return this.distanceBetweenStations;
    };


    public void addStation(StationConfiguration station) {
        this.stations.add(station);
    }

    public void setStations(ArrayList<StationConfiguration> stations) {
        this.stations = stations;
    }

    public ArrayList<StationConfiguration> getStations(){
        return this.stations;
    }

    public StationConfiguration getStation(int value){
        return this.stations.get(value);
    }

    public String getGameName() {
        return this.gameName;
    }

    public int getNumberOfPictogramsOfStations(){
        int numberOfPictograms = 0;
        for (StationConfiguration station : this.stations) {
            numberOfPictograms += station.getAcceptPictograms().size();
        }
        return numberOfPictograms;
    }

    public ArrayList<Long> getIdOfAllPictograms(){
        ArrayList<Long> pictogramIds = new ArrayList<Long>();

        for (StationConfiguration station : this.stations) {
            pictogramIds.addAll(station.getAcceptPictograms());
        }
        return pictogramIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.gameName);
        out.writeInt(this.distanceBetweenStations);
        out.writeList(this.stations);
    }


    public static final Parcelable.Creator<GameConfiguration> CREATOR = new Parcelable.Creator<GameConfiguration>() {
        @Override
        public GameConfiguration createFromParcel(Parcel in) {
            return new GameConfiguration(in);
        }

        @Override
        public GameConfiguration[] newArray(int size) {
            return new GameConfiguration[size];
        }
    };

    private GameConfiguration(Parcel in) {
        this.gameName = in.readString();
        this.distanceBetweenStations = in.readInt();
        in.readList(this.stations, StationConfiguration.class.getClassLoader());
    }

    /**
     * Write the configuration to a string.
     * Example format: gameID,guardianID,childID,gameName;category,pictogram,pictogram;category,pictogram\n
     * @return String representation of the configuration.
     * @throws IOException
     */
    public String writeConfiguration() throws IOException {
        StringWriter sWriter = new StringWriter(1024);

        sWriter.write(this.gameName);
        sWriter.append(",");
        sWriter.write(String.valueOf(this.distanceBetweenStations));

        for(StationConfiguration station : stations) {
            sWriter.append(";");
            sWriter.write(station.writeStation());
        }

        sWriter.append("\n");

        String result = sWriter.toString();
        sWriter.close();

        return result;
    }
}
