package com.outlook.notyetapp.screen.habit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.ActivitySettingsFragment;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.dagger.ActivityScoped.DaggerHabitActivityFragmentComponent;
import com.outlook.notyetapp.dagger.ActivityScoped.HabitActivityFragmentModule;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.activitysettings.ActivitySettingsActivity;
import com.outlook.notyetapp.screen.graph.GraphActivity;
import com.outlook.notyetapp.screen.main.MainActivity;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.CustomNumberFormatter;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.outlook.notyetapp.utilities.HabitValueValidator;
import com.outlook.notyetapp.utilities.library.GroupValidator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Used in both MainActivity and HabitActivity
// functionality to show graph and list view of data for a specific habit.
// update the data by clicking one item or swiping right on multiple to multiselect
public class HabitActivityFragment extends Fragment implements HabitActivityFragmentContract.View{

    public static final String ACTIVITY_ID_KEY = "activity_id";
    public static final String IS_TWO_PANE = "is_two_pane";
    public static final String LIST_VIEW_STATE = "list_view_state";
    public static final String FOOTER_LABEL = "footer_label";

    public long mActivityId = -99;
    public float mForecast;
    public boolean mIsTwoPane;
    public String mActivityTitle;

    ListView mHabitDataListView;

    private Parcelable listViewState;

    private HabitDataAdapter mHabitDataAdapter;
    private GroupValidator groupValidator;
    public ProgressDialog mProgressDialog;

    @BindView(R.id.footer_best_7)
    TextView mFooterBest7;

    @BindView(R.id.footer_best_30)
    TextView mFooterBest30;

    @BindView(R.id.footer_best_90)
    TextView mFooterBest90;

    @BindView(R.id.multiselect_value_dialog)
    LinearLayout mMultiselectDialog = null;

    @BindView(R.id.multiselect_value_edittext)
    EditText mMultiSelectDialogValueField = null;

    @BindView(R.id.habit_graph)
    GraphView mGraph = null;

    @Inject
    HabitActivityFragmentContract.ActionListener mPresenter;

    @Inject
    GraphUtilities mGraphUtilities;

    @Inject
    public DateHelper mDateHelper;

    // Use newInstance instead of this if possible to avoid missing a param
    public HabitActivityFragment() {
        // Required empty public constructor
    }

    public static HabitActivityFragment newInstance(long activityId, boolean isTwoPane) {
        HabitActivityFragment fragment = new HabitActivityFragment();
        Bundle args = new Bundle();
        args.putLong(ACTIVITY_ID_KEY, activityId);
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
            mIsTwoPane = getArguments().getBoolean(IS_TWO_PANE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_habit_activity, container, false);
        LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.habitdata_footer_add_more_history, mHabitDataListView, false);
        //Can't add the footer in the xml, so need to do *some* binding manually (lots done with ButterKnife).
        mHabitDataListView = (ListView)fragmentView.findViewById(R.id.habit_listview);
        mHabitDataListView.addFooterView(footer, FOOTER_LABEL, true);
        ButterKnife.bind(this, fragmentView);

        groupValidator = new GroupValidator(getContext());

        mMultiSelectDialogValueField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        groupValidator.AddFieldToValidate(mMultiSelectDialogValueField, HabitValueValidator.class);

        mHabitDataAdapter = new HabitDataAdapter(getContext(), null, 0);

        //Do this manually because we want to be able to remove the click listener when the user is doing multiselect
        mHabitDataListView.setOnItemClickListener(mItemClickListener);

        DaggerHabitActivityFragmentComponent.builder()
                .notYetApplicationComponent(NotYetApplication.get(this.getActivity()).component())
                .habitActivityFragmentModule(new HabitActivityFragmentModule(this))
                .build().inject(this);

        mHabitDataAdapter.setChecksChangedListener(mPresenter);
        mHabitDataListView.setAdapter(mHabitDataAdapter);

        if(savedInstanceState != null) {
            listViewState = savedInstanceState.getParcelable(LIST_VIEW_STATE);
        }

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_habit_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // not MVP right now. Low pri for unit testing.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_habit_settings:
                Intent settingsIntent = new Intent(getActivity(), ActivitySettingsActivity.class);
                settingsIntent.putExtra(ActivitySettingsFragment.ARG_ACTIVITY_ID, mActivityId);
                startActivity(settingsIntent);
                return true;
            case R.id.action_habit_delete:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.delete_habit_dialog_title))
                        .setMessage(getString(R.string.delete_habit_dialog_message))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
                                mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NO_HISTORY);

                                mPresenter.unsubscribe();

                                getActivity().getContentResolver().delete(
                                        HabitContract.ActivitiesEntry.buildActivityUri(mActivityId), null, null);

                                NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.HABIT_DELETED);

                                startActivity(mainActivityIntent);
                                Toast.makeText(getActivity(), getString(R.string.delete_habit_completed_toast), Toast.LENGTH_LONG).show();

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // Footer to add a longer history (if the user has been keeping track of data before beginning to use the app).
    @OnClick(R.id.add_more_history_layout)
    public void addMoreHistoryFooterClicked() {
        mPresenter.addMoreHistoryClicked();
    }

    @Override
    public void showAddMoreHistoryDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.add_more_history_dialog_question));

        final EditText input = new EditText(getActivity());
        input.setId(R.id.add_more_history_edit_text);
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
                int numberOfDaysToAdd = Integer.parseInt(input.getText().toString());
                mPresenter.addMoreHistoryDialogOKClicked(mActivityId, numberOfDaysToAdd);
            }
        });
        alertDialogBuilder.show();
    }



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
            input.setId(R.id.update_habit_value_edit_text);
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
                    mPresenter.updateHabitDataClicked(mActivityId, selectedDate, Float.parseFloat(input.getText().toString()));

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

    @Override
    public void hideUpdatingDialog() {
        if(this.mProgressDialog != null){
            this.mProgressDialog.dismiss();
        }
    }

    @Override
    public void showUpdatingDialog() {
        mProgressDialog = ProgressDialog.show(getActivity(), getContext().getString(R.string.habit_data_update_title), null);
    }

    @Override
    public void renderHabitDataToList(Cursor data) {
        mHabitDataAdapter.swapCursor(data);
        if(listViewState != null) {
            mHabitDataListView.onRestoreInstanceState(listViewState);
        }
    }

    @Override
    public void renderHabitDataToGraph(List<DataPoint[]> data) {
        if(data.size() > 0) {
            mGraphUtilities.AddSeriesFromData(mGraph, data);

            DataPoint[] valDataPoints = data.get(0);
            //By default display the last 90 days of data in the graph. (so 1-90 back from the length)
            double minX = valDataPoints[valDataPoints.length - 90].getX();
            double maxX = valDataPoints[valDataPoints.length - 1].getX();

            mGraph.getViewport().setMinX(minX);
            mGraph.getViewport().setMaxX(maxX);
            mGraph.getViewport().setScrollable(false);
            mGraph.getViewport().setScalable(false);
            mGraph.getViewport().setXAxisBoundsManual(true);
            mGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(mGraph.getContext(), GraphUtilities.DateFormat));
            mGraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
            mGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
            mGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
            mGraph.getGridLabelRenderer().setNumVerticalLabels(5);
            mGraphUtilities.ShowTodayLine(mGraph);
        }
    }

    @Override
    public void renderBestData(String activityTitle, float best7, float best30, float best90) {
        mActivityTitle = activityTitle;
        if(!mIsTwoPane) {
            getActivity().setTitle(activityTitle);
        }
        mFooterBest7.setText(CustomNumberFormatter.formatToThreeCharacters(best7));
        mFooterBest30.setText(CustomNumberFormatter.formatToThreeCharacters(best30));
        mFooterBest90.setText(CustomNumberFormatter.formatToThreeCharacters(best90));
    }

    @Override
    public void showGraph() {
        mGraph.setVisibility(View.VISIBLE);
        mMultiselectDialog.setVisibility(View.INVISIBLE);
        mHabitDataListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHabitDataListView.setOnItemClickListener(mItemClickListener);
            }
        } , 300);
    }

    @Override
    public void showMultiSelectDialog() {
        mHabitDataListView.setOnItemClickListener(null);
        mGraph.setVisibility(View.INVISIBLE);
        mMultiselectDialog.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.multiselect_cancel_button)
    public void MultiSelectCancelClicked() {
        mHabitDataAdapter.ClearCheckmarks();
        mPresenter.multiSelectCancelClicked(mActivityId);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mGraph.getWindowToken(), 0);
    }

    @OnClick(R.id.multiselect_ok_button)
    public void MultiSelectOKClicked() {
        if(groupValidator.ValidateAll()) {
            ArrayList<Long> selectedDates = (ArrayList<Long>) mHabitDataAdapter.GetSelectedDates().clone();
            mHabitDataAdapter.ClearCheckmarks();
            EditText field = (EditText) mMultiselectDialog.findViewById(R.id.multiselect_value_edittext);
            float newValue = Float.parseFloat(field.getText().toString());
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mGraph.getWindowToken(), 0);
            mPresenter.updateHabitDataClicked(mActivityId, selectedDates, newValue);
        }
    }

    // The graph activity needs the forecast to let you pan right past today's date
    // we are already querying activity settings, so we store the forecast in case the user clicks the graph
    // this also keeps the forecast correct if the user updates it via settings
    @Override
    public void currentForecastData(float forecast) {
        mForecast = forecast;
    }

    // If the user clicks on the graph, go to a full screen graph view.
    @OnClick(R.id.habit_graph)
    public void graphClicked() {
        Intent intent = new Intent(getActivity(), GraphActivity.class);
        intent.putExtra(GraphActivity.ACTIVITY_FORECAST_KEY, mForecast);
        intent.putExtra(GraphActivity.ACTIVITY_ID_KEY, mActivityId);
        intent.putExtra(GraphActivity.ACTIVITY_TITLE_KEY, mActivityTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        mPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void onResume() {
        mPresenter.subscribeToHabitDataAndBestData(mActivityId);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_VIEW_STATE, mHabitDataListView.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }
}
