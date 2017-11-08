package com.android.systemui.doze;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.TimeUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DozeLog {
    private static final boolean DEBUG = Log.isLoggable("DozeLog", 3);
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final int SIZE = (Build.IS_DEBUGGABLE ? 400 : 50);
    private static int sCount;
    private static SummaryStats sEmergencyCallStats;
    private static final KeyguardUpdateMonitorCallback sKeyguardCallback = new KeyguardUpdateMonitorCallback() {
        public void onEmergencyCallAction() {
            DozeLog.traceEmergencyCall();
        }

        public void onKeyguardBouncerChanged(boolean bouncer) {
            DozeLog.traceKeyguardBouncerChanged(bouncer);
        }

        public void onStartedWakingUp() {
            DozeLog.traceScreenOn();
        }

        public void onFinishedGoingToSleep(int why) {
            DozeLog.traceScreenOff(why);
        }

        public void onKeyguardVisibilityChanged(boolean showing) {
            DozeLog.traceKeyguard(showing);
        }
    };
    private static String[] sMessages;
    private static SummaryStats sNotificationPulseStats;
    private static SummaryStats sPickupPulseNearVibrationStats;
    private static SummaryStats sPickupPulseNotNearVibrationStats;
    private static int sPosition;
    private static SummaryStats[][] sProxStats;
    private static boolean sPulsing;
    private static SummaryStats sScreenOnNotPulsingStats;
    private static SummaryStats sScreenOnPulsingStats;
    private static long sSince;
    private static long[] sTimes;

    private static class SummaryStats {
        private int mCount;

        private SummaryStats() {
        }

        public void append() {
            this.mCount++;
        }

        public void dump(PrintWriter pw, String type) {
            if (this.mCount != 0) {
                pw.print("    ");
                pw.print(type);
                pw.print(": n=");
                pw.print(this.mCount);
                pw.print(" (");
                pw.print((((((double) this.mCount) / ((double) (System.currentTimeMillis() - DozeLog.sSince))) * 1000.0d) * 60.0d) * 60.0d);
                pw.print("/hr)");
                pw.println();
            }
        }
    }

    public static void tracePickupPulse(boolean withinVibrationThreshold) {
        SummaryStats summaryStats;
        log("pickupPulse withinVibrationThreshold=" + withinVibrationThreshold);
        if (withinVibrationThreshold) {
            summaryStats = sPickupPulseNearVibrationStats;
        } else {
            summaryStats = sPickupPulseNotNearVibrationStats;
        }
        summaryStats.append();
    }

    public static void tracePulseStart(int reason) {
        sPulsing = true;
        log("pulseStart reason=" + pulseReasonToString(reason));
    }

    public static void tracePulseFinish() {
        sPulsing = false;
        log("pulseFinish");
    }

    public static void traceNotificationPulse(long instance) {
        log("notificationPulse instance=" + instance);
        sNotificationPulseStats.append();
    }

    private static void init(Context context) {
        synchronized (DozeLog.class) {
            if (sMessages == null) {
                sTimes = new long[SIZE];
                sMessages = new String[SIZE];
                sSince = System.currentTimeMillis();
                sPickupPulseNearVibrationStats = new SummaryStats();
                sPickupPulseNotNearVibrationStats = new SummaryStats();
                sNotificationPulseStats = new SummaryStats();
                sScreenOnPulsingStats = new SummaryStats();
                sScreenOnNotPulsingStats = new SummaryStats();
                sEmergencyCallStats = new SummaryStats();
                sProxStats = (SummaryStats[][]) Array.newInstance(SummaryStats.class, new int[]{4, 2});
                for (int i = 0; i < 4; i++) {
                    sProxStats[i][0] = new SummaryStats();
                    sProxStats[i][1] = new SummaryStats();
                }
                log("init");
                KeyguardUpdateMonitor.getInstance(context).registerCallback(sKeyguardCallback);
            }
        }
    }

    public static void traceDozing(Context context, boolean dozing) {
        sPulsing = false;
        init(context);
        log("dozing " + dozing);
    }

    public static void traceFling(boolean expand, boolean aboveThreshold, boolean thresholdNeeded, boolean screenOnFromTouch) {
        log("fling expand=" + expand + " aboveThreshold=" + aboveThreshold + " thresholdNeeded=" + thresholdNeeded + " screenOnFromTouch=" + screenOnFromTouch);
    }

    public static void traceEmergencyCall() {
        log("emergencyCall");
        sEmergencyCallStats.append();
    }

    public static void traceKeyguardBouncerChanged(boolean showing) {
        log("bouncer " + showing);
    }

    public static void traceScreenOn() {
        log("screenOn pulsing=" + sPulsing);
        (sPulsing ? sScreenOnPulsingStats : sScreenOnNotPulsingStats).append();
        sPulsing = false;
    }

    public static void traceScreenOff(int why) {
        log("screenOff why=" + why);
    }

    public static void traceKeyguard(boolean showing) {
        log("keyguard " + showing);
        if (!showing) {
            sPulsing = false;
        }
    }

    public static void traceProximityResult(Context context, boolean near, long millis, int pulseReason) {
        int i;
        log("proximityResult reason=" + pulseReasonToString(pulseReason) + " near=" + near + " millis=" + millis);
        init(context);
        SummaryStats[] summaryStatsArr = sProxStats[pulseReason];
        if (near) {
            i = 0;
        } else {
            i = 1;
        }
        summaryStatsArr[i].append();
    }

    public static String pulseReasonToString(int pulseReason) {
        switch (pulseReason) {
            case 0:
                return "intent";
            case 1:
                return "notification";
            case 2:
                return "sigmotion";
            case 3:
                return "pickup";
            default:
                throw new IllegalArgumentException("bad reason: " + pulseReason);
        }
    }

    public static void dump(PrintWriter pw) {
        synchronized (DozeLog.class) {
            if (sMessages == null) {
                return;
            }
            int i;
            pw.println("  Doze log:");
            int start = ((sPosition - sCount) + SIZE) % SIZE;
            for (i = 0; i < sCount; i++) {
                int j = (start + i) % SIZE;
                pw.print("    ");
                pw.print(FORMAT.format(new Date(sTimes[j])));
                pw.print(' ');
                pw.println(sMessages[j]);
            }
            pw.print("  Doze summary stats (for ");
            TimeUtils.formatDuration(System.currentTimeMillis() - sSince, pw);
            pw.println("):");
            sPickupPulseNearVibrationStats.dump(pw, "Pickup pulse (near vibration)");
            sPickupPulseNotNearVibrationStats.dump(pw, "Pickup pulse (not near vibration)");
            sNotificationPulseStats.dump(pw, "Notification pulse");
            sScreenOnPulsingStats.dump(pw, "Screen on (pulsing)");
            sScreenOnNotPulsingStats.dump(pw, "Screen on (not pulsing)");
            sEmergencyCallStats.dump(pw, "Emergency call");
            for (i = 0; i < 4; i++) {
                String reason = pulseReasonToString(i);
                sProxStats[i][0].dump(pw, "Proximity near (" + reason + ")");
                sProxStats[i][1].dump(pw, "Proximity far (" + reason + ")");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void log(String msg) {
        synchronized (DozeLog.class) {
            if (sMessages == null) {
                return;
            }
            sTimes[sPosition] = System.currentTimeMillis();
            sMessages[sPosition] = msg;
            sPosition = (sPosition + 1) % SIZE;
            sCount = Math.min(sCount + 1, SIZE);
        }
    }
}
