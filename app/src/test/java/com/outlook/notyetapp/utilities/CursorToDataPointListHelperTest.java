package com.outlook.notyetapp.utilities;

import android.database.Cursor;
import android.database.MatrixCursor;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.DateConverter;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.factories.DataPointFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Need this to be able to use MatrixCursor (mocking the matrix cursor is impractical).
// This is why the tests run slowly.
@RunWith(RobolectricTestRunner.class)
public class CursorToDataPointListHelperTest {

    @Mock
    Cursor mockCursor;

    DateConverter mockDateConverter;

    @Mock
    DataPointFactory mockDataPointFactory;

    CursorToDataPointListHelper cursorToDataPointListHelper;

    CursorToDataPointListHelper.CustomTransformerWithFloatParam transformerWithFloatParam;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        mockDateConverter = new DateConverter();
        cursorToDataPointListHelper = new CursorToDataPointListHelper(mockDateConverter, mockDataPointFactory);
        transformerWithFloatParam = cursorToDataPointListHelper.GetCursorToDataPointListMapFunction(100f);
    }

    @After
    public void after() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void getCursorToDataPointListMapFunctionTest() {

        when(mockDataPointFactory.getDataPoint(any(Date.class), anyDouble())).then(returnMockDataPoint());

        MatrixCursor matrixCursor = new MatrixCursor(HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION);
        matrixCursor.addRow(new Object[]{4, 4, 4, 4, 4, 4, 4}); //newest (think Day 4)
        matrixCursor.addRow(new Object[]{3, 3, 3, 3, 3, 3, 3});
        matrixCursor.addRow(new Object[]{2, 2, 2, 2, 2, 2, 2});
        matrixCursor.addRow(new Object[]{1, 1, 1, 1, 1, 1, 1}); //oldest (think Day 1)

        Observable observable = Observable.just(matrixCursor);
        TestSubscriber<List<DataPoint[]>> testSubscriber = new TestSubscriber<>();
        observable.compose(transformerWithFloatParam).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        List<List<DataPoint[]>> events = testSubscriber.getOnNextEvents();

        assertEquals("only one OnNext", 1, events.size());

        List<DataPoint[]> series = events.get(0);
        assertEquals("4 series of data", 4, series.size());
        DataPoint[] valSeries = series.get(0);
        DataPoint[] a7Series = series.get(1);
        DataPoint[] a30Series = series.get(2);
        DataPoint[] a90Series = series.get(3);
        assertEquals("valSeries has 94 datapoints, 4 initial, 90 forecast", 94, valSeries.length);
        assertEquals("a7Series has 94 datapoints, 4 initial, 90 forecast", 94, a7Series.length);
        assertEquals("a30Series has 94 datapoints, 4 initial, 90 forecast", 94, a30Series.length);
        assertEquals("a90Series has 94 datapoints, 4 initial, 90 forecast", 94, a90Series.length);

        //spot check some values
        assertEquals(1, valSeries[0].getY(), .00001);
        assertEquals(3, valSeries[2].getY(), .00001);
        assertEquals(100, valSeries[6].getY(), .00001);
        assertEquals(100, valSeries[46].getY(), .00001);
        assertEquals(100, valSeries[91].getY(), .00001);

        assertEquals(1, a7Series[0].getY(), .00001);
        assertEquals(3, a7Series[2].getY(), .00001);
        assertEquals(4, a7Series[3].getY(), .00001);
        assertEquals(44.28571319580078, a7Series[6].getY(), .00001);
        assertEquals(100, a7Series[46].getY(), .00001);
        assertEquals(100, a7Series[91].getY(), .00001);

        assertEquals(1, a30Series[0].getY(), .00001);
        assertEquals(3, a30Series[2].getY(), .00001);
        assertEquals(44.28571319580078, a30Series[6].getY(), .00001);
        assertEquals(96.80000305175781, a30Series[32].getY(), .00001);
        assertEquals(100, a30Series[35].getY(), .00001);
        assertEquals(100, a30Series[46].getY(), .00001);
        assertEquals(100, a30Series[91].getY(), .00001);

        assertEquals(1, a90Series[0].getY(), .00001);
        assertEquals(3, a90Series[2].getY(), .00001);
        assertEquals(44.28571319580078, a90Series[6].getY(), .00001);
        assertEquals(88.18181610107422, a90Series[32].getY(), .00001);
        assertEquals(89.16666412353516, a90Series[35].getY(), .00001);
        assertEquals(97.85555267333984, a90Series[91].getY(), .00001);
        assertEquals(100, a90Series[93].getY(), .00001);
    }

    public Answer<DataPoint> returnMockDataPoint(){
        return new Answer<DataPoint>() {
            @Override
            public DataPoint answer(InvocationOnMock invocation) throws Throwable {
                DataPoint mockDataPoint = mock(DataPoint.class);
                when(mockDataPoint.getX()).thenReturn((double)((Date)invocation.getArguments()[0]).getTime());
                when(mockDataPoint.getY()).thenReturn((double)invocation.getArguments()[1]);
                return mockDataPoint;
            }
        };
    }
}