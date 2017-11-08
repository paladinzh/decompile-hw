package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class TimingTask {
    private int customRepeatCycle;
    private boolean enabled;
    private int hour;
    private int minute;
    private int repeat;
    private int type;

    static final class DaysOfWeek {
        static final int[] DAY_MAP = new int[]{2, 3, 4, 5, 6, 7, 1};
        private int mDays;

        public void set(int r1, boolean r2) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.settings.TimingTask.DaysOfWeek.set(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.TimingTask.DaysOfWeek.set(int, boolean):void");
        }

        DaysOfWeek(int days) {
            this.mDays = days;
        }

        public String toGogaleString(Context context) {
            StringBuilder ret = new StringBuilder();
            if (context == null || this.mDays == 0) {
                return "";
            }
            int i;
            int dayCount = 0;
            for (int days = this.mDays; days > 0; days >>= 1) {
                if ((days & 1) == 1) {
                    dayCount++;
                }
            }
            int offSet = (Calendar.getInstance().getFirstDayOfWeek() + 5) % 7;
            String[] shortWeekdaysOrigin = LocaleData.get(Locale.getDefault()).shortStandAloneWeekdayNames;
            String[] shortWeekdays = new String[7];
            for (i = 1; i < shortWeekdaysOrigin.length; i++) {
                shortWeekdays[(i + 5) % 7] = shortWeekdaysOrigin[i];
            }
            boolean[] checkedItemsArray = getBooleanArray();
            boolean[] checkedItemsArrayTransformed = new boolean[7];
            String[] repeatArray = new String[7];
            for (int j = 0; j < checkedItemsArray.length; j++) {
                int transformedIndex = (j + offSet) % 7;
                repeatArray[j] = shortWeekdays[transformedIndex];
                checkedItemsArrayTransformed[j] = checkedItemsArray[transformedIndex];
            }
            int count = checkedItemsArrayTransformed.length;
            for (i = 0; i < count; i++) {
                if (checkedItemsArrayTransformed[i]) {
                    ret.append(repeatArray[i]);
                    dayCount--;
                    if (dayCount > 0) {
                        ret.append(" ");
                    }
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return (this.mDays & (1 << day)) > 0;
        }

        public boolean isChecked(int day) {
            return isSet(day);
        }

        public int getCoded() {
            return this.mDays;
        }

        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }
    }

    public static class TimingColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.providers.settings.timingtaskprovider/timingtask");
    }

    public TimingTask(int type, boolean enabled, int hour, int minute, int repeat, int customRepeatCycle) {
        this.type = type;
        this.enabled = enabled;
        this.hour = hour;
        this.minute = minute;
        this.repeat = repeat;
        this.customRepeatCycle = customRepeatCycle;
        updateCustomRepeatCycle();
    }

    public int getType() {
        return this.type;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
        updateCustomRepeatCycle();
    }

    public int getCustomRepeatCycle() {
        return this.customRepeatCycle;
    }

    public void setCustomRepeatCycle(int customRepeatCycle) {
        this.customRepeatCycle = customRepeatCycle;
        updateRepeat();
    }

    private void updateCustomRepeatCycle() {
        if (this.repeat == 0) {
            this.customRepeatCycle = 0;
        } else if (this.repeat == 1) {
            this.customRepeatCycle = 31;
        } else if (this.repeat == 3) {
            this.customRepeatCycle = 127;
        }
    }

    private void updateRepeat() {
        if (this.customRepeatCycle == 0) {
            this.repeat = 0;
        } else if (this.customRepeatCycle == 31) {
            this.repeat = 1;
        } else if (this.customRepeatCycle == 127) {
            this.repeat = 3;
        }
    }

    public int hashCode() {
        return ((((((((((this.customRepeatCycle + 31) * 31) + (this.enabled ? 1231 : 1237)) * 31) + this.hour) * 31) + this.minute) * 31) + this.repeat) * 31) + this.type;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TimingTask other = (TimingTask) obj;
        return this.customRepeatCycle == other.customRepeatCycle && this.enabled == other.enabled && this.hour == other.hour && this.minute == other.minute && this.repeat == other.repeat && this.type == other.type;
    }
}
