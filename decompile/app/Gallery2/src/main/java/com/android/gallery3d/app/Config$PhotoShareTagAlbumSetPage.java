package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;

public class Config$PhotoShareTagAlbumSetPage extends Config$CommonAlbumSetPage {
    private static Config$PhotoShareTagAlbumSetPage sInstance;

    public static synchronized Config$PhotoShareTagAlbumSetPage get(Context context) {
        Config$PhotoShareTagAlbumSetPage config$PhotoShareTagAlbumSetPage;
        synchronized (Config$PhotoShareTagAlbumSetPage.class) {
            if (sInstance == null) {
                sInstance = new Config$PhotoShareTagAlbumSetPage(context);
            }
            config$PhotoShareTagAlbumSetPage = sInstance;
        }
        return config$PhotoShareTagAlbumSetPage;
    }

    private Config$PhotoShareTagAlbumSetPage(Context context) {
        super(context);
        Resources r = context.getResources();
        this.slotViewSpec.port_slot_count = r.getInteger(R.integer.port_tag_albumSet_count);
        this.slotViewSpec.land_slot_count = r.getInteger(R.integer.land_tag_albumSet_count);
        this.slotViewSpec.album_name_text_height = r.getDimensionPixelSize(R.dimen.tag_albumSet_text_size);
    }
}
