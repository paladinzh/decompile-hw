package com.huawei.cspcommon.util;

import android.content.Context;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.HanziToPinyin.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DialerHighlighter {
    static final int[] ENGLISH_DIALPAD_MAP = new int[]{2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9};
    private static HanziToPinyin mHanziToPinyin = null;
    private static HanziToBopomofo mHanziToZhuyin = null;

    static {
        new Thread() {
            public void run() {
                DialerHighlighter.mHanziToPinyin = HanziToPinyin.getInstance();
                DialerHighlighter.mHanziToZhuyin = HanziToBopomofo.getInstance();
            }
        }.start();
    }

    public static Object[] convertToPinyin(String aHanzi) {
        if (mHanziToPinyin == null || aHanzi == null) {
            return new Object[0];
        }
        if (mHanziToPinyin.getTokensWithName(aHanzi).getTokens().size() <= 0) {
            return new Object[0];
        }
        return new Object[]{mHanziToPinyin.getTokensWithName(aHanzi).getTokens(), mHanziToPinyin.getTokensWithName(aHanzi).getName().toLowerCase(Locale.getDefault())};
    }

    public static Object[] convertToZhuyin(String aHanzi) {
        if (mHanziToZhuyin == null) {
            return new Object[0];
        }
        ArrayList<Token> tokens = mHanziToZhuyin.get(aHanzi);
        if (tokens.size() <= 0) {
            return new Object[0];
        }
        StringBuilder sb = new StringBuilder();
        boolean isNotEmpty = false;
        for (Token token : tokens) {
            if (isNotEmpty) {
                sb.append(' ');
            }
            sb.append(token.target);
            isNotEmpty = true;
        }
        return new Object[]{tokens, sb.toString().toLowerCase(Locale.getDefault())};
    }

    public Integer[] findIndexForWords(String aTarget, StringBuffer normDialMapBuffer) {
        ArrayList<Integer> indexes = new ArrayList();
        int temp = 0;
        int length = aTarget.length();
        String tempString = aTarget;
        int lOriginalLength = aTarget.length();
        char c = aTarget.charAt(0);
        while (true) {
            if (c != ' ' && c != '.') {
                break;
            }
            temp++;
            if (temp >= length) {
                break;
            }
            c = aTarget.charAt(temp);
        }
        indexes.add(Integer.valueOf(temp));
        int aStart = temp + 0;
        aTarget = aTarget.substring(temp);
        length = aTarget.length();
        normDialMapBuffer.append(c);
        while (true) {
            temp = getMinPositive(aTarget.indexOf(" "), aTarget.indexOf(" "));
            if (temp != -1) {
                temp++;
                while (temp < length && (aTarget.charAt(temp) == '.' || aTarget.charAt(temp) == ' ')) {
                    temp++;
                }
                if (temp != 0) {
                    aStart += temp;
                    if (aStart < lOriginalLength) {
                        normDialMapBuffer.append(tempString.charAt(aStart));
                        indexes.add(Integer.valueOf(aStart));
                    }
                    aTarget = aTarget.substring(temp);
                    length = aTarget.length();
                }
                if (temp == 0) {
                    break;
                }
            } else {
                break;
            }
        }
        if (indexes.size() <= 0) {
            return new Integer[0];
        }
        Integer[] tempArray = new Integer[indexes.size()];
        indexes.toArray(tempArray);
        return tempArray;
    }

    public static String convertToDialMapEX(String aInput, Context context) {
        if (Locale.getDefault().getLanguage().equals("de")) {
            aInput = aInput.replace('ß', 'S');
        }
        String lower = aInput.toLowerCase(Locale.getDefault());
        StringBuffer sb = new StringBuffer();
        char[] array = lower.toCharArray();
        String[] digitArray = context.getResources().getStringArray(R.array.digit_dialpad);
        String[] alphabetArrayUpper = context.getResources().getStringArray(R.array.alphabet_dialpad);
        int strLength = alphabetArrayUpper.length;
        String[] alphabetArrayLower = new String[strLength];
        boolean uppperToLowerDone = false;
        for (char c : array) {
            if (c <= '`' || c >= '{') {
                if (!uppperToLowerDone) {
                    for (int i = 0; i < strLength; i++) {
                        alphabetArrayLower[i] = alphabetArrayUpper[i].toLowerCase(Locale.getDefault());
                    }
                    uppperToLowerDone = true;
                }
                sb.append(getDigit(alphabetArrayLower, digitArray, c));
            } else {
                sb.append(ENGLISH_DIALPAD_MAP[c - 97]);
            }
        }
        return sb.toString();
    }

    private int getMinPositive(int aFirst, int aSecond) {
        if (aFirst < 0) {
            if (aSecond > -1) {
                return aSecond;
            }
            return -1;
        } else if (aSecond >= 0 && aFirst > aSecond) {
            return aSecond;
        } else {
            return aFirst;
        }
    }

    private static char getDigit(String[] alphabets, String[] digits, char searchKey) {
        int i;
        boolean continuousUnicode = true;
        int arrayLength = alphabets.length;
        char first = alphabets[0].charAt(0);
        for (i = 0; i < arrayLength; i++) {
            if (alphabets[i].charAt(0) - first != i) {
                continuousUnicode = false;
                break;
            }
        }
        if (!continuousUnicode) {
            HashMap<Character, Character> dialpadMap = new HashMap(arrayLength);
            for (i = 0; i < arrayLength; i++) {
                dialpadMap.put(Character.valueOf(alphabets[i].charAt(0)), Character.valueOf(digits[i].charAt(0)));
            }
            Object searchValue = dialpadMap.get(Character.valueOf(searchKey));
            if (searchValue != null) {
                return searchValue.toString().charAt(0);
            }
            return searchKey;
        } else if (searchKey < alphabets[0].charAt(0) || searchKey > alphabets[arrayLength - 1].charAt(0)) {
            return searchKey;
        } else {
            return digits[searchKey - first].charAt(0);
        }
    }
}
