package dk.aau.cs.giraf.train.opengl;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
/***
 * PictoFrameLayout a custom class with methods aiding to simplify the code.
 * This class is a container for the pictograms which accepts pictograms on drag.
 * @author Jacob
 *
 */
public class PictoFrameLayout extends FrameLayout {

	public PictoFrameLayout(Context context){
		super(context);
	}
	
	public PictoFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void addPictogramsToFrames(Long pictoId){
		Pictogram pic = PictoFactory.getPictogram(super.getContext(), pictoId);
		pic.setOnTouchListener(new TouchListener());
		pic.renderAll();
		
		PictoFrameLayout.LayoutParams frameLayoutParams = new PictoFrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		
		this.addView(pic, frameLayoutParams);
		this.setTag("filled");
		this.invalidate();
	}
	
	public Pictogram getPictogram(){
		return (Pictogram)this.getChildAt(0);
	}
	
	/**
	 * A touch listener that starts a drag event. There should also be a
	 * receiver implementing {@link OnDragListener}.
	 * 
	 * @see DragListener
	 */
	private final class TouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				view.startDrag(data, shadowBuilder, view, 0);
				view.setVisibility(View.INVISIBLE);
				return true;
			}
			else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {// prevents that a picto disapears if only pressed and no drag
				if(view != null && view.getVisibility() == View.INVISIBLE){
					view.setVisibility(View.VISIBLE);
				}
				return true;
			}
			else {
				return false;
			}
		}
		
	}

}
