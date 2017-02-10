package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Neil on 2/9/2017.
 */

@Module
public class HabitActivityFragmentContractViewModule {

    HabitActivityFragmentContract.View view;

    public HabitActivityFragmentContractViewModule(HabitActivityFragmentContract.View view) {
        this.view = view;
    }

    @Provides
    public HabitActivityFragmentContract.View habitActivityFragmentContractView(){
        return view;
    }
}
