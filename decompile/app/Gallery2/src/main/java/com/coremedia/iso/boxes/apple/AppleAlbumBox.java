package com.coremedia.iso.boxes.apple;

public final class AppleAlbumBox extends AbstractAppleMetaDataBox {
    public AppleAlbumBox() {
        super("©alb");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
