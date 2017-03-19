package com.outlook.notyetapp.data;

import android.text.format.Time;

import java.util.Date;

// Used to work with the DBDate (long number of days since the julian era)
// respects the user's preferred start time to the day
public class DateHelper {

    private SharedPreferencesManager sharedPreferencesManager;

    public DateHelper(SharedPreferencesManager sharedPreferencesManager) {
        this.sharedPreferencesManager = sharedPreferencesManager;
    }

    public long getTodaysDBDateIgnoreOffset(){
        return HabitContract.HabitDataEntry.getTodaysDBDate(0);
    }

    public long getTodaysDBDate(){
        long offset = Long.parseLong(sharedPreferencesManager.getOffset());
        Time time = new Time();
        time.set(System.currentTimeMillis());
        int julianDay = Time.getJulianDay(System.currentTimeMillis() - offset, time.gmtoff);
        return (long) julianDay;
    }

    public Date getTodaysDBDateAsDate(){
        return convertDBDateToDate(getTodaysDBDate());
    }

    public Date convertDBDateToDate(long daysSinceJulianEraStart){
        Time time = new Time();
        time.setJulianDay((int)daysSinceJulianEraStart);
        return new Date(time.toMillis(false));
    }
}
