package com.outlook.notyetapp.data.models;


import com.outlook.notyetapp.data.HabitContract;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverColumn;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverType;

@StorIOContentResolverType(uri = "content://com.outlook.notyetapp.demo/habitdata")
public class HabitData {

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry._ID, key = true)
    public Long _id;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_DATE)
    public Long date;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_VALUE)
    public Float value;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7, ignoreNull = true)
    public Float avg7;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30, ignoreNull = true)
    public Float avg30;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90, ignoreNull = true)
    public Float avg90;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_TYPE, ignoreNull = true)
    public Integer type;
}
