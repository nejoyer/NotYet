package com.outlook.notyetapp.screen.habit;

import android.database.Cursor;
import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract.ActionListener;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.observables.ConnectableObservable;

public class HabitActivityFragmentPresenter implements ActionListener {

    private HabitActivityFragmentContract.View view;
    private StorIOContentResolver storIOContentResolver;
    private CursorToDataPointListHelper cursorToDataPointListHelper;

    private Subscription cursorSubscription = null;
    private Subscription listSubscription = null;
    private Subscription bestSubscription = null;
    private Subscription sharedSubscription = null;

    public HabitActivityFragmentPresenter(HabitActivityFragmentContract.View view, StorIOContentResolver storIOContentResolver, CursorToDataPointListHelper cursorToDataPointListHelper) {
        this.view = view;
        this.storIOContentResolver = storIOContentResolver;
        this.cursorToDataPointListHelper = cursorToDataPointListHelper;
    }

    @Override
    public void loadHabitData(Uri habitDataUriForActivity, float forecast) {
        ConnectableObservable<Cursor> observable = storIOContentResolver
                .get()
                .cursor()
                .withQuery(
                        Query.builder()
                                .uri(habitDataUriForActivity)
                                .columns(HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION)
                                .sortOrder(HabitContract.HabitDataQueryHelper.SORT_BY_DATE_DESC)
                                .build()
                ).prepare()
                .asRxObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .publish();

        cursorSubscription = observable.subscribe(
                new Subscriber<Cursor>() {
                    @Override
                    public void onCompleted() {
//                        Log.v("onCompleted", "onCompleted");
                    }

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

        listSubscription = observable.compose(cursorToDataPointListHelper.GetCursorToDataPointListMapFunction(forecast))
                .subscribe(new Subscriber<List<DataPoint[]>>() {
                               @Override
                               public void onCompleted() {
//                                   Log.v("onCompleted", "onCompleted");
                               }

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }

                               @Override
                               public void onNext(List<DataPoint[]> dataPoints) {
                                   HabitActivityFragmentPresenter.this.view.renderHabitDataToGraph(dataPoints);
                               }
                           }
                );

        sharedSubscription = observable.connect();
    }

    @Override
    public void loadBestData(Uri activityUri) {
        bestSubscription = storIOContentResolver
                .get()
                .cursor()
                .withQuery(
                        Query.builder()
                                .uri(activityUri)
                                .columns(HabitContract.ActivityBestQueryHelper.ACTIVITY_BEST_PROJECTION)
                                .build()
                ).prepare().asRxObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Cursor>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        throw Exceptions.propagate(e);
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        if(cursor.moveToFirst()) {
                            String title = cursor.getString(HabitContract.ActivityBestQueryHelper.COLUMN_ACTIVITY_TITLE);
                            boolean higherIsBetter = cursor.getInt(HabitContract.ActivityBestQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1;
                            float best7 = cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST7);
                            float best30 = cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST30);
                            float best90 = cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST90);
                            view.renderBestData(title, higherIsBetter, best7, best30, best90);
                        }
                    }
                });
    }

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
