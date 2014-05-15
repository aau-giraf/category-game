package dk.aau.cs.giraf.train;

import android.content.Context;

import java.util.List;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Category;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

public final class Data {

    public Profile guardianProfile;
    public Profile childProfile;
    public Data(int guardianID, int childID, Context context){
        Helper localDataFetcher = null;
        try {
            localDataFetcher = new Helper(context);
        } catch (Exception e) {
            //TODO:lav en ordenlig exception
        }

        guardianProfile = localDataFetcher.profilesHelper.getProfileById(guardianID);
        childProfile = localDataFetcher.profilesHelper.getProfileById(childID);
    }
}
