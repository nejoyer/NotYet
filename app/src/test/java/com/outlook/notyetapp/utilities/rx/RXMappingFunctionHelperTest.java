package com.outlook.notyetapp.utilities.rx;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.data.models.HabitDataOldestDate;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

import static org.junit.Assert.*;

public class RXMappingFunctionHelperTest {

    RXMappingFunctionHelper rxMappingFunctionHelper = new RXMappingFunctionHelper();

    final float ALLOWED_DELTA = .00001f;

    @Test
    public void getHabitDataOldestDateToLongMappingFunctionTest(){
        ArrayList<HabitDataOldestDate> oldestDates = new ArrayList<>(3);
        oldestDates.add(new HabitDataOldestDate(){{date=6L;}});
        oldestDates.add(new HabitDataOldestDate(){{date=7L;}});
        oldestDates.add(new HabitDataOldestDate(){{date=8L;}});

        TestSubscriber<List<Long>> testSubscriber = new TestSubscriber<>();


        Observable.just(oldestDates).map(rxMappingFunctionHelper.getHabitDataOldestDateToLongMappingFunction(2)).subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        List<List<Long>> onNextEvents = testSubscriber.getOnNextEvents();
        assertEquals(1, onNextEvents.size());
        List<Long> onNextEvent = onNextEvents.get(0);
        assertEquals(2, onNextEvent.size());
        assertEquals((Long)4L, onNextEvent.get(0));
        assertEquals((Long)5L, onNextEvent.get(1));
    }

    @Test
    public void getLongActivitySettingsZipToUpdateHabitDataHelperParamsTest(){
        final long FAKE_ACTIVITY_ID = 22L;
        final float FAKE_HISTORICAL = .4F;

        ArrayList<ActivitySettings> activitySettingses = new ArrayList<>(1);
        activitySettingses.add(new ActivitySettings(){{_id = FAKE_ACTIVITY_ID; historical = FAKE_HISTORICAL;}});

        ArrayList<Long> longs = new ArrayList<>(3);
        longs.add(2L);
        longs.add(3L);
        longs.add(4L);

        TestSubscriber<UpdateHabitDataHelper.Params> testSubscriber = new TestSubscriber<>();

        Single.just(longs).zipWith(
                Single.just((List<ActivitySettings>)activitySettingses),
                rxMappingFunctionHelper.getLongActivitySettingsZipToUpdateHabitDataHelperParamsFunction()
        ).subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        List<UpdateHabitDataHelper.Params> onNextEvents = testSubscriber.getOnNextEvents();
        assertEquals(1, onNextEvents.size());
        UpdateHabitDataHelper.Params onNext = onNextEvents.get(0);
        assertEquals(FAKE_ACTIVITY_ID, onNext.mActivityId);
        assertEquals(longs, onNext.mDatesToUpdate);
        assertEquals(FAKE_HISTORICAL, onNext.mNewValue, ALLOWED_DELTA);
        assertEquals(HabitContract.HabitDataEntry.HabitValueType.HISTORICAL, onNext.mType);
    }

}