package dk.aau.cs.giraf.train;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.List;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Category;
import dk.aau.cs.giraf.dblib.models.Profile;


public final class ProfileData {

    public Profile guardianProfile;
    public Profile childProfile;
    public ProfileData(int guardianID, int childID, Context context){
        Helper localDataFetcher = null;
        try {
            localDataFetcher = new Helper(context);
        } catch (Exception e) {
            //TODO:lav en ordenlig exception
        }

        guardianProfile = localDataFetcher.profilesHelper.getProfileById(guardianID);
        if(childID != -1){
            childProfile = localDataFetcher.profilesHelper.getProfileById(childID);
        }
    }
    public ProfileData() {
        guardianProfile = new Profile("Tony Stark", 12345678, null, "tony@stark.dk", Profile.Roles.GUARDIAN, "address 1", null, 1, 0);
        Bitmap img = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < img.getWidth();i++){
            img.setPixel(i,i, Color.CYAN);
        }

        childProfile = new Profile("William Jensen", 88888888,  img, "william@jensen.dk", Profile.Roles.CHILD, "address 1", null, 1, 0);

    }
}
