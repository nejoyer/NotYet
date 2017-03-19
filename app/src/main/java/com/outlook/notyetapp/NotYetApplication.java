package com.outlook.notyetapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.outlook.notyetapp.dagger.ApplicationScoped.ContextModule;
import com.outlook.notyetapp.dagger.ApplicationScoped.DaggerNotYetApplicationComponent;
import com.outlook.notyetapp.dagger.ApplicationScoped.NotYetApplicationComponent;

// Extend the application to allow for some effective globals.
public class NotYetApplication extends Application{

    private static FirebaseAnalytics mFirebaseAnalytics = null;

    NotYetApplicationComponent component;

    public static NotYetApplication get(Activity activity) {
        return (NotYetApplication) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerNotYetApplicationComponent.builder().contextModule(new ContextModule(this)).build();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setMinimumSessionDuration(5000);
        mFirebaseAnalytics.setSessionTimeoutDuration(300000);
    }

    public NotYetApplicationComponent component(){
        return component;
    }

    public static void logFirebaseAnalyticsEvent(String eventName){
        logFirebaseAnalyticsEvent(eventName, new Bundle());
    }

    public static void logFirebaseAnalyticsEvent(String eventName, Bundle bundle){
        mFirebaseAnalytics.logEvent(eventName, bundle);
    }



}
