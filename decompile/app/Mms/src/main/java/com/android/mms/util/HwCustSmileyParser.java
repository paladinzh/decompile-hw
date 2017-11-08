package com.android.mms.util;

import android.content.Context;
import java.util.HashMap;
import java.util.regex.Pattern;

public class HwCustSmileyParser {
    public HwCustSmileyParser(Context context) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void initSmileyParser(Context context, SmileyParser sp) {
    }

    public String[] getSmileyText() {
        return new String[0];
    }

    public String[] getSmileyName() {
        return new String[0];
    }

    public HashMap<String, Integer> getSmileyToRes() {
        return null;
    }

    public Pattern getPattern() {
        return null;
    }

    public int getIconIds(int which) {
        return 0;
    }

    public int[] getSmileyResIds() {
        return new int[0];
    }
}
