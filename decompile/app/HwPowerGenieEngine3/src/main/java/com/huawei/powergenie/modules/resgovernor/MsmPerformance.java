package com.huawei.powergenie.modules.resgovernor;

import android.util.Log;
import java.util.ArrayList;

public final class MsmPerformance {
    private static int CPUS_ON_2 = 1794;
    private static int CPUS_ON_3 = 1795;
    private static int CPUS_ON_MAX = 1796;
    private static Object mPerfLock = null;
    private boolean mIsPerfLocking = false;

    protected boolean initPerfLock() {
        try {
            Class c = Class.forName("org.codeaurora.Performance");
            if (c == null) {
                Log.w("MsmPerformance", "not found Performance");
                return false;
            }
            CPUS_ON_2 = Integer.valueOf(String.valueOf(c.getField("CPUS_ON_2").get(c))).intValue();
            CPUS_ON_3 = Integer.valueOf(String.valueOf(c.getField("CPUS_ON_3").get(c))).intValue();
            CPUS_ON_MAX = Integer.valueOf(String.valueOf(c.getField("CPUS_ON_MAX").get(c))).intValue();
            mPerfLock = c.getConstructor(new Class[0]).newInstance(new Object[0]);
            mPerfLock.getClass().getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
            mPerfLock.getClass().getMethod("perfLockRelease", new Class[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean perfLockAcquire(int freq, int minCores) {
        boolean ret = false;
        if (mPerfLock != null) {
            try {
                ArrayList<Integer> value = new ArrayList();
                if (minCores == 2) {
                    value.add(Integer.valueOf(CPUS_ON_2));
                } else if (minCores == 3) {
                    value.add(Integer.valueOf(CPUS_ON_3));
                } else if (minCores >= 4) {
                    value.add(Integer.valueOf(CPUS_ON_MAX));
                }
                if (freq != -1) {
                    int frequency = freq / 100000;
                    value.add(Integer.valueOf(frequency + 512));
                    if (minCores == 2) {
                        value.add(Integer.valueOf(frequency + 768));
                    } else if (minCores == 3) {
                        value.add(Integer.valueOf(frequency + 768));
                        value.add(Integer.valueOf(frequency + 1024));
                    } else if (minCores >= 4) {
                        value.add(Integer.valueOf(frequency + 768));
                        value.add(Integer.valueOf(frequency + 1024));
                        value.add(Integer.valueOf(frequency + 1280));
                    }
                }
                if (value.size() == 0) {
                    return false;
                }
                int[] freqArray = new int[value.size()];
                int i = 0;
                for (Integer v : value) {
                    int i2 = i + 1;
                    freqArray[i] = v.intValue();
                    i = i2;
                }
                mPerfLock.getClass().getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class}).invoke(mPerfLock, new Object[]{Integer.valueOf(1000), freqArray});
                this.mIsPerfLocking = true;
                ret = true;
            } catch (NoSuchMethodException e) {
                Log.w("MsmPerformance", "NoSuchMethod: perfLockAcquire [int]");
            } catch (Exception e2) {
                Log.w("MsmPerformance", "perfLockAcquire: other error");
            }
        } else {
            Log.w("MsmPerformance", "mPerfLock is null");
        }
        return ret;
    }

    protected boolean perfLockRelease() {
        if (this.mIsPerfLocking && mPerfLock != null) {
            try {
                mPerfLock.getClass().getMethod("perfLockRelease", new Class[0]).invoke(mPerfLock, new Object[0]);
                this.mIsPerfLocking = false;
            } catch (Exception e) {
                Log.w("MsmPerformance", "perfLockRelease: other error");
                return false;
            }
        }
        return true;
    }
}
