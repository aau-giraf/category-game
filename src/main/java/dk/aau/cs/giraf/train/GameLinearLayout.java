package dk.aau.cs.giraf.train;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import dk.aau.cs.giraf.dblib.models.Profile;
import dk.aau.cs.giraf.pictogram.PictoFactory;
//Bliver ikke brugt længere!
public class GameLinearLayout extends LinearLayout {

    private ArrayList<GameConfiguration> gameConfigurations = new ArrayList<GameConfiguration>();
    private ArrayList<GameConfiguration> visibleGameConfigurations = new ArrayList<GameConfiguration>();
    private AlertDialog deleteDialog;
    private int deleteIndex;
    private Profile selectedChild;

    public GameLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.deleteDialog = this.createAlertDialog();
    }

    public void setSelectedChild(Profile selectedChild) {
        this.selectedChild = selectedChild;
        this.makeAllViews();
    }

    private AlertDialog createAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext());
        //myAlertDialog.setTitle("Title");
        alertDialogBuilder.setMessage(R.string.delete_dialog);
        alertDialogBuilder.setPositiveButton(super.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //'Ja' button is clicked
                /*GameLinearLayout.this.removeVisibleGameConfiguration(GameLinearLayout.this.deleteIndex);
                try {
                    ((MainActivity) GameLinearLayout.this.getContext()).saveAllConfigurations(GameLinearLayout.this.getGameConfigurations());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(GameLinearLayout.this.getContext(), "Kan ikke gemme", Toast.LENGTH_SHORT).show();
                }*/
            }
        });
        alertDialogBuilder.setNegativeButton(super.getResources().getString(R.string.cancel), null);
        return alertDialogBuilder.create();
    }



    public ArrayList<GameConfiguration> getGameConfigurations() {
        return this.gameConfigurations;
    }

    public void addGameConfiguration(GameConfiguration gameConfiguration) {
        //Add to list of all configurations
        if (!this.gameConfigurations.contains(gameConfiguration)){
            this.gameConfigurations.add(gameConfiguration);

            if(this.selectedChild != null && gameConfiguration.getChildId() == this.selectedChild.getId()) {
                //If it belongs to current Child, make the view.
                this.makeView(gameConfiguration);
            }
        }
    }

    private void makeAllViews() {
        super.removeAllViews();
        this.visibleGameConfigurations.clear();

        if (selectedChild == null) {
            return;
        }

        for (GameConfiguration gameConfiguration : this.gameConfigurations) {
            if(gameConfiguration.getChildId() != this.selectedChild.getId()) {
                continue;
            }

            this.makeView(gameConfiguration);
        }
    }

    private void makeView(GameConfiguration gameConfiguration) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gameListItem = layoutInflater.inflate(R.layout.game_list_item, null); // Use same as profile

        TextView gameNameTextView = (TextView) gameListItem.findViewById(R.id.profileName);
        gameNameTextView.setText(gameConfiguration.getGameName());

        ImageView profilePictureImageView = (ImageView) gameListItem.findViewById(R.id.profilePic);
        profilePictureImageView.setImageResource(R.drawable.default_profile);

        @SuppressWarnings("static-access")
        Bitmap bitmap = PictoFactory.INSTANCE.getPictogram(super.getContext(),gameConfiguration.getStation(0).getCategory()).getImageData();
        profilePictureImageView.setImageBitmap(bitmap);

        gameListItem.setOnClickListener(new OnItemClickListener(gameConfiguration));
        gameListItem.setOnLongClickListener(new OnItemLongClickListener(gameConfiguration));

        this.visibleGameConfigurations.add(gameConfiguration); //Add to list of visible configurations
        super.addView(gameListItem);
    }

    public void removeVisibleGameConfiguration(GameConfiguration gameConfiguration) {
        this.removeVisibleGameConfiguration(this.visibleGameConfigurations.indexOf(gameConfiguration));
    }

    public void removeVisibleGameConfiguration(int index) {
        this.removeViewAt(index);
        this.gameConfigurations.remove(this.visibleGameConfigurations.remove(index)); //Remove from both lists
    }

    public void loadAllConfigurations() {
        FileInputStream fis = null;
        StringWriter sWriter = new StringWriter(1024);

        try {
            fis = getContext().openFileInput(MainActivity.SAVEFILE_PATH);

            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and append to string
                sWriter.append((char) content);
            }
        } catch(FileNotFoundException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sWriter.close();
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        this.splitConfigurations(sWriter.toString());
    }

    private void splitConfigurations(String data) {
        String[] configurations = data.split("\n");

        // For each configuration
        for (int i = 0; i < configurations.length; i++) {

            String[] parts = configurations[i].split(";");
            String[] game = parts[0].split(",");

            int gameID = Integer.parseInt(game[0]);
            long guardianID =  Long.parseLong(game[1]);
            long childID =  Long.parseLong(game[2]);
            String gameName = game[3];
            int TempdistanceBetweenStations = Integer.parseInt(game[4]);
            ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();

            // For each station
            for (int k = 1; k < parts.length; k++) {
                StationConfiguration station = new StationConfiguration();
                String[] stationParts = parts[k].split(",");

                station.setCategory((Integer.parseInt(stationParts[0])));

                // For each accept pictogram of station
                for (int n = 1; n < stationParts.length; n++) {
                    station.addAcceptPictogram(Integer.parseInt(stationParts[n]));
                }
                stations.add(station);
            }

            GameConfiguration gameConf = new GameConfiguration(gameName, gameID, childID, guardianID, TempdistanceBetweenStations);
            gameConf.setStations(stations);

            this.addGameConfiguration(gameConf);
        }
    }

    private class OnItemClickListener implements OnClickListener {
        private GameConfiguration gameConfiguration;

        public OnItemClickListener(GameConfiguration gameConfiguration) {
            this.gameConfiguration = gameConfiguration;
        }

        @Override
        public void onClick(View v) {
            ((MainActivity) GameLinearLayout.this.getContext()).setGameConfiguration(gameConfiguration);
        }
    }

    private class OnItemLongClickListener implements OnLongClickListener {

        private GameConfiguration gameConfiguration;

        public OnItemLongClickListener(GameConfiguration gameConfiguration) {
            this.gameConfiguration = gameConfiguration;
        }

        @Override
        public boolean onLongClick(View v) {
            GameLinearLayout.this.deleteIndex = GameLinearLayout.this.visibleGameConfigurations.indexOf(gameConfiguration);
            GameLinearLayout.this.deleteDialog.show();
            return true;
        }

    }
}
