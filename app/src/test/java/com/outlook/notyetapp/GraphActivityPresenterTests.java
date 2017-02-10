package com.outlook.notyetapp;

import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.library.TestException;
import com.outlook.notyetapp.screen.graph.GraphActivityContract;
import com.outlook.notyetapp.screen.graph.GraphActivityPresenter;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGet;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGetCursor;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.exceptions.OnErrorFailedException;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PreparedGet.Builder.class, PreparedGetCursor.Builder.class, PreparedGetCursor.CompleteBuilder.class, Observable.class})
public class GraphActivityPresenterTests {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    GraphActivityContract.View view;

    @Mock
    CursorToDataPointListHelper cursorToDataPointListHelper;

    //Mocked by PowerMock
    StorIOContentResolver mockStorIOContentResolver;
    PreparedGet.Builder mockGetResult;
    PreparedGetCursor.Builder mockCursorResult;
    PreparedGetCursor.CompleteBuilder mockWithQueryResult;
    PreparedGetCursor mockPrepareResult;
    Observable mockObservable;

    private GraphActivityPresenter graphActivityPresenter;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        // Use PowerMock to create these mocks because some of them are "final" and mockito mocking doesn't work.
        mockStorIOContentResolver = PowerMockito.mock(StorIOContentResolver.class);
        mockGetResult = PowerMockito.mock(PreparedGet.Builder.class);
        mockCursorResult = PowerMockito.mock(PreparedGetCursor.Builder.class);
        mockWithQueryResult = PowerMockito.mock(PreparedGetCursor.CompleteBuilder.class);
        mockPrepareResult = PowerMockito.mock(PreparedGetCursor.class);
        mockObservable = PowerMockito.mock(Observable.class);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        graphActivityPresenter = new GraphActivityPresenter(this.view, this.mockStorIOContentResolver, this.cursorToDataPointListHelper);
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

        //Powermock doesn't support "RETURNS_DEEP_STUBS" Answer, so need to mock the whole chain.
        when(mockStorIOContentResolver.get()).thenReturn(mockGetResult);
        when(mockGetResult.cursor()).thenReturn(mockCursorResult);
        when(mockCursorResult.withQuery(any(Query.class))).thenReturn(mockWithQueryResult);
        when(mockWithQueryResult.prepare()).thenReturn(mockPrepareResult);
        when(mockPrepareResult.asRxObservable()).thenReturn(mockObservable);
        when(mockObservable.compose(any(Observable.Transformer.class))).thenReturn(Observable.just(dataPoints));

        graphActivityPresenter.loadHabitData(mock(Uri.class), 1);
        verify(view).renderHabitData(dataPoints);
    }

    @Test
    public void loadHabitDataErrorPathTest(){
        //onError in our subscriber rethrows all errors because anything it errors on should be fatal.
        //This is the error that wraps anything that gets thrown.
        exception.expect(OnErrorFailedException.class);

        //Powermock doesn't support "RETURNS_DEEP_STUBS" Answer, so need to mock the whole chain.
        when(mockStorIOContentResolver.get()).thenReturn(mockGetResult);
        when(mockGetResult.cursor()).thenReturn(mockCursorResult);
        when(mockCursorResult.withQuery(any(Query.class))).thenReturn(mockWithQueryResult);
        when(mockWithQueryResult.prepare()).thenReturn(mockPrepareResult);
        when(mockPrepareResult.asRxObservable()).thenReturn(mockObservable);
        when(mockObservable.compose(any(Observable.Transformer.class))).thenReturn(Observable.error(new TestException()));

        graphActivityPresenter.loadHabitData(mock(Uri.class), 1f);
        verify(view, never()).renderHabitData(null);
    }
}