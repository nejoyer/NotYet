package com.outlook.notyetapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.outlook.notyetapp.utilities.DemoUtils;

// Pull all the menu handling code for the Main Activity into a fragment to avoid making that file too long.
public class MainMenuFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public boolean mShowAll = false;

    private int mVisibilityChangedCount = 0;

    private Menu mMenu = null;
    private MenuInflater mMenuInflater = null;

    private MenuItem mVisibilityChangedMenuItem;


    /**
     * Static factory method that takes an boolean parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static MainMenuFragment newInstance(boolean showAll) {
        MainMenuFragment fragment = new MainMenuFragment();
        Bundle args = new Bundle();
        args.putBoolean(MainActivity.SHOW_ALL_KEY, showAll);
        fragment.setArguments(args);
        return fragment;
    }

    public void loadMemberVariablesFromBundle(Bundle bundle){
        mShowAll = bundle.getBoolean(MainActivity.SHOW_ALL_KEY);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null)
        {
            loadMemberVariablesFromBundle(getArguments());
        }
        else {
            loadMemberVariablesFromBundle(savedInstanceState);
        }

        setHasOptionsMenu(true);
    }

    public void inflateDemoMenu(){
        if(BuildConfig.FLAVOR.equals("demo")){
            mMenuInflater.inflate(R.menu.main_menu_demo_fragment, mMenu);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MainActivity.SHOW_ALL_KEY, mShowAll);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mVisibilityChangedMenuItem = menu.findItem(R.id.action_visibility_changed);
        setVisiblityIcon();
    }

    public void setVisiblityIcon()
    {

        mVisibilityChangedMenuItem.setChecked(mShowAll);
        if(mShowAll)
        {
            mVisibilityChangedMenuItem.setIcon(R.drawable.ic_visibility_off_black_24dp);
        }
        else {
            mVisibilityChangedMenuItem.setIcon(R.drawable.ic_visibility_black_24dp);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu_fragment, menu);
        if(BuildConfig.FLAVOR.equals("demo")){
            mMenu = menu;
            mMenuInflater = inflater;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_create_new_activity:
                Intent settingsIntent = new Intent(getActivity(), CreateActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_visibility_changed:
                mShowAll = !mShowAll;
                if(BuildConfig.FLAVOR.equals("demo")){
                    mVisibilityChangedCount++;
                    if(mVisibilityChangedCount == 5)
                    {
                        inflateDemoMenu();
                    }
                }
                setVisiblityIcon();
                mListener.visibilityChanged(mShowAll);
                return true;
            case R.id.action_change_sort:
                Intent sortIntent = new Intent(getActivity(), SortActivity.class);
                startActivity(sortIntent);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_help:
                startActivity(new Intent(getActivity(), HelpActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            case R.id.action_demo_copy_db:
                DemoUtils.CopyDemoDBAndUpdate(getActivity());
                ((MainActivity)getActivity()).forceUpdateActivities();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment. This allows this fragment to call the activity back when
     * the visibility button has been toggled.
     */
    public interface OnFragmentInteractionListener {
        void visibilityChanged(boolean showAll);
    }
}
