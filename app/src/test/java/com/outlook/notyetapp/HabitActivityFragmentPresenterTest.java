package com.outlook.notyetapp;

import android.database.Cursor;
import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentPresenter;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGet;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGetCursor;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.internal.operators.OperatorPublish;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PreparedGet.Builder.class, PreparedGetCursor.Builder.class, PreparedGetCursor.CompleteBuilder.class, Observable.class, ConnectableObservable.class, OperatorPublish.class})
public class HabitActivityFragmentPresenterTest {
    private static final String MOCK_TITLE = "mock title";
    private static final int MOCK_HIGHER_IS_BETTER = 1;
    private static final float MOCK_BEST7 = 1.1f;
    private static final float MOCK_BEST30 = 1.2f;
    private static final float MOCK_BEST90 = 1.3f;

    @Mock
    HabitActivityFragmentContract.View view;

    @Mock
    CursorToDataPointListHelper cursorToDataPointListHelper;

    //Mocked by PowerMock
    StorIOContentResolver mockStorIOContentResolver;
    PreparedGet.Builder mockGetResult;
    PreparedGetCursor.Builder mockCursorResult;
    PreparedGetCursor.CompleteBuilder mockWithQueryResult;
    PreparedGetCursor mockPrepareResult;
    Observable mockObservable, mockObservable2;

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

        // Use PowerMock to create these mocks because some of them are "final" and mockito mocking doesn't work.
        mockStorIOContentResolver = PowerMockito.mock(StorIOContentResolver.class);
        mockGetResult = PowerMockito.mock(PreparedGet.Builder.class);
        mockCursorResult = PowerMockito.mock(PreparedGetCursor.Builder.class);
        mockWithQueryResult = PowerMockito.mock(PreparedGetCursor.CompleteBuilder.class);
        mockPrepareResult = PowerMockito.mock(PreparedGetCursor.class);
        mockObservable = PowerMockito.mock(Observable.class);
        mockObservable2 = PowerMockito.mock(Observable.class);

        habitActivityFragmentPresenter = new HabitActivityFragmentPresenter(this.view, this.mockStorIOContentResolver, this.cursorToDataPointListHelper);
    }

    @After
    public void after() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void loadHabitData() {
        Cursor cursor = mock(Cursor.class);
        List<DataPoint[]> dataPoints = Arrays.asList(new DataPoint[]{new DataPoint(5,5)}, new DataPoint[]{new DataPoint(6,6)});
        ConnectableObservable spyConnectableObservable = PowerMockito.spy(Observable.just(cursor).publish());

        doReturn(Observable.just(dataPoints))
                .when(spyConnectableObservable)
                .compose((rx.Observable.Transformer<Cursor, List<DataPoint[]>>)any());

        when(mockStorIOContentResolver.get()).thenReturn(mockGetResult);
        when(mockGetResult.cursor()).thenReturn(mockCursorResult);
        when(mockCursorResult.withQuery(any(Query.class))).thenReturn(mockWithQueryResult);
        when(mockWithQueryResult.prepare()).thenReturn(mockPrepareResult);
        when(mockPrepareResult.asRxObservable()).thenReturn(mockObservable);
        when(mockObservable.observeOn(any(Scheduler.class))).thenReturn(mockObservable2);
        when(mockObservable2.publish()).thenReturn(spyConnectableObservable);

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

        when(mockStorIOContentResolver.get()).thenReturn(mockGetResult);
        when(mockGetResult.cursor()).thenReturn(mockCursorResult);
        when(mockCursorResult.withQuery(any(Query.class))).thenReturn(mockWithQueryResult);
        when(mockWithQueryResult.prepare()).thenReturn(mockPrepareResult);
        when(mockPrepareResult.asRxObservable()).thenReturn(mockObservable);
        when(mockObservable.observeOn(any(Scheduler.class))).thenReturn(Observable.just(cursor));

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