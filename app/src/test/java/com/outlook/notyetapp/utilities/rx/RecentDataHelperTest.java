package com.outlook.notyetapp.utilities.rx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.HabitData;
import com.outlook.notyetapp.library.TestException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.exceptions.OnErrorFailedException;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// This slows it way down, but this way,
// I don't need to add a ContentValues factory to provide in the constructor
// And I don't have to mock it. That *really* isn't the class under test.
@RunWith(RobolectricTestRunner.class)
public class RecentDataHelperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    final float ALLOWED_DELTA = .00001f;

    @Mock
    StorIOContentResolverHelper mockStorIOContentResolverHelper;

    @Mock
    DateHelper mockDateHelper;

    @Mock
    ContentResolver mockContentResolver;

    @Mock
    HabitContractUriBuilder mockHabitContractUriBuilder;

    @Mock
    Uri mockHabitDataEntryUri;

    @Mock
    Uri mockActivitiesStatsUri;

    RecentDataHelper recentDataHelper;




    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

//        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
//            @Override
//            public Scheduler getMainThreadScheduler() {
//                return Schedulers.immediate();
//            }
//        });
        recentDataHelper = new RecentDataHelper(mockStorIOContentResolverHelper, mockDateHelper, mockHabitContractUriBuilder);
    }

    @Test
    public void getRecentDataTransformerTest(){
        long FAKE_ACTIVITY_ID = 22;
        float FAKE_VALUE = .3f;
        HabitContract.HabitDataEntry.HabitValueType habitValueType = HabitContract.HabitDataEntry.HabitValueType.USER;
        RecentDataHelper.Params params = new RecentDataHelper.Params(FAKE_ACTIVITY_ID, FAKE_VALUE, habitValueType);

        ArrayList<HabitData> habitDatas = new ArrayList<>();
        habitDatas.add(new HabitData(){{value=2f;date=6L;}});
        habitDatas.add(new HabitData(){{value=3f;date=5L;}});
        habitDatas.add(new HabitData(){{value=4f;date=4L;}});
        habitDatas.add(new HabitData(){{value=5f;date=3L;}});
        habitDatas.add(new HabitData(){{value=6f;date=2L;}});
        habitDatas.add(new HabitData(){{value=7f;date=1L;}});

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();

        when(mockStorIOContentResolverHelper.getSingleListOfObjectsHabitDataNormalizeLimit90OnIOThread(FAKE_ACTIVITY_ID))
                .thenReturn(Single.just((List<HabitData>)habitDatas));
        when(mockStorIOContentResolverHelper.getContentResolver()).thenReturn(mockContentResolver);
        when(mockDateHelper.getTodaysDBDate()).thenReturn(8L);

        when(mockHabitContractUriBuilder.getHabitDataEntryUri()).thenReturn(mockHabitDataEntryUri);
        when(mockHabitContractUriBuilder.buildActivitiesStatsUri()).thenReturn(mockActivitiesStatsUri);

        Observable.just(params)
                .compose(recentDataHelper.getRecentDataTransformer()).subscribe(testSubscriber);

        ArgumentCaptor<ContentValues[]> captorContentValues = ArgumentCaptor.forClass(ContentValues[].class);
        ArgumentCaptor<Uri> captorUri = ArgumentCaptor.forClass(Uri.class);
        verify(mockContentResolver).bulkInsert(captorUri.capture(), captorContentValues.capture());

        InOrder inOrder = inOrder(mockContentResolver, mockStorIOContentResolverHelper);

        inOrder.verify(mockContentResolver, times(1)).bulkInsert(any(Uri.class), any(ContentValues[].class));
        inOrder.verify(mockStorIOContentResolverHelper, times(1)).notifyChangeAtUri(mockActivitiesStatsUri);

        ContentValues[] contentValues = captorContentValues.getValue();
        Uri uri = captorUri.getValue();

        assertEquals(mockHabitDataEntryUri, uri);

        assertEquals((Long)7L, contentValues[0].getAsLong(HabitContract.HabitDataEntry.COLUMN_DATE));
        assertEquals((Long)FAKE_ACTIVITY_ID, contentValues[0].getAsLong(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID));
        assertEquals(3.899999f, contentValues[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(3.899999f, contentValues[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(3.899999f, contentValues[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertEquals((Integer)2, contentValues[0].getAsInteger(HabitContract.HabitDataEntry.COLUMN_TYPE));
        assertEquals(FAKE_VALUE, contentValues[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_VALUE), ALLOWED_DELTA);

        assertEquals((Long)8L, contentValues[1].getAsLong(HabitContract.HabitDataEntry.COLUMN_DATE));
        assertEquals((Long)FAKE_ACTIVITY_ID, contentValues[1].getAsLong(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID));
        assertEquals(2.942857f, contentValues[1].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(3.4499998f, contentValues[1].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(3.4499998f, contentValues[1].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertEquals((Integer)HabitContract.HabitDataEntry.HabitValueType.USER.getValue(), contentValues[1].getAsInteger(HabitContract.HabitDataEntry.COLUMN_TYPE));
        assertEquals(FAKE_VALUE, contentValues[1].getAsFloat(HabitContract.HabitDataEntry.COLUMN_VALUE), ALLOWED_DELTA);
    }

    @Test
    public void getRecentDataTransformerErrorPathTest(){
        //onError in our subscriber rethrows all errors because anything it errors on should be fatal.
        //This is the error that wraps anything that gets thrown.
        exception.expect(OnErrorFailedException.class);

        long FAKE_ACTIVITY_ID = 22;
        float FAKE_VALUE = .3f;

        HabitContract.HabitDataEntry.HabitValueType habitValueType = HabitContract.HabitDataEntry.HabitValueType.USER;
        RecentDataHelper.Params params = new RecentDataHelper.Params(FAKE_ACTIVITY_ID, FAKE_VALUE, habitValueType);

        ArrayList<HabitData> habitDatas = new ArrayList<>();
        habitDatas.add(new HabitData(){{value=2f;date=6L;}});

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();

        when(mockStorIOContentResolverHelper.getSingleListOfObjectsHabitDataNormalizeLimit90OnIOThread(FAKE_ACTIVITY_ID))
                .thenReturn(Single.just((List<HabitData>)habitDatas));
        when(mockStorIOContentResolverHelper.getContentResolver()).thenReturn(mockContentResolver);
        when(mockDateHelper.getTodaysDBDate()).thenReturn(0L); //This will make our code throw an error.

        when(mockHabitContractUriBuilder.getHabitDataEntryUri()).thenReturn(mockHabitDataEntryUri);
        when(mockHabitContractUriBuilder.buildActivitiesStatsUri()).thenReturn(mockActivitiesStatsUri);

        Observable.just(params)
                .compose(recentDataHelper.getRecentDataTransformer()).subscribe(testSubscriber);
    }

}