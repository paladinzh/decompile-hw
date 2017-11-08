package com.huawei.gallery.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryInnerStorage;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ImmersionUtils;
import java.io.File;
import java.util.ArrayList;
import tmsdk.common.module.update.UpdateConfig;

public class CreateAlbumDialog {
    private String mBucketPath;
    private final Activity mContext;
    private String mDefaultName;
    private AlertDialog mDialog;
    private final Handler mHandler = new Handler();
    private CallBackListner mListner;
    private boolean mMakeDir = false;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            boolean result = true;
            PhotoShareUtils.hideSoftInput(CreateAlbumDialog.this.mTextView);
            switch (which) {
                case -1:
                    String fileName = CreateAlbumDialog.this.mTextView.getText().toString().trim();
                    if (CreateAlbumDialog.this.checkName(dialog, fileName)) {
                        if (CreateAlbumDialog.this.mMakeDir) {
                            String bucketPath = CreateAlbumDialog.this.makeDir(dialog, fileName);
                            if (bucketPath == null) {
                                result = false;
                            }
                            if (result) {
                                GalleryUtils.makeOutsideFileForNewAlbum(CreateAlbumDialog.this.mContext, bucketPath);
                            }
                            CreateAlbumDialog.this.setResult(result, bucketPath, fileName);
                            break;
                        }
                        CreateAlbumDialog.this.setResult(true, null, fileName);
                        return;
                    }
                    CreateAlbumDialog.this.setResult(false, null, null);
                    return;
                default:
                    GalleryUtils.setDialogDismissable(dialog, true);
                    if (CreateAlbumDialog.this.mDialog != null) {
                        GalleryUtils.dismissDialogSafely(CreateAlbumDialog.this.mDialog, null);
                        CreateAlbumDialog.this.mDialog = null;
                        break;
                    }
                    break;
            }
        }
    };
    OnCheckedChangeListener mRadioBtnChangeLister = new OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            View radioButton = group.findViewById(checkedId);
            if (radioButton != null) {
                Object tag = radioButton.getTag();
                if (tag instanceof Integer) {
                    int checkedStorageLocationId = ((Integer) tag).intValue();
                    if (CreateAlbumDialog.this.mStorageLocationId != checkedStorageLocationId) {
                        CreateAlbumDialog.this.mTextView.setText(GalleryUtils.getDefualtAlbumName(CreateAlbumDialog.this.mContext, CreateAlbumDialog.this.getBaseDirPathByIndex(checkedStorageLocationId)));
                        CreateAlbumDialog.this.mTextView.selectAll();
                        CreateAlbumDialog.this.mStorageLocationId = checkedStorageLocationId;
                    }
                }
            }
        }
    };
    private int mStorageLocationId;
    private EditText mTextView;

    public interface CallBackListner {
        void dialogDismiss();

        void onFinish(boolean z, String str, String str2);
    }

    private void setResult(boolean created, String dir, String fileName) {
        if (this.mListner != null) {
            this.mListner.onFinish(created, dir, fileName);
        }
    }

    private boolean checkName(DialogInterface dialog, String fileName) {
        boolean result = true;
        if (!GalleryUtils.isFileNameValid(this.mContext, fileName)) {
            result = false;
        } else if (isNameExistForRename(fileName)) {
            ContextedUtils.showToastQuickly(this.mContext, (int) R.string.create_album_file_exist_Toast, 0);
            result = false;
        }
        GalleryUtils.setDialogDismissable(dialog, result);
        return result;
    }

    private boolean isNameExistForRename(String fileName) {
        if (this.mMakeDir) {
            return false;
        }
        if (fileName != null && fileName.equalsIgnoreCase(this.mDefaultName)) {
            return true;
        }
        if (this.mBucketPath != null) {
            File newAlbumPath = new File(new File(this.mBucketPath).getParent(), fileName);
            return newAlbumPath.exists() && (GalleryUtils.hasSpecialExtraFile(this.mContext, newAlbumPath.getPath()) || GalleryUtils.isDirContainMultimedia(this.mContext, newAlbumPath.getPath()));
        }
    }

    private String makeDir(DialogInterface dialog, String fileName) {
        File albumFileDir = new File(getBaseDirPathByIndex(this.mStorageLocationId), fileName);
        if (GalleryUtils.hasSpaceForSize(UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST, albumFileDir.toString())) {
            String bucketPath = albumFileDir.getAbsolutePath();
            if (albumFileDir.exists()) {
                if (GalleryUtils.isDirContainMultimedia(this.mContext, albumFileDir.getPath())) {
                    GalleryUtils.setDialogDismissable(dialog, false);
                    ContextedUtils.showToastQuickly(this.mContext, (int) R.string.create_album_file_exist_Toast, 0);
                    return null;
                }
            } else if (!albumFileDir.mkdirs()) {
                GalleryUtils.setDialogDismissable(dialog, false);
                return null;
            }
            GalleryUtils.setDialogDismissable(dialog, true);
            return bucketPath;
        }
        hide();
        ContextedUtils.showToastQuickly(this.mContext, this.mContext.getString(R.string.insufficient_storage_space), 0);
        return null;
    }

    public CreateAlbumDialog(Activity context) {
        this.mContext = context;
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            this.mStorageLocationId = innerGalleryStorage.getRootBucketID();
        }
    }

    public void setListner(CallBackListner Listner) {
        this.mListner = Listner;
    }

    public void showForRename(String defaultName, int titleId, String bucketPath) {
        this.mDefaultName = defaultName;
        this.mBucketPath = bucketPath;
        show(defaultName, titleId, false);
    }

    public void show(String defaultName, int titleId, boolean makeDir) {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            this.mMakeDir = makeDir;
            this.mTextView = new EditText(this.mContext);
            this.mTextView.setSingleLine(true);
            this.mTextView.setContentDescription(this.mContext.getString(R.string.input_frame));
            ColorfulUtils.decorateColorfulForEditText(this.mContext, this.mTextView);
            GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
            if (innerGalleryStorage != null) {
                this.mStorageLocationId = innerGalleryStorage.getRootBucketID();
            }
            this.mDialog = GalleryUtils.createDialog(this.mContext, defaultName, titleId, this.mOnClickListener, this.mListner, this.mTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(CreateAlbumDialog.this.mTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.d("CreateAlbumDialog", "The dialog is showing, do not create any more");
    }

    public void hide() {
        if (this.mDialog != null) {
            GalleryUtils.setDialogDismissable(this.mDialog, true);
            GalleryUtils.dismissDialogSafely(this.mDialog, null);
            this.mDialog = null;
        }
        this.mMakeDir = false;
    }

    public void showWithSpaceInfo() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            this.mMakeDir = true;
            this.mDialog = GalleryUtils.createDialog(this.mContext, GalleryUtils.getDefualtAlbumName(this.mContext, getBaseDirPathByIndex(this.mStorageLocationId)), R.string.new_album, this.mOnClickListener, this.mListner, initialCustomView(this.mContext), this.mTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(CreateAlbumDialog.this.mTextView);
                }
            }, 300);
            return;
        }
        GalleryLog.d("CreateAlbumDialog", "The dialog is showing, do not create any more");
    }

    private View initialCustomView(Context context) {
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.create_new_album_with_space, null);
        RadioGroup locationRadioGp = (RadioGroup) view.findViewById(R.id.location_radiogp);
        int index = 0;
        int internalId = 0;
        ArrayList<GalleryStorage> galleryStorageArrayList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        galleryStorageArrayList.add(0, GalleryStorageManager.getInstance().getInnerGalleryStorage());
        for (GalleryStorage galleryStorage : galleryStorageArrayList) {
            if (galleryStorage != null && galleryStorage.isMounted() && galleryStorage.isMountedOnCurrentUser()) {
                RadioButton spaceRadioButton = new RadioButton(context);
                spaceRadioButton.setButtonDrawable(null);
                int color = ImmersionUtils.getControlColor(context);
                int hwExtDrawableId = ColorfulUtils.getHwExtDrawable(context.getResources(), "btn_radio_colorful");
                int hwExtDefaultDrawableId = context.getResources().getIdentifier("androidhwext:drawable/btn_radio_emui", null, null);
                if (color == 0 || hwExtDrawableId == 0) {
                    spaceRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, hwExtDefaultDrawableId, 0);
                } else {
                    spaceRadioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, hwExtDrawableId, 0);
                }
                index++;
                spaceRadioButton.setId(index);
                spaceRadioButton.setTextSize(13.0f);
                if (galleryStorage instanceof GalleryInnerStorage) {
                    this.mStorageLocationId = galleryStorage.getRootBucketID();
                    internalId = index;
                }
                spaceRadioButton.setTag(Integer.valueOf(galleryStorage.getRootBucketID()));
                spaceRadioButton.setText(String.format(context.getString(R.string.storage_multi_availible_space), new Object[]{galleryStorage.getName(), Formatter.formatFileSize(context, GalleryUtils.getAvailableSpace(galleryStorage.getPath()))}));
                LayoutParams layoutParams = new LayoutParams(-1, context.getResources().getDimensionPixelSize(R.dimen.create_album_radio_button_height));
                layoutParams.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.create_album_radio_button_start_margin));
                layoutParams.setMarginEnd(context.getResources().getDimensionPixelSize(R.dimen.create_album_radio_button_end_margin));
                locationRadioGp.addView(spaceRadioButton, layoutParams);
            }
        }
        this.mTextView = (EditText) view.findViewById(R.id.album_name_content);
        this.mTextView.setContentDescription(context.getString(R.string.input_frame));
        ColorfulUtils.decorateColorfulForEditText(this.mContext, this.mTextView);
        locationRadioGp.setOnCheckedChangeListener(this.mRadioBtnChangeLister);
        if (internalId != 0) {
            locationRadioGp.check(internalId);
        }
        return view;
    }

    private String getBaseDirPathByIndex(int storageLocationId) {
        GalleryStorage galleryStorage = GalleryStorageManager.getInstance().getGalleryStorageByBucketID(storageLocationId);
        if (galleryStorage == null) {
            return "";
        }
        return galleryStorage.getPath() + File.separator + "Pictures";
    }
}
