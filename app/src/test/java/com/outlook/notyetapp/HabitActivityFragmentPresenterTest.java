package com.outlook.notyetapp;

import android.database.Cursor;
import android.net.Uri;

import com.jjoe64.graphview.series.DataPoint;
import com.outlook.notyetapp.data.DateHelper;
import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.data.models.ActivitySettings;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentContract;
import com.outlook.notyetapp.screen.habit.HabitActivityFragmentPresenter;
import com.outlook.notyetapp.utilities.rx.CursorToDataPointListHelper;
import com.outlook.notyetapp.utilities.rx.RXMappingFunctionHelper;
import com.outlook.notyetapp.utilities.rx.UpdateHabitDataHelper;
import com.outlook.notyetapp.utilities.rx.UpdateStatsHelper;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGet;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGetCursor;
import com.pushtorefresh.storio.contentresolver.operations.get.PreparedGetListOfObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.internal.operators.OperatorPublish;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PreparedGet.Builder.class,
        PreparedGetCursor.Builder.class,
        PreparedGetCursor.CompleteBuilder.class,
        Observable.class,
        ConnectableObservable.class,
        OperatorPublish.class,
        Single.class,
        Completable.class})
public class HabitActivityFragmentPresenterTest {
    private static final String FAKE_TITLE = "mock title";
    private static final long FAKE_ACTIVITY_ID = 44;
    private static final float FAKE_NEW_VALUE = .5f;
    private static final long FAKE_DATE_TO_UPDATE = 33;
    private static final int FAKE_DAYS_TO_SHOW = 4;
    private static final float FAKE_HISTORICAL = .1f;
    private static final float FAKE_FORECAST = .4f;
    private static final float FAKE_SWIPE_VALUE = .4f;
    private static final int FAKE_HIGHER_IS_BETTER = 1;
    private static final float FAKE_BEST7 = 1.1f;
    private static final float FAKE_BEST30 = 1.2f;
    private static final float FAKE_BEST90 = 1.3f;

    @Mock
    HabitActivityFragmentContract.View mockView;

    @Mock
    CursorToDataPointListHelper mockCursorToDataPointListHelper;

    @Mock
    UpdateHabitDataHelper mockUpdateHabitDataHelper;

    @Mock
    UpdateStatsHelper mockUpdateStatsHelper;

    @Mock
    HabitContractUriBuilder mockHabitContractUriBuilder;

    @Mock
    StorIOContentResolverHelper mockStorIOContentResolverHelper;

    @Mock
    RXMappingFunctionHelper mockRXMappingFunctionHelper;

    @Mock
    DateHelper mockDateHelper;

    @Mock
    Single.Transformer<UpdateHabitDataHelper.Params, Void> mockTransformer;

    @Mock
    Uri mockUri;

    //Mocked by PowerMock
    Observable mockObservable;
    Single mockSingle, mockSingle2, mockSingle3, mockSingle4, mockSingle5;
    Completable mockCompletable;


    private HabitActivityFragmentPresenter habitActivityFragmentPresenter;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });

        // Use PowerMock to create these mocks because some of them are "final" and mockito mocking doesn't work.
        mockObservable = PowerMockito.mock(Observable.class);

        mockSingle = PowerMockito.mock(Single.class);
        mockSingle2 = PowerMockito.mock(Single.class);
        mockSingle3 = PowerMockito.mock(Single.class);
        mockSingle4 = PowerMockito.mock(Single.class);
        mockSingle5 = PowerMockito.mock(Single.class);

        mockCompletable = PowerMockito.mock(Completable.class);

        habitActivityFragmentPresenter = new HabitActivityFragmentPresenter(this.mockView,
                this.mockCursorToDataPointListHelper,
                this.mockUpdateHabitDataHelper,
                this.mockUpdateStatsHelper,
                this.mockHabitContractUriBuilder,
                this.mockStorIOContentResolverHelper,
                this.mockRXMappingFunctionHelper,
                this.mockDateHelper);

        when(mockUpdateHabitDataHelper.getUpdateHabitDataTransformer()).thenReturn(mockTransformer);
        when(mockHabitContractUriBuilder.buildHabitDataUriForActivity(anyLong())).thenReturn(mockUri);
    }

    @After
    public void after() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void subscribeToHabitDataAndBestDataTest() {
        Cursor habitDataCursor = mock(Cursor.class);
        List<DataPoint[]> dataPoints = Arrays.asList(new DataPoint[]{new DataPoint(5,5)}, new DataPoint[]{new DataPoint(6,6)});
        ConnectableObservable spyConnectableObservable = spy(Observable.just(habitDataCursor).publish());

        //Transformer needs to be tested separately
        doReturn(Observable.just(dataPoints))
                .when(spyConnectableObservable)
                .compose((rx.Observable.Transformer<Cursor, List<DataPoint[]>>)any());

        when(mockStorIOContentResolverHelper.getCursorHabitDataOnMainThread(anyLong())).thenReturn(mockObservable);
        when(mockObservable.publish()).thenReturn(spyConnectableObservable);

        ActivitySettings activitySettings = new ActivitySettings();
        activitySettings.title = FAKE_TITLE;
        activitySettings.best7 = FAKE_BEST7;
        activitySettings.best30 = FAKE_BEST30;
        activitySettings.best90 = FAKE_BEST90;
        activitySettings.forecast = FAKE_FORECAST;

        List<ActivitySettings> activitySettingsList = new ArrayList<>();
        activitySettingsList.add(activitySettings);

        when(mockStorIOContentResolverHelper.getObservableListOfObjectsActivitySettingsOnMainThread(anyLong())).thenReturn(Observable.just(activitySettingsList));

        habitActivityFragmentPresenter.subscribeToHabitDataAndBestData(1);

        verify(mockView).renderHabitDataToGraph(dataPoints);
        verify(mockView).renderHabitDataToList(habitDataCursor);
        verify(mockView).renderBestData(FAKE_TITLE, FAKE_BEST7, FAKE_BEST30, FAKE_BEST90);
    }

    @Test
    public void checksChangedWithItemsTest(){
        ArrayList<Long> checkedItems = mock(ArrayList.class);
        when(checkedItems.size()).thenReturn(22);
        habitActivityFragmentPresenter.ChecksChanged(checkedItems);
        verify(mockView).showMultiSelectDialog();
        verify(mockView, never()).showGraph();
    }
    @Test
    public void checksChangedWithNoItemsTest(){
        ArrayList<Long> checkedItems = mock(ArrayList.class);
        when(checkedItems.size()).thenReturn(0);
        habitActivityFragmentPresenter.ChecksChanged(checkedItems);
        verify(mockView).showGraph();
        verify(mockView, never()).showMultiSelectDialog();
    }

    @Test
    public void addMoreHistoryClickedTest(){
        habitActivityFragmentPresenter.addMoreHistoryClicked();
        verify(mockView).showAddMoreHistoryDialog();
    }

    @Test
    public void addMoreHistoryDialogOKClickedTest(){
        when(mockStorIOContentResolverHelper.getSingleListOfObjectsHabitDataOldestDateOnIOThread(anyLong())).thenReturn(mockSingle);
        when(mockSingle.map(any(Func1.class))).thenReturn(mockSingle2);
        when(mockStorIOContentResolverHelper.getSingleListofObjectsActivitySettingsOnIOThread(anyLong())).thenReturn(mockSingle3);
        when(mockSingle2.zipWith(any(Single.class), any(Func2.class))).thenReturn(mockSingle4);

        when(mockSingle4.compose(any(Single.Transformer.class))).thenReturn(mockSingle5);
        when(mockSingle5.toCompletable()).thenReturn(mockCompletable);
        //todo ensure that this scheduler is mainthread.
        when(mockCompletable.observeOn(any(Scheduler.class))).thenReturn(Completable.complete());

        habitActivityFragmentPresenter.addMoreHistoryDialogOKClicked(22, 3);

        InOrder inOrder = inOrder(mockView, mockSingle4, mockUpdateStatsHelper);
        inOrder.verify(mockView).showUpdatingDialog();
        inOrder.verify(mockSingle4).compose(mockTransformer);
        inOrder.verify(mockView).hideUpdatingDialog();
        inOrder.verify(mockUpdateStatsHelper).UpdateStats(anyLong());
    }

    @Test
    public void updateHabitDataClickedNoDialogTest() {
        when(mockDateHelper.getTodaysDBDateIgnoreOffset()).thenReturn(0L);

        updateHabitDataTestShared();
        InOrder inOrder = inOrder(mockView, mockSingle2, mockUpdateStatsHelper);
        inOrder.verify(mockView, never()).showUpdatingDialog();
        inOrder.verify(mockSingle2).compose(mockTransformer);
        inOrder.verify(mockView).hideUpdatingDialog();
        inOrder.verify(mockUpdateStatsHelper).UpdateStats(FAKE_ACTIVITY_ID);
    }

    @Test
    public void updateHabitDataClickedWithDialogTest() {
        when(mockDateHelper.getTodaysDBDateIgnoreOffset()).thenReturn(255L);

        updateHabitDataTestShared();
        InOrder inOrder = inOrder(mockView, mockSingle2, mockUpdateStatsHelper);
        inOrder.verify(mockView).showUpdatingDialog();
        inOrder.verify(mockSingle2).compose(mockTransformer);
        inOrder.verify(mockView).hideUpdatingDialog();
        inOrder.verify(mockUpdateStatsHelper).UpdateStats(FAKE_ACTIVITY_ID);
    }

    public void updateHabitDataTestShared(){
        mockStatic(Single.class);
        ArgumentCaptor<UpdateHabitDataHelper.Params> captor = ArgumentCaptor.forClass(UpdateHabitDataHelper.Params.class);

        when(Single.just(Matchers.any())).thenReturn(mockSingle);
        when(mockSingle.observeOn(any(Scheduler.class))).thenReturn(mockSingle2);
        when(mockSingle2.compose(any(Single.Transformer.class))).thenReturn(mockSingle3);
        when(mockSingle3.toCompletable()).thenReturn(mockCompletable);
        when(mockCompletable.observeOn(any(Scheduler.class))).thenReturn(Completable.complete());

        habitActivityFragmentPresenter.updateHabitDataClicked(FAKE_ACTIVITY_ID, FAKE_DATE_TO_UPDATE, FAKE_NEW_VALUE);
        verifyStatic();
        Single.just(captor.capture());
        ArrayList<Long> arrayList = new ArrayList<Long>(1);
        arrayList.add(FAKE_DATE_TO_UPDATE);
        assertEquals(
                new UpdateHabitDataHelper.Params(
                        FAKE_ACTIVITY_ID,
                        arrayList,
                        FAKE_NEW_VALUE,
                        HabitContract.HabitDataEntry.HabitValueType.USER
                ),
                captor.getValue()
        );
    }
}