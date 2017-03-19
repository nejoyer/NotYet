package com.outlook.notyetapp.screen.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.outlook.notyetapp.ErrorActivity;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.dagger.ActivityScoped.DaggerMainActivityComponent;
import com.outlook.notyetapp.dagger.ActivityScoped.MainActivityModule;
import com.outlook.notyetapp.data.DBHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.screen.habit.HabitActivity;
import com.outlook.notyetapp.screen.habit.HabitActivityFragment;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.library.SwipeOpenListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

import static com.outlook.notyetapp.data.SharedPreferencesManager.DBDATE_LAST_UPDATED_TO_KEY;
import static com.outlook.notyetapp.data.SharedPreferencesManager.HIDE_HEADER_KEY;
import static com.outlook.notyetapp.data.SharedPreferencesManager.SHOULD_SHOW_HERO_TOOL_TIP_KEY;
import static com.outlook.notyetapp.data.SharedPreferencesManager.SHOW_ALL_KEY;

// Main activity is basically a list of Activities that the user has created that he/she wants to try to improve on.
public class MainActivity extends AppCompatActivity implements MainMenuFragment.OnFragmentInteractionListener, MainActivityContract.View {

    private ActivityAdapter mActivityAdapter;
    @BindView(R.id.main_listview)
    ListView mMainListView;

    private long mDBDateLastUpdatedTo = 0;
    private boolean mShowAll = false;
    private final static String CLICKED_POSITION_KEY = "clicked_position";
    private int mClickedPosition;
    private final static String SCROLLED_POSITION_KEY = "scrolled_position";
    private Parcelable mScrolledPosition;
    private boolean mFirstLoad = true;
    private boolean mShouldShowHeroToolTip = true;
    private boolean mHideHeader = false;

    private boolean mIsTwoPane = false;
    private static final String HABIT_ACTIVITY_FRAGMENT_TAG = "habit_activity_fragment_tag";

    @Inject
    MainActivityContract.ActionListener mPresenter;

    @Inject
    SharedPreferencesManager mSharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerMainActivityComponent.builder()
                .notYetApplicationComponent(NotYetApplication.get(this).component())
                .mainActivityModule(new MainActivityModule(this))
                .build().inject(this);

        if (savedInstanceState == null) {

            MobileAds.initialize(this, getString(R.string.admob_appid));

            final Thread.UncaughtExceptionHandler oldHandler =
                    Thread.getDefaultUncaughtExceptionHandler();

            //top level error handling
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    if (ex.getCause() != null && ex.getCause().getMessage() != null && ex.getCause().getMessage().compareTo(DBHelper.DOWNGRADE_NOT_SUPPORTED) == 0) {
                        Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
                        intent.putExtra(ErrorActivity.ERROR_MESSAGE_KEY, getString(R.string.downgrade_not_allowed));
                        MainActivity.this.startActivity(intent);
                    } else {

                        if (oldHandler != null) {
                            oldHandler.uncaughtException(
                                    thread,
                                    ex
                            ); //Delegates to Android's error handling
                        } else {
                            System.exit(2); //Prevents the service/app from freezing
                        }
                    }
                }
            });

            loadMemberVariablesFromPreferences();

            getSupportFragmentManager().beginTransaction()
                    .add(MainMenuFragment.newInstance(mShowAll), "MainMenuFragment")
                    .commit();
        } else {
            loadMemberVariablesFromBundle(savedInstanceState);
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter.onAttached();

        if (findViewById(R.id.right_pane_frame) != null) {
            mIsTwoPane = true;
        }

        mActivityAdapter = new ActivityAdapter(this, null, 0, swipeOpenListener);

        if (!mHideHeader) {
            mMainListView.addHeaderView(GenerateHelpHeader());
        }

        mMainListView.setAdapter(mActivityAdapter);

        AdView adView = (AdView) findViewById(R.id.main_banner_ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.test_device_id))
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    public void showEULA() {
        String message = getString(R.string.eula_date_updated_label) + getString(R.string.eula_date_updated) + "\n\n" +
                getString(R.string.eula_content);
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.eula_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.eula_agree), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String eulaAgreedDate = getString(R.string.eula_date_updated);
                        mSharedPreferencesManager.setEULAAgreed(eulaAgreedDate);
                    }
                })
                .setCancelable(false)
                .setNegativeButton(getString(R.string.eula_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    @OnItemClick(R.id.main_listview)
    public void onItemClick(AdapterView<?> adapterView, int position) {
        mClickedPosition = position;

        // CursorAdapter returns a cursor at the correct position for getItem(), or null
        // if it cannot seek to that position.
        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
        if (cursor != null) {
            mPresenter.itemClicked(
                    cursor.getLong(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_ID),
                    cursor.getString(HabitContract.ActivitiesTodaysStatsQueryHelper.COLUMN_ACTIVITY_TITLE));
        }
    }

    // navigate to the HabitActivity for the selected item if we are in one-pane mode
    // show the HabitFragment in the right pane if we are in two-pane mode for tablets
    @Override
    public void showActivity(long activityId, String activityTitle) {
        if (mIsTwoPane) {
            TextView rightPaneTitle = (TextView) findViewById(R.id.right_pane_title);
            rightPaneTitle.setText(activityTitle);
            HabitActivityFragment fragment = HabitActivityFragment.newInstance(activityId, mIsTwoPane);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_pane_frame, fragment, HABIT_ACTIVITY_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(MainActivity.this, HabitActivity.class);
            intent.putExtra(HabitActivityFragment.ACTIVITY_ID_KEY, activityId);

            startActivity(intent);
        }
    }

    private View GenerateHelpHeader() {
        final SwipeLayout header = (SwipeLayout) getLayoutInflater().inflate(R.layout.list_item_activity, mMainListView, false);
        header.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //This is a bit hacky. But this ensures that the header stays looking like the list items.
        LinearLayout section7 = (LinearLayout) header.findViewById(R.id.section_7);
        LinearLayout section7curTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section7curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section7curText = (TextView) section7.getChildAt(0);
        section7.removeViewAt(0);
        section7curTextAndHelp.addView(section7curText, 0);
        section7.addView(section7curTextAndHelp, 0);
        LinearLayout section7bestTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section7bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section7bestText = (TextView) section7.getChildAt(1);
        section7.removeViewAt(1);
        section7bestTextAndHelp.addView(section7bestText, 0);
        section7.addView(section7bestTextAndHelp, 1);

        LinearLayout section30 = (LinearLayout) header.findViewById(R.id.section_30);
        LinearLayout section30curTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section30curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section30curText = (TextView) section30.getChildAt(0);
        section30.removeViewAt(0);
        section30curTextAndHelp.addView(section30curText, 0);
        section30.addView(section30curTextAndHelp, 0);
        LinearLayout section30bestTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section30bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section30bestText = (TextView) section30.getChildAt(1);
        section30.removeViewAt(1);
        section30bestTextAndHelp.addView(section30bestText, 0);
        section30.addView(section30bestTextAndHelp, 1);

        LinearLayout section90 = (LinearLayout) header.findViewById(R.id.section_90);
        LinearLayout section90curTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section90curTextAndHelp.setOnClickListener(mHeaderHelpCurrentClicked);
        TextView section90curText = (TextView) section90.getChildAt(0);
        section90.removeViewAt(0);
        section90curTextAndHelp.addView(section90curText, 0);
        section90.addView(section90curTextAndHelp, 0);
        LinearLayout section90bestTextAndHelp = (LinearLayout) getLayoutInflater().inflate(R.layout.main_header_info_button_segment, header, false);
        section90bestTextAndHelp.setOnClickListener(mHeaderHelpBestClicked);
        TextView section90bestText = (TextView) section90.getChildAt(1);
        section90.removeViewAt(1);
        section90bestTextAndHelp.addView(section90bestText, 0);
        section90.addView(section90bestTextAndHelp, 1);

        TextView badBottomText = (TextView) header.findViewById(R.id.bad_bottom_text);
        badBottomText.setText(getString(R.string.list_item_activity_header_hide));
        TextView goodBottomText = (TextView) header.findViewById(R.id.good_bottom_text);
        goodBottomText.setText(getString(R.string.list_item_activity_header_hide));
        header.removeAllSwipeListener();
        header.addSwipeListener(new SwipeOpenListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                mMainListView.removeHeaderView(header);
                mHideHeader = true;
                mSharedPreferencesManager.setHideHeader(mHideHeader);
            }
        });

        return header;
    }

    private View.OnClickListener mHeaderHelpCurrentClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.help_title))
                    .setMessage(getString(R.string.main_header_help_current))
                    .setIcon(R.drawable.ic_dialog_info_with_tint)
                    .show();
        }
    };
    private View.OnClickListener mHeaderHelpBestClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.help_title))
                    .setMessage(getString(R.string.main_header_help_best))
                    .setIcon(R.drawable.ic_dialog_info_with_tint)
                    .show();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putLong(DBDATE_LAST_UPDATED_TO_KEY, mDBDateLastUpdatedTo);
        saveInstanceState.putBoolean(SHOW_ALL_KEY, mShowAll);
        saveInstanceState.putInt(CLICKED_POSITION_KEY, mClickedPosition);
        saveInstanceState.putParcelable(SCROLLED_POSITION_KEY, mScrolledPosition);
        saveInstanceState.putBoolean(SHOULD_SHOW_HERO_TOOL_TIP_KEY, mShouldShowHeroToolTip);
        saveInstanceState.putBoolean(HIDE_HEADER_KEY, mHideHeader);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadMemberVariablesFromBundle(savedInstanceState);
    }

    // When the user swipes one of the habits, extract the info attached to the tag for that list item and pass it to the presenter.
    private SwipeOpenListener swipeOpenListener = new SwipeOpenListener() {
        @Override
        public void onOpen(SwipeLayout layout) {
            ActivityAdapter.TagParams tagParams = (ActivityAdapter.TagParams) layout.findViewById(R.id.list_item_title).getTag();

            if (layout.getDragEdge() == SwipeLayout.DragEdge.Right) {
                mPresenter.swipeLeft(tagParams.mActivityId, tagParams.mSwipeValue);
            } else {
                mPresenter.swipeRight(tagParams.mActivityId);
            }
        }
    };

    private void loadMemberVariablesFromBundle(Bundle savedInstanceState) {
        mDBDateLastUpdatedTo = savedInstanceState.getLong(DBDATE_LAST_UPDATED_TO_KEY);
        mShowAll = savedInstanceState.getBoolean(SHOW_ALL_KEY);
        mClickedPosition = savedInstanceState.getInt(CLICKED_POSITION_KEY);
        mScrolledPosition = savedInstanceState.getParcelable(SCROLLED_POSITION_KEY);
        mShouldShowHeroToolTip = savedInstanceState.getBoolean(SHOULD_SHOW_HERO_TOOL_TIP_KEY);
        mHideHeader = savedInstanceState.getBoolean(HIDE_HEADER_KEY);
    }

    private void loadMemberVariablesFromPreferences() {
        mDBDateLastUpdatedTo = mSharedPreferencesManager.getLastDBDateUpdatedTo();
        mShowAll = mSharedPreferencesManager.getShowAll();
        mShouldShowHeroToolTip = mSharedPreferencesManager.getShouldShowHeroToolTip();
        mHideHeader = mSharedPreferencesManager.getHideHeader();
    }

    // When we resume (this also gets called on initial launch), let the presenter know to subscribe to the db.
    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResumed(mShowAll, mActivityAdapter.getCount());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mActivityAdapter.getCount() < 1 && mShouldShowHeroToolTip) {
            mMainListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View createButton = findViewById(R.id.action_create_new_activity);

                    boolean animated = true;
                    //Try to animate it bouncing unless we know they've tried to turn off animation.
                    if (Build.VERSION.SDK_INT >= 17) {
                        animated = Settings.System.getFloat(getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0) != 0;
                    }

                    new SimpleTooltip.Builder(MainActivity.this)
                            .anchorView(createButton)
                            .contentView(R.layout.hero_tooltip, R.id.tooltip_text)
                            .text(getString(R.string.no_activities_new_user_hero))
                            .gravity(Gravity.BOTTOM)
                            .animated(animated)
                            .transparentOverlay(false)
                            .onDismissListener(
                                    new SimpleTooltip.OnDismissListener() {
                                        @Override
                                        public void onDismiss(SimpleTooltip tooltip) {
                                            mShouldShowHeroToolTip = false;
                                            mSharedPreferencesManager.setShouldShowHeroToolTip(mShouldShowHeroToolTip);
                                        }
                                    })
                            .build()
                            .show();
                }
            }, 500);

        }
    }

    // Render the data to the list view.
    @Override
    public void renderData(Cursor cursor) {
        // manually save the state of the list view in the case that the data is getting refreshed.
        // if this is the first load, then that means the activity has been re-created (ex. due to rotation)
        // so the state should have already been saved when it was previously destroyed... if we save the state
        // in that case, then it would be saving the state of an empty list view (and hence restore would scroll to the top).
        if(!mFirstLoad) {
            mScrolledPosition = mMainListView.onSaveInstanceState();
        }
        mFirstLoad = false;

        mActivityAdapter.swapCursor(cursor);

        // Restore the state of the list view. This keeps the user's scroll state when she rotates.
        if(mScrolledPosition != null) {
            mMainListView.onRestoreInstanceState(mScrolledPosition);
        }

        // make sure that the selected item in the list view is visible after the user rotates.
        if (mIsTwoPane && cursor.getCount() > 1 && mMainListView.getCheckedItemPosition() < 0) {
            if (mClickedPosition == 0 && !mHideHeader) {
                mClickedPosition = 1;
            }
            mMainListView.smoothScrollToPosition(mClickedPosition);
            mMainListView.post(new Runnable() {
                @Override
                public void run() {
                    mMainListView.performItemClick(mActivityAdapter.getView(mClickedPosition, null, null), mClickedPosition, mActivityAdapter.getItemId(mClickedPosition));
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScrolledPosition = mMainListView.onSaveInstanceState();
    }

    @Override
    protected void onStop() {
        mPresenter.unsubscribe();
        super.onStop();
    }

    // The user has tapped the eye icon in the appbar to signify that they want to see everything, or only uncompleted tasks.
    @Override
    public void visibilityChanged(boolean showAll) {
        mShowAll = showAll;
        NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.SHOW_ALL);
        mPresenter.subscribeToTodaysStats(mShowAll);
    }

    // Available via easter egg by tapping the eye icon six times (only in the internal demo version).
    @Override
    public void resetDemoClicked() {
        mPresenter.resetDemo();
    }
}
