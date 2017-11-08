package com.coremedia.iso.boxes.apple;

public final class AppleCommentBox extends AbstractAppleMetaDataBox {
    public AppleCommentBox() {
        super("©cmt");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
