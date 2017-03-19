package com.outlook.notyetapp.data.models;

import com.outlook.notyetapp.data.HabitContract;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverColumn;
import com.pushtorefresh.storio.contentresolver.annotations.StorIOContentResolverType;

@StorIOContentResolverType(uri = "content://com.outlook.notyetapp.demo/habitdata")
public class HabitDataOldestDate {
    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry._ID, key = true)
    public Long _id;

    @StorIOContentResolverColumn(name = HabitContract.HabitDataEntry.COLUMN_DATE)
    public Long date;
}
