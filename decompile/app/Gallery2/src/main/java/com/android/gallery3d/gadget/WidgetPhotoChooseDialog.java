package com.android.gallery3d.gadget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.app.SinglePhotoActivity;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.util.UIUtils;
import java.io.File;

public class WidgetPhotoChooseDialog extends Activity implements OnClickListener, OnCancelListener {
    private static final String KEY_MIME_TYPE = "Mimetype";
    private static final String KEY_URI = "Uri";
    private static final int REQUEST_CHOOSE_ALBUM = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;
    private static final String TAG = "WidgetPhotoChooseDialog";
    private static final int[] chooseItems = new int[]{R.string.widget_image_detail, R.string.widget_type_album, R.string.widget_type_photo};
    private AlertDialog chooseDialog = null;
    private String[] mItem;
    private String mMimetype;
    private Uri mUri;
    private int mWidgetId;

    protected void onCreate(Bundle savedInstanceState) {
        boolean init;
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        if (ApiHelper.HAS_VIEW_FLAG_TRANSLUCENT_NAVIGATION) {
            getWindow().addFlags(134217728);
        }
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            UIUtils.setStatusBarColor(getWindow(), 0);
        }
        if (savedInstanceState != null) {
            init = init(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (intent == null || !WidgetPhotoView.ACTION_CHOOSE.equals(intent.getAction())) {
                init = true;
            } else {
                init = init(intent.getExtras());
            }
        }
        if (init) {
            finish();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mUri != null) {
            outState.putParcelable(KEY_URI, this.mUri);
        }
        if (this.mMimetype != null) {
            outState.putString(KEY_MIME_TYPE, this.mMimetype);
        }
        outState.putInt("appWidgetId", this.mWidgetId);
    }

    private boolean init(Bundle data) {
        if (!BundleUtils.isValid(data)) {
            return true;
        }
        this.mUri = (Uri) data.getParcelable(KEY_URI);
        this.mMimetype = data.getString(KEY_MIME_TYPE);
        if (this.mUri == null || this.mMimetype == null) {
            this.mItem = new String[]{getString(chooseItems[1]), getString(chooseItems[2])};
        } else {
            this.mItem = new String[]{getString(chooseItems[0]), getString(chooseItems[1]), getString(chooseItems[2])};
        }
        if (isFinishing()) {
            return false;
        }
        showPhotoChooseDialog();
        this.mWidgetId = data.getInt("appWidgetId", 0);
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int resultWidgetId = this.mWidgetId;
        if (resultCode != -1) {
            setResult(resultCode, new Intent().putExtra("appWidgetId", resultWidgetId));
            finish();
            return;
        }
        String[] items;
        Intent intent;
        if (requestCode == 1) {
            String[] albumsPath = data.getStringArrayExtra("albums-path");
            if (albumsPath != null && albumsPath.length != 0) {
                String albumPath = albumsPath[0];
                if (albumPath != null && albumPath.contains("/local/image/")) {
                    items = albumPath.split("/");
                    WidgetUtils.setSharedPrefer(getApplicationContext(), resultWidgetId, items[items.length - 1]);
                    WidgetUtils.setAlbumPath(getApplicationContext(), resultWidgetId, albumPath);
                    new PhotoAppWidgetProvider().onUpdate(getApplicationContext(), AppWidgetManager.getInstance(this), new int[]{resultWidgetId});
                    intent = new Intent();
                    intent.setData(Uri.parse(albumPath));
                    intent.putExtra("appWidgetId", resultWidgetId);
                    setResult(-1, intent);
                }
                finish();
            }
        } else if (requestCode == 2) {
            Uri choosedImage = data.getData();
            items = choosedImage.toString().split("/");
            String bucketId = getImgBucketId(items[items.length - 1]);
            if (bucketId == null) {
                setResult(0);
                finish();
                return;
            }
            WidgetUtils.setSharedPrefer(getApplicationContext(), resultWidgetId, bucketId);
            WidgetUtils.deleteAlbumPath(getApplicationContext(), resultWidgetId);
            new PhotoAppWidgetProvider().onUpdate(getApplicationContext(), AppWidgetManager.getInstance(this), new int[]{resultWidgetId});
            intent = new Intent();
            intent.setData(choosedImage);
            intent.putExtra("appWidgetId", resultWidgetId);
            setResult(-1, intent);
            finish();
        } else {
            throw new AssertionError("unknown request: " + requestCode);
        }
    }

    private String getImgBucketId(String imageId) {
        String SELECTION = "_id = ? ";
        String str = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(WidgetPhotoView.ROOT_URI, new String[]{"bucket_id", "_display_name"}, "_id = ? ", new String[]{imageId}, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(0) + File.separator + cursor.getString(1);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            GalleryLog.i(TAG, "Catch a RuntimeException in getImgBucketId() method.");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            GalleryLog.i(TAG, "Catch an exception in getImgBucketId() method.");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    private void startChooseAlbum() {
        Intent albumIntent = new Intent("android.intent.action.PICK").setClassName(HicloudAccountManager.PACKAGE_NAME, "com.android.gallery3d.app.AlbumPicker");
        albumIntent.putExtra("get-album-include-virtual", true);
        albumIntent.setFlags(8388608);
        albumIntent.putExtra("choosed_album_path", WidgetUtils.getAlbumPath(getApplicationContext(), this.mWidgetId));
        try {
            startActivityForResult(albumIntent, 1);
        } catch (Exception e) {
            GalleryLog.i(TAG, "Catch an exception in startChooseAlbum() method.");
        }
    }

    private void startChooseSinglePhoto() {
        Intent imgIntent = new Intent("android.intent.action.PICK").setType("vnd.android.cursor.dir/image");
        imgIntent.setPackage(HicloudAccountManager.PACKAGE_NAME);
        imgIntent.setFlags(8388608);
        try {
            startActivityForResult(imgIntent, 2);
        } catch (Exception e) {
            GalleryLog.i(TAG, "Catch an exception in startChooseSinglePhoto() method.");
        }
    }

    private void startViewPhoto(Uri uri, String mimetype) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(uri, mimetype);
            intent.setFlags(8388608);
            String albumBucketId = WidgetUtils.getSharedPrefer(getApplicationContext(), this.mWidgetId);
            if (WidgetUtils.isCameraAlbum(albumBucketId)) {
                intent.putExtra("media-set-path", "/local/camera");
            } else if (isScreenShotsAlbum(albumBucketId)) {
                intent.putExtra("media-set-path", "/local/screenshots");
            }
            intent.setClass(this, SinglePhotoActivity.class);
            startActivity(intent);
        } catch (Throwable th) {
            GalleryLog.i(TAG, "Start intent error.");
        } finally {
            finish();
        }
    }

    private boolean isScreenShotsAlbum(String bucketId) {
        if (!GalleryUtils.isScreenRecorderExist() || bucketId == null) {
            return false;
        }
        boolean z;
        bucketId = bucketId.split("/")[0];
        if (bucketId.startsWith(String.valueOf(MediaSetUtils.getScreenshotsBucketID()))) {
            z = true;
        } else {
            z = GalleryStorageManager.getInstance().isOuterGalleryStorageScreenshotsBucketID(bucketId);
        }
        return z;
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mItem != null) {
            String chooseType = null;
            if (this.mItem.length != 3) {
                if (this.mItem.length == 2) {
                    switch (which) {
                        case 0:
                            chooseType = "ChooseAlbum";
                            startChooseAlbum();
                            break;
                        case 1:
                            chooseType = "ChooseSinglePhoto";
                            startChooseSinglePhoto();
                            break;
                        default:
                            break;
                    }
                }
            }
            switch (which) {
                case 0:
                    chooseType = "ViewPhoto";
                    startViewPhoto(this.mUri, this.mMimetype);
                    break;
                case 1:
                    chooseType = "ChooseAlbum";
                    startChooseAlbum();
                    break;
                case 2:
                    chooseType = "ChooseSinglePhoto";
                    startChooseSinglePhoto();
                    break;
            }
            if (chooseType != null) {
                ReportToBigData.report(57, String.format("{WidgetDialogPick:%s}", new Object[]{chooseType}));
            }
        }
    }

    private void showPhotoChooseDialog() {
        this.chooseDialog = new Builder(this).setTitle(R.string.widget_type).setItems(this.mItem, this).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                WidgetPhotoChooseDialog.this.finish();
            }
        }).create();
        this.chooseDialog.setOnCancelListener(this);
        this.chooseDialog.show();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.chooseDialog != null) {
            this.chooseDialog.dismiss();
            this.chooseDialog = null;
        }
    }
}
