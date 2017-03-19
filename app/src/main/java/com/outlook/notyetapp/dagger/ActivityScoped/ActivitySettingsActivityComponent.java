package com.outlook.notyetapp.dagger.ActivityScoped;

import com.outlook.notyetapp.ActivitySettingsFragment;
import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.dagger.scope.ActivityScope;
import com.outlook.notyetapp.screen.activitysettings.ActivitySettingsActivity;

import dagger.Component;

@ActivityScope
@Component(dependencies = NotYetApplicationComponent.class,
        modules = {ActivitySettingsActivityModule.class})
public interface ActivitySettingsActivityComponent {

    void inject(ActivitySettingsActivity activitySettingsActivity);
    void inject(ActivitySettingsFragment activitySettingsFragment);

}
