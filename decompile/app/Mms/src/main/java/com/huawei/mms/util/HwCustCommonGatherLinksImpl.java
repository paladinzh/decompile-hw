package com.huawei.mms.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.util.Linkify.MatchFilter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwCustCommonGatherLinksImpl extends HwCustCommonGatherLinks {
    private static final boolean LINKIFY_USSD = SystemProperties.get("ro.config.ussd_linkify", "false").equals("true");
    private static final int NUMBER_LINK_MIN_DIGITS = SystemProperties.getInt("ro.config.numberLinkMinDigits", 3);
    private static final int NUMBER_LINK_MIN_DIGITS_DEFAULT = 3;
    protected static final String TAG = "CustCommonGatherLinks";
    private static Pattern sPhoneLinkPattern = null;
    private Pattern mUSSD_NUMBER = null;

    public HwCustCommonGatherLinksImpl() {
        if (LINKIFY_USSD) {
            this.mUSSD_NUMBER = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})((\\*#|\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*))?)?)?)?#)");
        }
    }

    public void gatherUssdLink(List<TextSpan> spanList, CharSequence sourceText, String[] schemes, MatchFilter matchFilter, Context context) {
        if (LINKIFY_USSD) {
            Pattern pattern = this.mUSSD_NUMBER;
            Matcher m = pattern.matcher(sourceText);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (matchFilter == null || matchFilter.acceptMatch(sourceText, start, end)) {
                    String url = makeUrl(m.group(0), schemes, m);
                    if (pattern == this.mUSSD_NUMBER) {
                        spanList.add(new TextSpan(url, start, end, 1));
                    }
                }
            }
        }
    }

    private static final String makeUrl(String url, String[] prefixes, Matcher m) {
        boolean hasPrefix = false;
        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0, prefixes[i].length())) {
                hasPrefix = true;
                if (!url.regionMatches(false, 0, prefixes[i], 0, prefixes[i].length())) {
                    url = prefixes[i] + url.substring(prefixes[i].length());
                }
                if (hasPrefix) {
                    return prefixes[0] + url;
                }
                return url;
            }
        }
        if (hasPrefix) {
            return url;
        }
        return prefixes[0] + url;
    }

    public Intent getIntentForUssdNumber(Intent intent, String ussdNumber) {
        if (!LINKIFY_USSD || !ussdNumber.startsWith("tel") || !ussdNumber.endsWith("#")) {
            return intent;
        }
        return new Intent("android.intent.action.CALL", Uri.fromParts("tel", ussdNumber.replaceAll("tel:", ""), "#"));
    }

    public Pattern getWebUrl(Pattern webURL) {
        return webURL;
    }

    public Pattern getCustPhoneNumberLinkPattern(Pattern aDefaultPattern) {
        if (NUMBER_LINK_MIN_DIGITS <= 3) {
            return aDefaultPattern;
        }
        if (sPhoneLinkPattern != null) {
            return sPhoneLinkPattern;
        }
        String lPatternStr = "(\\+[0-9]+[\\- \\.]*)?(\\([0-9]+\\)[\\- \\.]*)?([0-9][0-9\\- \\.]";
        for (int i = 3; i < NUMBER_LINK_MIN_DIGITS; i++) {
            lPatternStr = lPatternStr + "[0-9\\- \\.]";
        }
        sPhoneLinkPattern = Pattern.compile(lPatternStr + "+[0-9])");
        return sPhoneLinkPattern;
    }
}
