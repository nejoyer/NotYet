package com.outlook.notyetapp.data.models;

import com.outlook.notyetapp.data.HabitContract;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverColumn;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverType;

@StorIOContentResolverType(uri = "content://com.outlook.notyetapp.demo/activities/mostrecent")
public class RecentData {

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID, key = true)
    public Long _id;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_DATE)
    public Long date;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_HIGHER_IS_BETTER)
    public int higherIsBetter;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_HISTORICAL)
    public float historical;
}
