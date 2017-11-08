package java.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;

public final class StringBuffer extends AbstractStringBuilder implements Serializable, Appendable, CharSequence {
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("value", char[].class), new ObjectStreamField("count", Integer.TYPE), new ObjectStreamField("shared", Boolean.TYPE)};
    static final long serialVersionUID = 3388685877147921107L;

    public StringBuffer() {
        super(16);
    }

    public StringBuffer(int capacity) {
        super(capacity);
    }

    public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }

    public StringBuffer(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    public synchronized int length() {
        return this.count;
    }

    public synchronized int capacity() {
        return this.value.length;
    }

    public synchronized void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > this.value.length) {
            expandCapacity(minimumCapacity);
        }
    }

    public synchronized void trimToSize() {
        super.trimToSize();
    }

    public synchronized void setLength(int newLength) {
        super.setLength(newLength);
    }

    public synchronized char charAt(int index) {
        if (index >= 0) {
            if (index < this.count) {
            }
        }
        throw new StringIndexOutOfBoundsException(index);
        return this.value[index];
    }

    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public synchronized void setCharAt(int index, char ch) {
        if (index >= 0) {
            if (index < this.count) {
                this.value[index] = ch;
            }
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    public synchronized StringBuffer append(Object obj) {
        super.append(String.valueOf(obj));
        return this;
    }

    public synchronized StringBuffer append(String str) {
        super.append(str);
        return this;
    }

    public synchronized StringBuffer append(StringBuffer sb) {
        super.append(sb);
        return this;
    }

    public StringBuffer append(CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return append((String) s);
        }
        if (s instanceof StringBuffer) {
            return append((StringBuffer) s);
        }
        return append(s, 0, s.length());
    }

    public synchronized StringBuffer append(CharSequence s, int start, int end) {
        super.append(s, start, end);
        return this;
    }

    public synchronized StringBuffer append(char[] str) {
        super.append(str);
        return this;
    }

    public synchronized StringBuffer append(char[] str, int offset, int len) {
        super.append(str, offset, len);
        return this;
    }

    public synchronized StringBuffer append(boolean b) {
        super.append(b);
        return this;
    }

    public synchronized StringBuffer append(char c) {
        super.append(c);
        return this;
    }

    public synchronized StringBuffer append(int i) {
        super.append(i);
        return this;
    }

    public synchronized StringBuffer appendCodePoint(int codePoint) {
        super.appendCodePoint(codePoint);
        return this;
    }

    public synchronized StringBuffer append(long lng) {
        super.append(lng);
        return this;
    }

    public synchronized StringBuffer append(float f) {
        super.append(f);
        return this;
    }

    public synchronized StringBuffer append(double d) {
        super.append(d);
        return this;
    }

    public synchronized StringBuffer delete(int start, int end) {
        super.delete(start, end);
        return this;
    }

    public synchronized StringBuffer deleteCharAt(int index) {
        super.deleteCharAt(index);
        return this;
    }

    public synchronized StringBuffer replace(int start, int end, String str) {
        super.replace(start, end, str);
        return this;
    }

    public synchronized String substring(int start) {
        return substring(start, this.count);
    }

    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    public synchronized StringBuffer insert(int index, char[] str, int offset, int len) {
        super.insert(index, str, offset, len);
        return this;
    }

    public synchronized StringBuffer insert(int offset, Object obj) {
        super.insert(offset, String.valueOf(obj));
        return this;
    }

    public synchronized StringBuffer insert(int offset, String str) {
        super.insert(offset, str);
        return this;
    }

    public synchronized StringBuffer insert(int offset, char[] str) {
        super.insert(offset, str);
        return this;
    }

    public StringBuffer insert(int dstOffset, CharSequence s) {
        if (s == null) {
            s = "null";
        }
        if (s instanceof String) {
            return insert(dstOffset, (String) s);
        }
        return insert(dstOffset, s, 0, s.length());
    }

    public synchronized StringBuffer insert(int dstOffset, CharSequence s, int start, int end) {
        super.insert(dstOffset, s, start, end);
        return this;
    }

    public StringBuffer insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    public synchronized StringBuffer insert(int offset, char c) {
        super.insert(offset, c);
        return this;
    }

    public StringBuffer insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    public StringBuffer insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    public StringBuffer insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    public StringBuffer insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public synchronized int indexOf(String str, int fromIndex) {
        return String.indexOf(this.value, 0, this.count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, this.count);
    }

    public synchronized int lastIndexOf(String str, int fromIndex) {
        return String.lastIndexOf(this.value, 0, this.count, str.toCharArray(), 0, str.length(), fromIndex);
    }

    public synchronized StringBuffer reverse() {
        super.reverse();
        return this;
    }

    public synchronized String toString() {
        return new String(this.value, 0, this.count);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        PutField fields = s.putFields();
        fields.put("value", this.value);
        fields.put("count", this.count);
        fields.put("shared", false);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        GetField fields = s.readFields();
        this.value = (char[]) fields.get("value", null);
        this.count = fields.get("count", 0);
    }
}
