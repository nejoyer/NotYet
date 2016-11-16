package com.example.notyet;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A fragment containing a "Done" and "Cancel" button at the top.
 * Activities that contain this fragment must implement the
 * {@link DoneCancelFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DoneCancelFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private AppCompatActivity mActivity;

    public DoneCancelFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the custom action bar, wire up the events, and display it.
        View actionBarButtons = inflater.inflate(R.layout.done_cancel_fragment,
                new LinearLayout(getActivity()), false);
        View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
        cancelActionView.setOnClickListener(mActionBarListener);
        View doneActionView = actionBarButtons.findViewById(R.id.action_done);
        doneActionView.setOnClickListener(mActionBarListener);

        mActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
        mActivity.getSupportActionBar().setCustomView(actionBarButtons);

        return null;
    }

    @Override
    public void onAttach(Context context) {
        mActivity = (AppCompatActivity)getActivity();
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

    private final View.OnClickListener mActionBarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.action_cancel:
                    mListener.cancelClicked();
                    break;
                case R.id.action_done:
                    mListener.doneClicked();
                    break;
                default:
                    throw new UnsupportedOperationException("invalid view clicked");
            }
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment. This allows this fragment to let the activity know which
     * button has been pressed so the activity can handle it.
     */
    public interface OnFragmentInteractionListener {
        void doneClicked();
        void cancelClicked();
    }
}
