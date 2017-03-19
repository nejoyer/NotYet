package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.dagger.scope.ActivityScope;
import com.outlook.notyetapp.screen.createactivity.CreateActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = NotYetApplicationComponent.class,
        modules = {CreateActivityModule.class})
public interface CreateActivityComponent {
    void inject(CreateActivity createActivity);
}
