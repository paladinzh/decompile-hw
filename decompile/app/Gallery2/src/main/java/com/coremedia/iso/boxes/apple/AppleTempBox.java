package com.coremedia.iso.boxes.apple;

public final class AppleTempBox extends AbstractAppleMetaDataBox {
    public AppleTempBox() {
        super("tmpo");
        this.appleDataBox = AppleDataBox.getUint16AppleDataBox();
    }
}
