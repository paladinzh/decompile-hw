package com.android.mms.dom.smil;

import org.w3c.dom.smil.Time;

public class TimeImpl implements Time {
    boolean mResolved;
    double mResolvedOffset;
    short mTimeType;

    TimeImpl(String timeValue, int constraints) {
        if (timeValue.equals("indefinite") && (constraints & 1) != 0) {
            this.mTimeType = (short) 0;
        } else if ((constraints & 2) != 0) {
            int sign = 1;
            if (timeValue.startsWith("+")) {
                timeValue = timeValue.substring(1);
            } else if (timeValue.startsWith("-")) {
                timeValue = timeValue.substring(1);
                sign = -1;
            }
            this.mResolvedOffset = ((double) (((float) sign) * parseClockValue(timeValue))) / 1000.0d;
            this.mResolved = true;
            this.mTimeType = (short) 1;
        } else {
            throw new IllegalArgumentException("Unsupported time value");
        }
    }

    public static float parseClockValue(String clockValue) {
        String[] timeValues;
        int indexOfMinutes;
        float result = 0.0f;
        try {
            clockValue = clockValue.trim();
            if (clockValue.endsWith("ms")) {
                result = parseFloat(clockValue, 2, true);
            } else if (clockValue.endsWith("s")) {
                result = 1000.0f * parseFloat(clockValue, 1, true);
            } else if (clockValue.endsWith("min")) {
                result = 60000.0f * parseFloat(clockValue, 3, true);
            } else if (!clockValue.endsWith("h")) {
                return parseFloat(clockValue, 0, true) * 1000.0f;
            } else {
                result = 3600000.0f * parseFloat(clockValue, 1, true);
            }
        } catch (NumberFormatException e) {
            timeValues = clockValue.split(":");
            if (timeValues.length == 2) {
                indexOfMinutes = 0;
            } else if (timeValues.length == 3) {
                result = (float) (((int) parseFloat(timeValues[0], 0, false)) * 3600000);
                indexOfMinutes = 1;
            } else {
                throw new IllegalArgumentException();
            }
            minutes = (int) parseFloat(timeValues[indexOfMinutes], 0, false);
            int minutes;
            if (minutes < 0 || minutes > 59) {
                throw new IllegalArgumentException();
            }
            result += (float) (60000 * minutes);
            float seconds = parseFloat(timeValues[indexOfMinutes + 1], 0, true);
            if (seconds < 0.0f || seconds >= 60.0f) {
                throw new IllegalArgumentException();
            }
            result += 60000.0f * seconds;
        } catch (NumberFormatException e2) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    private static float parseFloat(String value, int ignoreLast, boolean parseDecimal) {
        value = value.substring(0, value.length() - ignoreLast);
        int indexOfComma = value.indexOf(46);
        if (indexOfComma == -1) {
            return (float) Integer.parseInt(value);
        }
        if (parseDecimal) {
            value = value + "000";
            return Float.parseFloat(value.substring(0, indexOfComma)) + (Float.parseFloat(value.substring(indexOfComma + 1, indexOfComma + 4)) / 1000.0f);
        }
        throw new IllegalArgumentException("int value contains decimal");
    }

    public double getOffset() {
        return 0.0d;
    }

    public boolean getResolved() {
        return this.mResolved;
    }

    public double getResolvedOffset() {
        return this.mResolvedOffset;
    }

    public short getTimeType() {
        return this.mTimeType;
    }
}
