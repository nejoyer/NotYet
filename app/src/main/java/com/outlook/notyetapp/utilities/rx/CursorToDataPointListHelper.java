package com.outlook.notyetapp.utilities.rx;

import android.database.Cursor;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.RollingAverageHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

// Data comes from the DB in the form of a cursor.
// It is consumed by the ListView via a CursorAdapter, so that is good.
// But it can also be consumed by a graph which requires an array of DataPoints
// Use this transformer with RX to convert the cursor to DataPoints for graphing.
public class CursorToDataPointListHelper {

    private DateHelper mDateHelper;

    private DataPointFactory mDataPointFactory;

    private RollingAverageHelper mRollingAverageHelper;

    public CursorToDataPointListHelper(DateHelper dateHelper, DataPointFactory dataPointFactory, RollingAverageHelper rollingAverageHelper) {
        this.mDateHelper = dateHelper;
        this.mDataPointFactory = dataPointFactory;
        this.mRollingAverageHelper = rollingAverageHelper;
    }

    public CustomTransformerWithFloatParam<Cursor, List<DataPoint[]>> GetCursorToDataPointListMapFunction(Float forecast) {
        return new CustomTransformerWithFloatParam<Cursor, List<DataPoint[]>>(forecast) {
            @Override
            public Observable<List<DataPoint[]>> call(Observable<Cursor> source) {
                final Float forecast = this.mFloatVal;

                return source.map(
                        new Func1<Cursor, List<DataPoint[]>>() {
                            @Override
                            public List<DataPoint[]> call(Cursor cursor) {
                                int cursorCount = cursor.getCount();
                                if(cursorCount == 0){
                                    return null;
                                }

                                int dataPointCount;
                                //On the GraphActivity, we show 90 days of the future.
                                if(forecast != null) {
                                    dataPointCount = cursorCount + 90; // We want room for 90 days of forecasts
                                }//In HabitActivity, we don't need to show future, so the forecast value coming in is null
                                else {
                                    dataPointCount = cursorCount;
                                }

                                DataPoint[] dataPoints = new DataPoint[dataPointCount];
                                DataPoint[] avg7Points = new DataPoint[dataPointCount];
                                DataPoint[] avg30Points = new DataPoint[dataPointCount];
                                DataPoint[] avg90Points = new DataPoint[dataPointCount];

                                long date = 0;
                                Date dataPointDate;

                                // The dates come in Most Recent to Oldest (DESC), this puts them in the DataPoint arrays in correct order
                                int i = 0;
                                cursor.moveToLast();
                                do {
                                    date = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
                                    dataPointDate = mDateHelper.convertDBDateToDate(date);

                                    float val = cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
                                    dataPoints[i] = mDataPointFactory.getDataPoint(dataPointDate, val);
                                    avg7Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7));
                                    avg30Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30));
                                    avg90Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90));
                                    if(forecast != null) {
                                        mRollingAverageHelper.PushNumber(val);
                                    }
                                    i++;
                                } while (cursor.moveToPrevious());

                                //Load the forecast values
                                if(forecast != null)
                                    for (int j = cursorCount; j < dataPointCount; j++) {
                                        date++;
                                        dataPointDate = mDateHelper.convertDBDateToDate(date);
                                        mRollingAverageHelper.PushNumber(forecast);
                                        dataPoints[j] = mDataPointFactory.getDataPoint(dataPointDate, forecast);
                                        avg7Points[j] = mDataPointFactory.getDataPoint(dataPointDate, mRollingAverageHelper.GetAverage7());
                                        avg30Points[j] = mDataPointFactory.getDataPoint(dataPointDate, mRollingAverageHelper.GetAverage30());
                                        avg90Points[j] = mDataPointFactory.getDataPoint(dataPointDate, mRollingAverageHelper.GetAverage90());
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
        protected Float mFloatVal = null;

        public CustomTransformerWithFloatParam(Float floatVal) {
            mFloatVal = floatVal;
        }
    }
}
