package com.outlook.notyetapp.utilities.rx;

import android.content.ContentValues;
import android.database.Cursor;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;

import java.util.List;

import rx.CompletableSubscriber;
import rx.Single;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

// Any time a value in a habit is updated, that value might affect the user's "best" stats
// for any of the averages. This should be called to update those values (if necessary)
public class UpdateStatsHelper {
    private HabitContractUriBuilder habitContractUriBuilder;
    private StorIOContentResolverHelper storIOContentResolverHelper;

    public UpdateStatsHelper(HabitContractUriBuilder habitContractUriBuilder, StorIOContentResolverHelper storIOContentResolverHelper) {
        this.habitContractUriBuilder = habitContractUriBuilder;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
    }

    public void UpdateStats(final long activityId) {

        storIOContentResolverHelper.getSingleListofObjectsActivitySettingsOnIOThread(activityId)
                .compose(getUpdateStatsTransformer())
                .toCompletable()
                .subscribe(new CompletableSubscriber() {
                    @Override
                    public void onCompleted() {
                        //We have updated the Best data for the activity, which affects both the activityUri and the stats uri.
                        storIOContentResolverHelper.notifyChangeAtUri(
                                habitContractUriBuilder.buildActivityUri(activityId));
                        storIOContentResolverHelper.notifyChangeAtUri(
                                habitContractUriBuilder.buildActivitiesStatsUri());
                    }

                    @Override
                    public void onError(Throwable e) {
                        throw Exceptions.propagate(e);
                    }

                    @Override
                    public void onSubscribe(Subscription d) {}
                });
    }

    public Single.Transformer<List<ActivitySettings>, Void> getUpdateStatsTransformer(){

        return new Single.Transformer<List<ActivitySettings>, Void>(){
            @Override
            public Single<Void> call(Single<List<ActivitySettings>> listSingle) {
                return listSingle.map(new Func1<List<ActivitySettings>, Void>() {
                    @Override
                    public Void call(List<ActivitySettings> activitySettingses) {
                        ActivitySettings settings = activitySettingses.get(0);

                        String[] projection = null;
                        if (settings.higherIsBetter == 1) {
                            projection = HabitContract.UpdateStatsTaskQueryHelper.MAX_UPDATE_STATS_TASK_PROJECTION;
                        } else {
                            projection = HabitContract.UpdateStatsTaskQueryHelper.MIN_UPDATE_STATS_TASK_PROJECTION;
                        }

                        Cursor cursor = null;
                        try {
                             cursor = storIOContentResolverHelper.getContentResolver().query(
                                    habitContractUriBuilder.buildUriForHabitDataStatsForActivityId(settings._id),
                                    projection,
                                    null, // selection, handled by URI
                                    null, // selectionargs, handled by URI
                                    null // sort order
                            );


                            if (cursor.moveToFirst()) {
                                ContentValues cvs = new ContentValues(3);
                                cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST7, cursor.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_7));
                                cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST30, cursor.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_30));
                                cvs.put(HabitContract.ActivitiesEntry.COLUMN_BEST90, cursor.getFloat(HabitContract.UpdateStatsTaskQueryHelper.COLUMN_ROLLING_AVG_90));
                                storIOContentResolverHelper.getContentResolver().update(
                                        habitContractUriBuilder.buildActivityUri(settings._id),
                                        cvs,
                                        null, //where, handled by Uri
                                        null);//selectionArgs, handled by Uri

                            }
                        }
                        finally {
                            if(cursor != null) {
                                cursor.close();
                            }
                        }


                        return null;
                    }
                });
            }
        };
    }
}
