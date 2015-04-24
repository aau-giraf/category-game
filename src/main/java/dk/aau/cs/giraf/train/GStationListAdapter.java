package dk.aau.cs.giraf.train;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import dk.aau.cs.giraf.gui.GButtonProfileSelect;
import dk.aau.cs.giraf.gui.GHorizontalScrollViewSnapper;
import dk.aau.cs.giraf.dblib.models.Pictogram;

public class GStationListAdapter extends BaseAdapter{
    private ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();
    private Activity parent = null;
    private static LayoutInflater inflater = null;

    public GStationListAdapter(Activity a,
                               ArrayList<StationConfiguration> s){
        parent = a;
        stations = s;
        inflater = (LayoutInflater)parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public Object getItem(int position) {
        return stations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = inflater.inflate(R.layout.station_list_item, null);
        StationConfiguration station = this.stations.get(position);

        PictogramButton categoryPictogramButton = (PictogramButton) v.findViewById(R.id.list_category);
        categoryPictogramButton.bindStationAsCategory(station,position);

        //The order og image button and associated pictograms layout statements, are very important here
        ImageButton addPictogramsButton = (ImageButton) v.findViewById(R.id.addPictogramButton);

        HorizontalScrollView gScroller = (HorizontalScrollView) v.findViewById(R.id.scrollview);
        LinearLayout scrollerL = (LinearLayout) gScroller.findViewById(R.id.scrollviewlayout);
        int number = 0;
        for (int i : station.getAcceptPictograms()){
            PictogramButton temp = new PictogramButton(parent.getContext(),position, number);
            temp.setPictogram(i);
            temp.setRemovable(true);
            number++;
            scrollerL.addView(temp);
        }

        if (station.getAcceptPictograms().size() < 6){
            addPictogramsButton.setVisibility(View.VISIBLE);
            addPictogramsButton.setOnClickListener(new AddClickListner(position));
        }
        else{
            addPictogramsButton.setVisibility(View.INVISIBLE);
        }

        ImageView deleteButton = (ImageView) v.findViewById(R.id.deleteRowButton);

        deleteButton.setOnClickListener(new RemoveClickListener(this.stations.get(position)));

        return v;
    }

    private final class AddClickListner implements View.OnClickListener {
        private int station;

        public AddClickListner(int position){
            station = position;
        }

        @Override
        public void onClick(View v) {
            ((MainActivity) GStationListAdapter.this.parent).startPictoAdmin(MainActivity.RECEIVE_MULTIPLE, station, v);
        }
    }

    private final class RemoveClickListener implements View.OnClickListener {

        private StationConfiguration station;

        public RemoveClickListener(StationConfiguration station) {
            this.station = station;
        }

        @Override
        public void onClick(View view) {
            ((MainActivity)GStationListAdapter.this.parent).listOfStations.removeStation(station);
            GStationListAdapter.this.notifyDataSetChanged();
        }
    }
}
