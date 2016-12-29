package com.outlook.notyetapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.CustomNumberFormatter;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.outlook.notyetapp.utilities.TextValidator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HabitActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, HabitDataAdapter.ChecksChangedListener{

    public static final String ACTIVITY_ID_KEY = "activity_id";
    public static final String ACTIVITY_FORECAST_KEY = "activity_forecast";
    public static final String ACTIVITY_HIGHER_IS_BETTER_KEY = "higher_is_better_key";
    public static final String IS_TWO_PANE = "is_two_pane";

    public long mActivityId = -99;
    public float mForecast;
    public boolean mHigherIsBetter;
    public boolean mIsTwoPane;
    public String mActivityTitle;

    private ListView mHabitDataListView;

    private HabitDataAdapter mHabitDataAdapter;

    private TextView mFooterBest7;
    private TextView mFooterBest30;
    private TextView mFooterBest90;

    private LinearLayout mMultiselectDialog = null;
    private EditText mMultiSelectDialogValueField = null;
    private TextValidator mMultiSelectFieldValidator;
    private boolean mMultiSelectFieldHasError = true;
    private GraphView mGraph = null;

    private LineGraphSeries<DataPoint> mValuesDataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg7DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg30DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mAvg90DataSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> mTodaySeries = new LineGraphSeries<DataPoint>();


    // Use newInstance instead of this if possible to avoid missing a param
    public HabitActivityFragment() {
        // Required empty public constructor
    }

    public static HabitActivityFragment newInstance(long activityId, float forecastVal, boolean higherIsBetter, boolean isTwoPane) {
        HabitActivityFragment fragment = new HabitActivityFragment();
        Bundle args = new Bundle();
        args.putLong(ACTIVITY_ID_KEY, activityId);
        args.putFloat(ACTIVITY_FORECAST_KEY, forecastVal);
        args.putBoolean(ACTIVITY_HIGHER_IS_BETTER_KEY, higherIsBetter);
        args.putBoolean(IS_TWO_PANE, isTwoPane);
        fragment.setArguments(args);
        return fragment;
    }

    public static HabitActivityFragment newInstance(Bundle args) {
        HabitActivityFragment fragment = new HabitActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mActivityId = getArguments().getLong(ACTIVITY_ID_KEY);
            mForecast = getArguments().getFloat(ACTIVITY_FORECAST_KEY);
            mHigherIsBetter = getArguments().getBoolean(ACTIVITY_HIGHER_IS_BETTER_KEY);
            mIsTwoPane = getArguments().getBoolean(IS_TWO_PANE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_habit_activity, container, false);

        mGraph = (GraphView) fragmentView.findViewById(R.id.habit_graph);
        mGraph.setOnClickListener(mGraphClicked);
        mHabitDataListView = (ListView)fragmentView.findViewById(R.id.habit_listview);

        View footerParent = fragmentView.findViewById(R.id.habit_data_footer_layout);
        mFooterBest7 = (TextView)footerParent.findViewById(R.id.footer_best_7);
        mFooterBest30 = (TextView)footerParent.findViewById(R.id.footer_best_30);
        mFooterBest90 = (TextView)footerParent.findViewById(R.id.footer_best_90);

        LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.habitdata_footer_add_more_history, null);
        footer.setOnClickListener(mFooterClickListener);
        mHabitDataListView.addFooterView(footer);

        mMultiselectDialog = (LinearLayout) fragmentView.findViewById(R.id.multiselect_value_dialog);
        mMultiSelectDialogValueField = (EditText) mMultiselectDialog.findViewById(R.id.multiselect_value_edittext);
        mMultiSelectDialogValueField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mMultiSelectFieldValidator = new TextValidator(mMultiSelectDialogValueField) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    textView.setError("Cannot be empty");
                    mMultiSelectFieldHasError = true;
                }
                else {
                    mMultiSelectFieldHasError = false;
                }
            }
        };
        mMultiSelectDialogValueField.addTextChangedListener(mMultiSelectFieldValidator);
        mMultiselectDialog.findViewById(R.id.multiselect_cancel_button).setOnClickListener(mMultiSelectClickListener);
        mMultiselectDialog.findViewById(R.id.multiselect_ok_button).setOnClickListener(mMultiSelectClickListener);

        mHabitDataAdapter = new HabitDataAdapter(getContext(), null, 0);

        mHabitDataListView.setOnItemClickListener(mItemClickListener);

        mHabitDataAdapter.setChecksChangedListener(this);
        mHabitDataListView.setAdapter(mHabitDataAdapter);

        getActivity().getSupportLoaderManager().restartLoader(HabitContract.HabitDataQueryHelper.HABITDATA_LOADER, null, this);
        getActivity().getSupportLoaderManager().restartLoader(HabitContract.ActivityBestQueryHelper.ACTIVITY_BEST_LOADER, null, this);

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_habit_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_habit_settings:
                Uri activityUri = HabitContract.ActivitiesEntry.buildActivityUri(mActivityId);
                Intent settingsIntent = new Intent(getActivity(), ActivitySettingsActivity.class);
                settingsIntent.setData(activityUri);
                startActivity(settingsIntent);
                return true;
            case R.id.action_habit_delete:
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Activity?")
                        .setMessage("Do you really want to delete this activity and all associated data?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
                                mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NO_HISTORY);
                                getActivity().getContentResolver().delete(
                                        HabitContract.ActivitiesEntry.buildActivityUri(mActivityId), null, null);

                                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
                                mFirebaseAnalytics.logEvent(AnalyticsConstants.EventNames.HABIT_DELETED, new Bundle());

                                startActivity(mainActivityIntent);
                                Toast.makeText(getActivity(), "Activity deleted", Toast.LENGTH_LONG).show();

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Footer to add a longer history (if the user has been keeping track of data before beginning to use the app).
    private  View.OnClickListener mFooterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("How many days to add?");
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            alertDialogBuilder.setView(input);
            alertDialogBuilder.setIcon(R.drawable.ic_menu_done_holo_light);
            alertDialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertDialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Cursor oldestData = getActivity().getContentResolver().query(HabitContract.HabitDataOldestQueryHelper.buildHabitDataUriForActivity(mActivityId),
                            HabitContract.HabitDataOldestQueryHelper.HABITDATA_OLDEST_PROJECTION,
                            null, //selection handled by URI
                            null, //selection args handled by URI
                            HabitContract.HabitDataOldestQueryHelper.SORT_BY_DATE_ASC_LIMIT_1);
                    oldestData.moveToFirst();

                    long oldestDate = oldestData.getLong(HabitContract.HabitDataOldestQueryHelper.COLUMN_DATE);

                    Cursor activityData = getActivity().getContentResolver().query(HabitContract.ActivitiesEntry.buildActivityUri(mActivityId),
                            HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION,
                            null, //selection taken care of by URI
                            null, //selectionArgs taken care of by URI
                            null);//sort order
                    activityData.moveToFirst();

                    float historicalVal = activityData.getFloat(HabitContract.ActivitySettingsQueryHelper.COLUMN_HISTORICAL);

                    int numberOfDaysToAdd = Integer.parseInt(input.getText().toString());
                    ArrayList<Long> datesToAdd = new ArrayList<Long>(numberOfDaysToAdd);

                    for(Long date = oldestDate - numberOfDaysToAdd; date < oldestDate; date++) {
                        datesToAdd.add(date);
                    }

                    HabitDataDatesUpdated(getActivity(),
                            mActivityId,
                            mHigherIsBetter,
                            datesToAdd,
                            historicalVal,
                            HabitContract.HabitDataEntry.HabitValueType.HISTORICAL);
                }
            });
            final AlertDialog alertDialog = alertDialogBuilder.show();
        }
    };

    // Clicking a HabitData point allows the user to change the value for that day using a popup.
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
            final long selectedDate = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(HabitContract.HabitDataEntry.convertDBDateToString(cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE)));
            alertDialogBuilder.setMessage(getString(R.string.new_value_label));

            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setText(cursor.getString(HabitContract.HabitDataQueryHelper.COLUMN_VALUE));
            input.selectAll();
            input.setLayoutParams(lp);
            input.setImeOptions(EditorInfo.IME_ACTION_DONE);


            alertDialogBuilder.setView(input);
            alertDialogBuilder.setIcon(R.drawable.ic_menu_done_holo_light);

            alertDialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Just close the keyboard and dialog
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            });
            alertDialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HabitDataDateUpdated(getActivity(),
                            mActivityId,
                            mHigherIsBetter,
                            selectedDate,
                            Float.parseFloat(input.getText().toString()),
                            HabitContract.HabitDataEntry.HabitValueType.USER);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            });
            final AlertDialog alertDialog = alertDialogBuilder.show();

            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                        return true;
                    }
                    return false;
                }
            });
        }
    };

    // for when updating only one date
    public void HabitDataDateUpdated(Context context,
                                     long activityId,
                                     boolean higherIsBetter,
                                     long dateToUpdate,
                                     float newValue,
                                     HabitContract.HabitDataEntry.HabitValueType type){
        ArrayList<Long> arrayList = new ArrayList<Long>(1);
        arrayList.add(dateToUpdate);

        HabitDataDatesUpdated(context, activityId, higherIsBetter, arrayList, newValue, type);
    }

    public void HabitDataDatesUpdated(Context context,
                                      long activityId,
                                      boolean higherIsBetter,
                                      ArrayList<Long> datesToUpdate,
                                      float newValue,
                                      HabitContract.HabitDataEntry.HabitValueType type){
        Collections.sort(datesToUpdate, new Comparator<Long>() {
            @Override
            public int compare(Long long1, Long long2) {
                return (int)(long1 - long2);
            }
        });

        //only show the dialog if we are changing a date more than 30 days old. That is when the most work needs to be done.
        // otherwise, the process should be so quick that the progress dialog only has an instant to show and is more distracting.
        boolean showDialog = HabitContract.HabitDataEntry.getTodaysDBDate(0 /*offset doesn't matter for this*/) -  datesToUpdate.get(0) > 30;

        new UpdateHabitDataTask(getActivity(), showDialog).execute(
                new UpdateHabitDataTask.Params(
                        context,
                        activityId,
                        higherIsBetter,
                        datesToUpdate,
                        newValue,
                        type
                )
        );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mActivityId != -99) {
            switch (id) {
                case HabitContract.HabitDataQueryHelper.HABITDATA_LOADER:
                    return new CursorLoader(getActivity(),//context
                            HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(mActivityId),//Uri
                            HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION,//Projection
                            null,//Selection
                            null,//SelectionArgs
                            HabitContract.HabitDataQueryHelper.SORT_BY_DATE_DESC);//sortOrder
                case HabitContract.ActivityBestQueryHelper.ACTIVITY_BEST_LOADER:
                    return new CursorLoader(getActivity(),
                            HabitContract.ActivitiesEntry.buildActivityUri(mActivityId),
                            HabitContract.ActivityBestQueryHelper.ACTIVITY_BEST_PROJECTION,
                            null,
                            null,
                            null);
                default:
                    throw new IllegalArgumentException("Invalid id");
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId())
        {
            case HabitContract.ActivityBestQueryHelper.ACTIVITY_BEST_LOADER:
                updateBestData(data);
                break;
            case HabitContract.HabitDataQueryHelper.HABITDATA_LOADER:
                mHabitDataAdapter.swapCursor(data);
                if(data.getCount() > 0) {
                    List<DataPoint[]> dataPoints = GraphUtilities.UpdateSeriesData(data, mForecast, mValuesDataSeries, mAvg7DataSeries, mAvg30DataSeries, mAvg90DataSeries);
                    DataPoint[] valDataPoints = dataPoints.get(0);
                    GraphUtilities.AddSeriesAndConfigureXScale(valDataPoints[valDataPoints.length - 180].getX(),
                            valDataPoints[valDataPoints.length - 90].getX(),
                            mGraph,
                            mValuesDataSeries,
                            mAvg7DataSeries,
                            mAvg30DataSeries,
                            mAvg90DataSeries,
                            mTodaySeries);
                    mGraph.getViewport().setScrollable(false);
                    mGraph.getViewport().setScalable(false);
                    mGraph.getViewport().setXAxisBoundsManual(true);
                    mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mGraph.getContext(), GraphUtilities.DateFormat));
                    mGraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
                    mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
                    mGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
                    mGraph.getGridLabelRenderer().setNumVerticalLabels(5);
                    GraphUtilities.AddTodayLine(mGraph, mTodaySeries);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid id");
        }
    }

    private void updateBestData(Cursor data){
        if(data.moveToFirst()) {
            mActivityTitle = data.getString(HabitContract.ActivityBestQueryHelper.COLUMN_ACTIVITY_TITLE);
            if(!mIsTwoPane) {
                getActivity().setTitle(mActivityTitle);
            }
            mHigherIsBetter = data.getInt(HabitContract.ActivityBestQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1;
            mFooterBest7.setText(CustomNumberFormatter.formatToThreeCharacters(data.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST7)));
            mFooterBest30.setText(CustomNumberFormatter.formatToThreeCharacters(data.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST30)));
            mFooterBest90.setText(CustomNumberFormatter.formatToThreeCharacters(data.getFloat(HabitContract.ActivityBestQueryHelper.COLUMN_BEST90)));
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHabitDataAdapter.swapCursor(null);
    }

    // Decide if the graph is shown (nothing selected) or the MultiSelectDialog is shown (at least one item selected)
    private void ChangeTopHalf(boolean graph) {
        if(graph) {
            mGraph.setVisibility(View.VISIBLE);
            mMultiselectDialog.setVisibility(View.INVISIBLE);
        } else {
            mGraph.setVisibility(View.INVISIBLE);
            mMultiselectDialog.setVisibility(View.VISIBLE);
        }
    }

    // Whenever a user selects or unselects a HabidData point by swiping, this will be called.
    @Override
    public void ChecksChanged(ArrayList<Long> checkedItems) {
        if(checkedItems.size() > 0) {
            mHabitDataListView.setOnItemClickListener(null);
            ChangeTopHalf(false);
        }
        else {
            ChangeTopHalf(true);
            mHabitDataListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHabitDataListView.setOnItemClickListener(mItemClickListener);
                }
            } , 300);
        }
    }

    public View.OnClickListener mMultiSelectClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.multiselect_cancel_button:
                    MultiSelectCancelClicked(v);
                    break;
                case R.id.multiselect_ok_button:
                    MultiSelectOKClicked(v);
                    break;
            }
        }
    };

    public void MultiSelectCancelClicked(View view) {
        mHabitDataAdapter.ClearCheckmarks();
        //force re-render... there might be a more efficient way to do this?
        getActivity().getContentResolver().notifyChange(HabitContract.HabitDataQueryHelper.buildHabitDataUriForActivity(mActivityId), null);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mGraph.getWindowToken(), 0);
    }
    public void MultiSelectOKClicked(View view) {
        //force validation
        mMultiSelectFieldValidator.afterTextChanged(null);
        if(!mMultiSelectFieldHasError) {
            ArrayList<Long> selectedDates = (ArrayList<Long>) mHabitDataAdapter.GetSelectedDates().clone();
            mHabitDataAdapter.ClearCheckmarks();
            EditText field = (EditText) mMultiselectDialog.findViewById(R.id.multiselect_value_edittext);
            float newValue = Float.parseFloat(field.getText().toString());
            HabitDataDatesUpdated(getActivity(), mActivityId, mHigherIsBetter, selectedDates, newValue, HabitContract.HabitDataEntry.HabitValueType.USER);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mGraph.getWindowToken(), 0);
        }
    }

    // If the user clicks on the graph, go to a full screen graph view.
    public View.OnClickListener mGraphClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), GraphActivity.class);
            intent.putExtra(GraphActivity.ACTIVITY_FORECAST_KEY, mForecast);
            intent.putExtra(GraphActivity.ACTIVITY_ID_KEY, mActivityId);
            intent.putExtra(GraphActivity.ACTIVITY_TITLE_KEY, mActivityTitle);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    };
}
