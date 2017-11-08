package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.Path;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractSampleEncryptionBox extends AbstractFullBox {
    int algorithmId = -1;
    List<Entry> entries = new LinkedList();
    int ivSize = -1;
    byte[] kid = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};

    public class Entry {
        public byte[] iv;
        public List<Pair> pairs = new LinkedList();

        public class Pair {
            public int clear;
            public long encrypted;

            public Pair(int clear, long encrypted) {
                this.clear = clear;
                this.encrypted = encrypted;
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                Pair pair = (Pair) o;
                return this.clear == pair.clear && this.encrypted == pair.encrypted;
            }

            public int hashCode() {
                return (this.clear * 31) + ((int) (this.encrypted ^ (this.encrypted >>> 32)));
            }

            public String toString() {
                return "clr:" + this.clear + " enc:" + this.encrypted;
            }
        }

        public int getSize() {
            int size;
            if (AbstractSampleEncryptionBox.this.isOverrideTrackEncryptionBoxParameters()) {
                size = AbstractSampleEncryptionBox.this.ivSize;
            } else {
                size = this.iv.length;
            }
            if (AbstractSampleEncryptionBox.this.isSubSampleEncryption()) {
                size += 2;
                for (Pair pair : this.pairs) {
                    size += 6;
                }
            }
            return size;
        }

        public Pair createPair(int clear, long encrypted) {
            return new Pair(clear, encrypted);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry) o;
            if (new BigInteger(this.iv).equals(new BigInteger(entry.iv))) {
                return this.pairs == null ? entry.pairs == null : this.pairs.equals(entry.pairs);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result;
            int i = 0;
            if (this.iv != null) {
                result = Arrays.hashCode(this.iv);
            } else {
                result = 0;
            }
            int i2 = result * 31;
            if (this.pairs != null) {
                i = this.pairs.hashCode();
            }
            return i2 + i;
        }

        public String toString() {
            return "Entry{iv=" + Hex.encodeHex(this.iv) + ", pairs=" + this.pairs + '}';
        }
    }

    protected AbstractSampleEncryptionBox(String type) {
        super(type);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int useThisIvSize = -1;
        if ((getFlags() & 1) > 0) {
            this.algorithmId = IsoTypeReader.readUInt24(content);
            this.ivSize = IsoTypeReader.readUInt8(content);
            useThisIvSize = this.ivSize;
            this.kid = new byte[16];
            content.get(this.kid);
        } else {
            for (Box tkhd : Path.getPaths(this, "/moov[0]/trak/tkhd")) {
                if (((TrackHeaderBox) tkhd).getTrackId() == ((TrackFragmentHeaderBox) getParent().getBoxes(TrackFragmentHeaderBox.class).get(0)).getTrackId()) {
                    AbstractTrackEncryptionBox tenc = (AbstractTrackEncryptionBox) Path.getPath(tkhd, "../mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/tenc[0]");
                    if (tenc == null) {
                        tenc = (AbstractTrackEncryptionBox) Path.getPath(tkhd, "../mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/uuid[0]");
                    }
                    useThisIvSize = tenc.getDefaultIvSize();
                }
            }
        }
        long numOfEntries = IsoTypeReader.readUInt32(content);
        while (true) {
            long numOfEntries2 = numOfEntries - 1;
            if (numOfEntries > 0) {
                int i;
                Entry e = new Entry();
                if (useThisIvSize < 0) {
                    i = 8;
                } else {
                    i = useThisIvSize;
                }
                e.iv = new byte[i];
                content.get(e.iv);
                if ((getFlags() & 2) > 0) {
                    int numOfPairs = IsoTypeReader.readUInt16(content);
                    e.pairs = new LinkedList();
                    int numOfPairs2 = numOfPairs;
                    while (true) {
                        numOfPairs = numOfPairs2 - 1;
                        if (numOfPairs2 <= 0) {
                            break;
                        }
                        e.pairs.add(e.createPair(IsoTypeReader.readUInt16(content), IsoTypeReader.readUInt32(content)));
                        numOfPairs2 = numOfPairs;
                    }
                }
                this.entries.add(e);
                numOfEntries = numOfEntries2;
            } else {
                return;
            }
        }
    }

    public boolean isSubSampleEncryption() {
        return (getFlags() & 2) > 0;
    }

    public boolean isOverrideTrackEncryptionBoxParameters() {
        return (getFlags() & 1) > 0;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (isOverrideTrackEncryptionBoxParameters()) {
            IsoTypeWriter.writeUInt24(byteBuffer, this.algorithmId);
            IsoTypeWriter.writeUInt8(byteBuffer, this.ivSize);
            byteBuffer.put(this.kid);
        }
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.entries.size());
        for (Entry entry : this.entries) {
            if (isOverrideTrackEncryptionBoxParameters()) {
                byte[] ivFull = new byte[this.ivSize];
                System.arraycopy(entry.iv, 0, ivFull, this.ivSize - entry.iv.length, entry.iv.length);
                byteBuffer.put(ivFull);
            } else {
                byteBuffer.put(entry.iv);
            }
            if (isSubSampleEncryption()) {
                IsoTypeWriter.writeUInt16(byteBuffer, entry.pairs.size());
                for (Pair pair : entry.pairs) {
                    IsoTypeWriter.writeUInt16(byteBuffer, pair.clear);
                    IsoTypeWriter.writeUInt32(byteBuffer, pair.encrypted);
                }
            }
        }
    }

    protected long getContentSize() {
        long contentSize = 4;
        if (isOverrideTrackEncryptionBoxParameters()) {
            contentSize = 8 + ((long) this.kid.length);
        }
        contentSize += 4;
        for (Entry entry : this.entries) {
            contentSize += (long) entry.getSize();
        }
        return contentSize;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        super.getBox(os);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractSampleEncryptionBox that = (AbstractSampleEncryptionBox) o;
        if (this.algorithmId != that.algorithmId || this.ivSize != that.ivSize) {
            return false;
        }
        if (this.entries == null ? that.entries == null : this.entries.equals(that.entries)) {
            return Arrays.equals(this.kid, that.kid);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int i2 = ((this.algorithmId * 31) + this.ivSize) * 31;
        if (this.kid != null) {
            hashCode = Arrays.hashCode(this.kid);
        } else {
            hashCode = 0;
        }
        hashCode = (i2 + hashCode) * 31;
        if (this.entries != null) {
            i = this.entries.hashCode();
        }
        return hashCode + i;
    }
}
