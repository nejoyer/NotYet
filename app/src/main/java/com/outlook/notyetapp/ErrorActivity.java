package com.outlook.notyetapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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
    }
}
