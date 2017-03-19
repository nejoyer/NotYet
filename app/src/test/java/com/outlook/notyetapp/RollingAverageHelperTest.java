package com.outlook.notyetapp;

import com.outlook.notyetapp.utilities.RollingAverageHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RollingAverageHelperTest {
    private RollingAverageHelper mHelper;

    @Before
    public void setUp() throws Exception {
        mHelper = new RollingAverageHelper();
        for (int i = 1; i<91;i++) {
            mHelper.PushNumber(i);
        }
    }

    @Test
    public void average7_isCorrect() throws Exception {
        assertEquals(87f, mHelper.GetAverage7(), 0.00001f);
    }

    @Test
    public void average30_isCorrect() throws Exception {
        assertEquals(75.5f, mHelper.GetAverage30(), 0.00001f);
    }

    @Test
    public void average90_isCorrect() throws Exception {
        assertEquals(45.5f, mHelper.GetAverage90(), 0.00001f);
    }

}