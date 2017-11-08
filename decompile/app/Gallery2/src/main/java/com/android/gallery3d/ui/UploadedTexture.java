package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import java.util.HashMap;
import javax.microedition.khronos.opengles.GL11;

public abstract class UploadedTexture extends BasicTexture {
    private static final Object BORDER_LOCK = new Object();
    private static BorderKey sBorderKey = new BorderKey();
    private static HashMap<BorderKey, Bitmap> sBorderLines = new HashMap();
    protected Bitmap mBitmap;
    private int mBorder;
    private boolean mContentValid;
    private boolean mIsUploading;
    private boolean mOpaque;

    private static class BorderKey implements Cloneable {
        public Config config;
        public int length;
        public boolean vertical;

        private BorderKey() {
        }

        public int hashCode() {
            int x = this.config.hashCode() ^ this.length;
            return this.vertical ? x : -x;
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (!(object instanceof BorderKey)) {
                return false;
            }
            BorderKey o = (BorderKey) object;
            if (this.vertical == o.vertical && this.config == o.config && this.length == o.length) {
                z = true;
            }
            return z;
        }

        public BorderKey clone() {
            try {
                return (BorderKey) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    protected abstract void onFreeBitmap(Bitmap bitmap);

    protected abstract Bitmap onGetBitmap();

    protected UploadedTexture() {
        this(false);
    }

    protected UploadedTexture(boolean hasBorder) {
        super(null, 0, 0);
        this.mContentValid = true;
        this.mIsUploading = false;
        this.mOpaque = true;
        if (hasBorder) {
            setBorder(true);
            this.mBorder = 1;
        }
    }

    protected void setIsUploading(boolean uploading) {
        this.mIsUploading = uploading;
    }

    public void onSetIsUploading(boolean uploading) {
        this.mIsUploading = uploading;
    }

    public boolean isUploading() {
        return this.mIsUploading;
    }

    private static Bitmap getBorderLine(boolean vertical, Config config, int length) {
        Bitmap bitmap;
        synchronized (BORDER_LOCK) {
            BorderKey key = sBorderKey;
            key.vertical = vertical;
            key.config = config;
            key.length = length;
            bitmap = (Bitmap) sBorderLines.get(key);
            if (bitmap == null) {
                if (vertical) {
                    bitmap = Bitmap.createBitmap(1, length, config);
                } else {
                    bitmap = Bitmap.createBitmap(length, 1, config);
                }
                sBorderLines.put(key.clone(), bitmap);
            }
        }
        return bitmap;
    }

    private Bitmap getBitmap() {
        if (this.mBitmap == null) {
            this.mBitmap = onGetBitmap();
            int w = this.mBitmap.getWidth() + (this.mBorder * 2);
            int h = this.mBitmap.getHeight() + (this.mBorder * 2);
            if (this.mWidth == -1) {
                setSize(w, h);
            }
        }
        return this.mBitmap;
    }

    private void freeBitmap() {
        Utils.assertTrue(this.mBitmap != null);
        onFreeBitmap(this.mBitmap);
        this.mBitmap = null;
    }

    public int getWidth() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return super.getWidth();
    }

    public int getHeight() {
        if (this.mWidth == -1) {
            getBitmap();
        }
        return super.getHeight();
    }

    protected void invalidateContent() {
        if (this.mBitmap != null) {
            freeBitmap();
        }
        this.mContentValid = false;
        this.mWidth = -1;
        this.mHeight = -1;
    }

    public boolean isContentValid() {
        return isLoaded() ? this.mContentValid : false;
    }

    public void updateContent(GLCanvas canvas) {
        TraceController.traceBegin("UploadedTexture.updateContent");
        if (!isLoaded()) {
            uploadToCanvas(canvas);
        } else if (!this.mContentValid) {
            Bitmap bitmap = getBitmap();
            int format = GLUtils.getInternalFormat(bitmap);
            int type = GLUtils.getType(bitmap);
            canvas.getGLInstance().glBindTexture(3553, this.mId);
            GLUtils.texSubImage2D(3553, 0, this.mBorder, this.mBorder, bitmap, format, type);
            freeBitmap();
            this.mContentValid = true;
        }
        TraceController.traceEnd();
    }

    private void uploadToCanvas(GLCanvas canvas) {
        GL11 gl = canvas.getGLInstance();
        Bitmap bitmap = getBitmap();
        TraceController.traceBegin("UploadedTexture.uploadToCanvas bitmap=" + bitmap);
        int[] textureId = canvas.getTextureId();
        if (bitmap != null) {
            try {
                int bWidth = bitmap.getWidth();
                int bHeight = bitmap.getHeight();
                int texWidth = getTextureWidth();
                int texHeight = getTextureHeight();
                boolean z = bWidth <= texWidth && bHeight <= texHeight;
                Utils.assertTrue(z);
                float[] cropRect = canvas.getCropRect();
                cropRect[0] = (float) this.mBorder;
                cropRect[1] = (float) (this.mBorder + bHeight);
                cropRect[2] = (float) bWidth;
                cropRect[3] = (float) (-bHeight);
                GLId.glGenTextures(1, textureId, 0);
                gl.glBindTexture(3553, textureId[0]);
                gl.glTexParameterfv(3553, 35741, cropRect, 0);
                gl.glTexParameteri(3553, 10242, 33071);
                gl.glTexParameteri(3553, 10243, 33071);
                gl.glTexParameterf(3553, 10241, 9729.0f);
                gl.glTexParameterf(3553, 10240, 9729.0f);
                if (bWidth == texWidth && bHeight == texHeight) {
                    GLUtils.texImage2D(3553, 0, bitmap, 0);
                } else {
                    if (bitmap.isRecycled()) {
                        GalleryLog.w("Texture", String.format("bitmap(%s) is recycled[mWidth=%d, mHeight=%d]", new Object[]{bitmap, Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight())}));
                    }
                    int format = GLUtils.getInternalFormat(bitmap);
                    int type = GLUtils.getType(bitmap);
                    Config config = bitmap.getConfig();
                    gl.glTexImage2D(3553, 0, format, texWidth, texHeight, 0, format, type, null);
                    GLUtils.texSubImage2D(3553, 0, this.mBorder, this.mBorder, bitmap, format, type);
                    if (this.mBorder > 0) {
                        GLUtils.texSubImage2D(3553, 0, 0, 0, getBorderLine(true, config, texHeight), format, type);
                        GLUtils.texSubImage2D(3553, 0, 0, 0, getBorderLine(false, config, texWidth), format, type);
                    }
                    if (this.mBorder + bWidth < texWidth) {
                        GLUtils.texSubImage2D(3553, 0, this.mBorder + bWidth, 0, getBorderLine(true, config, texHeight), format, type);
                    }
                    if (this.mBorder + bHeight < texHeight) {
                        GLUtils.texSubImage2D(3553, 0, 0, this.mBorder + bHeight, getBorderLine(false, config, texWidth), format, type);
                    }
                }
                freeBitmap();
                setAssociatedCanvas(canvas);
                this.mId = textureId[0];
                this.mState = 1;
                this.mContentValid = true;
                TraceController.traceEnd();
            } catch (Throwable th) {
                freeBitmap();
            }
        } else {
            this.mState = -1;
            throw new RuntimeException("Texture load fail, no bitmap");
        }
    }

    protected boolean onBind(GLCanvas canvas) {
        TraceController.traceBegin("UploadedTexture.onBind");
        updateContent(canvas);
        TraceController.traceEnd();
        return isContentValid();
    }

    protected int getTarget() {
        return 3553;
    }

    public void setOpaque(boolean isOpaque) {
        this.mOpaque = isOpaque;
    }

    public boolean isOpaque() {
        return this.mOpaque;
    }

    public void recycle() {
        super.recycle();
        if (this.mBitmap != null) {
            freeBitmap();
        }
    }

    public boolean sourceBitmapInvalid() {
        return false;
    }

    protected void setSize(Bitmap bitmap) {
        if (bitmap != null) {
            int w = bitmap.getWidth() + (this.mBorder * 2);
            int h = bitmap.getHeight() + (this.mBorder * 2);
            GalleryLog.d("Texture", "setTextureSize, w = " + w + ", h = " + h);
            super.setSize(w, h);
        }
    }
}
