package com.huawei.gallery.photoshare.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.app.PhotoShareTagAlbumSetHost;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareTagAlbumSetActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean hasSavedInstanceState = savedInstanceState != null;
        if (PhotoShareUtils.isSupportPhotoShare()) {
            if (PhotoShareUtils.getServer() != null) {
                setContext(hasSavedInstanceState);
            } else {
                PhotoShareUtils.setRunnable(new Runnable() {
                    public void run() {
                        PhotoShareTagAlbumSetActivity.this.setContext(hasSavedInstanceState);
                    }
                });
            }
        } else if (getIntent().getBooleanExtra("local-only", false)) {
            setContext(hasSavedInstanceState);
        } else {
            finish();
        }
    }

    private void setContext(boolean hasSavedInstanceState) {
        setContentView(R.layout.layout_gallery_activity);
        if (hasSavedInstanceState) {
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new PhotoShareTagAlbumSetHost();
            Bundle data = getIntent().getExtras();
            if (data == null) {
                finish();
                return;
            }
            this.mContent.setArguments(data);
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commit();
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
