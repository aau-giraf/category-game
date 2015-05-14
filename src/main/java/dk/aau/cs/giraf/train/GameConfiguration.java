package dk.aau.cs.giraf.train;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class contains all information of a game configuration.
 * It contains the id of the child associated with it, aswell as the guardian id, and a list of stations.
 * 
 * @author Nicklas Andersen
 * @see StationConfiguration
 */
public class GameConfiguration implements Parcelable {

    private long   guardianID;
	private String gameName;
	private long   childID;
	private int   gameID;
    private int  distanceBetweenStations;
	private ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();
	
	/*public GameConfiguration(String gameName, int gameID, int childID) {
		this.gameName = gameName;
		this.childID = childID;
		this.gameID = gameID;
		this.guardianID = Data.;
	}*/
	
	public GameConfiguration(String gameName, int gameID, long childID, long guardianID, int distanceBetweenStations) {
		this.gameName = gameName;
		this.childID = childID;
		this.gameID = gameID;
		this.guardianID = guardianID;
        this.distanceBetweenStations = distanceBetweenStations;
	}

    public int getDistanceBetweenStations(){
        return this.distanceBetweenStations;
    };
	public long getChildId() {
	    return this.childID;
	}
    public long getGuardianID(){ return this.guardianID; }

	
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
	
	public ArrayList<Integer> getIdOfAllPictograms(){
		ArrayList<Integer> pictogramIds =new ArrayList<Integer>();
		
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
        out.writeLong(this.guardianID);
        out.writeString(this.gameName);
        out.writeLong(this.childID);
        out.writeInt(this.gameID);
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
        this.guardianID = in.readInt();
        this.gameName = in.readString();
        this.childID = in.readInt();
        this.gameID = in.readInt();
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
    	
    	sWriter.write(String.valueOf(this.gameID));
    	sWriter.append(",");
    	sWriter.write(String.valueOf(this.guardianID));
    	sWriter.append(",");
    	sWriter.write(String.valueOf(this.childID));
    	sWriter.append(",");
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
    
    public boolean readConfiguration(int gameID) {
    	
    	return true;
    }
}
