package com.huawei.gallery.map.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class MapAlbumActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MapFragmentFactory.isMapReady()) {
            setContentView(R.layout.layout_gallery_activity);
            if (savedInstanceState == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (GallerySettings.getBoolean(this, GallerySettings.KEY_USE_NETWORK, false) || !MapFragmentFactory.shouldUseGaoDeMapFragment(this)) {
                    this.mContent = MapFragmentFactory.create(this);
                } else {
                    this.mContent = new EmptyMapFragment();
                }
                this.mContent.setArguments(getIntent().getExtras());
                ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
                ft.commit();
            } else {
                this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
            }
            return;
        }
        GalleryLog.d("MapAlbumActivity", "Map is not ready");
        finish();
    }
}
