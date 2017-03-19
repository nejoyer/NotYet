package com.outlook.notyetapp.dagger.ActivityScoped;

import android.content.Context;

import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.screen.main.MainActivityContract;
import com.outlook.notyetapp.screen.main.MainActivityPresenter;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {})
public class MainActivityModule {

    MainActivityContract.View view;

    public MainActivityModule(MainActivityContract.View view) {
        this.view = view;
    }

    @Provides
    public MainActivityContract.ActionListener mainActivityPresenter(MainActivityContract.View view,
                                                                     StorIOContentResolverHelper storIOContentResolverHelper,
                                                                     HabitContractUriBuilder habitContractUriBuilder,
                                                                     DateHelper dateHelper,
                                                                     Context context,
                                                                     SharedPreferencesManager sharedPreferencesManager,
                                                                     UpdateHabitDataHelper updateHabitDataHelper,
                                                                     UpdateStatsHelper updateStatsHelper,
                                                                     RecentDataHelper recentDataHelper){
        return new MainActivityPresenter(view,
                storIOContentResolverHelper,
                habitContractUriBuilder,
                dateHelper,
                context,
                sharedPreferencesManager,
                updateHabitDataHelper,
                updateStatsHelper,
                recentDataHelper);
    }

    @Provides
    public MainActivityContract.View mainActivityContractView(){
        return view;
    }
}
