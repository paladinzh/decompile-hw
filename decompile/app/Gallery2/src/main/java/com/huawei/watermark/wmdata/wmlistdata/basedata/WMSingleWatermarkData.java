package com.huawei.watermark.wmdata.wmlistdata.basedata;

import android.content.Context;
import com.huawei.watermark.wmutil.WMBaseUtil;

public abstract class WMSingleWatermarkData {
    private int mIndex = -1;
    public String mPath = "";
    private String mSupportLanguage = "";
    private String mThumbnailFileName;

    public abstract String consWMThumbnailFileName(Context context);

    public WMSingleWatermarkData(Context context, String name, String type, String path, int index) {
        this.mPath = path;
        this.mIndex = index;
        this.mThumbnailFileName = consWMThumbnailFileName(context);
        this.mSupportLanguage = WMBaseUtil.getSupportLanguage(context, this.mPath);
    }

    public String getWMPath() {
        return this.mPath;
    }

    public String getWMThumbnailFileName() {
        return this.mThumbnailFileName;
    }

    public String getWMSupportLanguage() {
        return this.mSupportLanguage;
    }

    public int getWMIndex() {
        return this.mIndex;
    }
}
