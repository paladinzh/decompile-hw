package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;

public class MovieFragmentBox extends AbstractContainerBox {
    public MovieFragmentBox() {
        super("moof");
    }

    @DoNotParseDetail
    public long getOffset() {
        long offset = 0;
        for (Box b = this; b.getParent() != null; b = b.getParent()) {
            for (Box box : b.getParent().getBoxes()) {
                if (b == box) {
                    break;
                }
                offset += box.getSize();
            }
        }
        return offset;
    }
}
