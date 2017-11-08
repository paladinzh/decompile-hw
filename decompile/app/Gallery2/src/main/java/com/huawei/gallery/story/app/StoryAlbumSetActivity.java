package com.huawei.gallery.story.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class StoryAlbumSetActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new StoryAlbumSetHost();
            Bundle data = getIntent().getExtras();
            if (data == null) {
                finish();
                return;
            }
            this.mContent.setArguments(data);
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commit();
        } else {
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        }
    }
}
