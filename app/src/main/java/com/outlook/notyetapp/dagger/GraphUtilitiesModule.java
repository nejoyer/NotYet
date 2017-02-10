package com.outlook.notyetapp.dagger;

import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.outlook.notyetapp.utilities.LineGraphSeriesFactory;

import dagger.Module;
import dagger.Provides;

@Module(includes = GraphModule.class)
public class GraphUtilitiesModule {
    @Provides
    public GraphUtilities graphUtilities(LineGraphSeriesFactory lineGraphSeriesFactory, DataPointFactory dataPointFactory){
        return new GraphUtilities(lineGraphSeriesFactory, dataPointFactory);
    }
}
