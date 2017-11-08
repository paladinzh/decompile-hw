package com.coremedia.iso.boxes.apple;

public final class AppleArtistBox extends AbstractAppleMetaDataBox {
    public AppleArtistBox() {
        super("©ART");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
