package com.outlook.notyetapp.screen.habit;

import android.database.Cursor;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.utilities.library.Presenter;

import java.util.ArrayList;
import java.util.List;

public class HabitActivityFragmentContract {
    public interface View{
        void renderHabitDataToGraph(List<DataPoint[]> data);
        void renderHabitDataToList(Cursor data);
        void renderBestData(String activityTitle, float best7, float best30, float best90);
        void showMultiSelectDialog();
        void currentForecastData(float forecast);
        void showGraph();
        void showAddMoreHistoryDialog();
        void showUpdatingDialog();
        void hideUpdatingDialog();
    }
    public interface ActionListener extends Presenter, HabitDataAdapter.ChecksChangedListener {
        void subscribeToHabitDataAndBestData(long activityId);
        void addMoreHistoryClicked();
        void addMoreHistoryDialogOKClicked(long activityId, int numberOfDaysToAdd);
        void updateHabitDataClicked(long activityId, long dateToUpdate, float newVal);
        void updateHabitDataClicked(long activityId, ArrayList<Long> datesToUpdate, float newValue);
        void multiSelectCancelClicked(long activityId);
    }
}
