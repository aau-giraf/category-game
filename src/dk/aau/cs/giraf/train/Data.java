package dk.aau.cs.giraf.train;

import android.content.Context;

import java.util.List;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public final class Data {

	public int appBackgroundColor;
    public Profile guardianProfile;
    public Profile childProfile;

    public Data(int guardianID, long childID, int backgroundColor, Context context){
        Helper localDataFetcher = null;
        try {
            localDataFetcher = new Helper(context);
        } catch (Exception e) {
            //TODO:lav en ordenlig exception
        }

        appBackgroundColor = backgroundColor;

        guardianProfile = localDataFetcher.profilesHelper.getProfileById(guardianID);
        childProfile = localDataFetcher.profilesHelper.getProfileById((int)childID);
    }
}
