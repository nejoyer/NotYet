package com.outlook.notyetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContract.ActivitiesEntry;
import com.outlook.notyetapp.utilities.TextValidator;

public class ActivitySettingsFragment extends Fragment {

    public static final String ARG_ACTIVITY_URI = "activityuri";
    private Uri mActivityUri = null;
    private View mFragmentView;
    public final CompoundButton[] mDayButtons = new CompoundButton[7];
    private static final String[] DAYS_OF_WEEK = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private static final int[] DAYS_OF_WEEK_FLAGS = new int[]{0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40};

    private TextValidator mTitleValidator;
    private boolean mTitleError = true;
    private TextValidator mHistoricalValidator;
    private boolean mHistoricalError = true;
    private TextValidator mForecastValidator;
    private boolean mForecastError = true;
    private TextValidator mSwipeValidator;
    private boolean mSwipeError = true;



    public ActivitySettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param activityUri the id of the activity for which the settings are being modified.
     * @return A new instance of fragment ActivitySettingsFragment.
     */
    public static ActivitySettingsFragment newInstance(Uri activityUri) {
        ActivitySettingsFragment fragment = new ActivitySettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACTIVITY_URI, activityUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityUri = getArguments().getParcelable(ARG_ACTIVITY_URI);
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

        if(mActivityUri != null)
        {
            Cursor activitySettingsCursor = getContext().getContentResolver().query(
                    mActivityUri,//Uri
                    HabitContract.ActivitySettingsQueryHelper.ACTIVITY_SETTINGS_PROJECTION,//projection
                    null,//selection
                    null,//selectionArgs
                    null//Sort Order
            );

            putSettingsToUI(activitySettingsCursor);
        }

        // Add validators to the EditText fields.
        EditText titleEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_title_edit);
        mTitleValidator = new TextValidator(titleEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    textView.setError(getString(R.string.cannot_be_empty));
                    mTitleError = true;
                }
                else {
                    mTitleError = false;
                }
            }
        };
        titleEdit.addTextChangedListener(mTitleValidator);

        EditText historicalEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_historical_edit);
        mHistoricalValidator = new TextValidator(historicalEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    textView.setError(getString(R.string.cannot_be_empty));
                    mHistoricalError = true;
                }
                else {
                    mHistoricalError = false;
                }
            }
        };
        historicalEdit.addTextChangedListener(mHistoricalValidator);

        EditText forecastEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_forecast_edit);
        mForecastValidator = new TextValidator(forecastEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    textView.setError(getString(R.string.cannot_be_empty));
                    mForecastError = true;
                }
                else {
                    mForecastError = false;
                }
            }
        };
        forecastEdit.addTextChangedListener(mForecastValidator);

        EditText swipeEdit = (EditText)mFragmentView.findViewById(R.id.activity_settings_one_swipe_edit);
        mSwipeValidator = new TextValidator(swipeEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 1) {
                    textView.setError(getString(R.string.cannot_be_empty));
                    mSwipeError = true;
                }
                else {
                    mSwipeError = false;
                }
            }
        };
        swipeEdit.addTextChangedListener(mSwipeValidator);

        return mFragmentView;
    }

    // Run all three validators and return if there are any errors.
    public boolean validate(){
        mTitleValidator.afterTextChanged(null);
        mHistoricalValidator.afterTextChanged(null);
        mForecastValidator.afterTextChanged(null);
        mSwipeValidator.afterTextChanged(null);
        return mTitleError || mHistoricalError || mForecastError || mSwipeError;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        mTitleError = false;

        EditText historicalEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_historical_edit));
        String historical = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_HISTORICAL);
        historicalEditText.setText(historical);
        mHistoricalError = false;

        EditText forecastEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_forecast_edit));
        String forecast = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_FORECAST);
        forecastEditText.setText(forecast);
        mForecastError = false;

        EditText swipeEditText = ((EditText)mFragmentView.findViewById(R.id.activity_settings_one_swipe_edit));
        String swipe = cursor.getString(HabitContract.ActivitySettingsQueryHelper.COLUMN_SWIPE_VALUE);
        swipeEditText.setText(swipe);
        mForecastError = false;

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
