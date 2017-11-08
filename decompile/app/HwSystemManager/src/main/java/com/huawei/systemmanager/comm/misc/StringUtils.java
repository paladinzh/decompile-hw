package com.huawei.systemmanager.comm.misc;

import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HSMConst;
import java.text.DecimalFormat;

public class StringUtils {
    public static String formatBytes(long size) {
        return formatBytes(size, true);
    }

    public static String formatBytes(long size, boolean hasByte) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        if (size >= 1073741824) {
            return formatter.format((double) (((float) size) / 1.07374182E9f)) + " G" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else if (size >= 1048576) {
            return formatter.format((double) (((float) size) / 1048576.0f)) + " M" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else if (size >= 1024) {
            return formatter.format((double) (((float) size) / 1024.0f)) + " K" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else {
            return size + (hasByte ? " B" : "");
        }
    }

    public static String formatBytesInKB(long size) {
        return formatBytes(1024 * size, true);
    }

    public static String formatBytesInK(long size) {
        return formatBytes(1024 * size, false);
    }

    public static String formatFloat(float f, int pos) {
        float p = Utility.ALPHA_MAX;
        StringBuilder format = new StringBuilder("#0");
        for (int i = 0; i < pos; i++) {
            if (i == 0) {
                format.append('.');
            }
            p *= HSMConst.DEVICE_SIZE_100;
            format.append('0');
        }
        return new DecimalFormat(format.toString()).format((double) (((float) Math.round(f * p)) / p));
    }

    public static int extractPositiveInteger(String str, int defValue) {
        int N = str.length();
        int index = 0;
        while (index < N) {
            char curCh = str.charAt(index);
            if (curCh < '0' || curCh > '9') {
                index++;
            } else {
                int start = index;
                index++;
                while (index < N) {
                    curCh = str.charAt(index);
                    if (curCh < '0' || curCh > '9') {
                        break;
                    }
                    index++;
                }
                return Integer.parseInt(str.substring(start, index));
            }
        }
        return defValue;
    }

    public static SpannableStringBuilder highlight(String text, int start, int end, int color) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new ForegroundColorSpan(color), start, end, 33);
        return spannable;
    }

    public static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    public static int parseInt(String s) {
        return parseInt(s, 0);
    }

    public static long parseLong(String value, long def) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static float parseFloat(String s) {
        if (s != null) {
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
            }
        }
        return 0.0f;
    }

    public static boolean isEmpty(String s) {
        if (s != null) {
            int count = s.length();
            for (int i = 0; i < count; i++) {
                char c = s.charAt(i);
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                    return false;
                }
            }
        }
        return true;
    }

    public static String trimAppName(String str) {
        int length = str.length();
        int index = 0;
        while (index < length && (str.charAt(index) <= ' ' || str.charAt(index) == ' ')) {
            index++;
        }
        if (index > 0) {
            return str.substring(index);
        }
        return str;
    }

    public static CharSequence formatBytesToUnit(Long size, boolean hasByte) {
        if (size.longValue() >= 1073741824) {
            return "G" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else if (size.longValue() >= 1048576) {
            return "M" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else if (size.longValue() >= 1024) {
            return "K" + (hasByte ? ConstValues.B_VERSION_CHAR : "");
        } else {
            return hasByte ? ConstValues.B_VERSION_CHAR : "";
        }
    }

    public static CharSequence formatBytesToNum(Long size) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        if (size.longValue() >= 1073741824) {
            return formatter.format((double) (((float) size.longValue()) / 1.07374182E9f));
        }
        if (size.longValue() >= 1048576) {
            return formatter.format((double) (((float) size.longValue()) / 1048576.0f));
        }
        if (size.longValue() >= 1024) {
            return formatter.format((double) (((float) size.longValue()) / 1024.0f));
        }
        return formatter.format(size);
    }
}
