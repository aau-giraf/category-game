package dk.aau.cs.giraf.train;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

import dk.aau.cs.giraf.gui.GTextView;

import dk.aau.cs.giraf.pictogram.PictoFactory;

public class GGameListAdapter extends BaseAdapter{

    private Activity activity;
    private ArrayList<GameConfiguration> data;
    private static LayoutInflater inflater = null;

    public GGameListAdapter(Activity a, ArrayList<GameConfiguration> d){
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if(convertView == null){
            vi = inflater.inflate(R.layout.game_list_item,null);
        }

        GTextView categoryView = (GTextView) vi.findViewById(R.id.configurationName);
        categoryView.setText(data.get(position).getGameName());

        ImageView categoryPic = (ImageView) vi.findViewById(R.id.categoryPic);
        categoryPic.setImageResource(R.drawable.default_profile);

        Bitmap bitmap = PictoFactory.INSTANCE.getPictogram(activity.getApplicationContext(),data.get(position).getStation(0).getCategory()).getImageData();
        categoryPic.setImageBitmap(bitmap);

        ImageButton deleteButton = (ImageButton) vi.findViewById(R.id.deleteConfigButton);
        deleteButton.setOnClickListener(new DeleteConfigClickListener(this.data.get(position)));
        deleteButton.setFocusable(false);

        return vi;
    }

    private final class DeleteConfigClickListener implements View.OnClickListener {

        private GameConfiguration configuration;

        public DeleteConfigClickListener(GameConfiguration g) {
            this.configuration = g;
        }

        @Override
        public void onClick(View view) {
            ((MainActivity)GGameListAdapter.this.activity).configurationHandler.removeConfiguration(configuration);
            try {
                ((MainActivity)GGameListAdapter.this.activity).configurationHandler.saveAllConfigurations(MainActivity.SAVEFILE_PATH);
            } catch (IOException e) {
                e.printStackTrace();
                //TODO:insert a alertbox
            }
            GGameListAdapter.this.notifyDataSetChanged();
        }
    }
}
