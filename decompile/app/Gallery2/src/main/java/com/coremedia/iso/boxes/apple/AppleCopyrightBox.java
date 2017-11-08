package com.coremedia.iso.boxes.apple;

public final class AppleCopyrightBox extends AbstractAppleMetaDataBox {
    public AppleCopyrightBox() {
        super("cprt");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
