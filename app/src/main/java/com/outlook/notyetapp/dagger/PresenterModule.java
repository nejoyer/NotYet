package com.outlook.notyetapp.dagger;

import android.content.Context;

import com.outlook.notyetapp.data.DateConverter;
import com.outlook.notyetapp.factories.DataPointFactory;
import com.outlook.notyetapp.utilities.CursorToDataPointListHelper;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class, GraphModule.class})
public class PresenterModule {
    @Provides
    public StorIOContentResolver storIOContentResolver(Context context){
        return DefaultStorIOContentResolver.builder().contentResolver(context.getContentResolver()).build();
    }
    @Provides
    public CursorToDataPointListHelper cursorToDataPointListHelper(DateConverter dateConverter, DataPointFactory dataPointFactory){
        return new CursorToDataPointListHelper(dateConverter, dataPointFactory);
    }
    @Provides
    public DateConverter dateConverter(){
        return new DateConverter();
    }
}
