package com.outlook.notyetapp.utilities;

import android.content.Context;
import android.widget.TextView;

import com.outlook.notyetapp.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TitleValidatorTest {

    TitleValidator titleValidator;

        @Mock
        Context context;

        @Mock
        TextView textView;

        private final String FAKE_STRING = "FAKE_STRING";

        @Before
        public void setup(){
            MockitoAnnotations.initMocks(this);

            titleValidator = new TitleValidator(textView, context);
        }

        @Test
        public void happyPath(){
            boolean result = titleValidator.validate("helloworld");
            assertTrue(result);
            verify(textView, never()).setError((String)any());
        }

        @Test
        public void TestEmpty(){
            when(context.getString(R.string.cannot_be_empty)).thenReturn(FAKE_STRING);
            boolean result = titleValidator.validate("");
            assertFalse(result);
            verify(textView).setError(FAKE_STRING);
        }
    }