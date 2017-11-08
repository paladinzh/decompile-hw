package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class SampleGroupDescriptionBox extends AbstractFullBox {
    private int defaultLength;
    private int descriptionLength;
    private List<GroupEntry> groupEntries = new LinkedList();
    private String groupingType;

    public SampleGroupDescriptionBox() {
        super("sgpd");
    }

    protected long getContentSize() {
        long size = 8;
        if (getVersion() == 1) {
            size = 12;
        }
        size += 4;
        for (GroupEntry groupEntry : this.groupEntries) {
            if (getVersion() == 1 && this.defaultLength == 0) {
                size += 4;
            }
            size += (long) groupEntry.size();
        }
        return size;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(this.groupingType.getBytes());
        if (getVersion() == 1) {
            IsoTypeWriter.writeUInt32(byteBuffer, (long) this.defaultLength);
        }
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.groupEntries.size());
        for (GroupEntry entry : this.groupEntries) {
            if (getVersion() == 1 && this.defaultLength == 0) {
                IsoTypeWriter.writeUInt32(byteBuffer, (long) entry.get().limit());
            }
            byteBuffer.put(entry.get());
        }
    }

    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() != 1) {
            throw new RuntimeException("SampleGroupDescriptionBox are only supported in version 1");
        }
        this.groupingType = IsoTypeReader.read4cc(content);
        if (getVersion() == 1) {
            this.defaultLength = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        }
        long entryCount = IsoTypeReader.readUInt32(content);
        while (true) {
            long entryCount2 = entryCount - 1;
            if (entryCount > 0) {
                int length = this.defaultLength;
                if (getVersion() == 1) {
                    if (this.defaultLength == 0) {
                        this.descriptionLength = CastUtils.l2i(IsoTypeReader.readUInt32(content));
                        length = this.descriptionLength;
                    }
                    int finalPos = content.position() + length;
                    ByteBuffer parseMe = content.slice();
                    parseMe.limit(length);
                    this.groupEntries.add(parseGroupEntry(parseMe, this.groupingType));
                    content.position(finalPos);
                    entryCount = entryCount2;
                } else {
                    throw new RuntimeException("This should be implemented");
                }
            }
            return;
        }
    }

    private GroupEntry parseGroupEntry(ByteBuffer content, String groupingType) {
        GroupEntry groupEntry;
        if ("roll".equals(groupingType)) {
            groupEntry = new RollRecoveryEntry();
        } else if ("rash".equals(groupingType)) {
            groupEntry = new RateShareEntry();
        } else if ("seig".equals(groupingType)) {
            groupEntry = new CencSampleEncryptionInformationGroupEntry();
        } else if ("rap ".equals(groupingType)) {
            groupEntry = new VisualRandomAccessEntry();
        } else if ("tele".equals(groupingType)) {
            groupEntry = new TemporalLevelEntry();
        } else {
            groupEntry = new UnknownEntry();
        }
        groupEntry.parse(content);
        return groupEntry;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SampleGroupDescriptionBox that = (SampleGroupDescriptionBox) o;
        if (this.defaultLength != that.defaultLength) {
            return false;
        }
        if (this.groupEntries == null ? that.groupEntries == null : this.groupEntries.equals(that.groupEntries)) {
            return this.groupingType == null ? that.groupingType == null : this.groupingType.equals(that.groupingType);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.groupingType != null) {
            result = this.groupingType.hashCode();
        } else {
            result = 0;
        }
        int i2 = ((result * 31) + this.defaultLength) * 31;
        if (this.groupEntries != null) {
            i = this.groupEntries.hashCode();
        }
        return i2 + i;
    }

    public String toString() {
        return "SampleGroupDescriptionBox{groupingType='" + this.groupingType + '\'' + ", defaultLength=" + this.defaultLength + ", groupEntries=" + this.groupEntries + '}';
    }
}
