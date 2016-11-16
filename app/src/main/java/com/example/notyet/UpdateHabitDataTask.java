package com.example.notyet;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.example.notyet.data.HabitContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// When the user updates HabitData points all the averages for that day and many future dates must be
// calculated and persisted to the DB. Do this asynchronously and display a progress dialog if it might be a while.
public class UpdateHabitDataTask extends AsyncTask<UpdateHabitDataTask.Params, Void, Void> {

    private Params mParams = null;

    private Context mContext;

    private ProgressDialog mProgressDialog = null;

    boolean mShowDialog = false;

    public UpdateHabitDataTask(Context context, boolean showDialog) {
        this.mContext = context;
        this.mShowDialog = showDialog;
    }

    @Override
    protected void onPreExecute() {
        if(this.mContext != null && mShowDialog){
            mProgressDialog = ProgressDialog.show(this.mContext, this.mContext.getResources().getString(R.string.habit_data_update_title), null);
        }

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Params... paramsSet) {
        mParams = paramsSet[0];

        Collections.sort(mParams.mDatesToUpdate, new Comparator<Long>() {
            @Override
            public int compare(Long long1, Long long2) {
                return (int)(long1 - long2);
            }
        });

        // 90 days prior to the changed date contribute to that date's 90 day average.
        long firstDateThatAffectChangedDate = mParams.mDatesToUpdate.get(0) - 90;
        // 90 days after the last changed date have their 90 day rolling average affected by that changed date.
        long affectsUpToDate = mParams.mDatesToUpdate.get(mParams.mDatesToUpdate.size() - 1) + 90;

        String selection = HabitContract.HabitDataEntry.COLUMN_DATE + " >= ? AND " + HabitContract.HabitDataEntry.COLUMN_DATE + " <= ?";
        String[] selectionArgs = new String[]{String.valueOf(firstDateThatAffectChangedDate), String.valueOf(affectsUpToDate)};

        // retrieve any data the DB already has
        Cursor data = mParams.mContext.getContentResolver().query(HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(mParams.mActivityId),
                HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION,
                selection,
                selectionArgs,
                HabitContract.HabitDataQueryHelper.SORT_BY_DATE_ASC);

        RollingAverageHelper helper = new RollingAverageHelper();
        data.moveToFirst();

        // Get the oldest date the DB has for this activity.
        long curDate = data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
        int index = 0;
        // If the caller has requested updates to dates that the DB doesn't have yet (adding more history)
        // Then bulk insert them.
        if(mParams.mDatesToUpdate.get(index) < curDate) {

            ArrayList<ContentValues> valuesToAdd = new ArrayList<ContentValues>();

            while (index < mParams.mDatesToUpdate.size() && mParams.mDatesToUpdate.get(index) < curDate) {
                //insert more history
                helper.PushNumber(mParams.mNewValue);
                ContentValues contentValues = helper.GetValues();
                contentValues.put(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID, mParams.mActivityId);
                contentValues.put(HabitContract.HabitDataEntry.COLUMN_DATE, mParams.mDatesToUpdate.get(index));
                contentValues.put(HabitContract.HabitDataEntry.COLUMN_VALUE, mParams.mNewValue);
                contentValues.put(HabitContract.HabitDataEntry.COLUMN_TYPE, mParams.mType.getValue());
                valuesToAdd.add(contentValues);
                index++;
            }
            ContentValues[] valuesToAddArray = new ContentValues[valuesToAdd.size()];
            valuesToAddArray = valuesToAdd.toArray(valuesToAddArray);
            mParams.mContext.getContentResolver().bulkInsert(HabitContract.HabitDataEntry.CONTENT_URI, valuesToAddArray);
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        //todo refactor
        // Until we get to all the affected dates or we get to the newest date in the DB
        // (this method doesn't support adding new data for recent dates that are not yet in the db,
        // that is handled by CreateRecentDataTask)
        while(!data.isAfterLast() && data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE) < affectsUpToDate)
        {
            float curVal = data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
            curDate = data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);

            if(mParams.mDatesToUpdate.contains(curDate)){
                curVal = mParams.mNewValue;
            }

            helper.PushNumber(curVal);

            // For dates before the oldest that we are updating, we only need the data to push into our averager...
            // for dates after the oldest that we are updating, we need to update the averages for sure and potentially the value.
            if(curDate >= mParams.mDatesToUpdate.get(0)) {
                // populates the values with the 3 averages
                ContentValues values = helper.GetValues();

                //Update the date's value as well if specified by the caller.
                if(mParams.mDatesToUpdate.contains(curDate)) {
                    values.put(HabitContract.HabitDataEntry.COLUMN_TYPE, mParams.mType.getValue());
                    values.put(HabitContract.HabitDataEntry.COLUMN_VALUE, mParams.mNewValue);
                }

                operations.add(
                        ContentProviderOperation.newUpdate(
                                HabitContract.HabitDataEntry.buildHabitDataUriForHabitDataEntryId(
                                        data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_HABIT_ID)
                                )
                        ).withValues(values)
                                .build()
                );
            }
            data.moveToNext();
        }
        try {
            mParams.mContext.getContentResolver().applyBatch(HabitContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException e) {
            // TODO
        } catch (OperationApplicationException e) {
            // TODO
        }

        //This will force the HabitActivity (graph, data) to re-render because we changed data.
        mParams.mContext.getContentResolver().notifyChange(HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(mParams.mActivityId), null);

        return null;
    }

    // After the task has run, back on the UI thread, hide the busy indicator.
    @Override
    protected void onPostExecute(Void aVoid) {
        UpdateStatsTask task = new UpdateStatsTask();
        task.execute(new UpdateStatsTask.Params(mParams.mActivityId, mParams.mContext, mParams.mHigherIsBetter));

        if(this.mProgressDialog != null){
            if(this.mProgressDialog.getOwnerActivity() != null) {
                this.mProgressDialog.dismiss();
            }
        }

        super.onPostExecute(aVoid);
    }

    public static class Params{
        public Context mContext;
        public long mActivityId;
        public boolean mHigherIsBetter;
        public ArrayList<Long> mDatesToUpdate;
        public float mNewValue;
        public HabitContract.HabitDataEntry.HabitValueType mType;

        public Params(Context mContext,
                      long mActivityId,
                      boolean mHigherIsBetter,
                      ArrayList<Long> mDatesToUpdate,
                      float mNewValue,
                      HabitContract.HabitDataEntry.HabitValueType mType) {
            this.mContext = mContext;
            this.mActivityId = mActivityId;
            this.mHigherIsBetter = mHigherIsBetter;
            this.mDatesToUpdate = mDatesToUpdate;
            this.mNewValue = mNewValue;
            this.mType = mType;
        }
    }
}

