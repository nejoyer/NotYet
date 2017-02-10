package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentPresenter;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;

import dagger.Module;
import dagger.Provides;

@Module(includes = {HabitActivityFragmentContractViewModule.class, PresenterModule.class})
public class HabitActivityFragmentModule {

    @Provides
    public HabitActivityFragmentContract.ActionListener habitActivityFragmentPresenter(HabitActivityFragmentContract.View view,
                                                                                       StorIOContentResolver storIOContentResolver,
                                                                                       CursorToDataPointListHelper cursorToDataPointListHelper){
        return new HabitActivityFragmentPresenter(view, storIOContentResolver, cursorToDataPointListHelper);
    }



}
