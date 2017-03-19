package com.outlook.notyetapp.screen.activitysettings;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.outlook.notyetapp.ActivitySettingsFragment;
import com.outlook.notyetapp.DoneCancelFragment;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.dagger.ActivityScoped.ActivitySettingsActivityComponent;
import com.outlook.notyetapp.dagger.ActivityScoped.ActivitySettingsActivityModule;
import com.outlook.notyetapp.dagger.ActivityScoped.DaggerActivitySettingsActivityComponent;

import javax.inject.Inject;

//import com.outlook.notyetapp.dagger.ActivitySettingsActivityContractViewModule;

// Used to change the settings for one of your habits (invoked from menu on HabitActivity).
public class ActivitySettingsActivity extends AppCompatActivity implements DoneCancelFragment.OnFragmentInteractionListener, ActivitySettingsActivityContract.View {

    private ActivitySettingsFragment mActivitySettingsFragment;

    @Inject
    ActivitySettingsActivityContract.ActionListener mPresenter;

    private long mActivityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityId = getIntent().getLongExtra(ActivitySettingsFragment.ARG_ACTIVITY_ID, -1);
        mActivitySettingsFragment = ActivitySettingsFragment.newInstance(mActivityId);

        // Use DoneCancelFragment to set the custom action bar
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_settings_container, new DoneCancelFragment(), "DoneCancelFragment")
                .add(R.id.activity_settings_container, mActivitySettingsFragment, "ActivitySettingsFragment")
                .commit();

        setContentView(R.layout.activity_activity_settings);

        ActivitySettingsActivityComponent component = DaggerActivitySettingsActivityComponent.builder()
                .notYetApplicationComponent(NotYetApplication.get(this).component())
                .activitySettingsActivityModule(new ActivitySettingsActivityModule(this))
                .build();
        component.inject(this);
        component.inject(mActivitySettingsFragment);
    }

    // true if there are no errors
    @Override
    public boolean validate() {
        return mActivitySettingsFragment.validate();
    }

    @Override
    public ContentValues getSettingsFromUI() {
        return mActivitySettingsFragment.getSettingsFromUI();
    }

    // This will get called from the fragment.
    @Override
    public void doneClicked() {
        mPresenter.doneClicked(mActivityId);
    }

    @Override
    public void closeActivity() {
        finish();
    }

    // This will get called from the fragment. Exits without saving any changes.
    @Override
    public void cancelClicked() {
        finish();
    }
}
