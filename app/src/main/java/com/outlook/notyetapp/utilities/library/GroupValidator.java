package com.outlook.notyetapp.utilities.library;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// Utility to help you easily validate multiple field (with potentially different criteria)
// all at once and determine if they are all valid or not.
public class GroupValidator {
    private List<TextValidator> viewsRequiringValidation = new ArrayList<TextValidator>();

    private Context context;

    public GroupValidator(Context context) {
        this.context = context;
    }

    public void AddFieldToValidate(EditText fieldToValidate, Class<? extends TextValidator> textValidatorType)
    {
        TextValidator validator = null;
        try {
            validator = textValidatorType.getDeclaredConstructor(TextView.class, Context.class).newInstance(fieldToValidate, this.context);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        fieldToValidate.addTextChangedListener(validator);

        viewsRequiringValidation.add(validator);
    }

    public boolean ValidateAll(){
        boolean allValid = true;
        for(TextValidator textValidator : viewsRequiringValidation)
        {
            allValid &= textValidator.validate();
        }
        return allValid;
    }
}
