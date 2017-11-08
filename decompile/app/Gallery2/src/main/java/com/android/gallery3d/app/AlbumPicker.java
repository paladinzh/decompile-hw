package com.android.gallery3d.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.app.ListAlbumPickerFragment;
import com.huawei.gallery.util.BundleUtils;

public class AlbumPicker extends AbstractGalleryActivity {
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
        Bundle data;
        Bundle extras = getIntent().getExtras();
        if (BundleUtils.isValid(extras)) {
            data = new Bundle(extras);
        } else {
            data = new Bundle();
        }
        String albumPath = BundleUtils.getString(extras, "choosed_album_path");
        if (!(albumPath == null || albumPath.length() == 0)) {
            data.putString("choosed_album_path", albumPath);
        }
        int typeBits = (BundleUtils.getBoolean(extras, "get-album-include-virtual", false) || BundleUtils.getBoolean(extras, "get-album-multiple", false)) ? 4194304 : 524292;
        data.putBoolean("get-album", true);
        data.putInt("get-title", R.string.select_album);
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
