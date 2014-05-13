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

import dk.aau.cs.giraf.gui.GComponent;
import dk.aau.cs.giraf.gui.GDialogAlert;
import dk.aau.cs.giraf.gui.GList;
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
    private Intent pictoAdminIntent = new Intent();

	//private GameLinearLayout gameLinearLayout;
	private CustomiseLinearLayout customiseLinearLayout;
	
	private ProgressDialog progressDialog;
	private AlertDialog errorDialog;
	private Data currentProfileData = null;
    private GGameListAdapter gameListAdapter;
    private ConfigurationList configurationHandler;

	public static final int ALLOWED_PICTOGRAMS = 12;
	public static final int ALLOWED_STATIONS   = ALLOWED_PICTOGRAMS;
     private static int minimumPixelDistance = 15*350 - 1750; // 15 seconds is so stations do not overlap, 11 can work but 15 people like round numbers

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

        GList testList = (GList)this.findViewById(R.id.TestList);
        this.configurationHandler = new ConfigurationList(this, this.currentProfileData.childProfile);
        gameListAdapter = new GGameListAdapter(this,this.configurationHandler.getGameconfiguration());
        testList.setAdapter(gameListAdapter);
        testList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.setGameConfiguration((GameConfiguration) parent.getAdapter().getItem(position));
            }
        });
        gameListAdapter.notifyDataSetChanged();

        //this.gameLinearLayout = ((GameLinearLayout) findViewById(R.id.gamelist));
		
		this.customiseLinearLayout = (CustomiseLinearLayout) super.findViewById(R.id.customiseLinearLayout);

        //this.gameLinearLayout.setSelectedChild(this.currentProfileData.childProfile);
        //this.gameLinearLayout.loadAllConfigurations();

		this.gameIntent = new Intent(this, GameActivity.class);
		this.saveIntent = new Intent(this, SaveDialogActivity.class);
		this.pictoAdminIntent.setComponent(new ComponentName("dk.aau.cs.giraf.pictosearch", "dk.aau.cs.giraf.pictosearch.PictoAdminMain"));
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(super.getResources().getString(R.string.okay), null);
        this.errorDialog = alertDialogBuilder.create();

        this.progressDialog.dismiss(); //Hide progressDialog after creation is done
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
	    this.customiseLinearLayout.addStation(new StationConfiguration());
	}
	
	public void onClickSaveGame(View view) throws IOException {
	    if (this.isValidConfiguration(view)) {
	    	this.saveIntent.putExtra(MainActivity.GAME_CONFIGURATIONS, this.configurationHandler.getGameconfiguration());
	    	
	    	this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_NAME, this.currentProfileData.childProfile.getName());
	    	this.saveIntent.putExtra(MainActivity.SELECTED_CHILD_ID, this.currentProfileData.childProfile.getId());
	    	
            super.startActivityForResult(this.saveIntent, MainActivity.RECEIVE_GAME_NAME);
        }
	}
	
	public void onClickStartGame(View view) {
	    if(this.isValidConfiguration(view)) {
            this.gameIntent.putExtra(MainActivity.GAME_CONFIGURATION, this.getGameConfiguration("the new game", 1337, this.currentProfileData.childProfile.getId(), distanceBetweenStations));
            this.startActivity(this.gameIntent);
        }
	}
	
	private void showAlertMessage(String message, View view) {
        new GDialogAlert(view.getContext(),message, null).show();
	}
	
	private boolean isValidConfiguration(View view) {
	    ArrayList<StationConfiguration> currentStation = this.customiseLinearLayout.getStations();
        EditText text = (EditText)findViewById(R.id.distanceForStations);
        if (text == null || text.getText().toString().equals(""))
        {
            this.showAlertMessage("Du skal skrive køretiden mellem stationer for at kunne starte og gemme spillet",view);
            currentStation = null; //Free memory
            return false;
        }

        distanceBetweenStations =(int)Math.ceil((Integer.parseInt(text.getText().toString()) * 350) - 1750);
	    //There needs to be at least one station
	    if(currentStation.size() < 1) {
	        this.showAlertMessage(super.getResources().getString(R.string.station_error),view);
	        currentStation = null; //Free memory
            return false;
        }
        if (distanceBetweenStations < minimumPixelDistance){
            this.showAlertMessage("Værdien skal være 15 eller derover", view);
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
	
	private GameConfiguration getGameConfiguration(String gameName, int gameID, long childID, int distanceBetweenStations) {

	    GameConfiguration gameConfiguration = new GameConfiguration(gameName, gameID, childID, currentProfileData.guardianProfile.getId(), distanceBetweenStations); //TODO Set appropriate IDs
	    gameConfiguration.setStations(this.customiseLinearLayout.getStations());
	    return gameConfiguration;
	}
	
	public void setGameConfiguration(GameConfiguration gameConfiguration) {
	    ArrayList<StationConfiguration> newReference = new ArrayList<StationConfiguration>();
	    for (int i = 0; i < gameConfiguration.getStations().size(); i++) {
	        newReference.add(new StationConfiguration(gameConfiguration.getStation(i)));
	    }
	    this.customiseLinearLayout.setStationConfigurations(newReference);
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
        	GameConfiguration gameConfiguration = getGameConfiguration(gameName, 1337, this.currentProfileData.guardianProfile.getId(),distanceBetweenStations); // TO DO
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