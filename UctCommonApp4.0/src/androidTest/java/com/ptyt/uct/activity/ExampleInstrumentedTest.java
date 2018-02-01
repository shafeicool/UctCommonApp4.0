package com.ptyt.uct.activity;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ptyt.uct.utils.FileUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ptyt.uct.activity", appContext.getPackageName());


    }

    @Test
    public void fileHelperTest() throws IOException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        String path="fileTest/files";
        File file= FileUtils.getInstance(appContext).createSDDirection(path);
        file= FileUtils.getInstance(appContext).creatSDFile(path+"/file1.txt");
    }

}
