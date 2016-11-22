package com.outlook.notyetapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.outlook.notyetapp.data.HabitContract;

import java.util.ArrayList;

public class ActivitySettingsActivity extends AppCompatActivity implements DoneCancelFragment.OnFragmentInteractionListener {

    private static ActivitySettingsFragment mActivitySettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySettingsFragment = ActivitySettingsFragment.newInstance(getIntent().getData());

        // Use DoneCancelFragment to set the custom action bar
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_settings_container, new DoneCancelFragment(), "DoneCancelFragment")
                .add(R.id.activity_settings_container, mActivitySettingsFragment, "ActivitySettingsFragment")
                .commit();

        setContentView(R.layout.activity_activity_settings);
    }

    // This will get called from the fragment.
    @Override
    public void doneClicked() {
        if(mActivitySettingsFragment.validate())
        {
            return;
        }
        long activityId = HabitContract.ActivitiesEntry.getActivityNumberFromUri(getIntent().getData());

        Cursor activitySettingsCursor = getContentResolver().query(
                getIntent().getData(),//Uri
                HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION,//projection
                null,//selection
                null,//selectionArgs
                null//Sort Order
        );
        activitySettingsCursor.moveToFirst();
        float initalHistorical = activitySettingsCursor.getFloat(HabitContract.ActivitySettingsQueryHelper.COLUMN_HISTORICAL);

        //update activity settings
        ContentValues values = mActivitySettingsFragment.getSettingsFromUI();
        getContentResolver().update(getIntent().getData(), values, null, null);

        float newHistorical = values.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL);

        if(initalHistorical != newHistorical)
        {
            //Historical value has been updated. Update all historical entries.
            Cursor data = getContentResolver().query(HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(activityId),
                    new String[]{HabitContract.HabitDataEntry.COLUMN_DATE},
                    HabitContract.HabitDataEntry.COLUMN_TYPE + " = ? ",
                    new String[]{String.valueOf(HabitContract.HabitDataEntry.HabitValueType.HISTORICAL.getValue())},
                    null); /* sort order*/

            ArrayList<Long> datesToUpdate = new ArrayList<Long>(data.getCount());
            data.moveToPosition(-1);
            while(data.moveToNext())
            {
                datesToUpdate.add(data.getLong(0));
            }

            UpdateHabitDataTask task = new UpdateHabitDataTask(this, true);
            task.execute(
                    new UpdateHabitDataTask.Params(this,
                            activityId,
                            values.getAsInteger(HabitContract.ActivitiesEntry.COLUMN_HIGHER_IS_BETTER) == 1,
                            datesToUpdate,
                            newHistorical,
                            HabitContract.HabitDataEntry.HabitValueType.HISTORICAL
                    )
            );
        }

        //Update the Best based on the data.
        UpdateStatsTask task = new UpdateStatsTask();
        task.execute(new UpdateStatsTask.Params(
                activityId,
                this,
                values.getAsInteger(HabitContract.ActivitiesEntry.COLUMN_HIGHER_IS_BETTER) == 1));
        finish();
    }

    // This will get called from the fragment. Exits without saving any changes.
    @Override
    public void cancelClicked() {
        finish();
    }
}
