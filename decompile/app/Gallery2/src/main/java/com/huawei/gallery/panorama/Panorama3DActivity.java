package com.huawei.gallery.panorama;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.view.FyuseView;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.util.GalleryPool;
import java.io.File;

public class Panorama3DActivity extends Activity {
    private FyuseView fv;
    private String mFyuseFilePath = "";
    private String mFyuseMediaItemPathInString = "";
    private boolean mIsLoadProgressHidden = false;
    private boolean mIsProcessedFyuseFile = true;
    private ProgressBar mLoadProgress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(3846);
        setContentView(R.layout.activity_fullscreen_local);
        setWindowParams();
        this.fv = (FyuseView) findViewById(R.id.fyuse_view);
        this.mLoadProgress = (ProgressBar) findViewById(R.id.load_progress);
        this.fv.setRotateWithGravity(false);
        getIntentData();
    }

    public void getIntentData() {
        Intent intent = getIntent();
        this.mFyuseFilePath = intent.getStringExtra("extra.fyuse.path");
        this.mFyuseMediaItemPathInString = intent.getStringExtra("media-item-path");
        if (TextUtils.isEmpty(this.mFyuseFilePath)) {
            GalleryLog.d("Panorama3DActivity", "mFyuseFilePath == null");
            finish();
            return;
        }
        File lastDir = new File(this.mFyuseFilePath);
        this.mIsProcessedFyuseFile = FyuseFile.isProcessedFyuseFile(lastDir);
        loadLocalData(lastDir, this.fv);
    }

    protected void loadLocalData(File fyuseDir, FyuseView target) {
        this.mIsLoadProgressHidden = false;
        FyuseViewer.with((Activity) this).load(fyuseDir).listener(new RequestListener() {
            public boolean onLoadFailed(@Nullable FyuseException e, Object model) {
                GalleryLog.e("Panorama3DActivity", "Panorama3DActivity loadLocalData fail = " + e.toString());
                Panorama3DActivity.this.hideProgress();
                return true;
            }

            public boolean onResourceReady(Object model) {
                GalleryLog.d("Panorama3DActivity", "loadLocalData onResourceReady");
                Panorama3DActivity.this.hideProgress();
                if (!Panorama3DActivity.this.mIsProcessedFyuseFile) {
                    GalleryLog.d("Panorama3DActivity", "deleteCachedThumbnails");
                    new Thread(new Runnable() {
                        public void run() {
                            Panorama3DActivity.this.deleteCachedThumbnails();
                        }
                    }).start();
                }
                return false;
            }

            public void onProgress(int progress) {
                GalleryLog.d("Panorama3DActivity", "loadLocalData progress == " + progress);
                Panorama3DActivity.this.hideProgress();
            }
        }).highRes(true).into(target);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideProgress() {
        if (this.mLoadProgress != null && !this.mIsLoadProgressHidden) {
            this.mIsLoadProgressHidden = true;
            runOnUiThread(new Runnable() {
                public void run() {
                    Panorama3DActivity.this.mLoadProgress.setVisibility(8);
                }
            });
        }
    }

    private void setWindowParams() {
        Window win = getWindow();
        LayoutParams params = win.getAttributes();
        params.flags |= 134217728;
        win.setAttributes(params);
    }

    private void deleteCachedThumbnails() {
        if (TextUtils.isEmpty(this.mFyuseMediaItemPathInString)) {
            GalleryLog.d("Panorama3DActivity", "mFyuseMediaItemPathInString == null");
        } else if (new File(this.mFyuseFilePath).exists()) {
            GalleryAppImpl galleryApp = (GalleryAppImpl) getApplication();
            Path mediaItemPath = Path.fromString(this.mFyuseMediaItemPathInString);
            MediaObject mediaObject = galleryApp.getDataManager().getMediaObject(this.mFyuseMediaItemPathInString);
            if (mediaObject == null) {
                GalleryLog.d("Panorama3DActivity", "dataManager.getMediaObject == null");
                return;
            }
            MediaItem mediaItem = (MediaItem) mediaObject;
            ImageCacheService cacheService = galleryApp.getImageCacheService();
            if (cacheService != null) {
                cacheService.removeImageData(mediaItemPath, mediaItem.getDateModifiedInSec(), 2);
                cacheService.removeImageData(mediaItemPath, mediaItem.getDateModifiedInSec(), 1);
                cacheService.removeImageData(mediaItemPath, mediaItem.getDateModifiedInSec(), 8);
            }
            GalleryPool.remove(mediaItemPath, mediaItem.getDateModifiedInSec());
        }
    }
}
