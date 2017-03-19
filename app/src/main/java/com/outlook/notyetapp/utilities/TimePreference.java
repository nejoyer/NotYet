package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.outlook.notyetapp.R;

import java.util.Calendar;
import java.util.Date;

// Allow the user to use a cool clock time picker to specify a preference.
// In this case, they can say when the end of their day is... so if they usually floss before bed
// and they are out until 1 am... they can say that their day ends at 3 am and so flossing will still count
// towards what they consider the current day.
// For programming convenience the user's preference is stored as the number of milliseconds extra that they want
// shift the day by.
// Note: They can only shift the day backwards, not forwards, so a date like 11:00 pm,
// will be like giving them a whole extra day to complete tasks and doesn't make any sense.
// a normal value should be like 2am, 3am, or 5am.

public class TimePreference extends DialogPreference {
    private int lastHour=0;
    private int lastMinute=0;
    private TimePicker picker=null;

    Calendar midnight = null;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(context.getString(R.string.pref_day_change_set));
        setNegativeButtonText(context.getString(R.string.pref_day_change_cancel));
        midnight = Calendar.getInstance();
        midnight.setTime(new Date(0));
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
    }

    @Override
    protected View onCreateDialogView() {
        picker=new TimePicker(getContext());
        return(picker);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        this.setSummary(this.getText());
        return super.onCreateView(parent);
    }

    private Calendar getCalendarFromMillisecondsPastMidnight(long millisecondsPastMidnight)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, 0);
        cal.setTime(new Date(millisecondsPastMidnight + 21600000));
        return cal;
    }

    private long getSettingMilliseconds(int hour, int lastMinute)
    {
        Calendar cal = (Calendar) midnight.clone();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, lastMinute);

        return cal.getTimeInMillis() - midnight.getTimeInMillis();
    }

    private String getText()
    {
        Calendar cal = getCalendarFromMillisecondsPastMidnight(getSettingMilliseconds(lastHour, lastMinute));

        return (String)DateFormat.format("hh:mm a", cal);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        this.setSummary(this.getText());
        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour=picker.getCurrentHour();
            lastMinute=picker.getCurrentMinute();

            long millisSinceMidnight = getSettingMilliseconds(lastHour, lastMinute);

            if (callChangeListener(String.valueOf(millisSinceMidnight))) {
                persistString(String.valueOf(millisSinceMidnight));
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String millisecondsPastMidnight = null;

        if (restoreValue) {
            if (defaultValue==null || defaultValue == "") {
                millisecondsPastMidnight = getPersistedString("0");
            } else {
                millisecondsPastMidnight = getPersistedString(defaultValue.toString());
            }
        } else {
            if (defaultValue==null || defaultValue.equals("")) {
                millisecondsPastMidnight = "0";
            } else {
                millisecondsPastMidnight = defaultValue.toString();
            }
        }

        Calendar cal = getCalendarFromMillisecondsPastMidnight(Long.valueOf(millisecondsPastMidnight));

        lastHour = cal.get(Calendar.HOUR_OF_DAY);
        lastMinute = cal.get(Calendar.MINUTE);
    }
}
