package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.widget.TextView;

import com.outlook.notyetapp.R;
import com.outlook.notyetapp.utilities.library.TextValidator;

// Validate that the user has entered a valid value for the title of a habit.
public class TitleValidator extends TextValidator {

    public TitleValidator(TextView textView, Context context) {
        super(textView, context);
    }

    @Override
    public boolean validate(String currentText) {
        if(currentText.length() < 1) {
            textView.setError(this.context.getString(R.string.cannot_be_empty));
            return false;
        }
        return true;
    }
}
