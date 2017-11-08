package com.coremedia.iso.boxes.apple;

import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.watermark.manager.parse.util.WMLocationService;
import java.util.HashMap;
import java.util.Map;

public class AppleMediaTypeBox extends AbstractAppleMetaDataBox {
    private static Map<String, String> mediaTypes = new HashMap();

    static {
        mediaTypes.put("0", "Movie (is now 9)");
        mediaTypes.put("1", "Normal (Music)");
        mediaTypes.put(GpsMeasureMode.MODE_2_DIMENSIONAL, "Audiobook");
        mediaTypes.put("6", "Music Video");
        mediaTypes.put("9", "Movie");
        mediaTypes.put(WMLocationService.CITYNOWSELECTED, "TV Show");
        mediaTypes.put("11", "Booklet");
        mediaTypes.put("14", "Ringtone");
    }

    public AppleMediaTypeBox() {
        super("stik");
        this.appleDataBox = AppleDataBox.getUint8AppleDataBox();
    }
}
