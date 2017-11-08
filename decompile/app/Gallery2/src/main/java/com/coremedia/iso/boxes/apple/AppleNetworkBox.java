package com.coremedia.iso.boxes.apple;

public final class AppleNetworkBox extends AbstractAppleMetaDataBox {
    public AppleNetworkBox() {
        super("tvnn");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
