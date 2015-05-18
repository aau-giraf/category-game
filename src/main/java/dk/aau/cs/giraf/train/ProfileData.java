package dk.aau.cs.giraf.train;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;


public final class ProfileData {

    public Profile guardianProfile;
    public Profile childProfile;
    public ProfileData(long guardianID, long childID, Context context){
        Helper localDataFetcher = null;
        try {
            localDataFetcher = new Helper(context);
        } catch (Exception e) {
            //TODO:lav en ordenlig exception
        }

        guardianProfile = localDataFetcher.profilesHelper.getById(guardianID);
        if(childID != -1){
            childProfile = localDataFetcher.profilesHelper.getById(childID);
        }
    }
}
