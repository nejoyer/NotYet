package com.example.notyet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

// Information to the user about this application and the application's attributions.
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutEmail = (TextView)findViewById(R.id.about_email);
        aboutEmail.setMovementMethod(LinkMovementMethod.getInstance());

        TextView aboutAttribution = (TextView)findViewById(R.id.about_attribution);
        //To make the links clickable
        aboutAttribution.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
