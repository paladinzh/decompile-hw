package com.android.server.rms.test;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.io.IOStatsService;
import com.android.server.rms.utils.Utils;

public class TestIOResourceService {
    private static final String SPLIT = ",";
    private static final String TAG = "IO.TestIOResourceService";
    private static IOStatsService mIOStatsService = null;
    private static TestIOResourceService mTestIOResourceService;

    private TestIOResourceService() {
    }

    public static synchronized TestIOResourceService getInstance(Context context) {
        TestIOResourceService testIOResourceService;
        synchronized (TestIOResourceService.class) {
            if (mTestIOResourceService == null) {
                mTestIOResourceService = new TestIOResourceService();
            }
            if (mIOStatsService == null) {
                mIOStatsService = IOStatsService.getInstance(null, null);
            }
            testIOResourceService = mTestIOResourceService;
        }
        return testIOResourceService;
    }

    public void testPeriodMonitorTask() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testPeriodMonitorTask");
        }
        try {
            mIOStatsService.periodMonitorTask();
        } catch (Exception ex) {
            Log.e(TAG, "testPeriodMonitorTask,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testPeriodReadTask() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testPeriodReadTask");
        }
        try {
            mIOStatsService.periodReadTask();
        } catch (Exception ex) {
            Log.e(TAG, "testReadStatsFromKernel,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testRefreshAddUidMonitored(String uidPkg) {
        try {
            String[] splitArray = uidPkg.split(SPLIT);
            mIOStatsService.refreshMonitoredUids(false, Integer.parseInt(splitArray[0]), splitArray[1]);
        } catch (Exception ex) {
            Log.e(TAG, "testRefreshAddUidMonitored,uidPkg parameter is invalid");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testRefreshRemoveUidMonitored(String uidPkg) {
        try {
            mIOStatsService.refreshMonitoredUids(true, Integer.parseInt(uidPkg.split(SPLIT)[0]), null);
        } catch (Exception ex) {
            Log.e(TAG, "testRefreshRemoveUidMonitored,uidPkg parameter is invalid");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testShutdown() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testShutdown");
        }
        try {
            mIOStatsService.saveIOStatsAndLatestUids(true);
        } catch (Exception ex) {
            Log.e(TAG, "saveIOStatsAndLatestUids,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }
}
