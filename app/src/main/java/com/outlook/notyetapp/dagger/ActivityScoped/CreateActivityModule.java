package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.screen.createactivity.CreateActivityContract;
import com.outlook.notyetapp.screen.createactivity.CreateActivityPresenter;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {})
public class CreateActivityModule {
    CreateActivityContract.View view;

    public CreateActivityModule(CreateActivityContract.View view) {
        this.view = view;
    }

    @Provides
    public CreateActivityContract.View createActivityContractView(){
        return this.view;
    }

    @Provides
    public CreateActivityContract.ActionListener createActivityPresenter(CreateActivityContract.View createActivityContractView,
                                                                         StorIOContentResolverHelper storIOContentResolverHelper,
                                                                         RecentDataHelper recentDataHelper){
        return new CreateActivityPresenter(createActivityContractView, storIOContentResolverHelper, recentDataHelper);
    }
}
