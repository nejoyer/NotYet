package com.outlook.notyetapp.utilities.rx;

import android.content.ContentValues;

import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.HabitData;
import com.outlook.notyetapp.utilities.RollingAverageHelper;

import java.util.List;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.observers.AsyncCompletableSubscriber;

// Used to create data to fill the gap between the last day that the user used the app and today
public class RecentDataHelper {

    private StorIOContentResolverHelper storIOContentResolverHelper;
    private DateHelper dateHelper;
    private HabitContractUriBuilder habitContractUriBuilder;

    public RecentDataHelper(StorIOContentResolverHelper storIOContentResolverHelper,
                            DateHelper dateHelper,
                            HabitContractUriBuilder habitContractUriBuilder) {
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.dateHelper = dateHelper;
        this.habitContractUriBuilder = habitContractUriBuilder;
    }

    public static class Params {
        public long activityId;
        public float valueToUse;
        public HabitContract.HabitDataEntry.HabitValueType type;

        public Params(long activityId, float valueToUse, HabitContract.HabitDataEntry.HabitValueType type) {
            this.activityId = activityId;
            this.valueToUse = valueToUse;
            this.type = type;
        }
    }

    public Observable.Transformer<RecentDataHelper.Params, Void> getRecentDataTransformer(){
        return new Observable.Transformer<RecentDataHelper.Params, Void>() {
            @Override
            public Observable<Void> call(Observable<RecentDataHelper.Params> recentDataParamsObservable) {
                return recentDataParamsObservable.map(
                        new Func1<RecentDataHelper.Params, Void>() {
                            @Override
                            public Void call(final RecentDataHelper.Params recentDataParams) {
                                RecentDataHelper.this.storIOContentResolverHelper
                                        .getSingleListOfObjectsHabitDataNormalizeLimit90OnIOThread(recentDataParams.activityId)
                                        .map(
                                                new Func1<List<HabitData>, Void>() {
                                                    @Override
                                                    public Void call(List<HabitData> habitDatas) {
                                                        long highestDate = 0;
                                                        RollingAverageHelper helper = new RollingAverageHelper();

                                                        for(int i = habitDatas.size() - 1; i >= 0; i--){
                                                            helper.PushNumber(habitDatas.get(i).value);
                                                            highestDate = habitDatas.get(i).date;
                                                        }

                                                        long todaysDBDate = dateHelper.getTodaysDBDate();
                                                        if(highestDate == 0)
                                                            highestDate = todaysDBDate - 90;

                                                        ContentValues[] contentValues = new ContentValues[(int)(todaysDBDate - highestDate)];

                                                        for(int i = 0; highestDate < todaysDBDate; i++)
                                                        {
                                                            highestDate++;
                                                            ContentValues values = new ContentValues();
                                                            helper.PushNumber(recentDataParams.valueToUse);
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID, recentDataParams.activityId);
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_DATE, highestDate);
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_VALUE, recentDataParams.valueToUse);
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7, helper.GetAverage7());
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30, helper.GetAverage30());
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90, helper.GetAverage90());
                                                            values.put(HabitContract.HabitDataEntry.COLUMN_TYPE, recentDataParams.type.getValue());
                                                            contentValues[i] = values;
                                                        }

                                                        RecentDataHelper.this.storIOContentResolverHelper.getContentResolver()
                                                                .bulkInsert(habitContractUriBuilder.getHabitDataEntryUri(), contentValues);
                                                        RecentDataHelper.this.storIOContentResolverHelper
                                                                .notifyChangeAtUri(habitContractUriBuilder.buildHabitDataUriForActivity(recentDataParams.activityId));
                                                        RecentDataHelper.this.storIOContentResolverHelper
                                                                .notifyChangeAtUri(habitContractUriBuilder.buildActivitiesStatsUri());

                                                        return null;
                                                    }
                                                }
                                        )
                                        .toCompletable()
                                        .subscribe(new AsyncCompletableSubscriber() {
                                            @Override
                                            public void onCompleted() {}

                                            @Override
                                            public void onError(Throwable e) {
                                                throw Exceptions.propagate(e);
                                            }
                                        });
                                return null;
                            }
                        }
                );
            }
        };
    }
}
