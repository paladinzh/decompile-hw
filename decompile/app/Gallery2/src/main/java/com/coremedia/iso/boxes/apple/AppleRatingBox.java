package com.coremedia.iso.boxes.apple;

public final class AppleRatingBox extends AbstractAppleMetaDataBox {
    public AppleRatingBox() {
        super("rtng");
        this.appleDataBox = AppleDataBox.getUint8AppleDataBox();
    }
}
