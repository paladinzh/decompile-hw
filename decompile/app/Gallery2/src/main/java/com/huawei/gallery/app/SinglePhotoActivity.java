package com.huawei.gallery.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ActivityExWrapper;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.ERROR_CODE;
import com.huawei.gallery.app.PhotoPage.PreviewDelgete;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.kidsmode.KidsPhotoPage;
import com.huawei.gallery.ui.PreviewView;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.util.PermissionInfoAlert;
import com.huawei.gallery.util.PermissionManager;

public class SinglePhotoActivity extends GLActivity implements PreviewDelgete {
    private int mDeviceRotation;
    private boolean mIsGif = false;
    private boolean mIsMyFavorite = false;
    protected boolean mIsSecureCamera = false;
    private Path mItemPath;
    PermissionInfoAlert mPermissionInfoAlert;
    private PreviewView mPreviewImageView;
    private Bundle mSaveInstanceState;
    private boolean mShouldSaveInstance;
    private ActivityExWrapper mWrapper;

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        TraceController.beginSection("T extends SinglePhotoActivity.onCreate");
        super.onCreate(savedInstanceState);
        this.mPermissionInfoAlert = new PermissionInfoAlert(this);
        this.mSaveInstanceState = savedInstanceState;
        setContentView(R.layout.layout_gl_activity);
        if (getIntent().getBooleanExtra("is-secure-camera-album", false)) {
            z = GalleryUtils.isKeyguardLocked(this);
        }
        this.mIsSecureCamera = z;
        this.mPreviewImageView = (PreviewView) findViewById(R.id.preview_view);
        this.mWrapper = new ActivityExWrapper(this);
        this.mDeviceRotation = getWindowManager().getDefaultDisplay().getRotation();
        if (PermissionManager.checkHasPermissions(this, getPermissionsType()) || !(this.mIsSecureCamera || shouldRequestPermissions())) {
            if (savedInstanceState == null) {
                initializeByIntent(getIntent());
            } else if (savedInstanceState.getParcelable("key-intent") != null) {
                initializeByIntent((Intent) savedInstanceState.getParcelable("key-intent"));
            } else {
                getStateManager().restoreFromState(savedInstanceState);
            }
        } else if (this.mIsSecureCamera) {
            getWindow().addFlags(524288);
            this.mPermissionInfoAlert.start();
        } else {
            this.mShouldSaveInstance = true;
        }
        TraceController.endSection();
    }

    protected boolean needToRequestPermissions() {
        return !this.mIsSecureCamera;
    }

    protected String[] getPermissionsType() {
        String uri = getIntent().getDataString();
        if (uri == null || !uri.startsWith("content://mms/")) {
            return PermissionManager.getPermissionsStorage();
        }
        return new String[]{"android.permission.READ_SMS"};
    }

    protected int getPermissionRequestCode() {
        return ERROR_CODE.CANCEL_ERROR;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != ERROR_CODE.CANCEL_ERROR) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (-1 == resultCode && PermissionManager.checkHasPermissions(this, getPermissionsType())) {
            requestPermissionSuccess(requestCode);
        } else {
            requestPermissionFailure(requestCode);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mShouldSaveInstance) {
            outState.putParcelable("key-intent", getIntent());
        }
    }

    protected void requestPermissionSuccess(int requestCode) {
        super.requestPermissionSuccess(requestCode);
        if (this.mSaveInstanceState != null) {
            getStateManager().restoreFromState(this.mSaveInstanceState);
        } else {
            initializeByIntent(getIntent());
        }
        onStart();
    }

    protected void requestPermissionFailure(int requestCode) {
        finish();
    }

    protected void onResume() {
        super.onResume();
        if (isPeeking()) {
            int ori;
            if (this.mDeviceRotation == 3) {
                ori = 8;
            } else if (this.mDeviceRotation == 1) {
                ori = 0;
            } else {
                ori = 1;
            }
            setRequestedOrientation(ori);
        }
    }

    protected void onPause() {
        if (this.mPermissionInfoAlert != null) {
            this.mPermissionInfoAlert.stop();
        }
        super.onPause();
    }

    protected void onDestroy() {
        if (this.mPermissionInfoAlert != null) {
            this.mPermissionInfoAlert.stop();
        }
        super.onDestroy();
    }

    private void initializeByIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            boolean isScreenShotEdit = "com.huawei.gallery.action.SCREENSHOTEDIT".equals(action);
            if ("android.intent.action.VIEW".equalsIgnoreCase(action) || "com.huawei.gallery.action.VIEW_PHOTO_FROM_HWCAMERA".equalsIgnoreCase(action)) {
                startViewAction(intent);
            } else if (!"android.intent.action.EDIT".equalsIgnoreCase(action) && !isScreenShotEdit) {
                startDefaultPage();
            } else if (EditorLoadLib.IS_SUPPORT_IMAGE_EDIT) {
                startSimpleEditor(intent, isScreenShotEdit);
            } else {
                startViewAction(intent);
            }
        }
    }

    private void startDefaultPage() {
        startActivity(new Intent(this, GalleryMain.class));
        finish();
    }

    private void startViewAction(Intent intent) {
        Uri uri = intent.getData();
        boolean isViewAsUriImage = BundleUtils.getBoolean(intent.getExtras(), "view-as-uri-image", false);
        boolean isKidsMode = intent.getBooleanExtra("is-kids-mode", false);
        String mediaSetPath = intent.getStringExtra("media-set-path");
        this.mIsMyFavorite = intent.getBooleanExtra("is_my_favorite", false);
        this.mIsGif = intent.getBooleanExtra("is_gif", false);
        String contentType = GalleryUtils.getContentType(this, intent);
        DataManager dm = getDataManager();
        uri = GalleryUtils.convertFileUriToContentUri(this, uri);
        if (!startPageForCursorDir(contentType, intent, uri, dm, isKidsMode)) {
            boolean fromCamera = intent.getBooleanExtra("local-merge-camera-album", false);
            long startTakenTime = intent.getLongExtra("start-taken-time", 0);
            if (isViewAsUriImage) {
                this.mItemPath = GalleryUtils.findPathByUriForUriImage(getGalleryApplication(), uri, contentType);
            } else {
                this.mItemPath = dm.findPathByUri(uri, contentType);
            }
            if (checkValid(contentType, fromCamera)) {
                Bundle data;
                if (!fromCamera) {
                    this.mItemPath = GalleryUtils.updatePathForBurst(this, this.mItemPath);
                }
                if (intent.getExtras() != null) {
                    data = new Bundle(intent.getExtras());
                } else {
                    data = new Bundle();
                }
                data.putBoolean("local-merge-camera-album", fromCamera);
                data.putBoolean("is-secure-camera-album", this.mIsSecureCamera);
                data.putLong("start-taken-time", startTakenTime);
                Path albumPath = fromCamera ? getAlbumPathIfFromCamera(isKidsMode) : isViewAsUriImage ? Path.fromString("/uri/all") : dm.getDefaultSetOf(this.mItemPath);
                if (!(albumPath == null || data.containsKey("media-set-path"))) {
                    data.putString("media-set-path", albumPath.toString());
                }
                if (this.mItemPath != null) {
                    this.mItemPath.clearObject();
                    GalleryLog.d("SinglePhotoActivity", "clear obj for " + this.mItemPath);
                    data.putString("media-item-path", this.mItemPath.toString());
                }
                if (isPeeking()) {
                    this.mPreviewImageView.setVisibility(0);
                    this.mPreviewImageView.setRotate(intent.getIntExtra("key_item_rotate", 0));
                    data.putBoolean("preview_mode", true);
                    data.putString("media-set-path", mediaSetPath);
                } else {
                    this.mPreviewImageView.setVisibility(8);
                }
                if (isKidsMode) {
                    getStateManager().startState(KidsPhotoPage.class, data);
                } else {
                    getStateManager().startState(PhotoPage.class, data);
                }
            }
        }
    }

    private boolean startPageForCursorDir(String contentType, Intent intent, Uri uri, DataManager dm, boolean isKidsMode) {
        if (contentType == null || !contentType.startsWith("vnd.android.cursor.dir")) {
            return false;
        }
        Bundle data = new Bundle();
        int mediaType = intent.getIntExtra("mediaTypes", 0);
        if (!(mediaType == 0 || uri == null)) {
            uri = uri.buildUpon().appendQueryParameter("mediaTypes", String.valueOf(mediaType)).build();
        }
        Path setPath = dm.findPathByUri(uri, null);
        MediaSet mediaSet = null;
        if (setPath != null) {
            mediaSet = (MediaSet) dm.getMediaObject(setPath);
        }
        if (mediaSet != null) {
            if (mediaSet.isLeafAlbum()) {
                data.putString("media-path", setPath.toString());
                data.putString("parent-media-path", dm.getTopSetPath(3));
                getStateManager().startState(SlotAlbumPage.class, data);
            } else {
                data.putString("media-path", setPath.toString());
                Intent target = new Intent(this, ListAlbumSetActivity.class);
                target.putExtras(data);
                startActivity(target);
                finish();
            }
        } else if (isKidsMode) {
            GalleryLog.w("SinglePhotoActivity", "kids mode cannot go to default page!");
            finish();
            return true;
        } else {
            startDefaultPage();
        }
        return true;
    }

    private boolean checkValid(String contentType, boolean fromCamera) {
        if (contentType != null && (this.mItemPath != null || fromCamera)) {
            return true;
        }
        ContextedUtils.showToastQuickly(getActivityContext(), (int) R.string.no_such_item_Toast, 1);
        finish();
        return false;
    }

    private Path getAlbumPathIfFromCamera(boolean isKidsMode) {
        String path;
        if (isKidsMode) {
            path = "/local/kids/camera";
        } else {
            path = "/local/album/from/camera";
        }
        return Path.fromString(path);
    }

    private void startSimpleEditor(Intent intent, boolean isScreenShotEdit) {
        Uri uri = intent.getData();
        String contentType = GalleryUtils.getContentType(this, intent);
        if (!(uri == null || contentType == null)) {
            this.mItemPath = getDataManager().findPathByUri(GalleryUtils.convertFileUriToContentUri(this, uri), contentType);
        }
        if (this.mItemPath == null) {
            ContextedUtils.showToastQuickly(getActivityContext(), (int) R.string.no_such_item_Toast, 1);
            finish();
            return;
        }
        Bundle data = new Bundle();
        data.putString("media-item-path", this.mItemPath.toString());
        data.putBoolean("to-simple-editor", true);
        data.putBoolean("is-screen-shot-edit", isScreenShotEdit);
        data.putBoolean("editor_photo_has_result", intent.getBooleanExtra("editor_photo_has_result", false));
        data.putBoolean("preview_photo_no_bar", intent.getBooleanExtra("preview_photo_no_bar", false));
        getStateManager().startState(PhotoPage.class, data);
    }

    public void updatePreviewView(Bitmap bitmap) {
        if (bitmap != null) {
            this.mPreviewImageView.setBitmap(bitmap);
        }
    }

    public void onBackPressed() {
        if (isPeeking()) {
            this.mWrapper.run("onExit");
        } else {
            super.onBackPressed();
        }
    }

    private boolean isPeeking() {
        Object ret = this.mWrapper.run("getIsPeeking");
        if (ret instanceof Boolean) {
            return ((Boolean) ret).booleanValue();
        }
        return false;
    }

    public void setPreviewVisible(int visible) {
        this.mPreviewImageView.setVisibility(visible);
    }

    protected void onMultiWinStateModeChangedCallback() {
        if (isPeeking()) {
            onBackPressed();
        }
    }
}
