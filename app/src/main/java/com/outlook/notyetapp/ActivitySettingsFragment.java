package com.outlook.notyetapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContract.ActivitiesEntry;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.utilities.HabitValueValidator;
import com.outlook.notyetapp.utilities.TitleValidator;
import com.outlook.notyetapp.utilities.library.GroupValidator;

import javax.inject.Inject;

// shows the settings for a habit.
// Used by both the CreateActivity and the ActivitySettingsActivity
public class ActivitySettingsFragment extends Fragment {

    public static final String ARG_ACTIVITY_ID = "activityid";
    private long mActivityId = -1;
    private View mFragmentView;
    public CompoundButton[] mDayButtons = new CompoundButton[7];
    private static final String[] DAYS_OF_WEEK = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private static final int[] DAYS_OF_WEEK_FLAGS = new int[]{0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40};

    private GroupValidator groupValidator;

    @Inject
    public HabitContractUriBuilder habitContractUriBuilder;

    public ActivitySettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param activityId the id of the activity for which the settings are being modified.
     * @return A new instance of fragment ActivitySettingsFragment.
     */
    public static ActivitySettingsFragment newInstance(long activityId) {
        ActivitySettingsFragment fragment = new ActivitySettingsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACTIVITY_ID, activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityId = getArguments().getLong(ARG_ACTIVITY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.activity_settings_fragment, container, false);

        //Wire up all the help buttons.
        View historical = mFragmentView.findViewById(R.id.help_historical);
        historical.setOnClickListener(mHelpClickedListener);
        View forecast = mFragmentView.findViewById(R.id.help_forecast);
        forecast.setOnClickListener(mHelpClickedListener);
        View swipe = mFragmentView.findViewById(R.id.help_swipe);
        swipe.setOnClickListener(mHelpClickedListener);
        View higher = mFragmentView.findViewById(R.id.help_higher);
        higher.setOnClickListener(mHelpClickedListener);
        View days = mFragmentView.findViewById(R.id.help_days);
        days.setOnClickListener(mHelpClickedListener);

        // Load the days of the week buttons
        LinearLayout repeatDays = (LinearLayout) mFragmentView.findViewById(R.id.activity_settings_days_to_show_holder);

        for (int i = 0; i < 7; i++) {
            final CompoundButton dayButton = (CompoundButton) inflater.inflate(
                    R.layout.day_button, repeatDays, false /* attachToRoot */);
            dayButton.setText(DAYS_OF_WEEK[i]);
            dayButton.setChecked(true);
            repeatDays.addView(dayButton);
            mDayButtons[i] = dayButton;
        }

        if(mActivityId != -1)
        {
            Cursor activitySettingsCursor = getContext().getContentResolver().query(
                    habitContractUriBuilder.buildActivityUri(mActivityId),//Uri
                    HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION,//projection
                    null,//selection
                    null,//selectionArgs
                    null//Sort Order
            );

            putSettingsToUI(activitySettingsCursor);
        }

        groupValidator = new GroupValidator(getContext());

        // Add validators to the EditText fields.
        EditText titleEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_title_edit);
        groupValidator.AddFieldToValidate(titleEdit, TitleValidator.class);

        EditText historicalEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_historical_edit);
        groupValidator.AddFieldToValidate(historicalEdit, HabitValueValidator.class);

        EditText forecastEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_forecast_edit);
        groupValidator.AddFieldToValidate(forecastEdit, HabitValueValidator.class);

        EditText swipeEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_one_swipe_edit);
        groupValidator.AddFieldToValidate(swipeEdit, HabitValueValidator.class);

        return mFragmentView;
    }

    // Run all validators and return true if there are no errors.
    public boolean validate(){
        return groupValidator.ValidateAll();
    }

    @Override
    public void onDestroyView() {
        //Avoid leaks of the activity by dropping references that can contain pointers to the context
        this.mFragmentView = null;
        this.mDayButtons = null;
        this.groupValidator = null;
        super.onDestroyView();
    }

    //Get the settings the user has entered from the UI and get them in a state where it is easy to put into the DB.
    public ContentValues getSettingsFromUI(){
        ContentValues values = new ContentValues();
        String title = ((EditText)mFragmentView.findViewById(R.id.activity_settings_title_edit)).getText().toString();
        values.put(ActivitiesEntry.COLUMN_ACTIVITY_TITLE, title);
        String historical = ((EditText)mFragmentView.findViewById(R.id.activity_settings_historical_edit)).getText().toString();
        values.put(ActivitiesEntry.COLUMN_HISTORICAL, Float.valueOf(historical));
        String forecast = ((EditText)mFragmentView.findViewById(R.id.activity_settings_forecast_edit)).getText().toString();
        values.put(ActivitiesEntry.COLUMN_FORECAST, Float.valueOf(forecast));
        String swipe = ((EditText)mFragmentView.findViewById(R.id.activity_settings_one_swipe_edit)).getText().toString();
        values.put(ActivitiesEntry.COLUMN_SWIPE_VALUE, Float.valueOf(swipe));
        int higherIsBetter = ((ToggleButton)mFragmentView.findViewById(R.id.activity_settings_higher_is_better_toggle)).isChecked() ? 1 : 0;
        values.put(ActivitiesEntry.COLUMN_HIGHER_IS_BETTER, Integer.valueOf(higherIsBetter));

        int daysVal = 0;
        for(int i = 0; i < 7; i++)
        {
            if(mDayButtons[i].isChecked()){
                daysVal += DAYS_OF_WEEK_FLAGS[i];
            }
        }
        values.put(ActivitiesEntry.COLUMN_DAYS_TO_SHOW, daysVal);

        return values;
    }

    // Take the settings from the DB in the form of a cursor and populate the data to the UI.
    public void putSettingsToUI(Cursor cursor){
        cursor.moveToFirst();
        EditText titleEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_title_edit));
        String title = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_ACTIVITY_TITLE);
        titleEditText.setText(title);

        EditText historicalEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_historical_edit));
        String historical = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_HISTORICAL);
        historicalEditText.setText(historical);

        EditText forecastEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_forecast_edit));
        String forecast = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_FORECAST);
        forecastEditText.setText(forecast);

        EditText swipeEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_one_swipe_edit));
        String swipe = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_SWIPE_VALUE);
        swipeEditText.setText(swipe);

        ToggleButton higherIsBetterToggle = ((ToggleButton)mFragmentView.findViewById(R.id.activity_settings_higher_is_better_toggle));
        higherIsBetterToggle.setChecked(cursor.getInt(HabitContract.ActivitySettingsQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1);

        int daysVal = cursor.getInt(HabitContract.ActivitySettingsQueryHelper.COLUMN_DAYS_TO_SHOW);
        for(int i = 0; i < 7; i++)
        {
            mDayButtons[i].setChecked((daysVal & DAYS_OF_WEEK_FLAGS[i]) > 0);
        }
    }

    // Show help in a dialog when the user clicks
    private View.OnClickListener mHelpClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String helpText = null;
            switch (view.getId()) {
                case R.id.help_historical:
                    helpText = getString(R.string.help_historical);
                    break;
                case R.id.help_forecast:
                    helpText = getString(R.string.help_forecast);
                    break;
                case R.id.help_swipe:
                    helpText = getString(R.string.help_swipe);
                    break;
                case R.id.help_higher:
                    helpText = getString(R.string.help_higher);
                    break;
                case R.id.help_days:
                    helpText = getString(R.string.help_days);
                    break;
            }

            new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.help_title))
                    .setMessage(helpText)
                    .setIcon(R.drawable.ic_dialog_info_with_tint)
                    .show();
        }
    };
}
