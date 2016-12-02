package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.outlook.notyetapp.MainActivity;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.DBHelper;
import com.outlook.notyetapp.data.HabitContract;

/**
 * Created by Neil on 11/30/2016.
 */

public class DemoUtils {

    public static final String DEMO_DATABASE_NAME = "notyet_demo.db";

    // A small demo DB is included in the app assets folder. If this is a demo build,
    // we can copy that db to be the application db. But the data will be old.
    // so we update all the db dates within the db so app will look like it is fresh.
    // this is ulgy/hacky... but demo code so perhaps no need to refactor?
    public static void CopyDemoDBAndUpdate(Context context){
        DBHelper helper = new DBHelper(context);
        helper.copyDemoDB(DEMO_DATABASE_NAME);

        Cursor data = context.getContentResolver().query(
                HabitContract.RecentDataQueryHelper.RECENT_DATA_URI,
                HabitContract.RecentDataQueryHelper.RECENT_DATA_PROJECTION,
                null, //selection
                null, //selectionArgs
                null //Sort order
        );
        long maxDate = 0;
        while (data.moveToNext()){
            long activityMax = data.getLong(HabitContract.RecentDataQueryHelper.COLUMN_DATE);
            if(activityMax > maxDate){
                maxDate = activityMax;
            }
        }

        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_day_change_key), "0"));
        long todaysDBDate = HabitContract.HabitDataEntry.getTodaysDBDate(offset);

        if(maxDate > 0 && todaysDBDate > maxDate) {
            long amountToUpdate = todaysDBDate - maxDate;

            // This is a fun trick, when we add the days to update, we invert the number to make it negative.
            // This helps us avoid uniqueness constraints. If we don't do this, about half of the rows will be deleted.
            helper.getWritableDatabase().execSQL("UPDATE " + HabitContract.HabitDataEntry.TABLE_NAME
                    + " SET " + HabitContract.HabitDataEntry.COLUMN_DATE + " = "
                    + " - (" + HabitContract.HabitDataEntry.COLUMN_DATE + " + " + String.valueOf(amountToUpdate) + ")");
            // Then we invert it back.
            helper.getWritableDatabase().execSQL("UPDATE " + HabitContract.HabitDataEntry.TABLE_NAME
                    + " SET " + HabitContract.HabitDataEntry.COLUMN_DATE + " = "
                    + " - " + HabitContract.HabitDataEntry.COLUMN_DATE);

            helper.getWritableDatabase().execSQL("UPDATE " + HabitContract.ActivitiesEntry.TABLE_NAME
                    + " SET " + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " = "
                    + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " + " + String.valueOf(amountToUpdate));
        }
        context.getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);

        SharedPreferences sharedPref = ((MainActivity)context).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }
}
