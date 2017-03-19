package com.outlook.notyetapp.screen.graph;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.Exceptions;

public class GraphActivityPresenter implements GraphActivityContract.ActionListener {

    private GraphActivityContract.View view;
    private StorIOContentResolverHelper storIOContentResolverHelper;
    CursorToDataPointListHelper cursorToDataPointListHelper;

    private Subscription subscription = null;

    public GraphActivityPresenter(GraphActivityContract.View view, StorIOContentResolverHelper storIOContentResolverHelper, CursorToDataPointListHelper cursorToDataPointListHelper) {
        this.view = view;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.cursorToDataPointListHelper = cursorToDataPointListHelper;
    }

    @Override
    public void loadHabitData(long activityId, final float forecast) {
        subscription = storIOContentResolverHelper.getCursorHabitDataOnMainThread(activityId)
                .first()
                .compose(cursorToDataPointListHelper.GetCursorToDataPointListMapFunction(forecast))
                .toSingle()
                .subscribe(new Subscriber<List<DataPoint[]>>() {
                               @Override
                               public void onCompleted() {}

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }

                               @Override
                               public void onNext(List<DataPoint[]> dataPoints) {
                                   view.renderHabitData(dataPoints);
                               }
                           }
                );
    }

    // If today's date is visible on the xAxis, the today line should be visible, otherwise no.
    // this allows autoscale on the Y axis to work corretly without the todayline data messing it up
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

