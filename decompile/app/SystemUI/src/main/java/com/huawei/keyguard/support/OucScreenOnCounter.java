package com.huawei.keyguard.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.util.HwLog;
import java.util.Calendar;

public class OucScreenOnCounter {
    private static int SECONDS_ONE_DAY = (SECONDS_ONE_HOUR * 24);
    private static int SECONDS_ONE_HOUR = 3600;
    private static int SECONDS_ONE_WEEK = (SECONDS_ONE_DAY * 7);
    private static final OucScreenOnCounter inst = new OucScreenOnCounter();
    private Context mContext = null;
    private final Uri mCounteUri = Uri.parse("content://com.android.huawei.magazineunlock/counter");
    private final String[] mDataProjections = new String[]{"type", "value", "value_2"};
    private long mLastScreenOnTime = 0;
    private volatile Runnable mSchedualdRunner = null;
    private final OucTrigger mScreenOffTrigger = new OucTrigger();
    private ContentValues mValues = new ContentValues();

    private class OucTrigger implements Runnable {
        private long mScreenOffTime;
        private long mWakeupTime;

        private OucTrigger() {
        }

        public void run() {
            if (OucScreenOnCounter.this.mContext != null) {
                long screenOffTime;
                long wakeupTime;
                synchronized (this) {
                    screenOffTime = this.mScreenOffTime;
                    wakeupTime = this.mWakeupTime;
                }
                if (screenOffTime == 0 || screenOffTime < wakeupTime) {
                    HwLog.e("OucScreenOnCounter", "Skip set as ScreenOnTime is invalide");
                    return;
                }
                OucScreenOnCounter.this.mValues.put("type", Integer.valueOf(1));
                OucScreenOnCounter.this.mValues.put("value", Long.valueOf(screenOffTime));
                OucScreenOnCounter.this.mValues.put("value_2", Long.valueOf(screenOffTime - wakeupTime));
                try {
                    OucScreenOnCounter.this.mContext.getContentResolver().insert(OucScreenOnCounter.this.mCounteUri, OucScreenOnCounter.this.mValues);
                } catch (SQLiteException e) {
                    HwLog.e("OucScreenOnCounter", "Insert Ouc data fail. " + wakeupTime + " " + screenOffTime, e);
                } catch (Exception e2) {
                    HwLog.e("OucScreenOnCounter", "Insert Ouc data fail. " + wakeupTime + " " + screenOffTime, e2);
                }
            }
        }

        private synchronized OucTrigger setTime(long wakupTime, long screenoffTime) {
            this.mWakeupTime = wakupTime;
            this.mScreenOffTime = screenoffTime;
            return this;
        }
    }

    private static class TimeCounter {
        private byte[] mBytes;
        private int mIdxLastHour;
        private long mNowTime;
        private int mOffsetIdx;
        private long mOnTimeLastHour;

        private TimeCounter() {
            this.mOnTimeLastHour = 0;
            this.mIdxLastHour = -1;
            this.mOffsetIdx = 0;
            this.mBytes = new byte[168];
            for (int idx = 0; idx < this.mBytes.length; idx++) {
                this.mBytes[idx] = (byte) 0;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(12, 0);
            cal.set(13, 0);
            cal.set(14, 0);
            this.mNowTime = cal.getTimeInMillis() / 1000;
            int hour = cal.get(11);
            int weekDay = (cal.get(7) + 5) % 7;
            this.mOffsetIdx = ((weekDay * 24) + hour) - 2;
            HwLog.i("OucScreenOnCounter", "TimeCounter init with Day:" + weekDay + "; Hour " + hour + "; offset " + this.mOffsetIdx);
        }

        private long getNowTime() {
            return this.mNowTime;
        }

        private void addRecord(long offTime, long duration) {
            int idx = (int) ((this.mNowTime - offTime) / 3600);
            HwLog.d("OucScreenOnCounter", "addRecord Now: IDX: " + this.mIdxLastHour + " - " + idx + " DURATION: " + duration);
            if (this.mIdxLastHour != idx) {
                writeBackData();
                this.mIdxLastHour = idx;
            }
            this.mOnTimeLastHour += duration;
            if (this.mOnTimeLastHour > 3600) {
                writeBackData();
                this.mIdxLastHour = -1;
            }
        }

        private void writeBackData() {
            if (this.mOnTimeLastHour <= 0 || this.mIdxLastHour < 0 || this.mBytes.length < this.mIdxLastHour) {
                this.mOnTimeLastHour = 0;
                return;
            }
            if (this.mIdxLastHour > 168 - ((24 - (this.mOffsetIdx % 24)) % 24)) {
                HwLog.w("OucScreenOnCounter", "skip : " + this.mIdxLastHour);
            }
            int idx = ((168 - this.mIdxLastHour) + this.mOffsetIdx) % 168;
            this.mBytes[idx] = OucScreenOnCounter.calculateVal(this.mOnTimeLastHour);
            this.mOnTimeLastHour = 0;
            HwLog.d("OucScreenOnCounter", "writeBackData [" + idx + "] = " + this.mBytes[idx]);
        }

        public byte[] getBytes() {
            return this.mBytes;
        }
    }

    public static OucScreenOnCounter getInst(Context context) {
        return inst.setContext(context);
    }

    private OucScreenOnCounter() {
        this.mValues.put("name", "OucScCnt");
    }

    private OucScreenOnCounter setContext(Context context) {
        if (this.mContext == null && context != null) {
            this.mContext = context.getApplicationContext();
        }
        return this;
    }

    public void trigger(int type) {
        long timeNow = System.currentTimeMillis() / 1000;
        if (type == 1) {
            if (this.mLastScreenOnTime == 0) {
                this.mLastScreenOnTime = timeNow;
            } else {
                HwLog.i("OucScreenOnCounter", "Screen already turned on at: " + this.mLastScreenOnTime);
            }
        } else if (type != 2) {
        } else {
            if (this.mLastScreenOnTime == 0) {
                HwLog.i("OucScreenOnCounter", "Screen not turned on : " + timeNow);
                return;
            }
            GlobalContext.getBackgroundHandler().removeCallbacks(this.mScreenOffTrigger);
            this.mScreenOffTrigger.setTime(this.mLastScreenOnTime, timeNow);
            GlobalContext.getBackgroundHandler().postDelayed(this.mScreenOffTrigger, 2000);
            this.mLastScreenOnTime = 0;
        }
    }

    public byte[] getUserRecords() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(this.mCounteUri, this.mDataProjections, "(name = 'OucScCnt' ) and (value > " + ((System.currentTimeMillis() / 1000) - (((long) SECONDS_ONE_DAY) * 8)) + ")", null, "value desc");
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                HwLog.i("OucScreenOnCounter", "OucTrigger getUserRecords fail.");
                return getDefaultErrorRet();
            }
            byte[] countData = countData(cursor);
            if (cursor != null) {
                cursor.close();
            }
            return countData;
        } catch (SQLiteException e) {
            HwLog.e("OucScreenOnCounter", "getUserRecords fail. ", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            HwLog.w("OucScreenOnCounter", "getUserRecords fail. ", e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private byte[] getDefaultErrorRet() {
        byte[] ret = new byte[168];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) -1;
        }
        return ret;
    }

    private static byte calculateVal(long time) {
        if (time <= 0) {
            return (byte) 0;
        }
        if (time < 600) {
            return (byte) 1;
        }
        if (time < 1800) {
            return (byte) 2;
        }
        if (time < 3600) {
            return (byte) 3;
        }
        if (time < 7200) {
            return (byte) 4;
        }
        if (time < 10800) {
            return (byte) 5;
        }
        if (time < 14400) {
            return (byte) 6;
        }
        if (time < 18000) {
            return (byte) 7;
        }
        if (time < 21600) {
            return (byte) 8;
        }
        if (time < 25200) {
            return (byte) 9;
        }
        return (byte) 10;
    }

    private byte[] countData(Cursor cursor) {
        TimeCounter counter = new TimeCounter();
        long nowRefNode = counter.getNowTime();
        long endRefNode = nowRefNode - ((long) SECONDS_ONE_WEEK);
        do {
            long time = (long) cursor.getInt(1);
            long duration = (long) cursor.getInt(2);
            if (time <= nowRefNode) {
                if (time < endRefNode) {
                    break;
                }
                counter.addRecord(time, duration);
            }
        } while (cursor.moveToNext());
        counter.writeBackData();
        cleanOldData();
        HwLog.d("OucScreenOnCounter", "countData succ: ");
        return counter.getBytes();
    }

    public void cleanOldData() {
        if (this.mSchedualdRunner != null) {
            HwLog.e("OucScreenOnCounter", "clean OldData skiped as already exists;");
            return;
        }
        this.mSchedualdRunner = new Runnable() {
            public void run() {
                if (OucScreenOnCounter.this.mContext == null) {
                    HwLog.e("OucScreenOnCounter", "cleanOldData skiped as no context;");
                    OucScreenOnCounter.this.mSchedualdRunner = null;
                    return;
                }
                try {
                    HwLog.w("OucScreenOnCounter", "clean old data: " + OucScreenOnCounter.this.mContext.getContentResolver().delete(OucScreenOnCounter.this.mCounteUri, "(name = 'OucScCnt' ) and (value < " + ((System.currentTimeMillis() / 1000) - (((long) OucScreenOnCounter.SECONDS_ONE_DAY) * 7)) + ")", null));
                } catch (Exception e) {
                    HwLog.w("OucScreenOnCounter", "getUserRecords fail. ", e);
                }
                OucScreenOnCounter.this.mSchedualdRunner = null;
            }
        };
        GlobalContext.getBackgroundHandler().postDelayed(this.mSchedualdRunner, 30000);
    }
}
