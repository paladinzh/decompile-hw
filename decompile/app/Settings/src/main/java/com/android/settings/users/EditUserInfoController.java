package com.android.settings.users;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settingslib.R$dimen;
import huawei.android.widget.ErrorTipTextLayout;
import java.io.File;
import java.util.Random;

public class EditUserInfoController {
    private String mDuplicationTip;
    private Dialog mEditUserInfoDialog;
    private EditUserPhotoController mEditUserPhotoController;
    private ErrorTipTextLayout mMasterNameEditLayout;
    private EditText mMasterNameEditTextView;
    private Bitmap mSavedPhoto;
    private TextWatcher mTextWatcher = null;
    private UserHandle mUser;
    private UserManager mUserManager;
    private TextView mUserMessage;
    private ErrorTipTextLayout mUserNameEditLayout;
    private EditText mUserNameEditTextView;
    private ImageView mUserPhotoView;
    private ImageView mViewAvatarView;
    private boolean mWaitingForActivityResult = false;

    public interface OnContentChangedCallback {
        void onLabelChanged(CharSequence charSequence);

        void onPhotoChanged(Drawable drawable);
    }

    public interface onPositiveButtonClickListener {
        void onclick(int i, String str, Bitmap bitmap);
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        this.mTextWatcher = textWatcher;
    }

    public void setNameDuplicationTip(String duplicationTip) {
        this.mDuplicationTip = duplicationTip;
    }

    public void clear() {
        this.mEditUserPhotoController.removeNewUserPhotoBitmapFile();
        this.mEditUserInfoDialog = null;
        this.mSavedPhoto = null;
    }

    public Dialog getDialog() {
        return this.mEditUserInfoDialog;
    }

    public void onRestoreInstanceState(Bundle icicle) {
        String pendingPhoto = icicle.getString("pending_photo");
        if (pendingPhoto != null) {
            this.mSavedPhoto = EditUserPhotoController.loadNewUserPhotoBitmap(new File(pendingPhoto));
        }
        this.mWaitingForActivityResult = icicle.getBoolean("awaiting_result", false);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!(this.mEditUserInfoDialog == null || !this.mEditUserInfoDialog.isShowing() || this.mEditUserPhotoController == null)) {
            File file = this.mEditUserPhotoController.saveNewUserPhotoBitmap();
            if (file != null) {
                outState.putString("pending_photo", file.getPath());
            }
        }
        if (this.mWaitingForActivityResult) {
            outState.putBoolean("awaiting_result", this.mWaitingForActivityResult);
        }
    }

    public void startingActivityForResult() {
        this.mWaitingForActivityResult = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mWaitingForActivityResult = false;
        if (this.mEditUserInfoDialog != null && this.mEditUserInfoDialog.isShowing() && !this.mEditUserPhotoController.onActivityResult(requestCode, resultCode, data)) {
        }
    }

    public Dialog createAddUserDialog(Fragment fragment, int userType, onPositiveButtonClickListener listener) {
        Activity activity = fragment.getActivity();
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(activity);
        }
        View content = activity.getLayoutInflater().inflate(2130968612, null);
        this.mUserNameEditLayout = (ErrorTipTextLayout) content.findViewById(2131886206);
        this.mUserNameEditTextView = this.mUserNameEditLayout.getEditText();
        this.mUserPhotoView = (ImageView) content.findViewById(2131886203);
        this.mViewAvatarView = (ImageView) content.findViewById(2131886204);
        this.mUserMessage = (TextView) content.findViewById(2131886208);
        final SharedPreferences preferences = activity.getPreferences(0);
        final boolean longMessageDisplayed = preferences.getBoolean("key_add_user_long_message_displayed", false);
        if (longMessageDisplayed) {
            this.mUserMessage.setText(2131628128);
        } else {
            this.mUserMessage.setText(2131628394);
        }
        if (this.mSavedPhoto != null) {
            this.mSavedPhoto = Utils.createCroppedImage(this.mSavedPhoto, (int) fragment.getResources().getDimension(R$dimen.circle_avatar_size));
        } else {
            this.mSavedPhoto = Utils.getDefaultUserIconAsBitmap(new Random().nextInt(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID));
        }
        Drawable drawable = Utils.createRoundPhotoDrawable(fragment.getResources(), this.mSavedPhoto);
        this.mUserPhotoView.setImageDrawable(drawable);
        this.mEditUserPhotoController = new EditUserPhotoController(fragment, this.mUserPhotoView, this.mViewAvatarView, this.mSavedPhoto, drawable, this.mWaitingForActivityResult);
        final onPositiveButtonClickListener onpositivebuttonclicklistener = listener;
        final int i = userType;
        this.mEditUserInfoDialog = new Builder(activity).setTitle(2131626452).setView(content).setCancelable(true).setPositiveButton(2131628702, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!longMessageDisplayed) {
                    preferences.edit().putBoolean("key_add_user_long_message_displayed", true).apply();
                }
                dialog.dismiss();
                EditUserInfoController.this.hideSoftInputFromWindow();
                onpositivebuttonclicklistener.onclick(i, EditUserInfoController.this.mUserNameEditTextView.getText().toString().trim(), EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap());
                EditUserInfoController.this.clear();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EditUserInfoController.this.clear();
            }
        }).create();
        changeDialogForPrivacy(userType);
        this.mUserNameEditTextView.setSelectAllOnFocus(true);
        this.mUserNameEditTextView.requestFocus();
        this.mUserNameEditTextView.addTextChangedListener(this.mTextWatcher);
        this.mEditUserInfoDialog.getWindow().setSoftInputMode(4);
        return this.mEditUserInfoDialog;
    }

    private void hideSoftInputFromWindow() {
        if (this.mEditUserInfoDialog != null && this.mEditUserInfoDialog.getWindow() != null) {
            this.mEditUserInfoDialog.getWindow().setSoftInputMode(2);
        }
    }

    public Dialog createDialog(Fragment fragment, Drawable currentUserIcon, CharSequence currentUserName, int titleResId, OnContentChangedCallback callback, UserHandle user) {
        Drawable drawable;
        Activity activity = fragment.getActivity();
        this.mUser = user;
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(activity);
        }
        View content = activity.getLayoutInflater().inflate(2130968766, null);
        UserInfo info = this.mUserManager.getUserInfo(this.mUser.getIdentifier());
        this.mMasterNameEditLayout = (ErrorTipTextLayout) content.findViewById(2131886552);
        this.mMasterNameEditTextView = this.mMasterNameEditLayout.getEditText();
        this.mMasterNameEditTextView.setText(info.name);
        if (info.name != null) {
            this.mMasterNameEditTextView.setSelection(info.name.length());
        }
        ImageView mUserPhotoView = (ImageView) content.findViewById(2131886203);
        ImageView mViewAvatarView = (ImageView) content.findViewById(2131886204);
        if (this.mSavedPhoto != null) {
            this.mSavedPhoto = Utils.createCroppedImage(this.mSavedPhoto, (int) fragment.getResources().getDimension(R$dimen.circle_avatar_size));
            drawable = Utils.createRoundPhotoDrawable(fragment.getResources(), this.mSavedPhoto);
        } else {
            drawable = currentUserIcon;
            if (currentUserIcon == null) {
                drawable = com.android.settingslib.Utils.getUserIcon(activity, this.mUserManager, info);
            }
        }
        mUserPhotoView.setImageDrawable(drawable);
        this.mEditUserPhotoController = new EditUserPhotoController(fragment, mUserPhotoView, mViewAvatarView, this.mSavedPhoto, drawable, this.mWaitingForActivityResult);
        final Fragment fragment2 = fragment;
        final CharSequence charSequence = currentUserName;
        final OnContentChangedCallback onContentChangedCallback = callback;
        final Drawable drawable2 = currentUserIcon;
        this.mEditUserInfoDialog = new Builder(activity).setView(content).setCancelable(true).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemUseStat.getInstance().handleClick(fragment2.getActivity(), 2, "user_edit_info");
                CharSequence userName = EditUserInfoController.this.mMasterNameEditTextView.getText().toString().trim();
                if (!TextUtils.isEmpty(userName) && (charSequence == null || !userName.toString().equals(charSequence.toString()))) {
                    if (onContentChangedCallback != null) {
                        onContentChangedCallback.onLabelChanged(userName.toString());
                    }
                    EditUserInfoController.this.mUserManager.setUserName(EditUserInfoController.this.mUser.getIdentifier(), userName.toString());
                }
                Drawable drawable = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoDrawable();
                Bitmap bitmap = EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap();
                if (!(drawable == null || bitmap == null || drawable.equals(drawable2))) {
                    if (onContentChangedCallback != null) {
                        onContentChangedCallback.onPhotoChanged(drawable);
                    }
                    new AsyncTask<Void, Void, Void>() {
                        protected Void doInBackground(Void... params) {
                            EditUserInfoController.this.mUserManager.setUserIcon(EditUserInfoController.this.mUser.getIdentifier(), EditUserInfoController.this.mEditUserPhotoController.getNewUserPhotoBitmap());
                            return null;
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                }
                fragment2.getActivity().removeDialog(1);
                dialog.dismiss();
                EditUserInfoController.this.clear();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EditUserInfoController.this.clear();
            }
        }).create();
        this.mMasterNameEditTextView.setSelectAllOnFocus(true);
        this.mMasterNameEditTextView.requestFocus();
        this.mMasterNameEditTextView.addTextChangedListener(this.mTextWatcher);
        this.mEditUserInfoDialog.getWindow().setSoftInputMode(4);
        return this.mEditUserInfoDialog;
    }

    public ProgressDialog createWatingDialog(String tips, Context context) {
        if (TextUtils.isEmpty(tips) || context == null) {
            return null;
        }
        ProgressDialog mWaitingDialog = new ProgressDialog(context);
        mWaitingDialog.setCancelable(false);
        mWaitingDialog.setIndeterminate(true);
        mWaitingDialog.setMessage(tips);
        return mWaitingDialog;
    }

    public void hideWatingDialog(Dialog watingdialog) {
        if (watingdialog != null && watingdialog.isShowing()) {
            try {
                watingdialog.dismiss();
            } catch (IllegalArgumentException exp) {
                exp.printStackTrace();
            }
        }
    }

    public void hintDuplicatedMastername(boolean isDuplicated) {
        if (isDuplicated) {
            this.mMasterNameEditLayout.setError(this.mDuplicationTip);
        } else {
            this.mMasterNameEditLayout.setError(null);
        }
    }

    public void hintDuplicatedUsername(boolean isDuplicated) {
        if (isDuplicated) {
            this.mUserNameEditLayout.setError(this.mDuplicationTip);
            this.mUserMessage.setVisibility(8);
            return;
        }
        this.mUserNameEditLayout.setError(null);
        this.mUserMessage.setVisibility(0);
    }

    public void hintDuplicatedNickname(boolean isDuplicated, int dialogId) {
        if (2 == dialogId) {
            hintDuplicatedUsername(isDuplicated);
        } else if (9 == dialogId) {
            hintDuplicatedMastername(isDuplicated);
        }
    }

    private void changeDialogForPrivacy(int userType) {
        if (userType == 4) {
            this.mUserMessage.setText(2131628776);
            this.mEditUserInfoDialog.setTitle(2131628717);
        }
    }
}
