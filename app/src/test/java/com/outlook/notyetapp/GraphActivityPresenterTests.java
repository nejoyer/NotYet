package com.outlook.notyetapp;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.library.TestException;
import com.outlook.notyetapp.screen.graph.GraphActivityContract;
import com.outlook.notyetapp.screen.graph.GraphActivityPresenter;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGet;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGetCursor;

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
import rx.Single;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.exceptions.OnErrorFailedException;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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

    @Mock
    StorIOContentResolverHelper mockStorIOContentResolverHelper;

    //Mocked by PowerMock
    Observable mockObservable, mockObservable2, mockObservable3;

    private GraphActivityPresenter graphActivityPresenter;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        // Use PowerMock to create these mocks because some of them are "final" and mockito mocking doesn't work.
        mockObservable = PowerMockito.mock(Observable.class);
        mockObservable2 = PowerMockito.mock(Observable.class);
        mockObservable3 = PowerMockito.mock(Observable.class);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        graphActivityPresenter = new GraphActivityPresenter(this.view, this.mockStorIOContentResolverHelper, this.cursorToDataPointListHelper);
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

        when(mockStorIOContentResolverHelper.getCursorHabitDataOnMainThread(anyLong())).thenReturn(mockObservable);
        when(mockObservable.first()).thenReturn(mockObservable2);
        when(mockObservable2.compose(any(Observable.Transformer.class))).thenReturn(mockObservable3);
        when(mockObservable3.toSingle()).thenReturn(Single.just(dataPoints));

        graphActivityPresenter.loadHabitData(25L /*doesn't matter*/, 1);
        verify(view).renderHabitData(dataPoints);
    }

    @Test
    public void loadHabitDataErrorPathTest(){
        //onError in our subscriber rethrows all errors because anything it errors on should be fatal.
        //This is the error that wraps anything that gets thrown.
        exception.expect(OnErrorFailedException.class);

        when(mockStorIOContentResolverHelper.getCursorHabitDataOnMainThread(anyLong())).thenReturn(mockObservable);
        when(mockObservable.first()).thenReturn(mockObservable2);
        when(mockObservable2.compose(any(Observable.Transformer.class))).thenReturn(mockObservable3);
        when(mockObservable3.toSingle()).thenReturn(Single.error(new TestException()));

        graphActivityPresenter.loadHabitData(25L /*doesn't matter*/, 1f);
        verify(view, never()).renderHabitData(null);
    }
}