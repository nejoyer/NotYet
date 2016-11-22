package com.outlook.notyetapp;

import android.content.ContentValues;

import com.outlook.notyetapp.data.HabitContract;

// Class to make it easy to calculate the rolling averages needed in this app.
public class RollingAverageHelper {

    private RollingAverageCalculator mAvg7;
    private RollingAverageCalculator mAvg30;
    private RollingAverageCalculator mAvg90;

    public RollingAverageHelper() {
        mAvg7 = new RollingAverageCalculator(7);
        mAvg30 = new RollingAverageCalculator(30);
        mAvg90 = new RollingAverageCalculator(90);
    }

    public void PushNumber(float number) {
        mAvg7.PushValue(number);
        mAvg30.PushValue(number);
        mAvg90.PushValue(number);
    }

    public ContentValues GetValues() {
        ContentValues values = new ContentValues();
        values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7, this.GetAverage7());
        values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30, this.GetAverage30());
        values.put(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90, this.GetAverage90());
        return values;
    }

    public float GetAverage7() {
        return mAvg7.GetAverage();
    }
    public float GetAverage30() {
        return mAvg30.GetAverage();
    }
    public float GetAverage90() {
        return mAvg90.GetAverage();
    }

    private class RollingAverageCalculator{
        private int mSize;
        private float[] mItems;
        private boolean isFull = false;
        private int curPointer = 0;

        public RollingAverageCalculator(int size) {
            mSize = size;
            mItems = new float[mSize];
        }

        //todo... maintain the average by subracting and adding
        // Low Pri. I did basic perf tests to indicate that making this change
        // would barely save anything compared to the DB call that
        // is always called in conjunction with calculating an average
        // in this app.
        public void PushValue(float val)
        {
            mItems[curPointer] = val;
            curPointer++;
            if(curPointer == mSize)
            {
                curPointer = 0;
                isFull = true;
            }
        }

        public float GetAverage(){
            int itemsToAverage = curPointer;
            if(isFull)
            {
                itemsToAverage = mSize;
            }
            float total = 0;
            for(int i = 0; i < itemsToAverage; i++)
            {
                total += mItems[i];
            }
            return total/itemsToAverage;
        }
    }
}
