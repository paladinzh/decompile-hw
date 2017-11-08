package tmsdkobf;

import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class rp {
    private static SimpleDateFormat Oh;
    public static final TimeZone Oi = TimeZone.getDefault();
    public static final String Oj = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String[] Ok = new String[]{"screenshot", "截屏"};
    private static final String[] Ol = new String[]{"xj", "androidgeek", "logo", "pt", "MYXJ", "C360"};
    private static String TAG = "MediaFileUtil";

    /* compiled from: Unknown */
    public static class a {
        public static final String[] Om = new String[]{"mp4", "avi", "3gpp", "mkv", "wmv", "3gpp2", "mp2ts", "3gp", "mov", "flv", "rmvb", "flv"};
        public static final String[] On = new String[]{"jpg", "jpeg", "png", "gif", "bmp"};
        public static final String[] Oo = new String[]{"mp3", "wma", "flac", "wav", "mid", "m4a", "aac"};
        public static final String[] Op = new String[]{"jpg", "jpeg"};
    }

    private static long a(ExifInterface exifInterface) {
        String attribute = exifInterface.getAttribute("DateTime");
        if (attribute == null) {
            return 0;
        }
        d.c(TAG, "exif time:" + attribute);
        ParsePosition parsePosition = new ParsePosition(0);
        try {
            if (Oh == null) {
                Oh = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                Oh.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            Date parse = Oh.parse(attribute, parsePosition);
            if (parse == null) {
                return 0;
            }
            long time = parse.getTime();
            return time - ((long) Oi.getOffset(time));
        } catch (Throwable e) {
            d.a(TAG, "exifDateTime", e);
            return 0;
        } catch (Exception e2) {
            return 0;
        }
    }

    public static String aN(String str) {
        if (str == null) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) ? str.substring(lastIndexOf + 1, str.length()) : str;
    }

    public static boolean dE(String str) {
        String toLowerCase = str.toLowerCase();
        for (CharSequence contains : Ok) {
            if (toLowerCase.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dF(String str) {
        String toLowerCase = dG(str).toLowerCase();
        for (String equals : a.On) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static String dG(String str) {
        String str2 = null;
        if (str == null) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) {
            str2 = str.substring(lastIndexOf + 1);
        }
        return str2;
    }

    public static boolean dH(String str) {
        if (str == null) {
            return false;
        }
        String toLowerCase = dG(str).toLowerCase();
        for (String equals : a.Op) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dI(String str) {
        return !TextUtils.isEmpty(str) ? str.startsWith(Oj) : false;
    }

    public static boolean dJ(String str) {
        if (str != null) {
            for (String startsWith : Ol) {
                if (str.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long dK(String str) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Throwable th) {
            d.a(TAG, "getImageTakenTime", th);
            exifInterface = null;
        }
        return exifInterface == null ? 0 : a(exifInterface);
    }
}
