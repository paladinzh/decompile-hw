package com.coremedia.iso.boxes.apple;

public final class AppleSortAlbumBox extends AbstractAppleMetaDataBox {
    public AppleSortAlbumBox() {
        super("soal");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
