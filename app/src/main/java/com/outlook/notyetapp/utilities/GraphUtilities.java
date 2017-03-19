package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.factories.DataPointFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// Code shared between HabitActivity and GraphActivity relating to graph manipulation.
public class GraphUtilities {

    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("MMM d");

    private static final int LINE_THICKNESS = 10;

    // Use this to generate series so that it can be mocked in UnitTests
    private LineGraphSeriesFactory lineGraphSeriesFactory;
    private DataPointFactory dataPointFactory;
    private DateHelper dateHelper;

    public GraphUtilities(LineGraphSeriesFactory lineGraphSeriesFactory, DataPointFactory dataPointFactory, DateHelper dateHelper) {
        this.lineGraphSeriesFactory = lineGraphSeriesFactory;
        this.dataPointFactory = dataPointFactory;
        this.dateHelper = dateHelper;
    }

    public void ShowTodayLine(GraphView graphView){
        ShowHideTodayLine(graphView, false);
    }
    public void HideTodayLine(GraphView graphView){
        ShowHideTodayLine(graphView, true);
    }

    // Show or hide the TodayLine on the graph.
    private void ShowHideTodayLine(GraphView graphView, boolean hideTodayLine){
        LineGraphSeries<DataPoint> todaySeries = null;
        Context context = graphView.getContext();
        String todayLabel = context.getString(R.string.today_label);

        for(Series<DataPoint> series : graphView.getSeries()){
            if(series.getTitle().compareTo(todayLabel) == 0)
            {
                todaySeries = (LineGraphSeries<DataPoint>)series;
                break;
            }
        }
        if(todaySeries == null)
        {
            if(!hideTodayLine) {
                todaySeries = this.lineGraphSeriesFactory.getLineGraphSeries();
                todaySeries.setTitle(todayLabel);
                todaySeries.setColor(Color.BLACK);
                todaySeries.setThickness(LINE_THICKNESS);
            }
        }
        else {
            graphView.removeSeries(todaySeries);
        }
        if(hideTodayLine) {
            return;
        }

        graphView.getViewport().calcCompleteRange();

        double minY = graphView.getViewport().getMinY(false);
        double maxY = graphView.getViewport().getMaxY(false);

        Date todayDate = dateHelper.getTodaysDBDateAsDate();

        DataPoint[] todayPoints = new DataPoint[]{
                dataPointFactory.getDataPoint(todayDate, minY),
                dataPointFactory.getDataPoint(todayDate, maxY)
        };
        todaySeries.resetData(todayPoints);
        graphView.addSeries(todaySeries);
    }

    public void SetThickness(GraphView graphView) {
        for(Series<DataPoint> series : graphView.getSeries()){
            ((LineGraphSeries<DataPoint>)series).setThickness(LINE_THICKNESS);
        }
    }

    public HashMap<LineGraphSeries<DataPoint>, DataPoint[]> AddSeriesFromData(GraphView graphView, List<DataPoint[]> data)
    {
        HashMap<LineGraphSeries<DataPoint>, DataPoint[]> toRet = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>(4);

        Context context = graphView.getContext();

        LineGraphSeries<DataPoint> valSeries = lineGraphSeriesFactory.getLineGraphSeries(data.get(0));
        toRet.put(valSeries, data.get(0));
        valSeries.setTitle(context.getString(R.string.val_column_label));
        valSeries.setColor(ContextCompat.getColor(context, R.color.colorValues));
        LineGraphSeries<DataPoint> a7Series = lineGraphSeriesFactory.getLineGraphSeries(data.get(1));
        toRet.put(a7Series, data.get(1));
        a7Series.setTitle(context.getString(R.string.a7_column_label));
        a7Series.setColor(ContextCompat.getColor(context, R.color.colorAvg7));
        LineGraphSeries<DataPoint> a30Series = lineGraphSeriesFactory.getLineGraphSeries(data.get(2));
        toRet.put(a30Series, data.get(2));
        a30Series.setTitle(context.getString(R.string.a30_column_label));
        a30Series.setColor(ContextCompat.getColor(context, R.color.colorAvg30));
        LineGraphSeries<DataPoint> a90Series = lineGraphSeriesFactory.getLineGraphSeries(data.get(3));
        toRet.put(a90Series, data.get(3));
        a90Series.setTitle(context.getString(R.string.a90_column_label));
        a90Series.setColor(ContextCompat.getColor(context, R.color.colorAvg90));

        graphView.removeAllSeries();

        graphView.addSeries(valSeries);
        graphView.addSeries(a7Series);
        graphView.addSeries(a30Series);
        graphView.addSeries(a90Series);

        return toRet;
    }
}
