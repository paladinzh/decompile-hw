package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;

public class Config$CommonAlbumSetPage {
    private static Config$CommonAlbumSetPage sInstance;
    public int placeholderColor;
    public Spec slotViewSpec;

    public static synchronized Config$CommonAlbumSetPage get(Context context) {
        Config$CommonAlbumSetPage config$CommonAlbumSetPage;
        synchronized (Config$CommonAlbumSetPage.class) {
            if (sInstance == null) {
                sInstance = new Config$CommonAlbumSetPage(context);
            }
            config$CommonAlbumSetPage = sInstance;
        }
        return config$CommonAlbumSetPage;
    }

    private Config$CommonAlbumSetPage(Context context) {
        Resources r = context.getResources();
        this.placeholderColor = r.getColor(R.color.album_placeholder);
        this.slotViewSpec = new Spec();
        this.slotViewSpec.slot_vertical_padding = r.getDimensionPixelSize(R.dimen.tag_albumSet_top_padding);
        this.slotViewSpec.slot_horizontal_padding = r.getDimensionPixelSize(R.dimen.tag_albumSet_horizontal_padding);
        this.slotViewSpec.slot_gap = r.getDimensionPixelSize(R.dimen.tag_albumSet_gap);
        this.slotViewSpec.port_slot_count = r.getInteger(R.integer.port_tag_albumSet_count);
        this.slotViewSpec.land_slot_count = r.getInteger(R.integer.land_tag_albumSet_count);
    }
}
