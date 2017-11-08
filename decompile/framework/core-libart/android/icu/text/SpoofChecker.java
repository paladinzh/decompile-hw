package android.icu.text;

import android.icu.impl.ICUBinary;
import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2;
import android.icu.impl.Trie2Writable;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.ULocale;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpoofChecker {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final int ALL_CHECKS = -1;
    public static final int ANY_CASE = 8;
    public static final int CHAR_LIMIT = 64;
    @Deprecated
    public static final UnicodeSet INCLUSION = new UnicodeSet("[\\u0027\\u002D-\\u002E\\u003A\\u00B7\\u0375\\u058A\\u05F3-\\u05F4\\u06FD-\\u06FE\\u0F0B\\u200C-\\u200D\\u2010\\u2019\\u2027\\u30A0\\u30FB]").freeze();
    public static final int INVISIBLE = 32;
    static final int KEY_LENGTH_SHIFT = 29;
    static final int KEY_MULTIPLE_VALUES = 268435456;
    static final int MAGIC = 944111087;
    static final int MA_TABLE_FLAG = 134217728;
    @Deprecated
    public static final int MIXED_NUMBERS = 128;
    public static final int MIXED_SCRIPT_CONFUSABLE = 2;
    static final int ML_TABLE_FLAG = 67108864;
    @Deprecated
    public static final UnicodeSet RECOMMENDED = new UnicodeSet("[\\u0030-\\u0039\\u0041-\\u005A\\u005F\\u0061-\\u007A\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u0131\\u0134-\\u013E\\u0141-\\u0148\\u014A-\\u017E\\u018F\\u01A0-\\u01A1\\u01AF-\\u01B0\\u01CD-\\u01DC\\u01DE-\\u01E3\\u01E6-\\u01F0\\u01F4-\\u01F5\\u01F8-\\u021B\\u021E-\\u021F\\u0226-\\u0233\\u0259\\u02BB-\\u02BC\\u02EC\\u0300-\\u0304\\u0306-\\u030C\\u030F-\\u0311\\u0313-\\u0314\\u031B\\u0323-\\u0328\\u032D-\\u032E\\u0330-\\u0331\\u0335\\u0338-\\u0339\\u0342\\u0345\\u037B-\\u037D\\u0386\\u0388-\\u038A\\u038C\\u038E-\\u03A1\\u03A3-\\u03CE\\u03FC-\\u045F\\u048A-\\u0529\\u052E-\\u052F\\u0531-\\u0556\\u0559\\u0561-\\u0586\\u05B4\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0620-\\u063F\\u0641-\\u0655\\u0660-\\u0669\\u0670-\\u0672\\u0674\\u0679-\\u068D\\u068F-\\u06D3\\u06D5\\u06E5-\\u06E6\\u06EE-\\u06FC\\u06FF\\u0750-\\u07B1\\u08A0-\\u08AC\\u08B2\\u0901-\\u094D\\u094F-\\u0950\\u0956-\\u0957\\u0960-\\u0963\\u0966-\\u096F\\u0971-\\u0977\\u0979-\\u097F\\u0981-\\u0983\\u0985-\\u098C\\u098F-\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9\\u09BC-\\u09C4\\u09C7-\\u09C8\\u09CB-\\u09CE\\u09D7\\u09E0-\\u09E3\\u09E6-\\u09F1\\u0A01-\\u0A03\\u0A05-\\u0A0A\\u0A0F-\\u0A10\\u0A13-\\u0A28\\u0A2A-\\u0A30\\u0A32\\u0A35\\u0A38-\\u0A39\\u0A3C\\u0A3E-\\u0A42\\u0A47-\\u0A48\\u0A4B-\\u0A4D\\u0A5C\\u0A66-\\u0A74\\u0A81-\\u0A83\\u0A85-\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0\\u0AB2-\\u0AB3\\u0AB5-\\u0AB9\\u0ABC-\\u0AC5\\u0AC7-\\u0AC9\\u0ACB-\\u0ACD\\u0AD0\\u0AE0-\\u0AE3\\u0AE6-\\u0AEF\\u0B01-\\u0B03\\u0B05-\\u0B0C\\u0B0F-\\u0B10\\u0B13-\\u0B28\\u0B2A-\\u0B30\\u0B32-\\u0B33\\u0B35-\\u0B39\\u0B3C-\\u0B43\\u0B47-\\u0B48\\u0B4B-\\u0B4D\\u0B56-\\u0B57\\u0B5F-\\u0B61\\u0B66-\\u0B6F\\u0B71\\u0B82-\\u0B83\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99-\\u0B9A\\u0B9C\\u0B9E-\\u0B9F\\u0BA3-\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB9\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8\\u0BCA-\\u0BCD\\u0BD0\\u0BD7\\u0BE6-\\u0BEF\\u0C01-\\u0C03\\u0C05-\\u0C0C\\u0C0E-\\u0C10\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35-\\u0C39\\u0C3D-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D\\u0C55-\\u0C56\\u0C60-\\u0C61\\u0C66-\\u0C6F\\u0C82-\\u0C83\\u0C85-\\u0C8C\\u0C8E-\\u0C90\\u0C92-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CBC-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0CD5-\\u0CD6\\u0CE0-\\u0CE3\\u0CE6-\\u0CEF\\u0CF1-\\u0CF2\\u0D02-\\u0D03\\u0D05-\\u0D0C\\u0D0E-\\u0D10\\u0D12-\\u0D3A\\u0D3D-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4E\\u0D57\\u0D60-\\u0D61\\u0D66-\\u0D6F\\u0D7A-\\u0D7F\\u0D82-\\u0D83\\u0D85-\\u0D8E\\u0D91-\\u0D96\\u0D9A-\\u0DA5\\u0DA7-\\u0DB1\\u0DB3-\\u0DBB\\u0DBD\\u0DC0-\\u0DC6\\u0DCA\\u0DCF-\\u0DD4\\u0DD6\\u0DD8-\\u0DDE\\u0DF2\\u0E01-\\u0E32\\u0E34-\\u0E3A\\u0E40-\\u0E4E\\u0E50-\\u0E59\\u0E81-\\u0E82\\u0E84\\u0E87-\\u0E88\\u0E8A\\u0E8D\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA-\\u0EAB\\u0EAD-\\u0EB2\\u0EB4-\\u0EB9\\u0EBB-\\u0EBD\\u0EC0-\\u0EC4\\u0EC6\\u0EC8-\\u0ECD\\u0ED0-\\u0ED9\\u0EDE-\\u0EDF\\u0F00\\u0F20-\\u0F29\\u0F35\\u0F37\\u0F3E-\\u0F42\\u0F44-\\u0F47\\u0F49-\\u0F4C\\u0F4E-\\u0F51\\u0F53-\\u0F56\\u0F58-\\u0F5B\\u0F5D-\\u0F68\\u0F6A-\\u0F6C\\u0F71-\\u0F72\\u0F74\\u0F7A-\\u0F80\\u0F82-\\u0F84\\u0F86-\\u0F92\\u0F94-\\u0F97\\u0F99-\\u0F9C\\u0F9E-\\u0FA1\\u0FA3-\\u0FA6\\u0FA8-\\u0FAB\\u0FAD-\\u0FB8\\u0FBA-\\u0FBC\\u0FC6\\u1000-\\u1049\\u1050-\\u109D\\u10C7\\u10CD\\u10D0-\\u10F0\\u10F7-\\u10FA\\u10FD-\\u10FF\\u1200-\\u1248\\u124A-\\u124D\\u1250-\\u1256\\u1258\\u125A-\\u125D\\u1260-\\u1288\\u128A-\\u128D\\u1290-\\u12B0\\u12B2-\\u12B5\\u12B8-\\u12BE\\u12C0\\u12C2-\\u12C5\\u12C8-\\u12D6\\u12D8-\\u1310\\u1312-\\u1315\\u1318-\\u135A\\u135D-\\u135F\\u1380-\\u138F\\u1780-\\u17A2\\u17A5-\\u17A7\\u17A9-\\u17B3\\u17B6-\\u17CA\\u17D2\\u17D7\\u17DC\\u17E0-\\u17E9\\u1E00-\\u1E99\\u1E9E\\u1EA0-\\u1EF9\\u1F00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1F50-\\u1F57\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F70\\u1F72\\u1F74\\u1F76\\u1F78\\u1F7A\\u1F7C\\u1F80-\\u1FB4\\u1FB6-\\u1FBA\\u1FBC\\u1FC2-\\u1FC4\\u1FC6-\\u1FC8\\u1FCA\\u1FCC\\u1FD0-\\u1FD2\\u1FD6-\\u1FDA\\u1FE0-\\u1FE2\\u1FE4-\\u1FEA\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-\\u1FF8\\u1FFA\\u1FFC\\u2D27\\u2D2D\\u2D80-\\u2D96\\u2DA0-\\u2DA6\\u2DA8-\\u2DAE\\u2DB0-\\u2DB6\\u2DB8-\\u2DBE\\u2DC0-\\u2DC6\\u2DC8-\\u2DCE\\u2DD0-\\u2DD6\\u2DD8-\\u2DDE\\u3005-\\u3007\\u3041-\\u3096\\u3099-\\u309A\\u309D-\\u309E\\u30A1-\\u30FA\\u30FC-\\u30FE\\u3105-\\u312D\\u31A0-\\u31BA\\u3400-\\u4DB5\\u4E00-\\u9FD5\\uA660-\\uA661\\uA674-\\uA67B\\uA67F\\uA69F\\uA717-\\uA71F\\uA788\\uA78D-\\uA78E\\uA790-\\uA793\\uA7A0-\\uA7AA\\uA7FA\\uA9E7-\\uA9FE\\uAA60-\\uAA76\\uAA7A-\\uAA7F\\uAB01-\\uAB06\\uAB09-\\uAB0E\\uAB11-\\uAB16\\uAB20-\\uAB26\\uAB28-\\uAB2E\\uAC00-\\uD7A3\\uFA0E-\\uFA0F\\uFA11\\uFA13-\\uFA14\\uFA1F\\uFA21\\uFA23-\\uFA24\\uFA27-\\uFA29\\U00020000-\\U0002A6D6\\U0002A700-\\U0002B734\\U0002B740-\\U0002B81D\\U0002B820-\\U0002CEA1]").freeze();
    @Deprecated
    public static final int RESTRICTION_LEVEL = 16;
    static final int SA_TABLE_FLAG = 33554432;
    @Deprecated
    public static final int SINGLE_SCRIPT = 16;
    public static final int SINGLE_SCRIPT_CONFUSABLE = 1;
    static final int SL_TABLE_FLAG = 16777216;
    public static final int WHOLE_SCRIPT_CONFUSABLE = 4;
    private static Normalizer2 nfdNormalizer = Normalizer2.getNFDInstance();
    private UnicodeSet fAllowedCharsSet;
    private Set<ULocale> fAllowedLocales;
    private IdentifierInfo fCachedIdentifierInfo;
    private int fChecks;
    private RestrictionLevel fRestrictionLevel;
    private SpoofData fSpoofData;

    public static class Builder {
        final UnicodeSet fAllowedCharsSet;
        final Set<ULocale> fAllowedLocales;
        int fChecks;
        private RestrictionLevel fRestrictionLevel;
        SpoofData fSpoofData;

        private static class ConfusabledataBuilder {
            static final /* synthetic */ boolean -assertionsDisabled = (!ConfusabledataBuilder.class.desiredAssertionStatus());
            private UnicodeSet fKeySet = new UnicodeSet();
            private ArrayList<Integer> fKeyVec = new ArrayList();
            private int fLineNum;
            private Hashtable<Integer, SPUString> fMATable = new Hashtable();
            private Hashtable<Integer, SPUString> fMLTable = new Hashtable();
            private Pattern fParseHexNum;
            private Pattern fParseLine;
            private Hashtable<Integer, SPUString> fSATable = new Hashtable();
            private Hashtable<Integer, SPUString> fSLTable = new Hashtable();
            private ArrayList<Integer> fStringLengthsTable;
            private StringBuffer fStringTable;
            private ArrayList<Integer> fValueVec = new ArrayList();
            private SPUStringPool stringPool = new SPUStringPool();

            private static class SPUString {
                String fStr;
                int fStrTableIndex = 0;

                SPUString(String s) {
                    this.fStr = s;
                }
            }

            private static class SPUStringComparator implements Comparator<SPUString> {
                private SPUStringComparator() {
                }

                public int compare(SPUString sL, SPUString sR) {
                    int lenL = sL.fStr.length();
                    int lenR = sR.fStr.length();
                    if (lenL < lenR) {
                        return -1;
                    }
                    if (lenL > lenR) {
                        return 1;
                    }
                    return sL.fStr.compareTo(sR.fStr);
                }
            }

            private static class SPUStringPool {
                private Hashtable<String, SPUString> fHash = new Hashtable();
                private Vector<SPUString> fVec = new Vector();

                public int size() {
                    return this.fVec.size();
                }

                public SPUString getByIndex(int index) {
                    return (SPUString) this.fVec.elementAt(index);
                }

                public SPUString addString(String src) {
                    SPUString hashedString = (SPUString) this.fHash.get(src);
                    if (hashedString != null) {
                        return hashedString;
                    }
                    hashedString = new SPUString(src);
                    this.fHash.put(src, hashedString);
                    this.fVec.addElement(hashedString);
                    return hashedString;
                }

                public void sort() {
                    Collections.sort(this.fVec, new SPUStringComparator());
                }
            }

            ConfusabledataBuilder() {
            }

            void build(Reader confusables, SpoofData dest) throws ParseException, IOException {
                int i;
                StringBuffer fInput = new StringBuffer();
                WSConfusableDataBuilder.readWholeFileToString(confusables, fInput);
                this.fParseLine = Pattern.compile("(?m)^[ \\t]*([0-9A-Fa-f]+)[ \\t]+;[ \\t]*([0-9A-Fa-f]+(?:[ \\t]+[0-9A-Fa-f]+)*)[ \\t]*;\\s*(?:(SL)|(SA)|(ML)|(MA))[ \\t]*(?:#.*?)?$|^([ \\t]*(?:#.*?)?)$|^(.*?)$");
                this.fParseHexNum = Pattern.compile("\\s*([0-9A-F]+)");
                if (fInput.charAt(0) == '﻿') {
                    fInput.setCharAt(0, ' ');
                }
                Matcher matcher = this.fParseLine.matcher(fInput);
                while (matcher.find()) {
                    int keyChar;
                    this.fLineNum++;
                    if (matcher.start(7) < 0) {
                        if (matcher.start(8) >= 0) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Unrecognized Line: " + matcher.group(8), matcher.start(8));
                        }
                        keyChar = Integer.parseInt(matcher.group(1), 16);
                        if (keyChar > 1114111) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + matcher.group(1), matcher.start(1));
                        }
                        Hashtable<Integer, SPUString> hashtable;
                        Matcher m = this.fParseHexNum.matcher(matcher.group(2));
                        StringBuilder mapString = new StringBuilder();
                        while (m.find()) {
                            int c = Integer.parseInt(m.group(1), 16);
                            if (keyChar > 1114111) {
                                throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + Integer.toString(c, 16), matcher.start(2));
                            }
                            mapString.appendCodePoint(c);
                        }
                        if (!-assertionsDisabled) {
                            if ((mapString.length() >= 1 ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        SPUString smapString = this.stringPool.addString(mapString.toString());
                        if (matcher.start(3) >= 0) {
                            hashtable = this.fSLTable;
                        } else if (matcher.start(4) >= 0) {
                            hashtable = this.fSATable;
                        } else if (matcher.start(5) >= 0) {
                            hashtable = this.fMLTable;
                        } else if (matcher.start(6) >= 0) {
                            hashtable = this.fMATable;
                        } else {
                            hashtable = null;
                        }
                        if (!-assertionsDisabled) {
                            if ((hashtable != null ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        if (hashtable != this.fMATable) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Table must be 'MA'.", 0);
                        }
                        this.fSLTable.put(Integer.valueOf(keyChar), smapString);
                        this.fSATable.put(Integer.valueOf(keyChar), smapString);
                        this.fMLTable.put(Integer.valueOf(keyChar), smapString);
                        this.fMATable.put(Integer.valueOf(keyChar), smapString);
                        this.fKeySet.add(keyChar);
                    }
                }
                this.stringPool.sort();
                this.fStringTable = new StringBuffer();
                this.fStringLengthsTable = new ArrayList();
                int previousStringLength = 0;
                int previousStringIndex = 0;
                int poolSize = this.stringPool.size();
                for (i = 0; i < poolSize; i++) {
                    SPUString s = this.stringPool.getByIndex(i);
                    int strLen = s.fStr.length();
                    int strIndex = this.fStringTable.length();
                    if (!-assertionsDisabled) {
                        if ((strLen >= previousStringLength ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (strLen == 1) {
                        s.fStrTableIndex = s.fStr.charAt(0);
                    } else {
                        if (strLen > previousStringLength && previousStringLength >= 4) {
                            this.fStringLengthsTable.add(Integer.valueOf(previousStringIndex));
                            this.fStringLengthsTable.add(Integer.valueOf(previousStringLength));
                        }
                        s.fStrTableIndex = strIndex;
                        this.fStringTable.append(s.fStr);
                    }
                    previousStringLength = strLen;
                    previousStringIndex = strIndex;
                }
                if (previousStringLength >= 4) {
                    this.fStringLengthsTable.add(Integer.valueOf(previousStringIndex));
                    this.fStringLengthsTable.add(Integer.valueOf(previousStringLength));
                }
                for (String keyCharStr : this.fKeySet) {
                    keyChar = keyCharStr.codePointAt(0);
                    addKeyEntry(keyChar, this.fSLTable, 16777216);
                    addKeyEntry(keyChar, this.fSATable, SpoofChecker.SA_TABLE_FLAG);
                    addKeyEntry(keyChar, this.fMLTable, 67108864);
                    addKeyEntry(keyChar, this.fMATable, 134217728);
                }
                int numKeys = this.fKeyVec.size();
                dest.fCFUKeys = new int[numKeys];
                int previousKey = 0;
                for (i = 0; i < numKeys; i++) {
                    int key = ((Integer) this.fKeyVec.get(i)).intValue();
                    if (!-assertionsDisabled) {
                        if (((16777215 & key) >= (16777215 & previousKey) ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if (((-16777216 & key) != 0 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    dest.fCFUKeys[i] = key;
                    previousKey = key;
                }
                int numValues = this.fValueVec.size();
                if (!-assertionsDisabled) {
                    if ((numKeys == numValues ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                dest.fCFUValues = new short[numValues];
                i = 0;
                for (Integer intValue : this.fValueVec) {
                    int value = intValue.intValue();
                    if (!-assertionsDisabled) {
                        if ((value < 65535 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    int i2 = i + 1;
                    dest.fCFUValues[i] = (short) value;
                    i = i2;
                }
                dest.fCFUStrings = this.fStringTable.toString();
                int previousLength = 0;
                int stringLengthsSize = this.fStringLengthsTable.size() / 2;
                dest.fCFUStringLengths = new SpoofStringLengthsElement[stringLengthsSize];
                for (i = 0; i < stringLengthsSize; i++) {
                    int offset = ((Integer) this.fStringLengthsTable.get(i * 2)).intValue();
                    int length = ((Integer) this.fStringLengthsTable.get((i * 2) + 1)).intValue();
                    if (!-assertionsDisabled) {
                        if ((offset < dest.fCFUStrings.length() ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if ((length < 40 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if ((length > previousLength ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    dest.fCFUStringLengths[i] = new SpoofStringLengthsElement();
                    dest.fCFUStringLengths[i].fLastString = offset;
                    dest.fCFUStringLengths[i].fStrLength = length;
                    previousLength = length;
                }
            }

            void addKeyEntry(int keyChar, Hashtable<Integer, SPUString> table, int tableFlag) {
                SPUString targetMapping = (SPUString) table.get(Integer.valueOf(keyChar));
                if (targetMapping != null) {
                    boolean keyHasMultipleValues = false;
                    int i = this.fKeyVec.size() - 1;
                    while (i >= 0) {
                        int key = ((Integer) this.fKeyVec.get(i)).intValue();
                        if ((16777215 & key) != keyChar) {
                            break;
                        } else if (getMapping(i).equals(targetMapping.fStr)) {
                            this.fKeyVec.set(i, Integer.valueOf(key | tableFlag));
                            return;
                        } else {
                            keyHasMultipleValues = true;
                            i--;
                        }
                    }
                    int newKey = keyChar | tableFlag;
                    if (keyHasMultipleValues) {
                        newKey |= 268435456;
                    }
                    int adjustedMappingLength = targetMapping.fStr.length() - 1;
                    if (adjustedMappingLength > 3) {
                        adjustedMappingLength = 3;
                    }
                    newKey |= adjustedMappingLength << 29;
                    int newData = targetMapping.fStrTableIndex;
                    this.fKeyVec.add(Integer.valueOf(newKey));
                    this.fValueVec.add(Integer.valueOf(newData));
                    if (keyHasMultipleValues) {
                        int previousKeyIndex = this.fKeyVec.size() - 2;
                        this.fKeyVec.set(previousKeyIndex, Integer.valueOf(((Integer) this.fKeyVec.get(previousKeyIndex)).intValue() | 268435456));
                    }
                }
            }

            String getMapping(int index) {
                int key = ((Integer) this.fKeyVec.get(index)).intValue();
                int value = ((Integer) this.fValueVec.get(index)).intValue();
                int length = SpoofChecker.getKeyLength(key);
                switch (length) {
                    case 0:
                        return new String(new char[]{(char) value});
                    case 1:
                    case 2:
                        return this.fStringTable.substring(value, (value + length) + 1);
                    case 3:
                        length = 0;
                        for (int i = 0; i < this.fStringLengthsTable.size(); i += 2) {
                            if (value <= ((Integer) this.fStringLengthsTable.get(i)).intValue()) {
                                length = ((Integer) this.fStringLengthsTable.get(i + 1)).intValue();
                                if (!-assertionsDisabled) {
                                    if ((length < 3 ? 1 : 0) == 0) {
                                        throw new AssertionError();
                                    }
                                }
                                return this.fStringTable.substring(value, value + length);
                            }
                        }
                        if (-assertionsDisabled) {
                            if (length < 3) {
                            }
                            if ((length < 3 ? 1 : 0) == 0) {
                                throw new AssertionError();
                            }
                        }
                        return this.fStringTable.substring(value, value + length);
                    default:
                        if (-assertionsDisabled) {
                            return "";
                        }
                        throw new AssertionError();
                }
            }

            public static void buildConfusableData(Reader confusables, SpoofData dest) throws IOException, ParseException {
                new ConfusabledataBuilder().build(confusables, dest);
            }
        }

        private static class WSConfusableDataBuilder {
            static final /* synthetic */ boolean -assertionsDisabled;
            static String parseExp = "(?m)^([ \\t]*(?:#.*?)?)$|^(?:\\s*([0-9A-F]{4,})(?:..([0-9A-F]{4,}))?\\s*;\\s*([A-Za-z]+)\\s*;\\s*([A-Za-z]+)\\s*;\\s*(?:(A)|(L))[ \\t]*(?:#.*?)?)$|^(.*?)$";

            static class BuilderScriptSet {
                int codePoint = -1;
                int index = 0;
                int rindex = 0;
                ScriptSet sset = null;
                Trie2Writable trie = null;

                BuilderScriptSet() {
                }
            }

            private WSConfusableDataBuilder() {
            }

            static {
                boolean z;
                if (WSConfusableDataBuilder.class.desiredAssertionStatus()) {
                    z = false;
                } else {
                    z = true;
                }
                -assertionsDisabled = z;
            }

            static void readWholeFileToString(Reader reader, StringBuffer buffer) throws IOException {
                LineNumberReader lnr = new LineNumberReader(reader);
                while (true) {
                    String line = lnr.readLine();
                    if (line != null) {
                        buffer.append(line);
                        buffer.append('\n');
                    } else {
                        return;
                    }
                }
            }

            static void buildWSConfusableData(Reader confusablesWS, SpoofData dest) throws ParseException, IOException {
                int i;
                StringBuffer input = new StringBuffer();
                int lineNum = 0;
                Trie2Writable anyCaseTrie = new Trie2Writable(0, 0);
                Trie2Writable trie2Writable = new Trie2Writable(0, 0);
                ArrayList<BuilderScriptSet> scriptSets = new ArrayList();
                scriptSets.add(null);
                scriptSets.add(null);
                readWholeFileToString(confusablesWS, input);
                Pattern parseRegexp = Pattern.compile(parseExp);
                if (input.charAt(0) == '﻿') {
                    input.setCharAt(0, ' ');
                }
                Matcher matcher = parseRegexp.matcher(input);
                while (matcher.find()) {
                    lineNum++;
                    if (matcher.start(1) < 0) {
                        if (matcher.start(8) >= 0) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Unrecognized input: " + matcher.group(), matcher.start());
                        }
                        int startCodePoint = Integer.parseInt(matcher.group(2), 16);
                        if (startCodePoint > 1114111) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": out of range code point: " + matcher.group(2), matcher.start(2));
                        }
                        int endCodePoint = startCodePoint;
                        if (matcher.start(3) >= 0) {
                            endCodePoint = Integer.parseInt(matcher.group(3), 16);
                        }
                        if (endCodePoint > 1114111) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": out of range code point: " + matcher.group(3), matcher.start(3));
                        }
                        String srcScriptName = matcher.group(4);
                        String targScriptName = matcher.group(5);
                        int srcScript = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, srcScriptName);
                        int targScript = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, targScriptName);
                        if (srcScript == -1) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Invalid script code t: " + matcher.group(4), matcher.start(4));
                        } else if (targScript == -1) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Invalid script code t: " + matcher.group(5), matcher.start(5));
                        } else {
                            Trie2Writable table = anyCaseTrie;
                            if (matcher.start(7) >= 0) {
                                table = trie2Writable;
                            }
                            for (int cp = startCodePoint; cp <= endCodePoint; cp++) {
                                BuilderScriptSet bsset;
                                int setIndex = table.get(cp);
                                if (setIndex > 0) {
                                    if (!-assertionsDisabled) {
                                        if ((setIndex < scriptSets.size() ? 1 : null) == null) {
                                            throw new AssertionError();
                                        }
                                    }
                                    bsset = (BuilderScriptSet) scriptSets.get(setIndex);
                                } else {
                                    bsset = new BuilderScriptSet();
                                    bsset.codePoint = cp;
                                    bsset.trie = table;
                                    bsset.sset = new ScriptSet();
                                    setIndex = scriptSets.size();
                                    bsset.index = setIndex;
                                    bsset.rindex = 0;
                                    scriptSets.add(bsset);
                                    table.set(cp, setIndex);
                                }
                                bsset.sset.Union(targScript);
                                bsset.sset.Union(srcScript);
                                if (UScript.getScript(cp) != srcScript) {
                                    throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Mismatch between source script and code point " + Integer.toString(cp, 16), matcher.start(5));
                                }
                            }
                            continue;
                        }
                    }
                }
                int rtScriptSetsCount = 2;
                for (int outeri = 2; outeri < scriptSets.size(); outeri++) {
                    BuilderScriptSet outerSet = (BuilderScriptSet) scriptSets.get(outeri);
                    if (outerSet.index == outeri) {
                        int rtScriptSetsCount2 = rtScriptSetsCount + 1;
                        outerSet.rindex = rtScriptSetsCount;
                        for (int inneri = outeri + 1; inneri < scriptSets.size(); inneri++) {
                            BuilderScriptSet innerSet = (BuilderScriptSet) scriptSets.get(inneri);
                            if (outerSet.sset.equals(innerSet.sset) && outerSet.sset != innerSet.sset) {
                                innerSet.sset = outerSet.sset;
                                innerSet.index = outeri;
                                innerSet.rindex = outerSet.rindex;
                            }
                        }
                        rtScriptSetsCount = rtScriptSetsCount2;
                    }
                }
                for (i = 2; i < scriptSets.size(); i++) {
                    BuilderScriptSet bSet = (BuilderScriptSet) scriptSets.get(i);
                    if (bSet.rindex != i) {
                        bSet.trie.set(bSet.codePoint, bSet.rindex);
                    }
                }
                UnicodeSet ignoreSet = new UnicodeSet();
                ignoreSet.applyIntPropertyValue(UProperty.SCRIPT, 0);
                UnicodeSet inheritedSet = new UnicodeSet();
                inheritedSet.applyIntPropertyValue(UProperty.SCRIPT, 1);
                ignoreSet.addAll(inheritedSet);
                for (int rn = 0; rn < ignoreSet.getRangeCount(); rn++) {
                    int rangeStart = ignoreSet.getRangeStart(rn);
                    int rangeEnd = ignoreSet.getRangeEnd(rn);
                    anyCaseTrie.setRange(rangeStart, rangeEnd, 1, true);
                    trie2Writable.setRange(rangeStart, rangeEnd, 1, true);
                }
                dest.fAnyCaseTrie = anyCaseTrie.toTrie2_16();
                dest.fLowerCaseTrie = trie2Writable.toTrie2_16();
                dest.fScriptSets = new ScriptSet[rtScriptSetsCount];
                dest.fScriptSets[0] = new ScriptSet();
                dest.fScriptSets[1] = new ScriptSet();
                int rindex = 2;
                for (i = 2; i < scriptSets.size(); i++) {
                    bSet = (BuilderScriptSet) scriptSets.get(i);
                    if (bSet.rindex >= rindex) {
                        if (!-assertionsDisabled) {
                            if ((rindex == bSet.rindex ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        dest.fScriptSets[rindex] = bSet.sset;
                        rindex++;
                    }
                }
            }
        }

        public Builder() {
            this.fAllowedCharsSet = new UnicodeSet(0, 1114111);
            this.fAllowedLocales = new LinkedHashSet();
            this.fChecks = -1;
            this.fSpoofData = null;
            this.fRestrictionLevel = RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        public Builder(SpoofChecker src) {
            this.fAllowedCharsSet = new UnicodeSet(0, 1114111);
            this.fAllowedLocales = new LinkedHashSet();
            this.fChecks = src.fChecks;
            this.fSpoofData = src.fSpoofData;
            this.fAllowedCharsSet.set(src.fAllowedCharsSet);
            this.fAllowedLocales.addAll(src.fAllowedLocales);
            this.fRestrictionLevel = src.fRestrictionLevel;
        }

        public SpoofChecker build() {
            if (this.fSpoofData == null) {
                this.fSpoofData = SpoofData.getDefault();
            }
            SpoofChecker result = new SpoofChecker();
            result.fChecks = this.fChecks;
            result.fSpoofData = this.fSpoofData;
            result.fAllowedCharsSet = (UnicodeSet) this.fAllowedCharsSet.clone();
            result.fAllowedCharsSet.freeze();
            result.fAllowedLocales = new HashSet(this.fAllowedLocales);
            result.fRestrictionLevel = this.fRestrictionLevel;
            return result;
        }

        public Builder setData(Reader confusables, Reader confusablesWholeScript) throws ParseException, IOException {
            this.fSpoofData = new SpoofData();
            ConfusabledataBuilder.buildConfusableData(confusables, this.fSpoofData);
            WSConfusableDataBuilder.buildWSConfusableData(confusablesWholeScript, this.fSpoofData);
            return this;
        }

        public Builder setChecks(int checks) {
            if ((checks & 0) != 0) {
                throw new IllegalArgumentException("Bad Spoof Checks value.");
            }
            this.fChecks = checks & -1;
            return this;
        }

        public Builder setAllowedLocales(Set<ULocale> locales) {
            this.fAllowedCharsSet.clear();
            for (ULocale locale : locales) {
                addScriptChars(locale, this.fAllowedCharsSet);
            }
            this.fAllowedLocales.clear();
            if (locales.size() == 0) {
                this.fAllowedCharsSet.add(0, 1114111);
                this.fChecks &= -65;
                return this;
            }
            UnicodeSet tempSet = new UnicodeSet();
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, 0);
            this.fAllowedCharsSet.addAll(tempSet);
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, 1);
            this.fAllowedCharsSet.addAll(tempSet);
            this.fAllowedLocales.clear();
            this.fAllowedLocales.addAll(locales);
            this.fChecks |= 64;
            return this;
        }

        public Builder setAllowedJavaLocales(Set<Locale> locales) {
            HashSet<ULocale> ulocales = new HashSet(locales.size());
            for (Locale locale : locales) {
                ulocales.add(ULocale.forLocale(locale));
            }
            return setAllowedLocales(ulocales);
        }

        private void addScriptChars(ULocale locale, UnicodeSet allowedChars) {
            int[] scripts = UScript.getCode(locale);
            UnicodeSet tmpSet = new UnicodeSet();
            for (int applyIntPropertyValue : scripts) {
                tmpSet.applyIntPropertyValue(UProperty.SCRIPT, applyIntPropertyValue);
                allowedChars.addAll(tmpSet);
            }
        }

        public Builder setAllowedChars(UnicodeSet chars) {
            this.fAllowedCharsSet.set(chars);
            this.fAllowedLocales.clear();
            this.fChecks |= 64;
            return this;
        }

        @Deprecated
        public Builder setRestrictionLevel(RestrictionLevel restrictionLevel) {
            this.fRestrictionLevel = restrictionLevel;
            this.fChecks |= 16;
            return this;
        }
    }

    public static class CheckResult {
        public int checks = 0;
        @Deprecated
        public UnicodeSet numerics;
        @Deprecated
        public int position = 0;
        @Deprecated
        public RestrictionLevel restrictionLevel;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("checks:");
            if (this.checks == 0) {
                sb.append(" none");
            } else if (this.checks == -1) {
                sb.append(" all");
            } else {
                if ((this.checks & 1) != 0) {
                    sb.append(" SINGLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 2) != 0) {
                    sb.append(" MIXED_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 4) != 0) {
                    sb.append(" WHOLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 8) != 0) {
                    sb.append(" ANY_CASE");
                }
                if ((this.checks & 16) != 0) {
                    sb.append(" RESTRICTION_LEVEL");
                }
                if ((this.checks & 32) != 0) {
                    sb.append(" INVISIBLE");
                }
                if ((this.checks & 64) != 0) {
                    sb.append(" CHAR_LIMIT");
                }
                if ((this.checks & 128) != 0) {
                    sb.append(" MIXED_NUMBERS");
                }
            }
            sb.append(", numerics: ").append(this.numerics.toPattern(false));
            sb.append(", position: ").append(this.position);
            sb.append(", restrictionLevel: ").append(this.restrictionLevel);
            return sb.toString();
        }
    }

    public enum RestrictionLevel {
        ASCII,
        SINGLE_SCRIPT_RESTRICTIVE,
        HIGHLY_RESTRICTIVE,
        MODERATELY_RESTRICTIVE,
        MINIMALLY_RESTRICTIVE,
        UNRESTRICTIVE
    }

    static class ScriptSet {
        static final /* synthetic */ boolean -assertionsDisabled = (!ScriptSet.class.desiredAssertionStatus());
        private int[] bits;

        public ScriptSet() {
            this.bits = new int[6];
        }

        public ScriptSet(ByteBuffer bytes) throws IOException {
            this.bits = new int[6];
            for (int j = 0; j < this.bits.length; j++) {
                this.bits[j] = bytes.getInt();
            }
        }

        public void output(DataOutputStream os) throws IOException {
            for (int writeInt : this.bits) {
                os.writeInt(writeInt);
            }
        }

        public boolean equals(Object other) {
            if (!(other instanceof ScriptSet)) {
                return false;
            }
            return Arrays.equals(this.bits, ((ScriptSet) other).bits);
        }

        public void Union(int script) {
            int i = 1;
            int index = script / 32;
            int bit = 1 << (script & 31);
            if (!-assertionsDisabled) {
                if (index >= (this.bits.length * 4) * 4) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            int[] iArr = this.bits;
            iArr[index] = iArr[index] | bit;
        }

        public void Union(ScriptSet other) {
            for (int i = 0; i < this.bits.length; i++) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] | other.bits[i];
            }
        }

        public void intersect(ScriptSet other) {
            for (int i = 0; i < this.bits.length; i++) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] & other.bits[i];
            }
        }

        public void intersect(int script) {
            int i;
            int i2 = 1;
            int index = script / 32;
            int bit = 1 << (script & 31);
            if (!-assertionsDisabled) {
                if (index >= (this.bits.length * 4) * 4) {
                    i2 = 0;
                }
                if (i2 == 0) {
                    throw new AssertionError();
                }
            }
            for (i = 0; i < index; i++) {
                this.bits[i] = 0;
            }
            int[] iArr = this.bits;
            iArr[index] = iArr[index] & bit;
            for (i = index + 1; i < this.bits.length; i++) {
                this.bits[i] = 0;
            }
        }

        public void setAll() {
            for (int i = 0; i < this.bits.length; i++) {
                this.bits[i] = -1;
            }
        }

        public void resetAll() {
            for (int i = 0; i < this.bits.length; i++) {
                this.bits[i] = 0;
            }
        }

        public int countMembers() {
            int count = 0;
            for (int x : this.bits) {
                for (int x2 = this.bits[i]; x2 != 0; x2 &= x2 - 1) {
                    count++;
                }
            }
            return count;
        }
    }

    private static class SpoofData {
        private static final int DATA_FORMAT = 1130788128;
        private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
        Trie2 fAnyCaseTrie;
        int[] fCFUKeys;
        SpoofStringLengthsElement[] fCFUStringLengths;
        String fCFUStrings;
        short[] fCFUValues;
        Trie2 fLowerCaseTrie;
        ScriptSet[] fScriptSets;

        private static final class DefaultData {
            private static SpoofData INSTANCE;

            private DefaultData() {
            }

            static {
                INSTANCE = null;
                try {
                    INSTANCE = new SpoofData(ICUBinary.getRequiredData("confusables.cfu"));
                } catch (IOException e) {
                }
            }
        }

        private static final class IsAcceptable implements Authenticate {
            private IsAcceptable() {
            }

            public boolean isDataVersionAcceptable(byte[] version) {
                return version[0] == (byte) 1;
            }
        }

        static class SpoofStringLengthsElement {
            int fLastString;
            int fStrLength;

            SpoofStringLengthsElement() {
            }

            public boolean equals(Object other) {
                boolean z = false;
                if (!(other instanceof SpoofStringLengthsElement)) {
                    return false;
                }
                SpoofStringLengthsElement otherEl = (SpoofStringLengthsElement) other;
                if (this.fLastString == otherEl.fLastString && this.fStrLength == otherEl.fStrLength) {
                    z = true;
                }
                return z;
            }
        }

        static SpoofData getDefault() {
            return DefaultData.INSTANCE;
        }

        SpoofData() {
        }

        SpoofData(ByteBuffer bytes) throws IOException {
            ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            bytes.mark();
            readData(bytes);
        }

        public boolean equals(Object other) {
            if (!(other instanceof SpoofData)) {
                return false;
            }
            SpoofData otherData = (SpoofData) other;
            if (!Arrays.equals(this.fCFUKeys, otherData.fCFUKeys) || !Arrays.equals(this.fCFUValues, otherData.fCFUValues) || !Arrays.deepEquals(this.fCFUStringLengths, otherData.fCFUStringLengths)) {
                return false;
            }
            if (this.fCFUStrings != otherData.fCFUStrings && this.fCFUStrings != null && !this.fCFUStrings.equals(otherData.fCFUStrings)) {
                return false;
            }
            if (this.fAnyCaseTrie != otherData.fAnyCaseTrie && this.fAnyCaseTrie != null && !this.fAnyCaseTrie.equals(otherData.fAnyCaseTrie)) {
                return false;
            }
            if ((this.fLowerCaseTrie == otherData.fLowerCaseTrie || this.fLowerCaseTrie == null || this.fLowerCaseTrie.equals(otherData.fLowerCaseTrie)) && Arrays.deepEquals(this.fScriptSets, otherData.fScriptSets)) {
                return true;
            }
            return false;
        }

        void readData(ByteBuffer bytes) throws IOException {
            if (bytes.getInt() != SpoofChecker.MAGIC) {
                throw new IllegalArgumentException("Bad Spoof Check Data.");
            }
            int i;
            int dataFormatVersion = bytes.getInt();
            int dataLength = bytes.getInt();
            int CFUKeysOffset = bytes.getInt();
            int CFUKeysSize = bytes.getInt();
            int CFUValuesOffset = bytes.getInt();
            int CFUValuesSize = bytes.getInt();
            int CFUStringTableOffset = bytes.getInt();
            int CFUStringTableSize = bytes.getInt();
            int CFUStringLengthsOffset = bytes.getInt();
            int CFUStringLengthsSize = bytes.getInt();
            int anyCaseTrieOffset = bytes.getInt();
            int anyCaseTrieSize = bytes.getInt();
            int lowerCaseTrieOffset = bytes.getInt();
            int lowerCaseTrieLength = bytes.getInt();
            int scriptSetsOffset = bytes.getInt();
            int scriptSetslength = bytes.getInt();
            this.fCFUKeys = null;
            this.fCFUValues = null;
            this.fCFUStringLengths = null;
            this.fCFUStrings = null;
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUKeysOffset);
            this.fCFUKeys = ICUBinary.getInts(bytes, CFUKeysSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUValuesOffset);
            this.fCFUValues = ICUBinary.getShorts(bytes, CFUValuesSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringTableOffset);
            this.fCFUStrings = ICUBinary.getString(bytes, CFUStringTableSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringLengthsOffset);
            this.fCFUStringLengths = new SpoofStringLengthsElement[CFUStringLengthsSize];
            for (i = 0; i < CFUStringLengthsSize; i++) {
                this.fCFUStringLengths[i] = new SpoofStringLengthsElement();
                this.fCFUStringLengths[i].fLastString = bytes.getShort();
                this.fCFUStringLengths[i].fStrLength = bytes.getShort();
            }
            bytes.reset();
            ICUBinary.skipBytes(bytes, anyCaseTrieOffset);
            this.fAnyCaseTrie = Trie2.createFromSerialized(bytes);
            bytes.reset();
            ICUBinary.skipBytes(bytes, lowerCaseTrieOffset);
            this.fLowerCaseTrie = Trie2.createFromSerialized(bytes);
            bytes.reset();
            ICUBinary.skipBytes(bytes, scriptSetsOffset);
            this.fScriptSets = new ScriptSet[scriptSetslength];
            for (i = 0; i < scriptSetslength; i++) {
                this.fScriptSets[i] = new ScriptSet(bytes);
            }
        }
    }

    static {
        boolean z;
        if (SpoofChecker.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    private SpoofChecker() {
        this.fCachedIdentifierInfo = null;
    }

    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        return this.fRestrictionLevel;
    }

    public int getChecks() {
        return this.fChecks;
    }

    public Set<ULocale> getAllowedLocales() {
        return Collections.unmodifiableSet(this.fAllowedLocales);
    }

    public Set<Locale> getAllowedJavaLocales() {
        HashSet<Locale> locales = new HashSet(this.fAllowedLocales.size());
        for (ULocale uloc : this.fAllowedLocales) {
            locales.add(uloc.toLocale());
        }
        return locales;
    }

    public UnicodeSet getAllowedChars() {
        return this.fAllowedCharsSet;
    }

    public boolean failsChecks(String text, CheckResult checkResult) {
        int i;
        int length = text.length();
        int result = 0;
        if (checkResult != null) {
            checkResult.position = 0;
            checkResult.numerics = null;
            checkResult.restrictionLevel = null;
        }
        IdentifierInfo identifierInfo = null;
        if ((this.fChecks & 144) != 0) {
            identifierInfo = getIdentifierInfo().setIdentifier(text).setIdentifierProfile(this.fAllowedCharsSet);
        }
        if ((this.fChecks & 16) != 0) {
            RestrictionLevel textRestrictionLevel = identifierInfo.getRestrictionLevel();
            if (textRestrictionLevel.compareTo(this.fRestrictionLevel) > 0) {
                result = 16;
            }
            if (checkResult != null) {
                checkResult.restrictionLevel = textRestrictionLevel;
            }
        }
        if ((this.fChecks & 128) != 0) {
            UnicodeSet numerics = identifierInfo.getNumerics();
            if (numerics.size() > 1) {
                result |= 128;
            }
            if (checkResult != null) {
                checkResult.numerics = numerics;
            }
        }
        int c;
        if ((this.fChecks & 64) != 0) {
            i = 0;
            while (i < length) {
                c = Character.codePointAt(text, i);
                i = Character.offsetByCodePoints(text, i, 1);
                if (!this.fAllowedCharsSet.contains(c)) {
                    result |= 64;
                    break;
                }
            }
        }
        if ((this.fChecks & 38) != 0) {
            String nfdText = nfdNormalizer.normalize(text);
            if ((this.fChecks & 32) != 0) {
                int firstNonspacingMark = 0;
                boolean haveMultipleMarks = false;
                UnicodeSet marksSeenSoFar = new UnicodeSet();
                i = 0;
                while (i < length) {
                    c = Character.codePointAt(nfdText, i);
                    i = Character.offsetByCodePoints(nfdText, i, 1);
                    if (Character.getType(c) != 6) {
                        firstNonspacingMark = 0;
                        if (haveMultipleMarks) {
                            marksSeenSoFar.clear();
                            haveMultipleMarks = false;
                        }
                    } else if (firstNonspacingMark == 0) {
                        firstNonspacingMark = c;
                    } else {
                        if (!haveMultipleMarks) {
                            marksSeenSoFar.add(firstNonspacingMark);
                            haveMultipleMarks = true;
                        }
                        if (marksSeenSoFar.contains(c)) {
                            result |= 32;
                            break;
                        }
                        marksSeenSoFar.add(c);
                    }
                }
            }
            if ((this.fChecks & 6) != 0) {
                if (identifierInfo == null) {
                    identifierInfo = getIdentifierInfo();
                    identifierInfo.setIdentifier(text);
                }
                int scriptCount = identifierInfo.getScriptCount();
                ScriptSet scripts = new ScriptSet();
                wholeScriptCheck(nfdText, scripts);
                int confusableScriptCount = scripts.countMembers();
                if ((this.fChecks & 4) != 0 && confusableScriptCount >= 2 && scriptCount == 1) {
                    result |= 4;
                }
                if ((this.fChecks & 2) != 0 && confusableScriptCount >= 1 && scriptCount > 1) {
                    result |= 2;
                }
            }
        }
        if (checkResult != null) {
            checkResult.checks = result;
        }
        releaseIdentifierInfo(identifierInfo);
        if (result != 0) {
            return true;
        }
        return false;
    }

    public boolean failsChecks(String text) {
        return failsChecks(text, null);
    }

    public int areConfusable(String s1, String s2) {
        if ((this.fChecks & 7) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        }
        int flagsForSkeleton = this.fChecks & 8;
        int result = 0;
        IdentifierInfo identifierInfo = getIdentifierInfo();
        identifierInfo.setIdentifier(s1);
        int s1ScriptCount = identifierInfo.getScriptCount();
        int s1FirstScript = identifierInfo.getScripts().nextSetBit(0);
        identifierInfo.setIdentifier(s2);
        int s2ScriptCount = identifierInfo.getScriptCount();
        int s2FirstScript = identifierInfo.getScripts().nextSetBit(0);
        releaseIdentifierInfo(identifierInfo);
        if ((this.fChecks & 1) != 0 && s1ScriptCount <= 1 && s2ScriptCount <= 1 && s1FirstScript == s2FirstScript) {
            flagsForSkeleton |= 1;
            if (getSkeleton(flagsForSkeleton, s1).equals(getSkeleton(flagsForSkeleton, s2))) {
                result = 1;
            }
        }
        if ((result & 1) != 0) {
            return result;
        }
        boolean possiblyWholeScriptConfusables = (s1ScriptCount > 1 || s2ScriptCount > 1) ? false : (this.fChecks & 4) != 0;
        if ((this.fChecks & 2) != 0 || possiblyWholeScriptConfusables) {
            flagsForSkeleton &= -2;
            if (getSkeleton(flagsForSkeleton, s1).equals(getSkeleton(flagsForSkeleton, s2))) {
                result |= 2;
                if (possiblyWholeScriptConfusables) {
                    result |= 4;
                }
            }
        }
        return result;
    }

    public String getSkeleton(int type, String id) {
        int tableMask;
        switch (type) {
            case 0:
                tableMask = 67108864;
                break;
            case 1:
                tableMask = 16777216;
                break;
            case 8:
                tableMask = 134217728;
                break;
            case 9:
                tableMask = SA_TABLE_FLAG;
                break;
            default:
                throw new IllegalArgumentException("SpoofChecker.getSkeleton(), bad type value.");
        }
        String nfdId = nfdNormalizer.normalize(id);
        int normalizedLen = nfdId.length();
        StringBuilder skelSB = new StringBuilder();
        int inputIndex = 0;
        while (inputIndex < normalizedLen) {
            int c = Character.codePointAt(nfdId, inputIndex);
            inputIndex += Character.charCount(c);
            confusableLookup(c, tableMask, skelSB);
        }
        return nfdNormalizer.normalize(skelSB.toString());
    }

    @Deprecated
    public boolean equals(Object other) {
        if (!(other instanceof SpoofChecker)) {
            return false;
        }
        SpoofChecker otherSC = (SpoofChecker) other;
        if ((this.fSpoofData != otherSC.fSpoofData && this.fSpoofData != null && !this.fSpoofData.equals(otherSC.fSpoofData)) || this.fChecks != otherSC.fChecks) {
            return false;
        }
        if (this.fAllowedLocales != otherSC.fAllowedLocales && this.fAllowedLocales != null && !this.fAllowedLocales.equals(otherSC.fAllowedLocales)) {
            return false;
        }
        if ((this.fAllowedCharsSet == otherSC.fAllowedCharsSet || this.fAllowedCharsSet == null || this.fAllowedCharsSet.equals(otherSC.fAllowedCharsSet)) && this.fRestrictionLevel == otherSC.fRestrictionLevel) {
            return true;
        }
        return false;
    }

    @Deprecated
    public int hashCode() {
        if (-assertionsDisabled) {
            return 1234;
        }
        throw new AssertionError();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void confusableLookup(int inChar, int tableMask, StringBuilder dest) {
        int low = 0;
        int limit = this.fSpoofData.fCFUKeys.length;
        boolean foundChar = false;
        while (true) {
            int mid = low + ((limit - low) / 2);
            int midc = this.fSpoofData.fCFUKeys[mid] & DictionaryData.TRANSFORM_OFFSET_MASK;
            if (inChar != midc) {
                if (inChar < midc) {
                    limit = mid;
                } else {
                    low = mid + 1;
                }
                if (low >= limit) {
                    break;
                }
            } else {
                break;
            }
        }
        if (foundChar) {
            boolean foundKey = false;
            int keyFlags = this.fSpoofData.fCFUKeys[mid] & -16777216;
            if ((keyFlags & tableMask) == 0) {
                if ((268435456 & keyFlags) != 0) {
                    int altMid;
                    for (altMid = mid - 1; (this.fSpoofData.fCFUKeys[altMid] & 16777215) == inChar; altMid--) {
                        keyFlags = this.fSpoofData.fCFUKeys[altMid] & -16777216;
                        if ((keyFlags & tableMask) != 0) {
                            mid = altMid;
                            foundKey = true;
                            break;
                        }
                    }
                    if (!foundKey) {
                        for (altMid = mid + 1; (this.fSpoofData.fCFUKeys[altMid] & 16777215) == inChar; altMid++) {
                            keyFlags = this.fSpoofData.fCFUKeys[altMid] & -16777216;
                            if ((keyFlags & tableMask) != 0) {
                                mid = altMid;
                                foundKey = true;
                                break;
                            }
                        }
                    }
                }
                if (!foundKey) {
                    dest.appendCodePoint(inChar);
                    return;
                }
            }
            int stringLen = getKeyLength(keyFlags) + 1;
            short value = this.fSpoofData.fCFUValues[mid];
            if (stringLen == 1) {
                dest.append((char) value);
                return;
            }
            if (stringLen == 4) {
                boolean dataOK = false;
                for (SpoofStringLengthsElement el : this.fSpoofData.fCFUStringLengths) {
                    if (el.fLastString >= value) {
                        stringLen = el.fStrLength;
                        dataOK = true;
                        break;
                    }
                }
                if (!(-assertionsDisabled || dataOK)) {
                    throw new AssertionError();
                }
            }
            dest.append(this.fSpoofData.fCFUStrings, value, value + stringLen);
            return;
        }
        dest.appendCodePoint(inChar);
    }

    private void wholeScriptCheck(CharSequence text, ScriptSet result) {
        int inputIdx = 0;
        Trie2 table = (this.fChecks & 8) != 0 ? this.fSpoofData.fAnyCaseTrie : this.fSpoofData.fLowerCaseTrie;
        result.setAll();
        while (inputIdx < text.length()) {
            int c = Character.codePointAt(text, inputIdx);
            inputIdx = Character.offsetByCodePoints(text, inputIdx, 1);
            int index = table.get(c);
            if (index == 0) {
                int cpScript = UScript.getScript(c);
                if (!-assertionsDisabled) {
                    if ((cpScript > 1 ? 1 : 0) == 0) {
                        throw new AssertionError();
                    }
                }
                result.intersect(cpScript);
            } else if (index != 1) {
                result.intersect(this.fSpoofData.fScriptSets[index]);
            }
        }
    }

    private IdentifierInfo getIdentifierInfo() {
        synchronized (this) {
            IdentifierInfo returnIdInfo = this.fCachedIdentifierInfo;
            this.fCachedIdentifierInfo = null;
        }
        if (returnIdInfo == null) {
            return new IdentifierInfo();
        }
        return returnIdInfo;
    }

    private void releaseIdentifierInfo(IdentifierInfo idInfo) {
        if (idInfo != null) {
            synchronized (this) {
                if (this.fCachedIdentifierInfo == null) {
                    this.fCachedIdentifierInfo = idInfo;
                }
            }
        }
    }

    static final int getKeyLength(int x) {
        return (x >> 29) & 3;
    }
}
