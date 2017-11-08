package com.android.deskclock.smartcover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;
import android.widget.ImageView;
import com.android.deskclock.R;
import com.android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DigitalClockAdapter {
    private static int mHour = 0;
    private static int mMinite = 0;
    private static int mPriHour = 0;
    private static int mPriMinite = 0;
    private Context mContext;
    private List<ImageView> mDigitalTimeImageViewList = new ArrayList();
    private ContentObserver mFormatChangeObserver;
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e("DigitalClockAdapter", "onReceive, the intent is null!");
            } else {
                DigitalClockAdapter.this.onTimeChanged();
            }
        }
    };
    private int[] mNumDrawableSrcArray = new int[]{R.drawable.number0, R.drawable.number1, R.drawable.number2, R.drawable.number3, R.drawable.number4, R.drawable.number5, R.drawable.number6, R.drawable.number7, R.drawable.number8, R.drawable.number9};

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            DigitalClockAdapter.this.onTimeChanged();
        }
    }

    public DigitalClockAdapter(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDigitalTimeViewList(List<ImageView> digitalTimeImageViewList) {
        if (!(digitalTimeImageViewList == null || digitalTimeImageViewList.size() == 0 || this.mDigitalTimeImageViewList == null)) {
            this.mDigitalTimeImageViewList.clear();
            this.mDigitalTimeImageViewList.addAll(digitalTimeImageViewList);
        }
    }

    public void registerContentObserver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        }
        this.mFormatChangeObserver = new FormatChangeObserver();
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver);
        }
        onTimeChanged();
    }

    public void unregisterContentObserver() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        if (this.mContext == null) {
            Log.e("DigitalClockAdapter", "mContext is null!");
            return;
        }
        if (this.mIntentReceiver != null) {
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mFormatChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mFormatChangeObserver);
        }
    }

    private void onTimeChanged() {
        setDigtalTimeView();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setDigtalTimeView() {
        if (this.mDigitalTimeImageViewList != null && this.mDigitalTimeImageViewList.size() != 0 && this.mNumDrawableSrcArray != null && this.mNumDrawableSrcArray.length != 0) {
            String timeValue = Util.getTime(this.mContext);
            if (timeValue != null && !timeValue.trim().isEmpty()) {
                timeValue = timeValue.replace(":", "");
                if (timeValue.length() < 4) {
                    timeValue = Util.padLeftZero(timeValue, 4 - timeValue.length());
                }
                Util.setNumImageViewByValue(timeValue, this.mDigitalTimeImageViewList);
            }
        }
    }

    public static void setHour(int hour) {
        mHour = hour;
    }

    public static void setPriHour(int priHour) {
        mPriHour = priHour;
    }

    public static void setMinite(int minite) {
        mMinite = minite;
    }

    public static void setPriMinite(int priMinite) {
        mPriMinite = priMinite;
    }

    public static int getHour() {
        return mHour;
    }

    public static int getPriHour() {
        return mPriHour;
    }

    public static int getMinite() {
        return mMinite;
    }

    public static int getPriMinite() {
        return mPriMinite;
    }
}
