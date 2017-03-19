package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.screen.graph.GraphActivityContract;
import com.outlook.notyetapp.screen.graph.GraphActivityPresenter;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {})
public class GraphActivityModule {

    GraphActivityContract.View view;

    public GraphActivityModule(GraphActivityContract.View view) {
        this.view = view;
    }

    @Provides
    public GraphActivityContract.ActionListener graphActivityPresenter(GraphActivityContract.View view,
                                                                       StorIOContentResolverHelper storIOContentResolverHelper,
                                                                       CursorToDataPointListHelper cursorToDataPointListHelper){
        return new GraphActivityPresenter(view, storIOContentResolverHelper, cursorToDataPointListHelper);
    }

    @Provides
    public GraphActivityContract.View graphActivityContractView(){
        return view;
    }
}