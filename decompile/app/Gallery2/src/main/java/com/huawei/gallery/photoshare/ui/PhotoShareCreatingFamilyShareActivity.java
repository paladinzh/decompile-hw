package com.huawei.gallery.photoshare.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.app.PhotoShareTimeBucketActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.FamilyShareCreateListener;
import java.util.List;

public class PhotoShareCreatingFamilyShareActivity extends Activity implements FamilyShareCreateListener {
    private String mFamilyID = null;
    private String mFamilyName = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getIntent().getExtras();
        if (data != null) {
            this.mFamilyID = data.getString("familyID");
            this.mFamilyName = data.getString("groupName");
        }
        if (!goToFamilyShare(getFamilyShare())) {
            setContentView(R.layout.photoshare_creating_familyshare);
            initActionBar();
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (TextUtils.isEmpty(this.mFamilyName)) {
            actionBar.setTitle(R.string.create_family_share_tips);
        } else {
            actionBar.setTitle(this.mFamilyName);
        }
    }

    private String getFamilyShare() {
        if (PhotoShareUtils.getServer() == null) {
            return null;
        }
        List shareList = null;
        try {
            shareList = PhotoShareUtils.getServer().getShareGroupList();
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (shareList != null && shareList.size() > 0) {
            if (TextUtils.isEmpty(this.mFamilyID)) {
                return ((ShareInfo) shareList.get(0)).getShareId();
            }
            for (int i = 0; i < shareList.size(); i++) {
                if (this.mFamilyID.equals(((ShareInfo) shareList.get(i)).getShareId())) {
                    return ((ShareInfo) shareList.get(i)).getShareId();
                }
            }
        }
        return null;
    }

    protected void onResume() {
        super.onResume();
        if (!goToFamilyShare(getFamilyShare())) {
            PhotoShareUtils.setFamilyShareCreateListener(this);
        }
    }

    protected void onPause() {
        super.onPause();
        PhotoShareUtils.setFamilyShareCreateListener(null);
    }

    private boolean goToFamilyShare(String familyID) {
        if (familyID == null) {
            return false;
        }
        PhotoShareUtils.setFamilyShareCreateListener(null);
        Bundle data = new Bundle();
        Intent intent = new Intent();
        data.putString("media-path", "/photoshare/all/share/preview/*".replace("*", familyID));
        data.putBoolean("only-local-camera-video-album", false);
        intent.setClass(this, PhotoShareTimeBucketActivity.class);
        intent.putExtras(data);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            GalleryLog.v("PhotoShareCreatingFamilyShareActivity", "ActivityNotFoundException " + e.toString());
        }
        finish();
        return true;
    }

    public void createFamilyShare(String familyID) {
        if (TextUtils.isEmpty(this.mFamilyID) || this.mFamilyID.equals(familyID)) {
            goToFamilyShare(familyID);
        }
    }
}
