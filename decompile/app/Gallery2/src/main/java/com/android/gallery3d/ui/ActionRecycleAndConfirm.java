package com.android.gallery3d.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class ActionRecycleAndConfirm extends ActionDeleteAndConfirm {
    private final int mFlag;

    public ActionRecycleAndConfirm(Context context, String message, String title) {
        super(context, message, title, R.string.delete, R.string.cancel);
        this.mFlag = 2;
    }

    public ActionRecycleAndConfirm(Context context, String message, String title, int positiveStringID, int negativeStringID, int flag) {
        super(context, message, title, positiveStringID, negativeStringID);
        this.mFlag = flag;
    }

    protected int getDialogViewLayout() {
        return R.layout.recycle_hicloud_tips;
    }

    protected boolean shouldUseNewStyle() {
        if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen()) {
            return !this.mIsHicloudAlbum ? this.mIsSyncedAlbum : true;
        } else {
            return false;
        }
    }

    protected void initView() {
        boolean z = false;
        if (this.mView == null || this.mFlag != 2) {
            this.mDialog.setView(null);
            GalleryUtils.setTitleAndMessage(this.mDialog, this.mMessage, this.mTitle);
            return;
        }
        String dialogMsg;
        View onlyDeleteLocalView = this.mView.findViewById(R.id.delete_only_phone_view);
        View deleteAllView = this.mView.findViewById(R.id.delete_all_view);
        if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen()) {
            deleteAllView.setVisibility(0);
            onlyDeleteLocalView.setVisibility(8);
            dialogMsg = String.format(this.mTitle, new Object[]{Integer.valueOf(30)});
        } else {
            deleteAllView.setVisibility(8);
            onlyDeleteLocalView.setVisibility(0);
            dialogMsg = String.format(this.mTitle, new Object[]{Integer.valueOf(30)});
        }
        AlertDialog alertDialog = this.mDialog;
        if (!shouldUseNewStyle()) {
            z = true;
        }
        GalleryUtils.setTitleAndMessage(alertDialog, dialogMsg, null, z);
        this.mDialog.setView(this.mView);
    }

    public void updateMessage(String message, String title) {
    }

    public void show(String message) {
        this.mMessage = null;
        this.mTitle = message;
        show();
    }
}
