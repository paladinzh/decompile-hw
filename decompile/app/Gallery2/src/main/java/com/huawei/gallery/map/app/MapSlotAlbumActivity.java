package com.huawei.gallery.map.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class MapSlotAlbumActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new MapSlotAlbumHost();
            this.mContent.setArguments(getIntent().getExtras());
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commit();
            return;
        }
        this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
    }
}
