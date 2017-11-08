package com.coremedia.iso.boxes.apple;

public final class AppleTvSeasonBox extends AbstractAppleMetaDataBox {
    public AppleTvSeasonBox() {
        super("tvsn");
        this.appleDataBox = AppleDataBox.getUint32AppleDataBox();
    }
}
