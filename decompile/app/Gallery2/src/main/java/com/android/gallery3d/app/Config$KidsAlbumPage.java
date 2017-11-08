package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;

public class Config$KidsAlbumPage {
    private static Config$KidsAlbumPage sInstance;
    public int placeholderColor;
    public Spec slotViewSpec = new Spec();

    public static synchronized Config$KidsAlbumPage get(Context context) {
        Config$KidsAlbumPage config$KidsAlbumPage;
        synchronized (Config$KidsAlbumPage.class) {
            if (sInstance == null) {
                sInstance = new Config$KidsAlbumPage(context);
            }
            config$KidsAlbumPage = sInstance;
        }
        return config$KidsAlbumPage;
    }

    private Config$KidsAlbumPage(Context context) {
        Resources r = context.getResources();
        this.placeholderColor = r.getColor(R.color.album_placeholder);
        this.slotViewSpec.slot_vertical_padding = r.getDimensionPixelSize(R.dimen.kids_album_slot_top_padding);
        this.slotViewSpec.slot_horizontal_padding = r.getDimensionPixelSize(R.dimen.kids_album_slot_left_padding);
        this.slotViewSpec.slotWidth = r.getDimensionPixelSize(R.dimen.kids_photo_width);
        this.slotViewSpec.slotHeight = r.getDimensionPixelSize(R.dimen.kids_photo_height);
    }
}
