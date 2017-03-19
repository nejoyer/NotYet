package com.outlook.notyetapp.dagger.ApplicationScoped;

import android.content.Context;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.RollingAverageHelper;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import dagger.Module;
import dagger.Provides;

//import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverDeleteResolver;
//import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverGetResolver;
//import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverPutResolver;
//import com.outlook.notyetapp.data.models.HabitData;
//import com.outlook.notyetapp.data.models.HabitDataOldestDate;
//import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverDeleteResolver;
//import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverGetResolver;
//import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverPutResolver;
//import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverDeleteResolver;
//import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverGetResolver;
//import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverPutResolver;

@Module(includes = {ContextModule.class, GraphModule.class})
public class PresenterModule {


    @Provides
    @ApplicationScope
    public CursorToDataPointListHelper cursorToDataPointListHelper(DateHelper dateHelper,
                                                                   DataPointFactory dataPointFactory,
                                                                   RollingAverageHelper rollingAverageHelper) {
        return new CursorToDataPointListHelper(dateHelper, dataPointFactory, rollingAverageHelper);
    }

    @Provides
    @ApplicationScope
    public DateHelper dateHelper(/*Context context, */SharedPreferencesManager sharedPreferencesManager) {
        return new DateHelper(/*context,*/ sharedPreferencesManager);
    }

    @Provides
    @ApplicationScope
    public UpdateHabitDataHelper updateHabitDataHelper(HabitContractUriBuilder habitContractUriBuilder,
                                                       StorIOContentResolverHelper storIOContentResolverHelper) {
        return new UpdateHabitDataHelper(habitContractUriBuilder, storIOContentResolverHelper);
    }

    @Provides
    @ApplicationScope
    public RecentDataHelper recentDataHelper(StorIOContentResolverHelper storIOContentResolverHelper,
                                             DateHelper dateHelper,
                                             HabitContractUriBuilder habitContractUriBuilder){
        return new RecentDataHelper(storIOContentResolverHelper, dateHelper, habitContractUriBuilder);
    }

    @Provides
    @ApplicationScope
    UpdateStatsHelper updateStatsHelper(HabitContractUriBuilder habitContractUriBuilder,
                                        StorIOContentResolverHelper storIOContentResolverHelper){
        return new UpdateStatsHelper(habitContractUriBuilder, storIOContentResolverHelper);
    }

    @Provides
    @ApplicationScope
    public HabitContractUriBuilder habitContractUriBuilder() {
        return new HabitContractUriBuilder();
    }

    @Provides
    @ApplicationScope
    public SharedPreferencesManager sharedPreferencesManager(Context context){
        return new SharedPreferencesManager(context);
    }

    @Provides
    @ApplicationScope
    public RXMappingFunctionHelper rxMappingFunctionHelper(){
        return new RXMappingFunctionHelper();
    }

    @Provides
    @ApplicationScope
    public RollingAverageHelper tollingAverageHelper(){
        return new RollingAverageHelper();
    }
}
