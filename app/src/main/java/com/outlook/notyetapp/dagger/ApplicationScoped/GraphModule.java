package com.outlook.notyetapp.dagger.ApplicationScoped;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;
import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.LineGraphSeriesFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class GraphModule {

    @Provides
    @ApplicationScope
    public LineGraphSeriesFactory lineGraphSeriesFactory(){
        return new LineGraphSeriesFactory();
    }

    @Provides
    @ApplicationScope
    public DataPointFactory dataPointFactory(){
        return new DataPointFactory();
    }
}
