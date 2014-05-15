package dk.aau.cs.giraf.train;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GList;
import dk.aau.cs.giraf.gui.GToast;
import dk.aau.cs.giraf.oasis.lib.models.Profile;
import dk.aau.cs.giraf.train.opengl.GameActivity;

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
    
    private Intent gameIntent;
    private Intent saveIntent;
    private Intent categoryIntent;
    private Intent pictoAdminIntent = new Intent();
	
	private ProgressDialog progressDialog;
    private GButtonProfileSelect gButtonProfileSelect;
	private AlertDialog errorDialog;
	private Data currentProfileData = null;
    private GGameListAdapter gameListAdapter;
    private ConfigurationList configurationHandler;

	public static final int ALLOWED_PICTOGRAMS = 12;
	public static final int ALLOWED_STATIONS   = ALLOWED_PICTOGRAMS;

    private final int MINIMUM_TIME = 15;
    private final int MAXIMUM_TIME = 60;

    public StationList listOfStations = null;
    private GStationListAdapter stationListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        GComponent.SetBaseColor(APPLICATIONBACKGROUND);
        LayoutInflater li = LayoutInflater.from(this);
        View mainView = li.inflate(R.layout.activity_main,null);

        //Set the background
        mainView.setBackgroundColor(APPLICATIONBACKGROUND);
        setContentView(mainView);

        /* Get data from launcher */
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            currentProfileData = new Data(
                    extras.getInt("currentGuardianID"),
                    extras.getInt("currentChildID"),
                    this.getApplicationContext());
        } else {
            //TODO: Overvej en exception istedet
            currentProfileData = new Data(
                    1,
                    11,
                    this.getApplicationContext());
        }

		this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage(super.getResources().getString(R.string.loading));
        this.progressDialog.setCancelable(true);
        
        //Show progressDialog while loading activity. Set the color to white only one time
        this.progressDialog.show();
        ((TextView) this.progressDialog.findViewById(android.R.id.message)).setTextColor(android.graphics.Color.WHITE);

        GList saveConfigurationList = (GList)this.findViewById(R.id.savedConfig);

        this.configurationHandler = new ConfigurationList(this, this.currentProfileData.childProfile);
        gameListAdapter = new GGameListAdapter(this,this.configurationHandler.getGameconfiguration());
        saveConfigurationList.setAdapter(gameListAdapter);
        saveConfigurationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.setGameConfiguration((GameConfiguration) parent.getAdapter().getItem(position));
            }
        });
        gameListAdapter.notifyDataSetChanged();

        listOfStations = new StationList();

        GList stationList = (GList)this.findViewById(R.id.stationList);
        stationListAdapter = new GStationListAdapter(this, listOfStations.stations);
        stationList.setAdapter(stationListAdapter);

		this.gameIntent = new Intent(this, GameActivity.class);
        this.categoryIntent = new Intent(this, CategoryDialogActivity.class);
		this.saveIntent = new Intent(this, SaveDialogActivity.class);
		this.pictoAdminIntent.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(super.getResources().getString(R.string.okay), null);
        this.errorDialog = alertDialogBuilder.create();

        this.progressDialog.dismiss(); //Hide progressDialog after creation is done

        //Find the GButton in your View
        gButtonProfileSelect = (GButtonProfileSelect) findViewById(R.id.ChanceProfile);
        //Call the method setup with a Profile guardian, no currentProfile (which means that the guardian is the current Profile) and the onCloseListener
        gButtonProfileSelect.setup(this.currentProfileData.guardianProfile, this.currentProfileData.childProfile, new GButtonProfileSelect.onCloseListener() {
            @Override
            public void onClose(Profile guardianProfile, Profile currentProfile) {
                //If the guardian is the selected profile create GToast displaying the name
                if(currentProfile == null){
                    GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + guardianProfile.getName().toString(), 2);
                    onChangeProfile(guardianProfile, null);
                    w.show();
                }
                //If another current Profile is the selected profile create GToast displaying the name
                else{
                    GToast w = new GToast(getApplicationContext(), "Den valgte profil er " + currentProfile.getName().toString(), 2);
                    onChangeProfile(guardianProfile, currentProfile);
                    w.show();
                }
            }
        });
	}

    public void onChangeProfile(Profile guardianProfile, Profile currentProfile) {
        if(currentProfile == null){
            this.currentProfileData.guardianProfile = guardianProfile;
            this.configurationHandler.update(guardianProfile);
            this.gameListAdapter.notifyDataSetChanged();
        }
        else{
            this.currentProfileData.childProfile = currentProfile;
            this.configurationHandler.update(currentProfile);
            this.gameListAdapter.notifyDataSetChanged();
        }

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
	    	
	    	this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, this.currentProfileData.childProfile.getName());
	    	this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, this.currentProfileData.childProfile.getId());
	    	
            super.startActivityForResult(this.saveIntent, MainActivity.RECEIVE_GAME_NAME);
        }
	}
    public void onClickCategory(View view) {
        super.startActivityForResult(this.categoryIntent, MainActivity.RECEIVE_GAME_NAME);
    }
	
	public void onClickStartGame(View view) {
	    if(this.isValidConfiguration(view)) {
            this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", 1337, this.currentProfileData.childProfile.getId(), distanceBetweenStations));
            this.startActivity(this.gameIntent);
        }
	}
    public void onClickInfo(View view){
        GDialogAlert diag = new GDialogAlert(view.getContext(),
                R.drawable.ic_launcher,
                "Infomation",
                "hej \n hej hej",
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
            this.showAlertMessage("Du skal skrive køretiden mellem stationer for at kunne starte og gemme spillet",view);
            currentStation = null; //Free memory
            return false;
        }

        distanceBetweenStations = Integer.parseInt(text.getText().toString());

        if (distanceBetweenStations < MINIMUM_TIME || distanceBetweenStations > MAXIMUM_TIME){
            this.showAlertMessage("Værdien skal mellem 15 og 60 sekunder.", view);
            return false;
        }

        distanceBetweenStations =(int)Math.ceil((Integer.parseInt(text.getText().toString()) * 350) - 1750);
	    //There needs to be at least one station
	    if(currentStation.size() < 1) {
	        this.showAlertMessage(super.getResources().getString(R.string.station_error),view);
	        currentStation = null; //Free memory
            return false;
        }
	    for (int i = 0; i < currentStation.size(); i++)
		{
			if (currentStation.get(i).isLoadingStation())
				continue;
				
	        if(currentStation.get(i).getCategory() == -1)
			{
                new GDialogAlert(view.getContext(),super.getResources().getString(R.string.category_error), null).show();
                //this.showAlertMessage(super.getResources().getString(R.string.category_error));
                currentStation = null; //Free memory
                return false;
            }
			else if (currentStation.get(i).getAcceptPictograms().size() < 1) {
                new GDialogAlert(view.getContext(),super.getResources().getString(R.string.pictogram_error), null).show();
	            //this.showAlertMessage(super.getResources().getString(R.string.pictogram_error));
	            currentStation = null; //Free memory
	            return false;
	        }
	    }
	    currentStation = null; //Free memory
	    //If we have come this far, then the configuration is valid
	    return true;
	}
	
	private GameConfiguration getGameConfiguration(String gameName, int gameID, int childID, int distanceBetweenStations) {

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
        text.setText(Integer.toString((gameConfiguration.getDistanceBetweenStations() + 1750)/350));
	}


	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.progressDialog.dismiss();
        
        //If we did not receive any data or the result was not OK, abort
        if(data == null || resultCode != RESULT_OK) {
            return;
        }
        
        int[] checkout;
        
        switch(requestCode) {
        case MainActivity.RECEIVE_SINGLE:
        	checkout = data.getExtras().getIntArray("checkoutIds"); //Pictogram IDs
            
            if(checkout.length > 0) {
                this.pictogramReceiver.receivePictograms(checkout, requestCode);
            }
        	break;
        case MainActivity.RECEIVE_MULTIPLE:
        	checkout = data.getExtras().getIntArray("checkoutIds"); //Pictogram IDs
            
            if(checkout.length > 0) {
                this.pictogramReceiver.receivePictograms(checkout, requestCode);
            }
        	break;
        case MainActivity.RECEIVE_GAME_NAME:

        	String gameName = data.getExtras().getString(SaveDialogActivity.GAME_NAME);
            EditText text = (EditText)findViewById(R.id.distanceForStations);
            int distanceBetweenStations = Integer.parseInt(text.getText().toString());
            if (distanceBetweenStations <= 0 ){
                distanceBetweenStations = 12000;
            }
            else if (distanceBetweenStations < 20)
            {
                distanceBetweenStations = distanceBetweenStations * 100;
            }
        	GameConfiguration gameConfiguration = getGameConfiguration(gameName, 1337, this.currentProfileData.childProfile.getId(),distanceBetweenStations); // TO DO
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
    }
	
	private PictogramReceiver pictogramReceiver;
	
	public void startPictoAdmin(int requestCode, PictogramReceiver pictogramRequester, View view) {
	    if(this.isCallable(this.pictoAdminIntent) == false) {
	        this.showAlertMessage(super.getResources().getString(R.string.picto_error), view );
	        return;
	    }
	    this.progressDialog.show();
	    
	    this.pictogramReceiver = pictogramRequester;
	    
	    //requestCode defines how many pictograms we want to receive
	    switch(requestCode) {
	    case MainActivity.RECEIVE_SINGLE:
	        this.pictoAdminIntent.putExtra("purpose", "single");
	        break;
	    case MainActivity.RECEIVE_MULTIPLE:
	        this.pictoAdminIntent.putExtra("purpose", "multi");
	        break;
	    }
        
        this.pictoAdminIntent.putExtra("currentChildID", this.currentProfileData.childProfile.getId());
        this.pictoAdminIntent.putExtra("currentGuardianID", this.currentProfileData.guardianProfile.getId());

        super.startActivityForResult(this.pictoAdminIntent, requestCode);
	}
	
	private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
	}
}