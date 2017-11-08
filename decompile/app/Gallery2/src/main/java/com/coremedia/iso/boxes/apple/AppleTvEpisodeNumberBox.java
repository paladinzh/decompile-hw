package com.coremedia.iso.boxes.apple;

public class AppleTvEpisodeNumberBox extends AbstractAppleMetaDataBox {
    public AppleTvEpisodeNumberBox() {
        super("tven");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
