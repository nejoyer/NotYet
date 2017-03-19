package com.outlook.notyetapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

// Right now, this is only shown if you try to downgrade.
// Right now, I don't have good reason to prevent it, but think I will in the future.
// Need to prevent it now to prevent someone from coming back to this version after I have reason
// to prevent downgrade.
public class ErrorActivity extends AppCompatActivity {

    public static final String ERROR_MESSAGE_KEY = "error_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        TextView title = (TextView)findViewById(R.id.error_title);
        title.setText(getString(R.string.unexpected_error));

        TextView message = (TextView)findViewById(R.id.error_message);
        message.setText(getIntent().getStringExtra(ERROR_MESSAGE_KEY));
    }

    @Override
    protected void onStop() {
        System.exit(2);
        super.onStop();
    }
}
