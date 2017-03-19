package com.outlook.notyetapp.dagger.ApplicationScoped;


import android.content.Context;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverDeleteResolver;
import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverGetResolver;
import com.outlook.notyetapp.data.models.ActivitySettingsStorIOContentResolverPutResolver;
import com.outlook.notyetapp.data.models.HabitData;
import com.outlook.notyetapp.data.models.HabitDataOldestDate;
import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverDeleteResolver;
import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverGetResolver;
import com.outlook.notyetapp.data.models.HabitDataOldestDateStorIOContentResolverPutResolver;
import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverDeleteResolver;
import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverGetResolver;
import com.outlook.notyetapp.data.models.HabitDataStorIOContentResolverPutResolver;
import com.outlook.notyetapp.data.models.RecentData;
import com.outlook.notyetapp.data.models.RecentDataStorIOContentResolverDeleteResolver;
import com.outlook.notyetapp.data.models.RecentDataStorIOContentResolverGetResolver;
import com.outlook.notyetapp.data.models.RecentDataStorIOContentResolverPutResolver;
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, PresenterModule.class})
public class StorIOContentResolverModule {

    @Provides
    @ApplicationScope
    public StorIOContentResolver storIOContentResolver(Context context) {
        return DefaultStorIOContentResolver
                .builder()
                .contentResolver(context.getContentResolver())
                .addTypeMapping(HabitData.class,
                        ContentResolverTypeMapping.<HabitData>builder()
                                .putResolver(new HabitDataStorIOContentResolverPutResolver())
                                .getResolver(new HabitDataStorIOContentResolverGetResolver())
                                .deleteResolver(new HabitDataStorIOContentResolverDeleteResolver())
                                .build()
                )
                .addTypeMapping(HabitDataOldestDate.class,
                        ContentResolverTypeMapping.<HabitDataOldestDate>builder()
                                .putResolver(new HabitDataOldestDateStorIOContentResolverPutResolver())
                                .getResolver(new HabitDataOldestDateStorIOContentResolverGetResolver())
                                .deleteResolver(new HabitDataOldestDateStorIOContentResolverDeleteResolver())
                                .build()
                )
                .addTypeMapping(ActivitySettings.class,
                        ContentResolverTypeMapping.<ActivitySettings>builder()
                                .putResolver(new ActivitySettingsStorIOContentResolverPutResolver())
                                .getResolver(new ActivitySettingsStorIOContentResolverGetResolver())
                                .deleteResolver(new ActivitySettingsStorIOContentResolverDeleteResolver())
                                .build()
                )
                .addTypeMapping(RecentData.class,
                        ContentResolverTypeMapping.<RecentData>builder()
                                .putResolver(new RecentDataStorIOContentResolverPutResolver())
                                .getResolver(new RecentDataStorIOContentResolverGetResolver())
                                .deleteResolver(new RecentDataStorIOContentResolverDeleteResolver())
                                .build()
                )
                .build();
    }

    @Provides
    @ApplicationScope
    public StorIOContentResolverHelper storIOContentResolverHelper(StorIOContentResolver storIOContentResolver,
                                                                   HabitContractUriBuilder habitContractUriBuilder){
        return new StorIOContentResolverHelper(storIOContentResolver, habitContractUriBuilder);
    }
}
