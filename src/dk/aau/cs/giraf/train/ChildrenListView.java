package dk.aau.cs.giraf.train;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * The ChildrenListView class is a {@link ListView} that lists children with the {@link ChildAdapter}.
 * see Child
 * see Guardian
 * @see ChildAdapter
 * @author Nicklas Andersen
 */
public class ChildrenListView extends ListView {
    
	private ChildAdapter adapter;
	
	public ChildrenListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		super.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                //ChildrenListView.this.adapter.setSelectedPosition(position);
            }
        });
	}
	
	/**
	 * loadChildren() fills the list with children associated to the current guardian.
	 * @see Guardian
	 * @see ChildAdapter
	 * @see Child
	 */
	/*public void loadChildren(List<Profile> listOfChildren) {
		//ArrayList<Child> children = guardian.publishList();
		
		//We want to remove the children with the names "Last Used" and "Predefined Profiles".
		//This is a terrible solution to remove them, but java does not have built-in support for this.
		/*for (int i = 0; i < listOfChildren.size(); i++) {
		    if (listOfChildren.get(i).name == "Last Used" || listOfChildren.get(i).name == "Predefined Profiles") {
                listOfChildren.remove(i);
		        i--; //Removed an item, then go back one index
		    }
		}
		
		this.adapter = new ChildAdapter(super.getContext(), R.drawable.list_item, listOfChildren);
		super.setAdapter(this.adapter);
		if(this.adapter.getCount() != 0) {
		    this.adapter.setSelectedPosition(0);
		}
	}
	
	public Child getSelectedChild() {
		return this.adapter.getSelectedChild();
	}*/
}
