package com.coremedia.iso.boxes.apple;

public final class AppleTrackTitleBox extends AbstractAppleMetaDataBox {
    public AppleTrackTitleBox() {
        super("©nam");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
