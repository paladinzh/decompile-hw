package com.android.gallery3d.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class ActionDeleteAndConfirm {
    private boolean mCheckedStatus;
    protected Context mContext;
    protected AlertDialog mDialog;
    protected OnDismissListener mDissimssListener;
    protected boolean mIsHicloudAlbum;
    protected boolean mIsShowing;
    protected boolean mIsSyncedAlbum;
    protected String mMessage;
    protected int mNegativeStringID;
    protected OnCancelListener mOnCancelListener;
    protected OnClickListener mOnClickListener;
    protected int mPositiveStringID;
    protected String mTitle;
    protected View mView;

    public static class ActionDelegate {
        private String mConfirmMsg;
        private ActionDeleteAndConfirm mDialog;
        private OnDismissListener mDismissListener;
        private int mFlag;
        private OnClickListener mListener;
        private String mTitle;

        public void setParams(Activity activity, String confirmMsg, String title, OnClickListener listener, int flag) {
            this.mConfirmMsg = confirmMsg;
            this.mListener = listener;
            this.mTitle = title;
            this.mFlag = flag;
            execute(activity);
        }

        public void setDismissListener(OnDismissListener dismissListener) {
            this.mDismissListener = dismissListener;
        }

        private void execute(Activity activity) {
            if (this.mDialog == null) {
                int positiveId;
                switch (this.mFlag) {
                    case 1:
                        positiveId = R.string.delete;
                        break;
                    case 2:
                        positiveId = R.string.button_recentlydeletedclearall;
                        break;
                    default:
                        positiveId = R.string.ok;
                        break;
                }
                this.mDialog = new ActionDeleteAndConfirm(activity, this.mConfirmMsg, this.mTitle, positiveId, R.string.cancel);
                this.mDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        ActionDelegate.this.mDialog = null;
                        if (ActionDelegate.this.mDismissListener != null) {
                            ActionDelegate.this.mDismissListener.onDismiss(dialog);
                            ActionDelegate.this.mDismissListener = null;
                        }
                    }
                });
                this.mDialog.setOnClickListener(this.mListener);
                this.mDialog.updateStatus(false, false);
                this.mDialog.show();
            } else if (this.mDialog.isShowing()) {
                this.mDialog.updateMessage(null, this.mConfirmMsg);
            } else {
                this.mDialog.updateStatus(false, false);
                this.mDialog.show(null, this.mConfirmMsg);
            }
        }
    }

    public ActionDeleteAndConfirm(Context context, String message) {
        this(context, message, null);
    }

    public ActionDeleteAndConfirm(Context context, String message, String title) {
        this(context, message, title, R.string.delete, R.string.cancel);
    }

    public ActionDeleteAndConfirm(Context context, String message, String title, int positiveStringID, int negativeStringID) {
        this.mContext = context;
        this.mMessage = message;
        this.mTitle = title;
        this.mPositiveStringID = positiveStringID;
        this.mNegativeStringID = negativeStringID;
    }

    public void updateStatus(boolean isSyncedAlbum, boolean isHicloudAlbum) {
        this.mIsSyncedAlbum = isSyncedAlbum;
        this.mIsHicloudAlbum = isHicloudAlbum;
    }

    public void show() {
        if (this.mDialog == null) {
            this.mDialog = new Builder(this.mContext).setOnCancelListener(this.mOnCancelListener).setPositiveButton(this.mPositiveStringID, this.mOnClickListener).setNegativeButton(this.mNegativeStringID, this.mOnClickListener).create();
            this.mDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    ActionDeleteAndConfirm.this.mIsShowing = false;
                    if (ActionDeleteAndConfirm.this.mDissimssListener != null) {
                        ActionDeleteAndConfirm.this.mDissimssListener.onDismiss(dialog);
                    }
                }
            });
            this.mView = LayoutInflater.from(this.mContext).inflate(getDialogViewLayout(), null);
        }
        initView();
        this.mIsShowing = true;
        this.mDialog.show();
        GalleryUtils.setTextColor(this.mDialog.getButton(-1), this.mContext.getResources());
    }

    protected int getDialogViewLayout() {
        return R.layout.delete_hicloud_tips;
    }

    protected void initView() {
        boolean z = false;
        if (this.mView == null || !shouldUseNewStyle()) {
            this.mDialog.setView(null);
            GalleryUtils.setTitleAndMessage(this.mDialog, this.mMessage, this.mTitle);
            return;
        }
        boolean z2;
        AlertDialog alertDialog = this.mDialog;
        String str = this.mTitle;
        if (shouldUseNewStyle()) {
            z2 = false;
        } else {
            z2 = true;
        }
        GalleryUtils.setTitleAndMessage(alertDialog, null, str, z2);
        final View onlyDeleteLocalView = this.mView.findViewById(R.id.delete_only_phone_view);
        final View deleteAllView = this.mView.findViewById(R.id.delete_all_view);
        TextView textView = (TextView) this.mView.findViewById(R.id.delete_all_text);
        CheckBox checkBox = (CheckBox) this.mView.findViewById(R.id.check_delete_all);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ActionDeleteAndConfirm.this.mCheckedStatus = isChecked;
                ActionDeleteAndConfirm.this.updateDeleteViewsShow(isChecked, deleteAllView, onlyDeleteLocalView);
            }
        });
        updateDeleteViewsShow(checkBox.isChecked(), deleteAllView, onlyDeleteLocalView);
        if (this.mIsHicloudAlbum) {
            AlertDialog alertDialog2 = this.mDialog;
            String str2 = this.mTitle;
            if (!shouldUseNewStyle()) {
                z = true;
            }
            GalleryUtils.setTitleAndMessage(alertDialog2, str2, null, z);
            updateDeleteViewsShow(true, deleteAllView, onlyDeleteLocalView);
            checkBox.setVisibility(8);
            textView.setVisibility(8);
        } else if (this.mIsSyncedAlbum) {
            updateDeleteViewsShow(false, deleteAllView, onlyDeleteLocalView);
            checkBox.setVisibility(0);
            textView.setVisibility(8);
        }
        this.mDialog.setView(this.mView);
    }

    protected boolean shouldUseNewStyle() {
        if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen()) {
            return !this.mIsHicloudAlbum ? this.mIsSyncedAlbum : true;
        } else {
            return false;
        }
    }

    protected void updateDeleteViewsShow(boolean isChecked, View deleteAllView, View onlyDeleteLocalView) {
        if (isChecked) {
            deleteAllView.setVisibility(0);
            onlyDeleteLocalView.setVisibility(8);
            return;
        }
        deleteAllView.setVisibility(8);
        onlyDeleteLocalView.setVisibility(0);
    }

    public boolean getCheckBoxStatus() {
        return this.mCheckedStatus;
    }

    public void show(String message) {
        this.mMessage = message;
        this.mTitle = null;
        show();
    }

    public void show(String message, String title) {
        this.mMessage = message;
        this.mTitle = title;
        show();
    }

    public void updateMessage(String message, String title) {
        boolean z = false;
        if (this.mDialog != null && this.mIsShowing) {
            this.mMessage = message;
            this.mTitle = title;
            AlertDialog alertDialog;
            String str;
            if (this.mIsHicloudAlbum) {
                alertDialog = this.mDialog;
                str = this.mTitle;
                if (!shouldUseNewStyle()) {
                    z = true;
                }
                GalleryUtils.setTitleAndMessage(alertDialog, str, null, z);
            } else {
                alertDialog = this.mDialog;
                str = this.mTitle;
                if (!shouldUseNewStyle()) {
                    z = true;
                }
                GalleryUtils.setTitleAndMessage(alertDialog, null, str, z);
            }
        }
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public void dismiss() {
        if (this.mDialog != null && this.mIsShowing) {
            GalleryUtils.dismissDialogSafely(this.mDialog, null);
            this.mDialog.setView(null);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDissimssListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mOnCancelListener = listener;
    }
}
