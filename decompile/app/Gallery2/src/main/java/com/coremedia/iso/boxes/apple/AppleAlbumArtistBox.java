package com.coremedia.iso.boxes.apple;

public class AppleAlbumArtistBox extends AbstractAppleMetaDataBox {
    public AppleAlbumArtistBox() {
        super("aART");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
