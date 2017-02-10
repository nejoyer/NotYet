package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.graph.GraphActivity;
import com.outlook.notyetapp.utilities.GraphUtilities;

import dagger.Component;

@Component(modules = {GraphActivityModule.class, GraphUtilitiesModule.class})
public interface GraphActivityComponent {
    void inject(GraphActivity graphActivity);
}
