package com.huawei.mms.util;

import android.content.Context;
import android.content.Intent;
import android.text.util.Linkify.MatchFilter;
import java.util.List;
import java.util.regex.Pattern;

public class HwCustCommonGatherLinks {
    public void gatherUssdLink(List<TextSpan> list, CharSequence sourceText, String[] schemes, MatchFilter matchFilter, Context context) {
    }

    public Intent getIntentForUssdNumber(Intent intent, String ussdNumber) {
        return intent;
    }

    public Pattern getWebUrl(Pattern webURL) {
        return webURL;
    }

    public Pattern getCustPhoneNumberLinkPattern(Pattern aDefaultPattern) {
        return aDefaultPattern;
    }
}
