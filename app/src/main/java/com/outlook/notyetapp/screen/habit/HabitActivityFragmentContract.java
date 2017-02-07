package com.outlook.notyetapp.screen.habit;

import android.database.Cursor;
import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.utilities.library.Presenter;

import java.util.List;

/**
 * Created by Neil on 2/1/2017.
 */

public class HabitActivityFragmentContract {
    public interface View{
        void renderHabitDataToGraph(List<DataPoint[]> data);
        void renderHabitDataToList(Cursor data);
        void renderBestData(String activityTitle, Boolean higherIsBetter, float best7, float best30, float best90);
        void showMultiSelectDialog();
        void showGraph();
    }
    public interface ActionListener extends Presenter, HabitDataAdapter.ChecksChangedListener {
        void loadHabitData (Uri habitDataUriForActivity, float forecast);
        void loadBestData (Uri activityUri);
    }
}
