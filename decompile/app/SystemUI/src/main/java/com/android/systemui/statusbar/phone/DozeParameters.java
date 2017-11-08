package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import com.android.systemui.R;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DozeParameters {
    private static final boolean DEBUG = Log.isLoggable("DozeParameters", 3);
    private static PulseSchedule sPulseSchedule;
    private final Context mContext;

    public static class PulseSchedule {
        private static final Pattern PATTERN = Pattern.compile("(\\d+?)s", 0);
        private int[] mSchedule;
        private String mSpec;

        public static PulseSchedule parse(String spec) {
            if (TextUtils.isEmpty(spec)) {
                return null;
            }
            try {
                PulseSchedule rt = new PulseSchedule();
                rt.mSpec = spec;
                String[] tokens = spec.split(",");
                rt.mSchedule = new int[tokens.length];
                int i = 0;
                while (i < tokens.length) {
                    Matcher m = PATTERN.matcher(tokens[i]);
                    if (m.matches()) {
                        rt.mSchedule[i] = Integer.parseInt(m.group(1));
                        i++;
                    } else {
                        throw new IllegalArgumentException("Bad token: " + tokens[i]);
                    }
                }
                if (DozeParameters.DEBUG) {
                    Log.d("DozeParameters", "Parsed spec [" + spec + "] as: " + rt);
                }
                return rt;
            } catch (RuntimeException e) {
                Log.w("DozeParameters", "Error parsing spec: " + spec, e);
                return null;
            }
        }

        public String toString() {
            return Arrays.toString(this.mSchedule);
        }

        public long getNextTime(long now, long notificationTime) {
            for (int i : this.mSchedule) {
                long time = notificationTime + ((long) (i * 1000));
                if (time > now) {
                    return time;
                }
            }
            return 0;
        }
    }

    public DozeParameters(Context context) {
        this.mContext = context;
    }

    public void dump(PrintWriter pw) {
        pw.println("  DozeParameters:");
        pw.print("    getDisplayStateSupported(): ");
        pw.println(getDisplayStateSupported());
        pw.print("    getPulseDuration(pickup=false): ");
        pw.println(getPulseDuration(false));
        pw.print("    getPulseDuration(pickup=true): ");
        pw.println(getPulseDuration(true));
        pw.print("    getPulseInDuration(pickup=false): ");
        pw.println(getPulseInDuration(false));
        pw.print("    getPulseInDuration(pickup=true): ");
        pw.println(getPulseInDuration(true));
        pw.print("    getPulseInVisibleDuration(): ");
        pw.println(getPulseVisibleDuration());
        pw.print("    getPulseOutDuration(): ");
        pw.println(getPulseOutDuration());
        pw.print("    getPulseOnSigMotion(): ");
        pw.println(getPulseOnSigMotion());
        pw.print("    getVibrateOnSigMotion(): ");
        pw.println(getVibrateOnSigMotion());
        pw.print("    getPulseOnPickup(): ");
        pw.println(getPulseOnPickup());
        pw.print("    getVibrateOnPickup(): ");
        pw.println(getVibrateOnPickup());
        pw.print("    getProxCheckBeforePulse(): ");
        pw.println(getProxCheckBeforePulse());
        pw.print("    getPulseOnNotifications(): ");
        pw.println(getPulseOnNotifications());
        pw.print("    getPulseSchedule(): ");
        pw.println(getPulseSchedule());
        pw.print("    getPulseScheduleResets(): ");
        pw.println(getPulseScheduleResets());
        pw.print("    getPickupVibrationThreshold(): ");
        pw.println(getPickupVibrationThreshold());
        pw.print("    getPickupPerformsProxCheck(): ");
        pw.println(getPickupPerformsProxCheck());
    }

    public boolean getDisplayStateSupported() {
        return getBoolean("doze.display.supported", R.bool.doze_display_state_supported);
    }

    public int getPulseDuration(boolean pickup) {
        return (getPulseInDuration(pickup) + getPulseVisibleDuration()) + getPulseOutDuration();
    }

    public int getPulseInDuration(boolean pickup) {
        if (pickup) {
            return getInt("doze.pulse.duration.in.pickup", R.integer.doze_pulse_duration_in_pickup);
        }
        return getInt("doze.pulse.duration.in", R.integer.doze_pulse_duration_in);
    }

    public int getPulseVisibleDuration() {
        return getInt("doze.pulse.duration.visible", R.integer.doze_pulse_duration_visible);
    }

    public int getPulseOutDuration() {
        return getInt("doze.pulse.duration.out", R.integer.doze_pulse_duration_out);
    }

    public boolean getPulseOnSigMotion() {
        return getBoolean("doze.pulse.sigmotion", R.bool.doze_pulse_on_significant_motion);
    }

    public boolean getVibrateOnSigMotion() {
        return SystemProperties.getBoolean("doze.vibrate.sigmotion", false);
    }

    public boolean getPulseOnPickup() {
        return getBoolean("doze.pulse.pickup", R.bool.doze_pulse_on_pick_up);
    }

    public boolean getVibrateOnPickup() {
        return SystemProperties.getBoolean("doze.vibrate.pickup", false);
    }

    public boolean getProxCheckBeforePulse() {
        return getBoolean("doze.pulse.proxcheck", R.bool.doze_proximity_check_before_pulse);
    }

    public boolean getPickupPerformsProxCheck() {
        return getBoolean("doze.pickup.proxcheck", R.bool.doze_pickup_performs_proximity_check);
    }

    public boolean getPulseOnNotifications() {
        return getBoolean("doze.pulse.notifications", R.bool.doze_pulse_on_notifications);
    }

    public PulseSchedule getPulseSchedule() {
        String spec = getString("doze.pulse.schedule", R.string.doze_pulse_schedule);
        if (sPulseSchedule == null || !sPulseSchedule.mSpec.equals(spec)) {
            sPulseSchedule = PulseSchedule.parse(spec);
        }
        return sPulseSchedule;
    }

    public int getPulseScheduleResets() {
        return getInt("doze.pulse.schedule.resets", R.integer.doze_pulse_schedule_resets);
    }

    public int getPickupVibrationThreshold() {
        return getInt("doze.pickup.vibration.threshold", R.integer.doze_pickup_vibration_threshold);
    }

    private boolean getBoolean(String propName, int resId) {
        return SystemProperties.getBoolean(propName, this.mContext.getResources().getBoolean(resId));
    }

    private int getInt(String propName, int resId) {
        return MathUtils.constrain(SystemProperties.getInt(propName, this.mContext.getResources().getInteger(resId)), 0, 60000);
    }

    private String getString(String propName, int resId) {
        return SystemProperties.get(propName, this.mContext.getString(resId));
    }
}
