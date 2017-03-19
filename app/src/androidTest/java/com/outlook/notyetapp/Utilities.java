package com.outlook.notyetapp;

import android.graphics.Rect;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.TimePicker;

import com.daimajia.swipe.SwipeLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

public class Utilities {

    public static void ThreadSleep(long milliseconds){
        try{
            Thread.sleep(milliseconds);
        }catch (InterruptedException e){}
    }

    public static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }

                @SuppressWarnings("rawtypes")
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ViewAssertion isDisplayed(){
        return matches(ViewMatchers.isDisplayed());
    }

    public static ViewAssertion isNotDisplayed(){
        return matches(new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is displayed on the screen to the user");
            }

            @Override
            public boolean matchesSafely(View view) {
                return !(view.getGlobalVisibleRect(new Rect())
                        && withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE).matches(view));
            }
        });
    }

    // Works for either phone or tablet view
    public static Matcher<View> isHabitShown(String habitTitle) {
        return anyOf(
                allOf(withParent(withId(R.id.action_bar)), withText(habitTitle)),
                allOf(withId(R.id.right_pane_title), withText(habitTitle))
        );
    }


    @SuppressWarnings({"unchecked"})
    public static ViewAssertion isHabitDateSelected(){
        return matches((Matcher)new TypeSafeMatcher<SwipeLayout>() {
            @Override
            protected boolean matchesSafely(SwipeLayout item) {
                return ViewMatchers.isDisplayed().matches(item.getChildAt(0));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" a list_item_habitdata that has been swiped right by the user");
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    public static ViewAssertion isHabitDateNotSelected(){
        return matches((Matcher)new TypeSafeMatcher<SwipeLayout>() {
            @Override
            protected boolean matchesSafely(SwipeLayout item) {
                return !ViewMatchers.isDisplayed().matches(item.getChildAt(0));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" a list_item_habitdata that has not been swiped right by the user");
            }
        });
    }



    public static ViewAction setTime(final int hours, final int minutes) {

        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TimePicker.class);
            }

            @Override
            public String getDescription() {
                return "set time";
            }

            @Override
            public void perform(UiController uiController, View view) {
                final TimePicker timePicker = (TimePicker)view;
                timePicker.setCurrentHour(hours);
                timePicker.setCurrentMinute(minutes);
            }
        };
    }

    public static ViewAction dragPastSiblings(String containerTypeName, int numberOfSiblings) {
        return new GeneralSwipeAction(
                Swipe.FAST,
                GeneralLocation.CENTER,
                new SwipeHelper(containerTypeName, numberOfSiblings),
                Press.FINGER);
    }

    public static class SwipeHelper implements CoordinatesProvider{

        int numberOfSiblings;
        String containerTypeName;

        public SwipeHelper(String containerTypeName, int numberOfSiblings) {
            this.containerTypeName = containerTypeName;
            this.numberOfSiblings = numberOfSiblings + 1;
        }

        @Override
        public float[] calculateCoordinates(View view) {
            ArrayList<Integer> map = new ArrayList<>();

            // Climb up to specified parent and keep track of the indexes to the child along the way.
            ViewGroup vg;
            View currentView = view;
            do {
                vg = (ViewGroup) currentView.getParent();
                map.add(vg.indexOfChild(currentView));
                currentView = (View)vg;
            } while (!vg.getClass().getName().endsWith(containerTypeName));

            // Find the first child that is down the list by numberOfSiblings
            currentView = vg.getChildAt(map.remove(map.size() - 1) + this.numberOfSiblings);

            // Find the view that is analogous to the original based on the indices.
            for(int i = map.size() - 1; i >= 0; i--){
                vg = (ViewGroup)currentView;
                currentView = vg.getChildAt(map.get(i));
            }

            // Find the middle of that view to drag to it
            final int[] xy = new int[2];
            currentView.getLocationOnScreen(xy);
            final float x = xy[0] + ((currentView.getWidth() - 1) / 2.0f);
            final float y = xy[1] + ((currentView.getHeight() - 1) / 2.0f);

            float[] coordinates = {x, y};

            return coordinates;
        }
    }
}
