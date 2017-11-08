package com.coremedia.iso.boxes.apple;

public final class AppleStandardGenreBox extends AbstractAppleMetaDataBox {
    public AppleStandardGenreBox() {
        super("gnre");
        this.appleDataBox = AppleDataBox.getUint16AppleDataBox();
    }
}
