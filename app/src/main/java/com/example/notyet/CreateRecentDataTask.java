package com.example.notyet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.example.notyet.data.HabitContract;

// Async task for adding HabitData entries to all activities up to the current date since the last time they had data added
public class CreateRecentDataTask extends AsyncTask<Context, Void, Void> {

    private Context mContext = null;

    @Override
    protected Void doInBackground(Context... contexts) {
        mContext = contexts[0];

        Cursor data = mContext.getContentResolver().query(
                HabitContract.RecentDataQueryHelper.RECENT_DATA_URI,
                HabitContract.RecentDataQueryHelper.RECENT_DATA_PROJECTION,
                null, //selection
                null, //selectionArgs
                null //Sort order
        );

        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.pref_day_change_key), "0"));
        long todaysDBDate = HabitContract.HabitDataEntry.getTodaysDBDate(offset);
        //Loop through each activity
        while (data.moveToNext())
        {
            if(data.getLong(HabitContract.RecentDataQueryHelper.COLUMN_DATE) < todaysDBDate) {
                long activityId = data.getLong(HabitContract.RecentDataQueryHelper.COLUMN_ACTIVITY_ID);
                boolean higherIsBetter = data.getInt(HabitContract.RecentDataQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1;
                float valueToUse = 0;
                //If higher is better then we default to 0, but if lower is better,
                // then we want to use the (presumably) bad historical value until the user enters another value.
                if(!higherIsBetter) {
                    valueToUse = data.getFloat(HabitContract.RecentDataQueryHelper.COLUMN_HISTORICAL);
                }

                //populate recent days with data
                CreateRecentDataTask.CreateRecentDataUsingValue(
                        mContext,
                        activityId,
                        valueToUse,
                        HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED,
                        higherIsBetter);
            }
        }

        // method must return since Void is not void.
        return null;
    }

    // public so that it can also be called from "CreateHistoricalDataTask".
    // does the work of bulk updating the DB
    public static void CreateRecentDataUsingValue(Context context,
                                                  long activityId,
                                                  float valueToUse,
                                                  HabitContract.HabitDataEntry.HabitValueType type,
                                                  boolean higherIsBetter){

        Cursor data = context.getContentResolver().query(
                HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(activityId), //Uri
                HabitContract.NormalizeActivityDataTaskQueryHelper.NORMALIZE_ACTIVITY_DATA_TASK_PROJECTION,
                null,//Selection (taken care of by Uri)
                null,//SelectionArgs
                HabitContract.NormalizeActivityDataTaskQueryHelper.SORT_ORDER_AND_LIMIT_90
        );

        RollingAverageHelper helper = new RollingAverageHelper();

        long highestDate = 0;

        data.moveToLast();
        while (data.moveToPrevious())
        {
            helper.PushNumber(data.getFloat(HabitContract.NormalizeActivityDataTaskQueryHelper.COLUMN_VALUE));
            highestDate = data.getLong(HabitContract.NormalizeActivityDataTaskQueryHelper.COLUMN_DATE);
        }

        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_day_change_key), "0"));
        long todaysDBDate = HabitContract.HabitDataEntry.getTodaysDBDate(offset);

        if(highestDate == 0)
            highestDate = todaysDBDate - 90;

        ContentValues[] contentValues = new ContentValues[(int)(todaysDBDate - highestDate)];

        for(int i = 0; highestDate < todaysDBDate; i++)
        {
            highestDate++;
            ContentValues values = new ContentValues();
            helper.PushNumber(valueToUse);
            values.put(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID, activityId);
            values.put(HabitContract.HabitDataEntry.COLUMN_DATE, highestDate);
            values.put(HabitContract.HabitDataEntry.COLUMN_VALUE, valueToUse);
            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7, helper.GetAverage7());
            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30, helper.GetAverage30());
            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90, helper.GetAverage90());
            values.put(HabitContract.HabitDataEntry.COLUMN_TYPE, type.getValue());
            contentValues[i] = values;
        }

        context.getContentResolver().bulkInsert(HabitContract.HabitDataEntry.CONTENT_URI, contentValues);
        context.getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);

        // Since the new values could have changed both the Best and Current averages, we need to run this task.
        UpdateStatsTask task = new UpdateStatsTask();
        task.execute(new UpdateStatsTask.Params(activityId, context, higherIsBetter));
    }
}
