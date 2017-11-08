package com.huawei.keyguard.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.HwSecureWaterMark;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import java.util.ArrayList;
import java.util.HashSet;

public class WaterMarkUtils implements Runnable {
    private static final WaterMarkUtils sInst = new WaterMarkUtils();
    private static AsynRunner sLastRunner = null;
    private static int sWaterMarkSupportState = -1;
    private BroadcastReceiver mStateChangeReceiver = null;
    private int mWaterMarkNumber = -1;
    private HashSet<Runnable> mWaterStateChangeListeners = new HashSet();

    private static class AsynRunner implements Runnable {
        LoadeCallback mCallback;
        boolean mCanceld;
        Context mContext;
        Drawable mDrawable;

        public void run() {
            if (!this.mCanceld && this.mCallback != null) {
                final Drawable waterDrawable = WaterMarkUtils.addWaterMark(this.mContext, this.mDrawable);
                GlobalContext.getUIHandler().post(new Runnable() {
                    public void run() {
                        if (!AsynRunner.this.mCanceld) {
                            AsynRunner.this.mCallback.onWaterMarkAdded(waterDrawable);
                            synchronized (WaterMarkUtils.class) {
                                WaterMarkUtils.sLastRunner = null;
                            }
                        }
                    }
                });
            }
        }
    }

    public interface LoadeCallback {
        void onWaterMarkAdded(Drawable drawable);
    }

    private BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                    HwLog.d("WaterMarkUtils", "onReceive ACTION_SERVICE_STATE_CHANGED");
                    if (WaterMarkUtils.this.getMarkNumbFromDevice() >= 0) {
                        GlobalContext.getUIHandler().post(WaterMarkUtils.this);
                    }
                }
            }
        };
    }

    public void run() {
        unlisten();
        int markNumber = getMarkNumbFromDevice();
        boolean update = markNumber != this.mWaterMarkNumber;
        if (update) {
            this.mWaterMarkNumber = markNumber;
            storeMarkNumbInPref(markNumber);
        }
        ArrayList<Runnable> templist = new ArrayList();
        synchronized (this) {
            if (update) {
                templist.addAll(this.mWaterStateChangeListeners);
            }
            this.mWaterStateChangeListeners.clear();
        }
        HwLog.w("WaterMarkUtils", "Water mark changed. " + markNumber + "; templist " + templist.size());
        for (Runnable r : templist) {
            r.run();
        }
    }

    private void unlisten() {
        HwLog.i("WaterMarkUtils", "unlisten  ForStateChange. ");
        synchronized (this) {
            BroadcastReceiver tmpBR = this.mStateChangeReceiver;
            this.mStateChangeReceiver = null;
        }
        if (tmpBR != null) {
            GlobalContext.getContext().unregisterReceiver(tmpBR);
        }
    }

    private void listenForStateChange() {
        synchronized (this) {
            if (this.mStateChangeReceiver != null) {
                HwLog.i("WaterMarkUtils", "listenForStateChange Exit as already in listen");
                return;
            }
            BroadcastReceiver tmpBR = getBroadcastReceiver();
            this.mStateChangeReceiver = tmpBR;
            HwLog.i("WaterMarkUtils", "listenForStateChange  ");
            GlobalContext.getContext().registerReceiver(tmpBR, new IntentFilter("android.intent.action.SERVICE_STATE"));
        }
    }

    private int getMarkNumb() {
        int markNumber = getMarkNumbFromDevice();
        if (markNumber < 0) {
            listenForStateChange();
            markNumber = getMarkNumbFromPref();
            HwLog.i("WaterMarkUtils", "getMarkNumb from pref.return " + markNumber);
            return markNumber;
        } else if (markNumber == this.mWaterMarkNumber) {
            return markNumber;
        } else {
            storeMarkNumbInPref(this.mWaterMarkNumber);
            return markNumber;
        }
    }

    private int getMarkNumbFromPref() {
        return 1513916325 ^ PreferenceManager.getDefaultSharedPreferences(GlobalContext.getContext()).getInt("mark_number", -1513916326);
    }

    private void storeMarkNumbInPref(int val) {
        PreferenceManager.getDefaultSharedPreferences(GlobalContext.getContext()).edit().putInt("mark_number", 1513916325 ^ val).apply();
    }

    private int getMarkNumbFromDevice() {
        try {
            return HwSecureWaterMark.getWatermarkNumber();
        } catch (Exception e) {
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.getMarkNumb fail", e);
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.getMarkNumb fail");
            return -1;
        } catch (NoClassDefFoundError e2) {
            sWaterMarkSupportState = 0;
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.getMarkNumb not support", e2);
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.getMarkNumb fail");
            return -1;
        }
    }

    private static int getWaterMarkSupportState() {
        int i = 0;
        if (sWaterMarkSupportState == -1) {
            boolean support = false;
            try {
                support = HwSecureWaterMark.isWatermarkEnable();
            } catch (Exception e) {
                HwLog.e("WaterMarkUtils", "HwSecureWaterMark.isWatermarkEnable fail", e);
            } catch (NoClassDefFoundError e2) {
                sWaterMarkSupportState = 0;
                HwLog.e("WaterMarkUtils", "HwSecureWaterMark.isWatermarkEnable not support", e2);
            } catch (UnsatisfiedLinkError e3) {
                sWaterMarkSupportState = 0;
                HwLog.e("WaterMarkUtils", "HwSecureWaterMark.addWatermark UnsatisfiedLinkError", e3);
            }
            if (support) {
                i = 1;
            }
            sWaterMarkSupportState = i;
        }
        return sWaterMarkSupportState;
    }

    public static Drawable addWaterMark(Context context, Drawable drawable) {
        if (getWaterMarkSupportState() != 1 || sInst.getMarkNumb() <= 0) {
            return drawable;
        }
        if (drawable instanceof BitmapDrawable) {
            return addWaterMarkInner(context, (BitmapDrawable) drawable);
        }
        if (drawable instanceof ColorDrawable) {
            return addWaterMarkInner(context, (ColorDrawable) drawable);
        }
        return drawable;
    }

    private static Drawable addWaterMarkInner(Context context, BitmapDrawable drawable) {
        HwLog.w("WaterMarkUtils", "add watermark for bitmap");
        return new BitmapDrawable(context.getResources(), addWaterMark(drawable.getBitmap(), false));
    }

    private static Drawable addWaterMarkInner(Context context, ColorDrawable drawable) {
        HwLog.w("WaterMarkUtils", "add watermark for color");
        Point screenSize = HwUnlockUtils.getPoint(context);
        Bitmap bmp = Bitmap.createBitmap(screenSize.x, screenSize.y, Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, screenSize.x, screenSize.y);
        drawable.draw(canvas);
        return new BitmapDrawable(context.getResources(), addWaterMark(bmp));
    }

    public static Bitmap addWaterMark(Bitmap inputBitmap) {
        return addWaterMark(inputBitmap, true);
    }

    public static Bitmap addWaterMark(Bitmap inputBitmap, boolean autoRecycle) {
        Bitmap newBitmap = null;
        if (getWaterMarkSupportState() != 1 || sInst.getMarkNumb() <= 0) {
            return inputBitmap;
        }
        try {
            long time = System.currentTimeMillis();
            int markNo = sInst.getMarkNumb();
            if (inputBitmap != null) {
                newBitmap = HwSecureWaterMark.addWatermark(inputBitmap, markNo);
            }
            if (newBitmap != null) {
                HwLog.d("WaterMarkUtils", "addWatermark with " + markNo + " consume:" + (System.currentTimeMillis() - time) + "; size:" + newBitmap.getWidth() + newBitmap.getHeight());
                if (autoRecycle && newBitmap != inputBitmap) {
                    inputBitmap.recycle();
                }
                return newBitmap;
            }
        } catch (Exception e) {
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.addWatermark fail", e);
        } catch (NoClassDefFoundError e2) {
            sWaterMarkSupportState = 0;
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.addWatermark not support", e2);
        } catch (UnsatisfiedLinkError e3) {
            sWaterMarkSupportState = 0;
            HwLog.e("WaterMarkUtils", "HwSecureWaterMark.addWatermark UnsatisfiedLinkError", e3);
        }
        HwLog.d("WaterMarkUtils", "HwSecureWaterMark.addWatermark fail");
        return inputBitmap;
    }
}
