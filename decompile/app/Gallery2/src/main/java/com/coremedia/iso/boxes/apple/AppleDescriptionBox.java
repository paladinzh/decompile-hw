package com.coremedia.iso.boxes.apple;

public final class AppleDescriptionBox extends AbstractAppleMetaDataBox {
    public AppleDescriptionBox() {
        super("desc");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
