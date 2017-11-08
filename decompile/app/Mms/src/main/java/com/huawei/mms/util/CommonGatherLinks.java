package com.huawei.mms.util;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify.MatchFilter;
import android.util.Patterns;
import com.android.mms.MmsConfig;
import com.huawei.cust.HwCustUtils;
import com.huawei.tmr.util.TMRManagerProxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonGatherLinks {
    static final String[] BROWER_SCHEMES = new String[]{"http://", "https://", "rtsp://", "ftp://"};
    private static final String[] MAIL_SCHEMES = new String[]{"mailto:"};
    private static final String[] TELE_SCHEMES = new String[]{"tel:"};
    public static final HwCustCommonGatherLinks mHwCust = ((HwCustCommonGatherLinks) HwCustUtils.createObj(HwCustCommonGatherLinks.class, new Object[0]));
    private static Pattern mWebURL = Patterns.AUTOLINK_WEB_URL_EMUI;
    public static final MatchFilter sUrlMatchFilter = new MatchFilter() {
        public final boolean acceptMatch(CharSequence s, int start, int end) {
            return start == 0 || s.charAt(start - 1) != '@';
        }
    };

    public static List<TextSpan> getTextSpans(int[] textAddrPos, int[] textDatePos, int[] textRiskUrlPos, CharSequence sourceText, Context context, long currentTime) {
        List<TextSpan> textSpanList = new ArrayList();
        if (sourceText == null) {
            return textSpanList;
        }
        gatherDateLinks(textSpanList, sourceText.toString(), textDatePos, currentTime);
        if (mHwCust != null) {
            mWebURL = mHwCust.getWebUrl(mWebURL);
        }
        gatherLinks(textSpanList, sourceText, mWebURL, BROWER_SCHEMES, sUrlMatchFilter, context, textDatePos);
        gatherLinks(textSpanList, sourceText, Patterns.EMAIL_ADDRESS, MAIL_SCHEMES, null, context, textDatePos);
        if (mHwCust != null) {
            mHwCust.gatherUssdLink(textSpanList, sourceText, new String[]{"tel:"}, null, context);
        }
        if (MmsConfig.enableShortPhoneNumberLink()) {
            Pattern lPhonePattern;
            if (mHwCust == null) {
                lPhonePattern = Patterns.PHONE;
            } else {
                lPhonePattern = mHwCust.getCustPhoneNumberLinkPattern(Patterns.PHONE);
            }
            gatherLinks(textSpanList, sourceText, lPhonePattern, TELE_SCHEMES, null, context, textDatePos);
        } else {
            int[] matches = TMRManagerProxy.getMatchedPhoneNumber(sourceText.toString(), Locale.getDefault().getCountry());
            for (int k = 0; k < matches[0]; k++) {
                int start = matches[(k * 2) + 1];
                int end = matches[(k * 2) + 2];
                if (!isPartMatchedByTime(textDatePos, start, end)) {
                    textSpanList.add(new TextSpan("tel:" + PhoneNumberUtils.normalizeNumber(sourceText.toString().substring(start, end)), start, end, 1));
                }
            }
        }
        gatherAddressLinks(textSpanList, sourceText, textAddrPos);
        if (HwMessageUtils.getRiskUrlEnable(context)) {
            gatherRiskUrlLinks(textSpanList, sourceText, textRiskUrlPos);
        }
        return textSpanList;
    }

    public static List<TextSpan> getTextSpans(int[] textAddrPos, int[] textDatePos, CharSequence sourceText, Context context, long currentTime) {
        return getTextSpans(textAddrPos, textDatePos, null, sourceText, context, currentTime);
    }

    public static void gatherSafetySms(List<TextSpan> spanList) {
        String tag = "safety";
        spanList.add(new TextSpan(tag, 0, tag.length(), 5));
    }

    private static final void gatherLinks(List<TextSpan> spanList, CharSequence sourceText, Pattern pattern, String[] schemes, MatchFilter matchFilter, Context context, int[] textDatePos) {
        Pattern lPhonePattern;
        Matcher m = pattern.matcher(sourceText);
        if (mHwCust == null) {
            lPhonePattern = Patterns.PHONE;
        } else {
            lPhonePattern = mHwCust.getCustPhoneNumberLinkPattern(Patterns.PHONE);
        }
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (matchFilter == null || matchFilter.acceptMatch(sourceText, start, end)) {
                String url = makeUrl(m.group(0), schemes, m);
                if (pattern == mWebURL) {
                    spanList.add(new TextSpan(url, start, end, 0));
                } else if (pattern == Patterns.EMAIL_ADDRESS) {
                    spanList.add(new TextSpan(url, start, end, 2));
                } else if (pattern == lPhonePattern && !isPartMatchedByTime(textDatePos, start, end)) {
                    if (url.startsWith("tel:")) {
                        url = url.substring("tel:".length());
                    }
                    spanList.add(new TextSpan("tel:" + PhoneNumberUtils.normalizeNumber(url), start, end, 1));
                }
            }
        }
    }

    private static void gatherDateLinks(List<TextSpan> spanList, String sourceText, int[] timePos, long currentTime) {
        if (timePos != null) {
            int timeTotal = timePos[0];
            int length = sourceText.length();
            if ((timeTotal * 3) + 1 <= timePos.length) {
                for (int i = 0; i < timeTotal; i++) {
                    int timeType = timePos[(i * 3) + 1];
                    int timeBegin = timePos[(i * 3) + 2];
                    int timeEnd = timePos[(i * 3) + 3] + 1;
                    if (timeBegin < length && timeEnd <= length) {
                        spanList.add(new TextSpan(sourceText.substring(timeBegin, timeEnd), timeBegin, timeEnd, 4, timeType, currentTime));
                    }
                }
            }
        }
    }

    private static void gatherAddressLinks(List<TextSpan> spanList, CharSequence sourceText, int[] addressPos) {
        if (sourceText != null && addressPos != null) {
            int addrTotal = addressPos[0];
            int addrIndex = 0;
            while (addrIndex < addrTotal) {
                if ((addrIndex * 2) + 1 < sourceText.length() && (addrIndex * 2) + 1 < addressPos.length) {
                    int addrBegin = addressPos[(addrIndex * 2) + 1];
                    if ((addrIndex * 2) + 2 < sourceText.length() && (addrIndex * 2) + 2 < addressPos.length) {
                        int addrEnd = addressPos[(addrIndex * 2) + 2] + 1;
                        spanList.add(new TextSpan("geo:0,0?q=" + sourceText.toString().substring(addrBegin, addrEnd), addrBegin, addrEnd, 3));
                    }
                }
                addrIndex++;
            }
        }
    }

    public static void gatherRiskUrlLinksUpgrade(List<TextSpan> spanList, CharSequence sourceText, int[] riskUrlPos) {
        if (sourceText != null && riskUrlPos != null && riskUrlPos.length != 0) {
            int riskUrlTotal = riskUrlPos[0];
            int addrIndex = 0;
            while (addrIndex < riskUrlTotal) {
                if ((addrIndex * 2) + 1 < sourceText.length() && (addrIndex * 2) + 1 < riskUrlPos.length) {
                    int riskUrlBegin = riskUrlPos[(addrIndex * 2) + 1];
                    if ((addrIndex * 2) + 2 < sourceText.length() && (addrIndex * 2) + 2 < riskUrlPos.length) {
                        int riskUrlEnd = riskUrlPos[(addrIndex * 2) + 2] + 1;
                        TextSpan delSpan = null;
                        for (TextSpan span : spanList) {
                            if (span.getStart() == riskUrlBegin) {
                                delSpan = span;
                                break;
                            }
                        }
                        if (delSpan != null) {
                            spanList.remove(delSpan);
                            spanList.add(new TextSpan(delSpan.getUrl(), riskUrlBegin, riskUrlEnd, -6));
                        }
                    }
                }
                addrIndex++;
            }
        }
    }

    public static void gatherRiskUrlLinks(List<TextSpan> spanList, CharSequence sourceText, int[] riskUrlPos) {
        if (sourceText != null && riskUrlPos != null && riskUrlPos.length > 1) {
            if (riskUrlPos[0] > 0) {
                gatherRiskUrlLinksUpgrade(spanList, sourceText, riskUrlPos);
                return;
            }
            int riskUrlTotal = riskUrlPos[1];
            int addrIndex = 0;
            while (addrIndex < riskUrlTotal) {
                if ((addrIndex * 2) + 2 < sourceText.length() && (addrIndex * 2) + 2 < riskUrlPos.length) {
                    int riskUrlBegin = riskUrlPos[(addrIndex * 2) + 2];
                    if ((addrIndex * 2) + 3 < sourceText.length() && (addrIndex * 2) + 3 < riskUrlPos.length) {
                        int riskUrlEnd = riskUrlPos[(addrIndex * 2) + 3] + 1;
                        TextSpan delSpan = null;
                        for (TextSpan span : spanList) {
                            if (span.getStart() == riskUrlBegin) {
                                delSpan = span;
                                break;
                            }
                        }
                        if (delSpan != null) {
                            spanList.remove(delSpan);
                            spanList.add(new TextSpan(delSpan.getUrl(), riskUrlBegin, riskUrlEnd, riskUrlPos[0]));
                        }
                    }
                }
                addrIndex++;
            }
        }
    }

    public static final String makeUrl(String url, String[] prefixes, Matcher m) {
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

    private static boolean isPartMatchedByTime(int[] timePos, int start, int end) {
        if (timePos == null) {
            return false;
        }
        int timeTotal = timePos[0];
        if ((timeTotal * 3) + 1 > timePos.length) {
            return false;
        }
        for (int i = 0; i < timeTotal; i++) {
            int timeBegin = timePos[(i * 3) + 2];
            if (start < timePos[(i * 3) + 3] + 1 && end > timeBegin) {
                return true;
            }
        }
        return false;
    }
}
