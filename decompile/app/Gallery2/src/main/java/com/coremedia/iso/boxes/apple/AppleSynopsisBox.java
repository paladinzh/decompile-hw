package com.coremedia.iso.boxes.apple;

public final class AppleSynopsisBox extends AbstractAppleMetaDataBox {
    public AppleSynopsisBox() {
        super("ldes");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
