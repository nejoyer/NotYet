package com.outlook.notyetapp.screen.habit;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract.ActionListener;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.observables.ConnectableObservable;
import rx.observers.AsyncCompletableSubscriber;
import rx.schedulers.Schedulers;

public class HabitActivityFragmentPresenter implements ActionListener {

    private HabitActivityFragmentContract.View view;
    private CursorToDataPointListHelper cursorToDataPointListHelper;
    private UpdateHabitDataHelper updateHabitDataHelper;
    private RecentDataHelper recentDataHelper;
    private UpdateStatsHelper updateStatsHelper;
    private HabitContractUriBuilder habitContractUriBuilder;
    private StorIOContentResolverHelper storIOContentResolverHelper;
    private RXMappingFunctionHelper rxMappingFunctionHelper;
    private DateHelper dateHelper;

    private Subscription cursorSubscription = null;
    private Subscription listSubscription = null;
    private Subscription bestSubscription = null;
    private Subscription sharedSubscription = null;

    // constructed via DI, so it is okay to have lots of dependencies to make unit testing easier
    public HabitActivityFragmentPresenter(HabitActivityFragmentContract.View view,
                                          CursorToDataPointListHelper cursorToDataPointListHelper,
                                          UpdateHabitDataHelper updateHabitDataHelper,
                                          RecentDataHelper recentDataHelper,
                                          UpdateStatsHelper updateStatsHelper,
                                          HabitContractUriBuilder habitContractUriBuilder,
                                          StorIOContentResolverHelper storIOContentResolverHelper,
                                          RXMappingFunctionHelper rxMappingFunctionHelper,
                                          DateHelper dateHelper) {
        this.view = view;
        this.cursorToDataPointListHelper = cursorToDataPointListHelper;
        this.updateHabitDataHelper = updateHabitDataHelper;
        this.recentDataHelper = recentDataHelper;
        this.updateStatsHelper = updateStatsHelper;
        this.habitContractUriBuilder = habitContractUriBuilder;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.rxMappingFunctionHelper = rxMappingFunctionHelper;
        this.dateHelper = dateHelper;
    }

    @Override
    public void subscribeToHabitDataAndBestData(long activityId) {

        ConnectableObservable<Cursor> observable = storIOContentResolverHelper.getCursorHabitDataOnMainThread(activityId)
                .publish();

        cursorSubscription = observable.subscribe(
                new Subscriber<Cursor>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        throw Exceptions.propagate(e);
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        view.renderHabitDataToList(cursor);
                    }
                }
        );

        listSubscription = observable
                .compose(cursorToDataPointListHelper.GetCursorToDataPointListMapFunction(null))
                .subscribe(new Subscriber<List<DataPoint[]>>() {
                               @Override
                               public void onCompleted() {}

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }

                               @Override
                               public void onNext(List<DataPoint[]> dataPoints) {
                                   view.renderHabitDataToGraph(dataPoints);
                               }
                           }
                );

        sharedSubscription = observable.connect();

        bestSubscription =
                storIOContentResolverHelper
                        .getObservableListOfObjectsActivitySettingsOnMainThread(activityId)
                        .subscribe(new Subscriber<List<ActivitySettings>>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                throw Exceptions.propagate(e);
                            }

                            @Override
                            public void onNext(List<ActivitySettings> activitySettingsList) {
                                ActivitySettings activitySettings = activitySettingsList.get(0);
                                view.renderBestData(activitySettings.title, activitySettings.best7, activitySettings.best30, activitySettings.best90);
                                view.currentForecastData(activitySettings.forecast);
                            }
                        });
    }

    // The user has changed the items that they have selected for multiselect update
    @Override
    public void ChecksChanged(ArrayList<Long> checkedItems) {
        if(checkedItems.size() > 0) {
            view.showMultiSelectDialog();
        }
        else {
            view.showGraph();
        }
    }

    @Override
    public void addMoreHistoryClicked() {
        view.showAddMoreHistoryDialog();
    }

    @Override
    public void addMoreHistoryDialogOKClicked(final long activityId, final int numberOfDaysToAdd) {

        view.showUpdatingDialog();

        Single<ArrayList<Long>> datesToAddSingle =
                storIOContentResolverHelper.getSingleListOfObjectsHabitDataOldestDateOnIOThread(activityId)
                        .map(rxMappingFunctionHelper.getHabitDataOldestDateToLongMappingFunction(numberOfDaysToAdd));

        Single<List<ActivitySettings>> settingsSingle =
                storIOContentResolverHelper.getSingleListofObjectsActivitySettingsOnIOThread(activityId);


        datesToAddSingle
                .zipWith(
                        settingsSingle,
                        rxMappingFunctionHelper.getLongActivitySettingsZipToUpdateHabitDataHelperParamsFunction()
                )
                .compose(
                        updateHabitDataHelper.getUpdateHabitDataTransformer()
                )
                .toCompletable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AsyncCompletableSubscriber() {
                               @Override
                               public void onCompleted() {
                                   view.hideUpdatingDialog();
                                   updateStatsHelper.UpdateStats(activityId);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }
                           }
                );
    }

    // Useful if the user is only updating one date
    @Override
    public void updateHabitDataClicked(long activityId, long dateToUpdate, float newVal) {
        ArrayList<Long> arrayList = new ArrayList<Long>(1);
        arrayList.add(dateToUpdate);
        updateHabitDataClicked(activityId, arrayList, newVal);
    }

    @Override
    public void updateHabitDataClicked(final long activityId, ArrayList<Long> datesToUpdate, float newValue) {
        //only show the dialog if we are changing a date more than 30 days old. That is when the most work needs to be done.
        // otherwise, the process should be so quick that the progress dialog only has an instant to show and is more distracting than useful.
        boolean showDialog = dateHelper.getTodaysDBDateIgnoreOffset() /*offset doesn't matter for this*/ -  datesToUpdate.get(0) > 30;

        if(showDialog){
            view.showUpdatingDialog();
        }

        Single.just(
                new UpdateHabitDataHelper.Params(
                        activityId,
                        datesToUpdate,
                        newValue,
                        HabitContract.HabitDataEntry.HabitValueType.USER))
                .observeOn(Schedulers.io())
                .compose(
                        updateHabitDataHelper.getUpdateHabitDataTransformer()
                )
                .toCompletable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AsyncCompletableSubscriber() {
                               @Override
                               public void onCompleted() {
                                   view.hideUpdatingDialog();
                                   updateStatsHelper.UpdateStats(activityId);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }
                           }
                );
    }

    @Override
    public void multiSelectCancelClicked(long activityId) {
        storIOContentResolverHelper.notifyChangeAtUri(habitContractUriBuilder.buildHabitDataUriForActivity(activityId));
    }

    @Override
    public void resetAllHabitData(final long activityId){
        storIOContentResolverHelper.getContentResolver().delete(
        HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(activityId), null, null);

        Cursor activitySettingsCursor = storIOContentResolverHelper.getContentResolver().query(
                habitContractUriBuilder.buildActivityUri(activityId),//Uri
                HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION,//projection
                null,//selection
                null,//selectionArgs
                null//Sort Order
        );
        activitySettingsCursor.moveToFirst();
        float historical = activitySettingsCursor.getFloat(HabitContract.ActivitySettingsQueryHelper.COLUMN_HISTORICAL);

        ContentValues contentValues = new ContentValues();
        contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST7, historical);
        contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST30, historical);
        contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST90, historical);

        storIOContentResolverHelper.getContentResolver().update(
                habitContractUriBuilder.buildActivityUri(activityId), contentValues, null, null
        );

        Observable.just(new RecentDataHelper.Params(
                activityId,
                historical,
                HabitContract.HabitDataEntry.HabitValueType.HISTORICAL))
                .compose(this.recentDataHelper.getRecentDataTransformer())
                .toCompletable()
                .subscribe(new AsyncCompletableSubscriber() {
                               @Override
                               public void onCompleted() {
                                   view.showHabitResetToast();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }
                           }
                );


    }

    // Since these subscriptions are ongoing (receiving updates to keep the UI updated)
    // make sure to unsubscribe or we'll leak Activities
    @Override
    public void unsubscribe() {
        if(cursorSubscription != null) {
            if(!cursorSubscription.isUnsubscribed()) {
                cursorSubscription.unsubscribe();
            }
        }
        if(listSubscription != null) {
            if(!listSubscription.isUnsubscribed()) {
                listSubscription.unsubscribe();
            }
        }
        if(bestSubscription != null) {
            if(!bestSubscription.isUnsubscribed()) {
                bestSubscription.unsubscribe();
            }
        }
        if(sharedSubscription != null) {
            if(!sharedSubscription.isUnsubscribed()) {
                sharedSubscription.unsubscribe();
            }
        }
    }
}
