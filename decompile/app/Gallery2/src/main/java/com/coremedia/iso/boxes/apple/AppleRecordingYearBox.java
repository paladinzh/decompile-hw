package com.coremedia.iso.boxes.apple;

public class AppleRecordingYearBox extends AbstractAppleMetaDataBox {
    public AppleRecordingYearBox() {
        super("©day");
        this.appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}
