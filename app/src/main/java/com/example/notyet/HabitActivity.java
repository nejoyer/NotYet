package com.example.notyet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notyet.data.HabitContract;
import com.example.notyet.utilities.CustomNumberFormatter;
import com.example.notyet.utilities.GraphUtilities;
import com.example.notyet.utilities.TextValidator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// When you tap on an activity in the Main ListView, you see this activity which includes a graph and list of data points.
// You can click any of the data points to update it, or swipe right on multiple data points to bulk update.
// The content mentioned above is all done in the fragment so that it can be included in the main activity on tablets.
public class HabitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_habit);

        if(savedInstanceState == null)
        {
            Bundle args = getIntent().getExtras();

            HabitActivityFragment fragment = HabitActivityFragment.newInstance(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.habit_activity_frame, fragment)
                    .commit();
        }
    }
}