package com.coremedia.iso.boxes.apple;

public final class ApplePurchaseDateBox extends AbstractAppleMetaDataBox {
    public ApplePurchaseDateBox() {
        super("purd");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
