package com.android.contacts.format;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import com.android.contacts.compatibility.QueryUtil;
import com.huawei.cspcommon.util.SearchMatch;

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
        if (!((QueryUtil.isUseHwSearch() && isSearchTypeName(this.mSearchMatchType)) || prefix == null)) {
            lHighlightedText = apply(text, String.copyValueOf(prefix).toCharArray());
        }
        view.setText(lHighlightedText);
    }

    public void setSearchMatchType(int type) {
        this.mSearchMatchType = type;
    }

    private boolean isSearchTypeName(int searchType) {
        return searchType == 40 || searchType == 32;
    }

    public CharSequence apply(CharSequence text, char[] prefix) {
        if (prefix == null) {
            return text;
        }
        int index = FormatUtils.indexOfWordPrefix(text, prefix);
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

    public CharSequence apply(CharSequence text, String[] queryString, int[] matchTypeArray, int matchType) {
        if (!isInputLegal(text, queryString, matchTypeArray)) {
            return null;
        }
        SpannableString result = new SpannableString(text);
        int highlightTimes = Math.min(queryString.length, matchTypeArray.length);
        for (int i = 0; i < highlightTimes; i++) {
            if (matchType == matchTypeArray[i]) {
                int[] matchRange = getIndexOfWordPreFix(text, queryString[i]);
                if (matchRange.length == 0 || matchRange[0] == -1) {
                    matchRange = getRangeOfMatchNumber(text, queryString[i]);
                    if (2 == matchRange.length) {
                        result.setSpan(new ForegroundColorSpan(this.mPrefixHighlightColor), matchRange[0], matchRange[1], 0);
                    }
                } else {
                    result.setSpan(new ForegroundColorSpan(this.mPrefixHighlightColor), matchRange[0], matchRange[1], 0);
                }
            }
        }
        return result;
    }

    private int[] getIndexOfWordPreFix(CharSequence text, String input) {
        int[] index = new int[]{-1, -1};
        index[0] = FormatUtils.indexOfWordPrefix(text, input.toCharArray());
        index[1] = index[0] + input.length();
        if (-1 == index[0]) {
            char[] str = SearchMatch.lettersAndDigitsOnly(input, false, true).toCharArray();
            index[0] = FormatUtils.indexOfWordPrefix(text, str);
            index[1] = str.length + index[0];
        }
        return index;
    }

    private int[] getRangeOfMatchNumber(CharSequence text, String input) {
        int[] index = rangeOfMatchNumber(text.toString(), input);
        if (index.length == 0) {
            return rangeOfMatchNumber(text.toString(), SearchMatch.lettersAndDigitsOnly(input, false, true));
        }
        return index;
    }

    private boolean isInputLegal(CharSequence text, String[] queryString, int[] matchTypeArray) {
        if (text == null || queryString == null || matchTypeArray == null) {
            return false;
        }
        return true;
    }

    private CharSequence highlightMatchNumber(String aTarget, String input) {
        int[] matchRange = rangeOfMatchNumber(aTarget, input);
        if (2 != matchRange.length) {
            return null;
        }
        if (this.mPrefixColorSpan == null) {
            this.mPrefixColorSpan = new ForegroundColorSpan(this.mPrefixHighlightColor);
        }
        SpannableString result = new SpannableString(aTarget);
        result.setSpan(this.mPrefixColorSpan, matchRange[0], matchRange[1], 0);
        return result;
    }

    private int[] rangeOfMatchNumber(String aTarget, String input) {
        int aTargetLength = aTarget.length();
        int aInputLength = input.length();
        int end = 0;
        int indexInput = 0;
        if (aInputLength == 0) {
            return new int[0];
        }
        int tempIndex = aTarget.indexOf(input.charAt(0));
        if (tempIndex == -1) {
            return new int[0];
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
                        return new int[0];
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
            return new int[0];
        }
        return new int[]{start, end};
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
