package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.LineGraphSeriesFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class GraphModule {
    @Provides
    public LineGraphSeriesFactory lineGraphSeriesFactory(){
        return new LineGraphSeriesFactory();
    }

    @Provides
    public DataPointFactory dataPointFactory(){
        return new DataPointFactory();
    }
}
