package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositionTimeToSample extends AbstractFullBox {
    static final /* synthetic */ boolean -assertionsDisabled = (!CompositionTimeToSample.class.desiredAssertionStatus());
    List<Entry> entries = Collections.emptyList();

    public static class Entry {
        int count;
        int offset;

        public Entry(int count, int offset) {
            this.count = count;
            this.offset = offset;
        }

        public int getCount() {
            return this.count;
        }

        public int getOffset() {
            return this.offset;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String toString() {
            return "Entry{count=" + this.count + ", offset=" + this.offset + '}';
        }
    }

    public CompositionTimeToSample() {
        super("ctts");
    }

    protected long getContentSize() {
        return (long) ((this.entries.size() * 8) + 8);
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int numberOfEntries = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.entries = new ArrayList(numberOfEntries);
        for (int i = 0; i < numberOfEntries; i++) {
            this.entries.add(new Entry(CastUtils.l2i(IsoTypeReader.readUInt32(content)), content.getInt()));
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.entries.size());
        for (Entry entry : this.entries) {
            IsoTypeWriter.writeUInt32(byteBuffer, (long) entry.getCount());
            byteBuffer.putInt(entry.getOffset());
        }
    }

    public static int[] blowupCompositionTimes(List<Entry> entries) {
        long numOfSamples = 0;
        for (Entry entry : entries) {
            numOfSamples += (long) entry.getCount();
        }
        if (!-assertionsDisabled) {
            if ((numOfSamples <= 2147483647L ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int[] decodingTime = new int[((int) numOfSamples)];
        int current = 0;
        for (Entry entry2 : entries) {
            int i = 0;
            while (i < entry2.getCount()) {
                int current2 = current + 1;
                decodingTime[current] = entry2.getOffset();
                i++;
                current = current2;
            }
        }
        return decodingTime;
    }
}
