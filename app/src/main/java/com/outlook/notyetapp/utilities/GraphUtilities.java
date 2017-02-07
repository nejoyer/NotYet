package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import com.jjoe64.graphview.series.Series;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.RollingAverageHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

// Code shared between HabitActivity and GraphActivity relating to graph manipulation.
public class GraphUtilities {

    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("MMM d");

//    public void AddTodayLine(GraphView graph, LineGraphSeries<DataPoint> todaySeries){
//        graph.removeSeries(todaySeries);
//
//        double minY = graph.getViewport().getMinY(false);
//        double maxY = graph.getViewport().getMaxY(false);
//
//        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(graph.getContext()).getString(graph.getContext().getString(R.string.pref_day_change_key), "0"));
//        Date todayDate = HabitContract.HabitDataEntry.convertDBDateToDate(HabitContract.HabitDataEntry.getTodaysDBDate(offset));
//
//        DataPoint[] todayPoints = new DataPoint[]{
//                new DataPoint(todayDate, minY),
//                new DataPoint(todayDate, maxY)
//        };
//        todaySeries.resetData(todayPoints);
//        graph.addSeries(todaySeries);
//    }

    public void ShowTodayLine(GraphView graphView, Date todayDate){
        ShowHideTodayLine(graphView, todayDate, false);
    }
    public void HideTodayLine(GraphView graphView, Date todayDate){
        ShowHideTodayLine(graphView, todayDate, true);
    }

    private void ShowHideTodayLine(GraphView graphView, Date todayDate, boolean hideTodayLine){
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
            todaySeries = new LineGraphSeries<DataPoint>();
            todaySeries.setTitle(todayLabel);
            todaySeries.setColor(Color.BLACK);
        }
        else {
            graphView.removeSeries(todaySeries);
        }
        if(hideTodayLine) {
            return;
        }

        double minY = graphView.getViewport().getMinY(false);
        double maxY = graphView.getViewport().getMaxY(false);

//        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_day_change_key), "0"));
//        Date todayDate = HabitContract.HabitDataEntry.convertDBDateToDate(HabitContract.HabitDataEntry.getTodaysDBDate(offset));

        DataPoint[] todayPoints = new DataPoint[]{
                new DataPoint(todayDate, minY),
                new DataPoint(todayDate, maxY)
        };
        todaySeries.resetData(todayPoints);
        graphView.addSeries(todaySeries);
    }

//    public void ColorSeries(GraphView graphView){
//        Context context = graphView.getContext();
//
//        for(Series<DataPoint> series : graphView.getSeries()){
//            String title = series.getTitle();
//
//            String valLabel = context.getString(R.string.val_column_label);
//            String a7Label = context.getString(R.string.a7_column_label);
//            String a30Label = context.getString(R.string.a30_column_label);
//            String a90Label = context.getString(R.string.a90_column_label);
//            String todayLabel = context.getString(R.string.today_label);
//
//
//            if(title.compareTo(valLabel) == 0)
//            {
//                ((LineGraphSeries<DataPoint>)series).setColor(ContextCompat.getColor(context, R.color.colorValues));
//            }
//            else if (title.compareTo(a7Label) == 0)
//            {
//                ((LineGraphSeries<DataPoint>)series).setColor(ContextCompat.getColor(context, R.color.colorAvg7));
//            }
//            else if (title.compareTo(a30Label) == 0)
//            {
//                ((LineGraphSeries<DataPoint>)series).setColor(ContextCompat.getColor(context, R.color.colorAvg30));
//            }
//            else if (title.compareTo(a90Label) == 0)
//            {
//                ((LineGraphSeries<DataPoint>)series).setColor(ContextCompat.getColor(context, R.color.colorAvg90));
//            }
//            else if (title.compareTo(todayLabel) == 0)
//            {
//                ((LineGraphSeries<DataPoint>)series).setColor(Color.BLACK);
//            }
//        }
//    }

    public void SetThickness(GraphView graphView, int i) {
        for(Series<DataPoint> series : graphView.getSeries()){
            ((LineGraphSeries<DataPoint>)series).setThickness(i);
        }
    }

    public HashMap<LineGraphSeries<DataPoint>, DataPoint[]> AddSeriesFromData(GraphView graphView, List<DataPoint[]> data)
    {
        HashMap<LineGraphSeries<DataPoint>, DataPoint[]> toRet = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>(4);

        Context context = graphView.getContext();

        LineGraphSeries<DataPoint> valSeries = new LineGraphSeries<DataPoint>(data.get(0));
        toRet.put(valSeries, data.get(0));
        valSeries.setTitle(context.getString(R.string.val_column_label));
        valSeries.setColor(ContextCompat.getColor(context, R.color.colorValues));
        LineGraphSeries<DataPoint> a7Series = new LineGraphSeries<DataPoint>(data.get(1));
        toRet.put(a7Series, data.get(1));
        a7Series.setTitle(context.getString(R.string.a7_column_label));
        a7Series.setColor(ContextCompat.getColor(context, R.color.colorAvg7));
        LineGraphSeries<DataPoint> a30Series = new LineGraphSeries<DataPoint>(data.get(2));
        toRet.put(a30Series, data.get(2));
        a30Series.setTitle(context.getString(R.string.a30_column_label));
        a30Series.setColor(ContextCompat.getColor(context, R.color.colorAvg30));
        LineGraphSeries<DataPoint> a90Series = new LineGraphSeries<DataPoint>(data.get(3));
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

    public CustomTransformerWithFloatParam<Cursor, List<DataPoint[]>> GetCursorToDataPointListMapFunction(float forecast) {
        return new CustomTransformerWithFloatParam<Cursor, List<DataPoint[]>>(forecast) {
            @Override
            public Observable<List<DataPoint[]>> call(Observable<Cursor> source) {
                final float forecast = this.mFloatVal;

                return source.map(
                        new Func1<Cursor, List<DataPoint[]>>() {
                            @Override
                            public List<DataPoint[]> call(Cursor cursor) {
                                int cursorCount = cursor.getCount();
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
                                cursor.moveToLast();
                                do {
                                    date = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
                                    dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);

                                    float val = cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
                                    dataPoints[i] = new DataPoint(dataPointDate, val);
                                    avg7Points[i] = new DataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7));
                                    avg30Points[i] = new DataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30));
                                    avg90Points[i] = new DataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90));
                                    rollingAverageHelper.PushNumber(val);
                                    i++;
                                } while (cursor.moveToPrevious());

                                //Load the forecast values
                                for (int j = cursorCount; j < dataPointCount; j++) {
                                    date++;
                                    dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);
                                    rollingAverageHelper.PushNumber(forecast);
                                    dataPoints[j] = new DataPoint(dataPointDate, forecast);
                                    avg7Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage7());
                                    avg30Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage30());
                                    avg90Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage90());
                                }

                                // The order here matters because it is used to set the titles in the graph
                                List<DataPoint[]> toRet = Arrays.asList(dataPoints, avg7Points, avg30Points, avg90Points);

                                // return the data points for one of the series. This is used to figure out what to set as the initial X axis. Doesn't matter which series.
                                return toRet;
                            }
                        }
                );
            }
        };
    }

    public abstract class CustomTransformerWithFloatParam<T,R> implements Observable.Transformer<T, R> {
        public float mFloatVal;

        public CustomTransformerWithFloatParam(float floatVal) {
            mFloatVal = floatVal;
        }
    }

//    public List<DataPoint[]> UpdateSeriesData(Cursor data,
//                                              float forecastVal,
//                                              LineGraphSeries<DataPoint> valuesDataSeries,
//                                              LineGraphSeries<DataPoint> avg7DataSeries,
//                                              LineGraphSeries<DataPoint> avg30DataSeries,
//                                              LineGraphSeries<DataPoint> avg90DataSeries){
//
//        int cursorCount = data.getCount();
//        int dataPointCount = cursorCount + 90; // We want room for 90 days of forecasts
//
//        DataPoint[] dataPoints = new DataPoint[dataPointCount];
//        DataPoint[] avg7Points = new DataPoint[dataPointCount];
//        DataPoint[] avg30Points = new DataPoint[dataPointCount];
//        DataPoint[] avg90Points = new DataPoint[dataPointCount];
//
//        RollingAverageHelper rollingAverageHelper = new RollingAverageHelper();
//
//
//        long date = 0;
//        Date dataPointDate;
//
//        // The dates come in Most Recent to Oldest (DESC), this puts them in the DataPoint arrays in correct order
//        int i = 0;
//        data.moveToLast();
//        do{
//            date = data.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
//            dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);
//
//            float val = data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
//            dataPoints[i] = new DataPoint(dataPointDate, val);
//            avg7Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7));
//            avg30Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30));
//            avg90Points[i] = new DataPoint(dataPointDate, data.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90));
//            rollingAverageHelper.PushNumber(val);
//            i++;
//        }while (data.moveToPrevious());
//
//        //Load the forecast values
//        for (int j = cursorCount; j < dataPointCount; j++)
//        {
//            date++;
//            dataPointDate = HabitContract.HabitDataEntry.convertDBDateToDate(date);
//            rollingAverageHelper.PushNumber(forecastVal);
//            dataPoints[j] = new DataPoint(dataPointDate, forecastVal);
//            avg7Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage7());
//            avg30Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage30());
//            avg90Points[j] = new DataPoint(dataPointDate, rollingAverageHelper.GetAverage90());
//        }
//
//        List<DataPoint[]> toRet = Arrays.asList(dataPoints, avg7Points, avg30Points, avg90Points);
//        valuesDataSeries.resetData(dataPoints);
//        avg7DataSeries.resetData(avg7Points);
//        avg30DataSeries.resetData(avg30Points);
//        avg90DataSeries.resetData(avg90Points);
//
//        // return the data points for one of the series. This is used to figure out what to set as the initial X axis. Doesn't matter which series.
//        return toRet;
//    }
//
//    // Add the series to the graph and zoom the x axis as specified (presumably to show the last 90 days.
//    public void AddSeriesAndConfigureXScale(double minX,
//                                            double maxX,
//                                            final GraphView graph,
//                                            LineGraphSeries<DataPoint> valuesDataSeries,
//                                            LineGraphSeries<DataPoint> avg7DataSeries,
//                                            LineGraphSeries<DataPoint> avg30DataSeries,
//                                            LineGraphSeries<DataPoint> avg90DataSeries,
//                                            final LineGraphSeries<DataPoint> todaySeries)
//    {
//        Context context = graph.getContext();
//        valuesDataSeries.setColor(ContextCompat.getColor(context, R.color.colorValues));
//        avg7DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg7));
//        avg30DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg30));
//        avg90DataSeries.setColor(ContextCompat.getColor(context, R.color.colorAvg90));
//        todaySeries.setColor(Color.BLACK);
//
//        graph.addSeries(valuesDataSeries);
//        graph.addSeries(avg7DataSeries);
//        graph.addSeries(avg30DataSeries);
//        graph.addSeries(avg90DataSeries);
//
//        graph.getViewport().setMinX(minX);
//        graph.getViewport().setMaxX(maxX);
//    }
}
