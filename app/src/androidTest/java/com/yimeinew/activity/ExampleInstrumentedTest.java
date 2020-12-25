package com.yimeinew.activity;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.yimeinew.utils.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    //@Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.yimei.activity", appContext.getPackageName());
    }
    @Test
    public void myTTT(){
        String s1="PCT;PCC";
        String s2="PCT";
        System.err.println("cc="+s1.contains(s2));
    }
    @Test
    public void mmmm(){
        int ss=DateUtil.subSecond("2019-07-23 15:08:53","2019-07-23 12:29:29");
        System.err.println("ss="+ss);
    }
}
