package com.outlook.notyetapp.screen.activitysettings;

import android.content.ContentValues;


public interface ActivitySettingsActivityContract {
    interface View{
        boolean validate();
        ContentValues getSettingsFromUI();
        void closeActivity();
    }
    interface ActionListener {
        void doneClicked(long activityId);
    }
}
