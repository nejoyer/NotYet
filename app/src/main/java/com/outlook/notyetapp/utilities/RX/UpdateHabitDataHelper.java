package com.outlook.notyetapp.utilities.rx;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.utilities.RollingAverageHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rx.Single;
import rx.functions.Func1;

// If the user updates the value for a habit on a given day or days, we need to update the value
// and the averages in the db for that day and all the days around it.
public class UpdateHabitDataHelper {

    private HabitContractUriBuilder habitContractUriBuilder;
    private StorIOContentResolverHelper storIOContentResolverHelper;

    public UpdateHabitDataHelper(HabitContractUriBuilder habitContractUriBuilder, StorIOContentResolverHelper storIOContentResolverHelper) {
        this.habitContractUriBuilder = habitContractUriBuilder;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
    }

    public static class Params {
        public long mActivityId;
        public ArrayList<Long> mDatesToUpdate;
        public float mNewValue;
        public HabitContract.HabitDataEntry.HabitValueType mType;

        public Params(long mActivityId,
                      ArrayList<Long> mDatesToUpdate,
                      float mNewValue,
                      HabitContract.HabitDataEntry.HabitValueType mType) {
            this.mActivityId = mActivityId;
            this.mDatesToUpdate = mDatesToUpdate;
            this.mNewValue = mNewValue;
            this.mType = mType;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Params)
            {
                Params params = (Params) o;

                if(params.mDatesToUpdate == null || this.mDatesToUpdate == null){
                    return params.mActivityId == this.mActivityId &&
                            params.mDatesToUpdate == this.mDatesToUpdate &&
                            params.mNewValue == this.mNewValue &&
                            params.mType == this.mType;
                }

                return params.mActivityId == this.mActivityId &&
                        params.mDatesToUpdate.toString().compareTo(this.mDatesToUpdate.toString()) == 0 &&
                        params.mNewValue == this.mNewValue &&
                        params.mType == this.mType;
            }
            return false;
        }
    }


    public Single.Transformer<Params, Void> getUpdateHabitDataTransformer() {
        return new Single.Transformer<Params, Void>() {
            @Override
            public Single<Void> call(Single<Params> datesToUpdateSingle) {
                return datesToUpdateSingle.map(new Func1<Params, Void>() {
                    @Override
                    public Void call(Params params) {

                        if(params == null){
                            return null;
                        }

                        long activityId = params.mActivityId;
                        ArrayList<Long> datesToUpdate = params.mDatesToUpdate;
                        float newValue = params.mNewValue;
                        HabitContract.HabitDataEntry.HabitValueType type = params.mType;


                        Collections.sort(datesToUpdate, new Comparator<Long>() {
                            @Override
                            public int compare(Long long1, Long long2) {
                                return (int) (long1 - long2);
                            }
                        });

                        // 90 days prior to the changed date contribute to that date's 90 day average.
                        long firstDateThatAffectChangedDate = datesToUpdate.get(0) - 90;
                        // 90 days after the last changed date have their 90 day rolling average affected by that changed date.
                        long affectsUpToDate = datesToUpdate.get(datesToUpdate.size() - 1) + 90;

                        String selection = HabitContract.HabitDataEntry.COLUMN_DATE + " >= ? AND " + HabitContract.HabitDataEntry.COLUMN_DATE + " <= ?";
                        String[] selectionArgs = new String[]{String.valueOf(firstDateThatAffectChangedDate), String.valueOf(affectsUpToDate)};

                        Cursor cursor = null;
                        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

                        try {
                            // retrieve any data the DB already has
                            cursor = storIOContentResolverHelper.getContentResolver().query(habitContractUriBuilder.buildHabitDataUriForActivity(activityId),
                                    HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION,
                                    selection,
                                    selectionArgs,
                                    HabitContract.HabitDataQueryHelper.SORT_BY_DATE_ASC);


                            RollingAverageHelper helper = new RollingAverageHelper();
                            cursor.moveToFirst();

                            // Get the oldest date the DB has for this activity.
                            long curDate = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
                            int index = 0;
                            // If the caller has requested updates to dates that the DB doesn't have yet (adding more history)
                            // Then bulk insert them.
                            if (datesToUpdate.get(index) < curDate) {

                                ArrayList<ContentValues> valuesToAdd = new ArrayList<ContentValues>();

                                while (index < datesToUpdate.size() && datesToUpdate.get(index) < curDate) {
                                    //insert more history
                                    helper.PushNumber(newValue);
                                    ContentValues contentValues = helper.GetValues();
                                    contentValues.put(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID, activityId);
                                    contentValues.put(HabitContract.HabitDataEntry.COLUMN_DATE, datesToUpdate.get(index));
                                    contentValues.put(HabitContract.HabitDataEntry.COLUMN_VALUE, newValue);
                                    contentValues.put(HabitContract.HabitDataEntry.COLUMN_TYPE, type.getValue());
                                    valuesToAdd.add(contentValues);
                                    index++;
                                }
                                ContentValues[] valuesToAddArray = new ContentValues[valuesToAdd.size()];
                                valuesToAddArray = valuesToAdd.toArray(valuesToAddArray);
                                storIOContentResolverHelper.getContentResolver().bulkInsert(HabitContract.HabitDataEntry.CONTENT_URI, valuesToAddArray);
                            }

                            //todo refactor
                            // Until we get to all the affected dates or we get to the newest date in the DB
                            // (this method doesn't support adding new data for recent dates that are not yet in the db,
                            // that is handled by CreateRecentDataTask)
                            while (!cursor.isAfterLast() && cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE) < affectsUpToDate) {
                                float curVal = cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
                                curDate = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);

                                if (datesToUpdate.contains(curDate)) {
                                    curVal = newValue;
                                }

                                helper.PushNumber(curVal);

                                // For dates before the oldest that we are updating, we only need the data to push into our averager...
                                // for dates after the oldest that we are updating, we need to update the averages for sure and potentially the value.
                                if (curDate >= datesToUpdate.get(0)) {
                                    // populates the values with the 3 averages
                                    ContentValues values = helper.GetValues();

                                    //Update the date's value as well if specified by the caller.
                                    if (datesToUpdate.contains(curDate)) {
                                        values.put(HabitContract.HabitDataEntry.COLUMN_TYPE, type.getValue());
                                        values.put(HabitContract.HabitDataEntry.COLUMN_VALUE, newValue);
                                    }

                                    operations.add(
                                            ContentProviderOperation.newUpdate(
                                                    HabitContract.HabitDataEntry.buildHabitDataUriForHabitDataEntryId(
                                                            cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_HABIT_ID)
                                                    )
                                            ).withValues(values)
                                                    .build()
                                    );
                                }
                                cursor.moveToNext();
                            }
                        }
                        finally {
                            if(cursor != null) {
                                cursor.close();
                            }
                        }
                        try {
                            storIOContentResolverHelper.getContentResolver().applyBatch(HabitContract.CONTENT_AUTHORITY, operations);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        } catch (OperationApplicationException e) {
                            throw new RuntimeException(e);
                        }

                        //This will force the HabitActivity (graph, data) to re-render because we changed data.
                        storIOContentResolverHelper.notifyChangeAtUri(HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(activityId));

                        return null;
                    }
                });
            }
        };
    }
}
