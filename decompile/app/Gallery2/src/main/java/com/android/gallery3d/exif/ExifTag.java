package com.android.gallery3d.exif;

import com.android.gallery3d.gadget.XmlUtils;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ExifTag {
    private static Charset EXIF_TAG_CHARSET = Charset.forName(XmlUtils.INPUT_ENCODING);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    private static final int[] TYPE_TO_SIZE_MAP = new int[11];
    private int mComponentCountActual;
    private final short mDataType;
    private boolean mHasDefinedDefaultComponentCount;
    private int mIfd;
    private int mOffset;
    private final short mTagId;
    private Object mValue = null;

    static {
        TYPE_TO_SIZE_MAP[1] = 1;
        TYPE_TO_SIZE_MAP[2] = 1;
        TYPE_TO_SIZE_MAP[3] = 2;
        TYPE_TO_SIZE_MAP[4] = 4;
        TYPE_TO_SIZE_MAP[5] = 8;
        TYPE_TO_SIZE_MAP[7] = 1;
        TYPE_TO_SIZE_MAP[9] = 4;
        TYPE_TO_SIZE_MAP[10] = 8;
    }

    public static boolean isValidIfd(int ifdId) {
        if (ifdId == 0 || ifdId == 1 || ifdId == 2 || ifdId == 3 || ifdId == 4) {
            return true;
        }
        return false;
    }

    public static boolean isValidType(short type) {
        if (type == (short) 1 || type == (short) 2 || type == (short) 3 || type == (short) 4 || type == (short) 5 || type == (short) 7 || type == (short) 9 || type == (short) 10) {
            return true;
        }
        return false;
    }

    ExifTag(short tagId, short type, int componentCount, int ifd, boolean hasDefinedComponentCount) {
        this.mTagId = tagId;
        this.mDataType = type;
        this.mComponentCountActual = componentCount;
        this.mHasDefinedDefaultComponentCount = hasDefinedComponentCount;
        this.mIfd = ifd;
    }

    public static int getElementSize(short type) {
        return TYPE_TO_SIZE_MAP[type];
    }

    public int getIfd() {
        return this.mIfd;
    }

    protected void setIfd(int ifdId) {
        this.mIfd = ifdId;
    }

    public short getTagId() {
        return this.mTagId;
    }

    public short getDataType() {
        return this.mDataType;
    }

    public int getDataSize() {
        return getComponentCount() * getElementSize(getDataType());
    }

    public int getComponentCount() {
        return this.mComponentCountActual;
    }

    protected void forceSetComponentCount(int count) {
        this.mComponentCountActual = count;
    }

    public boolean hasValue() {
        return this.mValue != null;
    }

    public boolean setValue(int[] value) {
        if (checkBadComponentCount(value.length)) {
            return false;
        }
        if (this.mDataType != (short) 3 && this.mDataType != (short) 9 && this.mDataType != (short) 4) {
            return false;
        }
        if (this.mDataType == (short) 3 && checkOverflowForUnsignedShort(value)) {
            return false;
        }
        if (this.mDataType == (short) 4 && checkOverflowForUnsignedLong(value)) {
            return false;
        }
        long[] data = new long[value.length];
        for (int i = 0; i < value.length; i++) {
            data[i] = (long) value[i];
        }
        this.mValue = data;
        this.mComponentCountActual = value.length;
        return true;
    }

    public boolean setValue(int value) {
        return setValue(new int[]{value});
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setValue(long[] value) {
        if (checkBadComponentCount(value.length) || this.mDataType != (short) 4 || checkOverflowForUnsignedLong(value)) {
            return false;
        }
        this.mValue = value;
        this.mComponentCountActual = value.length;
        return true;
    }

    public boolean setValue(long value) {
        return setValue(new long[]{value});
    }

    public boolean setValue(String value) {
        if (this.mDataType != (short) 2 && this.mDataType != (short) 7) {
            return false;
        }
        byte[] buf = value.getBytes(EXIF_TAG_CHARSET);
        byte[] finalBuf = buf;
        if (buf.length > 0) {
            finalBuf = (buf[buf.length + -1] == (byte) 0 || this.mDataType == (short) 7) ? buf : Arrays.copyOf(buf, buf.length + 1);
        } else if (this.mDataType == (short) 2 && this.mComponentCountActual == 1) {
            finalBuf = new byte[]{(byte) 0};
        }
        int count = finalBuf.length;
        if (checkBadComponentCount(count)) {
            return false;
        }
        this.mComponentCountActual = count;
        this.mValue = finalBuf;
        return true;
    }

    public boolean setValue(Rational[] value) {
        if (checkBadComponentCount(value.length)) {
            return false;
        }
        if (this.mDataType != (short) 5 && this.mDataType != (short) 10) {
            return false;
        }
        if (this.mDataType == (short) 5 && checkOverflowForUnsignedRational(value)) {
            return false;
        }
        if (this.mDataType == (short) 10 && checkOverflowForRational(value)) {
            return false;
        }
        this.mValue = value;
        this.mComponentCountActual = value.length;
        return true;
    }

    public boolean setValue(Rational value) {
        return setValue(new Rational[]{value});
    }

    public boolean setValue(byte[] value, int offset, int length) {
        if (checkBadComponentCount(length)) {
            return false;
        }
        if (this.mDataType != (short) 1 && this.mDataType != (short) 7) {
            return false;
        }
        this.mValue = new byte[length];
        System.arraycopy(value, offset, this.mValue, 0, length);
        this.mComponentCountActual = length;
        return true;
    }

    public boolean setValue(byte[] value) {
        return setValue(value, 0, value.length);
    }

    public boolean setValue(byte value) {
        return setValue(new byte[]{value});
    }

    public boolean setValue(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Short) {
            return setValue(((Short) obj).shortValue() & 65535);
        }
        if (obj instanceof String) {
            return setValue((String) obj);
        }
        if (obj instanceof int[]) {
            return setValue((int[]) obj);
        }
        if (obj instanceof long[]) {
            return setValue((long[]) obj);
        }
        if (obj instanceof Rational) {
            return setValue((Rational) obj);
        }
        if (obj instanceof Rational[]) {
            return setValue((Rational[]) obj);
        }
        if (obj instanceof byte[]) {
            return setValue((byte[]) obj);
        }
        if (obj instanceof Integer) {
            return setValue(((Integer) obj).intValue());
        }
        if (obj instanceof Long) {
            return setValue(((Long) obj).longValue());
        }
        if (obj instanceof Byte) {
            return setValue(((Byte) obj).byteValue());
        }
        int[] fin;
        int i;
        if (obj instanceof Short[]) {
            Short[] arr = (Short[]) obj;
            fin = new int[arr.length];
            for (i = 0; i < arr.length; i++) {
                fin[i] = arr[i] == null ? 0 : arr[i].shortValue() & 65535;
            }
            return setValue(fin);
        } else if (obj instanceof Integer[]) {
            Integer[] arr2 = (Integer[]) obj;
            fin = new int[arr2.length];
            for (i = 0; i < arr2.length; i++) {
                fin[i] = arr2[i] == null ? 0 : arr2[i].intValue();
            }
            return setValue(fin);
        } else if (obj instanceof Long[]) {
            Long[] arr3 = (Long[]) obj;
            long[] fin2 = new long[arr3.length];
            for (i = 0; i < arr3.length; i++) {
                fin2[i] = arr3[i] == null ? 0 : arr3[i].longValue();
            }
            return setValue(fin2);
        } else if (!(obj instanceof Byte[])) {
            return false;
        } else {
            Byte[] arr4 = (Byte[]) obj;
            byte[] fin3 = new byte[arr4.length];
            for (i = 0; i < arr4.length; i++) {
                fin3[i] = arr4[i] == null ? (byte) 0 : arr4[i].byteValue();
            }
            return setValue(fin3);
        }
    }

    public String getValueAsString() {
        if (this.mValue == null) {
            return null;
        }
        if (this.mValue instanceof String) {
            return (String) this.mValue;
        }
        if (this.mValue instanceof byte[]) {
            return new String((byte[]) this.mValue, EXIF_TAG_CHARSET);
        }
        return null;
    }

    public byte[] getValueAsBytes() {
        if (this.mValue instanceof byte[]) {
            return (byte[]) this.mValue;
        }
        return null;
    }

    public Rational[] getValueAsRationals() {
        if (this.mValue instanceof Rational[]) {
            return (Rational[]) this.mValue;
        }
        return null;
    }

    public Rational getValueAsRational(Rational defaultValue) {
        Rational[] r = getValueAsRationals();
        if (r == null || r.length < 1) {
            return defaultValue;
        }
        return r[0];
    }

    public Rational getValueAsRational(long defaultValue) {
        return getValueAsRational(new Rational(defaultValue, 1));
    }

    public int[] getValueAsInts() {
        if (this.mValue == null || !(this.mValue instanceof long[])) {
            return null;
        }
        long[] val = this.mValue;
        int[] arr = new int[val.length];
        for (int i = 0; i < val.length; i++) {
            arr[i] = (int) val[i];
        }
        return arr;
    }

    public int getValueAsInt(int defaultValue) {
        int[] i = getValueAsInts();
        if (i == null || i.length < 1) {
            return defaultValue;
        }
        return i[0];
    }

    public long[] getValueAsLongs() {
        if (this.mValue instanceof long[]) {
            return (long[]) this.mValue;
        }
        return null;
    }

    public Object getValue() {
        return this.mValue;
    }

    public long forceGetValueAsLong(long defaultValue) {
        long[] l = getValueAsLongs();
        if (l != null && l.length >= 1) {
            return l[0];
        }
        byte[] b = getValueAsBytes();
        if (b != null && b.length >= 1) {
            return (long) b[0];
        }
        Rational[] r = getValueAsRationals();
        if (r == null || r.length < 1 || r[0].getDenominator() == 0) {
            return defaultValue;
        }
        return (long) r[0].toDouble();
    }

    public String forceGetValueAsString() {
        if (this.mValue == null) {
            return "";
        }
        if (this.mValue instanceof byte[]) {
            if (this.mDataType == (short) 2) {
                return new String((byte[]) this.mValue, EXIF_TAG_CHARSET);
            }
            return Arrays.toString((byte[]) this.mValue);
        } else if (this.mValue instanceof long[]) {
            if (((long[]) this.mValue).length == 1) {
                return String.valueOf(((long[]) this.mValue)[0]);
            }
            return Arrays.toString((long[]) this.mValue);
        } else if (!(this.mValue instanceof Object[])) {
            return this.mValue.toString();
        } else {
            if (((Object[]) this.mValue).length != 1) {
                return Arrays.toString((Object[]) this.mValue);
            }
            Object val = ((Object[]) this.mValue)[0];
            if (val == null) {
                return "";
            }
            return val.toString();
        }
    }

    protected long getValueAt(int index) {
        if (this.mValue instanceof long[]) {
            return ((long[]) this.mValue)[index];
        }
        if (this.mValue instanceof byte[]) {
            return (long) ((byte[]) this.mValue)[index];
        }
        throw new IllegalArgumentException("Cannot get integer value from " + convertTypeToString(this.mDataType));
    }

    protected byte[] getStringByte() {
        return (byte[]) this.mValue;
    }

    protected Rational getRational(int index) {
        if (this.mDataType == (short) 10 || this.mDataType == (short) 5) {
            return ((Rational[]) this.mValue)[index];
        }
        throw new IllegalArgumentException("Cannot get RATIONAL value from " + convertTypeToString(this.mDataType));
    }

    protected void getBytes(byte[] buf) {
        getBytes(buf, 0, buf.length);
    }

    protected void getBytes(byte[] buf, int offset, int length) {
        if (this.mDataType == (short) 7 || this.mDataType == (short) 1) {
            Object obj = this.mValue;
            if (length > this.mComponentCountActual) {
                length = this.mComponentCountActual;
            }
            System.arraycopy(obj, 0, buf, offset, length);
            return;
        }
        throw new IllegalArgumentException("Cannot get BYTE value from " + convertTypeToString(this.mDataType));
    }

    protected int getOffset() {
        return this.mOffset;
    }

    protected void setOffset(int offset) {
        this.mOffset = offset;
    }

    protected void setHasDefinedCount(boolean d) {
        this.mHasDefinedDefaultComponentCount = d;
    }

    protected boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    private boolean checkBadComponentCount(int count) {
        if (!this.mHasDefinedDefaultComponentCount || this.mComponentCountActual == count) {
            return false;
        }
        return true;
    }

    private static String convertTypeToString(short type) {
        switch (type) {
            case (short) 1:
                return "UNSIGNED_BYTE";
            case (short) 2:
                return "ASCII";
            case (short) 3:
                return "UNSIGNED_SHORT";
            case (short) 4:
                return "UNSIGNED_LONG";
            case (short) 5:
                return "UNSIGNED_RATIONAL";
            case (short) 7:
                return "UNDEFINED";
            case (short) 9:
                return "LONG";
            case (short) 10:
                return "RATIONAL";
            default:
                return "";
        }
    }

    private boolean checkOverflowForUnsignedShort(int[] value) {
        for (int v : value) {
            if (v > 65535 || v < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(long[] value) {
        for (long v : value) {
            if (v < 0 || v > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(int[] value) {
        for (int v : value) {
            if (v < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedRational(Rational[] value) {
        for (Rational v : value) {
            if (v.getNumerator() < 0 || v.getDenominator() < 0 || v.getNumerator() > 4294967295L || v.getDenominator() > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForRational(Rational[] value) {
        for (Rational v : value) {
            if (v.getNumerator() < -2147483648L || v.getDenominator() < -2147483648L || v.getNumerator() > 2147483647L || v.getDenominator() > 2147483647L) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof ExifTag)) {
            return false;
        }
        ExifTag tag = (ExifTag) obj;
        if (tag.mTagId != this.mTagId || tag.mComponentCountActual != this.mComponentCountActual || tag.mDataType != this.mDataType) {
            return false;
        }
        if (this.mValue == null) {
            if (tag.mValue == null) {
                z = true;
            }
            return z;
        } else if (tag.mValue == null) {
            return false;
        } else {
            if (this.mValue instanceof long[]) {
                if (tag.mValue instanceof long[]) {
                    return Arrays.equals((long[]) this.mValue, (long[]) tag.mValue);
                }
                return false;
            } else if (this.mValue instanceof Rational[]) {
                if (tag.mValue instanceof Rational[]) {
                    return Arrays.equals((Rational[]) this.mValue, (Rational[]) tag.mValue);
                }
                return false;
            } else if (!(this.mValue instanceof byte[])) {
                return this.mValue.equals(tag.mValue);
            } else {
                if (tag.mValue instanceof byte[]) {
                    return Arrays.equals((byte[]) this.mValue, (byte[]) tag.mValue);
                }
                return false;
            }
        }
    }

    public String toString() {
        return String.format("tag id: %04X%n", new Object[]{Short.valueOf(this.mTagId)}) + "ifd id: " + this.mIfd + "\ntype: " + convertTypeToString(this.mDataType) + "\ncount: " + this.mComponentCountActual + "\noffset: " + this.mOffset + "\nvalue: " + forceGetValueAsString() + "\n";
    }
}
