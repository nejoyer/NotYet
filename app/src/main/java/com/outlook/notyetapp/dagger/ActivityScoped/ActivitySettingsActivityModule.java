package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.screen.activitysettings.ActivitySettingsActivityContract;
import com.outlook.notyetapp.screen.activitysettings.ActivitySettingsActivityPresenter;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import dagger.Module;
import dagger.Provides;

@Module(includes = {})
public class ActivitySettingsActivityModule {

    ActivitySettingsActivityContract.View view;

    public ActivitySettingsActivityModule(ActivitySettingsActivityContract.View view) {
        this.view = view;
    }

    @Provides
    public ActivitySettingsActivityContract.ActionListener activitySettingsActivityPresenter(ActivitySettingsActivityContract.View view,
                                                                                          StorIOContentResolverHelper storIOContentResolverHelper,
                                                                                             HabitContractUriBuilder habitContractUriBuilder,
                                                                                             UpdateHabitDataHelper updateHabitDataHelper,
                                                                                             UpdateStatsHelper updateStatsHelper) {
        return new ActivitySettingsActivityPresenter(view, storIOContentResolverHelper, habitContractUriBuilder, updateHabitDataHelper, updateStatsHelper);
    }

    @Provides
    public ActivitySettingsActivityContract.View activitySettingsContractView(){
        return view;
    }
}
