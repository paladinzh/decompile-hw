package com.coremedia.iso.boxes.apple;

public final class AppleIdBox extends AbstractAppleMetaDataBox {
    public AppleIdBox() {
        super("apID");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
