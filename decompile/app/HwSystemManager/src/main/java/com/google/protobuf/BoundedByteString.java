package com.google.protobuf;

import com.google.protobuf.ByteString.ByteIterator;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import java.util.NoSuchElementException;

class BoundedByteString extends LiteralByteString {
    private final int bytesLength;
    private final int bytesOffset;

    private class BoundedByteIterator implements ByteIterator {
        private final int limit;
        private int position;

        private BoundedByteIterator() {
            this.position = BoundedByteString.this.getOffsetIntoBytes();
            this.limit = this.position + BoundedByteString.this.size();
        }

        public boolean hasNext() {
            return this.position < this.limit;
        }

        public Byte next() {
            return Byte.valueOf(nextByte());
        }

        public byte nextByte() {
            if (this.position < this.limit) {
                byte[] bArr = BoundedByteString.this.bytes;
                int i = this.position;
                this.position = i + 1;
                return bArr[i];
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    BoundedByteString(byte[] bArr, int i, int i2) {
        Object obj = null;
        super(bArr);
        if (i < 0) {
            throw new IllegalArgumentException("Offset too small: " + i);
        } else if (i2 >= 0) {
            if (((long) i) + ((long) i2) <= ((long) bArr.length)) {
                obj = 1;
            }
            if (obj == null) {
                throw new IllegalArgumentException("Offset+Length too large: " + i + "+" + i2);
            }
            this.bytesOffset = i;
            this.bytesLength = i2;
        } else {
            throw new IllegalArgumentException("Length too small: " + i);
        }
    }

    public byte byteAt(int i) {
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Index too small: " + i);
        } else if (i < size()) {
            return this.bytes[this.bytesOffset + i];
        } else {
            throw new ArrayIndexOutOfBoundsException("Index too large: " + i + SqlMarker.COMMA_SEPARATE + size());
        }
    }

    public int size() {
        return this.bytesLength;
    }

    protected int getOffsetIntoBytes() {
        return this.bytesOffset;
    }

    protected void copyToInternal(byte[] bArr, int i, int i2, int i3) {
        System.arraycopy(this.bytes, getOffsetIntoBytes() + i, bArr, i2, i3);
    }

    public ByteIterator iterator() {
        return new BoundedByteIterator();
    }
}
