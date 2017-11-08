package com.google.protobuf;

import com.google.protobuf.ByteString.ByteIterator;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

class RopeByteString extends ByteString {
    private static final int[] minLengthByDepth;
    private int hash;
    private final ByteString left;
    private final int leftLength;
    private final ByteString right;
    private final int totalLength;
    private final int treeDepth;

    private static class Balancer {
        private final Stack<ByteString> prefixesStack;

        private Balancer() {
            this.prefixesStack = new Stack();
        }

        private ByteString balance(ByteString byteString, ByteString byteString2) {
            doBalance(byteString);
            doBalance(byteString2);
            ByteString byteString3 = (ByteString) this.prefixesStack.pop();
            while (!this.prefixesStack.isEmpty()) {
                byteString3 = new RopeByteString((ByteString) this.prefixesStack.pop(), byteString3);
            }
            return byteString3;
        }

        private void doBalance(ByteString byteString) {
            if (byteString.isBalanced()) {
                insert(byteString);
            } else if (byteString instanceof RopeByteString) {
                RopeByteString ropeByteString = (RopeByteString) byteString;
                doBalance(ropeByteString.left);
                doBalance(ropeByteString.right);
            } else {
                throw new IllegalArgumentException("Has a new type of ByteString been created? Found " + byteString.getClass());
            }
        }

        private void insert(ByteString byteString) {
            int depthBinForLength = getDepthBinForLength(byteString.size());
            int i = RopeByteString.minLengthByDepth[depthBinForLength + 1];
            if (!this.prefixesStack.isEmpty() && ((ByteString) this.prefixesStack.peek()).size() < i) {
                int i2 = RopeByteString.minLengthByDepth[depthBinForLength];
                ByteString byteString2 = (ByteString) this.prefixesStack.pop();
                while (!this.prefixesStack.isEmpty() && ((ByteString) this.prefixesStack.peek()).size() < i2) {
                    byteString2 = new RopeByteString((ByteString) this.prefixesStack.pop(), byteString2);
                }
                byteString2 = new RopeByteString(byteString2, byteString);
                while (!this.prefixesStack.isEmpty()) {
                    if (((ByteString) this.prefixesStack.peek()).size() >= RopeByteString.minLengthByDepth[getDepthBinForLength(byteString2.size()) + 1]) {
                        break;
                    }
                    byteString2 = new RopeByteString((ByteString) this.prefixesStack.pop(), byteString2);
                }
                this.prefixesStack.push(byteString2);
                return;
            }
            this.prefixesStack.push(byteString);
        }

        private int getDepthBinForLength(int i) {
            int binarySearch = Arrays.binarySearch(RopeByteString.minLengthByDepth, i);
            if (binarySearch >= 0) {
                return binarySearch;
            }
            return (-(binarySearch + 1)) - 1;
        }
    }

    private static class PieceIterator implements Iterator<LiteralByteString> {
        private final Stack<RopeByteString> breadCrumbs;
        private LiteralByteString next;

        private PieceIterator(ByteString byteString) {
            this.breadCrumbs = new Stack();
            this.next = getLeafByLeft(byteString);
        }

        private LiteralByteString getLeafByLeft(ByteString byteString) {
            ByteString byteString2 = byteString;
            while (byteString2 instanceof RopeByteString) {
                RopeByteString ropeByteString = (RopeByteString) byteString2;
                this.breadCrumbs.push(ropeByteString);
                byteString2 = ropeByteString.left;
            }
            return (LiteralByteString) byteString2;
        }

        private LiteralByteString getNextNonEmptyLeaf() {
            while (!this.breadCrumbs.isEmpty()) {
                LiteralByteString leafByLeft = getLeafByLeft(((RopeByteString) this.breadCrumbs.pop()).right);
                if (!leafByLeft.isEmpty()) {
                    return leafByLeft;
                }
            }
            return null;
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public LiteralByteString next() {
            if (this.next != null) {
                LiteralByteString literalByteString = this.next;
                this.next = getNextNonEmptyLeaf();
                return literalByteString;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class RopeByteIterator implements ByteIterator {
        private ByteIterator bytes;
        int bytesRemaining;
        private final PieceIterator pieces;

        private RopeByteIterator() {
            this.pieces = new PieceIterator(RopeByteString.this);
            this.bytes = this.pieces.next().iterator();
            this.bytesRemaining = RopeByteString.this.size();
        }

        public boolean hasNext() {
            return this.bytesRemaining > 0;
        }

        public Byte next() {
            return Byte.valueOf(nextByte());
        }

        public byte nextByte() {
            if (!this.bytes.hasNext()) {
                this.bytes = this.pieces.next().iterator();
            }
            this.bytesRemaining--;
            return this.bytes.nextByte();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class RopeInputStream extends InputStream {
        private LiteralByteString currentPiece;
        private int currentPieceIndex;
        private int currentPieceOffsetInRope;
        private int currentPieceSize;
        private int mark;
        private PieceIterator pieceIterator;

        public RopeInputStream() {
            initialize();
        }

        public int read(byte[] bArr, int i, int i2) {
            if (bArr == null) {
                throw new NullPointerException();
            } else if (i >= 0 && i2 >= 0 && i2 <= bArr.length - i) {
                return readSkipInternal(bArr, i, i2);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public long skip(long j) {
            int i;
            int i2 = 1;
            if (j >= 0) {
                i = 1;
            } else {
                i = 0;
            }
            if (i == 0) {
                throw new IndexOutOfBoundsException();
            }
            if (j > 2147483647L) {
                i2 = 0;
            }
            if (i2 == 0) {
                j = 2147483647L;
            }
            return (long) readSkipInternal(null, 0, (int) j);
        }

        private int readSkipInternal(byte[] bArr, int i, int i2) {
            int i3 = i2;
            int i4 = i;
            while (i3 > 0) {
                advanceIfCurrentPieceFullyRead();
                if (this.currentPiece != null) {
                    int min = Math.min(this.currentPieceSize - this.currentPieceIndex, i3);
                    if (bArr != null) {
                        this.currentPiece.copyTo(bArr, this.currentPieceIndex, i4, min);
                        i4 += min;
                    }
                    this.currentPieceIndex += min;
                    i3 -= min;
                } else {
                    if (i3 == i2) {
                        return -1;
                    }
                    return i2 - i3;
                }
            }
            return i2 - i3;
        }

        public int read() throws IOException {
            advanceIfCurrentPieceFullyRead();
            if (this.currentPiece == null) {
                return -1;
            }
            LiteralByteString literalByteString = this.currentPiece;
            int i = this.currentPieceIndex;
            this.currentPieceIndex = i + 1;
            return literalByteString.byteAt(i) & 255;
        }

        public int available() throws IOException {
            return RopeByteString.this.size() - (this.currentPieceOffsetInRope + this.currentPieceIndex);
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int i) {
            this.mark = this.currentPieceOffsetInRope + this.currentPieceIndex;
        }

        public synchronized void reset() {
            initialize();
            readSkipInternal(null, 0, this.mark);
        }

        private void initialize() {
            this.pieceIterator = new PieceIterator(RopeByteString.this);
            this.currentPiece = this.pieceIterator.next();
            this.currentPieceSize = this.currentPiece.size();
            this.currentPieceIndex = 0;
            this.currentPieceOffsetInRope = 0;
        }

        private void advanceIfCurrentPieceFullyRead() {
            if (this.currentPiece != null && this.currentPieceIndex == this.currentPieceSize) {
                this.currentPieceOffsetInRope += this.currentPieceSize;
                this.currentPieceIndex = 0;
                if (this.pieceIterator.hasNext()) {
                    this.currentPiece = this.pieceIterator.next();
                    this.currentPieceSize = this.currentPiece.size();
                    return;
                }
                this.currentPiece = null;
                this.currentPieceSize = 0;
            }
        }
    }

    static {
        int i = 1;
        List arrayList = new ArrayList();
        int i2 = 1;
        while (i > 0) {
            arrayList.add(Integer.valueOf(i));
            int i3 = i2 + i;
            i2 = i;
            i = i3;
        }
        arrayList.add(Integer.valueOf(SpaceConst.SCANNER_TYPE_ALL));
        minLengthByDepth = new int[arrayList.size()];
        for (i = 0; i < minLengthByDepth.length; i++) {
            minLengthByDepth[i] = ((Integer) arrayList.get(i)).intValue();
        }
    }

    private RopeByteString(ByteString byteString, ByteString byteString2) {
        this.hash = 0;
        this.left = byteString;
        this.right = byteString2;
        this.leftLength = byteString.size();
        this.totalLength = this.leftLength + byteString2.size();
        this.treeDepth = Math.max(byteString.getTreeDepth(), byteString2.getTreeDepth()) + 1;
    }

    static ByteString concatenate(ByteString byteString, ByteString byteString2) {
        RopeByteString ropeByteString = !(byteString instanceof RopeByteString) ? null : (RopeByteString) byteString;
        if (byteString2.size() == 0) {
            return byteString;
        }
        if (byteString.size() == 0) {
            return byteString2;
        }
        int size = byteString.size() + byteString2.size();
        if (size < 128) {
            return concatenateBytes(byteString, byteString2);
        }
        if (ropeByteString != null && ropeByteString.right.size() + byteString2.size() < 128) {
            return new RopeByteString(ropeByteString.left, concatenateBytes(ropeByteString.right, byteString2));
        } else if (ropeByteString != null && ropeByteString.left.getTreeDepth() > ropeByteString.right.getTreeDepth() && ropeByteString.getTreeDepth() > byteString2.getTreeDepth()) {
            return new RopeByteString(ropeByteString.left, new RopeByteString(ropeByteString.right, byteString2));
        } else {
            if (size < minLengthByDepth[Math.max(byteString.getTreeDepth(), byteString2.getTreeDepth()) + 1]) {
                return new Balancer().balance(byteString, byteString2);
            }
            return new RopeByteString(byteString, byteString2);
        }
    }

    private static LiteralByteString concatenateBytes(ByteString byteString, ByteString byteString2) {
        int size = byteString.size();
        int size2 = byteString2.size();
        byte[] bArr = new byte[(size + size2)];
        byteString.copyTo(bArr, 0, 0, size);
        byteString2.copyTo(bArr, 0, size, size2);
        return new LiteralByteString(bArr);
    }

    static RopeByteString newInstanceForTest(ByteString byteString, ByteString byteString2) {
        return new RopeByteString(byteString, byteString2);
    }

    public byte byteAt(int i) {
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Index < 0: " + i);
        } else if (i > this.totalLength) {
            throw new ArrayIndexOutOfBoundsException("Index > length: " + i + SqlMarker.COMMA_SEPARATE + this.totalLength);
        } else if (i >= this.leftLength) {
            return this.right.byteAt(i - this.leftLength);
        } else {
            return this.left.byteAt(i);
        }
    }

    public int size() {
        return this.totalLength;
    }

    protected int getTreeDepth() {
        return this.treeDepth;
    }

    protected boolean isBalanced() {
        return this.totalLength >= minLengthByDepth[this.treeDepth];
    }

    public ByteString substring(int i, int i2) {
        if (i < 0) {
            throw new IndexOutOfBoundsException("Beginning index: " + i + " < 0");
        } else if (i2 <= this.totalLength) {
            int i3 = i2 - i;
            if (i3 < 0) {
                throw new IndexOutOfBoundsException("Beginning index larger than ending index: " + i + SqlMarker.COMMA_SEPARATE + i2);
            } else if (i3 == 0) {
                return ByteString.EMPTY;
            } else {
                if (i3 == this.totalLength) {
                    return this;
                }
                if (i2 <= this.leftLength) {
                    return this.left.substring(i, i2);
                }
                if (i < this.leftLength) {
                    return new RopeByteString(this.left.substring(i), this.right.substring(0, i2 - this.leftLength));
                }
                return this.right.substring(i - this.leftLength, i2 - this.leftLength);
            }
        } else {
            throw new IndexOutOfBoundsException("End index: " + i2 + " > " + this.totalLength);
        }
    }

    protected void copyToInternal(byte[] bArr, int i, int i2, int i3) {
        if (i + i3 <= this.leftLength) {
            this.left.copyToInternal(bArr, i, i2, i3);
        } else if (i < this.leftLength) {
            int i4 = this.leftLength - i;
            this.left.copyToInternal(bArr, i, i2, i4);
            this.right.copyToInternal(bArr, 0, i2 + i4, i3 - i4);
        } else {
            this.right.copyToInternal(bArr, i - this.leftLength, i2, i3);
        }
    }

    public void copyTo(ByteBuffer byteBuffer) {
        this.left.copyTo(byteBuffer);
        this.right.copyTo(byteBuffer);
    }

    public ByteBuffer asReadOnlyByteBuffer() {
        return ByteBuffer.wrap(toByteArray()).asReadOnlyBuffer();
    }

    public List<ByteBuffer> asReadOnlyByteBufferList() {
        List<ByteBuffer> arrayList = new ArrayList();
        PieceIterator pieceIterator = new PieceIterator(this);
        while (pieceIterator.hasNext()) {
            arrayList.add(pieceIterator.next().asReadOnlyByteBuffer());
        }
        return arrayList;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        this.left.writeTo(outputStream);
        this.right.writeTo(outputStream);
    }

    public String toString(String str) throws UnsupportedEncodingException {
        return new String(toByteArray(), str);
    }

    public boolean isValidUtf8() {
        if (this.right.partialIsValidUtf8(this.left.partialIsValidUtf8(0, 0, this.leftLength), 0, this.right.size()) != 0) {
            return false;
        }
        return true;
    }

    protected int partialIsValidUtf8(int i, int i2, int i3) {
        if (i2 + i3 <= this.leftLength) {
            return this.left.partialIsValidUtf8(i, i2, i3);
        }
        if (i2 >= this.leftLength) {
            return this.right.partialIsValidUtf8(i, i2 - this.leftLength, i3);
        }
        int i4 = this.leftLength - i2;
        return this.right.partialIsValidUtf8(this.left.partialIsValidUtf8(i, i2, i4), 0, i3 - i4);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ByteString)) {
            return false;
        }
        ByteString byteString = (ByteString) obj;
        if (this.totalLength != byteString.size()) {
            return false;
        }
        if (this.totalLength == 0) {
            return true;
        }
        if (this.hash != 0) {
            int peekCachedHashCode = byteString.peekCachedHashCode();
            if (!(peekCachedHashCode == 0 || this.hash == peekCachedHashCode)) {
                return false;
            }
        }
        return equalsFragments(byteString);
    }

    private boolean equalsFragments(ByteString byteString) {
        Iterator pieceIterator = new PieceIterator(this);
        LiteralByteString literalByteString = (LiteralByteString) pieceIterator.next();
        Iterator pieceIterator2 = new PieceIterator(byteString);
        LiteralByteString literalByteString2 = (LiteralByteString) pieceIterator2.next();
        int i = 0;
        LiteralByteString literalByteString3 = literalByteString;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int i4;
            int size = literalByteString3.size() - i2;
            int size2 = literalByteString2.size() - i;
            int min = Math.min(size, size2);
            if (i2 != 0 ? literalByteString2.equalsRange(literalByteString3, i2, min) : literalByteString3.equalsRange(literalByteString2, i, min)) {
                i4 = i3 + min;
                if (i4 >= this.totalLength) {
                    break;
                }
                int i5;
                if (min != size) {
                    i2 += min;
                } else {
                    literalByteString3 = (LiteralByteString) pieceIterator.next();
                    i2 = 0;
                }
                if (min != size2) {
                    LiteralByteString literalByteString4 = literalByteString2;
                    i5 = i + min;
                    literalByteString = literalByteString4;
                } else {
                    literalByteString = (LiteralByteString) pieceIterator2.next();
                    boolean z = false;
                }
                i = i5;
                literalByteString2 = literalByteString;
                i3 = i4;
            } else {
                return false;
            }
        }
        if (i4 == this.totalLength) {
            return true;
        }
        throw new IllegalStateException();
    }

    public int hashCode() {
        int i = this.hash;
        if (i == 0) {
            i = partialHash(this.totalLength, 0, this.totalLength);
            if (i == 0) {
                i = 1;
            }
            this.hash = i;
        }
        return i;
    }

    protected int peekCachedHashCode() {
        return this.hash;
    }

    protected int partialHash(int i, int i2, int i3) {
        if (i2 + i3 <= this.leftLength) {
            return this.left.partialHash(i, i2, i3);
        }
        if (i2 >= this.leftLength) {
            return this.right.partialHash(i, i2 - this.leftLength, i3);
        }
        int i4 = this.leftLength - i2;
        return this.right.partialHash(this.left.partialHash(i, i2, i4), 0, i3 - i4);
    }

    public CodedInputStream newCodedInput() {
        return CodedInputStream.newInstance(new RopeInputStream());
    }

    public InputStream newInput() {
        return new RopeInputStream();
    }

    public ByteIterator iterator() {
        return new RopeByteIterator();
    }
}
