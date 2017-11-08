package com.huawei.systemmanager.power.model;

import android.app.AlarmManager;
import android.content.Context;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.InvocationTargetException;

public class ChangeMode {
    public static final int GENIE_NORMAL_MODE = 3;
    public static final int GENIE_SMART_MODE = 2;
    public static final int GENIE_SUPER_MODE = 1;
    private static final String TAG = "ChangeMode";
    private static ChangeMode sChangeMode = null;
    private Context mContext;

    private ChangeMode(Context context) {
        this.mContext = context;
    }

    public static ChangeMode getInstance(Context context) {
        if (sChangeMode == null) {
            sChangeMode = new ChangeMode(context.getApplicationContext());
        }
        return sChangeMode;
    }

    public void change(int mSaveModeNum) {
        if (mSaveModeNum == 0) {
            normalSpan(this.mContext, mSaveModeNum);
        } else if (mSaveModeNum == 1) {
            smartSpan(this.mContext, mSaveModeNum);
        } else if (mSaveModeNum == 2) {
            superSpan(this.mContext, mSaveModeNum);
        }
    }

    public void wirteSaveMode(int mSaveMode, int genieValue) {
        HwLog.i(TAG, "wirteSaveMode, old function, it does not work from 5.0");
    }

    public int readSaveMode() {
        HwLog.i(TAG, "readSaveMode, old function, it does not work from 5.0");
        return 1;
    }

    private void normalSpan(Context context, int mSaveMode) {
        setConnect(context, "min_level");
        setAlarmPolicyState(context, false);
        setUseCtrlSocket(context, true);
        wirteSaveMode(0, 3);
    }

    private void smartSpan(Context context, int mSaveMode) {
        setConnect(context, "normal_level");
        setAlarmPolicyState(context, true);
        setUseCtrlSocket(context, false);
        wirteSaveMode(1, 2);
    }

    private void superSpan(Context context, int mSaveMode) {
        setConnect(context, "normal_level");
        setAlarmPolicyState(context, true);
        setUseCtrlSocket(context, false);
        wirteSaveMode(2, 1);
    }

    private void setConnect(Context context, String level) {
        ConnectivityManagerEx.getDefault().setSmartKeyguardLevel(level);
    }

    private void setAlarmPolicyState(Context context, boolean flag) {
        AlarmManager am = (AlarmManager) context.getApplicationContext().getSystemService("alarm");
        try {
            am.getClass().getMethod("setAlarmPolicyState", new Class[]{Boolean.TYPE}).invoke(am, new Object[]{Boolean.valueOf(flag)});
        } catch (NoSuchMethodException e) {
            HwLog.w(TAG, "setAlarmPolicyState, NoSuchMethodException");
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    private void setUseCtrlSocket(Context context, boolean state) {
        ConnectivityManagerEx.getDefault().setUseCtrlSocket(state);
    }
}
