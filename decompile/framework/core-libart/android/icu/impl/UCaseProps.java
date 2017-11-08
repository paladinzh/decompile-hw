package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2.Range;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.ULocale;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public final class UCaseProps {
    private static final int ABOVE = 64;
    private static final int CLOSURE_MAX_LENGTH = 15;
    private static final String DATA_FILE_NAME = "ucase.icu";
    private static final String DATA_NAME = "ucase";
    private static final String DATA_TYPE = "icu";
    private static final int DELTA_SHIFT = 7;
    private static final int DOT_MASK = 96;
    private static final int EXCEPTION = 16;
    private static final int EXC_CLOSURE = 6;
    private static final int EXC_CONDITIONAL_FOLD = 32768;
    private static final int EXC_CONDITIONAL_SPECIAL = 16384;
    private static final int EXC_DOT_SHIFT = 7;
    private static final int EXC_DOUBLE_SLOTS = 256;
    private static final int EXC_FOLD = 1;
    private static final int EXC_FULL_MAPPINGS = 7;
    private static final int EXC_LOWER = 0;
    private static final int EXC_SHIFT = 5;
    private static final int EXC_TITLE = 3;
    private static final int EXC_UPPER = 2;
    private static final int FMT = 1665225541;
    private static final int FOLD_CASE_OPTIONS_MASK = 255;
    private static final int FULL_LOWER = 15;
    public static final UCaseProps INSTANCE;
    private static final int IX_EXC_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int IX_UNFOLD_LENGTH = 4;
    private static final int LOC_LITHUANIAN = 3;
    private static final int LOC_ROOT = 1;
    private static final int LOC_TURKISH = 2;
    private static final int LOC_UNKNOWN = 0;
    public static final int LOWER = 1;
    public static final int MAX_STRING_LENGTH = 31;
    public static final int NONE = 0;
    private static final int OTHER_ACCENT = 96;
    private static final int SENSITIVE = 8;
    private static final int SOFT_DOTTED = 32;
    public static final int TITLE = 3;
    public static final int TYPE_MASK = 3;
    private static final int UNFOLD_ROWS = 0;
    private static final int UNFOLD_ROW_WIDTH = 1;
    private static final int UNFOLD_STRING_WIDTH = 2;
    public static final int UPPER = 2;
    public static final StringBuilder dummyStringBuilder = new StringBuilder();
    private static final byte[] flagsOffset = new byte[]{(byte) 0, (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 1, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 2, (byte) 3, (byte) 3, (byte) 4, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 3, (byte) 4, (byte) 4, (byte) 5, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 4, (byte) 5, (byte) 5, (byte) 6, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 5, (byte) 6, (byte) 6, (byte) 7, (byte) 6, (byte) 7, (byte) 7, (byte) 8};
    private static final String iDot = "i̇";
    private static final String iDotAcute = "i̇́";
    private static final String iDotGrave = "i̇̀";
    private static final String iDotTilde = "i̇̃";
    private static final String iOgonekDot = "į̇";
    private static final String jDot = "j̇";
    private static final int[] rootLocCache = new int[]{1};
    private char[] exceptions;
    private int[] indexes;
    private Trie2_16 trie;
    private char[] unfold;

    public interface ContextIterator {
        int next();

        void reset(int i);
    }

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == (byte) 3;
        }
    }

    private final int toUpperOrTitle(int r1, android.icu.impl.UCaseProps.ContextIterator r2, java.lang.StringBuilder r3, android.icu.util.ULocale r4, int[] r5, boolean r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toUpperOrTitle(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[], boolean):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toUpperOrTitle(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[], boolean):int");
    }

    public final int toFullFolding(int r1, java.lang.StringBuilder r2, int r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toFullFolding(int, java.lang.StringBuilder, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toFullFolding(int, java.lang.StringBuilder, int):int");
    }

    public final int toFullLower(int r1, android.icu.impl.UCaseProps.ContextIterator r2, java.lang.StringBuilder r3, android.icu.util.ULocale r4, int[] r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toFullLower(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[]):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toFullLower(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[]):int");
    }

    private UCaseProps() throws IOException {
        readData(ICUBinary.getRequiredData(DATA_FILE_NAME));
    }

    private final void readData(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, FMT, new IsAcceptable());
        int count = bytes.getInt();
        if (count < 16) {
            throw new IOException("indexes[0] too small in ucase.icu");
        }
        this.indexes = new int[count];
        this.indexes[0] = count;
        for (int i = 1; i < count; i++) {
            this.indexes[i] = bytes.getInt();
        }
        this.trie = Trie2_16.createFromSerialized(bytes);
        int expectedTrieLength = this.indexes[2];
        int trieLength = this.trie.getSerializedLength();
        if (trieLength > expectedTrieLength) {
            throw new IOException("ucase.icu: not enough bytes for the trie");
        }
        ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
        count = this.indexes[3];
        if (count > 0) {
            this.exceptions = ICUBinary.getChars(bytes, count, 0);
        }
        count = this.indexes[4];
        if (count > 0) {
            this.unfold = ICUBinary.getChars(bytes, count, 0);
        }
    }

    public final void addPropertyStarts(UnicodeSet set) {
        Iterator<Range> trieIterator = this.trie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (!range.leadSurrogate) {
                set.add(range.startCodePoint);
            } else {
                return;
            }
        }
    }

    private static final int getExceptionsOffset(int props) {
        return props >> 5;
    }

    private static final boolean propsHasException(int props) {
        return (props & 16) != 0;
    }

    static {
        try {
            INSTANCE = new UCaseProps();
        } catch (Throwable e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static final boolean hasSlot(int flags, int index) {
        return ((1 << index) & flags) != 0;
    }

    private static final byte slotOffset(int flags, int index) {
        return flagsOffset[((1 << index) - 1) & flags];
    }

    private final long getSlotValueAndOffset(int excWord, int index, int excOffset) {
        long value;
        if ((excWord & 256) == 0) {
            excOffset += slotOffset(excWord, index);
            value = (long) this.exceptions[excOffset];
        } else {
            excOffset += slotOffset(excWord, index) * 2;
            int excOffset2 = excOffset + 1;
            value = (((long) this.exceptions[excOffset]) << 16) | ((long) this.exceptions[excOffset2]);
            excOffset = excOffset2;
        }
        return (((long) excOffset) << 32) | value;
    }

    private final int getSlotValue(int excWord, int index, int excOffset) {
        if ((excWord & 256) == 0) {
            return this.exceptions[excOffset + slotOffset(excWord, index)];
        }
        excOffset += slotOffset(excWord, index) * 2;
        int excOffset2 = excOffset + 1;
        int value = (this.exceptions[excOffset] << 16) | this.exceptions[excOffset2];
        excOffset = excOffset2;
        return value;
    }

    public final int tolower(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, 0)) {
                return getSlotValue(excWord, 0, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) >= 2) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int toupper(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, 2)) {
                return getSlotValue(excWord, 2, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) == 1) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int totitle(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int index;
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, 3)) {
                index = 3;
            } else if (!hasSlot(excWord, 2)) {
                return c;
            } else {
                index = 2;
            }
            c = getSlotValue(excWord, index, excOffset2);
        } else if (getTypeFromProps(props) == 1) {
            c += getDelta(props);
        }
        return c;
    }

    public final void addCaseClosure(int c, UnicodeSet set) {
        switch (c) {
            case 73:
                set.add(105);
                return;
            case 105:
                set.add(73);
                return;
            case 304:
                set.add(iDot);
                return;
            case 305:
                return;
            default:
                int props = this.trie.get(c);
                if (propsHasException(props)) {
                    int index;
                    long value;
                    int closureLength;
                    int closureOffset;
                    int excOffset = getExceptionsOffset(props);
                    int excOffset2 = excOffset + 1;
                    int excWord = this.exceptions[excOffset];
                    int excOffset0 = excOffset2;
                    excOffset = excOffset2;
                    for (index = 0; index <= 3; index++) {
                        if (hasSlot(excWord, index)) {
                            excOffset = excOffset0;
                            set.add(getSlotValue(excWord, index, excOffset0));
                        }
                    }
                    if (hasSlot(excWord, 6)) {
                        excOffset = excOffset0;
                        value = getSlotValueAndOffset(excWord, 6, excOffset0);
                        closureLength = ((int) value) & 15;
                        closureOffset = ((int) (value >> 32)) + 1;
                    } else {
                        closureLength = 0;
                        closureOffset = 0;
                    }
                    if (hasSlot(excWord, 7)) {
                        excOffset = excOffset0;
                        value = getSlotValueAndOffset(excWord, 7, excOffset0);
                        int fullLength = ((int) value) & 65535;
                        excOffset = (((int) (value >> 32)) + 1) + (fullLength & 15);
                        fullLength >>= 4;
                        int length = fullLength & 15;
                        if (length != 0) {
                            set.add(new String(this.exceptions, excOffset, length));
                            excOffset += length;
                        }
                        fullLength >>= 4;
                        closureOffset = (excOffset + (fullLength & 15)) + (fullLength >> 4);
                    }
                    index = 0;
                    while (index < closureLength) {
                        c = UTF16.charAt(this.exceptions, closureOffset, this.exceptions.length, index);
                        set.add(c);
                        index += UTF16.getCharCount(c);
                    }
                } else if (getTypeFromProps(props) != 0) {
                    int delta = getDelta(props);
                    if (delta != 0) {
                        set.add(c + delta);
                    }
                }
                return;
        }
    }

    private final int strcmpMax(String s, int unfoldOffset, int max) {
        int length = s.length();
        max -= length;
        int i1 = 0;
        while (true) {
            int i12 = i1 + 1;
            int c1 = s.charAt(i1);
            int unfoldOffset2 = unfoldOffset + 1;
            int c2 = this.unfold[unfoldOffset];
            if (c2 != 0) {
                c1 -= c2;
                if (c1 == 0) {
                    length--;
                    if (length <= 0) {
                        break;
                    }
                    i1 = i12;
                    unfoldOffset = unfoldOffset2;
                } else {
                    return c1;
                }
            }
            return 1;
        }
        if (max == 0 || this.unfold[unfoldOffset2] == '\u0000') {
            return 0;
        }
        return -max;
    }

    public final boolean addStringCaseClosure(String s, UnicodeSet set) {
        if (this.unfold == null || s == null) {
            return false;
        }
        int length = s.length();
        if (length <= 1) {
            return false;
        }
        int unfoldRows = this.unfold[0];
        int unfoldRowWidth = this.unfold[1];
        int unfoldStringWidth = this.unfold[2];
        if (length > unfoldStringWidth) {
            return false;
        }
        int start = 0;
        int limit = unfoldRows;
        while (start < limit) {
            int i = (start + limit) / 2;
            int unfoldOffset = (i + 1) * unfoldRowWidth;
            int result = strcmpMax(s, unfoldOffset, unfoldStringWidth);
            if (result == 0) {
                i = unfoldStringWidth;
                while (i < unfoldRowWidth && this.unfold[unfoldOffset + i] != '\u0000') {
                    int c = UTF16.charAt(this.unfold, unfoldOffset, this.unfold.length, i);
                    set.add(c);
                    addCaseClosure(c, set);
                    i += UTF16.getCharCount(c);
                }
                return true;
            } else if (result < 0) {
                limit = i;
            } else {
                start = i + 1;
            }
        }
        return false;
    }

    public final int getType(int c) {
        return getTypeFromProps(this.trie.get(c));
    }

    public final int getTypeOrIgnorable(int c) {
        return getTypeAndIgnorableFromProps(this.trie.get(c));
    }

    public final int getDotType(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            return (this.exceptions[getExceptionsOffset(props)] >> 7) & 96;
        }
        return props & 96;
    }

    public final boolean isSoftDotted(int c) {
        return getDotType(c) == 32;
    }

    public final boolean isCaseSensitive(int c) {
        return (this.trie.get(c) & 8) != 0;
    }

    private static final int getCaseLocale(ULocale locale, int[] locCache) {
        int result;
        if (locCache != null) {
            result = locCache[0];
            if (result != 0) {
                return result;
            }
        }
        result = 1;
        String language = locale.getLanguage();
        if (language.equals("tr") || language.equals("tur") || language.equals("az") || language.equals("aze")) {
            result = 2;
        } else if (language.equals("lt") || language.equals("lit")) {
            result = 3;
        }
        if (locCache != null) {
            locCache[0] = result;
        }
        return result;
    }

    private final boolean isFollowedByCasedLetter(ContextIterator iter, int dir) {
        if (iter == null) {
            return false;
        }
        iter.reset(dir);
        int type;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            type = getTypeOrIgnorable(c);
        } while ((type & 4) != 0);
        if (type != 0) {
            return true;
        }
        return false;
    }

    private final boolean isPrecededBySoftDotted(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        int dotType;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == 32) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isPrecededBy_I(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        int c;
        do {
            c = iter.next();
            if (c < 0) {
                return false;
            }
            if (c == 73) {
                return true;
            }
        } while (getDotType(c) == 96);
        return false;
    }

    private final boolean isFollowedByMoreAbove(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(1);
        int dotType;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == 64) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isFollowedByDotAbove(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(1);
        int c;
        do {
            c = iter.next();
            if (c < 0) {
                return false;
            }
            if (c == 775) {
                return true;
            }
        } while (getDotType(c) == 96);
        return false;
    }

    public final int toFullUpper(int c, ContextIterator iter, StringBuilder out, ULocale locale, int[] locCache) {
        return toUpperOrTitle(c, iter, out, locale, locCache, true);
    }

    public final int toFullTitle(int c, ContextIterator iter, StringBuilder out, ULocale locale, int[] locCache) {
        return toUpperOrTitle(c, iter, out, locale, locCache, false);
    }

    public final int fold(int c, int options) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int index;
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + 1;
            int excWord = this.exceptions[excOffset];
            if ((32768 & excWord) != 0) {
                if ((options & 255) == 0) {
                    if (c == 73) {
                        return 105;
                    }
                    if (c == 304) {
                        return c;
                    }
                } else if (c == 73) {
                    return 305;
                } else {
                    if (c == 304) {
                        return 105;
                    }
                }
            }
            if (hasSlot(excWord, 1)) {
                index = 1;
            } else if (!hasSlot(excWord, 0)) {
                return c;
            } else {
                index = 0;
            }
            c = getSlotValue(excWord, index, excOffset2);
        } else if (getTypeFromProps(props) >= 2) {
            c += getDelta(props);
        }
        return c;
    }

    public final boolean hasBinaryProperty(int c, int which) {
        boolean z = true;
        switch (which) {
            case 22:
                return 1 == getType(c);
            case 27:
                return isSoftDotted(c);
            case 30:
                if (2 != getType(c)) {
                    z = false;
                }
                return z;
            case 34:
                return isCaseSensitive(c);
            case 49:
                if (getType(c) == 0) {
                    z = false;
                }
                return z;
            case 50:
                if ((getTypeOrIgnorable(c) >> 2) == 0) {
                    z = false;
                }
                return z;
            case 51:
                dummyStringBuilder.setLength(0);
                if (toFullLower(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case 52:
                dummyStringBuilder.setLength(0);
                if (toFullUpper(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case 53:
                dummyStringBuilder.setLength(0);
                if (toFullTitle(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case 55:
                dummyStringBuilder.setLength(0);
                if (toFullLower(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    if (toFullUpper(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                        if (toFullTitle(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                            z = false;
                        }
                    }
                }
                return z;
            default:
                return false;
        }
    }

    private static final int getTypeFromProps(int props) {
        return props & 3;
    }

    private static final int getTypeAndIgnorableFromProps(int props) {
        return props & 7;
    }

    private static final int getDelta(int props) {
        return ((short) props) >> 7;
    }
}
