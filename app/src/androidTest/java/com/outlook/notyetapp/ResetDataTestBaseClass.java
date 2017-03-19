package com.outlook.notyetapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.outlook.notyetapp.dagger.ApplicationScoped.ContextModule;
import com.outlook.notyetapp.dagger.ApplicationScoped.DaggerNotYetApplicationComponent;
import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;
import com.outlook.notyetapp.data.DBHelper;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.screen.main.MainActivityPresenter;

import org.junit.Before;

public class ResetDataTestBaseClass {

    public DBHelper dbHelper;
    public Context context;
    public DateHelper dateHelper;
    public SharedPreferencesManager sharedPreferencesManager;

    public ResetDataTestBaseClass() {
        context = InstrumentationRegistry.getTargetContext();
        dbHelper = new DBHelper(context);

        NotYetApplicationComponent component = DaggerNotYetApplicationComponent.builder().contextModule(new ContextModule(context)).build();
        dateHelper = component.dateHelper();
        sharedPreferencesManager = component.sharedPreferencesManager();
    }

    @Before
    public void resetState(){
        sharedPreferencesManager.clearOffset();
        dbHelper.copyDemoDB(MainActivityPresenter.DEMO_DATABASE_NAME);
        dbHelper.updateDemoDBByDate();
        Utilities.ThreadSleep(1000);
    }
}
