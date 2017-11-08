package com.coremedia.iso.boxes.apple;

public final class AppleCustomGenreBox extends AbstractAppleMetaDataBox {
    public AppleCustomGenreBox() {
        super("©gen");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
