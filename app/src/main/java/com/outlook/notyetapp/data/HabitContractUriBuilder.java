package com.outlook.notyetapp.data;

import android.net.Uri;

// Helper for building Uris. Pulled into this class so that it can be mocked for unit tests.
public class HabitContractUriBuilder {

    public Uri buildHabitDataUriForActivity(long activityId){
        return HabitContract.HabitDataEntry.buildUriForAllHabitDataForActivityId(activityId);
    }
    public Uri buildActivityUri(long activityId){
        return HabitContract.ActivitiesEntry.buildActivityUri(activityId);
    }

    public Uri buildUriForHabitDataStatsForActivityId(long activityId){
        return HabitContract.UpdateStatsTaskQueryHelper.buildUriForHabitDataStatsForActivityId(activityId);
    }

    public Uri buildActivitiesStatsUri(){
        return HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri();
    }

    public Uri getRecentDataUri(){
        return HabitContract.RecentDataQueryHelper.RECENT_DATA_URI;
    }

    public Uri getHabitDataEntryUri(){
        return HabitContract.HabitDataEntry.CONTENT_URI;
    }
}
