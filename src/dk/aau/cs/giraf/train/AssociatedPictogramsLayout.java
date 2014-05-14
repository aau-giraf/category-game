package dk.aau.cs.giraf.train;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * A layout containing the pictograms associated to the station.
 * @author Jesper Riemer Andersen
 * 
 */
public class AssociatedPictogramsLayout extends LinearLayout implements PictogramReceiver {
    
    private StationConfiguration station;
    private ArrayList<PictogramButton> pictogramButtons = new ArrayList<PictogramButton>();
    private ImageButton addPB;
    private Context parent;

    public AssociatedPictogramsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent = context;
        addPB = (ImageButton) ((MainActivity) context).findViewById(R.id.addPictogramButton);
    }
    
    public int getPictogramCount() {
        return this.pictogramButtons.size();
    }
    
    public void bindStation(StationConfiguration station) {
        this.station = station;
        this.pictogramButtons.clear();

        ArrayList<Integer> acceptPictograms = new ArrayList<Integer>(station.getAcceptPictograms());
        
        for (int i = 0; i < acceptPictograms.size(); i++) {
            this.addPictogram(acceptPictograms.get(i));

            if(this.pictogramButtons.size() >= MainActivity.ALLOWED_PICTOGRAMS) {
                this.addPB.setVisibility(INVISIBLE);
                break;
            }
        }
    }
    
    public synchronized void addPictogram(int pictogramId) {
        PictogramButton pictogramButton = new PictogramButton(parent);
        pictogramButton.setPictogram(pictogramId);
        pictogramButton.setRemovable(true);
        LayoutParams params = new LayoutParams(75, 75);
        params.setMargins(0, 0, 1, 0);
        pictogramButton.setLayoutParams(params);
        
        this.pictogramButtons.add(pictogramButton);
        this.addView(pictogramButton);
    }
    
    private void bindPictograms() {
        this.station.clearAcceptPictograms();
        
        for (PictogramButton pictogramButton : this.pictogramButtons) {
            this.station.addAcceptPictogram(pictogramButton.getPictogram().getPictogramID());
        }
    }
    
    @Override
    public void removeView(View view) {
        super.removeView(view);
        this.station.removeAccepPictogram(pictogramButtons.indexOf(view));
        this.pictogramButtons.remove(view);

        if(this.pictogramButtons.size() >= MainActivity.ALLOWED_PICTOGRAMS) {
            this.addPB.setVisibility(VISIBLE);
        }
    }
    @Override
    public void receivePictograms(int[] pictogramIds, int requestCode) {
        for (int id : pictogramIds) {
            this.addPictogram(id);
            if(this.pictogramButtons.size() >= MainActivity.ALLOWED_PICTOGRAMS) {
                this.addPB.setVisibility(INVISIBLE);
                break;
            }
        }

        bindPictograms();
    }
}
