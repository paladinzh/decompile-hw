package com.huawei.powergenie.modules.resgovernor;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.lang.reflect.Method;

public class ScalingFreqAdapterHisi extends ScalingFreqAdapter {
    private static Method mPerfConfigGet = null;
    private static Method mPerfConfigSet = null;
    private static Object mPerfHub = null;
    private static ScalingFreqAdapter sInstance = null;
    private final Context mContext;

    public static ScalingFreqAdapter getInstance(Context context) {
        ScalingFreqAdapter scalingFreqAdapter;
        synchronized (ScalingFreqAdapterHisi.class) {
            if (sInstance == null && initPerfHub(context)) {
                sInstance = new ScalingFreqAdapterHisi(context);
            }
            scalingFreqAdapter = sInstance;
        }
        return scalingFreqAdapter;
    }

    private static boolean initPerfHub(Context context) {
        if (SystemProperties.getBoolean("ro.config.hw_perfhub", false)) {
            try {
                Class clazz = context.getClassLoader().loadClass("com.hisi.perfhub.PerfHub");
                if (clazz != null) {
                    mPerfHub = clazz.newInstance();
                    mPerfConfigSet = mPerfHub.getClass().getMethod("perfConfigSet", new Class[]{int[].class, int[].class});
                    mPerfConfigGet = mPerfHub.getClass().getMethod("perfConfigGet", new Class[]{int[].class, int[].class});
                    return true;
                }
                Log.w("ScalingFreqAdapterHisi", "not found perfhub");
                return false;
            } catch (Exception e) {
                Log.v("ScalingFreqAdapterHisi", "init perfhub: ", e);
                return false;
            }
        }
        Log.w("ScalingFreqAdapterHisi", "no prop: hw_perfhub ");
        return false;
    }

    private ScalingFreqAdapterHisi(Context context) {
        this.mContext = context;
    }

    public boolean setFreq(int type, int value) {
        int[] types = new int[1];
        int[] values = new int[]{type};
        values[0] = value;
        return setFreq(types, values);
    }

    public boolean setFreq(int[] types, int[] values) {
        if (mPerfConfigSet != null) {
            try {
                return ((Integer) mPerfConfigSet.invoke(mPerfHub, new Object[]{types, values})).intValue() == 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w("ScalingFreqAdapterHisi", "perfConfigSet is not implement.");
            return false;
        }
    }

    public int getFreq(int type) {
        if (mPerfConfigGet != null) {
            try {
                int[] types = new int[1];
                int[] values = new int[]{type};
                if (((Integer) mPerfConfigGet.invoke(mPerfHub, new Object[]{types, values})).intValue() == 0) {
                    return values[0];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w("ScalingFreqAdapterHisi", "perfConfigGet is not implement.");
        }
        return -1;
    }
}
