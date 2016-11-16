package com.example.notyet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.notyet.data.HabitContract;
import com.mobeta.android.dslv.DragSortCursorAdapter;

// Handle laying out the SortActivity list items. Also includes functionality to save the order to the DB.
public class SortAdapter extends DragSortCursorAdapter{

    private Context mContext = null;

    public SortAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_sort, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.titleView.setText(cursor.getString(HabitContract.ActivitySortQueryHelper.COLUMN_ACTIVITY_TITLE));
    }

    // Called from the activity when the user hits save.
    public void PersistSort()
    {
        Cursor cursor = getCursor();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            int listPos = getListPosition(cursor.getPosition());
            if (listPos != cursor.getPosition()) {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY, listPos);
                mContext.getContentResolver().update(
                        HabitContract.ActivitiesEntry.buildActivityUri(
                                cursor.getLong(HabitContract.ActivitySortQueryHelper.COLUMN_ACTIVITY_ID)
                        ),
                        contentValues,
                        null, // selection, handled by Uri
                        null // selectionArgs, handled by Uri
                );
            }
        }
        mContext.getContentResolver().notifyChange(HabitContract.ActivitiesTodaysStatsQueryHelper.buildActivitiesStatsUri(), null);
    }

    private static class ViewHolder {
        public final TextView titleView;

        public ViewHolder(View view) {
            this.titleView = (TextView)view.findViewById(R.id.sort_activity_title);
        }
    }
}
