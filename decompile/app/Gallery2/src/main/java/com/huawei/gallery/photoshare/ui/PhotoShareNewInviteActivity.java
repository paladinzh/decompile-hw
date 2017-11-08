package com.huawei.gallery.photoshare.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareNewInviteActivity extends AbstractGalleryActivity {
    private boolean mRecreated;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mRecreated = savedInstanceState != null;
        if (PhotoShareUtils.getServer() != null) {
            createFragment();
        } else {
            PhotoShareUtils.setRunnable(new Runnable() {
                public void run() {
                    PhotoShareNewInviteActivity.this.createFragment();
                }
            });
        }
    }

    private void createFragment() {
        setContentView(R.layout.layout_gallery_activity);
        if (this.mRecreated) {
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        this.mContent = new PhotoShareNewInviteFragment();
        this.mContent.setArguments(getIntent().getExtras());
        ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
        ft.commit();
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
