package com.coremedia.iso.boxes.apple;

public final class AppleTrackAuthorBox extends AbstractAppleMetaDataBox {
    public AppleTrackAuthorBox() {
        super("©wrt");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
