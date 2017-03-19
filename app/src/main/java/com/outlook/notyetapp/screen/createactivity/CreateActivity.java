package com.outlook.notyetapp.screen.createactivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.outlook.notyetapp.ActivitySettingsFragment;
import com.outlook.notyetapp.DoneCancelFragment;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.dagger.ActivityScoped.CreateActivityModule;
import com.outlook.notyetapp.dagger.ActivityScoped.DaggerCreateActivityComponent;

import javax.inject.Inject;

// Used to create a new habit which you want to track.
// Invoked using the plus sign on MainActivity
public class CreateActivity extends AppCompatActivity implements DoneCancelFragment.OnFragmentInteractionListener, CreateActivityContract.View {

    private ActivitySettingsFragment mActivitySettingsFragment;

    @Inject
    public CreateActivityContract.ActionListener mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySettingsFragment = new ActivitySettingsFragment();

        // Use DoneCancelFragment to set the custom action bar
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_create_container, new DoneCancelFragment(), "DoneCancelFragment")
                .add(R.id.activity_create_container, mActivitySettingsFragment, "ActivitySettingsFragment")
                .commit();

        setContentView(R.layout.activity_create);

        DaggerCreateActivityComponent.builder()
                .notYetApplicationComponent(NotYetApplication.get(this).component())
                .createActivityModule(new CreateActivityModule(this))
                .build().inject(this);
    }

    @Override
    public boolean validate() {
        return mActivitySettingsFragment.validate();
    }

    @Override
    public void closeActivity() {
        finish();
    }

    @Override
    public void showError() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error_black_24dp)
                .setTitle(getString(R.string.error_creating_activity_title))
                .setMessage(getString(R.string.error_creating_activity_message))
                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //Called by DoneCancelFragment
    @Override
    public void doneClicked() {
        mPresenter.doneClicked(mActivitySettingsFragment.getSettingsFromUI());
    }

    //Called by DoneCancelFragment
    // Just close the activity without saving anything since the user hit cancel.
    @Override
    public void cancelClicked() {
        finish();
    }
}
