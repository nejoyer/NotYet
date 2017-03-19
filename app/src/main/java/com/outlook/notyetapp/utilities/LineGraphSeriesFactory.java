package com.outlook.notyetapp.utilities;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

// Factory for creating LineGraphSeries. Created to make unit testing easier (possible).
public class LineGraphSeriesFactory {
    public LineGraphSeries<DataPoint> getLineGraphSeries(){
        return new LineGraphSeries<DataPoint>();
    }
    public LineGraphSeries<DataPoint> getLineGraphSeries(DataPoint[] data){
        return new LineGraphSeries<DataPoint>(data);
    }
}
