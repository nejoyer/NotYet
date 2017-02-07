package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.widget.TextView;

import com.outlook.notyetapp.R;
import com.outlook.notyetapp.utilities.library.TextValidator;

/**
 * Created by Neil on 2/6/2017.
 */

public class HabitValueValidator extends TextValidator {

    public HabitValueValidator(TextView textView, Context context) {
        super(textView, context);
    }

    @Override
    public boolean validate(String currentText) {
        if(currentText.length() < 1) {
            textView.setError(this.context.getString(R.string.cannot_be_empty));
            return false;
        }

        Float val = null;
        try {
            val = Float.valueOf(currentText);
        }
        catch (Exception e){}

        if(val == null){
            textView.setError(this.context.getString(R.string.must_be_a_number));
            return false;
        }

        return true;
    }
}
