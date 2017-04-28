package com.outlook.notyetapp.screen.createactivity;

import com.outlook.notyetapp.ActivitySettingsFragment;

public interface CreateActivityContract {
    interface View{
        boolean validate();
        void showError();
        void closeActivity();
    }
    interface ActionListener {
        void doneClicked(ActivitySettingsFragment activitySettingsFragment);
    }
}
