package com.outlook.notyetapp.dagger.ApplicationScoped;

import android.content.Context;

import com.outlook.notyetapp.dagger.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    private final Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides
    @ApplicationScope
    public Context context(){
        return context;
    }
}
