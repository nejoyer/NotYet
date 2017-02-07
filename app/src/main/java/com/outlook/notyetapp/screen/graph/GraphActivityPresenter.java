package com.outlook.notyetapp.screen.graph;

import android.net.Uri;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;


/**
 * Created by Neil on 1/16/2017.
 */

public class GraphActivityPresenter implements GraphActivityContract.ActionListener {

    private GraphActivityContract.View view;
    private StorIOContentResolver storIOContentResolver;
    GraphUtilities graphUtilities;

    private Subscription subscription = null;

    public GraphActivityPresenter(GraphActivityContract.View view, StorIOContentResolver storIOContentResolver, GraphUtilities graphUtilities) {
        this.view = view;
        this.storIOContentResolver = storIOContentResolver;
        this.graphUtilities = graphUtilities;
    }

    @Override
    public void loadHabitData(Uri habitDataUriForActivity, final float forecast) {
        subscription = storIOContentResolver
                .get()
                .cursor()
                .withQuery(
                        Query.builder()
                                .uri(habitDataUriForActivity)
                                .columns(HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION)
                                .sortOrder(HabitContract.HabitDataQueryHelper.SORT_BY_DATE_DESC)
                                .build()
                ).prepare().asRxObservable()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .compose(graphUtilities.GetCursorToDataPointListMapFunction(forecast))
                .subscribeOn(AndroidSchedulers.mainThread())
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
                                   GraphActivityPresenter.this.view.renderHabitData(dataPoints);
                               }
                           }
                );
    }

    @Override
    public void xAxisChanged(double minX, double maxX) {
        double now = (double)System.currentTimeMillis();
        if(now > minX && now <= maxX){
            view.showTodayLine();
        }
        else {
            view.hideTodayLine();
        }
    }

    @Override
    public void unsubscribe() {
        if(subscription != null) {
            if(!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }
}

