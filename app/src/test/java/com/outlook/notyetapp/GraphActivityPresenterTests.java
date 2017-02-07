package com.outlook.notyetapp;

import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.screen.graph.GraphActivityContract;
import com.outlook.notyetapp.screen.graph.GraphActivityPresenter;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class GraphActivityPresenterTests {

    @Mock
    GraphActivityContract.View view;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    StorIOContentResolver storIOContentResolver;

    @Mock
    GraphUtilities graphUtilities;

    private GraphActivityPresenter graphActivityPresenter;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        graphActivityPresenter = new GraphActivityPresenter(this.view, this.storIOContentResolver, this.graphUtilities);
    }

    @After
    public void after() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void xAxisChangedLowTest(){
        graphActivityPresenter.xAxisChanged(2f, 4f);
        verify(view).hideTodayLine();
        verify(view, never()).showTodayLine();
    }

    @Test
    public void xAxisChangedHighTest(){
        // AxisChanged to only show dates way in the future (rather than trying to mock static System instance).
        graphActivityPresenter.xAxisChanged(3333333333332f, 3333333333333f);
        verify(view).hideTodayLine();
        verify(view, never()).showTodayLine();
    }

    @Test
    public void xAxisChangedGoldilocksTest(){
        // AxisChanged to only show all date in the this geologic age. (rather than trying to mock static System instance).
        graphActivityPresenter.xAxisChanged(1111111111111f, 3333333333333f);
        verify(view).showTodayLine();
        verify(view, never()).hideTodayLine();
    }

    @Test
    public void loadHabitDataHappyPathTest(){
        List<DataPoint[]> dataPoints = Arrays.asList(new DataPoint[]{new DataPoint(5,5)}, new DataPoint[]{new DataPoint(6,6)});

        when(storIOContentResolver
                .get()
                .cursor()
                .withQuery(any(Query.class))
                .prepare()
                .asRxObservable()
                .observeOn(any(Scheduler.class))
                .subscribeOn(any(Scheduler.class))
                .compose(any(Observable.Transformer.class)) //Transformer should be tested as part of GraphUtilities tests.
                .subscribeOn(any(Scheduler.class))).thenReturn(Observable.just(dataPoints));

        graphActivityPresenter.loadHabitData(mock(Uri.class), 1);
        verify(view).renderHabitData(dataPoints);
    }

    @Test(expected=Exception.class)
    public void loadHabitDataErrorPathTest(){
        when(storIOContentResolver
                .get()
                .cursor()
                .withQuery(any(Query.class))
                .prepare()
                .asRxObservable()
                .observeOn(any(Scheduler.class))
                .subscribeOn(any(Scheduler.class))
                .compose(any(Observable.Transformer.class))
                .subscribeOn(any(Scheduler.class))).thenReturn(Observable.error(new Exception("error")));

        graphActivityPresenter.loadHabitData(mock(Uri.class), 1);
        verify(view, never()).renderHabitData(null);
    }
}