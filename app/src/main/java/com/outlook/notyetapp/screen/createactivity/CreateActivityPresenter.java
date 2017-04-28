package com.outlook.notyetapp.screen.createactivity;

import android.content.ContentValues;
import android.database.SQLException;
import android.net.Uri;

import com.outlook.notyetapp.ActivitySettingsFragment;
import com.outlook.notyetapp.NotYetApplication;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.utilities.AnalyticsConstants;
import com.outlook.notyetapp.utilities.rx.RecentDataHelper;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.observers.AsyncCompletableSubscriber;

public class CreateActivityPresenter implements CreateActivityContract.ActionListener{

    private CreateActivityContract.View view;
    private StorIOContentResolverHelper storIOContentResolverHelper;
    private RecentDataHelper recentDataHelper;

    public CreateActivityPresenter(CreateActivityContract.View view, StorIOContentResolverHelper storIOContentResolverHelper, RecentDataHelper recentDataHelper) {
        this.view = view;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.recentDataHelper = recentDataHelper;
    }

    @Override
    public void doneClicked(ActivitySettingsFragment activitySettingsFragment) {
        if(view.validate())
        {
            ContentValues contentValues = activitySettingsFragment.getSettingsFromUI();
            contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST7, contentValues.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL));
            contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST30, contentValues.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL));
            contentValues.put(HabitContract.ActivitiesEntry.COLUMN_BEST90, contentValues.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL));
            //Put the activity at the end by default (this application isn't designed for more than 100 activities (not enforced).
            contentValues.put(HabitContract.ActivitiesEntry.COLUMN_SORT_PRIORITY, 999);
            contentValues.put(HabitContract.ActivitiesEntry.COLUMN_HIDE_DATE, 0);

            try {
                Uri resultUri = storIOContentResolverHelper.getContentResolver().insert(HabitContract.ActivitiesEntry.CONTENT_URI, contentValues);

                long activityId = HabitContract.ActivitiesEntry.getActivityNumberFromUri(resultUri);

                if (activityId != -1) {
                    Observable.just(new RecentDataHelper.Params(
                            activityId,
                            contentValues.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL),
                            HabitContract.HabitDataEntry.HabitValueType.HISTORICAL))
                            .compose(this.recentDataHelper.getRecentDataTransformer())
                            .toCompletable()
                            .subscribe(new AsyncCompletableSubscriber() {
                                           @Override
                                           public void onCompleted() {
                                               NotYetApplication.logFirebaseAnalyticsEvent(AnalyticsConstants.EventNames.HABIT_CREATED);
                                               view.closeActivity();
                                           }

                                           @Override
                                           public void onError(Throwable e) {
                                               throw Exceptions.propagate(e);
                                           }
                                       }
                            );
                }
            }
            catch (SQLException e){
                view.showError();
            }
        }
    }
}
