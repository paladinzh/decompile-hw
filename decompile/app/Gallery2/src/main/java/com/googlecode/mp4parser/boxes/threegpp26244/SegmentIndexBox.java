package com.googlecode.mp4parser.boxes.threegpp26244;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitWriterBuffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SegmentIndexBox extends AbstractFullBox {
    long earliestPresentationTime;
    List<Entry> entries = new ArrayList();
    long firstOffset;
    long referenceId;
    int reserved;
    long timeScale;

    public static class Entry {
        byte referenceType;
        int referencedSize;
        int sapDeltaTime;
        byte sapType;
        byte startsWithSap;
        long subsegmentDuration;

        public byte getReferenceType() {
            return this.referenceType;
        }

        public void setReferenceType(byte referenceType) {
            this.referenceType = referenceType;
        }

        public int getReferencedSize() {
            return this.referencedSize;
        }

        public void setReferencedSize(int referencedSize) {
            this.referencedSize = referencedSize;
        }

        public long getSubsegmentDuration() {
            return this.subsegmentDuration;
        }

        public void setSubsegmentDuration(long subsegmentDuration) {
            this.subsegmentDuration = subsegmentDuration;
        }

        public byte getStartsWithSap() {
            return this.startsWithSap;
        }

        public void setStartsWithSap(byte startsWithSap) {
            this.startsWithSap = startsWithSap;
        }

        public byte getSapType() {
            return this.sapType;
        }

        public void setSapType(byte sapType) {
            this.sapType = sapType;
        }

        public int getSapDeltaTime() {
            return this.sapDeltaTime;
        }

        public void setSapDeltaTime(int sapDeltaTime) {
            this.sapDeltaTime = sapDeltaTime;
        }

        public String toString() {
            return "Entry{referenceType=" + this.referenceType + ", referencedSize=" + this.referencedSize + ", subsegmentDuration=" + this.subsegmentDuration + ", startsWithSap=" + this.startsWithSap + ", sapType=" + this.sapType + ", sapDeltaTime=" + this.sapDeltaTime + '}';
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry) o;
            return this.referenceType == entry.referenceType && this.referencedSize == entry.referencedSize && this.sapDeltaTime == entry.sapDeltaTime && this.sapType == entry.sapType && this.startsWithSap == entry.startsWithSap && this.subsegmentDuration == entry.subsegmentDuration;
        }

        public int hashCode() {
            return (((((((((this.referenceType * 31) + this.referencedSize) * 31) + ((int) (this.subsegmentDuration ^ (this.subsegmentDuration >>> 32)))) * 31) + this.startsWithSap) * 31) + this.sapType) * 31) + this.sapDeltaTime;
        }
    }

    public SegmentIndexBox() {
        super("sidx");
    }

    protected long getContentSize() {
        return ((((8 + 4) + ((long) (getVersion() == 0 ? 8 : 16))) + 2) + 2) + ((long) (this.entries.size() * 12));
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.referenceId);
        IsoTypeWriter.writeUInt32(byteBuffer, this.timeScale);
        if (getVersion() == 0) {
            IsoTypeWriter.writeUInt32(byteBuffer, this.earliestPresentationTime);
            IsoTypeWriter.writeUInt32(byteBuffer, this.firstOffset);
        } else {
            IsoTypeWriter.writeUInt64(byteBuffer, this.earliestPresentationTime);
            IsoTypeWriter.writeUInt64(byteBuffer, this.firstOffset);
        }
        IsoTypeWriter.writeUInt16(byteBuffer, this.reserved);
        IsoTypeWriter.writeUInt16(byteBuffer, this.entries.size());
        for (Entry entry : this.entries) {
            BitWriterBuffer b = new BitWriterBuffer(byteBuffer);
            b.writeBits(entry.getReferenceType(), 1);
            b.writeBits(entry.getReferencedSize(), 31);
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getSubsegmentDuration());
            b = new BitWriterBuffer(byteBuffer);
            b.writeBits(entry.getStartsWithSap(), 1);
            b.writeBits(entry.getSapType(), 3);
            b.writeBits(entry.getSapDeltaTime(), 28);
        }
    }

    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.referenceId = IsoTypeReader.readUInt32(content);
        this.timeScale = IsoTypeReader.readUInt32(content);
        if (getVersion() == 0) {
            this.earliestPresentationTime = IsoTypeReader.readUInt32(content);
            this.firstOffset = IsoTypeReader.readUInt32(content);
        } else {
            this.earliestPresentationTime = IsoTypeReader.readUInt64(content);
            this.firstOffset = IsoTypeReader.readUInt64(content);
        }
        this.reserved = IsoTypeReader.readUInt16(content);
        int numEntries = IsoTypeReader.readUInt16(content);
        for (int i = 0; i < numEntries; i++) {
            BitReaderBuffer b = new BitReaderBuffer(content);
            Entry e = new Entry();
            e.setReferenceType((byte) b.readBits(1));
            e.setReferencedSize(b.readBits(31));
            e.setSubsegmentDuration(IsoTypeReader.readUInt32(content));
            b = new BitReaderBuffer(content);
            e.setStartsWithSap((byte) b.readBits(1));
            e.setSapType((byte) b.readBits(3));
            e.setSapDeltaTime(b.readBits(28));
            this.entries.add(e);
        }
    }
}
