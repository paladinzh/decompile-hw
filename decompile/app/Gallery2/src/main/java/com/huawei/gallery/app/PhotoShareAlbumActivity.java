package com.huawei.gallery.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareAlbumActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PhotoShareUtils.isSupportPhotoShare() || getIntent().getBooleanExtra("local-only", false)) {
            setContentView(R.layout.layout_photoshare_activity);
            if (savedInstanceState == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                this.mContent = new PhotoShareAlbumHost();
                this.mContent.setArguments(getIntent().getExtras());
                ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
                ft.commit();
            } else {
                this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
            }
            return;
        }
        finish();
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
