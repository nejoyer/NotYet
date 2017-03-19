package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.dagger.scope.ActivityScope;
import com.outlook.notyetapp.screen.main.MainActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = NotYetApplicationComponent.class,
        modules = {MainActivityModule.class})
public interface MainActivityComponent {
    void inject(MainActivity mainActivity);
}