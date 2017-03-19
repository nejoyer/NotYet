package com.outlook.notyetapp.screen.graph;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.utilities.library.Presenter;

import java.util.List;

public interface GraphActivityContract {
    interface View{
        void renderHabitData(List<DataPoint[]> data);
        void showTodayLine();
        void hideTodayLine();
    }
    interface ActionListener extends Presenter{
        void xAxisChanged(double minX, double maxX);
        void loadHabitData (long acitivityId, float forecast);
    }
}
