package com.huawei.gallery.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class PasteActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new ListAlbumPasteFragment();
            Bundle data = getIntent().getExtras();
            if (data != null) {
                this.mContent.setArguments(data);
                ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
                ft.commit();
            } else {
                return;
            }
        }
        this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
    }
}
