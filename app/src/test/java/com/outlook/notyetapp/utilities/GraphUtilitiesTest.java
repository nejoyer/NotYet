package com.outlook.notyetapp.utilities;

import android.content.Context;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.outlook.notyetapp.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphUtilitiesTest {

    GraphUtilities graphUtilities = null;

    private String FAKE_TODAY_STRING = "today";
    private String FAKE_STRING = "title";

    @Mock
    GraphView graphView;

    @Mock
    Context context;

    @Mock
    LineGraphSeries<DataPoint> mockValSeries, mockA7Series, mockTodaySeries, mockA30Series, mockA90Series;


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        graphUtilities = new GraphUtilities();
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



        when(context.getString(R.string.today_label)).thenReturn(FAKE_TODAY_STRING);
        when(graphView.getContext()).thenReturn(context);
        when(graphView.getSeries()).thenReturn(serieses);

        graphUtilities.HideTodayLine(graphView, new Date(System.currentTimeMillis()));

        verify(graphView, times(1)).removeSeries(mockTodaySeries);
        verify(graphView, never()).addSeries((Series)any());
        verify(graphView, never()).getViewport();
    }

//    @Test
//    public void HideTodayLineTestNull(){
//        List<Series> serieses = new ArrayList<Series>();
//
////        when(mockTodaySeries.getTitle()).thenReturn(FAKE_TODAY_STRING);
//
//        when(mockValSeries.getTitle()).thenReturn(FAKE_STRING);
//        when(mockA7Series.getTitle()).thenReturn(FAKE_STRING);
//        when(mockA30Series.getTitle()).thenReturn(FAKE_STRING);
//        when(mockA90Series.getTitle()).thenReturn(FAKE_STRING);
//
//        serieses.add(mockValSeries);
//        serieses.add(mockA7Series);
////        serieses.add(mockTodaySeries);
//        serieses.add(mockA30Series);
//        serieses.add(mockA90Series);
//
//
//
//        when(context.getString(R.string.today_label)).thenReturn(FAKE_TODAY_STRING);
//        when(graphView.getContext()).thenReturn(context);
//        when(graphView.getSeries()).thenReturn(serieses);
//
//        graphUtilities.HideTodayLine(graphView, new Date(System.currentTimeMillis()));
//
//        verify(graphView, never()).removeSeries((Series)any());
//        verify(graphView, never()).addSeries((Series)any());
//        verify(graphView, never()).getViewport();
//    }
}