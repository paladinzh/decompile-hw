package com.android.gallery3d.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.app.ListAlbumPickerFragment;

public class DialogPicker extends AbstractGalleryActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            initializeByIntent();
        } else {
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        }
    }

    private void initializeByIntent() {
        int typeBits = GalleryUtils.determineTypeBits(this, getIntent());
        Bundle extras = getIntent().getExtras();
        Bundle data = extras == null ? new Bundle() : new Bundle(extras);
        data.putBoolean("get-content", true);
        data.putInt("get-title", GalleryUtils.getSelectionModePrompt(typeBits));
        data.putString("media-path", getDataManager().getTopSetPath(typeBits));
        startFragment(data);
    }

    protected void startFragment(Bundle data) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        this.mContent = new ListAlbumPickerFragment();
        this.mContent.setArguments(data);
        ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
        ft.commit();
    }
}
