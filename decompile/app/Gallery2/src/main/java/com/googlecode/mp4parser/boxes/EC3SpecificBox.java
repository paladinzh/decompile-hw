package com.googlecode.mp4parser.boxes;

import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitWriterBuffer;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class EC3SpecificBox extends AbstractBox {
    int dataRate;
    List<Entry> entries = new LinkedList();
    int numIndSub;

    public static class Entry {
        public int acmod;
        public int bsid;
        public int bsmod;
        public int chan_loc;
        public int fscod;
        public int lfeon;
        public int num_dep_sub;
        public int reserved;
        public int reserved2;

        public String toString() {
            return "Entry{fscod=" + this.fscod + ", bsid=" + this.bsid + ", bsmod=" + this.bsmod + ", acmod=" + this.acmod + ", lfeon=" + this.lfeon + ", reserved=" + this.reserved + ", num_dep_sub=" + this.num_dep_sub + ", chan_loc=" + this.chan_loc + ", reserved2=" + this.reserved2 + '}';
        }
    }

    public EC3SpecificBox() {
        super("dec3");
    }

    public long getContentSize() {
        long size = 2;
        for (Entry entry : this.entries) {
            if (entry.num_dep_sub > 0) {
                size += 4;
            } else {
                size += 3;
            }
        }
        return size;
    }

    public void _parseDetails(ByteBuffer content) {
        BitReaderBuffer brb = new BitReaderBuffer(content);
        this.dataRate = brb.readBits(13);
        this.numIndSub = brb.readBits(3) + 1;
        for (int i = 0; i < this.numIndSub; i++) {
            Entry e = new Entry();
            e.fscod = brb.readBits(2);
            e.bsid = brb.readBits(5);
            e.bsmod = brb.readBits(5);
            e.acmod = brb.readBits(3);
            e.lfeon = brb.readBits(1);
            e.reserved = brb.readBits(3);
            e.num_dep_sub = brb.readBits(4);
            if (e.num_dep_sub > 0) {
                e.chan_loc = brb.readBits(9);
            } else {
                e.reserved2 = brb.readBits(1);
            }
            this.entries.add(e);
        }
    }

    public void getContent(ByteBuffer byteBuffer) {
        BitWriterBuffer bwb = new BitWriterBuffer(byteBuffer);
        bwb.writeBits(this.dataRate, 13);
        bwb.writeBits(this.entries.size() - 1, 3);
        for (Entry e : this.entries) {
            bwb.writeBits(e.fscod, 2);
            bwb.writeBits(e.bsid, 5);
            bwb.writeBits(e.bsmod, 5);
            bwb.writeBits(e.acmod, 3);
            bwb.writeBits(e.lfeon, 1);
            bwb.writeBits(e.reserved, 3);
            bwb.writeBits(e.num_dep_sub, 4);
            if (e.num_dep_sub > 0) {
                bwb.writeBits(e.chan_loc, 9);
            } else {
                bwb.writeBits(e.reserved2, 1);
            }
        }
    }
}
