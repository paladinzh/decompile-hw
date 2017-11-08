package com.huawei.watermark.wmdata.wmlistdata;

import android.content.Context;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleWatermarkData;

public class WMSingleWatermarkDataZH extends WMSingleWatermarkData {
    public WMSingleWatermarkDataZH(Context context, String name, String type, String path, int index) {
        super(context, name, type, path, index);
    }

    public String consWMThumbnailFileName(Context context) {
        return "wm_thumbnail.png";
    }
}
