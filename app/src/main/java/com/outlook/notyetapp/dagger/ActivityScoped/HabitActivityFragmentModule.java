package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentPresenter;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {})
public class HabitActivityFragmentModule {

    HabitActivityFragmentContract.View view;

    public HabitActivityFragmentModule(HabitActivityFragmentContract.View view) {
        this.view = view;
    }

    @Provides
    public HabitActivityFragmentContract.ActionListener habitActivityFragmentPresenter(HabitActivityFragmentContract.View view,
                                                                                       CursorToDataPointListHelper cursorToDataPointListHelper,
                                                                                       UpdateHabitDataHelper updateHabitDataHelper,
                                                                                       RecentDataHelper recentDataHelper,
                                                                                       UpdateStatsHelper updateStatsHelper,
                                                                                       HabitContractUriBuilder habitContractUriBuilder,
                                                                                       StorIOContentResolverHelper storIOContentResolverHelper,
                                                                                       RXMappingFunctionHelper rxMappingFunctionHelper,
                                                                                       DateHelper dateHelper){
        return new HabitActivityFragmentPresenter(view,
                cursorToDataPointListHelper,
                updateHabitDataHelper,
                recentDataHelper,
                updateStatsHelper,
                habitContractUriBuilder,
                storIOContentResolverHelper,
                rxMappingFunctionHelper,
                dateHelper);
    }

    @Provides
    public HabitActivityFragmentContract.View habitActivityFragmentContractView(){
        return view;
    }

}
