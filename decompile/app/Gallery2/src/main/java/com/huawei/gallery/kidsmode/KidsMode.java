package com.huawei.gallery.kidsmode;

import android.net.Uri;
import android.net.Uri.Builder;

public final class KidsMode {
    public static final Uri CAMERA_URI = new Builder().scheme("content").authority("com.huawei.kidsmode").appendPath("camera").build();
    public static final Uri MEDIA_URI = new Builder().scheme("content").authority("com.huawei.kidsmode").appendPath("media").build();
    public static final Uri PAINT_URI = new Builder().scheme("content").authority("com.huawei.kidsmode").appendPath("paint").build();
}
