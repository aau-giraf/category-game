package dk.aau.cs.giraf.categorygame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.google.analytics.tracking.android.EasyTracker;
import java.util.List;

import dk.aau.cs.giraf.gui.GList;
import dk.aau.cs.giraf.gui.GTextView;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.dblib.models.Category;

public class CategoryDialogActivity extends Activity{
    private Intent resultIntent = new Intent();
    private AlertDialog errorDialog;
    private List<Category> listOfCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View catelogview = inflater.inflate(R.layout.activity_category_dialog, null);

        Helper localDataFetcher = null;
        try {
            localDataFetcher = new Helper(this);
        } catch (Exception e) {}

        this.listOfCategories = localDataFetcher.categoryHelper.getCategories();
        GList categoryList = (GList)catelogview.findViewById(R.id.categoryList);
        for (Category cat: listOfCategories){
            GTextView temp = new GTextView(this);
            temp.setText(cat.getName());
            categoryList.addView(temp);
        }

        setContentView(R.layout.activity_category_dialog);

        Drawable backgroundDrawable = getResources().getDrawable(R.drawable.background_with_shape);
        backgroundDrawable.setColorFilter(this.getIntent().getExtras().getInt("appBackgroundColor"), PorterDuff.Mode.OVERLAY);
        super.findViewById(R.id.categorySelector).setBackgroundDrawable(backgroundDrawable);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setNegativeButton(super.getResources().getString(R.string.okay), null);
        this.errorDialog = alertDialogBuilder.create();

        // Start logging this activity
        EasyTracker.getInstance(this).activityStart(this);

    }
    /**
     * Stops Google Analytics logging.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Stop logging this activity
        EasyTracker.getInstance(this).activityStop(this);
    }
    private void showAlertMessage(String message) {
        this.errorDialog.setMessage(message);
        this.errorDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        super.setResult(Activity.RESULT_CANCELED, this.resultIntent);
        super.finish();
    }
}