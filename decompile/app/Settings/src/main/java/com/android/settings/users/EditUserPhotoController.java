package com.android.settings.users;

import android.app.Fragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.os.UserHandle;
import android.provider.ContactsContract.DisplayPhoto;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$id;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditUserPhotoController {
    private final Context mContext;
    private final Uri mCropPictureUri;
    private final Fragment mFragment;
    private final ImageView mImageView;
    private Bitmap mNewUserPhotoBitmap;
    private Drawable mNewUserPhotoDrawable;
    private final int mPhotoSize;
    private final Uri mTakePictureUri;
    private final ImageView mViewAvatarView;

    private static final class RestrictedMenuItem {
        private final Runnable mAction;
        private final EnforcedAdmin mAdmin;
        private final Context mContext;
        private final boolean mIsRestrictedByBase;
        private final String mTitle;

        public RestrictedMenuItem(Context context, String title, String restriction, Runnable action) {
            this.mContext = context;
            this.mTitle = title;
            this.mAction = action;
            int myUserId = UserHandle.myUserId();
            this.mAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, restriction, myUserId);
            this.mIsRestrictedByBase = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, restriction, myUserId);
        }

        public String toString() {
            return this.mTitle;
        }

        final void doAction() {
            if (!isRestrictedByBase()) {
                if (isRestrictedByAdmin()) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mAdmin);
                } else {
                    this.mAction.run();
                }
            }
        }

        final boolean isRestrictedByAdmin() {
            return this.mAdmin != null;
        }

        final boolean isRestrictedByBase() {
            return this.mIsRestrictedByBase;
        }
    }

    private static final class RestrictedPopupMenuAdapter extends ArrayAdapter<RestrictedMenuItem> {
        public RestrictedPopupMenuAdapter(Context context, List<RestrictedMenuItem> items) {
            super(context, 2130969060, 2131886308, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int i = 0;
            View view = super.getView(position, convertView, parent);
            RestrictedMenuItem item = (RestrictedMenuItem) getItem(position);
            TextView text = (TextView) view.findViewById(2131886308);
            ImageView image = (ImageView) view.findViewById(R$id.restricted_icon);
            boolean z = (item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? false : true;
            text.setEnabled(z);
            if (!item.isRestrictedByAdmin() || item.isRestrictedByBase()) {
                i = 8;
            }
            image.setVisibility(i);
            return view;
        }
    }

    public EditUserPhotoController(Fragment fragment, ImageView view, ImageView avatarview, Bitmap bitmap, Drawable drawable, boolean waiting) {
        boolean z;
        boolean z2 = false;
        this.mContext = view.getContext();
        this.mFragment = fragment;
        this.mImageView = view;
        this.mViewAvatarView = avatarview;
        Context context = this.mContext;
        String str = "CropEditUserPhoto.jpg";
        if (waiting) {
            z = false;
        } else {
            z = true;
        }
        this.mCropPictureUri = createTempImageUri(context, str, z);
        Log.d("EditUserPhotoController", "mCropPictureUri in Constructor is:" + this.mCropPictureUri);
        Context context2 = this.mContext;
        String str2 = "TakeEditUserPhoto2.jpg";
        if (!waiting) {
            z2 = true;
        }
        this.mTakePictureUri = createTempImageUri(context2, str2, z2);
        Log.d("EditUserPhotoController", "mTakePictureUri in Constructor is:" + this.mTakePictureUri);
        this.mPhotoSize = getPhotoSize(this.mContext);
        this.mViewAvatarView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ItemUseStat.getInstance().handleClick(EditUserPhotoController.this.mContext, 2, "edit_user_photo");
                EditUserPhotoController.this.showUpdatePhotoPopup();
            }
        });
        this.mNewUserPhotoBitmap = bitmap;
        this.mNewUserPhotoDrawable = drawable;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("EditUserPhotoController", "onActivityResult called, request code: " + requestCode + " result code is: " + resultCode);
        if (resultCode != -1) {
            return false;
        }
        Uri pictureUri = (data == null || data.getData() == null) ? this.mTakePictureUri : data.getData();
        Log.d("EditUserPhotoController", "data is:" + data);
        Log.d("EditUserPhotoController", "pictureUri is:" + pictureUri);
        switch (requestCode) {
            case 1001:
            case 1002:
                cropPhoto(pictureUri);
                Log.d("EditUserPhotoController", "cropPhoto, param is pictureUri");
                return true;
            case 1003:
                onPhotoCropped(this.mCropPictureUri, true);
                Log.d("EditUserPhotoController", "onPhotoCropped, param is:" + pictureUri);
                return true;
            default:
                return false;
        }
    }

    public Bitmap getNewUserPhotoBitmap() {
        return this.mNewUserPhotoBitmap;
    }

    public Drawable getNewUserPhotoDrawable() {
        return this.mNewUserPhotoDrawable;
    }

    private void showUpdatePhotoPopup() {
        boolean canTakePhoto = canTakePhoto();
        boolean canChoosePhoto = canChoosePhoto();
        if (canTakePhoto || canChoosePhoto) {
            int hoffset;
            int voffset;
            Context context = this.mImageView.getContext();
            List<RestrictedMenuItem> items = new ArrayList();
            if (canTakePhoto) {
                items.add(new RestrictedMenuItem(context, context.getString(2131626594), "no_set_user_icon", new Runnable() {
                    public void run() {
                        EditUserPhotoController.this.takePhoto();
                    }
                }));
            }
            if (canChoosePhoto) {
                items.add(new RestrictedMenuItem(context, context.getString(2131626595), "no_set_user_icon", new Runnable() {
                    public void run() {
                        EditUserPhotoController.this.choosePhoto();
                    }
                }));
            }
            final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
            listPopupWindow.setAnchorView(this.mImageView);
            listPopupWindow.setModal(true);
            listPopupWindow.setInputMethodMode(2);
            listPopupWindow.setAdapter(new RestrictedPopupMenuAdapter(context, items));
            listPopupWindow.setWidth(Math.max(this.mImageView.getWidth(), context.getResources().getDimensionPixelSize(2131558619)));
            listPopupWindow.setDropDownGravity(8388611);
            if (context.getResources().getConfiguration().orientation == 1) {
                hoffset = context.getResources().getDimensionPixelOffset(2131558865);
                voffset = context.getResources().getDimensionPixelOffset(2131558866);
                Log.e("EditUserPhotoController", "a:" + hoffset);
            } else {
                hoffset = context.getResources().getDimensionPixelOffset(2131558867);
                voffset = context.getResources().getDimensionPixelOffset(2131558868);
                Log.e("EditUserPhotoController", "a:" + hoffset);
            }
            listPopupWindow.setHorizontalOffset(-hoffset);
            listPopupWindow.setVerticalOffset(-voffset);
            listPopupWindow.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listPopupWindow.dismiss();
                    ((RestrictedMenuItem) parent.getAdapter().getItem(position)).doAction();
                }
            });
            listPopupWindow.show();
        }
    }

    private boolean canTakePhoto() {
        return this.mImageView.getContext().getPackageManager().queryIntentActivities(new Intent("android.media.action.IMAGE_CAPTURE"), 65536).size() > 0;
    }

    private boolean canChoosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        if (this.mImageView.getContext().getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            return true;
        }
        return false;
    }

    private void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1002);
    }

    private void choosePhoto() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mFragment.startActivityForResult(intent, 1001);
    }

    private void cropPhoto(Uri pictureUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(pictureUri, "image/*");
        appendOutputExtra(intent, this.mCropPictureUri);
        appendCropExtras(intent);
        if (intent.resolveActivity(this.mContext.getPackageManager()) != null) {
            Log.d("EditUserPhotoController", "startActivityForResult REQUEST_CODE_CROP_PHOTO");
            try {
                StrictMode.disableDeathOnFileUriExposure();
                this.mFragment.startActivityForResult(intent, 1003);
            } finally {
                StrictMode.enableDeathOnFileUriExposure();
            }
        } else {
            onPhotoCropped(pictureUri, false);
            Log.d("EditUserPhotoController", "onPhotoCropped, param is pictureUri: " + pictureUri);
        }
    }

    private void appendOutputExtra(Intent intent, Uri pictureUri) {
        intent.putExtra("output", pictureUri);
        intent.addFlags(3);
        intent.setClipData(ClipData.newRawUri("output", pictureUri));
    }

    private void appendCropExtras(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", this.mPhotoSize);
        intent.putExtra("outputY", this.mPhotoSize);
    }

    private void onPhotoCropped(final Uri data, final boolean cropped) {
        new AsyncTask<Void, Void, Bitmap>() {
            protected Bitmap doInBackground(Void... params) {
                Log.d("EditUserPhotoController", "onPhotoCropped, cropped: " + cropped);
                if (cropped) {
                    InputStream inputStream = null;
                    try {
                        inputStream = EditUserPhotoController.this.mContext.getContentResolver().openInputStream(data);
                        Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ioe) {
                                Log.w("EditUserPhotoController", "Cannot close image stream", ioe);
                            }
                        }
                        return decodeStream;
                    } catch (FileNotFoundException fe) {
                        Log.w("EditUserPhotoController", "Cannot find image file", fe);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ioe2) {
                                Log.w("EditUserPhotoController", "Cannot close image stream", ioe2);
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ioe22) {
                                Log.w("EditUserPhotoController", "Cannot close image stream", ioe22);
                            }
                        }
                    }
                } else {
                    try {
                        Bitmap fullImage = BitmapFactory.decodeStream(EditUserPhotoController.this.mContext.getContentResolver().openInputStream(data));
                        Log.d("EditUserPhotoController", "onPhotoCropped, fullImage: " + fullImage);
                        if (fullImage == null) {
                            return null;
                        }
                        Log.d("EditUserPhotoController", "createCroppedImage, fullImage: " + fullImage + " mPhotoSize: " + EditUserPhotoController.this.mPhotoSize);
                        return Utils.createCroppedImage(fullImage, EditUserPhotoController.this.mPhotoSize);
                    } catch (Exception fe2) {
                        Log.w("EditUserPhotoController", "Cannot find fullimage file", fe2);
                        return null;
                    }
                }
            }

            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    int mPhotoSize = (int) EditUserPhotoController.this.mContext.getResources().getDimension(R$dimen.circle_avatar_size);
                    Log.d("EditUserPhotoController", "onPostExecute,  mPhotoSize: " + mPhotoSize);
                    EditUserPhotoController.this.mNewUserPhotoBitmap = Utils.createCroppedImage(bitmap, mPhotoSize);
                    EditUserPhotoController.this.mNewUserPhotoDrawable = Utils.createRoundPhotoDrawable(EditUserPhotoController.this.mContext.getResources(), EditUserPhotoController.this.mNewUserPhotoBitmap);
                    Log.d("EditUserPhotoController", "setImageDrawable, mNewUserPhotoDrawable: " + EditUserPhotoController.this.mNewUserPhotoDrawable);
                    EditUserPhotoController.this.mImageView.setImageDrawable(EditUserPhotoController.this.mNewUserPhotoDrawable);
                }
                if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
                    new File(EditUserPhotoController.this.mContext.getCacheDir(), "TakeEditUserPhoto2.jpg").delete();
                    new File(EditUserPhotoController.this.mContext.getCacheDir(), "CropEditUserPhoto.jpg").delete();
                    return;
                }
                new File(EditUserPhotoController.this.mContext.getExternalCacheDir(), "TakeEditUserPhoto2.jpg").delete();
                new File(EditUserPhotoController.this.mContext.getExternalCacheDir(), "CropEditUserPhoto.jpg").delete();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    private static int getPhotoSize(Context context) {
        Cursor cursor = context.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        try {
            cursor.moveToFirst();
            int i = cursor.getInt(0);
            return i;
        } finally {
            cursor.close();
        }
    }

    private Uri createTempImageUri(Context context, String fileName, boolean purge) {
        File file = new File(getRootFilePath(context), fileName);
        Uri uri = Uri.parse("");
        try {
            uri = FileProvider.getUriForFile(context, "com.android.settings.files", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    private File getRootFilePath(Context context) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
            Log.d("EditUserPhotoController", "dir, cache: " + dir);
        } else {
            dir = context.getExternalCacheDir();
            Log.d("EditUserPhotoController", "dir, external cache: " + dir);
        }
        if (!(dir == null || dir.mkdirs())) {
            dir.deleteOnExit();
        }
        return dir;
    }

    File saveNewUserPhotoBitmap() {
        if (this.mNewUserPhotoBitmap == null) {
            return null;
        }
        try {
            File file = new File(this.mContext.getCacheDir(), "NewUserPhoto.png");
            OutputStream os = new FileOutputStream(file);
            this.mNewUserPhotoBitmap.compress(CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            return file;
        } catch (IOException e) {
            Log.e("EditUserPhotoController", "Cannot create temp file", e);
            return null;
        }
    }

    static Bitmap loadNewUserPhotoBitmap(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    void removeNewUserPhotoBitmapFile() {
        new File(this.mContext.getCacheDir(), "NewUserPhoto.png").delete();
    }
}
