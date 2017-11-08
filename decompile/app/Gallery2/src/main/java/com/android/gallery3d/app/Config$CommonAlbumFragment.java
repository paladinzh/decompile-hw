package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;

public class Config$CommonAlbumFragment {
    private static Config$CommonAlbumFragment sInstance;
    public int placeholderColor;
    public Spec slotViewSpec = new Spec();

    public static synchronized Config$CommonAlbumFragment get(Context context) {
        Config$CommonAlbumFragment config$CommonAlbumFragment;
        synchronized (Config$CommonAlbumFragment.class) {
            if (sInstance == null) {
                sInstance = new Config$CommonAlbumFragment(context);
            }
            config$CommonAlbumFragment = sInstance;
        }
        return config$CommonAlbumFragment;
    }

    private Config$CommonAlbumFragment(Context context) {
        Resources r = context.getResources();
        this.placeholderColor = r.getColor(R.color.album_placeholder);
        this.slotViewSpec.slot_vertical_padding = r.getDimensionPixelSize(R.dimen.album_slot_top_padding);
        this.slotViewSpec.slot_horizontal_padding = r.getDimensionPixelSize(R.dimen.album_slot_horizontal_padding);
        this.slotViewSpec.slot_gap = r.getDimensionPixelSize(R.dimen.album_slot_gap);
        this.slotViewSpec.port_slot_count = r.getInteger(R.integer.port_common_album_slot_count);
        this.slotViewSpec.land_slot_count = r.getInteger(R.integer.land_common_album_slot_count);
        this.slotViewSpec.camera_video_slot_horizontal_padding = r.getDimensionPixelSize(R.dimen.camera_video_album_slot_horizontal_padding);
        this.slotViewSpec.camera_video_slot_vertical_padding = r.getDimensionPixelSize(R.dimen.camera_video_album_slot_vertical_padding);
        this.slotViewSpec.camera_video_slot_gap = r.getDimensionPixelSize(R.dimen.camera_video_album_slot_gap);
        this.slotViewSpec.camera_video_port_slot_count = r.getInteger(R.integer.port_video_album_slot_count);
        this.slotViewSpec.camera_video_land_slot_count = r.getInteger(R.integer.land_video_album_slot_count);
    }
}
