package dk.aau.cs.giraf.train;
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
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.core.data.Constants;
import dk.aau.cs.giraf.core.data.Data;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GList;
import dk.aau.cs.giraf.gui.GToast;
import dk.aau.cs.giraf.gui.GirafButton;
import dk.aau.cs.giraf.gui.GirafNotifyDialog;
import dk.aau.cs.giraf.gui.GirafProfileSelectorDialog;
import dk.aau.cs.giraf.pictosearch.PictoAdminMain;
import dk.aau.cs.giraf.train.opengl.GameActivity;
import dk.aau.cs.giraf.utilities.IntentConstants;

public class MainActivity extends GirafActivity implements GirafProfileSelectorDialog.OnSingleProfileSelectedListener, GirafNotifyDialog.Notification{
    public static final String SAVEFILE_PATH = "game_configurations.txt";
    public static final String GAME_CONFIGURATION = "GameConfiguration";
    public static final String GAME_CONFIGURATIONS = "GameConfigurations";
    public static final String SELECTED_CHILD_ID = "selectedChildId";
    public static final String SELECTED_CHILD_NAME = "selectedChildName";
    private static final int CHANGE_USER_SELECTOR_DIALOG = 100;
    public static final int RECEIVE_SINGLE = 0;
    public static final int RECEIVE_MULTIPLE = 1;
    public static final int RECEIVE_GAME_NAME = 2;
    public static final int APPLICATIONBACKGROUND = 0xFFFFBB55;
    // Identifier for callback
    private static final int NOTIFY_DIALOG_ID = 1;
    // Fragment tag (android specific)
    private static final String NOTIFY_DIALOG_TAG = "DIALOG_TAG";
    //Identifier for fragment
    private static final int CHANGE_USER_DIALOG = 113;
    // Profiles of which the categories will be loaded from
    private int distanceBetweenStations;
    private boolean isGuestSession;
    private Intent gameIntent;
    private Intent saveIntent;
    private Intent categoryIntent;
    private Intent pictoAdminIntent = new Intent();
	private ProgressDialog progressDialog;
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
    private GirafButton changeUserButton;
    private GirafButton addStationButton;
    private GirafButton saveGameButton;
    private GirafButton startGameButton;
    private SeekBar distanceSelector;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GComponent.SetBaseColor(APPLICATIONBACKGROUND);
        LayoutInflater li = LayoutInflater.from(this);
        View mainView = li.inflate(R.layout.activity_main, null);
        Intent intent = getIntent();

        //Set the background
        mainView.setBackgroundDrawable(GComponent.GetBackgroundGradient());
        setContentView(mainView);
        createTopBarButtons();
        createDistanceSelector();

        if (ActivityManager.isUserAMonkey()) {
            Helper h = new Helper(this);
            h.CreateDummyData();
            currentProfileData = new ProfileData(h.profilesHelper.getGuardians().get(0).getId(),

            h.profilesHelper.getChildren().get(0).getId(),
            this.getApplicationContext());
        }
        else {
            //Find the gButton in your View (needs to be disabled if it is a guest session)

            // If the launcher is running it is not a guest session
            this.isGuestSession = !Data.isProcessRunning("dk.aau.cs.giraf.launcher", this);

            if (isGuestSession) {
                //new GToast(this, super.getResources().getString(R.string.guest_toast), 100).show();
                // Disable button to switch profile as there are no other profile than Guest in standalone execution
                changeUserButton.setVisibility(View.INVISIBLE);
                // Empty Data constructor creates a guest profile
                this.downloadAllPictograms();
                //Get guest guardian profile
                this.currentProfileData = new ProfileData(Constants.guestGuardianID, -1, this.getApplicationContext());


            } else {
            /* Get data from launcher */
                Bundle extras = getIntent().getExtras();
                if(extras == null) {
                    Log.d("Train", "Extras are null, shutdown application");
                    currentProfileData = new ProfileData(37, -1, this.getApplicationContext());
                }
                else{
                    long guardianID = extras.getLong("currentGuardianID");
                    long childID = extras.getLong("currentChildID");
                    Log.d("Train", "Extra values: " + guardianID + " - " + childID);
                    // If GIRAF launcher is running use its Profile data
                    currentProfileData = new ProfileData(guardianID, childID, this.getApplicationContext());
                }
            }
        }


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
        //Load predefined games
        //this.configurationHandler.loadPredefinedGames();
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
    /**
     * Creates and initilizes buttons for the actionbar,
     * furthermore it adds onClickListeners for the buttons.
     */
    private void createTopBarButtons(){
        createChangeUserButton();
        createAddStationButton();
        createSaveGameButton();
        createStartGameButton();


    }
    private void createStartGameButton() {
        startGameButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_play));
        startGameButton.setId(R.id.startGameFromProfileButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidConfiguration(view)) {
                    if(currentProfileData.childProfile != null){
                        gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, getGameConfiguration("the new game", currentProfileData.childProfile.getId()));
                    }
                    else {
                        gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, getGameConfiguration("the new game",  currentProfileData.guardianProfile.getId()));
                    }
                    startActivity(gameIntent);
                }
            }
        });
        addGirafButtonToActionBar(startGameButton, GirafActivity.RIGHT);
    }

    private void createAddStationButton() {
        addStationButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_add));
        addStationButton.setId(R.id.addStationButton);
        addStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listOfStations.stations.add(new StationConfiguration());
                stationListAdapter.notifyDataSetChanged();
            }
        });
        addGirafButtonToActionBar(addStationButton, GirafActivity.RIGHT);
    }

    private void createChangeUserButton() {
        changeUserButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_give_tablet));
        changeUserButton.setId(R.id.change_user);
        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//todo fix lige 37
                GirafProfileSelectorDialog changeUser = GirafProfileSelectorDialog.newInstance(MainActivity.this, currentProfileData.guardianProfile.getId(), false, false, "Vælg den borger du vil skifte til.", CHANGE_USER_SELECTOR_DIALOG);
                changeUser.show(getSupportFragmentManager(), "" + CHANGE_USER_SELECTOR_DIALOG);
            }
        });
        addGirafButtonToActionBar(changeUserButton, GirafActivity.RIGHT);
    }

    private void createSaveGameButton() {
        saveGameButton = new GirafButton(this, getResources().getDrawable(R.drawable.icon_save));
        saveGameButton.setId(R.id.saveGameButton);
        saveGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidConfiguration(view)) {
                    saveIntent.putExtra(MainActivity.GAME_CONFIGURATIONS, configurationHandler.getGameconfiguration());
                    if(currentProfileData.childProfile != null){
                        saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, currentProfileData.childProfile.getName());
                        saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, currentProfileData.childProfile.getId());
                    }
                    else {
                        saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, currentProfileData.guardianProfile.getName());
                        saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, currentProfileData.guardianProfile.getId());
                    }
                    startActivityForResult(saveIntent, MainActivity.RECEIVE_GAME_NAME);
                }
            }
        });
        addGirafButtonToActionBar(saveGameButton, GirafActivity.RIGHT);
    }

    @Override
    public void onProfileSelected(final int i, final Profile profile) {

        if (i == CHANGE_USER_SELECTOR_DIALOG) {

            // Update the profile
            currentProfileData.childProfile = profile;

            if (profile == null) {
                GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + currentProfileData.guardianProfile.getName().toString(), 2);
                onChangeProfile(currentProfileData.guardianProfile, null);
                w.show();
            }
            //If another current Profile is the selected profile create GToast displaying the name
            else {
                GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + currentProfileData.childProfile.getName().toString(), 2);
                onChangeProfile(currentProfileData.guardianProfile, currentProfileData.childProfile);
                w.show();
            }
        }

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


        this.stationListAdapter.notifyDataSetChanged();
        this.gameListAdapter.notifyDataSetChanged();
    }

    @Override
    public void noticeDialog(int i) {

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

    public void onClickCategory(View view) {
        super.startActivityForResult(this.categoryIntent, MainActivity.RECEIVE_GAME_NAME);
    }

    //todo lassse
    public void onClickStartGame(View view) {
        // TODO: ID should be implemented, instead of giving all games the id of '1337'
        if(this.isValidConfiguration(view)) {
            if(this.currentProfileData.childProfile != null){
                this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", this.currentProfileData.childProfile.getId()));
            }
            else {
                this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", this.currentProfileData.guardianProfile.getId()));
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
        // Creates an instance of the dialog
        GirafNotifyDialog notifyDialog = GirafNotifyDialog.newInstance("Hovsa", message, NOTIFY_DIALOG_ID);
        // Shows the dialog
        notifyDialog.show(getSupportFragmentManager(), NOTIFY_DIALOG_TAG);
    }

    private boolean isValidConfiguration(View view) {
        ArrayList<StationConfiguration> currentStation = this.listOfStations.getStations();

        if (distanceSelector.getProgress() < MINIMUM_TIME || distanceSelector.getProgress() > MAXIMUM_TIME) {
            this.showAlertMessage("Værdien skal mellem 15 og 300 sekunder.", view);
            return false;
        }
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
    //WTF?? //Lasse. todo
    private int getDistanceBetweenStations(){
        return ((distanceSelector.getProgress() * 350 - 1750));
    }

    private GameConfiguration getGameConfiguration(String gameName, long childID) {
        GameConfiguration gameConfiguration = new GameConfiguration(gameName, childID, currentProfileData.guardianProfile.getId(), getDistanceBetweenStations()); //TODO Set appropriate IDs
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
                GameConfiguration gameConfiguration;
                if(this.currentProfileData.childProfile != null){
                    gameConfiguration = getGameConfiguration(gameName, this.currentProfileData.childProfile.getId());
                }
                else{
                    gameConfiguration = getGameConfiguration(gameName, this.currentProfileData.guardianProfile.getId());
                }
                this.configurationHandler.addConfiguration(gameConfiguration);
                this.gameListAdapter.notifyDataSetChanged();
                this.configurationHandler.SaveSettings();
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
    private void createDistanceSelector(){
        distanceSelector = (SeekBar) findViewById(R.id.timeSlider);
        distanceSelector.setProgress(MINIMUM_TIME);
        distanceSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GToast w;
                distanceSelector.setProgress(distanceSelector.getProgress() < 15 ? MINIMUM_TIME : distanceSelector.getProgress());
                w = new GToast(getApplicationContext(), "Tiden mellem stationerne er nu: " + String.valueOf(distanceSelector.getProgress()) + " sekunder", 4);
                w.show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
    }
}
