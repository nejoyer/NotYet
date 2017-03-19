package com.outlook.notyetapp.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.data.models.HabitData;
import com.outlook.notyetapp.data.models.HabitDataOldestDate;
import com.outlook.notyetapp.data.models.RecentData;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import java.util.List;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

// Helper class to easily get RX objects for specific uses.
// By putting the chain of calls in a single DI'd class method, unit test mocking becomes waaaaay simpler.
public class StorIOContentResolverHelper {
    private StorIOContentResolver storIOContentResolver;
    private HabitContractUriBuilder habitContractUriBuilder;

    public StorIOContentResolverHelper(StorIOContentResolver storIOContentResolver, HabitContractUriBuilder habitContractUriBuilder) {
        this.storIOContentResolver = storIOContentResolver;
        this.habitContractUriBuilder = habitContractUriBuilder;
    }

    public Single<List<HabitDataOldestDate>> getSingleListOfObjectsHabitDataOldestDateOnIOThread(long activityId){

        Uri habitDataUri = habitContractUriBuilder.buildHabitDataUriForActivity(activityId);

        Single<List<HabitDataOldestDate>> datesToAddSingle = storIOContentResolver
                .get()
                .listOfObjects(HabitDataOldestDate.class)
                .withQuery(
                        Query.builder()
                                .uri(habitDataUri)
                                .columns(HabitContract.HabitDataOldestQueryHelper.HABITDATA_OLDEST_PROJECTION)
                                .sortOrder(HabitContract.HabitDataOldestQueryHelper.SORT_BY_DATE_ASC_LIMIT_1)
                                .build()
                ).prepare()
                .asRxSingle()
                .observeOn(Schedulers.io());

        return datesToAddSingle;
    }

    public Single<List<ActivitySettings>> getSingleListofObjectsActivitySettingsOnIOThread(long activityId) {

        Uri activityUri = habitContractUriBuilder.buildActivityUri(activityId);

        return storIOContentResolver
                .get()
                .listOfObjects(ActivitySettings.class)
                .withQuery(
                        Query.builder()
                                .uri(activityUri)
                                .columns(HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION)
                                .build()
                )
                .prepare()
                .asRxSingle()
                .observeOn(Schedulers.io());
    }

    public Observable<List<ActivitySettings>> getObservableListOfObjectsActivitySettingsOnMainThread(long activityId){

        Uri activityUri = habitContractUriBuilder.buildActivityUri(activityId);

        return storIOContentResolver
                .get()
                .listOfObjects(ActivitySettings.class)
                .withQuery(
                        Query.builder()
                                .uri(activityUri)
                                .columns(HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION)
                                .build()
                ).prepare()
                .asRxObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<HabitData>> getSingleListOfObjectsHabitDataNormalizeLimit90OnIOThread(long activityId){
        return storIOContentResolver
                .get()
                .listOfObjects(HabitData.class)
                .withQuery(
                        Query.builder()
                                .uri(habitContractUriBuilder.buildHabitDataUriForActivity(activityId))
                                .columns(HabitContract.NormalizeActivityDataTaskQueryHelper.NORMALIZE_ACTIVITY_DATA_TASK_PROJECTION)
                                .sortOrder(HabitContract.NormalizeActivityDataTaskQueryHelper.SORT_ORDER_AND_LIMIT_90)
                                .build()
                )
                .prepare()
                .asRxSingle()
                .observeOn(Schedulers.io());
    }

    public Observable<Cursor> getCursorHabitDataOnMainThread(long activityId){
        Uri habitDataUri = habitContractUriBuilder.buildHabitDataUriForActivity(activityId);

        return storIOContentResolver
                .get()
                .cursor()
                .withQuery(
                        Query.builder()
                                .uri(habitDataUri)
                                .columns(HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION)
                                .sortOrder(HabitContract.HabitDataQueryHelper.SORT_BY_DATE_DESC)
                                .build()
                ).prepare()
                .asRxObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Cursor> getCursorActivitiesTodaysStatsOnMainThread(String selection, String[] selectionArgs){

        Uri activityStatsUri = habitContractUriBuilder.buildActivitiesStatsUri();

        return storIOContentResolver
                .get()
                .cursor()
                .withQuery(
                        Query.builder()
                                .uri(activityStatsUri)
                                .columns(HabitContract.ActivitiesTodaysStatsQueryHelper.ACTIVITIES_TODAYS_STATS_PROJECTION)
                                .sortOrder(HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY)
                                .where(selection)
                                .whereArgs(selectionArgs)
                                .build()
                )
                .prepare()
                .asRxObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<RecentData>> getObservableListOfObjectsRecentData(){
        return storIOContentResolver
                .get()
                .listOfObjects(RecentData.class)
                .withQuery(
                        Query
                                .builder()
                                .uri(habitContractUriBuilder.getRecentDataUri())
                                .columns(HabitContract.RecentDataQueryHelper.RECENT_DATA_PROJECTION)
                                .build()
                )
                .prepare()
                .asRxObservable();
    }

    public ContentResolver getContentResolver(){
        return this.storIOContentResolver.lowLevel().contentResolver();
    }

    public void notifyChangeAtUri(Uri uriToNotify){
        getContentResolver().notifyChange(uriToNotify, null);
    }
}
