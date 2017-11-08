package com.huawei.gallery.photoshare.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class PhotoShareInviteFriendsActivity extends AbstractGalleryActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_photoshareinvitefriends_activity);
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mContent = new PhotoShareInviteFriendsFragment();
            this.mContent.setArguments(getIntent().getExtras());
            ft.add(R.id.fragment_container, this.mContent, getClass().getSimpleName());
            ft.commit();
            return;
        }
        this.mContent = GalleryUtils.getContentFragment(getSupportFragmentManager(), getClass().getSimpleName());
    }

    protected void requestPermissionSuccess(int requestCode) {
        if (requestCode == 1002) {
            PhotoShareInviteFriendsFragment photoShareInviteFriendsFragment = (PhotoShareInviteFriendsFragment) this.mContent;
            PhotoShareInviteFriendsFragment.startChooseContactsActivity(this);
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
