package com.outlook.notyetapp;

import android.database.Cursor;
import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentPresenter;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Neil on 2/2/2017.
 */
public class HabitActivityFragmentPresenterTest {
    private static final String MOCK_TITLE = "mock title";
    private static final int MOCK_HIGHER_IS_BETTER = 1;
    private static final float MOCK_BEST7 = 1.1f;
    private static final float MOCK_BEST30 = 1.2f;
    private static final float MOCK_BEST90 = 1.3f;

    @Mock
    HabitActivityFragmentContract.View view;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    StorIOContentResolver storIOContentResolver;

    @Mock
    GraphUtilities graphUtilities;

    private HabitActivityFragmentPresenter habitActivityFragmentPresenter;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        habitActivityFragmentPresenter = new HabitActivityFragmentPresenter(this.view, this.storIOContentResolver, this.graphUtilities);
    }

    @After
    public void after() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void loadHabitData() {
        Cursor cursor = mock(Cursor.class);
        List<DataPoint[]> dataPoints = Arrays.asList(new DataPoint[]{new DataPoint(5,5)}, new DataPoint[]{new DataPoint(6,6)});
        ConnectableObservable spyConnectableObservable = spy(Observable.just(cursor).publish());

        doReturn(Observable.just(dataPoints))
                .when(spyConnectableObservable)
                .compose((rx.Observable.Transformer<Cursor, List<DataPoint[]>>)any());

        when(storIOContentResolver
                .get()
                .cursor()
                .withQuery(any(Query.class))
                .prepare()
                .asRxObservable()
                .observeOn(any(Scheduler.class))
                .publish()
        ).thenReturn(spyConnectableObservable);

        habitActivityFragmentPresenter.loadHabitData(mock(Uri.class), 1f);

        verify(view).renderHabitDataToGraph(dataPoints);
        verify(view).renderHabitDataToList(cursor);
    }

    @Test
    public void loadBestData() {

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);

        when(cursor.getString(HabitContract.ActivityBestQueryHelper.COLUMN_ACTIVITY_TITLE)).thenReturn(MOCK_TITLE);
        when(cursor.getInt(HabitContract.ActivityBestQueryHelper.COLUMN_HIGHER_IS_BETTER)).thenReturn(MOCK_HIGHER_IS_BETTER);
        when(cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST7)).thenReturn(MOCK_BEST7);
        when(cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST30)).thenReturn(MOCK_BEST30);
        when(cursor.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST90)).thenReturn(MOCK_BEST90);

        when(storIOContentResolver
                        .get()
                        .cursor()
                        .withQuery(any(Query.class))
                        .prepare()
                        .asRxObservable()
                .observeOn(any(Scheduler.class)))
                .thenReturn(Observable.just(cursor));

        habitActivityFragmentPresenter.loadBestData(mock(Uri.class));

        verify(view).renderBestData(MOCK_TITLE, true, MOCK_BEST7, MOCK_BEST30, MOCK_BEST90);
    }

    @Test
    public void checksChangedWithItemsTest(){
        ArrayList<Long> checkedItems = mock(ArrayList.class);
        when(checkedItems.size()).thenReturn(22);
        habitActivityFragmentPresenter.ChecksChanged(checkedItems);
        verify(view).showMultiSelectDialog();
        verify(view, never()).showGraph();
    }
    @Test
    public void checksChangedWithNoItemsTest(){
        ArrayList<Long> checkedItems = mock(ArrayList.class);
        when(checkedItems.size()).thenReturn(0);
        habitActivityFragmentPresenter.ChecksChanged(checkedItems);
        verify(view).showGraph();
        verify(view, never()).showMultiSelectDialog();
    }
}