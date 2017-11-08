package com.huawei.gallery.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.photoshare.ui.PhotoShareLoginActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareTimeBucketActivity extends AbstractGalleryActivity {
    private Bundle mData = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setContentView(R.layout.layout_photoshare_activity);
            this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
        } else if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
        } else {
            Bundle data = getIntent().getExtras();
            if ("com.huawei.android.intent.action.GET_FAMILY_PHOTOS".equals(getIntent().getAction())) {
                String familyID = data.getString("familyID");
                String userID = data.getString("userID");
                String familyName = data.getString("groupName");
                data.clear();
                data.putString("media-path", "/photoshare/all/share/preview/*".replace("*", familyID));
                data.putString("user-id", userID);
                data.putString("groupName", familyName);
                data.putBoolean("only-local-camera-video-album", false);
                PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                ReportToBigData.report(88);
                if (!(PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isShareSwitchOpen())) {
                    Intent intent = new Intent(this, PhotoShareLoginActivity.class);
                    intent.putExtra("needPhotoshareOpen", true);
                    startActivityForResult(intent, 2001);
                    this.mData = data;
                    return;
                }
            }
            goToTimeBuckActivity(data);
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }

    private void goToTimeBuckActivity(Bundle data) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        setContentView(R.layout.layout_photoshare_activity);
        this.mContent = new PhotoShareTimeBucketHost();
        this.mContent.setArguments(data);
        ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
        ft.commitAllowingStateLoss();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2001:
                if (resultCode != -1 || this.mData == null) {
                    finish();
                    return;
                } else {
                    goToTimeBuckActivity(this.mData);
                    return;
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                return;
        }
    }
}
