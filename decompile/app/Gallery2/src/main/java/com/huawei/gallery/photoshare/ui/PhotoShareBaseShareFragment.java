package com.huawei.gallery.photoshare.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.app.AbstractGalleryFragment;

public class PhotoShareBaseShareFragment extends AbstractGalleryFragment {
    protected ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.show();
    }

    protected void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mProgressDialog != null) {
            GalleryUtils.setDialogDismissable(this.mProgressDialog, true);
            GalleryUtils.dismissDialogSafely(this.mProgressDialog, null);
            this.mProgressDialog = null;
        }
    }
}
