package com.huawei.systemmanager.spacecleanner.engine.jpeg;

import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;

public class JpegCode {
    private static final String TAG = "JpegCode";
    private int height = -1;
    private String path;
    private int width = -1;
    private byte[] yuvData;

    public JpegCode(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isDataValid() {
        if (TextUtils.isEmpty(this.path)) {
            HwLog.i(TAG, "path is null");
            return false;
        } else if (this.width <= 0 || this.height <= 0) {
            HwLog.i(TAG, "width or height is null");
            return false;
        } else if (this.yuvData != null && this.yuvData.length > 0) {
            return true;
        } else {
            HwLog.i(TAG, "yuvData is null");
            return false;
        }
    }

    @FindBugsSuppressWarnings({"UWF_UNWRITTEN_FIELD"})
    public static int isBlur(JpegCode jpegCode) {
        return JpegNative.getInstance().checkPhotoBlur(jpegCode.yuvData, jpegCode.width, jpegCode.height);
    }
}
