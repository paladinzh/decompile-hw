package com.android.deskclock.stopwatch;

import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.util.Utils;
import java.util.Locale;

public class Stopwatches {
    private static int mTextSize;
    private static Locale slocale = Locale.getDefault();

    public static void updateLocal(Locale local) {
        slocale = local;
    }

    public static String getTimeText(long time, boolean circulationBy24) {
        long hrs;
        String[] timePoint = Utils.getTimePoint(DeskClockApplication.getDeskClockApplication().getApplicationContext());
        if (time < 0) {
            time = 0;
        }
        long mil = (time % 1000) / 10;
        long sec = (time % 60000) / 1000;
        long min = (time % 3600000) / 60000;
        if (circulationBy24) {
            hrs = (time % 86400000) / 3600000;
        } else {
            hrs = time / 3600000;
        }
        if (hrs > 0) {
            setTextSize(R.dimen.paint_big_size_other);
            return String.format(slocale, "%02d" + timePoint[0] + "%02d" + timePoint[1] + "%02d.%02d", new Object[]{Long.valueOf(hrs), Long.valueOf(min), Long.valueOf(sec), Long.valueOf(mil)});
        }
        setTextSize(R.dimen.paint_big_size);
        return String.format(slocale, "%02d" + timePoint[1] + "%02d.%02d", new Object[]{Long.valueOf(min), Long.valueOf(sec), Long.valueOf(mil)});
    }

    private static void setTextSize(int paintBigSizeOther) {
        mTextSize = paintBigSizeOther;
    }
}
