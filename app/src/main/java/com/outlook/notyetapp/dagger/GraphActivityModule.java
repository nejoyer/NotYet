package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.screen.graph.GraphActivityContract;
import com.outlook.notyetapp.screen.graph.GraphActivityPresenter;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;

import dagger.Module;
import dagger.Provides;

@Module(includes = {GraphActivityContractViewModule.class, PresenterModule.class})
public class GraphActivityModule {
    @Provides
    public GraphActivityContract.ActionListener graphActivityPresenter(GraphActivityContract.View view,
                                                                       StorIOContentResolver storIOContentResolver,
                                                                       CursorToDataPointListHelper cursorToDataPointListHelper){
        return new GraphActivityPresenter(view, storIOContentResolver, cursorToDataPointListHelper);
    }
}