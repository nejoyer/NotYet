package com.outlook.notyetapp.dagger.ApplicationScoped;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.GraphUtilities;
import com.outlook.notyetapp.utilities.LineGraphSeriesFactory;

import dagger.Module;
import dagger.Provides;

@Module(includes = GraphModule.class)
public class GraphUtilitiesModule {
    @Provides
    @ApplicationScope
    public GraphUtilities graphUtilities(LineGraphSeriesFactory lineGraphSeriesFactory, DataPointFactory dataPointFactory, DateHelper dateHelper){
        return new GraphUtilities(lineGraphSeriesFactory, dataPointFactory, dateHelper);
    }
}
