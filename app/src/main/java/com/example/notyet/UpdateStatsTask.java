package com.example.notyet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.notyet.data.HabitContract;

// Whenever habit data is created or updated, we need to update what the best values are for the activity.
// That data is stored in the Activities table so that it can easily be displayed along with current user values
// on the MainActivity (or the HabitActivity) so the user knows if they are improving.
public class UpdateStatsTask extends AsyncTask<UpdateStatsTask.Params, Void, Void> {
    @Override
    protected Void doInBackground(Params... paramses) {

        String[] projection = null;
        if(paramses[0].mHigherIsBetter)
        {
            projection = HabitContract.UpdateStatsTaskQueryHelper.MAX_UPDATE_STATS_TASK_PROJECTION;
        }
        else {
            projection = HabitContract.UpdateStatsTaskQueryHelper.MIN_UPDATE_STATS_TASK_PROJECTION;
        }

        Cursor data = paramses[0].mContext.getContentResolver().query(
                HabitContract.UpdateStatsTaskQueryHelper.buildUriForHabitDataStatsForActivityId(paramses[0].mActivityId),
                projection,
                null, // selection, handled by URI
                null, // selectionargs, handled by URI
                null // sort order
        );

        if(data.moveToFirst()) {
            ContentValues cvs = new ContentValues(3);
            cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST7, data.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_7));
            cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST30, data.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_30));
            cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST90, data.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_90));
            paramses[0].mContext.getContentResolver().update(
                    HabitContract.ActivitiesEntry.buildActivityUri(paramses[0].mActivityId),
                    cvs,
                    null, //where, handled by Uri
                    null);//selectionArgs, handled by Uri

        }
        //We have updated the Best data for the activity, which affects both the activityUri and the stats uri.
        paramses[0].mContext.getContentResolver().notifyChange(HabitContract.ActivitiesEntry.buildActivityUri(paramses[0].mActivityId), null);
        paramses[0].mContext.getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);

        return null;
    }

    public static class Params{
        public long mActivityId;
        public Context mContext;
        public boolean mHigherIsBetter;

        public Params(long mActivityId, Context mContext, boolean mHigherIsBetter) {
            this.mActivityId = mActivityId;
            this.mContext = mContext;
            this.mHigherIsBetter = mHigherIsBetter;
        }
    }
}
