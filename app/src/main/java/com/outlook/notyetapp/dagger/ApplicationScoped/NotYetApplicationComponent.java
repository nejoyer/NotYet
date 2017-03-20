package com.outlook.notyetapp.dagger.ApplicationScoped;

import android.content.Context;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.utilities.ContentProviderOperationFactory;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import dagger.Component;

@ApplicationScope
@Component(modules = {StorIOContentResolverModule.class, PresenterModule.class, GraphUtilitiesModule.class})
public interface NotYetApplicationComponent {
    StorIOContentResolverHelper storIOContentResolverHelper();
    CursorToDataPointListHelper cursorToDataPointListHelper();
    DateHelper dateHelper();
    UpdateHabitDataHelper updateHabitDataHelper();
    UpdateStatsHelper updateStatsHelper();
    HabitContractUriBuilder habitContractUriBuilder();
    SharedPreferencesManager sharedPreferencesManager();
    GraphUtilities graphUtilities();
    Context context();
    RecentDataHelper recentDataHelper();
    RXMappingFunctionHelper rxMappingFunctionHelper();
}