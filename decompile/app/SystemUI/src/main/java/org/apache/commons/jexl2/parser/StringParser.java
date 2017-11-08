package org.apache.commons.jexl2.parser;

public class StringParser {
    public static String buildString(CharSequence str, boolean eatsep) {
        int i;
        int begin;
        StringBuilder strb = new StringBuilder(str.length());
        char sep = !eatsep ? '\u0000' : str.charAt(0);
        int length = str.length();
        if (eatsep) {
            i = 1;
        } else {
            i = 0;
        }
        int end = length - i;
        if (eatsep) {
            begin = 1;
        } else {
            begin = 0;
        }
        read(strb, str, begin, end, sep);
        return strb.toString();
    }

    private static int read(StringBuilder strb, CharSequence str, int begin, int end, char sep) {
        boolean escape = false;
        int index = begin;
        while (index < end) {
            char c = str.charAt(index);
            if (escape) {
                if (c == 'u' && index + 4 < end && readUnicodeChar(strb, str, index + 1) > 0) {
                    index += 4;
                } else {
                    boolean notSeparator = sep == '\u0000' ? c == '\'' || c == '\"' : c == sep;
                    if (notSeparator && c != '\\') {
                        strb.append('\\');
                    }
                    strb.append(c);
                }
                escape = false;
            } else if (c != '\\') {
                strb.append(c);
                if (c == sep) {
                    break;
                }
            } else {
                escape = true;
            }
            index++;
        }
        return index;
    }

    private static int readUnicodeChar(StringBuilder strb, CharSequence str, int begin) {
        char xc = '\u0000';
        int bits = 12;
        for (int offset = 0; offset < 4; offset++) {
            int value;
            char c = str.charAt(begin + offset);
            if (c >= '0' && c <= '9') {
                value = c - 48;
            } else if (c >= 'a' && c <= 'h') {
                value = (c - 97) + 10;
            } else if (c < 'A' || c > 'H') {
                return 0;
            } else {
                value = (c - 65) + 10;
            }
            xc = (char) ((value << bits) | xc);
            bits -= 4;
        }
        strb.append(xc);
        return 4;
    }

    public static String escapeString(String str, char delim) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        StringBuilder strb = new StringBuilder(length + 2);
        strb.append(delim);
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\u0000':
                    break;
                case '\b':
                    strb.append("\\b");
                    break;
                case '\t':
                    strb.append("\\t");
                    break;
                case '\n':
                    strb.append("\\n");
                    break;
                case '\f':
                    strb.append("\\f");
                    break;
                case '\r':
                    strb.append("\\r");
                    break;
                case '\"':
                    strb.append("\\\"");
                    break;
                case '\'':
                    strb.append("\\'");
                    break;
                case '\\':
                    strb.append("\\\\");
                    break;
                default:
                    if (c < ' ' || c > '') {
                        strb.append('\\');
                        strb.append('u');
                        String hex = Integer.toHexString(c);
                        for (int h = hex.length(); h < 4; h++) {
                            strb.append('0');
                        }
                        strb.append(hex);
                        break;
                    }
                    strb.append(c);
                    break;
            }
        }
        strb.append(delim);
        return strb.toString();
    }
}
