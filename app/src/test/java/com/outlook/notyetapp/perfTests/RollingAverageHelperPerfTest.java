package com.outlook.notyetapp.perfTests;

import com.outlook.notyetapp.RollingAverageHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Neil on 10/20/2016.
 */

@RunWith(MockitoJUnitRunner.class)
public class RollingAverageHelperPerfTest {
        private RollingAverageHelper mHelper;

        @Before
        public void setUp() throws Exception {
            mHelper = new RollingAverageHelper();

        }

        @After
        public void tearDown() throws Exception {

        }

        @Test
        public void time90Averages() throws Exception {
            //warm up
//            compute90Averages();

            long startTime = System.nanoTime();
            compute90Averages();
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            //divide by 1000000 to get milliseconds.
            //assertThat("time90Averages", duration, lessThan(3323760L));
        }

        public void compute90Averages(){
            Random r = new Random();
            for (int i = 1; i<91;i++) {
                mHelper.PushNumber(r.nextFloat());
                float a = mHelper.GetAverage7();
                float b = mHelper.GetAverage30();
                float c = mHelper.GetAverage90();
                System.out.println(c);
            }
        }
}
