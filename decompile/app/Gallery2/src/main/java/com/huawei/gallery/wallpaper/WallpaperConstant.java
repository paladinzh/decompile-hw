package com.huawei.gallery.wallpaper;

import android.net.Uri;
import android.os.Environment;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import java.io.File;

public final class WallpaperConstant {
    public static final Class<?> CROP_WALLPAPER_CLASS = CropWallpaperActivity.class;
    public static final String DEFAULT_WALLPAPER_PATH = (PATH_WALLPAPER + File.separator + "gallery_home_wallpaper_0.jpg");
    public static final String PATH_WALLPAPER = (Environment.getDataDirectory() + "/skin/wallpaper");
    public static final Uri URI_THEMEINFO = Uri.parse("content://com.huawei.android.thememanager.ContentProvider/theme");
    public static final int VIEW_ITEM_SIZE = GalleryUtils.getContext().getResources().getDimensionPixelSize(R.dimen.category_panel_item_width);
}
