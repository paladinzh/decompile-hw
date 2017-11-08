package com.coremedia.iso.boxes.apple;

public final class AppleEncoderBox extends AbstractAppleMetaDataBox {
    public AppleEncoderBox() {
        super("©too");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
