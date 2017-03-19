package com.outlook.notyetapp.data.models;

import com.outlook.notyetapp.data.HabitContract;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverColumn;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverType;

@StorIOContentResolverType(uri = "YOU MUST USE THE BUILDER TO PASS A URI")
public class ActivitySettings {

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry._ID, key = true)
    public long _id;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE)
    public String title;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_HISTORICAL)
    public Float historical;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_FORECAST)
    public Float forecast;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_SWIPE_VALUE)
    public Float swipeValue;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_HIGHER_IS_BETTER)
    public Integer higherIsBetter;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_DAYS_TO_SHOW)
    public Integer daysToShow;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_BEST7, ignoreNull = true)
    public Float best7;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_BEST30, ignoreNull = true)
    public Float best30;

    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_BEST90, ignoreNull = true)
    public Float best90;
//
//    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY, ignoreNull = true)
//    public Integer sortPriority;
//
//    @StorIOContentResolverColumn(name = HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE, ignoreNull = true)
//    public Long hideDate;
}