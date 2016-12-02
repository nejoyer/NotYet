package com.outlook.notyetapp.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.outlook.notyetapp.data.HabitContract.ActivitiesEntry;
import com.outlook.notyetapp.data.HabitContract.HabitDataEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "notyet.db";

    private Context mContext = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold Activities.
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
        // TODO
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

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
