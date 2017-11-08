package com.huawei.gallery.photoshare.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class PhotoShareShowMemberActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
            return;
        }
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new PhotoShareShowMemberFragment();
            this.mContent.setArguments(getIntent().getExtras());
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commit();
        } else {
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
