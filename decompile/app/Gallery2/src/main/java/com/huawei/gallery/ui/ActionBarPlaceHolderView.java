package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;

public class ActionBarPlaceHolderView extends GLView {
    private BitmapTexture mActionBarBitmapTexture;
    private Listener mListener;
    private boolean mNeedNotifyFirstRenderAfterUpdateContent;

    public interface Listener {
        void onFirstRenderAfterUpdateActionBarPlaceHolderContent();
    }

    public ActionBarPlaceHolderView(Listener listener) {
        this.mListener = listener;
    }

    public void setContent(Bitmap content) {
        BitmapTexture bitmapTexture = this.mActionBarBitmapTexture;
        if (bitmapTexture != null) {
            bitmapTexture.recycle();
            Bitmap bitmap = bitmapTexture.getBitmap();
            if (bitmap != null) {
                bitmap.recycle();
            }
            this.mActionBarBitmapTexture = null;
        }
        if (content != null) {
            this.mActionBarBitmapTexture = new BitmapTexture(content);
            this.mNeedNotifyFirstRenderAfterUpdateContent = true;
        }
        invalidate();
    }

    protected void render(GLCanvas canvas) {
        BitmapTexture bitmapTexture = this.mActionBarBitmapTexture;
        if (bitmapTexture != null) {
            bitmapTexture.draw(canvas, 0, 0);
            if (this.mNeedNotifyFirstRenderAfterUpdateContent) {
                if (this.mListener != null) {
                    this.mListener.onFirstRenderAfterUpdateActionBarPlaceHolderContent();
                }
                this.mNeedNotifyFirstRenderAfterUpdateContent = false;
            }
        }
    }
}
