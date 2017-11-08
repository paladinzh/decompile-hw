package com.android.server.location.ntp;

import android.os.SystemClock;
import android.util.Log;

public class TimeManager {
    private static boolean DBG = true;
    private static final long INVAILID_TIME = 0;
    private long mExpireTime;
    private String mTag;
    private long mTimeSynsBoot;
    private long mTimestamp;

    public TimeManager(String tag, long expireTime) {
        this.mExpireTime = expireTime;
        this.mTag = tag;
    }

    public long getCurrentTime() {
        if (this.mTimestamp == 0) {
            return 0;
        }
        if (ElapsedRealTimeCheck.getInstance().canTrustElapsedRealTime()) {
            long timeTillNow = SystemClock.elapsedRealtime() - this.mTimeSynsBoot;
            if (timeTillNow >= this.mExpireTime) {
                if (DBG) {
                    Log.d(this.mTag, "getCurrentTime INVAILID_TIME");
                }
                return 0;
            }
            if (DBG) {
                Log.d(this.mTag, "getCurrentTime:" + (this.mTimestamp + timeTillNow));
            }
            return this.mTimestamp + timeTillNow;
        }
        if (DBG) {
            Log.d(this.mTag, "getCurrentTime ElapsedRealTime INVAILID_TIME");
        }
        return 0;
    }

    public void setCurrentTime(long msTime, long msTimeSynsBoot) {
        this.mTimestamp = msTime;
        this.mTimeSynsBoot = msTimeSynsBoot;
        if (DBG) {
            Log.d(this.mTag, "setCurrentTime mTimestamp:" + this.mTimestamp + " mTimeReference:" + this.mTimeSynsBoot);
        }
    }

    public long getmTimestamp() {
        return this.mTimestamp;
    }
}
