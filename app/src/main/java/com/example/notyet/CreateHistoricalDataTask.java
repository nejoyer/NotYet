package com.example.notyet;

import android.content.Context;
import android.os.AsyncTask;

import com.example.notyet.data.HabitContract;

// When a new activity is created, create 90 days of data asynchronously using this class.
public class CreateHistoricalDataTask extends AsyncTask<CreateHistoricalDataTask.CreateHistoricalDataTaskParams, Void, Void> {

    @Override
    protected Void doInBackground(CreateHistoricalDataTaskParams... createHistoricalDataTaskParamses) {
        // This should never happen... not localized        
        if(createHistoricalDataTaskParamses.length != 1)
            throw new UnsupportedOperationException("Cannot run the task unless it has 1 param");
        CreateHistoricalDataTaskParams params = createHistoricalDataTaskParamses[0];

        CreateRecentDataTask.CreateRecentDataUsingValue(
                params.mContext,
                params.mActivityId,
                params.mHistoricalValue,
                HabitContract.HabitDataEntry.HabitValueType.HISTORICAL,
                params.mHigherIsBetter);

        return null;
    }

    public static class CreateHistoricalDataTaskParams{

        public Context mContext;
        public long mActivityId;
        public float mHistoricalValue;
        public boolean mHigherIsBetter;

        public CreateHistoricalDataTaskParams(Context context, long activityId, float historicalValue, boolean higherIsBetter) {
            mContext = context;
            mActivityId = activityId;
            mHistoricalValue = historicalValue;
            mHigherIsBetter = higherIsBetter;
        }
    }
}
