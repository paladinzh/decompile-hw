package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;

public class EmptyAlbumImage extends ActionImage {
    public EmptyAlbumImage(Path path, GalleryApp application) {
        super(path, application, R.drawable.btn_check_on_disable_emui_black);
    }

    public int getSupportedOperations() {
        return super.getSupportedOperations() | 16384;
    }
}
