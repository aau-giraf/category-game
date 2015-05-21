package dk.aau.cs.giraf.categorygame;

import android.content.Context;

import dk.aau.cs.giraf.dblib.controllers.ProfileController;
import dk.aau.cs.giraf.dblib.models.Profile;

public final class ProfileData {
    private static final String TAG = ConfigurationList.class.getName();
    public Profile guardianProfile;
    public Profile childProfile;
    public ProfileData(long guardianID, long childID, Context context){
        ProfileController pf = null;
        try {
            pf = new ProfileController(context);
        } catch (Exception e) {
            //TODO:lav en ordenlig exception
        }
        guardianProfile = pf.getById(guardianID);
        if(childID != -1){
            childProfile = pf.getById(childID);
        }
    }
}
