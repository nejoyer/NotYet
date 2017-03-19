package com.outlook.notyetapp.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.outlook.notyetapp.data.HabitContract.ActivitiesEntry;
import com.outlook.notyetapp.data.HabitContract.HabitDataEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Used to manage creation, upgrade, and downgrade of the db.
// Also has some utilities for preparing a demo
public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "notyet.db";

    public static final String DOWNGRADE_NOT_SUPPORTED = "Downgrade Not Allowed";

    private Context mContext = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException(DOWNGRADE_NOT_SUPPORTED);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + ActivitiesEntry.TABLE_NAME + " (" +
                ActivitiesEntry._ID + " INTEGER PRIMARY KEY," +
                ActivitiesEntry.COLUMN_ACTIVITY_TITLE + " TEXT UNIQUE NOT NULL, " +
                ActivitiesEntry.COLUMN_HISTORICAL + " REAL NOT NULL, " +
                ActivitiesEntry.COLUMN_FORECAST + " REAL NOT NULL, " +
                ActivitiesEntry.COLUMN_SWIPE_VALUE + " REAL NOT NULL, " +
                ActivitiesEntry.COLUMN_HIGHER_IS_BETTER + " INT(1) NOT NULL, " +
                ActivitiesEntry.COLUMN_DAYS_TO_SHOW + " INT(1) NOT NULL," +
                ActivitiesEntry.COLUMN_BEST7 + " REAL NOT NULL," +
                ActivitiesEntry.COLUMN_BEST30 + " REAL NOT NULL," +
                ActivitiesEntry.COLUMN_BEST90 + " REAL NOT NULL," +
                ActivitiesEntry.COLUMN_SORT_PRIORITY + " INT(2) NOT NULL," +
                ActivitiesEntry.COLUMN_HIDE_DATE + " INTEGER NOT NULL" +
                " );";

        final String SQL_CREATE_HABIT_DATA_TABLE = "CREATE TABLE " + HabitDataEntry.TABLE_NAME + " (" +
                HabitDataEntry._ID + " INTEGER PRIMARY KEY," +
                HabitDataEntry.COLUMN_ACTIVITY_ID + " INTEGER NOT NULL, " +
                HabitDataEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                HabitDataEntry.COLUMN_VALUE + " REAL NOT NULL, " +
                HabitDataEntry.COLUMN_ROLLING_AVG_7 + " REAL NOT NULL, " +
                HabitDataEntry.COLUMN_ROLLING_AVG_30 + " REAL NOT NULL," +
                HabitDataEntry.COLUMN_ROLLING_AVG_90 + " REAL NOT NULL, " +
                HabitDataEntry.COLUMN_TYPE + " INTEGER(1) NOT NULL," +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + HabitDataEntry.COLUMN_ACTIVITY_ID+ ") REFERENCES " +
                ActivitiesEntry.TABLE_NAME + " (" + ActivitiesEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + HabitDataEntry.COLUMN_DATE + ", " +
                HabitDataEntry.COLUMN_ACTIVITY_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_ACTIVITIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_HABIT_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // TODO If I need to change the schema, so far I haven't needed to since release.
        // For now, until we have the schema more set, just drop and re-create.
        // In the future, we'll need an upgrade strategy.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ActivitiesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HabitDataEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    // This is hacky, but is only for demo code, so less important.
    public void copyDemoDB(String demoDBNameInAssetsDirectory){
        AssetManager assetManager = mContext.getAssets();
        File destinationLocation = mContext.getDatabasePath(DBHelper.DATABASE_NAME);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(demoDBNameInAssetsDirectory);
            outputStream = new FileOutputStream(destinationLocation);
            copyFile(inputStream, outputStream);
        }
        catch (IOException e){}
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    // I've included a demo db in the demo version (unreleased) version of this project. But the demo
    // will get stale as no one is entering values. This gets called to rewrite all the dates in the demo
    // db by increasing the date values until it looks as recent as the day it was created.
    public void updateDemoDBByDate(){
        Cursor cursor = null;
        long maxDate = 0;
        try {
            cursor = mContext.getContentResolver().query(
                    HabitContract.RecentDataQueryHelper.RECENT_DATA_URI,
                    HabitContract.RecentDataQueryHelper.RECENT_DATA_PROJECTION,
                    null, //selection
                    null, //selectionArgs
                    null //Sort order
            );

            while (cursor.moveToNext()) {
                long activityMax = cursor.getLong(HabitContract.RecentDataQueryHelper.COLUMN_DATE);
                if (activityMax > maxDate) {
                    maxDate = activityMax;
                }
            }
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
        }
        //This is only used in test code, so DI not a goal.
        DateHelper dateHelper = new DateHelper(new SharedPreferencesManager(mContext));
        long todaysDBDate = dateHelper.getTodaysDBDate();

        if(maxDate > 0 && todaysDBDate > maxDate) {
            long amountToUpdate = todaysDBDate - maxDate;

            // This is a fun trick, when we add the days to update, we invert the number to make it negative.
            // This helps us avoid uniqueness constraints. If we don't do this, about half of the rows will be deleted.
            getWritableDatabase().execSQL("UPDATE " + HabitContract.HabitDataEntry.TABLE_NAME
                    + " SET " + HabitContract.HabitDataEntry.COLUMN_DATE + " = "
                    + " - (" + HabitContract.HabitDataEntry.COLUMN_DATE + " + " + String.valueOf(amountToUpdate) + ")");
            // Then we invert it back.
            getWritableDatabase().execSQL("UPDATE " + HabitContract.HabitDataEntry.TABLE_NAME
                    + " SET " + HabitContract.HabitDataEntry.COLUMN_DATE + " = "
                    + " - " + HabitContract.HabitDataEntry.COLUMN_DATE);

//            getWritableDatabase().execSQL("UPDATE " + HabitContract.ActivitiesEntry.TABLE_NAME
//                    + " SET " + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " = "
//                    + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " + " + String.valueOf(amountToUpdate));
        }
        mContext.getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
