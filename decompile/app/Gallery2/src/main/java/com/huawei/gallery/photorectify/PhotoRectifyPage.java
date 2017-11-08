package com.huawei.gallery.photorectify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.IImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.EmptyPhotoView;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.app.AbsPhotoPage;
import com.huawei.gallery.editor.cache.ImageLoader;
import com.huawei.gallery.photorectify.RectifyUtils.RectifyImageListener;
import com.huawei.gallery.util.LayoutHelper;
import com.intsig.scanner.ScannerSDK;
import java.io.File;

public class PhotoRectifyPage extends AbsPhotoPage implements RectifyImageListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private int[] mBitmapDetectBound = new int[8];
    private String mCurrentInputImagePath;
    private MediaItem mItem;
    private String mItemFilePath;
    private String mItemMimeType;
    private FutureListener<BitmapRegionDecoder> mLargeListener = new FutureListener<BitmapRegionDecoder>() {
        public void onFutureDone(Future<BitmapRegionDecoder> future) {
            BitmapRegionDecoder decoder = (BitmapRegionDecoder) future.get();
            if (decoder == null) {
                ContextedUtils.showToastQuickly(PhotoRectifyPage.this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 0);
                GalleryLog.w("PhotoRectifyPage", "onFutureDone decoder == null");
                PhotoRectifyPage.this.mHandler.sendEmptyMessage(50);
                return;
            }
            int width = decoder.getWidth();
            int height = decoder.getHeight();
            Options options = new Options();
            int displayWidth = GalleryUtils.getWidthPixels();
            int displayHeight = GalleryUtils.getHeightPixels();
            options.inSampleSize = BitmapUtils.computeSampleSize(width, height, Math.min(displayWidth, displayHeight) / 2, displayWidth * displayHeight);
            Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
            PhotoRectifyPage.this.mHandler.removeMessages(92);
            PhotoRectifyPage.this.mHandler.sendMessage(PhotoRectifyPage.this.mHandler.obtainMessage(92, bitmap));
        }
    };
    private Future<?> mLoadTask;
    private PhotoRectifyView mPhotoRectifyView;
    private Thread mPrepareThread;
    private ProgressBar mProgressBar;
    private RectifyUtils mRectifyUtils;
    private boolean mSaveCurrentState = true;
    private ScannerSDK mScannerSDK;
    private String mTempImagePath;
    private ThreadPool mThreadPool;
    private final OnSystemUiVisibilityChangeListener mUiVisibility = new OnSystemUiVisibilityChangeListener() {
        public void onSystemUiVisibilityChange(int visibility) {
            if (visibility == 0) {
                PhotoRectifyPage.this.mHandler.removeMessages(2);
                PhotoRectifyPage.this.mHandler.sendEmptyMessageDelayed(2, (long) PhotoRectifyPage.HIDE_BARS_TIMEOUT);
            }
        }
    };
    private float[] mViewTrimBound = new float[8];

    public class DetectBorderTask extends AsyncTask<Void, Void, Boolean> {
        private String mPath;

        public DetectBorderTask(String path) {
            this.mPath = path;
        }

        protected void onPreExecute() {
            if (PhotoRectifyPage.this.mProgressBar != null) {
                PhotoRectifyPage.this.mProgressBar.setVisibility(0);
            }
        }

        protected Boolean doInBackground(Void... params) {
            GalleryLog.d("PhotoRectifyPage", "DetectBorderTask, doInBackground");
            boolean success = false;
            if (PhotoRectifyPage.this.mScannerSDK != null) {
                int[] imgBound = RectifyUtils.getImageSizeBound(this.mPath);
                PhotoRectifyPage.this.mPhotoRectifyView.initImageData(imgBound[0], imgBound[1], PhotoRectifyPage.this.mItem.getRotation());
                int threadContext = PhotoRectifyPage.this.mScannerSDK.initThreadContext();
                int imageStruct = PhotoRectifyPage.this.mScannerSDK.decodeImageS(this.mPath);
                if (imageStruct != 0) {
                    int[] border = PhotoRectifyPage.this.mScannerSDK.detectBorder(threadContext, imageStruct);
                    if (border == null) {
                        border = PhotoRectifyPage.this.mPhotoRectifyView.getDefaultRectifyBounds();
                    }
                    PhotoRectifyPage.this.mViewTrimBound = RectifyUtils.getScanBoundF(imgBound, border);
                    if (PhotoRectifyPage.this.mViewTrimBound != null) {
                        for (int i = 0; i < PhotoRectifyPage.this.mViewTrimBound.length; i++) {
                            PhotoRectifyPage.this.mBitmapDetectBound[i] = (int) PhotoRectifyPage.this.mViewTrimBound[i];
                        }
                    }
                    PhotoRectifyPage.this.mScannerSDK.releaseImage(imageStruct);
                    success = true;
                }
                PhotoRectifyPage.this.mScannerSDK.destroyContext(threadContext);
            }
            return Boolean.valueOf(success);
        }

        protected void onPostExecute(Boolean result) {
            if (PhotoRectifyPage.this.mProgressBar != null) {
                PhotoRectifyPage.this.mProgressBar.setVisibility(8);
            }
            if (result.booleanValue()) {
                PhotoRectifyPage.this.mPhotoRectifyView.setRectifyBounds(PhotoRectifyPage.this.mViewTrimBound);
            } else {
                GalleryLog.d("PhotoRectifyPage", "DetectBorderTask onPost not execute result=" + result + " mIsActive=" + PhotoRectifyPage.this.mIsActive);
            }
        }
    }

    class TrimTask extends AsyncTask<Void, Void, Boolean> {
        private String mPath;

        public TrimTask(String path) {
            this.mPath = path;
        }

        protected void onPreExecute() {
            if (PhotoRectifyPage.this.mProgressBar != null) {
                PhotoRectifyPage.this.mProgressBar.setVisibility(0);
            }
        }

        protected Boolean doInBackground(Void... params) {
            GalleryLog.d("PhotoRectifyPage", "TrimTask, doInBackground");
            boolean succeed = false;
            if (PhotoRectifyPage.this.mScannerSDK != null) {
                int threadContext = PhotoRectifyPage.this.mScannerSDK.initThreadContext();
                int imageStruct = PhotoRectifyPage.this.mScannerSDK.decodeImageS(this.mPath);
                if (imageStruct != 0) {
                    PhotoRectifyPage.this.mScannerSDK.trimImage(threadContext, imageStruct, PhotoRectifyPage.this.mPhotoRectifyView.hasModifiedRect() ? PhotoRectifyPage.this.mPhotoRectifyView.getRectifyBounds() : PhotoRectifyPage.this.mBitmapDetectBound);
                    PhotoRectifyPage.this.mScannerSDK.saveImage(imageStruct, PhotoRectifyPage.this.mTempImagePath, 100);
                    PhotoRectifyPage.this.mScannerSDK.releaseImage(imageStruct);
                    succeed = true;
                }
                PhotoRectifyPage.this.mScannerSDK.destroyContext(threadContext);
                if (PhotoRectifyPage.this.mIsActive) {
                    PhotoRectifyPage.this.mRectifyUtils.processImageData(PhotoRectifyPage.this.mHost.getActivity(), PhotoRectifyPage.this.mItemMimeType, PhotoRectifyPage.this.mCurrentInputImagePath, PhotoRectifyPage.this.mTempImagePath, PhotoRectifyPage.this.mItemFilePath);
                }
                PhotoRectifyPage.this.mRectifyUtils.deleteTmpFile();
            }
            return Boolean.valueOf(succeed);
        }

        protected void onPostExecute(Boolean result) {
            if (PhotoRectifyPage.this.mProgressBar != null) {
                PhotoRectifyPage.this.mProgressBar.setVisibility(8);
            }
            GalleryLog.d("PhotoRectifyPage", "TrimTask result=" + result);
            PhotoRectifyPage.this.setStateResult(-1, new Intent());
            PhotoRectifyPage.this.mHandler.sendEmptyMessage(50);
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 9;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 17;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 18;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 19;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 20;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 21;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 24;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 25;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 26;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 27;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 28;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 31;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 32;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 33;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 34;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 35;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 36;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 1;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 37;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 2;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 39;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 40;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 41;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 42;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 43;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 44;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 45;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 46;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 47;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 48;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 49;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 50;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 51;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 52;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 53;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 54;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 55;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 57;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 58;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 60;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 61;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 62;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 63;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 64;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 65;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 66;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 67;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 68;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 69;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 70;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 71;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 72;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 73;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 74;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 75;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 76;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 77;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 78;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 79;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 80;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 81;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 82;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 83;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 84;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 85;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 87;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 88;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 89;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 90;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        boolean z;
        super.onCreate(data, storedState);
        if (storedState == null) {
            z = true;
        } else {
            z = false;
        }
        this.mSaveCurrentState = z;
        this.mFlags |= 16;
        if (data.getBoolean("is-secure-camera-album", false)) {
            this.mFlags |= FragmentTransaction.TRANSIT_EXIT_MASK;
        }
        try {
            this.mScannerSDK = new ScannerSDK(this.mHost.getGalleryContext().getAndroidContext());
        } catch (UnsatisfiedLinkError e) {
            GalleryLog.w("PhotoRectifyPage", "ScannerSDK load lib failed!");
        } catch (Exception e2) {
            GalleryLog.w("PhotoRectifyPage", "ScannerSDK init failed!");
        }
        initViews();
        initData(data);
    }

    private void initData(Bundle data) {
        Path itemPath = getItemPath(data);
        if (itemPath != null) {
            this.mItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
        }
        if (this.mItem == null) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
            return;
        }
        this.mThreadPool = this.mHost.getGalleryContext().getThreadPool();
        this.mItemFilePath = this.mItem.getFilePath();
        this.mItemMimeType = this.mItem.getMimeType();
        this.mRectifyUtils = new RectifyUtils(this.mHost.getActivity(), this.mItem, this);
        if (this.mRectifyUtils.isCacheDirInvalid()) {
            this.mHandler.sendEmptyMessage(50);
        }
        prepareOriginData();
    }

    private void initViews() {
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(this.mHost.getActivity()).inflate(R.layout.progress_bar, (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root));
        this.mProgressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        this.mPhotoRectifyView = new PhotoRectifyView(this.mHost.getActivity(), root, this.mHost.getGLRoot());
        this.mRootPane.addComponent(this.mPhotoRectifyView);
    }

    private void prepareOriginData() {
        this.mPrepareThread = new Thread(new Runnable() {
            public void run() {
                if (!PhotoRectifyPage.this.mRectifyUtils.prepare()) {
                    GalleryLog.w("PhotoRectifyPage", "prepare Origin data fail...");
                }
            }
        });
        this.mPrepareThread.start();
    }

    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                this.mHost.getGLRoot().setLightsOutMode(true);
                return;
            case 91:
                loadTrimImageFile(this.mRectifyUtils.getRectifyOffset() > 0 ? this.mRectifyUtils.getOriginTmpFilePath() : this.mItemFilePath);
                return;
            case 92:
                onDecodeLargeComplete((Bitmap) msg.obj);
                return;
            default:
                super.onHandleMessage(msg);
                return;
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(348);
        ActionMode am = this.mActionBar.enterActionMode(this.mSaveCurrentState);
        am.setTitle((int) R.string.folder_doc_rectify);
        am.setBothAction(Action.NO, Action.OK);
        am.show();
        this.mActionBar.setHeadBarVisible(true);
        this.mActionBar.setActionPanelVisible(false);
        this.mActionBar.setMenuVisible(false);
        this.mShowBars = true;
        return true;
    }

    protected void onResume() {
        super.onResume();
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(this.mUiVisibility);
        LayoutHelper.getNavigationBarHandler().update();
    }

    protected void onPause() {
        super.onPause();
        this.mHandler.removeMessages(91);
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(null);
    }

    protected boolean onItemSelected(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                onBackPressed();
                return true;
            case 2:
                startTrim();
                return true;
            default:
                return false;
        }
    }

    protected boolean onBackPressed() {
        if (this.mProgressBar != null && this.mProgressBar.getVisibility() == 0) {
            return true;
        }
        this.mRectifyUtils.deleteTmpFile();
        setStateResult(0, new Intent());
        return super.onBackPressed();
    }

    protected void onDestroy() {
        if (this.mProgressBar != null) {
            this.mProgressBar.setVisibility(8);
        }
        this.mPhotoRectifyView.destroy();
        checkAndWaitPrepareComplete();
        if (this.mSaveCurrentState) {
            this.mActionBar.leaveCurrentMode();
        }
        hideBars(true);
        this.mActionBar.setActionBarVisible(false);
        this.mShowBars = false;
        super.onDestroy();
    }

    private void checkAndWaitPrepareComplete() {
        try {
            if (this.mPrepareThread != null) {
                this.mPrepareThread.join(1000);
            }
            if (this.mLoadTask != null) {
                Future<?> task = this.mLoadTask;
                task.cancel();
                task.waitDone();
                this.mLoadTask = null;
            }
        } catch (InterruptedException e) {
            GalleryLog.i("PhotoRectifyPage", "Thread.join() failed in checkAndWaitPrepareComplete method.");
        }
    }

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mPhotoRectifyView.layout(0, 0, right - left, bottom - top);
    }

    public void onLoadStateChange(int state) {
        if (state == 2) {
            this.mHandler.sendEmptyMessage(50);
        }
    }

    protected void showBars(boolean barWithAnim) {
    }

    protected void hideBars(boolean barWithAnim) {
        this.mHost.getGLRoot().setLightsOutMode(true);
        if (this.mShowBars) {
            this.mShowBars = false;
            this.mHandler.removeMessages(1);
            this.mActionBar.setActionBarVisible(false, barWithAnim);
        }
    }

    public void refreshImage(byte[] bytes, int offset, int length) {
        invalidateData(bytes, offset, length);
    }

    public void invalidateData(byte[] bytes, int offset, int length) {
        if (this.mLoadTask != null) {
            Future<?> task = this.mLoadTask;
            task.cancel();
            task.waitDone();
            this.mLoadTask = null;
        }
        if (bytes == null) {
            this.mLoadTask = this.mThreadPool.submit(this.mItem.requestLargeImage(), this.mLargeListener);
        } else {
            this.mLoadTask = this.mThreadPool.submit(((IImage) this.mItem).requestLargeImage(bytes, offset, length), this.mLargeListener);
        }
    }

    private void onDecodeLargeComplete(Bitmap bitmap) {
        if (bitmap == null) {
            try {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
            } catch (Throwable t) {
                GalleryLog.w("PhotoRectifyPage", "fail to decode large." + t.getMessage());
            }
        } else {
            this.mPhotoRectifyView.updateBackGroundBitmap(ImageLoader.orientBitmap(bitmap, ImageLoader.getMetadataOrientation(this.mItem.getRotation()), true));
        }
    }

    public void prepareCompleted() {
        this.mHandler.sendEmptyMessage(91);
    }

    private void loadTrimImageFile(String imageFilePath) {
        if (TextUtils.isEmpty(imageFilePath)) {
            GalleryLog.d("PhotoRectifyPage", "imageFilePath is empty");
        } else if (new File(imageFilePath).exists()) {
            GalleryLog.d("PhotoRectifyPage", "loadTrimImageFile, imageFilePath=" + imageFilePath);
            this.mCurrentInputImagePath = imageFilePath;
            new DetectBorderTask(imageFilePath).execute(new Void[0]);
        } else {
            GalleryLog.d("PhotoRectifyPage", "imageFilePath is not exist");
        }
    }

    private void startTrim() {
        GalleryLog.d("PhotoRectifyPage", "startTrim");
        this.mTempImagePath = this.mRectifyUtils.getTrimTmpFilePath();
        if (!(TextUtils.isEmpty(this.mTempImagePath) || TextUtils.isEmpty(this.mCurrentInputImagePath))) {
            new TrimTask(this.mCurrentInputImagePath).execute(new Void[0]);
        }
    }

    protected AbsPhotoView createPhotoView(GalleryContext context, GLRoot root) {
        return new EmptyPhotoView();
    }
}
