package com.example.notyet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.notyet.data.HabitContract;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Calendar;

// Main activity is basically a list of Activities that the user has created that he/she wants to try to improve on.
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MainMenuFragment.OnFragmentInteractionListener {

    private ActivityAdapter mActivityAdapter;

    private static final String DBDATE_LAST_UPDATED_TO_KEY = "dbdatelastupdatedto";
    private long mDBDateLastUpdatedTo = 0;
    public static final String SHOW_ALL_KEY = "showall";
    private boolean mShowAll = false;

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

        mActivityAdapter = new ActivityAdapter(this, null, 0);
        final ListView mainListView = (ListView) findViewById(R.id.main_listview);

        // onclick launch the Habit Activity
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(MainActivity.this, HabitActivity.class);
                    intent.putExtra(HabitActivity.ACTIVITY_FORECAST_KEY, cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_FORECAST));
                    intent.putExtra(HabitActivity.ACTIVITY_ID_KEY, cursor.getLong(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_ID));
                    intent.putExtra(HabitActivity.ACTIVITY_HIGHER_IS_BETTER_KEY,
                            cursor.getInt(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_HIGHER_IS_BETTER)  == 1
                    );

                    startActivity(intent);
                }
            }
        });

        mainListView.setAdapter(mActivityAdapter);

        getSupportLoaderManager().initLoader(HabitContract.ActivitiesTodaysStatsQueryHelper.ACTIVITES_TODAYS_STATS_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putLong(DBDATE_LAST_UPDATED_TO_KEY, mDBDateLastUpdatedTo);
        saveInstanceState.putBoolean(SHOW_ALL_KEY, mShowAll);
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
    }

    private void loadMemberVariablesFromPreferences()
    {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mDBDateLastUpdatedTo = sharedPref.getLong(DBDATE_LAST_UPDATED_TO_KEY, 0);
        mShowAll = sharedPref.getBoolean(SHOW_ALL_KEY, false);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mActivityAdapter.swapCursor(data);
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
