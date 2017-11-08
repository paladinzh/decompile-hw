package com.android.vcard;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import com.android.vcard.exception.VCardException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VCardUtils {
    private static final String HW_OPTA = SystemProperties.get("ro.config.hw_opta", "");
    private static final String HW_OPTB = SystemProperties.get("ro.config.hw_optb", "");
    private static final int[] sEscapeIndicatorsV30 = new int[]{58, 59, 44, 32};
    private static final int[] sEscapeIndicatorsV40 = new int[]{59, 58};
    private static final Map<Integer, String> sKnownImPropNameMap_ItoS = new HashMap();
    private static final Map<String, Integer> sKnownPhoneTypeMap_StoI = new HashMap();
    private static final Map<Integer, String> sKnownPhoneTypesMap_ItoS = new HashMap();
    private static final Set<String> sMobilePhoneLabelSet = new HashSet(Arrays.asList(new String[]{"MOBILE", "携帯電話", "携帯", "ケイタイ", "ｹｲﾀｲ"}));
    private static final Set<String> sPhoneTypesUnknownToContactsSet = new HashSet();
    private static final Set<Character> sUnAcceptableAsciiInV21WordSet = new HashSet(Arrays.asList(new Character[]{Character.valueOf('['), Character.valueOf(']'), Character.valueOf('='), Character.valueOf(':'), Character.valueOf('.'), Character.valueOf(','), Character.valueOf(' ')}));

    private static class DecoderException extends Exception {
        public DecoderException(String pMessage) {
            super(pMessage);
        }
    }

    public static class PhoneNumberUtilsPort {
        public static String formatNumber(String source, int defaultFormattingType) {
            if ("26".equals(VCardUtils.HW_OPTA) && "392".equals(VCardUtils.HW_OPTB)) {
                Log.w("vCard", "E-mobile product, ust the japan number format");
                defaultFormattingType = 2;
            }
            SpannableStringBuilder text = new SpannableStringBuilder(source);
            PhoneNumberUtils.formatNumber(text, defaultFormattingType);
            return text.toString();
        }
    }

    private static class QuotedPrintableCodecPort {
        private static byte ESCAPE_CHAR = (byte) 61;

        private QuotedPrintableCodecPort() {
        }

        public static final byte[] decodeQuotedPrintable(byte[] bytes) throws DecoderException {
            if (bytes == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int i = 0;
            while (i < bytes.length) {
                byte b = bytes[i];
                if (b == ESCAPE_CHAR) {
                    i++;
                    int u = Character.digit((char) bytes[i], 16);
                    i++;
                    int l = Character.digit((char) bytes[i], 16);
                    if (u == -1 || l == -1) {
                        throw new DecoderException("Invalid quoted-printable encoding");
                    }
                    try {
                        buffer.write((char) ((u << 4) + l));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new DecoderException("Invalid quoted-printable encoding");
                    }
                }
                buffer.write(b);
                i++;
            }
            return buffer.toByteArray();
        }
    }

    public static class TextUtilsPort {
        public static boolean isPrintableAscii(char c) {
            if ((' ' <= c && c <= '~') || c == '\r' || c == '\n') {
                return true;
            }
            return false;
        }

        public static boolean isPrintableAsciiOnly(CharSequence str) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (!isPrintableAscii(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    static {
        sKnownPhoneTypesMap_ItoS.put(Integer.valueOf(9), "CAR");
        sKnownPhoneTypeMap_StoI.put("CAR", Integer.valueOf(9));
        sKnownPhoneTypesMap_ItoS.put(Integer.valueOf(6), "PAGER");
        sKnownPhoneTypeMap_StoI.put("PAGER", Integer.valueOf(6));
        sKnownPhoneTypesMap_ItoS.put(Integer.valueOf(11), "ISDN");
        sKnownPhoneTypeMap_StoI.put("ISDN", Integer.valueOf(11));
        sKnownPhoneTypeMap_StoI.put("HOME", Integer.valueOf(1));
        sKnownPhoneTypeMap_StoI.put("WORK", Integer.valueOf(3));
        sKnownPhoneTypeMap_StoI.put("CELL", Integer.valueOf(2));
        sKnownPhoneTypeMap_StoI.put("OTHER", Integer.valueOf(7));
        sKnownPhoneTypeMap_StoI.put("CALLBACK", Integer.valueOf(8));
        sKnownPhoneTypeMap_StoI.put("COMPANY-MAIN", Integer.valueOf(10));
        sKnownPhoneTypeMap_StoI.put("RADIO", Integer.valueOf(14));
        sKnownPhoneTypeMap_StoI.put("TTY-TDD", Integer.valueOf(16));
        sKnownPhoneTypeMap_StoI.put("ASSISTANT", Integer.valueOf(19));
        sKnownPhoneTypeMap_StoI.put("VOICE", Integer.valueOf(7));
        sPhoneTypesUnknownToContactsSet.add("MODEM");
        sPhoneTypesUnknownToContactsSet.add("MSG");
        sPhoneTypesUnknownToContactsSet.add("BBS");
        sPhoneTypesUnknownToContactsSet.add("VIDEO");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(0), "X-AIM");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(1), "X-MSN");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(2), "X-YAHOO");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(3), "X-SKYPE-USERNAME");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(5), "X-GOOGLE-TALK");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(6), "X-ICQ");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(7), "X-JABBER");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(4), "X-QQ");
        sKnownImPropNameMap_ItoS.put(Integer.valueOf(8), "X-NETMEETING");
    }

    public static String getPhoneTypeString(Integer type) {
        return (String) sKnownPhoneTypesMap_ItoS.get(type);
    }

    public static boolean isMobilePhoneLabel(String label) {
        return !"_AUTO_CELL".equals(label) ? sMobilePhoneLabelSet.contains(label) : true;
    }

    public static boolean isValidInV21ButUnknownToContactsPhoteType(String label) {
        return sPhoneTypesUnknownToContactsSet.contains(label);
    }

    public static String getPropertyNameForIm(int protocol) {
        return (String) sKnownImPropNameMap_ItoS.get(Integer.valueOf(protocol));
    }

    public static String[] sortNameElements(int nameOrder, String familyName, String middleName, String givenName) {
        String[] list = new String[3];
        switch (VCardConfig.getNameOrderType(nameOrder)) {
            case 4:
                list[0] = middleName;
                list[1] = givenName;
                list[2] = familyName;
                break;
            case 8:
                if (containsOnlyPrintableAscii(familyName)) {
                    if (containsOnlyPrintableAscii(givenName)) {
                        list[0] = givenName;
                        list[1] = middleName;
                        list[2] = familyName;
                        break;
                    }
                }
                list[0] = familyName;
                list[1] = middleName;
                list[2] = givenName;
                break;
            default:
                list[0] = givenName;
                list[1] = middleName;
                list[2] = familyName;
                break;
        }
        return list;
    }

    public static int getPhoneNumberFormat(int vcardType) {
        if (VCardConfig.isJapaneseDevice(vcardType)) {
            return 2;
        }
        return 1;
    }

    public static String constructNameFromElements(int nameOrder, String familyName, String middleName, String givenName) {
        return constructNameFromElements(nameOrder, familyName, middleName, givenName, null, null);
    }

    public static String constructNameFromElements(int nameOrder, String familyName, String middleName, String givenName, String prefix, String suffix) {
        StringBuilder builder = new StringBuilder();
        String[] nameList = sortNameElements(nameOrder, familyName, middleName, givenName);
        boolean first = true;
        if (!TextUtils.isEmpty(prefix)) {
            first = false;
            builder.append(prefix);
        }
        for (String namePart : nameList) {
            if (!TextUtils.isEmpty(namePart)) {
                if (first) {
                    first = false;
                } else {
                    builder.append(' ');
                }
                builder.append(namePart);
            }
        }
        if (!TextUtils.isEmpty(suffix)) {
            if (!first) {
                builder.append(' ');
            }
            builder.append(suffix);
        }
        return builder.toString();
    }

    public static List<String> constructListFromValue(String value, int vcardType) {
        List<String> list = new ArrayList();
        StringBuilder builder = new StringBuilder();
        int length = value.length();
        int i = 0;
        while (i < length) {
            char ch = value.charAt(i);
            if (ch == '\\' && i < length - 1) {
                String unescapedString;
                char nextCh = value.charAt(i + 1);
                if (VCardConfig.isVersion40(vcardType)) {
                    unescapedString = VCardParserImpl_V40.unescapeCharacter(nextCh);
                } else if (VCardConfig.isVersion30(vcardType)) {
                    unescapedString = VCardParserImpl_V30.unescapeCharacter(nextCh);
                } else {
                    if (!VCardConfig.isVersion21(vcardType)) {
                        Log.w("vCard", "Unknown vCard type");
                    }
                    unescapedString = VCardParserImpl_V21.unescapeCharacter(nextCh);
                }
                if (unescapedString != null) {
                    builder.append(unescapedString);
                    i++;
                } else {
                    builder.append(ch);
                }
            } else if (ch == ';') {
                list.add(builder.toString());
                builder = new StringBuilder();
            } else {
                builder.append(ch);
            }
            i++;
        }
        list.add(builder.toString());
        return list;
    }

    public static boolean containsOnlyPrintableAscii(String... values) {
        if (values == null) {
            return true;
        }
        return containsOnlyPrintableAscii(Arrays.asList(values));
    }

    public static boolean containsOnlyPrintableAscii(Collection<String> values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value) && !TextUtilsPort.isPrintableAsciiOnly(value)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsOnlyNonCrLfPrintableAscii(String... values) {
        if (values == null) {
            return true;
        }
        return containsOnlyNonCrLfPrintableAscii(Arrays.asList(values));
    }

    public static boolean containsOnlyNonCrLfPrintableAscii(Collection<String> values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                int length = value.length();
                for (int i = 0; i < length; i = value.offsetByCodePoints(i, 1)) {
                    int c = value.codePointAt(i);
                    if (32 > c || c > 126) {
                        return false;
                    }
                }
                continue;
            }
        }
        return true;
    }

    public static boolean containsOnlyAlphaDigitHyphen(String... values) {
        if (values == null) {
            return true;
        }
        return containsOnlyAlphaDigitHyphen(Arrays.asList(values));
    }

    public static boolean containsOnlyAlphaDigitHyphen(Collection<String> values) {
        if (values == null) {
            return true;
        }
        for (String str : values) {
            if (!TextUtils.isEmpty(str)) {
                int length = str.length();
                for (int i = 0; i < length; i = str.offsetByCodePoints(i, 1)) {
                    int codepoint = str.codePointAt(i);
                    if ((97 > codepoint || codepoint >= 123) && ((65 > codepoint || codepoint >= 91) && ((48 > codepoint || codepoint >= 58) && codepoint != 45))) {
                        return false;
                    }
                }
                continue;
            }
        }
        return true;
    }

    public static boolean containsOnlyWhiteSpaces(String... values) {
        if (values == null) {
            return true;
        }
        return containsOnlyWhiteSpaces(Arrays.asList(values));
    }

    public static boolean containsOnlyWhiteSpaces(Collection<String> values) {
        if (values == null) {
            return true;
        }
        for (String str : values) {
            if (!TextUtils.isEmpty(str)) {
                int length = str.length();
                for (int i = 0; i < length; i = str.offsetByCodePoints(i, 1)) {
                    if (!Character.isWhitespace(str.codePointAt(i))) {
                        return false;
                    }
                }
                continue;
            }
        }
        return true;
    }

    public static boolean isV21Word(String value) {
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        int length = value.length();
        int i = 0;
        while (i < length) {
            int c = value.codePointAt(i);
            if (32 > c || c > 126 || sUnAcceptableAsciiInV21WordSet.contains(Character.valueOf((char) c))) {
                return false;
            }
            i = value.offsetByCodePoints(i, 1);
        }
        return true;
    }

    public static String toStringAsV30ParamValue(String value) {
        return toStringAsParamValue(value, sEscapeIndicatorsV30);
    }

    public static String toStringAsV40ParamValue(String value) {
        return toStringAsParamValue(value, sEscapeIndicatorsV40);
    }

    private static String toStringAsParamValue(String value, int[] escapeIndicators) {
        if (TextUtils.isEmpty(value)) {
            value = "";
        }
        StringBuilder builder = new StringBuilder();
        int length = value.length();
        boolean needQuote = false;
        for (int i = 0; i < length; i = value.offsetByCodePoints(i, 1)) {
            int codePoint = value.codePointAt(i);
            if (codePoint >= 32 && codePoint != 34) {
                builder.appendCodePoint(codePoint);
                for (int indicator : escapeIndicators) {
                    if (codePoint == indicator) {
                        needQuote = true;
                        break;
                    }
                }
            }
        }
        String result = builder.toString();
        if (!result.isEmpty()) {
            if (!containsOnlyWhiteSpaces(result)) {
                if (needQuote) {
                    return '\"' + result + '\"';
                }
                return result;
            }
        }
        return "";
    }

    public static String toHalfWidthString(String orgString) {
        if (TextUtils.isEmpty(orgString)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int length = orgString.length();
        int i = 0;
        while (i < length) {
            char ch = orgString.charAt(i);
            String halfWidthText = JapaneseUtils.tryGetHalfWidthText(ch);
            if (halfWidthText != null) {
                builder.append(halfWidthText);
            } else {
                builder.append(ch);
            }
            i = orgString.offsetByCodePoints(i, 1);
        }
        return builder.toString();
    }

    public static String guessImageType(byte[] input) {
        if (input == null) {
            return null;
        }
        if (input.length >= 3 && input[0] == (byte) 71 && input[1] == (byte) 73 && input[2] == (byte) 70) {
            return "GIF";
        }
        if (input.length >= 4 && input[0] == (byte) -119 && input[1] == (byte) 80 && input[2] == (byte) 78 && input[3] == (byte) 71) {
            return "PNG";
        }
        if (input.length >= 2 && input[0] == (byte) -1 && input[1] == (byte) -40) {
            return "JPEG";
        }
        return null;
    }

    public static boolean areAllEmpty(String... values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    public static boolean appearsLikeAndroidVCardQuotedPrintable(String value) {
        int remainder = value.length() % 3;
        if (value.length() < 2 || (remainder != 1 && remainder != 0)) {
            return false;
        }
        for (int i = 0; i < value.length(); i += 3) {
            if (value.charAt(i) != '=') {
                return false;
            }
        }
        return true;
    }

    public static String parseQuotedPrintable(String value, boolean strictLineBreaking, String sourceCharset, String targetCharset) {
        byte[] rawBytes;
        byte[] decodedBytes;
        StringBuilder builder = new StringBuilder();
        int length = value.length();
        int i = 0;
        while (i < length) {
            char ch = value.charAt(i);
            if (ch == '=' && i < length - 1) {
                char nextCh = value.charAt(i + 1);
                if (nextCh == ' ' || nextCh == '\t') {
                    builder.append(nextCh);
                    i++;
                    i++;
                }
            }
            builder.append(ch);
            i++;
        }
        String quotedPrintable = builder.toString();
        String[] lines;
        if (strictLineBreaking) {
            lines = quotedPrintable.split("\r\n");
        } else {
            builder = new StringBuilder();
            length = quotedPrintable.length();
            ArrayList<String> list = new ArrayList();
            i = 0;
            while (i < length) {
                ch = quotedPrintable.charAt(i);
                if (ch == '\n') {
                    list.add(builder.toString());
                    builder = new StringBuilder();
                } else if (ch == '\r') {
                    list.add(builder.toString());
                    builder = new StringBuilder();
                    if (i < length - 1 && quotedPrintable.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    builder.append(ch);
                }
                i++;
            }
            String lastLine = builder.toString();
            if (lastLine.length() > 0) {
                list.add(lastLine);
            }
            lines = (String[]) list.toArray(new String[0]);
        }
        builder = new StringBuilder();
        for (String line : lines) {
            String line2;
            if (line2.endsWith("=")) {
                line2 = line2.substring(0, line2.length() - 1);
            }
            builder.append(line2);
        }
        String rawString = builder.toString();
        if (TextUtils.isEmpty(rawString)) {
            Log.w("vCard", "Given raw string is empty.");
        }
        try {
            rawBytes = rawString.getBytes(sourceCharset);
        } catch (UnsupportedEncodingException e) {
            Log.w("vCard", "Failed to decode: " + sourceCharset);
            rawBytes = rawString.getBytes();
        }
        try {
            decodedBytes = QuotedPrintableCodecPort.decodeQuotedPrintable(rawBytes);
        } catch (DecoderException e2) {
            Log.e("vCard", "DecoderException is thrown.");
            decodedBytes = rawBytes;
        }
        try {
            return new String(decodedBytes, targetCharset);
        } catch (UnsupportedEncodingException e3) {
            Log.e("vCard", "Failed to encode: charset=" + targetCharset);
            return new String(decodedBytes);
        }
    }

    public static final VCardParser getAppropriateParser(int vcardType) throws VCardException {
        if (VCardConfig.isVersion21(vcardType)) {
            return new VCardParser_V21();
        }
        if (VCardConfig.isVersion30(vcardType)) {
            return new VCardParser_V30();
        }
        if (VCardConfig.isVersion40(vcardType)) {
            return new VCardParser_V40();
        }
        throw new VCardException("Version is not specified");
    }

    public static final String convertStringCharset(String originalString, String sourceCharset, String targetCharset) {
        if (sourceCharset.equalsIgnoreCase(targetCharset)) {
            return originalString;
        }
        ByteBuffer byteBuffer = Charset.forName(sourceCharset).encode(originalString);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        try {
            return new String(bytes, targetCharset);
        } catch (UnsupportedEncodingException e) {
            Log.e("vCard", "Failed to encode: charset=" + targetCharset);
            return null;
        }
    }

    private VCardUtils() {
    }
}
