package com.android.settings;

import android.util.Log;
import java.lang.reflect.Method;

public class Reflect {
    static Reflect mInstance = null;
    private Method mCallATCommand;
    private boolean mClassExsit = false;
    private Method mGetInstance;
    private boolean mMethodExsit = false;
    private Class<?> mReflect;

    protected static Reflect getDefaultInstance() {
        if (mInstance == null) {
            mInstance = new Reflect();
        }
        return mInstance;
    }

    protected void initClass() {
        try {
            this.mReflect = Class.forName("com.huawei.telephony.HuaweiTelephonyManager");
            this.mCallATCommand = this.mReflect.getMethod("ATDirectChannel", new Class[]{String.class});
            this.mGetInstance = this.mReflect.getMethod("getDefault", new Class[]{(Class) null});
            this.mClassExsit = true;
            this.mMethodExsit = true;
        } catch (Exception e) {
            this.mClassExsit = false;
            this.mMethodExsit = false;
            Log.e("Reflect", e.toString());
        }
    }

    String[] callATCommand(String atCommand) {
        String[] response = null;
        if (this.mClassExsit && this.mMethodExsit) {
            try {
                Object obj = this.mGetInstance.invoke((Object[]) null, new Object[0]);
                return (String[]) this.mCallATCommand.invoke(obj, new Object[]{atCommand});
            } catch (Exception e) {
                Log.e("Reflect", e.toString());
                return response;
            }
        }
        Log.d("Reflect", "class or method does not exsit");
        return response;
    }
}
