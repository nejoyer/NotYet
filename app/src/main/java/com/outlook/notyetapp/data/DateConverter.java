package com.outlook.notyetapp.data;

import android.text.format.Time;

import java.util.Date;

public class DateConverter {
    public Date convertDBDateToDate(long daysSinceJulianEraStart){
        Time time = new Time();
        time.setJulianDay((int)daysSinceJulianEraStart);
        return new Date(time.toMillis(false));
    }
}
