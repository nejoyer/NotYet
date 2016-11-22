package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.outlook.notyetapp.R;
import com.outlook.notyetapp.RollingAverageHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// Code shared between HabitActivity and GraphActivity relating to graph manipulation.
public class GraphUtilities {

    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("MMM d");

    public static void AddTodayLine(GraphView graph, LineGraphSeries<DataPoint> todaySeries){
        graph.removeSeries(todaySeries);

        double minY = graph.getViewport().getMinY(false);
        double maxY = graph.getViewport().getMaxY(false);

        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(graph.getContext()).getString(graph.getContext().getString(R.string.pref_day_change_key), "0"));
        Date todayDate = HabitContract.HabitDataEntry.convertDBDateToDate(HabitContract.HabitDataEntry.getTodaysDBDate(offset));

        DataPoint[] todayPoints = new DataPoint[]{
                new DataPoint(todayDate, minY),
                new DataPoint(todayDate, maxY)
        };
        todaySeries.resetData(todayPoints);
        graph.addSeries(todaySeries);
    }

    public static List<DataPoint[]> UpdateSeriesData(Cursor data,
                                                         float forecastVal,
                                                         LineGraphSeries<DataPoint> valuesDataSeries,
                                                         LineGraphSeries<DataPoint> avg7DataSeries,
                                                         LineGraphSeries<DataPoint> avg30DataSeries,
                                                         LineGraphSeries<DataPoint> avg90DataSeries){

        int cursorCount = data.getCount();
        int dataPointCount = cursorCount + 90; // We want room for 90 days of forecasts

        DataPoint[] dataPoints = new DataPoint[dataPointCount];
        DataPoint[] avg7Points = new DataPoint[dataPointCount];
        DataPoint[] avg30Points = new DataPoint[dataPointCount];
        DataPoint[] avg90Points = new DataPoint[dataPointCount];

        RollingAverageHelper rollingAverageHelper = new RollingAverageHelper();


        long date = 0;
        Date dataPointDate;

        // The dates come in Most Recent to Oldest (DESC), this puts them in the DataPoint arrays in correct order
        int i = 0;
        data.moveToLast();
        do{
            date = data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
            dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);

            float val = data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
            dataPoints[i] = new DataPoint(dataPointDate, val);
            avg7Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7));
            avg30Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30));
            avg90Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90));
            rollingAverageHelper.PushNumber(val);
            i++;
        }while (data.moveToPrevious());

        //Load the forecast values
        for (int j = cursorCount; j < dataPointCount; j++)
        {
            date++;
            dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);
            rollingAverageHelper.PushNumber(forecastVal);
            dataPoints[j] = new DataPoint(dataPointDate, forecastVal);
            avg7Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage7());
            avg30Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage30());
            avg90Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage90());
        }

        List<DataPoint[]> toRet = Arrays.asList(dataPoints, avg7Points, avg30Points, avg90Points);
        valuesDataSeries.resetData(dataPoints);
        avg7DataSeries.resetData(avg7Points);
        avg30DataSeries.resetData(avg30Points);
        avg90DataSeries.resetData(avg90Points);

        // return the data points for one of the series. This is used to figure out what to set as the initial X axis. Doesn't matter which series.
        return toRet;
    }

    // Add the series to the graph and zoom the x axis as specified (presumably to show the last 90 days.
    public static void AddSeriesAndConfigureXScale(double minX,
                                                   double maxX,
                                                   final GraphView graph,
                                                   LineGraphSeries<DataPoint> valuesDataSeries,
                                                   LineGraphSeries<DataPoint> avg7DataSeries,
                                                   LineGraphSeries<DataPoint> avg30DataSeries,
                                                   LineGraphSeries<DataPoint> avg90DataSeries,
                                                   final LineGraphSeries<DataPoint> todaySeries)
    {
        Context context = graph.getContext();
        valuesDataSeries.setColor(ContextCompat.getColor(context, R.color.colorValues));
        avg7DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg7));
        avg30DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg30));
        avg90DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg90));
        todaySeries.setColor(Color.BLACK);

        graph.addSeries(valuesDataSeries);
        graph.addSeries(avg7DataSeries);
        graph.addSeries(avg30DataSeries);
        graph.addSeries(avg90DataSeries);

        graph.getViewport().setMinX(minX);
        graph.getViewport().setMaxX(maxX);
    }
}
