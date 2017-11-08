package rcstelephony;

import android.net.Uri;
import android.provider.Telephony.BaseMmsColumns;
import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RcsMessagingConstants$Mms implements BaseMmsColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://mms");
    public static final Pattern NAME_ADDR_EMAIL_PATTERN = Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");
    public static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("\\s*\"([^\"]*)\"\\s*");

    public static String extractAddrSpec(String address) {
        Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);
        if (match.matches()) {
            return match.group(2);
        }
        return address;
    }

    public static boolean isEmailAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(extractAddrSpec(address)).matches();
    }
}
