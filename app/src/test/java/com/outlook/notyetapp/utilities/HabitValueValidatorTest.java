package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.widget.TextView;

import com.outlook.notyetapp.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Neil on 2/6/2017.
 */
public class HabitValueValidatorTest {


    HabitValueValidator habitValueValidator;

    @Mock
    Context context;

    @Mock
    TextView textView;

    private final String FAKE_STRING = "FAKE_STRING";

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        habitValueValidator = new HabitValueValidator(textView, context);
    }

    @Test
    public void happyPathTestNegative(){
        boolean result = habitValueValidator.validate("-125");
        assertTrue(result);
        verify(textView, never()).setError((String)any());
    }

    @Test
    public void happyPathTestDecimal(){
        boolean result = habitValueValidator.validate(".45");
        assertTrue(result);
        verify(textView, never()).setError((String)any());
    }

    @Test
    public void happyPathTestLarge(){
        boolean result = habitValueValidator.validate("464645");
        assertTrue(result);
        verify(textView, never()).setError((String)any());
    }

    @Test
    public void TestEmpty(){
        when(context.getString(R.string.cannot_be_empty)).thenReturn(FAKE_STRING);
        boolean result = habitValueValidator.validate("");
        assertFalse(result);
        verify(textView).setError(FAKE_STRING);
    }

    @Test
    public void TestLettersAndNumbers(){
        when(context.getString(R.string.must_be_a_number)).thenReturn(FAKE_STRING);
        boolean result = habitValueValidator.validate("33a4");
        assertFalse(result);
        verify(textView).setError(FAKE_STRING);
    }

    @Test
    public void TestMultipleDecimals(){
        when(context.getString(R.string.must_be_a_number)).thenReturn(FAKE_STRING);
        boolean result = habitValueValidator.validate("33.4.7");
        assertFalse(result);
        verify(textView).setError(FAKE_STRING);
    }
}