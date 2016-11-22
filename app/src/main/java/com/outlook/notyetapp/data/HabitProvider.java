package com.outlook.notyetapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.outlook.notyetapp.R;

public class HabitProvider extends ContentProvider {

    private DBHelper mDBHelper = null;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder activitiesHabitDataJoinQueryBuilder;

    static{
        activitiesHabitDataJoinQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //activites INNER JOIN habitdata ON Activities._id = HabitData.activity_id
        activitiesHabitDataJoinQueryBuilder.setTables(
                HabitContract.ActivitiesEntry.TABLE_NAME + " INNER JOIN " +
                        HabitContract.HabitDataEntry.TABLE_NAME +
                        " ON " + HabitContract.ActivitiesEntry.TABLE_NAME +
                        "." + HabitContract.ActivitiesEntry._ID +
                        " = " + HabitContract.HabitDataEntry.TABLE_NAME +
                        "." + HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return true;
    }

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URI content://com.outlook.notyetapp.app/activities/#
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY,HabitContract.PATH_ACTIVITIES + "/#", HabitContract.ACTIVITY);
        // URI content://com.outlook.notyetapp.app/activities
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_ACTIVITIES, HabitContract.ACTIVITIES);
        // URI content://com.outlook.notyetapp.app/activities/#/stats
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY,HabitContract.PATH_ACTIVITIES + "/#/" + HabitContract.PATH_STATS, HabitContract.ACTIVITY_STATS);
        // URI content://com.outlook.notyetapp.app/activities/stats
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_ACTIVITIES + "/" + HabitContract.PATH_STATS, HabitContract.ACTIVITIES_TODAYS_STATS);
        // URI content://com.outlook.notyetapp.app/activities/mostrecent
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_ACTIVITIES + "/" + HabitContract.PATH_RECENT, HabitContract.ACTIVITIES_MOST_RECENT);
        // URI content://com.outlook.notyetapp.app/habitdata/#
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABIT_DATA + "/#", HabitContract.HABITDATA_ENTRY);
        // URI content://com.outlook.notyetapp.app/habitdata
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABIT_DATA, HabitContract.HABITDATA_ENTRIES);
        // URI content://com.outlook.notyetapp.app/activities/#/habitdata
        uriMatcher.addURI(HabitContract.CONTENT_AUTHORITY,
                HabitContract.PATH_ACTIVITIES + "/#/" + HabitContract.PATH_HABIT_DATA,
                HabitContract.ACTIVITY_HABITDATA_ENTRIES);

        return uriMatcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // returns the type... including if it is one item or many
            case HabitContract.ACTIVITY:
                return  HabitContract.ActivitiesEntry.CONTENT_ITEM_TYPE;
            case HabitContract.ACTIVITIES:
                return  HabitContract.ActivitiesEntry.CONTENT_TYPE;
            case HabitContract.ACTIVITY_STATS:
                return  HabitContract.ActivitiesEntry.CONTENT_TYPE_ACTIVITY_STATS;
            case HabitContract.ACTIVITIES_TODAYS_STATS:
                return  HabitContract.ActivitiesEntry.CONTENT_TYPE_ACTIVITIES_STATS;
            case HabitContract.ACTIVITIES_MOST_RECENT:
                return  HabitContract.ActivitiesEntry.CONTENT_TYPE_MOST_RECENT;
            case HabitContract.HABITDATA_ENTRY:
                return  HabitContract.HabitDataEntry.CONTENT_ITEM_TYPE;
            case HabitContract.HABITDATA_ENTRIES:
                return  HabitContract.HabitDataEntry.CONTENT_TYPE;
            case HabitContract.ACTIVITY_HABITDATA_ENTRIES:
                return  HabitContract.HabitDataEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }



    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            // content://com.outlook.notyetapp.app/activities/#
            case HabitContract.ACTIVITY:
            {
                //Note, here we ignore the user's selection and selection args in favor of our own which is what this Uri is intended for.
                retCursor = getActivity(uri, projection, sortOrder);
                break;
            }
            // content://com.outlook.notyetapp.app/activities
            case HabitContract.ACTIVITIES:
            {
                //Note, here we ignore the user's selection and selection args in favor of our own which is what this Uri is intended for.
                retCursor = mDBHelper.getReadableDatabase().query(
                        HabitContract.ActivitiesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, //group by
                        null, //having
                        sortOrder
                );
                break;
            }
            // content://com.outlook.notyetapp.app/activities/stats
            case HabitContract.ACTIVITIES_TODAYS_STATS:
            {
                retCursor = activitiesHabitDataJoinQueryBuilder.query(mDBHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,//GroupBy
                        null,//Having
                        sortOrder);
                break;
            }
            // content://com.outlook.notyetapp.app/activities/mostrecent
            case HabitContract.ACTIVITIES_MOST_RECENT: {
                long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getContext().getString(R.string.pref_day_change_key), "0"));
                retCursor = activitiesHabitDataJoinQueryBuilder.query(mDBHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        HabitContract.RecentDataQueryHelper.GROUP_BY_ACTIVITY_ID,
                        HabitContract.RecentDataQueryHelper.getHavingStatement(offset),//having
                        sortOrder);
                break;
            }
            // commented this out so it results in unsupported...
            // content://com.outlook.notyetapp.app/habitdata/#
            //case HabitContract.HABITDATA_ENTRY: {
            //    break;
            //}
            // content://com.outlook.notyetapp.app/activities/#/stats
            case HabitContract.ACTIVITY_STATS: {
                long activityId = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);
                retCursor = mDBHelper.getReadableDatabase()
                        .query(HabitContract.HabitDataEntry.TABLE_NAME,
                                projection,
                                HabitContract.UpdateStatsTaskQueryHelper.SELECT_BY_ACTIVITY_ID,
                                new String[]{String.valueOf(activityId)},
                                HabitContract.UpdateStatsTaskQueryHelper.GROUP_BY_ACTIVITY_ID,
                                null,//having
                                null, //order by
                                sortOrder);
                break;
            }
            // content://com.outlook.notyetapp.app/activities/#/habitdata
            case HabitContract.ACTIVITY_HABITDATA_ENTRIES: {
                retCursor = getHabitData(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //todo
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case HabitContract.ACTIVITIES: {
                long _id = db.insert(HabitContract.ActivitiesEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = HabitContract.ActivitiesEntry.buildActivityUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case HabitContract.HABITDATA_ENTRIES: {
                long _id = db.insert(HabitContract.HabitDataEntry.TABLE_NAME, null, contentValues);
                if(_id > 0)
                    returnUri = HabitContract.HabitDataEntry.buildHabitDataUriForHabitDataEntryId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //todo
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereClauseArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            // content://com.outlook.notyetapp.app/activities/#
            case HabitContract.ACTIVITY: {
                //Note, here we ignore the user's selection and selection args in favor of our own which is what this Uri is intended for.
                long activityId = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);
                whereClause = HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID + " = ?";
                db.delete(HabitContract.HabitDataEntry.TABLE_NAME, whereClause, new String[]{String.valueOf(activityId)});
                Uri uriToNotify = HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(activityId);
                getContext().getContentResolver().notifyChange(uriToNotify, null);
                whereClause = HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_ID + " = ?";
                db.delete(HabitContract.ActivitiesEntry.TABLE_NAME, whereClause, new String[]{String.valueOf(activityId)});
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            // content://com.outlook.notyetapp.app/activities/#
            case HabitContract.ACTIVITY:
            {
                //Note, here we ignore the user's selection and selection args in favor of our own which is what this Uri is intended for.
                long activityNumber = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);
                selection = HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(activityNumber)};
                rowsUpdated = db.update(HabitContract.ActivitiesEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            // content://com.outlook.notyetapp.app/habitdata/#
            case HabitContract.HABITDATA_ENTRY:
            {
                long habitDataId = HabitContract.HabitDataEntry.getHabitNumberFromUri(uri);
                selection = HabitContract.HabitDataEntry.COLUMN_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(habitDataId)};
                rowsUpdated = db.update(HabitContract.HabitDataEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                if(rowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            }
            //content://com.outlook.notyetapp/activities/#/habitdata
            case HabitContract.ACTIVITY_HABITDATA_ENTRIES:
            {
                long activityNumber = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);

                String[] newSelectionArgs = null;
                if(selection != null) {
                    selection += " AND ";
                    // There are utilities in libraries to do this, but this case isn't that hard.
                    newSelectionArgs = new String[selectionArgs.length + 1];
                    for(int i = 0; i<selectionArgs.length; i++)
                    {
                        newSelectionArgs[i] = selectionArgs[i];
                    }
                }
                else {
                    selection = "";
                    newSelectionArgs = new String[1];
                }
                selection += HabitContract.HabitDataEntry.TABLE_NAME +
                        "." + HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID + " = ? ";
                newSelectionArgs[newSelectionArgs.length - 1] = String.valueOf(activityNumber);

                rowsUpdated = db.update(HabitContract.HabitDataEntry.TABLE_NAME, contentValues, selection, newSelectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //todo
        if(rowsUpdated > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HabitContract.HABITDATA_ENTRIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(HabitContract.HabitDataEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getHabitData(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        long activityNumber = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);

        String[] newSelectionArgs = null;
        if(selection != null) {
            selection += " AND ";
            // There are utilities in libraries to do this, but this case isn't that hard.
            newSelectionArgs = new String[selectionArgs.length + 1];
            for(int i = 0; i<selectionArgs.length; i++)
            {
                newSelectionArgs[i] = selectionArgs[i];
            }
        }
        else {
            selection = "";
            newSelectionArgs = new String[1];
        }
        selection += HabitContract.HabitDataEntry.TABLE_NAME +
                "." + HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID + " = ? ";
        newSelectionArgs[newSelectionArgs.length - 1] = String.valueOf(activityNumber);

        return mDBHelper.getReadableDatabase()
                .query(HabitContract.HabitDataEntry.TABLE_NAME,
                        projection,
                        selection,
                        newSelectionArgs,
                        null,
                        null,
                        sortOrder);
    }

    private Cursor getActivity(Uri uri, String[] projection, String sortOrder){
        long id = HabitContract.ActivitiesEntry.getActivityNumberFromUri(uri);
        String selection = HabitContract.ActivitiesEntry.TABLE_NAME + "." + HabitContract.ActivitiesEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return mDBHelper.getReadableDatabase()
                .query(HabitContract.ActivitiesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
    }
}
