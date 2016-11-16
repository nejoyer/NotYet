package com.example.notyet;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.notyet.data.HabitContract;
import com.mobeta.android.dslv.DragSortListView;

// This activity allows the user to drag the activities around to specify a preferred order. (ex. "eat breakfast" comes before "tuck in kids")
public class SortActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DoneCancelFragment.OnFragmentInteractionListener{

    public SortAdapter mSortAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_sort, new DoneCancelFragment(), "DoneCancelFragment")
                .commit();

        setContentView(R.layout.activity_sort);

        mSortAdapter = new SortAdapter(this, null, 0);
        DragSortListView listView = (DragSortListView)findViewById(R.id.sort_activity_DSLV);
        listView.setAdapter(mSortAdapter);

        getSupportLoaderManager().initLoader(HabitContract.ActivitySortQueryHelper.ACTIVITES_SORT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,//context
                HabitContract.ActivitySortQueryHelper.getActivitiesUri(),//Uri
                HabitContract.ActivitySortQueryHelper.ACTIVITY_SORT_PROJECTION,//Projection
                null,//Selection This is taken care of in the provider
                null,//SelectionArgs This is taken care of in the provider
                HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY);//SortOrder This is taken care of in the provider
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSortAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSortAdapter.swapCursor(null);
    }

    @Override
    public void doneClicked() {
        mSortAdapter.PersistSort();
        finish();
    }

    @Override
    public void cancelClicked() {
        finish();
    }
}
