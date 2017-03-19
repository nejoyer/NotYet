package com.outlook.notyetapp.screen.activitysettings;

import android.content.ContentValues;
import android.database.Cursor;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.observers.AsyncCompletableSubscriber;

public class ActivitySettingsActivityPresenter implements ActivitySettingsActivityContract.ActionListener{

    private ActivitySettingsActivityContract.View view;
    private StorIOContentResolverHelper storIOContentResolverHelper;
    private HabitContractUriBuilder habitContractUriBuilder;
    private UpdateHabitDataHelper updateHabitDataHelper;
    private UpdateStatsHelper updateStatsHelper;

    public ActivitySettingsActivityPresenter(ActivitySettingsActivityContract.View view,
                                             StorIOContentResolverHelper storIOContentResolverHelper,
                                             HabitContractUriBuilder habitContractUriBuilder,
                                             UpdateHabitDataHelper updateHabitDataHelper,
                                             UpdateStatsHelper updateStatsHelper) {
        this.view = view;
        this.storIOContentResolverHelper = storIOContentResolverHelper;
        this.habitContractUriBuilder = habitContractUriBuilder;
        this.updateHabitDataHelper = updateHabitDataHelper;
        this.updateStatsHelper = updateStatsHelper;
    }

    @Override
    public void doneClicked(final long activityId) {
        if(this.view.validate())
        {
            final ContentValues contentValues = this.view.getSettingsFromUI();
            final float newHistorical = contentValues.getAsFloat(HabitContract.ActivitiesEntry.COLUMN_HISTORICAL);

            this.storIOContentResolverHelper.getSingleListofObjectsActivitySettingsOnIOThread(activityId)
                    .map(new Func1<List<ActivitySettings>, UpdateHabitDataHelper.Params>() {
                             @Override
                             public UpdateHabitDataHelper.Params call(List<ActivitySettings> activitySettingses) {
                                 storIOContentResolverHelper.getContentResolver().update(habitContractUriBuilder.buildActivityUri(activityId), contentValues, null, null);
                                 ActivitySettings activitySettings = activitySettingses.get(0);
                                 if(activitySettings.historical != newHistorical){
                                     Cursor cursor = null;
                                     ArrayList<Long> datesToUpdate;
                                     try {
                                         cursor = storIOContentResolverHelper.getContentResolver().query(habitContractUriBuilder.buildHabitDataUriForActivity(activityId),
                                                 new String[]{HabitContract.HabitDataEntry.COLUMN_DATE},
                                                 HabitContract.HabitDataEntry.COLUMN_TYPE + " = ? ",
                                                 new String[]{String.valueOf(HabitContract.HabitDataEntry.HabitValueType.HISTORICAL.getValue())},
                                                 null); /* sort order */

                                         datesToUpdate = new ArrayList<Long>(cursor.getCount());
                                         cursor.moveToPosition(-1);
                                         while (cursor.moveToNext()) {
                                             datesToUpdate.add(cursor.getLong(0));
                                         }
                                     }
                                     finally {
                                         if(cursor != null){
                                             cursor.close();
                                         }
                                     }

                                     return new UpdateHabitDataHelper.Params(activityId,
                                             datesToUpdate,
                                             newHistorical,
                                             HabitContract.HabitDataEntry.HabitValueType.HISTORICAL);
                                 }
                                 return null;
                             }
                         }
                    )
                    .compose(updateHabitDataHelper.getUpdateHabitDataTransformer())
                    .toCompletable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new AsyncCompletableSubscriber() {
                                   @Override
                                   public void onCompleted() {
                                       updateStatsHelper.UpdateStats(activityId);
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       throw Exceptions.propagate(e);
                                   }
                               }
                    );

            view.closeActivity();
        }
    }
}
