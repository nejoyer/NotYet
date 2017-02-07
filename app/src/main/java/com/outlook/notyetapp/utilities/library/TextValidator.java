package com.outlook.notyetapp.utilities.library;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

// helper class so that you can easily validate text without implementing all three fields.
public abstract class TextValidator implements TextWatcher {
    protected final TextView textView;
    protected final Context context;

    public TextValidator(TextView textView, Context context) {
        this.textView = textView;
        this.context = context;
    }

    public abstract boolean validate(String currentText);

    public final boolean validate(){
        return validate(textView.getText().toString());
    }

    @Override
    public final void afterTextChanged(Editable s) {
        validate();
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

}
