package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.graph.GraphActivityContract;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Neil on 2/9/2017.
 */

@Module
public class GraphActivityContractViewModule {

    GraphActivityContract.View view;

    public GraphActivityContractViewModule(GraphActivityContract.View view) {
        this.view = view;
    }

    @Provides
    public GraphActivityContract.View graphActivityContractView(){
        return view;
    }
}
