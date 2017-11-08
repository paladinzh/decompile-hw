package tmsdkobf;

import android.telephony.PhoneNumberUtils;

/* compiled from: Unknown */
public final class lu {
    static final String[] wG = new String[]{"-", "+86", "0086", "12593", "17909", "17951", "17911", "10193", "12583", "12520", "96688"};

    public static String bV(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        for (String str2 : wG) {
            if (str.startsWith(str2)) {
                return str.substring(str2.length());
            }
        }
        return str;
    }

    public static String bW(String str) {
        return bV(stripSeparators(str));
    }

    public static String stripSeparators(String str) {
        return str == null ? str : PhoneNumberUtils.stripSeparators(str).replace("-", "").replace(" ", "").trim();
    }
}
