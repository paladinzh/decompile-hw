package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class CompositionShiftLeastGreatestAtom extends AbstractFullBox {
    int compositionOffsetToDisplayOffsetShift;
    int displayEndTime;
    int displayStartTime;
    int greatestDisplayOffset;
    int leastDisplayOffset;

    public CompositionShiftLeastGreatestAtom() {
        super("cslg");
    }

    protected long getContentSize() {
        return 24;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.compositionOffsetToDisplayOffsetShift = content.getInt();
        this.leastDisplayOffset = content.getInt();
        this.greatestDisplayOffset = content.getInt();
        this.displayStartTime = content.getInt();
        this.displayEndTime = content.getInt();
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.putInt(this.compositionOffsetToDisplayOffsetShift);
        byteBuffer.putInt(this.leastDisplayOffset);
        byteBuffer.putInt(this.greatestDisplayOffset);
        byteBuffer.putInt(this.displayStartTime);
        byteBuffer.putInt(this.displayEndTime);
    }
}
