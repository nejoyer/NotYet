package com.outlook.notyetapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;

import com.outlook.notyetapp.BuildConfig;
import com.outlook.notyetapp.R;

import java.text.SimpleDateFormat;

/**
 * Defines table and column names for the habit database.
 */
public class HabitContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.outlook.notyetapp/activities/ is a valid path for
    // looking at activity settings. content://com.outlook.notyetapp/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_ACTIVITIES = "activities";
    public static final String PATH_HABIT_DATA = "habitdata";
    public static final String PATH_STATS = "stats";
    public static final String PATH_RECENT = "mostrecent";

    // A number for each type of query
    static final int ACTIVITY = 100;
    static final int ACTIVITIES = 101;
    static final int ACTIVITY_STATS = 200;
    static final int ACTIVITIES_TODAYS_STATS = 201;
    static final int ACTIVITIES_MOST_RECENT = 202;
    static final int HABITDATA_ENTRY = 300;
    static final int HABITDATA_ENTRIES = 301;
    static final int ACTIVITY_HABITDATA_ENTRIES = 302;

    // This is actually legacy and can be removed in the future.
    // I moved to MVP with StorIO rather than the loaders I had originally
    public static final int ACTIVITES_TODAYS_STATS_LOADER = 0;
    public static final int HABITDATA_LOADER = 1;
    public static final int ACTIVITY_BEST_LOADER = 2;
    public static final int ACTIVITES_SORT_LOADER = 3;
    public static final int GRAPHDATA_LOADER = 4;


    /* Inner class that defines the table contents of the Activities table */
    public static final class ActivitiesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACTIVITIES).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES;
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES;
        public static final String CONTENT_TYPE_ACTIVITY_STATS =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES + "/" + PATH_STATS + "/TODO";
        public static final String CONTENT_TYPE_ACTIVITIES_STATS =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES + "/" + PATH_STATS;
        public static final String CONTENT_TYPE_MOST_RECENT =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ACTIVITIES + "/" + PATH_RECENT;

        // Table name
        public static final String TABLE_NAME = "activities";

        // The activity id
        // long
        public static final String COLUMN_ACTIVITY_ID = "_id";

        // The activity title string is what the user decided to call this activity
        // (ex. Pushups)
        public static final String COLUMN_ACTIVITY_TITLE = "activity_title";

        // when the user is just starting out, there is no data, so they can't really see improvement
        // they choose some value that represents a habit not yet formed and then they can see
        // rapid improvement from there.
        // float/real
        public static final String COLUMN_HISTORICAL = "historical_values";

        // what they user is shooting for usually...
        // This allows them to see what there averages will look like in the future if they
        // keep up the good work
        // float/real
        public static final String COLUMN_FORECAST = "forecast_values";

        // When the user swipes to the left from the home screen, this value is applied for todays date.
        // float/real
        public static final String COLUMN_SWIPE_VALUE = "swipe_value";

        // is this a habit where higher is better? ex. Pushups.
        // or where lower is better... ex. TV minutes
        // boolean (but stored in db as int)
        public static final String COLUMN_HIGHER_IS_BETTER = "higher_is_better";

        // which days to show the activity on the homepage
        // INT(1) which is one byte used as a bitmask.
        public static final String COLUMN_DAYS_TO_SHOW = "days_to_show";

        // the best 7 day average the user has ever acheived (highest or lowest)
        // float/real
        public static final String COLUMN_BEST7 = "best7";

        // the best 30 day average the user has ever acheived (highest or lowest)
        // float/real
        public static final String COLUMN_BEST30 = "best30";

        // the best 90 day average the user has ever acheived (highest or lowest)
        // float/real
        public static final String COLUMN_BEST90 = "best90";

        // the 0 indexed place in line the activity should show on the homepage
        // int
        public static final String COLUMN_SORT_PRIORITY = "sort_priority";

        // if the user swipes to hide an activity for the day... the date that they swiped...
        // this allows us to put it back in the UI if it is a new day (user can only hide for a day).
        // long
        public static final String COLUMN_HIDE_DATE = "hide_date";

        public static Uri buildActivityUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getActivityNumberFromUri(Uri uri)
        {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /* Inner class that defines the table contents of the HabitData table */
    public static final class HabitDataEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HABIT_DATA).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HABIT_DATA;
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HABIT_DATA;

        // Table name
        public static final String TABLE_NAME = "habitdata";

        // The habit id
        // long
        public static final String COLUMN_ID = "_id";

        // The id of the activity this data point is associated with
        // long
        public static final String COLUMN_ACTIVITY_ID = "activity_id";

        // Date, stored as long in milliseconds since the epoch
        // long
        public static final String COLUMN_DATE = "date";

        // the value to use as the user's entry for that activity on that day
        // float/real
        public static final String COLUMN_VALUE = "value";

        // The rolling average for the last 7 days
        // float/real
        public static final String COLUMN_ROLLING_AVG_7 = "rolling_avg_7";

        // The rolling average for the last 30 days
        // float/real
        public static final String COLUMN_ROLLING_AVG_30 = "rolling_avg_30";

        // The rolling average for the last 90 days
        // float/real
        public static final String COLUMN_ROLLING_AVG_90 = "rolling_avg_90";

        // The type of data that is in the value column. 1 = Historical, 2 = User, 3 = Forecast, 4 = Never Entered
        // float/real
        public static final String COLUMN_TYPE = "type";

        // URI content://com.outlook.notyetapp/habitdata/#
        public static Uri buildHabitDataUriForHabitDataEntryId(long habitDataId) {
            return ContentUris.withAppendedId(CONTENT_URI, habitDataId);
        }

        // URI content://com.outlook.notyetapp/activities/#/habitdata
        public static Uri buildUriForAllHabitDataForActivityId(long activityId) {
            return ActivitiesEntry.buildActivityUri(activityId).buildUpon().appendPath(PATH_HABIT_DATA).build();
        }

        // gets a long which represents the number of days since the start of the Julian era. (Jan 1st 4713 BC).
        public static long getTodaysDBDate(long offset)
        {
            Time time = new Time();
            time.set(System.currentTimeMillis());
            int julianDay = Time.getJulianDay(System.currentTimeMillis() - offset, time.gmtoff);
            return (long) julianDay;
        }

        // convert the date (as long from DB) to a string for display in the UI. Not Locale safe.
        public static String convertDBDateToString(long daysSinceJulianEraStart){
            Time time = new Time();
            time.setJulianDay((int)daysSinceJulianEraStart);
            SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EE");
            SimpleDateFormat dayFormat = new SimpleDateFormat(" M/d");
            return dayOfWeekFormat.format(time.toMillis(false)).substring(0,2) + dayFormat.format(time.toMillis(false));
        }

        public static long getHabitNumberFromUri(Uri uri)
        {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public enum HabitValueType {
            HISTORICAL(1), USER(2), FORECAST(3), NEVERENTERED(4);

            private final int id;
            HabitValueType(int id) { this.id = id; }
            public int getValue() { return id; }
            public static int getColor(Context context, int id) {
                switch (id)
                {
                    case 1:
                        return ContextCompat.getColor(context, R.color.colorAccent);
                    case 2:
                        return Color.WHITE;
                    case 3:
                        return ContextCompat.getColor(context, R.color.colorPrimaryDark);
                    case 4:
                        return ContextCompat.getColor(context, R.color.colorBad);
                }
                return Color.WHITE;
            }
        }
    }

    // The main query that runs in MainActivity to give the most recent averages as well as the best averages
    public static final class ActivitiesTodaysStatsQueryHelper {
        public static final int ACTIVITES_TODAYS_STATS_LOADER = HabitContract.ACTIVITES_TODAYS_STATS_LOADER;

        public static final String[] ACTIVITIES_TODAYS_STATS_PROJECTION = {//This will include data from both tables
                ActivitiesEntry.TABLE_NAME + "." + ActivitiesEntry._ID,//This is required for CursorAdapter
                ActivitiesEntry.COLUMN_ACTIVITY_TITLE,
                ActivitiesEntry.COLUMN_FORECAST,
                ActivitiesEntry.COLUMN_HISTORICAL,
                ActivitiesEntry.COLUMN_SWIPE_VALUE,
                ActivitiesEntry.COLUMN_HIGHER_IS_BETTER,
                ActivitiesEntry.COLUMN_BEST7,
                ActivitiesEntry.COLUMN_BEST30,
                ActivitiesEntry.COLUMN_BEST90,
                HabitDataEntry.COLUMN_ROLLING_AVG_7,
                HabitDataEntry.COLUMN_ROLLING_AVG_30,
                HabitDataEntry.COLUMN_ROLLING_AVG_90,
                HabitDataEntry.COLUMN_VALUE
        };

        public static final int COLUMN_ACTIVITY_ID = 0;
        public static final int COLUMN_ACTIVITY_TITLE = 1;
        public static final int COLUMN_FORECAST = 2;
        public static final int COLUMN_HISTORICAL = 3;
        public static final int COLUMN_SWIPE_VALUE = 4;
        public static final int COLUMN_HIGHER_IS_BETTER = 5;
        public static final int COLUMN_BEST7 = 6;
        public static final int COLUMN_BEST30 = 7;
        public static final int COLUMN_BEST90 = 8;
        public static final int COLUMN_ROLLING_AVG_7 = 9;
        public static final int COLUMN_ROLLING_AVG_30 = 10;
        public static final int COLUMN_ROLLING_AVG_90 = 11;
        public static final int COLUMN_VALUE = 12;

        public static Uri buildActivitiesStatsUri(){
            return HabitContract.ActivitiesEntry.CONTENT_URI.buildUpon().appendPath(PATH_STATS).build();
        }
    }

    public static final class ActivitySettingsQueryHelper {
        public static final String[] ACTIVITY_SETTINGS_PROJECTION = {
                ActivitiesEntry.COLUMN_ACTIVITY_ID,
                ActivitiesEntry.COLUMN_ACTIVITY_TITLE,
                ActivitiesEntry.COLUMN_HISTORICAL,
                ActivitiesEntry.COLUMN_FORECAST,
                ActivitiesEntry.COLUMN_SWIPE_VALUE,
                ActivitiesEntry.COLUMN_HIGHER_IS_BETTER,
                ActivitiesEntry.COLUMN_DAYS_TO_SHOW,
                ActivitiesEntry.COLUMN_BEST7,
                ActivitiesEntry.COLUMN_BEST30,
                ActivitiesEntry.COLUMN_BEST90
        };

        public static final int COLUMN_ACTIVITY_ID = 0;
        public static final int COLUMN_ACTIVITY_TITLE = 1;
        public static final int COLUMN_HISTORICAL = 2;
        public static final int COLUMN_FORECAST = 3;
        public static final int COLUMN_SWIPE_VALUE = 4;
        public static final int COLUMN_HIGHER_IS_BETTER = 5;
        public static final int COLUMN_DAYS_TO_SHOW = 6;
        public static final int COLUMN_BEST7 = 7;
        public static final int COLUMN_BEST30 = 8;
        public static final int COLUMN_BEST90 = 9;
    }

    public static final class ActivitySortQueryHelper {
        public static final int ACTIVITES_SORT_LOADER = HabitContract.ACTIVITES_SORT_LOADER;

        public static final String[] ACTIVITY_SORT_PROJECTION = {
                ActivitiesEntry.COLUMN_ACTIVITY_ID,
                ActivitiesEntry.COLUMN_ACTIVITY_TITLE
        };

        public static final int COLUMN_ACTIVITY_ID = 0;
        public static final int COLUMN_ACTIVITY_TITLE = 1;

        public static Uri getActivitiesUri(){
            return ActivitiesEntry.CONTENT_URI;
        }
    }

    public static final class RecentDataQueryHelper {
        public static final String[] RECENT_DATA_PROJECTION = {
                HabitDataEntry.COLUMN_ACTIVITY_ID,
                "MAX(" + HabitDataEntry.COLUMN_DATE + ") AS " + HabitDataEntry.COLUMN_DATE,
                ActivitiesEntry.COLUMN_HIGHER_IS_BETTER,
                ActivitiesEntry.COLUMN_HISTORICAL
        };

        public static final int COLUMN_ACTIVITY_ID = 0;
        public static final int COLUMN_DATE = 1;
        public static final int COLUMN_HIGHER_IS_BETTER = 2;
        public static final int COLUMN_HISTORICAL = 3;

        public static final String GROUP_BY_ACTIVITY_ID = HabitDataEntry.COLUMN_ACTIVITY_ID;

        public static final Uri RECENT_DATA_URI = ActivitiesEntry.CONTENT_URI.buildUpon().appendPath(PATH_RECENT).build();

        public static String getHavingStatement(long date)
        {
            return HabitDataEntry.COLUMN_DATE + " != " + date;
        }
    }

    public static final class NormalizeActivityDataTaskQueryHelper {
        public static final String[] NORMALIZE_ACTIVITY_DATA_TASK_PROJECTION = {
                HabitDataEntry._ID,
                HabitDataEntry.COLUMN_DATE,
                HabitDataEntry.COLUMN_VALUE
        };

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_DATE = 1;
        public static final int COLUMN_VALUE = 2;

        public static final String SORT_ORDER_AND_LIMIT_90 = HabitDataEntry.COLUMN_DATE + " DESC LIMIT 90";
    }

    public static final class HabitDataQueryHelper {
        public static final int HABITDATA_LOADER = HabitContract.HABITDATA_LOADER;
        public static final int GRAPHDATA_LOADER = HabitContract.GRAPHDATA_LOADER;

        public static final String SORT_BY_DATE_DESC = HabitContract.HabitDataEntry.COLUMN_DATE + " DESC";
        public static final String SORT_BY_DATE_ASC = HabitContract.HabitDataEntry.COLUMN_DATE + " ASC";

        public static final String[] HABITDATA_PROJECTION = {
                HabitDataEntry._ID,
                HabitDataEntry.COLUMN_DATE,
                HabitDataEntry.COLUMN_VALUE,
                HabitDataEntry.COLUMN_ROLLING_AVG_7,
                HabitDataEntry.COLUMN_ROLLING_AVG_30,
                HabitDataEntry.COLUMN_ROLLING_AVG_90,
                HabitDataEntry.COLUMN_TYPE
        };

        public static final int COLUMN_HABIT_ID = 0;
        public static final int COLUMN_DATE = 1;
        public static final int COLUMN_VALUE = 2;
        public static final int COLUMN_ROLLING_AVG_7 = 3;
        public static final int COLUMN_ROLLING_AVG_30 = 4;
        public static final int COLUMN_ROLLING_AVG_90 = 5;
        public static final int COLUMN_TYPE = 6;

        //// TODO: 3/16/2017
//        //EX. URI content://com.outlook.notyetapp/activities/#/habitdata
//        public static Uri buildHabitDataUriForActivity(long activityId){
//            return ActivitiesEntry.buildActivityUri(activityId).buildUpon().appendPath(PATH_HABIT_DATA).build();
//        }
    }

    public static final class HabitDataOldestQueryHelper {
        public static final String SORT_BY_DATE_ASC_LIMIT_1 = HabitContract.HabitDataEntry.COLUMN_DATE + " ASC LIMIT 1";

        public static final String[] HABITDATA_OLDEST_PROJECTION = {
                HabitDataEntry.COLUMN_DATE
        };

        public static final int COLUMN_DATE = 0;

        //EX. URI content://com.outlook.notyetapp/activities/#/habitdata
        public static Uri buildHabitDataUriForActivity(long activityId){
            return ActivitiesEntry.buildActivityUri(activityId).buildUpon().appendPath(PATH_HABIT_DATA).build();
        }
    }

    public static final class UpdateStatsTaskQueryHelper {

        public static final String[] MAX_UPDATE_STATS_TASK_PROJECTION = {
                "MAX(" + HabitDataEntry.COLUMN_ROLLING_AVG_7 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_7,
                "MAX(" + HabitDataEntry.COLUMN_ROLLING_AVG_30 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_30,
                "MAX(" + HabitDataEntry.COLUMN_ROLLING_AVG_90 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_90
        };
        public static final String[] MIN_UPDATE_STATS_TASK_PROJECTION = {
                "MIN(" + HabitDataEntry.COLUMN_ROLLING_AVG_7 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_7,
                "MIN(" + HabitDataEntry.COLUMN_ROLLING_AVG_30 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_30,
                "MIN(" + HabitDataEntry.COLUMN_ROLLING_AVG_90 + ") AS " + HabitDataEntry.COLUMN_ROLLING_AVG_90
        };

        public static final String SELECT_BY_ACTIVITY_ID = HabitDataEntry.COLUMN_ACTIVITY_ID + " = ?";

        public static final int COLUMN_ROLLING_AVG_7 = 0;
        public static final int COLUMN_ROLLING_AVG_30 = 1;
        public static final int COLUMN_ROLLING_AVG_90 = 2;

        public static final String GROUP_BY_ACTIVITY_ID = HabitDataEntry.COLUMN_ACTIVITY_ID;

        // URI content://com.outlook.notyetapp/activities/#/stats
        public static Uri buildUriForHabitDataStatsForActivityId(long activityId) {
            return ActivitiesEntry.buildActivityUri(activityId).buildUpon().appendPath(PATH_STATS).build();
        }
    }
}
