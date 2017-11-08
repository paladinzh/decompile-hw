package tmsdkobf;

import java.io.Serializable;

public abstract class fs implements Serializable {
    public static final byte BYTE = (byte) 0;
    public static final byte DOUBLE = (byte) 5;
    public static final byte FLOAT = (byte) 4;
    public static final byte INT = (byte) 2;
    public static final int JCE_MAX_STRING_LENGTH = 104857600;
    public static final byte LIST = (byte) 9;
    public static final byte LONG = (byte) 3;
    public static final byte MAP = (byte) 8;
    public static final byte SHORT = (byte) 1;
    public static final byte SIMPLE_LIST = (byte) 13;
    public static final byte STRING1 = (byte) 6;
    public static final byte STRING4 = (byte) 7;
    public static final byte STRUCT_BEGIN = (byte) 10;
    public static final byte STRUCT_END = (byte) 11;
    public static final byte ZERO_TAG = (byte) 12;

    public abstract void readFrom(fq fqVar);

    public abstract void writeTo(fr frVar);

    public void display(StringBuilder sb, int level) {
    }

    public void displaySimple(StringBuilder sb, int level) {
    }

    public fs newInit() {
        return null;
    }

    public void recyle() {
    }

    public boolean containField(String name) {
        return false;
    }

    public Object getFieldByName(String name) {
        return null;
    }

    public void setFieldByName(String name, Object value) {
    }

    public byte[] toByteArray() {
        fr os = new fr();
        writeTo(os);
        return os.toByteArray();
    }

    public byte[] toByteArray(String encoding) {
        fr os = new fr();
        os.ae(encoding);
        writeTo(os);
        return os.toByteArray();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        display(sb, 0);
        return sb.toString();
    }
}
