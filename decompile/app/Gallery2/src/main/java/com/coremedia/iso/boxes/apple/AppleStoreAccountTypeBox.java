package com.coremedia.iso.boxes.apple;

public class AppleStoreAccountTypeBox extends AbstractAppleMetaDataBox {
    public AppleStoreAccountTypeBox() {
        super("akID");
        this.appleDataBox = AppleDataBox.getUint8AppleDataBox();
    }
}
