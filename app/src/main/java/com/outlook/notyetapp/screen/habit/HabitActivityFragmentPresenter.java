package com.outlook.notyetapp.screen.habit;

import android.database.Cursor;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract.ActionListener;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import java.util.ArrayList;
import java.util.List;

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
                                          UpdateStatsHelper updateStatsHelper,
                                          HabitContractUriBuilder habitContractUriBuilder,
                                          StorIOContentResolverHelper storIOContentResolverHelper,
                                          RXMappingFunctionHelper rxMappingFunctionHelper,
                                          DateHelper dateHelper) {
        this.view = view;
        this.cursorToDataPointListHelper = cursorToDataPointListHelper;
        this.updateHabitDataHelper = updateHabitDataHelper;
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
