package com.huawei.gallery.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.util.BundleUtils;

public class ListAlbumPickerActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_activity);
        if (savedInstanceState == null) {
            GalleryLog.d("ListAlbumPickerActivity", "savedInstanceState is null");
            initializeByIntent();
            return;
        }
        this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
    }

    private void initializeByIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        boolean getContent = "android.intent.action.GET_CONTENT".equalsIgnoreCase(action);
        boolean getActionPick = "android.intent.action.PICK".equalsIgnoreCase(action);
        if (getContent) {
            startGetContent(intent);
        } else if (getActionPick) {
            GalleryLog.w("ListAlbumPickerActivity", "action PICK is not supported");
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image")) {
                    intent.setType("image/*");
                }
                if (type.endsWith("/video")) {
                    intent.setType("video/*");
                }
            }
            startGetContent(intent);
        } else {
            finish();
        }
    }

    private void startGetContent(Intent intent) {
        Bundle data;
        if (intent.getExtras() != null) {
            data = new Bundle(intent.getExtras());
        } else {
            data = new Bundle();
        }
        if (BundleUtils.isValid(data)) {
            data.putBoolean("get-content", true);
            int typeBits = GalleryUtils.determineTypeBits(this, intent);
            data.putInt("type-bits", typeBits);
            data.putInt("get-title", GalleryUtils.getSelectionModePrompt(typeBits));
            data.putString("media-path", getDataManager().getTopSetPath(typeBits));
            startFragment(data);
            return;
        }
        GalleryLog.d("ListAlbumPickerActivity", "bundle invalid. ");
        finish();
    }

    protected void startFragment(Bundle data) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        this.mContent = new ListAlbumPickerFragment();
        this.mContent.setArguments(data);
        ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
        ft.commit();
    }
}
