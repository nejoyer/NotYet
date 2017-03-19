package com.outlook.notyetapp;

import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.matcher.CursorMatchers;
import android.support.test.espresso.web.assertion.WebViewAssertions;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.Button;
import android.widget.TimePicker;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.screen.main.MainActivity;

import junit.framework.Assert;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static com.outlook.notyetapp.Utilities.isDisplayed;
import static com.outlook.notyetapp.Utilities.isHabitShown;
import static com.outlook.notyetapp.Utilities.withAdaptedData;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTests extends ResetDataTestBaseClass  {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    // Clear EULA and Hero Text and Remove HelpHeader if necessary
    public void aaa_runOnceSetup(){
        try {
            onView(withText(context.getString(R.string.eula_agree))).inRoot(isDialog()).check(isDisplayed()).perform(click());
        } catch (NoMatchingRootException e){}
        try {
            onView(withText(context.getString(R.string.no_activities_new_user_hero))).check(isDisplayed()).perform(click());
        }catch (NoMatchingViewException e){}
        try {
            onView(withText(context.getString(R.string.list_item_activity_title))).check(isDisplayed()).perform(swipeLeft());
        }catch (NoMatchingViewException e){}
    }

    @Test
    public void bba_verifyListDisplayed() {
        onView(withId(R.id.main_listview)).check(isDisplayed());
    }

    @Test
    public void bca_clickVitamin() {
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("Vitamin")).check(isDisplayed());
    }

    @Test
    public void bda_clickFloss() {
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Floss"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("Floss")).check(isDisplayed());
    }

    @Test
    public void bea_swipeHideNoValue() {
        //Find Vitamin and swipe to hide it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(swipeRight());

        //Verify it is not present
        onView(withId(R.id.main_listview))
                .check(matches(not(withAdaptedData(CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))))));

        toggleShowAll();

        //Verify it can be found and click it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());

        //Make sure it navigates to the HabitActivity.
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("Vitamin")).check(isDisplayed());

        //Verify that the data is correct. (no value was set)
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).inAdapterView(withId(R.id.habit_listview)).check(isDisplayed());
    }

    @Test
    public void bfa_swipeHideWithValue() {
        //Find Vitamin and swipe to hide it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(swipeLeft());

        //Verify it is not present
        onView(withId(R.id.main_listview))
                .check(matches(not(withAdaptedData(CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))))));

        toggleShowAll();

        //Verify it can be found and click it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Vitamin"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());

        //Make sure it navigates to the HabitActivity.
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("Vitamin")).check(isDisplayed());

        //Verify that the data is correct. (value was set)
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 1),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).inAdapterView(withId(R.id.habit_listview)).check(isDisplayed());
    }

    @Test
    public void bga_createActivity() {
        onView(withId(R.id.action_create_new_activity)).check(isDisplayed()).perform(click());

        onView(withId(R.id.activity_settings_title_edit)).check(isDisplayed()).perform(typeTextIntoFocusedView("new activity"), closeSoftKeyboard());
        onView(withId(R.id.activity_settings_historical_edit)).perform(scrollTo()).check(isDisplayed()).perform(click(), typeText("1"), closeSoftKeyboard());
        onView(withId(R.id.activity_settings_forecast_edit)).perform(scrollTo()).check(isDisplayed()).perform(click(), typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.activity_settings_one_swipe_edit)).perform(scrollTo()).check(isDisplayed()).perform(click(), typeText("3"), closeSoftKeyboard());

        onView(withId(R.id.action_done)).check(isDisplayed()).perform(click());
        //Find Vitamin and swipe to hide it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("new activity"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());

        //Make sure it navigates to the HabitActivity.
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("new activity")).check(isDisplayed());

        //Verify that the data is correct. (no value was set)
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, dateHelper.getTodaysDBDate()),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 1),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.HISTORICAL.getValue())
                )
        ).inAdapterView(withId(R.id.habit_listview)).check(isDisplayed());
    }

    @Test
    public void bha_changeSort() {
        onData(CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, any(String.class)))
                .inAdapterView(withId(R.id.main_listview))
                .atPosition(1)
                .check(matches(hasDescendant(withText("Vitamin")))).check(isDisplayed());

        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(R.string.action_change_sort)).check(isDisplayed()).perform(click());

        onView(allOf(hasSibling(withText("Vitamin")), withId(R.id.list_item_drag_handle)))
                .check(isDisplayed())
                .perform(Utilities.dragPastSiblings("ListView", 5));

        onView(withId(R.id.action_done)).check(isDisplayed()).perform(click());

        onData(CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, any(String.class)))
                .inAdapterView(withId(R.id.main_listview))
                .atPosition(6)
                .check(matches(hasDescendant(withText("Vitamin")))).check(isDisplayed());
    }

    @Test
    public void bia_settings(){
        long origDBDate = dateHelper.getTodaysDBDate();

        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(R.string.action_settings)).check(isDisplayed()).perform(click());

        onView(withText("12:00 AM")).check(isDisplayed());
        onView(withText(R.string.pref_day_change_label)).check(isDisplayed()).perform(click());

        onView(isAssignableFrom(TimePicker.class)).check(isDisplayed()).perform(Utilities.setTime(11, 59));
        onView(withText("PM")).check(isDisplayed()).perform(click());
        onView(allOf(isAssignableFrom(Button.class), withText("Set"))).check(isDisplayed()).perform(click());
        onView(withText("11:59 PM")).check(isDisplayed());

        pressBack();

        long newDBDate = dateHelper.getTodaysDBDate();

        Assert.assertEquals(origDBDate - 1, newDBDate);

        //Find Vitamin and swipe to hide it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Snacks"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(swipeLeft());

        //Verify it is not present
        onView(withId(R.id.main_listview))
                .check(matches(not(withAdaptedData(CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Snacks"))))));

        toggleShowAll();

        //Verify it can be found and click it.
        onData(
                CursorMatchers.withRowString(HabitContract.ActivitiesEntry.COLUMN_ACTIVITY_TITLE, is("Snacks"))
        ).inAdapterView(withId(R.id.main_listview)).check(isDisplayed()).perform(click());

        //Make sure it navigates to the HabitActivity.
        onView(withId(R.id.habit_listview_and_best_layout)).check(isDisplayed());
        onView(isHabitShown("Snacks")).check(isDisplayed());

        //Verify that the data is correct. (no value was set on latest date, value was set on previous date)
        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, origDBDate),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 3),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED.getValue())
                )
        ).inAdapterView(withId(R.id.habit_listview)).check(isDisplayed());

        onData(
                allOf(
                        CursorMatchers.withRowLong(HabitContract.HabitDataEntry.COLUMN_DATE, newDBDate),
                        CursorMatchers.withRowFloat(HabitContract.HabitDataEntry.COLUMN_VALUE, 0),
                        CursorMatchers.withRowInt(HabitContract.HabitDataEntry.COLUMN_TYPE, HabitContract.HabitDataEntry.HabitValueType.USER.getValue())
                )
        ).inAdapterView(withId(R.id.habit_listview)).check(isDisplayed());
    }

    @Test
    public void bja_helpHowTo() {
        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(R.string.action_help)).check(isDisplayed()).perform(click());

        //Make sure something rendered in the webview.
        onWebView(withId(R.id.help_activity_webview))
                .forceJavascriptEnabled()
                .withElement(findElement(Locator.TAG_NAME, "h3")).check(WebViewAssertions.webMatches(getText(), containsString("Issues/Questions")));
    }

    @Test
    public void bka_about() {
        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(R.string.action_about)).check(isDisplayed()).perform(click());

        onView(withText(R.string.about_about_header));
    }

    public void toggleShowAll(){
        onView(withId(R.id.action_visibility_changed)).check(isDisplayed()).perform(click());
    }
}
