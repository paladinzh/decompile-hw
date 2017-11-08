package com.android.gallery3d.gadget;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.gadget.WidgetPhotoManager.PhotoInfo;
import com.android.gallery3d.gadget.WidgetPhotoManager.PhotoObserver;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.storage.GalleryStorageManager;

@RemoteView
public class WidgetPhotoView extends RelativeLayout implements PhotoObserver {
    public static final String ACTION_CHOOSE = "android.appwidget.action.APPWIDGET_CONFIGURE";
    private static final String EXTRA_REQUEST_PERMISSION = "request_permission";
    private static final String EXTRA_TARGET_INTENT = "target_intent";
    private static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    private static final String LAUNCHER_REQUEST_PERMISSION_ACTIVITY_NAME = "com.huawei.android.launcher.RequestPermissionActivity";
    private static final int NOTIFY_OBSERVOR_PHOTO = 2;
    private static final int REQUEST_DEFAULT_PHOTO = 4;
    private static final int REQUEST_DESTROY_VIEW = 3;
    private static final int REQUEST_UPDATE_PHOTO = 1;
    public static final Uri ROOT_URI = Uri.parse("content://media/external/images/media");
    private static final String TAG = "WidgetPhotoView";
    private static final int TIME_WAIT = 10000;
    private static final int UPDATE_PHOTO_TIME_WAIT = 15000;
    public static final String WIDGET_CONFIG_DIALOG = "com.android.gallery3d.gadget.WidgetPhotoChooseDialog";
    private boolean isAlbumCoverNeedRadius = false;
    private int mAlbumCoverHeight = 0;
    private int mAlbumCoverMarginLeft = 0;
    private int mAlbumCoverMarginTop = 0;
    private int mAlbumCoverWidth = 0;
    private int mAlbumNameMarginBottom = 0;
    private int mAlbumNameMarginLeft = 0;
    private Bitmap mBitmap;
    private int mBottomMarginLimit = Integer.MAX_VALUE;
    private String mBucketId;
    private int mClickedPhotoId;
    private int mDefaultViewHeight = 0;
    private int mDefaultViewWidth = 0;
    private boolean mDrawable = true;
    private HandlerThread mHandlerThread;
    private HwTransitionReflection mHwTransitionReflection;
    private volatile boolean mIsFirstUpdate = true;
    private KeyguardManager mKeyguardManager;
    private Handler mMainHandler;
    private String mMimetype;
    private boolean mNoPhotos = false;
    private volatile boolean mNotifyFlag = true;
    private int mPhotoCount = 0;
    private int mPhotoId;
    private PhotoInfo mPhotoInfo;
    private int mPhotoPosition = 0;
    private WidgetImageView mPhotoView;
    private Thread mSaveFileProcess;
    private Bitmap mSavedBitmap;
    private TextView mTextView;
    private ThreadHandler mThreadHandler;
    private String mUri;
    private int mViewHeight = 0;
    private int mViewWidth = 0;
    private int mWidgetId;

    private class ThreadHandler extends Handler {
        public ThreadHandler(Looper looper) {
            super(looper);
            WidgetPhotoView.this.mKeyguardManager = (KeyguardManager) WidgetPhotoView.this.getContext().getSystemService("keyguard");
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    boolean booleanValue = msg.obj != null ? ((Boolean) msg.obj).booleanValue() : false;
                    if (WidgetPhotoView.this.getIsUpdatePicValue() || WidgetPhotoView.this.mPhotoCount == 1) {
                        WidgetPhotoView.this.updatePic(booleanValue);
                    }
                    if (WidgetPhotoView.this.mIsFirstUpdate || WidgetPhotoView.this.isNotify()) {
                        WidgetPhotoView.this.mMainHandler.removeMessages(2);
                        if (booleanValue) {
                            WidgetPhotoView.this.mMainHandler.obtainMessage(2, Boolean.TRUE).sendToTarget();
                        } else {
                            WidgetPhotoView.this.mMainHandler.sendEmptyMessage(2);
                        }
                    }
                    if (!booleanValue) {
                        WidgetPhotoView.this.setNextPhotoAttribute();
                    }
                    if (WidgetPhotoView.this.mPhotoCount > 1) {
                        WidgetPhotoView.this.mIsFirstUpdate = false;
                        if (!WidgetPhotoView.this.mThreadHandler.hasMessages(1)) {
                            WidgetPhotoView.this.mThreadHandler.sendEmptyMessageDelayed(1, 15000);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public WidgetPhotoView(Context context) {
        super(context);
        init();
    }

    public WidgetPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WidgetPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (this.mHwTransitionReflection == null) {
            this.mHwTransitionReflection = new HwTransitionReflection(this, HwTransitionReflection.TRANS_TYPE_PAGE);
        }
        GalleryUtils.initializeScreenshotsRecoder(getContext());
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        GalleryLog.d(TAG, "enter onFinishInflate");
        this.mPhotoView = (WidgetImageView) findViewById(R.id.appwidget_image_view);
        this.mTextView = (TextView) findViewById(R.id.appwidget_album_name);
        initView();
        this.mBottomMarginLimit = ((LayoutParams) this.mTextView.getLayoutParams()).bottomMargin;
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WidgetPhotoView.ACTION_CHOOSE);
                intent.setFlags(268468224);
                intent.putExtra("appWidgetId", WidgetPhotoView.this.mWidgetId);
                if (!(WidgetPhotoView.this.mNoPhotos || WidgetPhotoView.this.mPhotoInfo == null || WidgetPhotoView.this.mClickedPhotoId <= 0)) {
                    intent.putExtra("Uri", ContentUris.withAppendedId(WidgetPhotoView.ROOT_URI, (long) WidgetPhotoView.this.mClickedPhotoId));
                    intent.putExtra("Mimetype", WidgetPhotoView.this.mPhotoInfo.mMimetype);
                }
                intent.setComponent(new ComponentName(HicloudAccountManager.PACKAGE_NAME, WidgetPhotoView.WIDGET_CONFIG_DIALOG));
                if (WidgetPhotoView.this.checkLauncherHasStoragePermission(WidgetPhotoView.this.getContext())) {
                    try {
                        WidgetPhotoView.this.getContext().startActivity(intent);
                        return;
                    } catch (Exception ex) {
                        GalleryLog.w(WidgetPhotoView.TAG, ex.toString());
                        return;
                    }
                }
                WidgetPhotoView.this.requestLauncherStoragePermission(intent);
            }
        });
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((View) getParent()).setPadding(0, 0, 0, 0);
        this.mWidgetId = WidgetUtils.getCurrentAppWidgetId(this);
        WidgetPhotoManager.getInstance().regeditPhotoObserver(getContext(), this.mWidgetId, this);
        initialize();
        initViewBackground();
        this.mMainHandler.removeMessages(3);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mViewWidth = w;
        this.mViewHeight = h;
        setWidgetParams();
        if (oldw == 0 || oldh == 0) {
            initViewBackground();
            if (this.mNoPhotos) {
                this.mThreadHandler.sendEmptyMessage(1);
                this.mMainHandler.sendEmptyMessage(2);
            }
        } else if (this.mNoPhotos) {
            this.mMainHandler.sendEmptyMessage(4);
        } else {
            this.mThreadHandler.removeMessages(1);
            this.mThreadHandler.sendMessageDelayed(this.mThreadHandler.obtainMessage(1, Boolean.TRUE), 60);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    private void setText(int resid) {
        if (this.mTextView != null) {
            this.mTextView.setText(resid);
        } else {
            GalleryLog.d(TAG, "null pointer");
        }
    }

    private void setText(CharSequence text) {
        if (this.mTextView != null) {
            this.mTextView.setText(text);
        } else {
            GalleryLog.d(TAG, "null pointer");
        }
    }

    private void setImageBitmap(Bitmap bm) {
        if (this.mPhotoView != null) {
            this.mPhotoView.setImageBitmap(bm);
        } else {
            GalleryLog.d(TAG, "null pointer");
        }
    }

    private void initViewBackground() {
        if (this.mNoPhotos) {
            setDefaultBackground();
            setText((int) R.string.widget_type_album);
        } else if (this.mWidgetId != 0) {
            Bitmap savedFile = WidgetUtils.readPng(this.mWidgetId);
            if (savedFile != null) {
                Bitmap cutBitmap = getCutBitmap(savedFile);
                savedFile.recycle();
                if (cutBitmap != null) {
                    this.mBitmap = getRoundBitmap(cutBitmap);
                    if (this.mBitmap != cutBitmap) {
                        cutBitmap.recycle();
                    }
                }
            }
            if (this.mBitmap != null) {
                setImageBitmap(this.mBitmap);
                if (this.mPhotoInfo != null) {
                    setAlbumName(this.mPhotoInfo);
                } else {
                    setText(null);
                }
            } else {
                setDefaultBackground();
                setText((int) R.string.widget_type_album);
            }
        }
        setAlbumNameParams();
        setAlbumNameLayoutParams();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        WidgetPhotoManager.getInstance().unregeditPhotoObserver(getContext(), this);
        if (this.mMainHandler != null) {
            this.mMainHandler.sendEmptyMessageDelayed(3, 20000);
        }
    }

    @RemotableViewMethod
    public void setViewChooseAlbum(int meWidgetId) {
        this.mNoPhotos = true;
        this.mWidgetId = meWidgetId;
        setDefaultBackground();
        setAlbumNameParams();
        setAlbumNameLayoutParams();
        setText((int) R.string.widget_type_album);
    }

    @RemotableViewMethod
    public void setViewWidgetId(int meWidgetId) {
        this.mWidgetId = meWidgetId;
    }

    @RemotableViewMethod
    public void setViewBucketId(String bucketId) {
        if (bucketId != null) {
            if (!bucketId.equals(this.mBucketId)) {
                this.mPhotoPosition = 0;
                this.mBucketId = bucketId;
            }
            setWillNotDraw(false);
            if (this.mHandlerThread != null) {
                WidgetPhotoManager.getInstance().changePhotoObserver(getContext(), this.mWidgetId, this);
            }
        }
    }

    private void destroy() {
        this.mThreadHandler.removeCallbacksAndMessages(null);
        this.mMainHandler.removeCallbacksAndMessages(null);
        this.mHandlerThread.quit();
    }

    @RemotableViewMethod
    public void setViewDelete(int bucketId) {
        destroy();
    }

    private boolean isNotify() {
        if (!this.mKeyguardManager.inKeyguardRestrictedInputMode() && this.mDrawable && getWindowVisibility() == 0 && WidgetUtils.isVisible(this)) {
            return true;
        }
        this.mNotifyFlag = false;
        return false;
    }

    private void setNextPhotoAttribute() {
        if (this.mNotifyFlag) {
            synchronized (WidgetPhotoManager.getInstance()) {
                this.mPhotoPosition++;
                if (this.mPhotoPosition >= this.mPhotoCount) {
                    this.mPhotoPosition = 0;
                }
            }
            if (this.mPhotoCount > 1) {
                this.mPhotoInfo = WidgetPhotoManager.getInstance().getPhotoInfo(this.mBucketId, this.mWidgetId, this.mPhotoPosition);
                if (this.mPhotoInfo == null) {
                    this.mPhotoPosition = 0;
                    this.mPhotoInfo = WidgetPhotoManager.getInstance().getPhotoInfo(this.mBucketId, this.mWidgetId, this.mPhotoPosition);
                }
            }
        }
    }

    private boolean getIsUpdatePicValue() {
        boolean isUpdatePic = true;
        if (!WidgetUtils.isVisible(this)) {
            isUpdatePic = false;
        } else if (getWindowVisibility() != 0) {
            isUpdatePic = false;
        }
        if (isUpdatePic || !this.mIsFirstUpdate) {
            return isUpdatePic;
        }
        return true;
    }

    private void initialize() {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread("handler_thread");
            this.mHandlerThread.start();
            this.mThreadHandler = new ThreadHandler(this.mHandlerThread.getLooper());
        }
        if (this.mMainHandler == null) {
            this.mMainHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 2:
                            WidgetPhotoView.this.onBitmapChange(WidgetPhotoView.this.mBitmap, WidgetPhotoView.this.mUri, WidgetPhotoView.this.mMimetype, WidgetPhotoView.this.mNoPhotos, WidgetPhotoView.this.mPhotoId, msg.obj != null ? ((Boolean) msg.obj).booleanValue() : false);
                            return;
                        case 3:
                            WidgetPhotoView.this.destroy();
                            return;
                        case 4:
                            WidgetPhotoView.this.setDefaultBackground();
                            WidgetPhotoView.this.setText((int) R.string.widget_type_album);
                            WidgetPhotoView.this.setAlbumNameLayoutParams();
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mDrawable = true;
    }

    private void getNextPhoto() {
        if (this.mPhotoInfo == null) {
            GalleryLog.e(TAG, "WidgetPhotoView getNextCursor fail");
            return;
        }
        this.mUri = this.mPhotoInfo.mUri;
        this.mMimetype = this.mPhotoInfo.mMimetype;
        this.mPhotoId = this.mPhotoInfo.mId;
    }

    private Bitmap getCompressBitmap() {
        Bitmap bitmap = null;
        int maxWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int maxHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(this.mUri, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                GalleryLog.e(TAG, " compress getCompressBitmap outWidth outHeight err!!!");
                return null;
            } else if (this.mAlbumCoverWidth <= 0 || this.mAlbumCoverHeight <= 0) {
                return null;
            } else {
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Config.RGB_565;
                options.inSampleSize = BitmapUtils.computeSampleSize(options.outWidth, options.outHeight, Math.min(maxWidth, maxHeight), (maxWidth * maxHeight) * 2);
                bitmap = BitmapFactory.decodeFile(this.mUri, options);
                return bitmap;
            }
        } catch (Throwable exp) {
            GalleryLog.e(TAG, "Compress getCompressBitmap  err!!!" + exp.getMessage());
        }
    }

    private Bitmap getCutBitmap(Bitmap compressBitmap) {
        int w = compressBitmap.getWidth();
        int h = compressBitmap.getHeight();
        int viewWidth = this.mAlbumCoverWidth;
        int viewHeight = this.mAlbumCoverHeight;
        float scale = Math.max(((float) viewWidth) / ((float) w), ((float) viewHeight) / ((float) h));
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(((float) viewWidth) / 2.0f, ((float) viewHeight) / 2.0f);
            canvas.rotate(0.0f);
            canvas.scale(scale, scale);
            canvas.drawBitmap(compressBitmap, ((float) (-w)) / 2.0f, ((float) (-h)) / 2.0f, new Paint(6));
            return bitmap;
        } catch (Throwable thow) {
            GalleryLog.e(TAG, "Compress getCutBitmap cutBitmap err!" + thow.getMessage());
            return bitmap;
        }
    }

    private void statusReset() {
        this.mUri = null;
        this.mMimetype = null;
        this.mNotifyFlag = true;
    }

    private void updatePic(boolean resizePicture) {
        if (this.mPhotoCount <= 0 || this.mPhotoInfo == null) {
            this.mNoPhotos = true;
            statusReset();
            return;
        }
        Bitmap cutBitmap;
        if (!resizePicture) {
            getNextPhoto();
        }
        Bitmap compressBitmap = null;
        if (!(this.mSaveFileProcess == null || this.mSaveFileProcess.isAlive())) {
            this.mSaveFileProcess = null;
        }
        if (resizePicture && this.mWidgetId != 0 && this.mSaveFileProcess == null) {
            compressBitmap = WidgetUtils.readPng(this.mWidgetId);
        }
        if (compressBitmap == null) {
            compressBitmap = getCompressBitmap();
            if (compressBitmap == null) {
                this.mNoPhotos = false;
                statusReset();
                GalleryLog.e(TAG, "WidgetPhotoView getCompressBitmap error ");
                return;
            }
            if (this.mPhotoInfo != null) {
                Bitmap rotateBitmap = rotateBitmap(this.mPhotoInfo.mOrientation, compressBitmap);
                if (!(rotateBitmap == null || rotateBitmap == compressBitmap)) {
                    compressBitmap.recycle();
                    compressBitmap = rotateBitmap;
                }
            }
            cutBitmap = getCutBitmap(compressBitmap);
            if (!(this.mSavedBitmap == null || this.mSavedBitmap.isRecycled())) {
                this.mSavedBitmap.recycle();
                this.mSavedBitmap = null;
            }
            this.mSavedBitmap = compressBitmap;
            this.mSaveFileProcess = new Thread(new Runnable() {
                public void run() {
                    Bitmap savedBitmap = WidgetPhotoView.this.mSavedBitmap;
                    WidgetPhotoView.this.mSavedBitmap = null;
                    if (savedBitmap != null && !savedBitmap.isRecycled()) {
                        WidgetUtils.savePng(savedBitmap, WidgetPhotoView.this.mWidgetId);
                        savedBitmap.recycle();
                    }
                }
            });
            this.mSaveFileProcess.start();
        } else {
            cutBitmap = getCutBitmap(compressBitmap);
            if (!compressBitmap.isRecycled()) {
                compressBitmap.recycle();
            }
        }
        Bitmap roundBitmap = getRoundBitmap(cutBitmap);
        if (!(cutBitmap == roundBitmap || cutBitmap == null || cutBitmap.isRecycled())) {
            cutBitmap.recycle();
        }
        if (roundBitmap != null) {
            this.mNoPhotos = false;
            this.mNotifyFlag = true;
        }
    }

    private Bitmap getRoundBitmap(Bitmap cutBitmap) {
        if (cutBitmap == null) {
            this.mNoPhotos = false;
            statusReset();
            GalleryLog.e(TAG, "WidgetPhotoView getCutBitmap error ");
            return null;
        }
        Bitmap roundBitmap = null;
        try {
            if (this.isAlbumCoverNeedRadius) {
                roundBitmap = CutBitmapUtils.getCornerBitmap(cutBitmap, getResources(), R.drawable.widget_bg_template);
            } else {
                roundBitmap = cutBitmap;
            }
        } catch (Throwable exp) {
            GalleryLog.i(TAG, "CutBitmapUtils.getCornerBitmap() failed." + exp.getMessage());
        }
        if (roundBitmap == null) {
            GalleryLog.e(TAG, "WidgetPhotoView updatePic getRoundBitmap is null");
            this.mNoPhotos = false;
            statusReset();
            return null;
        }
        if (roundBitmap == cutBitmap || cutBitmap.isRecycled()) {
            this.mBitmap = cutBitmap;
        } else {
            this.mBitmap = roundBitmap;
        }
        return this.mBitmap;
    }

    public void draw(Canvas canvas) {
        try {
            if (this.mPhotoCount <= 0 || WidgetUtils.isScreenScroll(this)) {
                super.draw(canvas);
            } else if (!this.mHwTransitionReflection.animateDraw(canvas)) {
                super.draw(canvas);
            }
        } catch (Exception ex) {
            GalleryLog.i(TAG, "draw canvas err." + ex.getMessage());
        }
    }

    private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        if (angle % 360 == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void onBitmapChange(Bitmap bitmap, String uri, String mimetype, boolean noPhotos, int photoId, boolean onResizeMode) {
        this.mClickedPhotoId = photoId;
        this.mUri = uri;
        this.mMimetype = mimetype;
        this.mDrawable = false;
        if (this.mPhotoView != null && this.mTextView != null) {
            if (noPhotos) {
                setDefaultBackground();
                setText((int) R.string.widget_type_album);
            } else if (bitmap == null) {
                setDefaultBackground();
                if (this.mPhotoCount <= 0 || this.mPhotoInfo == null) {
                    setText((int) R.string.widget_type_album);
                } else {
                    setAlbumName(this.mPhotoInfo);
                }
            } else {
                if (!(onResizeMode || WidgetUtils.isScreenScroll(this))) {
                    this.mHwTransitionReflection.startAnimation(this);
                }
                setAlbumCoverLayoutParams();
                setImageBitmap(bitmap);
                setAlbumName(this.mPhotoInfo);
            }
            setAlbumNameLayoutParams();
        }
    }

    private void setAlbumName(PhotoInfo photoInfo) {
        if (photoInfo != null) {
            setText(updateAlbumName(getResId(photoInfo.mBucketId, getContext()), photoInfo.mBucketId, photoInfo.mAlbumName));
        }
    }

    public String updateAlbumName(int id, int bucketID, String albumName) {
        if (id != 0) {
            switch (id) {
                case R.string.screenshots_folder_multi_sdcard:
                case R.string.external_storage_multi_root_directory:
                case R.string.camera_folder_multi_sdcard:
                    if (GalleryStorageManager.getInstance().getGalleryStorageByBucketID(bucketID) != null) {
                        return getContext().getResources().getString(id, new Object[]{galleryStorage.getName()});
                    }
                    break;
                default:
                    return getContext().getResources().getString(id);
            }
        }
        return albumName;
    }

    private int getResId(int bucketId, Context context) {
        if (bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(bucketId)) {
            return MediaSetUtils.getCameraAlbumStringId();
        }
        if (GalleryUtils.isScreenRecorderExist() && (bucketId == MediaSetUtils.getScreenshotsBucketID() || GalleryStorageManager.getInstance().isOuterGalleryStorageScreenshotsBucketID(bucketId))) {
            return MediaSetUtils.getScreenshotsAlbumStringId();
        }
        return MediaSetUtils.bucketId2ResourceId(bucketId, context);
    }

    private void setDefaultBackground() {
        Bitmap bitmap = null;
        Drawable drawable = getResources().getDrawable(R.drawable.gallery_widget_bg);
        try {
            if (this.mBitmap != null) {
                bitmap = this.mBitmap;
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                canvas.setBitmap(null);
            }
        } catch (Exception ex) {
            GalleryLog.e("ResourceTexture", "failed to create the Bitmap " + ex.getMessage());
        }
        if (bitmap != null) {
            Bitmap cutBitmap = getCutBitmap(bitmap);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (cutBitmap != null) {
                Bitmap roundBitmap = null;
                try {
                    if (this.isAlbumCoverNeedRadius) {
                        roundBitmap = CutBitmapUtils.getCornerBitmap(cutBitmap, getResources(), R.drawable.widget_bg_template);
                    } else {
                        roundBitmap = cutBitmap;
                    }
                } catch (Throwable exp) {
                    GalleryLog.i(TAG, "failed to get corner bitmap." + exp.getMessage());
                }
                if (roundBitmap != null) {
                    setAlbumCoverLayoutParams();
                    if (roundBitmap == cutBitmap || cutBitmap.isRecycled()) {
                        setImageBitmap(cutBitmap);
                    } else {
                        setImageBitmap(roundBitmap);
                        cutBitmap.recycle();
                    }
                }
            }
        }
    }

    public String getBucketId() {
        return this.mBucketId;
    }

    public int getUnitId() {
        return this.mWidgetId;
    }

    public void onPhotoChange(int photoCount) {
        if (!(this.mPhotoCount == 0 || photoCount != 0 || this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        synchronized (WidgetPhotoManager.getInstance()) {
            this.mPhotoCount = photoCount;
        }
        if (this.mPhotoPosition >= photoCount) {
            this.mPhotoPosition = 0;
        }
        if (this.mPhotoCount == 0) {
            this.mNoPhotos = true;
            onBitmapChange(this.mBitmap, this.mUri, this.mMimetype, this.mNoPhotos, this.mPhotoId, false);
            WidgetUtils.deletePng(this.mWidgetId);
            return;
        }
        this.mNoPhotos = false;
        this.mPhotoInfo = WidgetPhotoManager.getInstance().getPhotoInfo(this.mBucketId, this.mWidgetId, this.mPhotoPosition);
        this.mThreadHandler.removeMessages(1);
        this.mIsFirstUpdate = true;
        this.mThreadHandler.sendEmptyMessageDelayed(1, 200);
    }

    private void setAlbumNameLayoutParams() {
        if (this.mTextView != null) {
            LayoutParams layoutParams = (LayoutParams) this.mTextView.getLayoutParams();
            layoutParams.setMargins(this.mAlbumNameMarginLeft, this.mAlbumNameMarginBottom, this.mAlbumNameMarginLeft, this.mAlbumNameMarginBottom);
            this.mTextView.setLayoutParams(layoutParams);
        }
    }

    private void setAlbumCoverLayoutParams() {
        if (this.mPhotoView != null) {
            LayoutParams layoutParams = (LayoutParams) this.mPhotoView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.setMargins(this.mAlbumCoverMarginLeft, this.mAlbumCoverMarginTop, this.mAlbumCoverMarginLeft, this.mAlbumCoverMarginTop);
            this.mPhotoView.setLayoutParams(layoutParams);
        }
    }

    private void setAlbumCoverParams() {
        boolean z = true;
        AttributeEntry albumCoverEntrie = WidgetPhotoManager.getInstance().getAlbumCoverAttributeEntries(getContext());
        if (albumCoverEntrie != null) {
            this.mAlbumCoverMarginLeft = (int) (albumCoverEntrie.getMarginLeft() * ((double) this.mViewWidth));
            this.mAlbumCoverMarginTop = (int) (albumCoverEntrie.getMarginBottom() * ((double) this.mViewHeight));
            if (albumCoverEntrie.getmFlag() != 1) {
                z = false;
            }
            this.isAlbumCoverNeedRadius = z;
            if (this.mViewWidth != 0 && this.mViewHeight != 0) {
                this.mAlbumCoverWidth = (int) (albumCoverEntrie.getmWidth() * ((double) this.mViewWidth));
                this.mAlbumCoverHeight = (int) (albumCoverEntrie.getmHeight() * ((double) this.mViewHeight));
            }
        }
    }

    private void setAlbumNameParams() {
        AttributeEntry albumNameEntrie = WidgetPhotoManager.getInstance().getAlbumNameAttributeEntries(getContext());
        if (albumNameEntrie != null) {
            this.mAlbumNameMarginLeft = (int) (albumNameEntrie.getMarginLeft() * ((double) this.mViewWidth));
            this.mAlbumNameMarginBottom = (int) (albumNameEntrie.getMarginBottom() * ((double) this.mViewHeight));
            if (this.mAlbumNameMarginLeft == 0 || !(this.mAlbumNameMarginBottom != 0 || this.mDefaultViewWidth == 0 || this.mDefaultViewHeight == 0)) {
                this.mAlbumNameMarginLeft = (int) (albumNameEntrie.getMarginLeft() * ((double) this.mDefaultViewWidth));
                this.mAlbumNameMarginBottom = (int) (albumNameEntrie.getMarginBottom() * ((double) this.mDefaultViewHeight));
            }
        }
        this.mAlbumNameMarginBottom = Math.min(this.mAlbumNameMarginBottom, this.mBottomMarginLimit);
    }

    private void setWidgetParams() {
        setAlbumCoverParams();
        setAlbumNameParams();
    }

    private void initView() {
        this.mViewWidth = getContext().getResources().getInteger(R.integer.widgetunit_view_width);
        this.mViewHeight = getContext().getResources().getInteger(R.integer.widgetunit_view_height_1);
        this.mDefaultViewWidth = this.mViewWidth;
        this.mDefaultViewHeight = this.mViewHeight;
    }

    private boolean checkLauncherHasStoragePermission(Context context) {
        if (context.getPackageManager().checkPermission("android.permission.READ_EXTERNAL_STORAGE", LAUNCHER_PACKAGE_NAME) == 0) {
            return true;
        }
        return false;
    }

    private void requestLauncherStoragePermission(Intent targetIntent) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REQUEST_PERMISSION, new String[]{"android.permission.READ_EXTERNAL_STORAGE"});
        intent.putExtra(EXTRA_TARGET_INTENT, targetIntent);
        intent.setComponent(new ComponentName(LAUNCHER_PACKAGE_NAME, LAUNCHER_REQUEST_PERMISSION_ACTIVITY_NAME));
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            GalleryLog.i(TAG, "Catch an exception in requestLauncherWriteExternalStoragePermission() method.");
        }
    }
}
