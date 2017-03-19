package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.factories.DataPointFactory;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContextCompat.class})
public class GraphUtilitiesTest {

    GraphUtilities graphUtilities = null;

    private String FAKE_TODAY_STRING = "fake today string";
    private String FAKE_VAL_STRING = "fake val string";
    private String FAKE_A7_STRING = "fake a7 string";
    private String FAKE_A30_STRING = "fake a30 string";
    private String FAKE_A90_STRING = "fake a90 string";
    private String FAKE_STRING = "title";

    @Mock
    LineGraphSeriesFactory mockLineGraphSeriesFactory;

    @Mock
    DateHelper mockDateHelper;

    @Mock
    GraphView mockGraphView;

    @Mock
    Viewport mockViewport;

    @Mock
    Context mockContext;

    @Mock
    LineGraphSeries<DataPoint> mockValSeries, mockA7Series, mockTodaySeries, mockA30Series, mockA90Series;

    DataPointFactory dataPointFactory;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        dataPointFactory = new DataPointFactory();
        graphUtilities = new GraphUtilities(mockLineGraphSeriesFactory, dataPointFactory, mockDateHelper);
    }

    @Test
    public void HideTodayLineTestRemove(){
        List<Series> serieses = new ArrayList<Series>();

        when(mockTodaySeries.getTitle()).thenReturn(FAKE_TODAY_STRING);

        when(mockValSeries.getTitle()).thenReturn(FAKE_STRING);
        when(mockA7Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA30Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA90Series.getTitle()).thenReturn(FAKE_STRING);

        serieses.add(mockValSeries);
        serieses.add(mockA7Series);
        serieses.add(mockTodaySeries);
        serieses.add(mockA30Series);
        serieses.add(mockA90Series);

        when(mockContext.getString(R.string.today_label)).thenReturn(FAKE_TODAY_STRING);
        when(mockGraphView.getContext()).thenReturn(mockContext);
        when(mockGraphView.getSeries()).thenReturn(serieses);

        graphUtilities.HideTodayLine(mockGraphView);

        verify(mockGraphView, times(1)).removeSeries(mockTodaySeries);
        verify(mockGraphView, never()).addSeries((Series)any());
        verify(mockGraphView, never()).getViewport();
    }

    @Test
    public void HideTodayLineTestNull(){
        List<Series> serieses = new ArrayList<Series>();

        when(mockLineGraphSeriesFactory.getLineGraphSeries()).thenReturn(mockTodaySeries);
        when(mockValSeries.getTitle()).thenReturn(FAKE_STRING);
        when(mockA7Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA30Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA90Series.getTitle()).thenReturn(FAKE_STRING);

        serieses.add(mockValSeries);
        serieses.add(mockA7Series);
        serieses.add(mockA30Series);
        serieses.add(mockA90Series);

        when(mockContext.getString(R.string.today_label)).thenReturn(FAKE_TODAY_STRING);
        when(mockGraphView.getContext()).thenReturn(mockContext);
        when(mockGraphView.getSeries()).thenReturn(serieses);

        graphUtilities.HideTodayLine(mockGraphView);

        verify(mockGraphView, never()).removeSeries((Series)any());
        verify(mockGraphView, never()).addSeries((Series)any());
        verify(mockGraphView, never()).getViewport();
        verify(mockTodaySeries, never()).setTitle(anyString());
        verify(mockTodaySeries, never()).setColor(anyInt());
    }

    @Test
    public void ShowTodayLineTestRepealAndReplace(){
        List<Series> serieses = new ArrayList<Series>();

        Date todayDate = new Date(System.currentTimeMillis());
        Double minY = 22d;
        Double maxY = 55d;

        DataPoint[] todayPoints = new DataPoint[]{
                new DataPoint(todayDate, minY),
                new DataPoint(todayDate, maxY)
        };

        when(mockGraphView.getViewport()).thenReturn(mockViewport);
        when(mockViewport.getMinY(false)).thenReturn(minY);
        when(mockViewport.getMaxY(false)).thenReturn(maxY);

        when(mockTodaySeries.getTitle()).thenReturn(FAKE_TODAY_STRING);
        when(mockValSeries.getTitle()).thenReturn(FAKE_STRING);
        when(mockA7Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA30Series.getTitle()).thenReturn(FAKE_STRING);
        when(mockA90Series.getTitle()).thenReturn(FAKE_STRING);

        serieses.add(mockValSeries);
        serieses.add(mockA7Series);
        serieses.add(mockTodaySeries);
        serieses.add(mockA30Series);
        serieses.add(mockA90Series);

        when(mockContext.getString(R.string.today_label)).thenReturn(FAKE_TODAY_STRING);
        when(mockGraphView.getContext()).thenReturn(mockContext);
        when(mockGraphView.getSeries()).thenReturn(serieses);

        when(mockDateHelper.getTodaysDBDateAsDate()).thenReturn(new Date());

        graphUtilities.ShowTodayLine(mockGraphView);

        InOrder inOrder = inOrder(mockGraphView, mockTodaySeries);
        inOrder.verify(mockTodaySeries, times(1)).resetData(argThat(isDataPointArrayThatMatches(todayPoints)));
        inOrder.verify(mockGraphView, times(1)).addSeries(mockTodaySeries);
    }

    private Matcher<DataPoint[]> isDataPointArrayThatMatches(final DataPoint[] dataPointArray) {
        return new ArgumentMatcher<DataPoint[]>() {
            public boolean matches(Object compareTo) {
                DataPoint[] compareToDP = (DataPoint[])compareTo;
                if(compareToDP.length != dataPointArray.length){
                    return false;
                }

                for(int i = 0; i < compareToDP.length; i++)
                {
                    if(compareToDP[i].getX() != dataPointArray[i].getX() && compareToDP[i].getY() != dataPointArray[i].getY())
                    {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @Test
    public void AddSeriesFromDataTest() {
        mockStatic(ContextCompat.class);

        List<DataPoint[]> dataPoints = new ArrayList(4);
        dataPoints.add(new DataPoint[1]);
        dataPoints.add(new DataPoint[1]);
        dataPoints.add(new DataPoint[1]);
        dataPoints.add(new DataPoint[1]);

        when(mockGraphView.getContext()).thenReturn(mockContext);
        when(mockContext.getString(R.string.val_column_label)).thenReturn(FAKE_VAL_STRING);
        when(mockContext.getString(R.string.a7_column_label)).thenReturn(FAKE_A7_STRING);
        when(mockContext.getString(R.string.a30_column_label)).thenReturn(FAKE_A30_STRING);
        when(mockContext.getString(R.string.a90_column_label)).thenReturn(FAKE_A90_STRING);

        when(ContextCompat.getColor(eq(mockContext), anyInt())).thenReturn(1,2,3,4);

        when(mockLineGraphSeriesFactory.getLineGraphSeries(any(DataPoint[].class))).thenReturn(mockValSeries, mockA7Series, mockA30Series, mockA90Series);

        HashMap<LineGraphSeries<DataPoint>, DataPoint[]> retval = graphUtilities.AddSeriesFromData(mockGraphView, dataPoints);

        InOrder inOrder = inOrder(mockValSeries, mockA7Series, mockA30Series, mockA90Series, mockGraphView);
        
        inOrder.verify(mockValSeries).setTitle(FAKE_VAL_STRING);
        inOrder.verify(mockValSeries).setColor(1);
        inOrder.verify(mockA7Series).setTitle(FAKE_A7_STRING);
        inOrder.verify(mockA7Series).setColor(2);
        inOrder.verify(mockA30Series).setTitle(FAKE_A30_STRING);
        inOrder.verify(mockA30Series).setColor(3);
        inOrder.verify(mockA90Series).setTitle(FAKE_A90_STRING);
        inOrder.verify(mockA90Series).setColor(4);
        
        inOrder.verify(mockGraphView).removeAllSeries();
        
        inOrder.verify(mockGraphView).addSeries(mockValSeries);
        inOrder.verify(mockGraphView).addSeries(mockA7Series);
        inOrder.verify(mockGraphView).addSeries(mockA30Series);
        inOrder.verify(mockGraphView).addSeries(mockA90Series);

        assertTrue(retval.containsKey(mockValSeries));
        assertEquals(retval.get(mockValSeries), dataPoints.get(0));
        assertTrue(retval.containsKey(mockA7Series));
        assertEquals(retval.get(mockA7Series), dataPoints.get(1));
        assertTrue(retval.containsKey(mockA30Series));
        assertEquals(retval.get(mockA30Series), dataPoints.get(2));
        assertTrue(retval.containsKey(mockA90Series));
        assertEquals(retval.get(mockA90Series), dataPoints.get(3));
    }
}