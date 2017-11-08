package com.huawei.cspcommon.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.util.HwLog;
import com.android.internal.util.ArrayUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.HanziToPinyin.Token;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class DialerHighlighter {
    static final int[] ENGLISH_DIALPAD_MAP = new int[]{2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9};
    private static char IGNORE_CHARS = '-';
    private static HanziToPinyin mHanziToPinyin = null;
    private static HanziToBopomofo mHanziToZhuyin = null;
    private Activity mActivity;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private int mHighlightColorInt;

    class HighlightChineseItem implements Runnable {
        private int mEnd;
        private String mKey;
        private Spannable mSpannable;
        private int mStart;
        private ArrayList<Token> mTokens;

        public HighlightChineseItem(int aStart, int aEnd, Spannable aSpannable, String aKey, ArrayList<Token> aTokens) {
            this.mStart = aStart;
            this.mEnd = aEnd;
            this.mSpannable = aSpannable;
            this.mKey = aKey;
            this.mTokens = aTokens;
        }

        public void run() {
            StringBuffer sb = new StringBuffer();
            Integer[] wordsIndexes = DialerHighlighter.this.findIndexForWords(this.mKey, sb);
            if (wordsIndexes != null && this.mSpannable != null) {
                int keyLength = this.mKey.length();
                String displayName = this.mSpannable.toString();
                int length = displayName.length();
                if (wordsIndexes.length <= length && wordsIndexes.length <= this.mTokens.size()) {
                    int index = 0;
                    int tempStart = 0;
                    int charsToHighlight = (this.mEnd - this.mStart) + 1;
                    int offset = 0;
                    while (index < wordsIndexes.length) {
                        if (wordsIndexes[index].intValue() == this.mStart) {
                            tempStart = index + offset;
                            break;
                        }
                        if (((Token) this.mTokens.get(index)).type == 1 || ((Token) this.mTokens.get(index)).type == 4) {
                            offset += ((Token) this.mTokens.get(index)).source.length() - 1;
                        }
                        index++;
                        while (index + offset < length && (displayName.charAt(index + offset) == ' ' || displayName.charAt(index + offset) == '.')) {
                            offset++;
                        }
                    }
                    int tempEnd = tempStart;
                    int i = tempStart;
                    while (charsToHighlight > 0 && i < length && index < wordsIndexes.length) {
                        int tokenType = ((Token) this.mTokens.get(index)).type;
                        char c = displayName.charAt(i);
                        if (c == ' ' || c == '.') {
                            tempEnd++;
                        } else if (tokenType == 1) {
                            int sourceL = ((Token) this.mTokens.get(index)).source.length();
                            if (charsToHighlight > sourceL) {
                                charsToHighlight -= sourceL + 1;
                                tempEnd += sourceL;
                            } else {
                                tempEnd += charsToHighlight;
                                charsToHighlight = 0;
                            }
                            index++;
                            i += sourceL - 1;
                        } else if (tokenType == 2 || (SortUtils.isTWChineseDialpadShow() && tokenType == 3)) {
                            int intValue;
                            if (index < wordsIndexes.length - 1) {
                                intValue = wordsIndexes[index + 1].intValue() - wordsIndexes[index].intValue();
                            } else {
                                intValue = keyLength - wordsIndexes[index].intValue();
                            }
                            charsToHighlight -= intValue;
                            index++;
                            tempEnd++;
                        } else {
                            tempEnd++;
                        }
                        i++;
                    }
                    if (tempStart > -1 && tempEnd <= length) {
                        this.mSpannable.setSpan(new ForegroundColorSpan(DialerHighlighter.this.mHighlightColorInt), tempStart, tempEnd, 33);
                    }
                }
            }
        }
    }

    class HighlightItem implements Runnable {
        private int mEnd;
        private Spannable mSpannable;
        private int mStart;

        public HighlightItem(int aStart, int aEnd, Spannable aSpannable) {
            this.mStart = aStart;
            this.mEnd = aEnd;
            this.mSpannable = aSpannable;
        }

        public void run() {
            if (this.mSpannable != null) {
                int length = this.mSpannable.toString().length();
                if (this.mStart > -1 && this.mEnd <= length && this.mStart <= this.mEnd) {
                    this.mSpannable.setSpan(new ForegroundColorSpan(DialerHighlighter.this.mHighlightColorInt), this.mStart, this.mEnd, 33);
                }
            }
        }
    }

    class RequestItem implements Runnable {
        public Context context;
        String input;
        public PhoneItem item;
        public Spannable mSpannableName;
        public Spannable mSpannableNumber;
        public TextView nameView;
        public TextView numberView;

        public RequestItem(PhoneItem item, TextView nameView, TextView numberView, String input, Context context) {
            this.item = item;
            this.nameView = nameView;
            if (nameView.getText() instanceof Spannable) {
                this.mSpannableName = (Spannable) nameView.getText();
            }
            this.numberView = numberView;
            if (numberView.getText() instanceof Spannable) {
                this.mSpannableNumber = (Spannable) numberView.getText();
            }
            this.input = input;
            this.context = context;
        }

        public void run() {
            Spannable data;
            if (this.item.mType == 0 || this.item.mType == -3) {
                data = this.mSpannableNumber;
                if (data == null || data.length() == 0) {
                    data = (Spannable) this.numberView.getText();
                }
                if (this.item.mNumber != null) {
                    if (EmuiFeatureManager.isSupportRussiaNumberRelevance() && EmuiFeatureManager.isRussiaNumberSearchEnabled()) {
                        String numberFormatted = PhoneNumberUtils.stripSeparators(this.item.mNumber);
                        if (numberFormatted.indexOf(PhoneNumberUtils.stripSeparators(this.input)) < 0 && (numberFormatted.startsWith("+7") || numberFormatted.startsWith("8"))) {
                            return;
                        }
                    }
                    DialerHighlighter.this.highlightMatchNumber(this.item.mNumber, this.input, data, this.nameView, this.item);
                }
                return;
            }
            String name;
            int mode = 0;
            ArrayList tokens = null;
            Locale locale = Locale.getDefault();
            if (locale.getLanguage().equals("ja")) {
                if (this.item.mSortKey != null) {
                    name = DialerHighlighter.convertToDialMapEX(this.item.mSortKey, this.context);
                } else {
                    name = DialerHighlighter.convertToDialMapEX(this.item.mName, this.context);
                }
            } else if (this.item.mSortKey == null || !this.item.mName.toLowerCase(locale).equals(this.item.mSortKey.toLowerCase(locale))) {
                if (SortUtils.isContainChinese(this.item.mName)) {
                    Object[] objs;
                    boolean tw_dialpad_check = SortUtils.isTWChineseDialpadShow();
                    if (tw_dialpad_check) {
                        objs = DialerHighlighter.convertToZhuyin(this.item.mName);
                    } else {
                        objs = DialerHighlighter.this.convertToPinyinDialMap(this.item.mName);
                    }
                    if (!ArrayUtils.isEmpty(objs)) {
                        ArrayList<Token> tokens2 = objs[0];
                        name = (String) objs[1];
                        if (tw_dialpad_check) {
                            name = DialerHighlighter.convertToDialMapEX(name, this.context);
                        }
                        for (Token t : tokens2) {
                            if (t.type == 2 || (tw_dialpad_check && t.type == 3)) {
                                mode = 1;
                                break;
                            }
                        }
                    } else {
                        return;
                    }
                } else if (DialerHighlighter.mHanziToPinyin != null) {
                    name = DialerHighlighter.convertToDialMapEX(DialerHighlighter.mHanziToPinyin.getNormalizedStr(this.item.mName), this.context);
                } else {
                    HwLog.d("DialerHighlighter", "mHanziToPinyin is null");
                    name = DialerHighlighter.convertToDialMapEX(this.item.mName, this.context);
                }
            } else if (DialerHighlighter.mHanziToPinyin != null) {
                name = DialerHighlighter.convertToDialMapEX(DialerHighlighter.mHanziToPinyin.getNormalizedStr(this.item.mName), this.context);
            } else {
                HwLog.d("DialerHighlighter", "mHanziToPinyin is null");
                name = DialerHighlighter.convertToDialMapEX(this.item.mName, this.context);
            }
            if (!(this.item.mType == 2 || this.item.mType == 1 || this.item.mType == 3 || this.item.mType == -1)) {
                if (this.item.mType == -2) {
                }
            }
            data = this.mSpannableName;
            if ((data == null || data.length() == 0) && (this.nameView.getText() instanceof Spannable)) {
                data = (Spannable) this.nameView.getText();
            }
            if (data != null) {
                DialerHighlighter.this.highlightMatchInitials(name, this.input, data, this.item.mType, this.item, mode, tokens);
            }
        }
    }

    static {
        if (!CommonUtilMethods.getIsLiteFeatureProducts()) {
            dialerHighlighterInit();
        }
    }

    public static void dialerHighlighterInit() {
        new Thread() {
            public void run() {
                DialerHighlighter.mHanziToPinyin = HanziToPinyin.getInstance();
                DialerHighlighter.mHanziToZhuyin = HanziToBopomofo.getInstance();
            }
        }.start();
    }

    public DialerHighlighter(Activity aActivity) {
        this.mActivity = aActivity;
        if (this.mActivity != null) {
            int color = ImmersionUtils.getControlColor(this.mActivity.getResources());
            if (color != 0) {
                this.mHighlightColorInt = color;
                return;
            } else {
                this.mHighlightColorInt = this.mActivity.getResources().getColor(R.color.people_app_theme_color);
                return;
            }
        }
        this.mHighlightColorInt = Color.rgb(51, 179, 226);
    }

    public Object[] convertToPinyinDialMap(String aHanzi) {
        if (mHanziToPinyin == null) {
            return null;
        }
        ArrayList<Token> tokens = mHanziToPinyin.get(aHanzi);
        if (tokens.size() <= 0) {
            return null;
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
        return new Object[]{tokens, convertToDialMap(sb.toString())};
    }

    public static Object[] convertToPinyin(String aHanzi) {
        if (mHanziToPinyin == null || aHanzi == null) {
            return null;
        }
        if (mHanziToPinyin.getTokensWithName(aHanzi).getTokens().size() <= 0) {
            return null;
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

    public static String cleanNumberForDialpad(String aInput, boolean aAddSpace) {
        if (aInput == null) {
            return null;
        }
        char[] temp = aInput.toCharArray();
        char[] res = new char[temp.length];
        int index = 0;
        Pattern pattern = Pattern.compile("[N,.()/+;*#]");
        int i = 0;
        while (i < temp.length) {
            if ((temp[i] - 48 > -1 && temp[i] - 48 < 10) || pattern.matcher(String.valueOf(temp[i])).matches()) {
                res[index] = temp[i];
                index++;
            } else if (aAddSpace) {
                res[index] = ' ';
                index++;
            }
            i++;
        }
        if (index < temp.length) {
            res[index] = '\u0000';
        }
        return String.valueOf(res, 0, index);
    }

    public static String cleanNumber(String aInput, boolean aAddSpace) {
        if (aInput == null) {
            return null;
        }
        char[] temp = aInput.toCharArray();
        char[] res = new char[temp.length];
        int index = 0;
        int i = 0;
        while (i < temp.length) {
            if ((temp[i] - 48 > -1 && temp[i] - 48 < 10) || temp[i] == '*' || temp[i] == '#' || temp[i] == '+') {
                res[index] = temp[i];
                index++;
            } else if (aAddSpace) {
                res[index] = ' ';
                index++;
            }
            i++;
        }
        if (index < temp.length) {
            res[index] = '\u0000';
        }
        return String.valueOf(res, 0, index);
    }

    public void highlightText(PhoneItem dialItem, TextView nameView, TextView numberView, String input, Context context) {
        this.mExecutorService.execute(new RequestItem(dialItem, nameView, numberView, input, context));
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
            temp = getMinPositive(aTarget.indexOf(HwCustPreloadContacts.EMPTY_STRING), aTarget.indexOf(HwCustPreloadContacts.EMPTY_STRING));
            if (temp != -1) {
                temp++;
                while (temp < length && (aTarget.charAt(temp) == '.' || aTarget.charAt(temp) == ' ')) {
                    temp++;
                }
                if (temp == 0) {
                    break;
                }
                aStart += temp;
                if (aStart < lOriginalLength) {
                    normDialMapBuffer.append(tempString.charAt(aStart));
                    indexes.add(Integer.valueOf(aStart));
                }
                aTarget = aTarget.substring(temp);
                length = aTarget.length();
                if (temp == 0) {
                    break;
                }
            } else {
                break;
            }
        }
        if (indexes.size() <= 0) {
            return null;
        }
        Integer[] tempArray = new Integer[indexes.size()];
        indexes.toArray(tempArray);
        return tempArray;
    }

    private void highlightMatchInitials(String aTarget, String input, Spannable aNameData, int aType, PhoneItem item, int aMode, ArrayList<Token> aTokens) {
        if (input != null && input.length() != 0) {
            StringBuffer newMatchPinyin = new StringBuffer();
            int[] startIndex = SearchMatch.getMatchIndex(aTarget, input, item.mName, true, newMatchPinyin, this.mActivity);
            if (startIndex != null) {
                if (newMatchPinyin.length() != 0) {
                    aTarget = newMatchPinyin.toString();
                }
                int targetLength = aTarget.length();
                if (startIndex.length != 0) {
                    for (int i = 0; i < startIndex.length / 2; i++) {
                        int begin = startIndex[i * 2];
                        int end = startIndex[(i * 2) + 1];
                        if (begin >= 0 && end >= begin && end < targetLength) {
                            if (aMode == 0) {
                                this.mActivity.runOnUiThread(new HighlightItem(begin, end + 1, aNameData));
                            } else {
                                this.mActivity.runOnUiThread(new HighlightChineseItem(begin, end, aNameData, aTarget, aTokens));
                            }
                        }
                    }
                } else if (aMode == 0) {
                    hightLightMatchNotPinyin(aTarget, input, aNameData, aType, aMode);
                }
            }
        }
    }

    public static String convertToDialMap(String aInput) {
        String lower = aInput.toLowerCase(Locale.getDefault());
        StringBuffer sb = new StringBuffer();
        for (char c : lower.toCharArray()) {
            if (c <= '`' || c >= '{') {
                sb.append(c);
            } else {
                sb.append(ENGLISH_DIALPAD_MAP[c - 97]);
            }
        }
        return sb.toString();
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
                sb.append(getDigit(alphabetArrayLower, digitArray, toSectionChar(c)));
            } else {
                sb.append(ENGLISH_DIALPAD_MAP[c - 97]);
            }
        }
        return sb.toString();
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

    private static char toSectionChar(char ch) {
        if (!"ja".equals(Locale.getDefault().getLanguage()) || ((ch < '぀' || ch > 'ゟ') && (ch < '゠' || ch > 'ヿ'))) {
            return ch;
        }
        return getJapanaeseKanaSection(String.valueOf(ch)).charAt(0);
    }

    private static String getJapanaeseKanaSection(String ch) {
        char c = ch.charAt(0);
        if ((c > 'ヺ' && c < 'ヿ') || c == '゠') {
            return "#";
        }
        if (c == 'ヿ') {
            return "あ";
        }
        int index;
        String[] kanas = new String[]{"あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ"};
        Collator coll = Collator.getInstance(Locale.JAPANESE);
        coll.setStrength(0);
        int low = 0;
        int high = kanas.length - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (coll.equals(ch, kanas[middle])) {
                return kanas[middle];
            }
            if (coll.compare(ch, kanas[middle]) < 0) {
                high = middle - 1;
            } else {
                low = middle + 1;
            }
        }
        if (low < high) {
            index = low;
        } else {
            index = high;
        }
        if (index < 0) {
            return kanas[0];
        }
        if (index >= kanas.length) {
            return kanas[kanas.length - 1];
        }
        return kanas[index];
    }

    private int getMinPositive(int aFirst, int aSecond) {
        if (aFirst < 0) {
            if (aSecond > -1) {
                return aSecond;
            }
            return -1;
        } else if (aSecond < 0) {
            return -1;
        } else {
            if (aFirst > aSecond) {
                return aSecond;
            }
            return aFirst;
        }
    }

    private int getInBraceCount(int start, int end, String target) {
        int count = 0;
        while (true) {
            int start2 = start + 1;
            char c = target.charAt(start);
            if ('(' == c) {
                count++;
            } else if (')' == c) {
                count--;
            }
            if (start2 >= end) {
                return count;
            }
            start = start2;
        }
    }

    private int getStartIndex(int start, int inBraces, String target) {
        if (start < 1) {
            return start;
        }
        while ('(' == target.charAt(start - 1)) {
            start--;
            inBraces++;
            if (start > 0) {
                if (inBraces >= 0) {
                }
            }
            return start;
        }
        return start;
    }

    private int getEndIndex(int end, int inBraces, String target) {
        if (end >= target.length() - 1) {
            return end;
        }
        while (')' == target.charAt(end + 1)) {
            inBraces--;
            end++;
            if (end < target.length() - 1) {
                if (inBraces <= 0) {
                }
            }
            return end;
        }
        return end;
    }

    private void highlightMatchNumber(String aTarget, String input, Spannable aNumberSpannableData, TextView aNameView, PhoneItem item) {
        int aTargetLength = aTarget.length();
        int aInputLength = input.length();
        int end = 0;
        int indexInput = 0;
        if (aInputLength != 0) {
            int tempIndex = aTarget.indexOf(input.charAt(0));
            if (tempIndex != -1) {
                int start = tempIndex;
                do {
                    char targetChar = aTarget.charAt(tempIndex);
                    if (targetChar != input.charAt(indexInput)) {
                        if (islegalChar(targetChar)) {
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
                                return;
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
                if (aTarget.contains("(") && aTarget.contains(")")) {
                    int inBraces = getInBraceCount(start, end, aTarget);
                    if (inBraces < 0) {
                        start = getStartIndex(start, inBraces, aTarget);
                    } else if (inBraces > 0) {
                        end = getEndIndex(end, inBraces, aTarget);
                    }
                    while (true) {
                        int tempStart = getStartIndex(start, -1, aTarget);
                        int tempEnd = getEndIndex(end, 1, aTarget);
                        if (tempStart == start || tempEnd == end) {
                            break;
                        }
                        start = tempStart;
                        end = tempEnd;
                    }
                }
                end++;
                if (start < end && start > -1 && end <= aTargetLength) {
                    if (item.equals(aNameView.getTag())) {
                        this.mActivity.runOnUiThread(new HighlightItem(start, end, aNumberSpannableData));
                    }
                }
            }
        }
    }

    private boolean islegalChar(char targetChar) {
        if (IGNORE_CHARS == targetChar || '(' == targetChar || ')' == targetChar || ' ' == targetChar || ',' == targetChar || ';' == targetChar || '#' == targetChar || '+' == targetChar || '.' == targetChar || '/' == targetChar || '*' == targetChar) {
            return false;
        }
        return true;
    }

    public static void loadSelf() {
    }

    private void hightLightMatchNotPinyin(String aTarget, String input, Spannable aNameData, int aType, int aMode) {
        int queryIndex = aTarget.indexOf(input);
        int endIdx = 0;
        if (queryIndex > -1) {
            endIdx = queryIndex + input.length();
        } else {
            String aTargetClean = aTarget.replace(HwCustPreloadContacts.EMPTY_STRING, "");
            String aInputClean = input.replace(HwCustPreloadContacts.EMPTY_STRING, "");
            queryIndex = aTargetClean.indexOf(aInputClean);
            if (queryIndex > -1) {
                String nameStr = aNameData.toString();
                int nameStrLen = nameStr.length();
                int inputLen = aInputClean.length();
                int countOfSpaces = 0;
                int countOfNonSpace = 0;
                for (int i = queryIndex; i < nameStrLen; i++) {
                    if (nameStr.charAt(i) == ' ') {
                        countOfSpaces++;
                    } else {
                        countOfNonSpace++;
                    }
                    if (countOfNonSpace >= inputLen) {
                        break;
                    }
                }
                endIdx = (queryIndex + inputLen) + countOfSpaces;
            }
        }
        if (queryIndex > -1) {
            int maxEndIdx = aNameData.toString().length();
            if (endIdx > maxEndIdx) {
                endIdx = maxEndIdx;
            }
            this.mActivity.runOnUiThread(new HighlightItem(queryIndex, endIdx, aNameData));
        }
    }
}
