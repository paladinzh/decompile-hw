package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.mms.attachment.ui.PagerViewHolder;
import com.android.mms.attachment.utils.ImageUtils;
import com.android.mms.attachment.utils.ThreadUtil;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;

public class GalleryCompressItemView implements PagerViewHolder {
    private boolean isCursorValid = true;
    private String mContentType;
    private Context mContext;
    private String mFileSize = null;
    private LoadThread mLoadThread = null;
    private long mMediaId = -1;
    private Bitmap mPictureBitmap;
    private String mPictureSource;
    private ImageView mPictureView;
    private View mRootView;
    private View mVideoPlayIcon;

    private class LoadThread extends Thread {
        private String contentType;
        private String sourcePath;

        public LoadThread(String sourcepath, String contenttype) {
            this.sourcePath = sourcepath;
            this.contentType = contenttype;
        }

        public void run() {
            initImageView(this.sourcePath, this.contentType);
        }

        private void initImageView(String filePath, final String type) {
            if (!TextUtils.isEmpty(filePath) && !TextUtils.isEmpty(type)) {
                try {
                    if (type.startsWith("image")) {
                        Options options = new Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(filePath, options);
                        options.inSampleSize = ImageUtils.get().calculateInSampleSize(options, 480, 270);
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Config.ARGB_4444;
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        GalleryCompressItemView.this.mPictureBitmap = BitmapFactory.decodeFile(filePath, options);
                    } else {
                        GalleryCompressItemView.this.mPictureBitmap = ThumbnailUtils.createVideoThumbnail(filePath, 0);
                    }
                    GalleryCompressItemView.this.mFileSize = Formatter.formatFileSize(GalleryCompressItemView.this.mContext, new File(filePath).length());
                    if (GalleryCompressItemView.this.mPictureBitmap != null) {
                        GalleryCompressItemView.this.mPictureBitmap = RcsUtility.fixRotateBitmap(filePath, GalleryCompressItemView.this.mPictureBitmap);
                        ThreadUtil.getMainThreadHandler().post(new Runnable() {
                            public void run() {
                                if (type.startsWith("video")) {
                                    GalleryCompressItemView.this.mVideoPlayIcon.setVisibility(0);
                                } else {
                                    GalleryCompressItemView.this.mVideoPlayIcon.setVisibility(8);
                                }
                                if (GalleryCompressItemView.this.mPictureView != null) {
                                    GalleryCompressItemView.this.mPictureView.setImageBitmap(GalleryCompressItemView.this.mPictureBitmap);
                                } else if (GalleryCompressItemView.this.mPictureBitmap != null && !GalleryCompressItemView.this.mPictureBitmap.isRecycled()) {
                                    GalleryCompressItemView.this.mPictureBitmap.recycle();
                                }
                            }
                        });
                    }
                } catch (OutOfMemoryError e) {
                    try {
                        Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception e2) {
                    MLog.e("GalleryCompressItemView", "Exception:" + e2.getMessage());
                }
            }
        }
    }

    public GalleryCompressItemView(Context context) {
        this.mContext = context;
    }

    public View getView(ViewGroup container) {
        this.mRootView = LayoutInflater.from(this.mContext).inflate(R.layout.gallery_compress_item_layout, container, false);
        this.mPictureView = (ImageView) this.mRootView.findViewById(R.id.gallery_compress_imageview);
        this.mVideoPlayIcon = this.mRootView.findViewById(R.id.gallery_video_play);
        this.mVideoPlayIcon.setVisibility(8);
        this.mVideoPlayIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (TextUtils.isEmpty(GalleryCompressItemView.this.mPictureSource) || TextUtils.isEmpty(GalleryCompressItemView.this.mContentType) || GalleryCompressItemView.this.mMediaId < 0) {
                    MLog.e("GalleryCompressItemView", "viewVideo failed, params is error.");
                } else if (GalleryCompressItemView.this.mContentType.startsWith("video")) {
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addFlags(1);
                    intent.setDataAndType(Uri.parse("content://media/external/video/media/" + GalleryCompressItemView.this.mMediaId), GalleryCompressItemView.this.mContentType);
                    try {
                        GalleryCompressItemView.this.mContext.startActivity(intent);
                    } catch (Exception e) {
                        MLog.e("GalleryCompressItemView", "Unsupported Format,startActivity(intent) error,intent");
                        MessageUtils.showErrorDialog(GalleryCompressItemView.this.mContext, GalleryCompressItemView.this.mContext.getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
                        e.printStackTrace();
                    }
                }
            }
        });
        return this.mRootView;
    }

    public View getView(ViewGroup container, Cursor cursor) {
        recycleGalleryBitmap();
        View view = getView(container);
        bind(cursor);
        return view;
    }

    public View getRootView() {
        return this.mRootView;
    }

    public void bind(Cursor cursor) {
        if (this.isCursorValid) {
            if (!(cursor == null || cursor.isClosed())) {
                this.mPictureSource = cursor.getString(1);
                this.mContentType = cursor.getString(4);
                this.mMediaId = !TextUtils.isEmpty(cursor.getString(0)) ? Long.parseLong(cursor.getString(0)) : -1;
                if (TextUtils.isEmpty(this.mPictureSource)) {
                    MLog.w("GalleryCompressItemView", "sourcePath is valid");
                } else {
                    this.mLoadThread = new LoadThread(this.mPictureSource, this.mContentType);
                    this.mLoadThread.start();
                }
            }
            return;
        }
        MLog.w("GalleryCompressItemView", "cursor is valid");
    }

    public String getSourcePath() {
        return this.mPictureSource;
    }

    public String getFileSize() {
        if (this.mFileSize == null) {
            this.mFileSize = Formatter.formatFileSize(this.mContext, new File(this.mPictureSource).length());
        }
        return this.mFileSize;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public void setCursorValid(boolean iscursorvalid) {
        this.isCursorValid = iscursorvalid;
    }

    public void recycleGalleryBitmap() {
        if (this.mPictureBitmap != null && !this.mPictureBitmap.isRecycled()) {
            this.mPictureBitmap.recycle();
            this.mPictureBitmap = null;
        }
    }

    public View destroyView() {
        if (this.mLoadThread != null && this.mLoadThread.isAlive()) {
            this.mLoadThread.interrupt();
        }
        recycleGalleryBitmap();
        this.mPictureView = null;
        this.mRootView = null;
        this.mContext = null;
        this.mLoadThread = null;
        return null;
    }

    public Parcelable saveState() {
        return null;
    }

    public void restoreState(Parcelable restoredState) {
    }
}
