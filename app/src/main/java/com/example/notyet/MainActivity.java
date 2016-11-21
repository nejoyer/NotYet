package com.example.notyet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.example.notyet.data.HabitContract;
import com.example.notyet.utilities.SwipeOpenListener;

import java.util.Calendar;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

// Main activity is basically a list of Activities that the user has created that he/she wants to try to improve on.
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MainMenuFragment.OnFragmentInteractionListener {

    private ActivityAdapter mActivityAdapter;
    private ListView mMainListView;



    private static final String DBDATE_LAST_UPDATED_TO_KEY = "dbdatelastupdatedto";
    private long mDBDateLastUpdatedTo = 0;
    public static final String SHOW_ALL_KEY = "showall";
    private boolean mShowAll = false;
    private final static String POSITION_KEY = "position";
    private int mPosition;
    private final static String NEW_USER_KEY = "newuser";
    private boolean mNewUser = true;
    private final static String HIDE_HEADER_KEY = "hideheader";
    private boolean mHideHeader = false;

    private boolean mIsTwoPane = false;
    private static final String HABIT_ACTIVITY_FRAGMENT_TAG = "habit_activity_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            loadMemberVariablesFromPreferences();

            getSupportFragmentManager().beginTransaction()
                    .add(MainMenuFragment.newInstance(mShowAll), "MainMenuFragment")
                    .commit();
        }
        else {
            loadMemberVariablesFromBundle(savedInstanceState);
        }

        setContentView(R.layout.activity_main);

        if(findViewById(R.id.right_pane_frame) != null){
            mIsTwoPane = true;
        }


        mActivityAdapter = new ActivityAdapter(this, null, 0);
        mMainListView = (ListView) findViewById(R.id.main_listview);

        // onclick launch the Habit Activity
        mMainListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                mPosition = position;
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    itemSelected(
                            cursor.getLong(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_ID),
                            cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_FORECAST),
                            cursor.getInt(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1,
                            cursor.getString(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_TITLE)
                    );
                }
            }
        });

        mMainListView.setAdapter(mActivityAdapter);

        if(!mHideHeader) {
            mMainListView.addHeaderView(GenerateHelpHeader());
        }

        getSupportLoaderManager().initLoader(HabitContract.ActivitiesTodaysStatsQueryHelper.ACTIVITES_TODAYS_STATS_LOADER, null, this);
    }

    private View GenerateHelpHeader(){
        final SwipeLayout header = (SwipeLayout) getLayoutInflater().inflate(R.layout.list_item_activity, null);
        header.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //This is a bit hacky. But this ensures that the header stays looking like the list items.
        LinearLayout section7 = (LinearLayout)header.findViewById(R.id.section_7);
        LinearLayout section7curTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section7curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section7curText = (TextView)section7.getChildAt(0);
        section7.removeViewAt(0);
        section7curTextAndHelp.addView(section7curText, 0);
        section7.addView(section7curTextAndHelp, 0);
        LinearLayout section7bestTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section7bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section7bestText = (TextView)section7.getChildAt(1);
        section7.removeViewAt(1);
        section7bestTextAndHelp.addView(section7bestText, 0);
        section7.addView(section7bestTextAndHelp, 1);

        LinearLayout section30 = (LinearLayout)header.findViewById(R.id.section_30);
        LinearLayout section30curTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section30curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section30curText = (TextView)section30.getChildAt(0);
        section30.removeViewAt(0);
        section30curTextAndHelp.addView(section30curText, 0);
        section30.addView(section30curTextAndHelp, 0);
        LinearLayout section30bestTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section30bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section30bestText = (TextView)section30.getChildAt(1);
        section30.removeViewAt(1);
        section30bestTextAndHelp.addView(section30bestText, 0);
        section30.addView(section30bestTextAndHelp, 1);

        LinearLayout section90 = (LinearLayout)header.findViewById(R.id.section_90);
        LinearLayout section90curTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section90curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section90curText = (TextView)section90.getChildAt(0);
        section90.removeViewAt(0);
        section90curTextAndHelp.addView(section90curText, 0);
        section90.addView(section90curTextAndHelp, 0);
        LinearLayout section90bestTextAndHelp = (LinearLayout)getLayoutInflater().inflate(R.layout.main_header_info_button_segment, null);
        section90bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section90bestText = (TextView)section90.getChildAt(1);
        section90.removeViewAt(1);
        section90bestTextAndHelp.addView(section90bestText, 0);
        section90.addView(section90bestTextAndHelp, 1);

        TextView badBottomText = (TextView)header.findViewById(R.id.bad_bottom_text);
        badBottomText.setText(getString(R.string.list_item_activity_header_hide));
        TextView goodBottomText = (TextView)header.findViewById(R.id.good_bottom_text);
        goodBottomText.setText(getString(R.string.list_item_activity_header_hide));
        header.removeAllSwipeListener();
        header.addSwipeListener(new SwipeOpenListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                mMainListView.removeHeaderView(header);
                mHideHeader = true;
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(HIDE_HEADER_KEY, mHideHeader);
                editor.commit();
            }
        });

        return header;
    }

    private View.OnClickListener mHeaderHelpCurrentClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.help_title))
                    .setMessage(getString(R.string.main_header_help_current))
                    .setIcon(R.drawable.ic_dialog_info_with_tint)
                    .show();
        }
    };
    private View.OnClickListener mHeaderHelpBestClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.help_title))
                    .setMessage(getString(R.string.main_header_help_best))
                    .setIcon(R.drawable.ic_dialog_info_with_tint)
                    .show();
        }
    };

    public void itemSelected(long activityId, float forecastVal, boolean higherIsBetter, String activityTitle)
    {
        if(mIsTwoPane)
        {
            TextView rightPaneTitle = (TextView)findViewById(R.id.right_pane_title);
            rightPaneTitle.setText(activityTitle);
            HabitActivityFragment fragment = HabitActivityFragment.newInstance(activityId, forecastVal, higherIsBetter);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_pane_frame, fragment, HABIT_ACTIVITY_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(MainActivity.this, HabitActivity.class);
            intent.putExtra(HabitActivityFragment.ACTIVITY_ID_KEY, activityId);
            intent.putExtra(HabitActivityFragment.ACTIVITY_FORECAST_KEY, forecastVal);
            intent.putExtra(HabitActivityFragment.ACTIVITY_HIGHER_IS_BETTER_KEY, higherIsBetter);

            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putLong(DBDATE_LAST_UPDATED_TO_KEY, mDBDateLastUpdatedTo);
        saveInstanceState.putBoolean(SHOW_ALL_KEY, mShowAll);
        saveInstanceState.putInt(POSITION_KEY, mPosition);
        saveInstanceState.putBoolean(NEW_USER_KEY, mNewUser);
        saveInstanceState.putBoolean(HIDE_HEADER_KEY, mHideHeader);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadMemberVariablesFromBundle(savedInstanceState);
    }

    private void loadMemberVariablesFromBundle(Bundle savedInstanceState)
    {
        mDBDateLastUpdatedTo = savedInstanceState.getLong(DBDATE_LAST_UPDATED_TO_KEY);
        mShowAll = savedInstanceState.getBoolean(SHOW_ALL_KEY);
        mPosition = savedInstanceState.getInt(POSITION_KEY);
        mNewUser = savedInstanceState.getBoolean(NEW_USER_KEY);
        mHideHeader = savedInstanceState.getBoolean(HIDE_HEADER_KEY);
    }

    private void loadMemberVariablesFromPreferences()
    {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mDBDateLastUpdatedTo = sharedPref.getLong(DBDATE_LAST_UPDATED_TO_KEY, 0);
        mShowAll = sharedPref.getBoolean(SHOW_ALL_KEY, false);
        mNewUser = sharedPref.getBoolean(NEW_USER_KEY, true);
        mHideHeader = sharedPref.getBoolean(HIDE_HEADER_KEY, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateActivitiesIfNecessary();
    }

    // If the user hasn't used the app today, we need to populate the DB with default data for today (or more days if necessary).
    // TODO: allow a way to force this to happen?
    public void updateActivitiesIfNecessary()
    {
        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_day_change_key), "0"));
        long todaysDBDate = HabitContract.HabitDataEntry.getTodaysDBDate(offset);
        if(todaysDBDate > mDBDateLastUpdatedTo)
        {
            CreateRecentDataTask task = new CreateRecentDataTask();
            task.execute(this);
            mDBDateLastUpdatedTo = todaysDBDate;

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(DBDATE_LAST_UPDATED_TO_KEY, mDBDateLastUpdatedTo);
            editor.commit();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;
        long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_day_change_key), "0"));
        String dbDateString = String.valueOf(HabitContract.HabitDataEntry.getTodaysDBDate(offset));
        if(mShowAll) {
            selection = HabitContract.HabitDataEntry.COLUMN_DATE + " = ?";
            selectionArgs = new String[]{dbDateString};
        }
        else {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            String dayOfWeekMask = "0";
            switch (day) {
                case Calendar.SUNDAY:
                    dayOfWeekMask = "1";
                    break;
                case Calendar.MONDAY:
                    dayOfWeekMask = "2";
                    break;
                case Calendar.TUESDAY:
                    dayOfWeekMask = "4";
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeekMask = "8";
                    break;
                case Calendar.THURSDAY:
                    dayOfWeekMask = "16";
                    break;
                case Calendar.FRIDAY:
                    dayOfWeekMask = "32";
                    break;
                case Calendar.SATURDAY:
                    dayOfWeekMask = "64";
                    break;
            }

            selection = HabitContract.HabitDataEntry.COLUMN_DATE + " = ? AND "
                    + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " < ? AND "
                    + HabitContract.ActivitiesEntry.COLUMN_DAYS_TO_SHOW + " & ?";
            selectionArgs = new String[]{dbDateString, dbDateString, dayOfWeekMask};
        }

        return new CursorLoader(this,//context
                HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(),//Uri
                HabitContract.ActivitiesTodaysStatsQueryHelper.ACTIVITIES_TODAYS_STATS_PROJECTION,//Projection
                selection,
                selectionArgs,//SelectionArgs This is taken care of in the provider
                HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(mActivityAdapter.getCount() < 1 && mNewUser)
        {
            mMainListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View createButton = findViewById(R.id.action_create_new_activity);
                    new SimpleTooltip.Builder(MainActivity.this)
                            .anchorView(createButton)
                            .contentView(R.layout.hero_tooltip, R.id.tooltip_text)
                            .text(getString(R.string.no_activities_new_user_hero))
                            .gravity(Gravity.BOTTOM)
                            .animated(true)
                            .transparentOverlay(false).onDismissListener(
                                new SimpleTooltip.OnDismissListener() {
                                    @Override
                                    public void onDismiss(SimpleTooltip tooltip) {
                                        mNewUser = false;
                                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putBoolean(NEW_USER_KEY, mNewUser);
                                        editor.commit();
                                    }
                                }
                            )
                            .build()
                            .show();
                }
            }, 500);

        }
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mActivityAdapter.swapCursor(data);

        if(mIsTwoPane && data.getCount() > 1 && mMainListView.getCheckedItemPosition() < 0) {
            mMainListView.smoothScrollToPosition(mPosition);
            mMainListView.post(new Runnable() {
                @Override
                public void run() {
                    mMainListView.performItemClick(mActivityAdapter.getView(mPosition, null, null), mPosition, mActivityAdapter.getItemId(mPosition));
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mActivityAdapter.swapCursor(null);
    }

    @Override
    public void visibilityChanged(boolean showAll) {
        mShowAll = showAll;
        getSupportLoaderManager().restartLoader(HabitContract.ActivitiesTodaysStatsQueryHelper.ACTIVITES_TODAYS_STATS_LOADER, null, this);
    }
}
