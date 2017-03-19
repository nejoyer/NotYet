package com.outlook.notyetapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

// Information to the user about this application and the application's attributions.
public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.about_email)
    TextView aboutEmail;

    @BindView(R.id.about_attribution)
    TextView aboutAttribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        //To make the links clickable
        aboutEmail.setMovementMethod(LinkMovementMethod.getInstance());
        aboutAttribution.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
