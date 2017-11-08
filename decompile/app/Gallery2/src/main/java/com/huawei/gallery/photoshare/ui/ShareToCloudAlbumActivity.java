package com.huawei.gallery.photoshare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.BundleUtils;

public class ShareToCloudAlbumActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PhotoShareUtils.isSupportPhotoShare() && getIntent() != null && BundleUtils.isValid(getIntent().getExtras())) {
            if (PhotoShareUtils.getServer() != null) {
                createFragment();
            } else {
                PhotoShareUtils.setRunnable(new Runnable() {
                    public void run() {
                        ShareToCloudAlbumActivity.this.createFragment();
                    }
                });
            }
            return;
        }
        finish();
    }

    private void createFragment() {
        if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isShareSwitchOpen()) {
            setContentView(R.layout.layout_gallery_activity);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new PhotoShareMainFragment();
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString("media-path", "/photoshare/myshare/*".replace("*", bundle.getString("exclude-path", "-1")));
            this.mContent.setArguments(bundle);
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commitAllowingStateLoss();
            return;
        }
        finish();
    }

    protected void onResume() {
        super.onResume();
        if (!PhotoShareUtils.isHiCloudLogin() || !PhotoShareUtils.isShareSwitchOpen()) {
            finish();
        }
    }

    public void onBackPressed() {
        PhotoShareUtils.setRunnable(null);
        super.onBackPressed();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == -1) {
                    finish();
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
