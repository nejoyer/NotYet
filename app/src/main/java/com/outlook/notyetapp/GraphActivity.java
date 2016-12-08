package com.outlook.notyetapp;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// When you tap on the graph in the HabitActivity, you come to this full screen graph activity
public class GraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String ACTIVITY_ID_KEY = "activity_id";
    public static final String ACTIVITY_FORECAST_KEY = "activity_forecast";
    public static final String ACTIVITY_TITLE_KEY = "activity_title";

    public static final String BUNDLE_MIN_X_KEY = "min_x";
    public static final String BUNDLE_MAX_X_KEY = "max_x";

    public static double mMinX = 0;
    public static double mMaxX = 0;

    public long mActivityId;
    public float mForecast;
    public String mActivityTitle = "";

    private GraphView mGraph = null;
    private LineGraphSeries<DataPoint> mValuesDataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg7DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg30DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg90DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mTodaySeries = new LineGraphSeries<DataPoint>();
    private CustomLegendRenderer mCustomLegendRenderer;

    private HashMap<LineGraphSeries<DataPoint>, DataPoint[]> hiddenSeries = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>() {};
    private HashMap<LineGraphSeries<DataPoint>, DataPoint[]> visibleSeries = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>() {};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, getString(R.string.admob_appid));

        if(savedInstanceState != null){
            mMinX = savedInstanceState.getDouble(BUNDLE_MIN_X_KEY);
            mMaxX = savedInstanceState.getDouble(BUNDLE_MAX_X_KEY);
        }

        mActivityId = getIntent().getLongExtra(GraphActivity.ACTIVITY_ID_KEY, 0);
        mForecast = getIntent().getFloatExtra(GraphActivity.ACTIVITY_FORECAST_KEY, 0);
        mActivityTitle = getIntent().getStringExtra(GraphActivity.ACTIVITY_TITLE_KEY);

        setContentView(R.layout.activity_graph);

        setTitle(mActivityTitle);

        mGraph = (GraphView) findViewById(R.id.graph_graph);
        mCustomLegendRenderer = new CustomLegendRenderer(mGraph);
        mGraph.setLegendRenderer(mCustomLegendRenderer);

        // Make it so the user can toggle lines on the graph by clicking on the legend.
        mGraph.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int i = 0;
                if(event.getAction() == MotionEvent.ACTION_UP && event.getEventTime()- event.getDownTime() < 500 /* DEFAULT_LONG_PRESS_TIMEOUT */) {
                    for(CustomLegendRenderer.LegendMapping map : mCustomLegendRenderer.mLegendMapping){
                        if(map.mSeriesLegendRect.contains(event.getX(), event.getY())) {
                            toggleSeries(map);
                            return true;
                        }
                        i++;
                    }
                }
                return false;
            }
        });

        // When the section of the X axis in the viewport is changed, determin if we need "Today" marked on the graph.
        mGraph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
            @Override
            public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                double now = (double)System.currentTimeMillis();
                if(now > minX && now <= maxX){
                    GraphUtilities.AddTodayLine(mGraph, mTodaySeries);
                }
                else {
                    mGraph.removeSeries(mTodaySeries);
                }
            }
        });

        getSupportLoaderManager().initLoader(HabitContract.HabitDataQueryHelper.GRAPHDATA_LOADER, null, this);

        AdView adView = (AdView) findViewById(R.id.graph_banner_ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("***REMOVED***")
                .build();
        adView.loadAd(adRequest);
    }

    private void toggleSeries(CustomLegendRenderer.LegendMapping map)
    {
        if(map.mSeries.getColor() == Color.TRANSPARENT) {
            DataPoint[] data = hiddenSeries.remove(map.mSeries);
            if(data != null) {
                map.mSeries.resetData(data);
            }
            map.mSeries.setColor(map.mColor);
            visibleSeries.put(map.mSeries, data);
        } else {
            DataPoint[] realData = visibleSeries.remove(map.mSeries);
            hiddenSeries.put(map.mSeries, realData);

            if(visibleSeries.size() > 0) {
                Map.Entry<LineGraphSeries<DataPoint>, DataPoint[]> entry = visibleSeries.entrySet().iterator().next();
                DataPoint[] fakeData = entry.getValue();
                for(LineGraphSeries<DataPoint> series: hiddenSeries.keySet())
                {
                    series.resetData(fakeData);
                }
            }
            map.mColor = map.mSeries.getColor();
            map.mSeries.setColor(Color.TRANSPARENT);
        }
        mGraph.removeSeries(mTodaySeries);
        mGraph.invalidate();
        GraphUtilities.AddTodayLine(mGraph, mTodaySeries);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case HabitContract.HabitDataQueryHelper.GRAPHDATA_LOADER:
                return new CursorLoader(this,//context
                        HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(mActivityId),//Uri
                        HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION,//Projection
                        null,//Selection
                        null,//SelectionArgs
                        HabitContract.HabitDataQueryHelper.SORT_BY_DATE_DESC);//sortOrder
            default:
                throw new IllegalArgumentException("Invalid id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId())
        {
            case HabitContract.HabitDataQueryHelper.GRAPHDATA_LOADER:
                List<DataPoint[]> dataPoints = GraphUtilities.UpdateSeriesData(data, mForecast, mValuesDataSeries, mAvg7DataSeries, mAvg30DataSeries, mAvg90DataSeries);

                visibleSeries.put(mValuesDataSeries, dataPoints.get(0));
                visibleSeries.put(mAvg7DataSeries, dataPoints.get(1));
                visibleSeries.put(mAvg30DataSeries, dataPoints.get(2));
                visibleSeries.put(mAvg90DataSeries, dataPoints.get(3));

                DataPoint[] valDataPoints = dataPoints.get(0);
                if(mMinX == 0 && mMaxX == 0){
                    mMinX = valDataPoints[valDataPoints.length - 180].getX();
                    mMaxX = valDataPoints[valDataPoints.length - 90].getX();
                }

                GraphUtilities.AddSeriesAndConfigureXScale(mMinX,
                        mMaxX,
                        mGraph,
                        mValuesDataSeries,
                        mAvg7DataSeries,
                        mAvg30DataSeries,
                        mAvg90DataSeries,
                        mTodaySeries);

                mGraph.getViewport().setScrollable(true);
                mGraph.getViewport().setScalable(true);
                mGraph.getViewport().setXAxisBoundsManual(true);
                mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mGraph.getContext(), GraphUtilities.DateFormat));
                mGraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
                mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);

                mValuesDataSeries.setThickness(10);
                mAvg7DataSeries.setThickness(10);
                mAvg30DataSeries.setThickness(10);
                mAvg90DataSeries.setThickness(10);
                mTodaySeries.setThickness(10);

                mValuesDataSeries.setTitle(getString(R.string.val_column_label));
                mAvg7DataSeries.setTitle(getString(R.string.a7_column_label));
                mAvg30DataSeries.setTitle(getString(R.string.a30_column_label));
                mAvg90DataSeries.setTitle(getString(R.string.a90_column_label));
                mTodaySeries.setTitle(getString(R.string.today_label));
                mGraph.getLegendRenderer().setBackgroundColor(Color.WHITE);
                mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                mGraph.getLegendRenderer().setVisible(true);


                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    mGraph.getGridLabelRenderer().setNumHorizontalLabels(10);
                    mGraph.getGridLabelRenderer().setNumVerticalLabels(5);
                }
                else {// default to portrait
                    mGraph.getGridLabelRenderer().setNumHorizontalLabels(5);
                    mGraph.getGridLabelRenderer().setNumVerticalLabels(10);
                }
                mGraph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
                    @Override
                    public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                        double now = (double)System.currentTimeMillis();
                        if(now > minX && now <= maxX){
                            GraphUtilities.AddTodayLine(mGraph, mTodaySeries);
                        }
                        else {
                            mGraph.removeSeries(mTodaySeries);
                        }
                    }
                });
                GraphUtilities.AddTodayLine(mGraph, mTodaySeries);
                break;
            default:
                throw new IllegalArgumentException("Invalid id");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(BUNDLE_MIN_X_KEY, mGraph.getViewport().getMinX(false));
        outState.putDouble(BUNDLE_MAX_X_KEY, mGraph.getViewport().getMaxX(false));

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /* todo?*/
    }

    // When we leave the activity, clear this out.
    // Otherwise if you click the graph for a different activity, the viewport will be scoped inappropriately.
    @Override
    protected void onStop() {
        mMaxX = 0;
        mMinX = 0;
        super.onStop();
    }
}
