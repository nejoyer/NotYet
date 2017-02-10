package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.habit.HabitActivityFragment;

import dagger.Component;

@Component(modules = {HabitActivityFragmentModule.class, GraphUtilitiesModule.class})
public interface HabitActivityFragmentComponent {

    void inject(HabitActivityFragment habitActivityFragment);

}
