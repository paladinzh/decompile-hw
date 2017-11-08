package com.android.contacts.format;

import android.database.CharArrayBuffer;
import com.android.contacts.test.NeededForTesting;

public class FormatUtils {
    @NeededForTesting
    public static int overlapPoint(String string1, String string2) {
        if (string1 == null || string2 == null) {
            return -1;
        }
        return overlapPoint(string1.toCharArray(), string2.toCharArray());
    }

    public static int overlapPoint(char[] array1, char[] array2) {
        if (array1 == null || array2 == null) {
            return -1;
        }
        int count1 = array1.length;
        int count2 = array2.length;
        while (count1 > 0 && count2 > 0 && array1[count1 - 1] == array2[count2 - 1]) {
            count1--;
            count2--;
        }
        int size = count2;
        int i = 0;
        while (i < count1) {
            if (i + size > count1) {
                size = count1 - i;
            }
            int j = 0;
            while (j < size && array1[i + j] == array2[j]) {
                j++;
            }
            if (j == size) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @NeededForTesting
    public static void copyToCharArrayBuffer(String text, CharArrayBuffer buffer) {
        if (text != null) {
            char[] data = buffer.data;
            if (data == null || data.length < text.length()) {
                buffer.data = text.toCharArray();
            } else {
                text.getChars(0, text.length(), data, 0);
            }
            buffer.sizeCopied = text.length();
            return;
        }
        buffer.sizeCopied = 0;
    }

    @NeededForTesting
    public static String charArrayBufferToString(CharArrayBuffer buffer) {
        return new String(buffer.data, 0, buffer.sizeCopied);
    }

    public static int indexOfWordPrefix(CharSequence text, char[] prefix) {
        if (prefix == null || text == null) {
            return -1;
        }
        int textLength = text.length();
        int prefixLength = prefix.length;
        if (prefixLength == 0 || textLength < prefixLength) {
            return -1;
        }
        int i = 0;
        while (i < textLength) {
            while (i < textLength && !Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }
            if (i + prefixLength > textLength) {
                return -1;
            }
            int j = 0;
            while (j < prefixLength && Character.toLowerCase(text.charAt(i + j)) == prefix[j]) {
                j++;
            }
            if (j == prefixLength) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static CharSequence forceLeftToRight(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        sb.append('‪');
        sb.append(text);
        sb.append('‬');
        return sb.toString();
    }
}
