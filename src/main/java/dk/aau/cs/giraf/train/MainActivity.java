package dk.aau.cs.giraf.train;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import dk.aau.cs.giraf.core.data.Data;
import dk.aau.cs.giraf.dblib.models.Profile;


import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GList;
import dk.aau.cs.giraf.gui.GToast;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.models.Profile;

import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.train.opengl.GameActivity;

import dk.aau.cs.giraf.core.data.ProcessManager;
import dk.aau.cs.giraf.pictosearch.PictoAdminMain;
import dk.aau.cs.giraf.train.opengl.GameActivity;
import dk.aau.cs.giraf.utilities.IntentConstants;

public class MainActivity extends Activity {
    public static final String SAVEFILE_PATH = "game_configurations.txt";
    public static final String GAME_CONFIGURATION = "GameConfiguration";
    public static final String GAME_CONFIGURATIONS = "GameConfigurations";
    public static final String SELECTED_CHILD_ID = "selectedChildId";
    public static final String SELECTED_CHILD_NAME = "selectedChildName";

    public static final int RECEIVE_SINGLE = 0;
    public static final int RECEIVE_MULTIPLE = 1;
    public static final int RECEIVE_GAME_NAME = 2;
    public static final int APPLICATIONBACKGROUND = 0xFFFFBB55;

    private int distanceBetweenStations;

    private boolean isGuestSession;

    private Intent gameIntent;
    private Intent saveIntent;
    private Intent categoryIntent;
    private Intent pictoAdminIntent = new Intent();
    private Intent download;
	
	private ProgressDialog progressDialog;

    private GButtonProfileSelect gButtonProfileSelect;
    private AlertDialog errorDialog;
    private ProfileData currentProfileData = null;
    private GGameListAdapter gameListAdapter;
    public ConfigurationList configurationHandler;

    public static final int ALLOWED_PICTOGRAMS = 12;
    public static final int ALLOWED_STATIONS = ALLOWED_PICTOGRAMS;

    private final int MINIMUM_TIME = 15;
    private final int MAXIMUM_TIME = 300;

    public StationList listOfStations = null;
    private GStationListAdapter stationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GComponent.SetBaseColor(APPLICATIONBACKGROUND);
        LayoutInflater li = LayoutInflater.from(this);
        View mainView = li.inflate(R.layout.activity_main, null);

        Intent intent = getIntent();
        Bundle extras = null;

        //Set the background
        mainView.setBackgroundDrawable(GComponent.GetBackgroundGradient());
        setContentView(mainView);


        if (ActivityManager.isUserAMonkey()) {
            Helper h = new Helper(this);
            h.CreateDummyData();
            currentProfileData = new Data(h.profilesHelper.getGuardians().get(0).getId(),
                    h.profilesHelper.getChildren().get(0).getId(),
                    this.getApplicationContext());
        }
        else {
            /* Get data from launcher */
            extras = getIntent().getExtras();

            if (extras != null) {
                currentProfileData = new Data(
                        extras.getInt("currentGuardianID"),
                        extras.getInt("currentChildID"),
                        this.getApplicationContext());
            } else {
                super.finish();
            }
        //Find the gButton in your View (needs to be disabled if it is a guest session)
        gButtonProfileSelect     = (GButtonProfileSelect) findViewById(R.id.ChangeProfile);

        // If the launcher is running it is not a guest session
        this.isGuestSession = !Data.isProcessRunning("dk.aau.cs.giraf.launcher", this);

        if (isGuestSession) {
            new GToast(this, super.getResources().getString(R.string.guest_toast), 100).show();
            // Disable button to switch profile as there are no other profile than Guest in standalone execution
            gButtonProfileSelect.setEnabled(false);
            // Empty Data constructor creates a guest profile
            currentProfileData = new ProfileData();

            this.downloadAllPictograms();
        } else {
            /* Get data from launcher */
            Bundle extras = getIntent().getExtras();

            // If GIRAF launcher is running use its Profile data
            currentProfileData = new ProfileData(
                    extras.getLong("currentGuardianID"),
                    extras.getLong("currentChildID"),
                    this.getApplicationContext());
        }

        //Call the method setup with a Profile guardian, no currentProfile (which means that the guardian is the current Profile) and the onCloseListener
        gButtonProfileSelect.setup(this.currentProfileData.guardianProfile, null, new GButtonProfileSelect.onCloseListener() {
            @Override
            public void onClose(Profile guardianProfile, Profile currentProfile) {
                //If the guardian is the selected profile create GToast displaying the name
                if (currentProfile == null) {
                    GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + guardianProfile.getName().toString(), 2);
                    onChangeProfile(guardianProfile, null);
                    w.show();
                }
                //If another current Profile is the selected profile create GToast displaying the name
                else {
                    GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + currentProfile.getName().toString(), 2);
                    onChangeProfile(guardianProfile, currentProfile);
                    w.show();
                }
            }
        });

		/* Not used anymore but maybe the performClick method can be called in some cases
        if(extras == null){
            this.gButtonProfileSelect.performClick();
        }
		*/

        // Find buttons
        GList saveConfigurationList = (GList) this.findViewById(R.id.savedConfig);
        GList stationList = (GList) this.findViewById(R.id.stationList);

        if (this.currentProfileData.childProfile != null) {
            this.configurationHandler = new ConfigurationList(this, this.currentProfileData.childProfile);
        } else {
            this.configurationHandler = new ConfigurationList(this, this.currentProfileData.guardianProfile);
        }

        Log.d("Train", "number of saved gameconfigurations: " + this.configurationHandler.getGameconfiguration().size());

        gameListAdapter = new GGameListAdapter(this, this.configurationHandler.getGameconfiguration());
        saveConfigurationList.setAdapter(gameListAdapter);
        saveConfigurationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.setGameConfiguration((GameConfiguration) parent.getAdapter().getItem(position));
            }
        });

        listOfStations = new StationList();

        stationListAdapter = new GStationListAdapter(this, listOfStations.stations);
        stationList.setAdapter(stationListAdapter);

        this.PreConfigure();
    }

    private void PreConfigure() {
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage(super.getResources().getString(R.string.loading));
        this.progressDialog.setCancelable(true);

        //Show progressDialog while loading activity. Set the color to white only one time
        this.progressDialog.show();
        ((TextView) this.progressDialog.findViewById(android.R.id.message)).setTextColor(android.graphics.Color.WHITE);


		this.gameIntent = new Intent(this, GameActivity.class);
        this.categoryIntent = new Intent(this, CategoryDialogActivity.class);
		this.saveIntent = new Intent(this, SaveDialogActivity.class);
		//this.pictoAdminIntent.setComponent(new ComponentName("dk.aau.cs.giraf.core.pictosearch", "dk.aau.cs.giraf.core.pictosearch.PictoAdminMain"));
        this.pictoAdminIntent = new Intent(this, PictoAdminMain.class);

        // Create intents that are used throughout the app
        this.CreateIntents();



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(super.getResources().getString(R.string.okay), null);
        this.errorDialog = alertDialogBuilder.create();

        this.progressDialog.dismiss(); //Hide progressDialog after creation is done
    }

    // These intents are used throughout the app
    private void CreateIntents() {
        this.gameIntent = new Intent(this, GameActivity.class);
        this.categoryIntent = new Intent(this, CategoryDialogActivity.class);
        this.saveIntent = new Intent(this, SaveDialogActivity.class);
        this.pictoAdminIntent = new Intent(this, PictoAdminMain.class);
    }

    private void downloadAllPictograms() {
        super.startActivity(new Intent(this, Data.class));

    }

    // Allows the user to change current profile. NOT possible in guest mode
    public void onChangeProfile(Profile guardianProfile, Profile currentProfile) {
        if(currentProfile == null){
            this.currentProfileData.guardianProfile = guardianProfile;
            this.configurationHandler.update(guardianProfile);
        }
        else{
            this.currentProfileData.childProfile = currentProfile;
            this.configurationHandler.update(currentProfile);
        }
        this.gameListAdapter.notifyDataSetChanged();
        this.stationListAdapter.notifyDataSetChanged();
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {
        private GameConfiguration gameConfiguration;

        public OnItemClickListener(GameConfiguration gameConfiguration) {
            this.gameConfiguration = gameConfiguration;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            MainActivity.this.setGameConfiguration(gameConfiguration);

        }
    }

    public void onClickAddStation(View view) {
        this.listOfStations.stations.add(new StationConfiguration());
        this.stationListAdapter.notifyDataSetChanged();
    }

    public void onClickSaveGame(View view) throws IOException {
        if (this.isValidConfiguration(view)) {
            this.saveIntent.putExtra(MainActivity.GAME_CONFIGURATIONS, this.configurationHandler.getGameconfiguration());
            if(this.currentProfileData.childProfile != null){
                this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, this.currentProfileData.childProfile.getName());
                this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, this.currentProfileData.childProfile.getId());
            }
            else {
                this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, this.currentProfileData.guardianProfile.getName());
                this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, this.currentProfileData.guardianProfile.getId());
            }

            super.startActivityForResult(this.saveIntent, MainActivity.RECEIVE_GAME_NAME);
        }
    }
    public void onClickCategory(View view) {
        super.startActivityForResult(this.categoryIntent, MainActivity.RECEIVE_GAME_NAME);
    }

    public void onClickStartGame(View view) {
        // TODO: ID should be implemented, instead of giving all games the id of '1337'
        if(this.isValidConfiguration(view)) {
            if(this.currentProfileData.childProfile != null){
                this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", 1337, this.currentProfileData.childProfile.getId(), distanceBetweenStations));
            }
            else {
                this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", 1337, this.currentProfileData.guardianProfile.getId(), distanceBetweenStations));
            }
            this.startActivity(this.gameIntent);
        }
    }
    public void onClickInfo(View view){
        GDialogAlert diag = new GDialogAlert(view.getContext(),
                R.drawable.ic_launcher,
                "Infomation",
                super.getResources().getString(R.string.InfoText),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Do nothing
                    }
                });
        diag.show();
    }

    private void showAlertMessage(String message, View view) {
        new GDialogAlert(view.getContext(),message, null).show();
    }

    private boolean isValidConfiguration(View view) {
        ArrayList<StationConfiguration> currentStation = this.listOfStations.getStations();
        EditText text = (EditText)findViewById(R.id.distanceForStations);
        if (text == null || text.getText().toString().equals(""))
        {
            this.showAlertMessage("Du skal skrive køretiden mellem stationer" +
                    " for at kunne starte og gemme spillet", view);
            currentStation = null; //Free memory
            return false;
        }
        try{
            distanceBetweenStations = Integer.parseInt(text.getText().toString());
        } catch (NumberFormatException e){
            text.getText().clear();
            return false;
        }

        if (distanceBetweenStations < MINIMUM_TIME || distanceBetweenStations > MAXIMUM_TIME) {
            this.showAlertMessage("Værdien skal mellem 15 og 300 sekunder.", view);
            return false;
        }

        distanceBetweenStations =(int)Math.ceil((Integer.parseInt(text.getText().toString()) * 350) - 1750);
        //There needs to be at least one station
        if(currentStation.size() < 1) {
            this.showAlertMessage(super.getResources().getString(R.string.station_error),view);
            currentStation = null; //Free memory
            return false;
        }
        //Check if there is only one station with only one pictogram, as it does not make sense to start such a game.
        else if(currentStation.size() == 1 && currentStation.get(0).getAcceptPictograms().size() == 1)
        {
            //Not a valid game, return false
            this.showAlertMessage(super.getResources().getString(R.string.only_one_station_and_piktogram_error), view);
            return false;
        }
        for (int i = 0; i < currentStation.size(); i++)
        {
            if (currentStation.get(i).isLoadingStation())
                continue;

            if(currentStation.get(i).getCategory() == -1)
            {
                this.showAlertMessage(super.getResources().getString(R.string.category_error),view);
                currentStation = null; //Free memory
                return false;
            }
            else if (currentStation.get(i).getAcceptPictograms().size() < 1) {
                this.showAlertMessage(super.getResources().getString(R.string.pictogram_error),view);
                currentStation = null; //Free memory
                return false;
            }
        }


        currentStation = null; //Free memory
        //If we have come this far, then the configuration is valid
        return true;
    }

    private GameConfiguration getGameConfiguration(String gameName, int gameID, long childID, int distanceBetweenStations) {

        GameConfiguration gameConfiguration = new GameConfiguration(gameName, gameID, childID, currentProfileData.guardianProfile.getId(), distanceBetweenStations); //TODO Set appropriate IDs
        gameConfiguration.setStations(this.listOfStations.getStations());
        return gameConfiguration;
    }

    public void setGameConfiguration(GameConfiguration gameConfiguration) {
        ArrayList<StationConfiguration> newReference = new ArrayList<StationConfiguration>();
        for (int i = 0; i < gameConfiguration.getStations().size(); i++) {
            newReference.add(new StationConfiguration(gameConfiguration.getStation(i)));
        }
        this.listOfStations.setStationConfigurations(newReference);
        this.stationListAdapter.notifyDataSetChanged();
        EditText text = (EditText)findViewById(R.id.distanceForStations);
        text.setText(Integer.toString(gameConfiguration.getDistanceBetweenStations()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.progressDialog.dismiss();

        //If we did not receive any data or the result was not OK, abort
        if(data == null || resultCode != RESULT_OK) {
            return;
        }

        long[] checkout;

        switch(requestCode) {
            case MainActivity.RECEIVE_SINGLE:
                checkout = data.getExtras().getLongArray("checkoutIds"); //Pictogram IDs

                if(checkout.length > 0) {
                    Log.d("dk.aau.cs.giraf.train", "Single: " + checkout[0]);
                    this.listOfStations.receivePictograms(checkout[0], this.selectedStation, this.selectedAcceptPictogram, this.isCategory);
                }

                break;
            case MainActivity.RECEIVE_MULTIPLE:
                checkout = data.getExtras().getLongArray("checkoutIds"); //Pictogram IDs

                if(checkout.length > 0) {
                    Log.d("dk.aau.cs.giraf.train", "Multiple: " + checkout[0]);
                    this.listOfStations.receivePictograms(checkout, this.selectedStation);
                }
                break;
            case MainActivity.RECEIVE_GAME_NAME:

                String gameName = data.getExtras().getString(SaveDialogActivity.GAME_NAME);
                EditText text = (EditText)findViewById(R.id.distanceForStations);
                int distanceBetweenStations = Integer.parseInt(text.getText().toString());
                GameConfiguration gameConfiguration;
                if(this.currentProfileData.childProfile != null){
                    gameConfiguration = getGameConfiguration(gameName, 1337, this.currentProfileData.childProfile.getId(),distanceBetweenStations);
                }
                else{
                    gameConfiguration = getGameConfiguration(gameName, 1337, this.currentProfileData.guardianProfile.getId(),distanceBetweenStations);
                }
                this.configurationHandler.addConfiguration(gameConfiguration);
                this.gameListAdapter.notifyDataSetChanged();

                try {
                    this.configurationHandler.saveAllConfigurations(SAVEFILE_PATH);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Kan ikke gemme", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        this.stationListAdapter.notifyDataSetChanged();

    }
    private int selectedStation = -1;
    long selectedAcceptPictogram = -1;
    private boolean isCategory = false;
    public void startPictoAdmin(int requestCode, int selectedStation, long selectedAcceptPictogram, boolean isCategory, View view){
        this.selectedAcceptPictogram = selectedAcceptPictogram;
        this.isCategory = isCategory;
        this.startPictoAdmin(requestCode, selectedStation, view);
    }

    public void startPictoAdmin(int requestCode, int selectedStation, View view) {
        this.selectedStation = selectedStation; // Add pictogram to station goes here

        /* This method was used when corelib did not manage pictosearch as Train had to make sure pictosearch was installed.
	    if(this.isCallable(this.pictoAdminIntent) == false) {
	        this.showAlertMessage(super.getResources().getString(R.string.picto_error), view );
	        return;
	    }
	    */

        this.progressDialog.show();

        //requestCode defines how many pictograms we want to receive
        switch(requestCode) {
            case MainActivity.RECEIVE_SINGLE:
                this.pictoAdminIntent.putExtra(IntentConstants.PURPOSE, IntentConstants.SINGLE);
                break;
            case MainActivity.RECEIVE_MULTIPLE:
                this.pictoAdminIntent.putExtra(IntentConstants.PURPOSE, IntentConstants.MULTI);
                break;
        }
        if(this.currentProfileData.childProfile != null){
            this.pictoAdminIntent.putExtra(IntentConstants.CURRENT_CHILD_ID, this.currentProfileData.childProfile.getId());
        }
        this.pictoAdminIntent.putExtra(IntentConstants.CURRENT_GUARDIAN_ID, this.currentProfileData.guardianProfile.getId());

        super.startActivityForResult(this.pictoAdminIntent, requestCode);
    }

    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
