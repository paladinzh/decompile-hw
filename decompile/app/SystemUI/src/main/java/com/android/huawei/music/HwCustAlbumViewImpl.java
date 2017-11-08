package com.android.huawei.music;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import com.android.huawei.coverscreen.CoverResourceUtils;
import com.android.keyguard.R$dimen;

public class HwCustAlbumViewImpl extends HwCustAlbumView {
    private static final String TAG = "HwCustAlbumViewImpl";
    private Context context;

    public HwCustAlbumViewImpl(Context context) {
        this.context = context;
    }

    public int getMusicAlbumSize(int albumRadius) {
        try {
            return this.context.getResources().getDimensionPixelSize(CoverResourceUtils.getResIdentifier(this.context, "music_album_size", "dimen", "com.android.systemui", R$dimen.music_album_size)) / 2;
        } catch (NotFoundException ex) {
            Log.w(TAG, ex.toString());
            return albumRadius;
        }
    }

    public int getMusicBackgroundSize(int backgroundRadius) {
        try {
            return this.context.getResources().getDimensionPixelSize(CoverResourceUtils.getResIdentifier(this.context, "music_background_size", "dimen", "com.android.systemui", R$dimen.music_background_size)) / 2;
        } catch (NotFoundException ex) {
            Log.w(TAG, ex.toString());
            return backgroundRadius;
        }
    }
}
