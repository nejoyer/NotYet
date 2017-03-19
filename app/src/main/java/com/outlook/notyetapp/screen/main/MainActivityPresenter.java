package com.outlook.notyetapp.screen.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.R;
import com.outlook.notyetapp.data.DBHelper;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.SharedPreferencesManager;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.RecentData;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.observers.AsyncCompletableSubscriber;
import rx.schedulers.Schedulers;

public class MainActivityPresenter implements MainActivityContract.ActionListener {

    public static final String DEMO_DATABASE_NAME = "notyet_demo.db";

    private MainActivityContract.View view;
    private StorIOContentResolverHelper storIOContentResolverHelper;
    private HabitContractUriBuilder habitContractUriBuilder;
    private DateHelper dateHelper;
    private Context context;
    private SharedPreferencesManager sharedPreferencesManager;
    private UpdateHabitDataHelper updateHabitDataHelper;
    private UpdateStatsHelper updateStatsHelper;
    private RecentDataHelper recentDataHelper;

    private Subscription todayStatsSubscription;
    private Subscription recentDataSubscription;

    private long queryDate = 0;
    private long dbDateLastUpdatedTo = 0;

    // Constructed via DI, so lots of params are okay. This helps with unit testing.
    public MainActivityPresenter(MainActivityContract.View view,
                                 StorIOContentResolverHelper storIOContentResolverHelper,
                                 HabitContractUriBuilder habitContractUriBuilder,
                                 DateHelper dateHelper,
                                 Context context,
                                 SharedPreferencesManager sharedPreferencesManager,
                                 UpdateHabitDataHelper updateHabitDataHelper,
                                 UpdateStatsHelper updateStatsHelper,
                                 RecentDataHelper recentDataHelper) {
        this.view = view;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.habitContractUriBuilder = habitContractUriBuilder;
        this.dateHelper = dateHelper;
        this.context = context;
        this.sharedPreferencesManager = sharedPreferencesManager;
        this.updateHabitDataHelper = updateHabitDataHelper;
        this.updateStatsHelper = updateStatsHelper;
        this.recentDataHelper = recentDataHelper;
    }

    public static final SimpleDateFormat eulaAgreementFormat = new SimpleDateFormat("MMMMM dd, yyyy");

    // determine whether to show the EULA (based on if it has changed since the user agreed to it).
    @Override
    public void onAttached() {
        Date lastAgreedDate = null;
        Date agreementLastUpdatedDate = null;

        String lastAgreed = sharedPreferencesManager.getEULAAgreed();

        try {
            lastAgreedDate = eulaAgreementFormat.parse(lastAgreed);
            agreementLastUpdatedDate = eulaAgreementFormat.parse(context.getString(R.string.eula_date_updated));
        }
        catch (ParseException e){}
        if(lastAgreedDate != null && agreementLastUpdatedDate != null && agreementLastUpdatedDate.after(lastAgreedDate)){
            view.showEULA();
        }
    }

    @Override
    public void onResumed(boolean showAll, int numberOfHabits) {
        subscribeToTodaysStats(showAll);
        updateActivitiesIfNecessary(numberOfHabits);
    }

    // the HabitData table needs entries for all dates up to today.
    // If the app hasn't been opened in a few days, we need to generate new entries.
    // Number of habits is strictly for analytics sake, and this is the most convienent place to log it.
    public void updateActivitiesIfNecessary(int numberOfHabits){

        long todaysDBDate = this.dateHelper.getTodaysDBDate();

        if(dbDateLastUpdatedTo == 0){
            dbDateLastUpdatedTo = this.sharedPreferencesManager.getLastDBDateUpdatedTo();
        }

        if(todaysDBDate > dbDateLastUpdatedTo)
        {
            if(recentDataSubscription != null && !recentDataSubscription.isUnsubscribed()){
                recentDataSubscription.unsubscribe();
            }
            recentDataSubscription = storIOContentResolverHelper.getObservableListOfObjectsRecentData()
                    .flatMap(new Func1<List<RecentData>, Observable<RecentData>>() {
                        @Override
                        public Observable<RecentData> call(List<RecentData> recentDatas) {
                            return Observable.from(recentDatas);
                        }
                    })
                    .map(new Func1<RecentData, RecentDataHelper.Params>() {
                        @Override
                        public RecentDataHelper.Params call(RecentData recentData) {
                            float valueToUse = 0;
                            //If higher is better then we default to 0, but if lower is better,
                            // then we want to use the (presumably) bad historical value until the user enters another value.
                            if(recentData.higherIsBetter != 1) {
                                valueToUse = recentData.historical;
                            }

                            return new RecentDataHelper.Params(recentData._id, valueToUse, HabitContract.HabitDataEntry.HabitValueType.NEVERENTERED);
                        }
                    })
                    .compose(recentDataHelper.getRecentDataTransformer()).subscribe();


            long daysUpdated = todaysDBDate - dbDateLastUpdatedTo;
            dbDateLastUpdatedTo = todaysDBDate;

            sharedPreferencesManager.setLastDBDateUpdatedTo(dbDateLastUpdatedTo);

            Bundle bundle = new Bundle();
            bundle.putLong(AnalyticsConstants.ParamNames.NUMBER_OF_HABITS, numberOfHabits);
            bundle.putLong(AnalyticsConstants.ParamNames.DAYS_UPDATED, daysUpdated);
            bundle.putLong(FirebaseAnalytics.Param.VALUE, daysUpdated);
            NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.UPDATE_HABITS_IF_NECESSARY, bundle);
        }
    }

    // subscribe to the DB to get the data necessary to render the ListView.
    // if the data changes, the ListView should get the updates because of the todayStatsSubscription.
    @Override
    public void subscribeToTodaysStats(final boolean showAll) {

        this.unsubscribe();

        String selection = null;
        String[] selectionArgs = null;

        queryDate = dateHelper.getTodaysDBDate();
        String dbDateString = String.valueOf(queryDate);

        if (showAll) {
            selection = HabitContract.HabitDataEntry.COLUMN_DATE + " = ?";
            selectionArgs = new String[]{dbDateString};
        } else {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            // Users can configure habits to only show on certain days of the week.
            // ex. golf on Saturday and Sunday.
            // use a mask to only show habits that are configured to be shown for the current day of the week.
            String dayOfWeekMask = "0";
            switch (day) {
                case Calendar.SUNDAY:
                    dayOfWeekMask = "1";
                    break;
                case Calendar.MONDAY:
                    dayOfWeekMask = "2";
                    break;
                case Calendar.TUESDAY:
                    dayOfWeekMask = "4";
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeekMask = "8";
                    break;
                case Calendar.THURSDAY:
                    dayOfWeekMask = "16";
                    break;
                case Calendar.FRIDAY:
                    dayOfWeekMask = "32";
                    break;
                case Calendar.SATURDAY:
                    dayOfWeekMask = "64";
                    break;
            }

            selection = HabitContract.HabitDataEntry.COLUMN_DATE + " = ? AND "
                    + HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE + " < ? AND "
                    + HabitContract.ActivitiesEntry.COLUMN_DAYS_TO_SHOW + " & ?";
            selectionArgs = new String[]{dbDateString, dbDateString, dayOfWeekMask};
        }


        todayStatsSubscription = storIOContentResolverHelper
                .getCursorActivitiesTodaysStatsOnMainThread(selection, selectionArgs)
                .subscribe(new Subscriber<Cursor>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        throw Exceptions.propagate(e);
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        // if it is a new day since the query ran
                        // (ex. user went to bed and now it is morning and the app is still open)
                        // we need to change our query to reflect the new date (and day of the week).
                        if(queryDate != dateHelper.getTodaysDBDate()){
                            subscribeToTodaysStats(showAll);
                            return;
                        }
                        view.renderData(cursor);
                    }
                });
    }

    @Override
    public void itemClicked(long activityId, String activityTitle) {
        view.showActivity(activityId, activityTitle);
    }

    // Hide the activity for the rest of the day
    @Override
    public void swipeRight(long activityId) {
        NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.SWIPE_HIDE);
        hideActivityForToday(activityId);
    }

    // Set the value for today to a pre-configured value.
    // then hide the activity for the rest of the day
    @Override
    public void swipeLeft(final long activityId, float swipeValue) {
        ArrayList<Long> datesToUpdate = new ArrayList<Long>(1);
        datesToUpdate.add(dateHelper.getTodaysDBDate());

        Single.just(
                new UpdateHabitDataHelper.Params(
                        activityId,
                        datesToUpdate,
                        swipeValue,
                        HabitContract.HabitDataEntry.HabitValueType.USER))
                .observeOn(Schedulers.io())
                .compose(
                        updateHabitDataHelper.getUpdateHabitDataTransformer()
                )
                .toCompletable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AsyncCompletableSubscriber() {
                               @Override
                               public void onCompleted() {
                                   updateStatsHelper.UpdateStats(activityId);
                                   hideActivityForToday(activityId);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   throw Exceptions.propagate(e);
                               }
                           }
                );

        NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.SWIPE_HIDE_AND_UPDATE);
    }

    public void hideActivityForToday(final long activityId){
        Completable.fromCallable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE, dateHelper.getTodaysDBDate());

                        storIOContentResolverHelper.getContentResolver().update(
                                habitContractUriBuilder.buildActivityUri(activityId),
                                contentValues,
                                null, // selection, handled by Uri
                                null  // selectionArgs, handled by Uri
                        );
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AsyncCompletableSubscriber() {
                    @Override
                    public void onCompleted() {
                        storIOContentResolverHelper.notifyChangeAtUri(habitContractUriBuilder.buildActivitiesStatsUri());
                    }

                    @Override
                    public void onError(Throwable e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    // Doesn't need to be async because this is for demos only. Doesn't necessarily need to be UnitTested either.
    @Override
    public void resetDemo() {
        sharedPreferencesManager.clearPreferences();

        DBHelper helper = new DBHelper(context);
        helper.copyDemoDB(DEMO_DATABASE_NAME);
        helper.updateDemoDBByDate();
    }

    // Don't leak references to the activity.
    @Override
    public void unsubscribe() {
        if(todayStatsSubscription != null)
        {
            if(!todayStatsSubscription.isUnsubscribed()){
                todayStatsSubscription.unsubscribe();
            }
        }
    }
}
