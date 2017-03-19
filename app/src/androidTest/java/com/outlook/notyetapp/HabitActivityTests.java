package com.outlook.notyetapp;

import android.content.Intent;
import android.support.test.espresso.matcher.CursorMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.habit.HabitActivity;
import com.outlook.notyetapp.screen.habit.HabitActivityFragment;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.outlook.notyetapp.Utilities.ThreadSleep;
import static com.outlook.notyetapp.Utilities.isDisplayed;
import static com.outlook.notyetapp.Utilities.isHabitDateNotSelected;
import static com.outlook.notyetapp.Utilities.isHabitDateSelected;
import static com.outlook.notyetapp.Utilities.isNotDisplayed;
import static com.outlook.notyetapp.Utilities.withAdaptedData;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HabitActivityTests extends ResetDataTestBaseClass{
    @Rule
    public ActivityTestRule<HabitActivity> mActivityRule = new ActivityTestRule<>(HabitActivity.class, true, false);

    @Before
    public void LaunchActivity(){
        Intent intent = new Intent();
        intent.putExtra(HabitActivityFragment.ACTIVITY_ID_KEY, 18L/*Snooze*/);
        mActivityRule.launchActivity(intent);
    }

    @Test
    public void baa_verifyGraphAndListAreDisplayed() {
        onView(withId(R.id.habit_graph)).check(isDisplayed());
        onView(withId(R.id.habit_listview)).check(isDisplayed());
    }

    @Test
    public void bba_verifyManualEntryToday() {
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 3),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).check(isDisplayed()).perform(click());

        onView(withId(R.id.update_habit_value_edit_text)).check(isDisplayed()).perform(typeTextIntoFocusedView("33"));
        onView(withText(android.R.string.yes)).check(isDisplayed()).perform(click());

        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 33),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
    }

    @Test
    //todo verify that the updating dialog is shown (and hidden)
    public void bca_verifyManualEntryOlder() {
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-45),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-45),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).perform(click());

        onView(withId(R.id.update_habit_value_edit_text)).check(isDisplayed()).perform(typeTextIntoFocusedView("35"));
        onView(withText(android.R.string.yes)).check(isDisplayed()).perform(click());

        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate() - 45),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 35),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
    }

    @Test
    //todo verify that the updating dialog is shown (and hidden)
    public void bda_verifyMultiSelectUpdate() {
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-2),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-1),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 3),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight());

        onView(withId(R.id.multiselect_value_edittext)).check(isDisplayed()).perform(click(), typeTextIntoFocusedView("36"));
        onView(withId(R.id.multiselect_ok_button)).check(isDisplayed()).perform(click());

        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-2),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 36),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-1),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 36),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 36),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed());
    }

    @Test
    public void bdj_verifyMultiSelectCancel() {
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-2),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight()).check(isHabitDateSelected());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-1),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight()).check(isHabitDateSelected());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 3),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).check(isDisplayed()).perform(swipeRight()).check(isHabitDateSelected());

        onView(withId(R.id.multiselect_value_edittext)).check(isDisplayed());
        onView(withId(R.id.multiselect_cancel_button)).check(isDisplayed()).perform(click());

        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-2),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).check(isHabitDateNotSelected());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-1),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).check(isDisplayed()).check(isHabitDateNotSelected());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 3),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).check(isDisplayed()).check(isHabitDateNotSelected());

        onView(withId(R.id.multiselect_value_edittext)).check(isNotDisplayed());
    }

    @Test
    public void bea_verifyAddMoreHistory() {
        //Check that there are no dates older than 411 days old
        onView(withId(R.id.habit_listview))
                .check(matches(not(withAdaptedData(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, is(dateHelper.getTodaysDBDate()-412))))));
        onData(
                allOf(
                        allOf(is(instanceOf(String.class)), is(HabitActivityFragment.FOOTER_LABEL))
                )
        ).check(isDisplayed()).perform(click());
        onView(withId(R.id.add_more_history_edit_text)).check(isDisplayed()).perform(click(), typeTextIntoFocusedView("5"));
        onView(withText(android.R.string.yes)).check(isDisplayed()).perform(click());
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()-412),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 1),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.HISTORICAL.getValue())
                )
        ).check(isDisplayed());
    }

    @Test
    public void bfa_clickGraph() {
        onView(withId(R.id.habit_graph)).check(isDisplayed()).perform(click());
        onView(withId(R.id.graph_graph)).check(isDisplayed());
        pressBack();
        onView(withId(R.id.habit_graph)).check(isDisplayed());
    }
}
