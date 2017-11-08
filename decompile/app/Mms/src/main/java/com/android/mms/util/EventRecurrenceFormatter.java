package com.android.mms.util;

import android.content.res.Resources;
import android.text.format.DateUtils;
import com.android.calendarcommon2.EventRecurrence;
import com.google.android.gms.R;

public class EventRecurrenceFormatter {
    public static String getRepeatString(Resources r, EventRecurrence recurrence) {
        switch (recurrence.freq) {
            case 4:
                return r.getString(R.string.daily);
            case 5:
                if (!recurrence.repeatsOnEveryWeekDay()) {
                    String format = r.getString(R.string.weekly);
                    StringBuilder days = new StringBuilder();
                    int count = recurrence.bydayCount - 1;
                    if (count >= 0) {
                        for (int i = 0; i < count; i++) {
                            days.append(dayToString(recurrence.byday[i]));
                            days.append(",");
                        }
                        days.append(dayToString(recurrence.byday[count]));
                        return String.format(format, new Object[]{days.toString()});
                    } else if (recurrence.startDate == null) {
                        return null;
                    } else {
                        int day = EventRecurrence.timeDay2Day(recurrence.startDate.weekDay);
                        return String.format(format, new Object[]{dayToString(day)});
                    }
                } else if (recurrence.count == 0) {
                    if (recurrence.interval == 0 || 1 == recurrence.interval) {
                        return r.getString(R.string.every_weekday);
                    }
                    if (2 == recurrence.interval) {
                        return r.getString(R.string.everyotherweek_weekday);
                    }
                }
                break;
            case 6:
                break;
            case 7:
                return r.getString(R.string.yearly_plain);
            default:
                return null;
        }
        return r.getString(R.string.monthly);
    }

    private static String dayToString(int day) {
        return DateUtils.getDayOfWeekString(dayToUtilDay(day), 10);
    }

    private static int dayToUtilDay(int day) {
        switch (day) {
            case 65536:
                return 1;
            case 131072:
                return 2;
            case 262144:
                return 3;
            case 524288:
                return 4;
            case 1048576:
                return 5;
            case 2097152:
                return 6;
            case 4194304:
                return 7;
            default:
                throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
}
