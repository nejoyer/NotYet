package com.outlook.notyetapp.utilities.rx;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.data.models.HabitDataOldestDate;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;
import rx.functions.Func2;

// Pull a couple of mapping functions into their own class for easier unit testing.
public class RXMappingFunctionHelper {
    public Func1<List<HabitDataOldestDate>, ArrayList<Long>> getHabitDataOldestDateToLongMappingFunction(final int numberOfDaysToAdd){
        return new Func1<List<HabitDataOldestDate>, ArrayList<Long>>() {
            @Override
            public ArrayList<Long> call(List<HabitDataOldestDate> habitDataOldestDates) {
                long oldestDate = habitDataOldestDates.get(0).date;
                ArrayList<Long> datesToAdd = new ArrayList<Long>(numberOfDaysToAdd);

                for(Long date = oldestDate - numberOfDaysToAdd; date < oldestDate; date++) {
                    datesToAdd.add(date);
                }
                return datesToAdd;
            }
        };
    }

    public Func2<ArrayList<Long>, List<ActivitySettings>, UpdateHabitDataHelper.Params> getLongActivitySettingsZipToUpdateHabitDataHelperParamsFunction(){
        return new Func2<ArrayList<Long>, List<ActivitySettings>, UpdateHabitDataHelper.Params>() {
            @Override
            public UpdateHabitDataHelper.Params call(ArrayList<Long> longs, List<ActivitySettings> activitySettingses) {

                return new UpdateHabitDataHelper.Params(
                        activitySettingses.get(0)._id,
                        longs,
                        activitySettingses.get(0).historical,
                        HabitContract.HabitDataEntry.HabitValueType.HISTORICAL);
            }
        };
    }
}
