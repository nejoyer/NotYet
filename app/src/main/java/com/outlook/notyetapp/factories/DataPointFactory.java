package com.outlook.notyetapp.factories;

import com.jjoe64.graphview.series.DataPoint;

import java.util.Date;

// Used to allow unit tests to mock datapoints
public class DataPointFactory {
    public DataPoint getDataPoint(double x, double y){
        return new DataPoint(x, y);
    }
    public DataPoint getDataPoint(Date x, double y){
        return new DataPoint(x, y);
    }
}
