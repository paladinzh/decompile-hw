package com.coremedia.iso.boxes.apple;

public final class AppleGroupingBox extends AbstractAppleMetaDataBox {
    public AppleGroupingBox() {
        super("©grp");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
