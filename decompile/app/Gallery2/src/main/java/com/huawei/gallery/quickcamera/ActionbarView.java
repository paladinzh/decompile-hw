package com.huawei.gallery.quickcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;

public class ActionbarView extends GLView {
    private final Activity mActivity;
    private ColorTexture mBackground = new ColorTexture(0);
    private boolean mFirstUpdateTexture = true;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (msg.obj != null && ((Boolean) msg.obj).booleanValue()) {
                        ActionbarView.this.invalidateTexture();
                    }
                    ActionbarView.this.updateTexture();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private BitmapTexture mTexture = null;
    private boolean mTextureValid = false;

    public ActionbarView(Activity activity) {
        this.mActivity = activity;
    }

    protected void renderBackground(GLCanvas canvas) {
        super.renderBackground(canvas);
        this.mBackground.draw(canvas, 0, 0, getWidth(), getHeight());
    }

    protected void render(GLCanvas canvas) {
        super.render(canvas);
        if (!this.mTextureValid) {
            freeBitmapTexture();
            requestUpdateTexture();
        } else if (this.mTexture != null) {
            this.mTexture.draw(canvas, 0, 0, this.mTexture.getWidth(), this.mTexture.getHeight());
        } else {
            requestUpdateTexture();
        }
    }

    private void requestUpdateTexture() {
        if (!this.mHandler.hasMessages(1)) {
            int i;
            Handler handler = this.mHandler;
            Message obtainMessage = this.mHandler.obtainMessage(1, Boolean.valueOf(false));
            if (this.mFirstUpdateTexture) {
                i = 500;
            } else {
                i = 0;
            }
            handler.sendMessageDelayed(obtainMessage, (long) i);
            this.mFirstUpdateTexture = false;
        }
    }

    private View getHeadActionBar() {
        return this.mActivity.getWindow().getDecorView().findViewById(16909290);
    }

    public void show() {
        if (getHeadActionBar() != null) {
            getHeadActionBar().setVisibility(4);
        }
    }

    public void hide() {
        if (getHeadActionBar() != null) {
            getHeadActionBar().setVisibility(0);
        }
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
        if (changeSize) {
            invalidateTexture();
        }
    }

    public void refreshTexture() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, Boolean.valueOf(true)), 400);
        this.mFirstUpdateTexture = false;
    }

    private void invalidateTexture() {
        this.mTextureValid = false;
    }

    private void updateTexture() {
        GLRoot root = getGLRoot();
        View headActionBarView = getHeadActionBar();
        if (headActionBarView != null) {
            headActionBarView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(headActionBarView.getDrawingCache());
            headActionBarView.setDrawingCacheEnabled(false);
            if (root != null) {
                try {
                    root.lockRenderThread();
                } catch (Throwable th) {
                    if (root != null) {
                        root.unlockRenderThread();
                    }
                }
            }
            freeBitmapTexture();
            this.mTexture = new BitmapTexture(bitmap);
            this.mTextureValid = true;
            if (root != null) {
                root.unlockRenderThread();
            }
            invalidate();
        }
    }

    private void freeBitmapTexture() {
        if (this.mTexture != null) {
            this.mTexture.recycle();
            this.mTexture = null;
        }
    }
}
