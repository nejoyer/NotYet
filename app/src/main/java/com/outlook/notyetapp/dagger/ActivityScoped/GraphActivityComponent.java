package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.dagger.scope.ActivityScope;
import com.outlook.notyetapp.screen.graph.GraphActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = NotYetApplicationComponent.class,
        modules = {GraphActivityModule.class})
public interface GraphActivityComponent {
    void inject(GraphActivity graphActivity);
}
