package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryStats;
import android.os.BatteryStats.HistoryItem;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.SparseIntArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.graph.UsageView;

public class BatteryInfo {
    public String batteryPercentString;
    public int mBatteryLevel;
    public String mChargeLabelString;
    private boolean mCharging;
    public boolean mDischarging = true;
    private BatteryStats mStats;
    public String remainingLabel;
    public long remainingTimeUs = 0;
    private long timePeriod;

    public interface BatteryDataParser {
        void onDataGap();

        void onDataPoint(long j, HistoryItem historyItem);

        void onParsingDone();

        void onParsingStarted(long j, long j2);
    }

    public interface Callback {
        void onBatteryInfoLoaded(BatteryInfo batteryInfo);
    }

    public void bindHistory(final UsageView view, BatteryDataParser... parsers) {
        BatteryDataParser parser = new BatteryDataParser() {
            SparseIntArray points = new SparseIntArray();

            public void onParsingStarted(long startTime, long endTime) {
                BatteryInfo.this.timePeriod = (endTime - startTime) - (BatteryInfo.this.remainingTimeUs / 1000);
                view.clearPaths();
                view.configureGraph((int) (endTime - startTime), 100, BatteryInfo.this.remainingTimeUs != 0, BatteryInfo.this.mCharging);
            }

            public void onDataPoint(long time, HistoryItem record) {
                this.points.put((int) time, record.batteryLevel);
            }

            public void onDataGap() {
                if (this.points.size() > 1) {
                    view.addPath(this.points);
                }
                this.points.clear();
            }

            public void onParsingDone() {
                if (this.points.size() > 1) {
                    view.addPath(this.points);
                }
            }
        };
        BatteryDataParser[] parserList = new BatteryDataParser[(parsers.length + 1)];
        for (int i = 0; i < parsers.length; i++) {
            parserList[i] = parsers[i];
        }
        parserList[parsers.length] = parser;
        parse(this.mStats, this.remainingTimeUs, parserList);
        Context context = view.getContext();
        String timeString = context.getString(R$string.charge_length_format, new Object[]{Formatter.formatShortElapsedTime(context, this.timePeriod)});
        String remaining = "";
        if (this.remainingTimeUs != 0) {
            remaining = context.getString(R$string.remaining_length_format, new Object[]{Formatter.formatShortElapsedTime(context, this.remainingTimeUs / 1000)});
        }
        view.setBottomLabels(new CharSequence[]{timeString, remaining});
    }

    public static void getBatteryInfo(Context context, Callback callback) {
        getBatteryInfo(context, callback, false);
    }

    public static void getBatteryInfo(final Context context, final Callback callback, final boolean shortString) {
        new AsyncTask<Void, Void, BatteryStats>() {
            protected BatteryStats doInBackground(Void... params) {
                BatteryStatsHelper statsHelper = new BatteryStatsHelper(context, true);
                statsHelper.create((Bundle) null);
                return statsHelper.getStats();
            }

            protected void onPostExecute(BatteryStats batteryStats) {
                long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;
                callback.onBatteryInfoLoaded(BatteryInfo.getBatteryInfo(context, context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")), batteryStats, elapsedRealtimeUs, shortString));
            }
        }.execute(new Void[0]);
    }

    public static BatteryInfo getBatteryInfo(Context context, Intent batteryBroadcast, BatteryStats stats, long elapsedRealtimeUs) {
        return getBatteryInfo(context, batteryBroadcast, stats, elapsedRealtimeUs, false);
    }

    public static BatteryInfo getBatteryInfo(Context context, Intent batteryBroadcast, BatteryStats stats, long elapsedRealtimeUs, boolean shortString) {
        BatteryInfo info = new BatteryInfo();
        info.mStats = stats;
        info.mBatteryLevel = Utils.getBatteryLevel(batteryBroadcast);
        info.batteryPercentString = Utils.formatPercentage(info.mBatteryLevel);
        info.mCharging = batteryBroadcast.getIntExtra("plugged", 0) != 0;
        Resources resources = context.getResources();
        String timeString;
        if (info.mCharging) {
            long chargeTime = stats.computeChargeTimeRemaining(elapsedRealtimeUs);
            String statusLabel = Utils.getBatteryStatus(resources, batteryBroadcast, shortString);
            int status = batteryBroadcast.getIntExtra("status", 1);
            if (chargeTime <= 0 || status == 5) {
                info.remainingLabel = statusLabel;
                info.mChargeLabelString = resources.getString(R$string.power_charging, new Object[]{info.batteryPercentString, statusLabel});
            } else {
                int resId;
                info.mDischarging = false;
                info.remainingTimeUs = chargeTime;
                timeString = Formatter.formatShortElapsedTime(context, chargeTime / 1000);
                int plugType = batteryBroadcast.getIntExtra("plugged", 0);
                if (plugType == 1) {
                    if (shortString) {
                        resId = R$string.power_charging_duration_ac_short;
                    } else {
                        resId = R$string.power_charging_duration_ac;
                    }
                } else if (plugType == 2) {
                    if (shortString) {
                        resId = R$string.power_charging_duration_usb_short;
                    } else {
                        resId = R$string.power_charging_duration_usb;
                    }
                } else if (plugType == 4) {
                    if (shortString) {
                        resId = R$string.power_charging_duration_wireless_short;
                    } else {
                        resId = R$string.power_charging_duration_wireless;
                    }
                } else if (shortString) {
                    resId = R$string.power_charging_duration_short;
                } else {
                    resId = R$string.power_charging_duration;
                }
                info.remainingLabel = resources.getString(R$string.power_remaining_duration_only, new Object[]{timeString});
                info.mChargeLabelString = resources.getString(resId, new Object[]{info.batteryPercentString, timeString});
            }
        } else {
            long drainTime = stats.computeBatteryTimeRemaining(elapsedRealtimeUs);
            if (drainTime > 0) {
                int i;
                info.remainingTimeUs = drainTime;
                timeString = Formatter.formatShortElapsedTime(context, drainTime / 1000);
                if (shortString) {
                    i = R$string.power_remaining_duration_only_short;
                } else {
                    i = R$string.power_remaining_duration_only;
                }
                info.remainingLabel = resources.getString(i, new Object[]{timeString});
                if (shortString) {
                    i = R$string.power_discharging_duration_short;
                } else {
                    i = R$string.power_discharging_duration;
                }
                info.mChargeLabelString = resources.getString(i, new Object[]{info.batteryPercentString, timeString});
            } else {
                info.remainingLabel = null;
                info.mChargeLabelString = info.batteryPercentString;
            }
        }
        return info;
    }

    private static void parse(BatteryStats stats, long remainingTimeUs, BatteryDataParser... parsers) {
        HistoryItem rec;
        long startWalltime = 0;
        long historyStart = 0;
        long historyEnd = 0;
        byte lastLevel = (byte) -1;
        long curWalltime = 0;
        long lastWallTime = 0;
        long lastRealtime = 0;
        int lastInteresting = 0;
        int pos = 0;
        boolean first = true;
        if (stats.startIteratingHistoryLocked()) {
            rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec)) {
                pos++;
                if (first) {
                    first = false;
                    historyStart = rec.time;
                }
                if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                    if (rec.currentTime > 15552000000L + lastWallTime || rec.time < 300000 + historyStart) {
                        startWalltime = 0;
                    }
                    lastWallTime = rec.currentTime;
                    lastRealtime = rec.time;
                    if (startWalltime == 0) {
                        startWalltime = lastWallTime - (lastRealtime - historyStart);
                    }
                }
                if (rec.isDeltaData()) {
                    if (rec.batteryLevel != lastLevel || pos == 1) {
                        lastLevel = rec.batteryLevel;
                    }
                    lastInteresting = pos;
                    historyEnd = rec.time;
                }
            }
        }
        stats.finishIteratingHistoryLocked();
        long endDateWalltime = (lastWallTime + historyEnd) - lastRealtime;
        long endWalltime = endDateWalltime + (remainingTimeUs / 1000);
        int i = 0;
        int N = lastInteresting;
        for (BatteryDataParser onParsingStarted : parsers) {
            onParsingStarted.onParsingStarted(startWalltime, endWalltime);
        }
        if (endDateWalltime > startWalltime && stats.startIteratingHistoryLocked()) {
            rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec) && i < N) {
                if (rec.isDeltaData()) {
                    curWalltime += rec.time - lastRealtime;
                    lastRealtime = rec.time;
                    long x = curWalltime - startWalltime;
                    if (x < 0) {
                        x = 0;
                    }
                    for (BatteryDataParser onParsingStarted2 : parsers) {
                        onParsingStarted2.onDataPoint(x, rec);
                    }
                } else {
                    long lastWalltime = curWalltime;
                    if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                        if (rec.currentTime >= startWalltime) {
                            curWalltime = rec.currentTime;
                        } else {
                            curWalltime = startWalltime + (rec.time - historyStart);
                        }
                        lastRealtime = rec.time;
                    }
                    if (rec.cmd != (byte) 6 && (rec.cmd != (byte) 5 || Math.abs(lastWalltime - curWalltime) > 3600000)) {
                        for (BatteryDataParser onParsingStarted22 : parsers) {
                            onParsingStarted22.onDataGap();
                        }
                    }
                }
                i++;
            }
        }
        stats.finishIteratingHistoryLocked();
        for (BatteryDataParser onParsingStarted222 : parsers) {
            onParsingStarted222.onParsingDone();
        }
    }
}
