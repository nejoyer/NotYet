package com.outlook.notyetapp.screen.habit;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.utilities.CustomNumberFormatter;

import java.util.ArrayList;

// Data adapter to handle laying out of the list items in the HabitActivity list.
public class HabitDataAdapter extends CursorAdapter implements SwipeLayout.SwipeListener{

    private ChecksChangedListener mChecksChangedHandler = null;

    //todo don't make us use findViewById again
    @Override
    public void onOpen(SwipeLayout layout) {
        long date = (long)layout.findViewById(R.id.habit_list_item_date).getTag(R.id.habit_list_item_date_tag);
        if(!mSelectedDates.contains(date)) {
            mSelectedDates.add(date);
        }
        ChecksUpdated();
    }

    @Override
    public void onClose(SwipeLayout layout) {
        long date = (long)layout.findViewById(R.id.habit_list_item_date).getTag(R.id.habit_list_item_date_tag);
        mSelectedDates.remove(date);
        ChecksUpdated();
    }

    //required part of SwipeListener
    @Override
    public void onStartOpen(SwipeLayout layout) {}

    //required part of SwipeListener
    @Override
    public void onStartClose(SwipeLayout layout) {}

    //required part of SwipeListener
    @Override
    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

    //required part of SwipeListener
    @Override
    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}

    // Define this interface so that we can call into the Activity.
    public interface ChecksChangedListener{
        void ChecksChanged(ArrayList<Long> checkedItems);
    }

    public void setChecksChangedListener(ChecksChangedListener toSet) {
        mChecksChangedHandler = toSet;
    }

    public void ClearCheckmarks(){
        mSelectedDates.clear();
        ChecksUpdated();
    }

    public ArrayList<Long> GetSelectedDates(){
        return mSelectedDates;
    }

    private ArrayList<Long> mSelectedDates = new ArrayList<Long>();

    Context mContext = null;

    public HabitDataAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_habitdata, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.swipeLayout.addSwipeListener(this);
        view.setTag(R.id.habit_list_item_viewholder_tag, viewHolder);
        return view;
    }

    private void ChecksUpdated() {
        if(mChecksChangedHandler != null){
            mChecksChangedHandler.ChecksChanged(mSelectedDates);
        }
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setBackgroundColor(HabitContract.HabitDataEntry.HabitValueType.getColor(context, cursor.getInt(HabitContract.HabitDataQueryHelper.COLUMN_TYPE)));
        ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.habit_list_item_viewholder_tag);

        final long date = cursor.getLong(HabitContract.HabitDataQueryHelper.COLUMN_DATE);

        // This is really sad that we have to do it like this, but I couldn't get the Cursor Adapter that came with SwipeLayout to work properly
        // despite much effort.
        // So we have to reset the swipelayout ourselves as the user scrolls and the swipelayout is reused.
        if(mSelectedDates.contains(date)){
            viewHolder.swipeLayout.open(false, false, SwipeLayout.DragEdge.Left);
        }
        else {
            viewHolder.swipeLayout.close(false, false);
        }

        viewHolder.dateView.setText(HabitContract.HabitDataEntry.convertDBDateToString(date));
        viewHolder.dateView.setTag(R.id.habit_list_item_date_tag, date);
        viewHolder.valueView.setText(CustomNumberFormatter.formatToThreeCharacters(cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_VALUE)));
        viewHolder.avg7View.setText(CustomNumberFormatter.formatToThreeCharacters(cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_7)));
        viewHolder.avg30View.setText(CustomNumberFormatter.formatToThreeCharacters(cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_30)));
        viewHolder.avg90View.setText(CustomNumberFormatter.formatToThreeCharacters(cursor.getFloat(HabitContract.HabitDataQueryHelper.COLUMN_ROLLING_AVG_90)));
    }

    private static class ViewHolder {
        public final SwipeLayout swipeLayout;
        public final TextView dateView;
        public final TextView valueView;
        public final TextView avg7View;
        public final TextView avg30View;
        public final TextView avg90View;

        public ViewHolder(View view) {
            swipeLayout = (SwipeLayout) view.findViewById(R.id.habit_list_item_swipe_layout);
            dateView = (TextView) view.findViewById(R.id.habit_list_item_date);
            valueView = (TextView) view.findViewById(R.id.habit_list_item_value);
            avg7View = (TextView) view.findViewById(R.id.habit_list_item_7);
            avg30View = (TextView) view.findViewById(R.id.habit_list_item_30);
            avg90View = (TextView) view.findViewById(R.id.habit_list_item_90);
        }
    }
}
