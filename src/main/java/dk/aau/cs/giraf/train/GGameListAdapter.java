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

import java.util.ArrayList;

import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Pictogram;
import dk.aau.cs.giraf.gui.GTextView;

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

        //Needs a helper to get the images
        Helper helper = new Helper(activity.getApplicationContext());
        Long category =  data.get(position).getStation(0).getCategory();


        Pictogram pictogram = helper.pictogramHelper.getById(category);
        Bitmap bitmap = helper.pictogramHelper.getImage(pictogram);
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
            ((MainActivity)GGameListAdapter.this.activity).configurationHandler.SaveSettings();
            GGameListAdapter.this.notifyDataSetChanged();
        }
    }
}
