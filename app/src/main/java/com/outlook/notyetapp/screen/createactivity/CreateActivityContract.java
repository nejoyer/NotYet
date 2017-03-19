package com.outlook.notyetapp.screen.createactivity;

import android.content.ContentValues;

public interface CreateActivityContract {
    interface View{
        boolean validate();
        void showError();
        void closeActivity();
    }
    interface ActionListener {
        void doneClicked(ContentValues contentValues);
    }
}
