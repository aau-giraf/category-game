package dk.aau.cs.giraf.train;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import dk.aau.cs.giraf.gui.GLayout;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;

/**
 * A layout containing a pictogram. Click the button to choose a pictogram.
 * @author Jesper Riemer Andersen
 * @see Pictogram
 */
public class PictogramButton extends LinearLayout {
    
    private FrameLayout pictogramContainer;
    private int pictogramId = -1;
    private ImageButton removeButton;
    private StationConfiguration station = null;
    private boolean isCategory = false;
    private int selectedStation;
    private int number;
    
    private void setup() {
        LayoutParams layoutParams = new LayoutParams(75, 75);
        super.setLayoutParams(layoutParams);
        super.setBackgroundResource(R.drawable.shape_white);
        
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pictogramLayout = layoutInflater.inflate(R.layout.pictogram_layout, null);
        super.addView(pictogramLayout);
        
        this.pictogramContainer = (FrameLayout) pictogramLayout.findViewById(R.id.pictogramContainer);
        
        this.removeButton = (ImageButton) pictogramLayout.findViewById(R.id.removeButton);
        this.removeButton.setVisibility(View.INVISIBLE);
        this.removeButton.setOnClickListener(new RemoveClickListener());
        
        super.setOnClickListener(new PictogramClickListener());
    }

    public PictogramButton(Context context, int postion, int number){
        super(context);
        this.selectedStation = postion;
        this.number = number;
        this.setup();
    }
    
    public PictogramButton(Context context, int postion) {
        super(context);
        this.selectedStation = postion;
        this.setup();
    }
    
	public PictogramButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setup();
	}
	
	public void bindStationAsCategory(StationConfiguration station,int position) {
	    this.station = station;
	    this.setPictogram(station.getCategory());
        this.isCategory = true;
        this.selectedStation = position;
	}
	
	public int getPictogramId() {
        return this.pictogramId;
    }
	
    public Pictogram getPictogram() {
        return (Pictogram) this.pictogramContainer.getChildAt(0); //Returns null if non-existent
    }
	
	public void setRemovable(boolean isRemovable) {
	    if(isRemovable) {
	        this.removeButton.setVisibility(View.VISIBLE);   //Show remove button
	    } else {
	        this.removeButton.setVisibility(View.INVISIBLE); //Hide remove button
	    }
	}
	
	public void setPictogram(int pictogramId) {
	    this.pictogramId = pictogramId;
	    this.pictogramContainer.removeAllViews();
	    if(pictogramId == -1) { return; }
	    
	    if(this.station != null) { this.station.setCategory(pictogramId); } //If this is a category, save it
	    
	    Pictogram pictogram = PictoFactory.getPictogram(getContext(), pictogramId);
        pictogram.renderImage();
        pictogram.renderText();
        
        this.pictogramContainer.addView(pictogram);
	}
	
	private final class PictogramClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            //TODO Create loading picture
            ((MainActivity) PictogramButton.this.getContext()).startPictoAdmin(MainActivity.RECEIVE_SINGLE, PictogramButton.this.selectedStation, PictogramButton.this.number,PictogramButton.this.isCategory, PictogramButton.this);

        }
    }
	
	private final class RemoveClickListener implements OnClickListener {

        @Override
	    public void onClick(View view) {
	        if(PictogramButton.this.removeButton.getVisibility() == View.VISIBLE) {
                ((MainActivity) PictogramButton.this.getContext()).listOfStations.stations.get(PictogramButton.this.selectedStation).removeAccepPictogram(PictogramButton.this.getPictogramId());
                ((ViewGroup) PictogramButton.this.getParent()).removeView(PictogramButton.this);
               }
	    }
	}
}