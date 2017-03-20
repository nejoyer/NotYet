package com.outlook.notyetapp.utilities.rx;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.MatrixCursor;
import android.net.Uri;

import com.outlook.notyetapp.data.HabitContract;
import com.outlook.notyetapp.data.HabitContractUriBuilder;
import com.outlook.notyetapp.data.StorIOContentResolverHelper;
import com.outlook.notyetapp.utilities.ContentProviderOperationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import rx.Single;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Need this to be able to use MatrixCursor (mocking the matrix cursor is impractical),
// as well as other android components like ContentProviderOperation.newUpdate.
// This is why the tests run slowly.
@RunWith(RobolectricTestRunner.class)
public class UpdateHabitDataHelperTest {

    final float ALLOWED_DELTA = .00001f;

    @Mock
    HabitContractUriBuilder mockHabitContractUriBuilder;

    @Mock
    StorIOContentResolverHelper mockStorIOContentResolverHelper;

    @Mock
    ContentProviderOperationFactory mockContentProviderOperationFactory;

    @Mock
    ContentResolver mockContentResolver;

    @Mock
    Uri mockUri;

    // declare this one here to avoid nested generic problem
    // http://stackoverflow.com/questions/5606541/how-to-capture-a-list-of-specific-type-with-mockito
    @Captor
    private ArgumentCaptor<ArrayList<ContentProviderOperation>> contentProviderOperationCaptor;


    UpdateHabitDataHelper updateHabitDataHelper;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        updateHabitDataHelper = new UpdateHabitDataHelper(
                mockHabitContractUriBuilder,
                mockStorIOContentResolverHelper,
                mockContentProviderOperationFactory);
    }

    @Test
    public void getUpdateHabitDataTransformerWithHistoricalTest() throws Exception{

        long FAKE_ACTIVITY_ID = 33;
        ArrayList<Long> datesToUpdate = new ArrayList<Long>();
        datesToUpdate.add(10L);
        datesToUpdate.add(11L);
        datesToUpdate.add(12L);
        float FAKE_NEW_VALUE = .4f;
        HabitContract.HabitDataEntry.HabitValueType NEW_TYPE = HabitContract.HabitDataEntry.HabitValueType.USER;

        UpdateHabitDataHelper.Params params = new UpdateHabitDataHelper.Params(
                FAKE_ACTIVITY_ID,
                datesToUpdate,
                FAKE_NEW_VALUE,
                NEW_TYPE);

        //// TODO: 3/20/2017
        MatrixCursor matrixCursor = new MatrixCursor(HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION);
        for(int i=11;i<111;i++){
            matrixCursor.addRow(new Object[]{i, i, i, i, i, i, i}); //populate some fake data oldest to newest
        }


        when(mockHabitContractUriBuilder.buildHabitDataUriForActivity(FAKE_ACTIVITY_ID)).thenReturn(mockUri);

        when(mockStorIOContentResolverHelper.getContentResolver()).thenReturn(mockContentResolver);
        when(mockContentResolver.query(mockUri,
                HabitContract.HabitDataQueryHelper.HABITDATA_PROJECTION,
                "date >= ? AND date <= ?",
                new String[] {"-80", "102"},
                HabitContract.HabitDataQueryHelper.SORT_BY_DATE_ASC)).thenReturn(matrixCursor);

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();

        Single.just(params)
                .compose(updateHabitDataHelper.getUpdateHabitDataTransformer())
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        ArgumentCaptor<Uri> uriCaptor = ArgumentCaptor.forClass(Uri.class);
        ArgumentCaptor<ContentValues[]> contentValuesesCaptor = ArgumentCaptor.forClass(ContentValues[].class);

        verify(mockContentResolver).bulkInsert(uriCaptor.capture(), contentValuesesCaptor.capture());
        assertEquals(HabitContract.HabitDataEntry.CONTENT_URI, uriCaptor.getValue());
        assertEquals(1, uriCaptor.getAllValues().size());
        ContentValues[] contentValueses = contentValuesesCaptor.getValue();
        assertEquals(1, contentValueses.length);
        assertEquals((Long)10L, contentValueses[0].getAsLong(HabitContract.HabitDataEntry.COLUMN_DATE));
        assertEquals((Long)FAKE_ACTIVITY_ID, contentValueses[0].getAsLong(HabitContract.HabitDataEntry.COLUMN_ACTIVITY_ID));
        assertEquals(FAKE_NEW_VALUE, contentValueses[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(FAKE_NEW_VALUE, contentValueses[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(FAKE_NEW_VALUE, contentValueses[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertEquals((Integer)NEW_TYPE.getValue(), contentValueses[0].getAsInteger(HabitContract.HabitDataEntry.COLUMN_TYPE));
        assertEquals(FAKE_NEW_VALUE, contentValueses[0].getAsFloat(HabitContract.HabitDataEntry.COLUMN_VALUE), ALLOWED_DELTA);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockContentResolver).applyBatch(stringCaptor.capture(), contentProviderOperationCaptor.capture());
        assertEquals(HabitContract.CONTENT_AUTHORITY, stringCaptor.getValue());
        ArrayList<ContentProviderOperation> operations = contentProviderOperationCaptor.getValue();
        assertEquals(91, operations.size());

        uriCaptor = ArgumentCaptor.forClass(Uri.class);
        ArgumentCaptor<ContentValues> contentValuesCaptor = ArgumentCaptor.forClass(ContentValues.class);
        verify(mockContentProviderOperationFactory, times(91)).getNewUpdate(uriCaptor.capture(), contentValuesCaptor.capture());
        List<Uri> uris = uriCaptor.getAllValues();
        assertEquals(91, uris.size());
        assertTrue(uris.get(0).toString().endsWith("11"));
        assertTrue(uris.get(1).toString().endsWith("12"));
        assertTrue(uris.get(2).toString().endsWith("13"));

        List<ContentValues> contentValues = contentValuesCaptor.getAllValues();
        assertEquals(91, contentValues.size());
        assertEquals(FAKE_NEW_VALUE, contentValues.get(0).getAsFloat(HabitContract.HabitDataEntry.COLUMN_VALUE), ALLOWED_DELTA);
        assertEquals(FAKE_NEW_VALUE, contentValues.get(0).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(FAKE_NEW_VALUE, contentValues.get(0).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(FAKE_NEW_VALUE, contentValues.get(0).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertEquals((Integer)NEW_TYPE.getValue(), contentValues.get(0).getAsInteger(HabitContract.HabitDataEntry.COLUMN_TYPE));

        assertFalse(contentValues.get(2).containsKey(HabitContract.HabitDataEntry.COLUMN_VALUE));
        assertEquals(3.55, contentValues.get(2).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(3.55, contentValues.get(2).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(3.55, contentValues.get(2).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertFalse(contentValues.get(2).containsKey(HabitContract.HabitDataEntry.COLUMN_TYPE));

        assertFalse(contentValues.get(90).containsKey(HabitContract.HabitDataEntry.COLUMN_VALUE));
        assertEquals(98.0, contentValues.get(90).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_7), ALLOWED_DELTA);
        assertEquals(86.5, contentValues.get(90).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_30), ALLOWED_DELTA);
        assertEquals(56.37110900878906, contentValues.get(90).getAsFloat(HabitContract.HabitDataEntry.COLUMN_ROLLING_AVG_90), ALLOWED_DELTA);
        assertFalse(contentValues.get(90).containsKey(HabitContract.HabitDataEntry.COLUMN_TYPE));
    }
}