package com.outlook.notyetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.CustomNumberFormatter;
import com.outlook.notyetapp.utilities.SwipeOpenListener;

import java.util.ArrayList;

public class ActivityAdapter extends CursorAdapter {

    public ActivityAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_activity, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.swipeLayout.addSwipeListener(new SwipeOpenListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                TagParams tagParams = (TagParams)layout.findViewById(R.id.list_item_title).getTag();

                long offset = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_day_change_key), "0"));
                long todaysDBDate = HabitContract.HabitDataEntry.getTodaysDBDate(offset);

                //Swiped to the left, so update the value for that activity for today.
                if(layout.getDragEdge() == SwipeLayout.DragEdge.Right) {
                    ArrayList<Long> arrayList = new ArrayList<Long>(1);
                    arrayList.add(todaysDBDate);
                    new UpdateHabitDataTask(layout.getContext(), false).execute(
                            new UpdateHabitDataTask.Params(
                                    layout.getContext(),
                                    tagParams.mActivityId,
                                    tagParams.mHigherIsBetter,
                                    arrayList,
                                    tagParams.mSwipeValue,
                                    HabitContract.HabitDataEntry.HabitValueType.USER
                            )
                    );
                }
                //Whether they swiped right or left, now we want to hid it for the rest of the day.
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE, todaysDBDate);
                layout.getContext().getContentResolver().update(
                        HabitContract.ActivitiesEntry.buildActivityUri(tagParams.mActivityId),
                        contentValues,
                        null, // selection, handled by Uri
                        null  // selectionArgs, handled by Uri
                );
                layout.getContext().getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);
            }
        });
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // because we are re-using the swipe layout in our list item view, we need to reset the state to closed when you are scrolling
        // this works for us because if you are looking at it, it should be closed.
        // (when you swipe, it disappears from the view (or resets to open if you have selected to view hidden activities)).
        if(viewHolder.swipeLayout.getOpenStatus() == SwipeLayout.Status.Open) {
            viewHolder.swipeLayout.close(false, false);
        }

        boolean higherIsBetter = cursor.getInt(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_HIGHER_IS_BETTER) == 1;

        TagParams tagParams = new TagParams(
                cursor.getLong(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_ID),
                higherIsBetter,
                cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_SWIPE_VALUE)
        );

        viewHolder.titleView.setTag(tagParams);

        viewHolder.titleView.setText(cursor.getString(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_TITLE));

        viewHolder.todayView.setText(CustomNumberFormatter.formatToThreeCharacters(cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_VALUE)));

        int goodColor = ContextCompat.getColor(context, R.color.colorGood);
        int badColor = ContextCompat.getColor(context, R.color.colorBad);


        float avg7= cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ROLLING_AVG_7);
        float best7 = cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_BEST7);
        if(higherIsBetter) {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg7)) >= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best7))) {
                viewHolder.section7.setBackgroundColor(goodColor);
            } else {
                viewHolder.section7.setBackgroundColor(badColor);
            }
        }else {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg7)) <= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best7))) {
                viewHolder.section7.setBackgroundColor(goodColor);
            } else {
                viewHolder.section7.setBackgroundColor(badColor);
            }
        }

        float avg30 = cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ROLLING_AVG_30);
        float best30 = cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_BEST30);
        if(higherIsBetter) {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg30)) >= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best30))) {
                viewHolder.section30.setBackgroundColor(goodColor);
            } else {
                viewHolder.section30.setBackgroundColor(badColor);
            }
        } else {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg30)) <= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best30))) {
                viewHolder.section30.setBackgroundColor(goodColor);
            } else {
                viewHolder.section30.setBackgroundColor(badColor);
            }
        }

        float avg90 = cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ROLLING_AVG_90);
        float best90 = cursor.getFloat(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_BEST90);
        if(higherIsBetter) {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg90)) >= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best90))) {
                viewHolder.section90.setBackgroundColor(goodColor);
            } else {
                viewHolder.section90.setBackgroundColor(badColor);
            }
        } else {
            if (Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(avg90)) <= Float.valueOf(CustomNumberFormatter.formatToThreeCharacters(best90))) {
                viewHolder.section90.setBackgroundColor(goodColor);
            } else {
                viewHolder.section90.setBackgroundColor(badColor);
            }
        }

        viewHolder.avg7View.setText(CustomNumberFormatter.formatToThreeCharacters(avg7));
        viewHolder.best7View.setText(CustomNumberFormatter.formatToThreeCharacters(best7));
        viewHolder.avg30View.setText(CustomNumberFormatter.formatToThreeCharacters(avg30));
        viewHolder.best30View.setText(CustomNumberFormatter.formatToThreeCharacters(best30));
        viewHolder.avg90View.setText(CustomNumberFormatter.formatToThreeCharacters(avg90));
        viewHolder.best90View.setText(CustomNumberFormatter.formatToThreeCharacters(best90));
    }

    private static class ViewHolder {
        public final SwipeLayout swipeLayout;
        public final TextView titleView;
        public final TextView todayView;
        public final TextView avg7View;
        public final TextView best7View;
        public final TextView avg30View;
        public final TextView best30View;
        public final TextView avg90View;
        public final TextView best90View;
        public final LinearLayout section7;
        public final LinearLayout section30;
        public final LinearLayout section90;

        public ViewHolder(View view) {
            swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_layout);
            titleView = (TextView) view.findViewById(R.id.list_item_title);
            todayView = (TextView) view.findViewById(R.id.list_item_today);
            avg7View = (TextView) view.findViewById(R.id.list_item_7_avg);
            best7View = (TextView) view.findViewById(R.id.list_item_7_best);
            avg30View = (TextView) view.findViewById(R.id.list_item_30_avg);
            best30View = (TextView) view.findViewById(R.id.list_item_30_best);
            avg90View = (TextView) view.findViewById(R.id.list_item_90_avg);
            best90View = (TextView) view.findViewById(R.id.list_item_90_best);
            section7 = (LinearLayout) view.findViewById(R.id.section_7);
            section30 = (LinearLayout) view.findViewById(R.id.section_30);
            section90 = (LinearLayout) view.findViewById(R.id.section_90);
        }
    }

    public class TagParams{
        public long mActivityId;
        public boolean mHigherIsBetter;
        public float mSwipeValue;

        public TagParams(long activityId, boolean higherIsBetter, float swipeValue) {
            this.mActivityId = activityId;
            this.mHigherIsBetter = higherIsBetter;
            this.mSwipeValue = swipeValue;
        }
    }
}
