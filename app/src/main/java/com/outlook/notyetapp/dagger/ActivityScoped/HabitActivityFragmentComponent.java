package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.dagger.scope.ActivityScope;
import com.outlook.notyetapp.screen.habit.HabitActivityFragment;

import dagger.Component;

@ActivityScope
@Component(dependencies = NotYetApplicationComponent.class,
        modules = {HabitActivityFragmentModule.class})
public interface HabitActivityFragmentComponent {

    void inject(HabitActivityFragment habitActivityFragment);

}
