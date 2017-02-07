package com.outlook.notyetapp.screen.graph;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.outlook.notyetapp.CustomLegendRenderer;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

// When you tap on the graph in the HabitActivity, you come to this full screen graph activity
//public class GraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GraphActivityContract.View{
public class GraphActivity extends AppCompatActivity implements GraphActivityContract.View{

    public static final String ACTIVITY_ID_KEY = "activity_id";
    public static final String ACTIVITY_FORECAST_KEY = "activity_forecast";
    public static final String ACTIVITY_TITLE_KEY = "activity_title";

    public static final String BUNDLE_MIN_X_KEY = "min_x";
    public static final String BUNDLE_MAX_X_KEY = "max_x";

    public static double mMinX = 0;
    public static double mMaxX = 0;

    private Date todayDate = null;

    public long mActivityId;
    public float mForecast;
    public String mActivityTitle = "";

    @BindView(R.id.graph_graph)
    public GraphView mGraph;
    private CustomLegendRenderer mCustomLegendRenderer;

    private HashMap<LineGraphSeries<DataPoint>, DataPoint[]> hiddenSeries = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>() {};
    private HashMap<LineGraphSeries<DataPoint>, DataPoint[]> visibleSeries = new HashMap<LineGraphSeries<DataPoint>, DataPoint[]>() {};

    public GraphActivityPresenter mPresenter;

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

        ButterKnife.bind(this);

        GraphUtilities graphUtilities = new GraphUtilities();
        //todo DI
        mPresenter = new GraphActivityPresenter(this,
                DefaultStorIOContentResolver.builder().contentResolver(this.getContentResolver()).build(),
                graphUtilities
        );
        mPresenter.loadHabitData(HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(mActivityId), mForecast);

        setTitle(mActivityTitle);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent(AnalyticsConstants.EventNames.GRAPH_ACTIVITY, new Bundle());

        mCustomLegendRenderer = new CustomLegendRenderer(mGraph);
        mGraph.setLegendRenderer(mCustomLegendRenderer);

        // Make it so the user can toggle lines on the graph by clicking on the legend.
        // This isn't strictly MVP... but passing the call through the presenter seems extremely pointless since there is zero buiness logic it could deal with.
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

        // When the section of the X axis in the viewport is changed, determine if we need "Today" marked on the graph.
        mGraph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
            @Override
            public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                mPresenter.xAxisChanged(minX, maxX);
            }
        });

        AdView adView = (AdView) findViewById(R.id.graph_banner_ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("***REMOVED***")
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    public void showTodayLine() {
        GraphUtilities gu = new GraphUtilities();
        gu.ShowTodayLine(mGraph, getTodayDate());
    }

    @Override
    public void hideTodayLine() {
        GraphUtilities gu = new GraphUtilities();
        gu.HideTodayLine(mGraph, getTodayDate());
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

        hideTodayLine();
        mGraph.invalidate();
        showTodayLine();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble(BUNDLE_MIN_X_KEY, mGraph.getViewport().getMinX(false));
        outState.putDouble(BUNDLE_MAX_X_KEY, mGraph.getViewport().getMaxX(false));

        super.onSaveInstanceState(outState);
    }

    // When we leave the GraphActivity, clear this out.
    // Otherwise if you click the graph for a different activity, the viewport will be scoped inappropriately.
    @Override
    protected void onStop() {
        mMaxX = 0;
        mMinX = 0;
        mPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void renderHabitData(List<DataPoint[]> data) {

        GraphUtilities gu = new GraphUtilities();

        visibleSeries = gu.AddSeriesFromData(mGraph, data);

        DataPoint[] valDataPoints = data.get(0);
        if(mMinX == 0 && mMaxX == 0){
            mMinX = valDataPoints[valDataPoints.length - 180].getX();
            mMaxX = valDataPoints[valDataPoints.length - 90].getX();
        }

        gu.SetThickness(mGraph, 10);

        mGraph.getViewport().setMinX(mMinX);
        mGraph.getViewport().setMaxX(mMaxX);
        mGraph.getViewport().setScrollable(true);
        mGraph.getViewport().setScalable(true);
        mGraph.getViewport().setXAxisBoundsManual(true);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            mGraph.getGridLabelRenderer().setNumHorizontalLabels(10);
            mGraph.getGridLabelRenderer().setNumVerticalLabels(5);
        }
        else {// default to portrait
            mGraph.getGridLabelRenderer().setNumHorizontalLabels(5);
            mGraph.getGridLabelRenderer().setNumVerticalLabels(10);
        }
        mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mGraph.getContext(), GraphUtilities.DateFormat));
        mGraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
        mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);

        mGraph.getLegendRenderer().setBackgroundColor(Color.WHITE);
        mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraph.getLegendRenderer().setVisible(true);

        gu.ShowTodayLine(mGraph, getTodayDate());
    }

    private Date getTodayDate() {
        if(todayDate == null) {
            long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(mGraph.getContext()).getString(mGraph.getContext().getString(R.string.pref_day_change_key), "0"));
            todayDate = HabitContract.HabitDataEntry.convertDBDateToDate(HabitContract.HabitDataEntry.getTodaysDBDate(offset));
        }

        return todayDate;
    }
}
