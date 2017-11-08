package com.huawei.watermark.wmdata.wmlistdata;

import android.content.Context;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleWatermarkData;
import com.huawei.watermark.wmutil.WMFileUtil;

public class WMSingleWatermarkDataDZ extends WMSingleWatermarkData {
    public WMSingleWatermarkDataDZ(Context context, String name, String type, String path, int index) {
        super(context, name, type, path, index);
    }

    public String consWMThumbnailFileName(Context context) {
        String res = "wm_thumbnail_en_2.png";
        if (WMFileUtil.isFileExist(context, this.mPath, res)) {
            return res;
        }
        res = "wm_thumbnail_en.png";
        if (WMFileUtil.isFileExist(context, this.mPath, res)) {
            return res;
        }
        return "wm_thumbnail.png";
    }
}
