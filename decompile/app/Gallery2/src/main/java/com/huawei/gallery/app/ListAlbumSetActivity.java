package com.huawei.gallery.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class ListAlbumSetActivity extends AbstractGalleryActivity {
    private boolean mIsCloudAlbumList = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new ListAlbumSetFragment();
            Bundle data = getIntent().getExtras();
            if (data != null) {
                this.mIsCloudAlbumList = data.getBoolean("is-cloud", false);
                data.putBoolean("need-lazy-load", false);
                this.mContent.setArguments(data);
                ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
                ft.commit();
            } else {
                return;
            }
        }
        this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        this.mIsCloudAlbumList = savedInstanceState.getBoolean("is-cloud", false);
    }

    protected boolean needToRequestPermissions() {
        return !this.mIsCloudAlbumList;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is-cloud", this.mIsCloudAlbumList);
    }
}
