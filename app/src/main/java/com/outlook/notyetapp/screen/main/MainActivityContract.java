package com.outlook.notyetapp.screen.main;

import android.database.Cursor;

import com.outlook.notyetapp.utilities.library.Presenter;

public interface MainActivityContract {
    interface View{
        void showActivity(long activityId, String activityTitle);
        void showEULA();
        void renderData(Cursor cursor);
    }
    interface ActionListener extends Presenter {
        void onAttached();
        void onResumed(boolean showAll, int numberOfHabits);
        void itemClicked(long activityId, String activityTitle);
        void subscribeToTodaysStats(boolean showAll);
        void resetDemo();
        void swipeLeft(long activityId, float swipeValue);
        void swipeRight(long activityId);
    }
}