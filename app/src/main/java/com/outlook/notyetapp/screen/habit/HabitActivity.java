package com.outlook.notyetapp.screen.habit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.utilities.AnalyticsConstants;

// When you tap on an activity in the Main ListView, you see this activity which includes a graph and list of data points.
// You can click any of the data points to update it, or swipe right on multiple data points to bulk update.
// The actions mentioned above are all done in the fragment so that they can be included in the main activity on wide tablets.
public class HabitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, getString(R.string.admob_appid));

        setContentView(R.layout.activity_habit);

        NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.HABIT_ACTIVITY);

        if(savedInstanceState == null)
        {
            Bundle args = getIntent().getExtras();

            HabitActivityFragment fragment = HabitActivityFragment.newInstance(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.habit_activity_frame, fragment)
                    .commit();
        }

        AdView adView = (AdView) findViewById(R.id.habit_banner_ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("80FFA96B61CB6E6AC403A3FEBB8C90B0")
                .build();
        adView.loadAd(adRequest);
    }
}