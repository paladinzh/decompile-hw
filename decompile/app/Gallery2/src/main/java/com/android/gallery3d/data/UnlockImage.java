package com.android.gallery3d.data;

import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;

public class UnlockImage extends ActionImage {
    public UnlockImage(Path path, GalleryApp application) {
        super(path, application, R.drawable.btn_check_on_emui);
    }

    public int getSupportedOperations() {
        return super.getSupportedOperations() | FragmentTransaction.TRANSIT_EXIT_MASK;
    }
}
