package com.android.gallery3d.ui;

import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import java.util.ArrayDeque;

public class TextureUploader implements OnGLIdleListener {
    private final ArrayDeque<UploadedTexture> mBgTextures = new ArrayDeque(64);
    private final ArrayDeque<UploadedTexture> mFgTextures = new ArrayDeque(64);
    private final GLRoot mGLRoot;
    private volatile boolean mIsQueued = false;

    public TextureUploader(GLRoot root) {
        this.mGLRoot = root;
    }

    public synchronized void clear() {
        while (!this.mFgTextures.isEmpty()) {
            ((UploadedTexture) this.mFgTextures.pop()).setIsUploading(false);
        }
        while (!this.mBgTextures.isEmpty()) {
            ((UploadedTexture) this.mBgTextures.pop()).setIsUploading(false);
        }
    }

    private void queueSelfIfNeed() {
        if (!this.mIsQueued) {
            this.mIsQueued = true;
            this.mGLRoot.addOnGLIdleListener(this);
        }
    }

    public synchronized void addBgTexture(UploadedTexture t) {
        if (!t.isContentValid()) {
            this.mBgTextures.addLast(t);
            t.setIsUploading(true);
            queueSelfIfNeed();
        }
    }

    public synchronized void addFgTexture(UploadedTexture t) {
        if (!t.isContentValid()) {
            this.mFgTextures.addLast(t);
            t.setIsUploading(true);
            queueSelfIfNeed();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int upload(GLCanvas canvas, ArrayDeque<UploadedTexture> deque, int uploadQuota, boolean isBackground) {
        while (uploadQuota > 0) {
            synchronized (this) {
                if (!deque.isEmpty()) {
                    UploadedTexture t = (UploadedTexture) deque.removeFirst();
                    if (t.isUploading()) {
                        t.setIsUploading(false);
                        if (!(t.isContentValid() || t.sourceBitmapInvalid())) {
                            t.updateContent(canvas);
                        }
                    }
                }
            }
        }
        return uploadQuota;
    }

    public boolean onGLIdle(GLCanvas canvas, boolean renderRequested) {
        boolean z = true;
        int uploadQuota = upload(canvas, this.mFgTextures, 1, false);
        if (uploadQuota < 1) {
            this.mGLRoot.requestRender();
        }
        upload(canvas, this.mBgTextures, uploadQuota, true);
        synchronized (this) {
            if (this.mFgTextures.isEmpty() && this.mBgTextures.isEmpty()) {
                z = false;
            }
            this.mIsQueued = z;
            z = this.mIsQueued;
        }
        return z;
    }
}
