package com.huawei.gallery.photoshare.utils;

import android.net.Uri;
import com.huawei.gallery.provider.GalleryProvider;

public class PhotoShareConstants {
    public static final Uri AUTO_UPLOAD_ALBUM_TABLE_URI = Uri.withAppendedPath(GalleryProvider.BASE_URI, "auto_upload_album");
    public static final Uri CLOUD_ALBUM_TABLE_URI = Uri.withAppendedPath(GalleryProvider.BASE_URI, "cloud_album");
    public static final Uri CLOUD_FILE_TABLE_URI = Uri.withAppendedPath(GalleryProvider.BASE_URI, "cloud_file");
}
