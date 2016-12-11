package com.outlook.notyetapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.SQLException;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.outlook.notyetapp.data.HabitContract.ActivitiesEntry;
import com.outlook.notyetapp.utilities.AnalyticsConstants;

public class CreateActivity extends AppCompatActivity implements DoneCancelFragment.OnFragmentInteractionListener {

    private static ActivitySettingsFragment mActivitySettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySettingsFragment = new ActivitySettingsFragment();

        // Use DoneCancelFragment to set the custom action bar
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_create_container, new DoneCancelFragment(), "DoneCancelFragment")
                .add(R.id.activity_create_container, mActivitySettingsFragment, "ActivitySettingsFragment")
                .commit();

        setContentView(R.layout.activity_create);
    }


    //Called by DoneCancelFragment
    @Override
    public void doneClicked() {
        if(mActivitySettingsFragment.validate())
        {
            return;
        }
        ContentValues values = mActivitySettingsFragment.getSettingsFromUI();
        //Since we don't have any user data here, their best values will be the same as the historical values.
        values.put(ActivitiesEntry.COLUMN_BEST7, values.getAsFloat(ActivitiesEntry.COLUMN_HISTORICAL));
        values.put(ActivitiesEntry.COLUMN_BEST30, values.getAsFloat(ActivitiesEntry.COLUMN_HISTORICAL));
        values.put(ActivitiesEntry.COLUMN_BEST90, values.getAsFloat(ActivitiesEntry.COLUMN_HISTORICAL));
        //Put the activity at the end by default (this application isn't designed for more than 100 activities (not enforced).
        values.put(ActivitiesEntry.COLUMN_SORT_PRIORITY, 999);
        values.put(ActivitiesEntry.COLUMN_HIDE_DATE, 0);
        try {
            Uri resultUri = getContentResolver().insert(ActivitiesEntry.CONTENT_URI, values);
            long activityId = ActivitiesEntry.getActivityNumberFromUri(resultUri);

            if (activityId != -1) {
                CreateHistoricalDataTask task = new CreateHistoricalDataTask();
                task.execute(new CreateHistoricalDataTask.CreateHistoricalDataTaskParams[]{
                        new CreateHistoricalDataTask.CreateHistoricalDataTaskParams(this,
                                activityId,
                                values.getAsFloat(ActivitiesEntry.COLUMN_HISTORICAL),
                                values.getAsInteger(ActivitiesEntry.COLUMN_HIGHER_IS_BETTER) == 1)
                });

                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                mFirebaseAnalytics.logEvent(AnalyticsConstants.EventNames.HABIT_CREATED, new Bundle());
                finish();
            }
        }
        //If we hit an error, we show a somewhat generic error message
        catch (SQLException e){
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setTitle(getString(R.string.error_creating_activity_title))
                    .setMessage(getString(R.string.error_creating_activity_message))
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    //Called by DoneCancelFragment
    // Just close the activity without saving anything since the user hit cancel.
    @Override
    public void cancelClicked() {
        finish();
    }
}
