package com.huawei.mms.util;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class PrefixHighlighter {
    private static char IGNORE_CHARS = '-';
    private ForegroundColorSpan mPrefixColorSpan;
    private final int mPrefixHighlightColor;
    private int mSearchMatchType = -1;

    public PrefixHighlighter(int prefixHighlightColor) {
        this.mPrefixHighlightColor = prefixHighlightColor;
    }

    public void setText(TextView view, String text, char[] prefix) {
        String lOriginalText = text;
        CharSequence lHighlightedText = text;
        if (!(isSearchTypeName(this.mSearchMatchType) || prefix == null || prefix.length == 0)) {
            lHighlightedText = apply(text, String.copyValueOf(prefix).toCharArray());
        }
        view.setText(lHighlightedText);
    }

    private boolean isSearchTypeName(int searchType) {
        return searchType == 40 || searchType == 32;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CharSequence apply(CharSequence text, char[] prefix) {
        if (prefix == null || prefix.length == 0 || text == null) {
            return text;
        }
        int index = HighLightMatchUtils.indexOfWordPrefix(text, prefix);
        if (index != -1) {
            if (this.mPrefixColorSpan == null) {
                this.mPrefixColorSpan = new ForegroundColorSpan(this.mPrefixHighlightColor);
            }
            SpannableString result = new SpannableString(text);
            result.setSpan(this.mPrefixColorSpan, index, prefix.length + index, 0);
            return result;
        }
        CharSequence mresulttext = highlightMatchNumber(text.toString(), String.copyValueOf(prefix));
        if (mresulttext != null) {
            return mresulttext;
        }
        return text;
    }

    private CharSequence highlightMatchNumber(String aTarget, String input) {
        int aTargetLength = aTarget.length();
        int aInputLength = input.length();
        int end = 0;
        int indexInput = 0;
        if (aInputLength == 0) {
            return null;
        }
        int tempIndex = aTarget.indexOf(input.charAt(0));
        if (tempIndex == -1) {
            return null;
        }
        int start = tempIndex;
        do {
            char targetChar = aTarget.charAt(tempIndex);
            if (targetChar != input.charAt(indexInput)) {
                if (isLegalChar(targetChar)) {
                    indexInput = 0;
                    start++;
                    int nextIndex = aTarget.substring(start).indexOf(input.charAt(0));
                    if (nextIndex != -1) {
                        start += nextIndex;
                        tempIndex = start;
                        if (tempIndex >= aTargetLength) {
                            break;
                        }
                    } else {
                        return null;
                    }
                }
            }
            indexInput++;
            end = tempIndex;
            tempIndex++;
            if (tempIndex >= aTargetLength) {
                break;
            }
            break;
        } while (indexInput < aInputLength);
        end++;
        if (start >= end || start <= -1 || end > aTargetLength) {
            return null;
        }
        if (this.mPrefixColorSpan == null) {
            this.mPrefixColorSpan = new ForegroundColorSpan(this.mPrefixHighlightColor);
        }
        SpannableString result = new SpannableString(aTarget);
        result.setSpan(this.mPrefixColorSpan, start, end, 0);
        return result;
    }

    private boolean isLegalChar(char targetChar) {
        if (IGNORE_CHARS == targetChar || '(' == targetChar || ')' == targetChar || ' ' == targetChar) {
            return false;
        }
        return true;
    }

    public int getHighlightColor() {
        return this.mPrefixHighlightColor;
    }
}
