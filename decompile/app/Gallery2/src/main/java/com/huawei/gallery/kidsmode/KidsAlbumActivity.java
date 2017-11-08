package com.huawei.gallery.kidsmode;

import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.GLActivity;

public class KidsAlbumActivity extends GLActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gl_activity);
        if (savedInstanceState != null) {
            getDataManager().notifyChange(KidsMode.MEDIA_URI);
            getStateManager().restoreFromState(savedInstanceState);
            return;
        }
        initializeByIntent();
    }

    protected void initializeByIntent() {
        Bundle data = getIntent().getExtras() != null ? new Bundle(getIntent().getExtras()) : new Bundle();
        String action = getIntent().getAction();
        String mediaPath = null;
        if ("com.huawei.gallery.action.VIEW_KIDS_ALBUM".equalsIgnoreCase(action)) {
            switch (data.getInt("kids-album-type", 0)) {
                case 0:
                    mediaPath = "/local/kids/camera";
                    break;
                case 1:
                    mediaPath = "/local/kids/paint";
                    break;
                case 2:
                    mediaPath = "/local/kids/media";
                    break;
            }
            data.putString("media-path", mediaPath);
            data.putBoolean("key-is-album-kids", true);
            getStateManager().startState(KidsAlbumPage.class, data);
        } else if ("com.huawei.gallery.action.VIEW_PARENT_ADDED_ALBUM".equalsIgnoreCase(action)) {
            String albumPath = data.getString("parent-added-album-path");
            if (albumPath == null) {
                finish();
                return;
            }
            data.putString("media-path", "/local/kids/parent/" + GalleryUtils.getBucketId(albumPath));
            data.putInt("kids-album-type", 3);
            getStateManager().startState(ParentAddedAlbumPage.class, data);
        }
    }
}
