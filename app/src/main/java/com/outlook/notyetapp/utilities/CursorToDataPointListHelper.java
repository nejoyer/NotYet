package com.outlook.notyetapp.utilities;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.RollingAverageHelper;
import com.outlook.notyetapp.data.DateConverter;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.factories.DataPointFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class CursorToDataPointListHelper {

    public CursorToDataPointListHelper(DateConverter dateConverter, DataPointFactory dataPointFactory) {
        this.mDateConverter = dateConverter;
        this.mDataPointFactory = dataPointFactory;
    }

    private DateConverter mDateConverter;

    private DataPointFactory mDataPointFactory;

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
                                    dataPointDate = mDateConverter.convertDBDateToDate(date);

                                    float val = cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE);
                                    dataPoints[i] = mDataPointFactory.getDataPoint(dataPointDate, val);
                                    avg7Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7));
                                    avg30Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30));
                                    avg90Points[i] = mDataPointFactory.getDataPoint(dataPointDate, cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90));
                                    rollingAverageHelper.PushNumber(val);
                                    i++;
                                } while (cursor.moveToPrevious());

                                //Load the forecast values
                                for (int j = cursorCount; j < dataPointCount; j++) {
                                    date++;
                                    dataPointDate = mDateConverter.convertDBDateToDate(date);
                                    rollingAverageHelper.PushNumber(forecast);
                                    dataPoints[j] = mDataPointFactory.getDataPoint(dataPointDate, forecast);
                                    avg7Points[j] = mDataPointFactory.getDataPoint(dataPointDate, rollingAverageHelper.GetAverage7());
                                    avg30Points[j] = mDataPointFactory.getDataPoint(dataPointDate, rollingAverageHelper.GetAverage30());
                                    avg90Points[j] = mDataPointFactory.getDataPoint(dataPointDate, rollingAverageHelper.GetAverage90());
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
        protected float mFloatVal;

        public CustomTransformerWithFloatParam(float floatVal) {
            mFloatVal = floatVal;
        }
    }
}
