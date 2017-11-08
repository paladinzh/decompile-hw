package com.coremedia.iso.boxes.apple;

public final class AppleShowBox extends AbstractAppleMetaDataBox {
    public AppleShowBox() {
        super("tvsh");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
