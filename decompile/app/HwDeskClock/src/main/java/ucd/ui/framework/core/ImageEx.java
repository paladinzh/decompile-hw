package ucd.ui.framework.core;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLBaseSettings.Env;
import ucd.ui.framework.Settings.GLObjectSettings;
import ucd.ui.framework.Settings.GLObjectSettings.ImageExConfig.ScaleType;
import ucd.ui.framework.Settings.GLObjectSettings.ViewConfig.GLObjectType;
import ucd.ui.framework.lib.GL;
import ucd.ui.framework.lib.Model;
import ucd.ui.util.BitmapUtil;
import ucd.ui.util.Downloader;
import ucd.ui.util.Downloader.Callback;
import ucd.ui.util.GenerateColor;
import ucd.ui.util.GenerateColor.OnGenerateColorListener;

public class ImageEx extends GLObject {
    protected int imgHeight;
    protected int imgWidth;
    private LoadRunnable loadRunnable = new LoadRunnable() {
        public void run() {
            ImageEx.this.settings.viewcfg.type = GLObjectType.Image;
            ImageEx.this.createTexture();
            synchronized (ImageEx.this) {
                ImageEx.this.bitmap2Texture(this.bit2, ImageEx.this.texObj);
            }
            if (!(this.bit2 == null || this.bit2.equals(this.bit1))) {
                this.bit2.recycle();
            }
            this.bit1 = null;
            this.bit2 = null;
            ImageEx.this.setDirty();
            ImageEx.this.requestRender();
        }
    };
    private boolean needToUpdateUV = true;
    private OnGenerateColorListener onGenerateColorListener;
    protected int texObj = 0;
    protected int texTarget = 3553;
    protected String value;

    private static class LoadRunnable implements Runnable {
        protected Bitmap bit1;
        protected Bitmap bit2;

        private LoadRunnable() {
        }

        public void run() {
        }
    }

    public ImageEx(GLBase root, int w, int h) {
        super(root, w, h);
    }

    protected void onInit(GLBaseSettings info) {
        int[] buffers = new int[1];
        GL.glGenBuffers(1, buffers, 0);
        this.texBufferPos = buffers[0];
        super.onInit(info);
    }

    protected void getHandles(int shader) {
        this.settings.imageExCfg.samplerUniform = GL.glGetUniformLocation(shader, "uSampler");
        this.settings.imageExCfg.textureCoordAttribute = GL.glGetAttribLocation(shader, "aTextureCoord");
        super.getHandles(shader);
    }

    protected void UVtoGL() {
        if (this.settings.imageExCfg.textureCoords == null || this.settings.imageExCfg.textureCoords.length == 0) {
            this.settings.imageExCfg.numItems = Env.getDefaultTexCoords().length / this.settings.imageExCfg.itemSize;
            Spirit.setBuffer(this.root.settings.env.defaultTexBufferPos, this.settings.imageExCfg.textureCoordAttribute, this.settings.imageExCfg.itemSize);
            return;
        }
        this.settings.imageExCfg.numItems = this.settings.imageExCfg.textureCoords.length / this.settings.imageExCfg.itemSize;
        Spirit.setBuffer(this.texBufferPos, this.settings.imageExCfg.textureCoordAttribute, this.settings.imageExCfg.itemSize, this.settings.imageExCfg.textureCoords);
    }

    protected void createTextureCoords(float l, float t, float w, float h) {
        if (w != 1.0f || h != 1.0f) {
            this.settings.imageExCfg.textureCoords = Model.planeTModel(0.0f, 0.0f, w, h);
        }
    }

    protected boolean createTexture() {
        if (this.texObj != 0) {
            return false;
        }
        this.texObj = this.root.settings.env.texManager.createTex();
        return true;
    }

    protected void updateData(GLObjectSettings settings, GLBaseSettings info) {
        super.updateData(settings, info);
        if (this.needToUpdateUV) {
            float w = 1.0f;
            float h = 1.0f;
            if (!(this.imgWidth == 0 || this.imgHeight == 0)) {
                if (settings.imageExCfg.scaleType == ScaleType.repeatX) {
                    w = (((float) getWidth()) * 1.0f) / ((float) this.imgWidth);
                } else if (settings.imageExCfg.scaleType == ScaleType.repeatY) {
                    h = (((float) getHeight()) * 1.0f) / ((float) this.imgHeight);
                } else if (settings.imageExCfg.scaleType == ScaleType.repeatXY) {
                    w = (((float) getWidth()) * 1.0f) / ((float) this.imgWidth);
                    h = (((float) getHeight()) * 1.0f) / ((float) this.imgHeight);
                }
            }
            createTextureCoords(0.0f, 0.0f, w, h);
            UVtoGL();
            this.needToUpdateUV = false;
        }
        createTexture();
        updateTexture();
        GL.glTexParameterf(this.texTarget, 10241, 9729);
        GL.glTexParameterf(this.texTarget, 10240, 9729);
        setScaleType();
    }

    protected final void bitmap2Texture(Bitmap bit, int texId) {
        if (bit != null && !bit.isRecycled()) {
            GL.glBindTexture(this.texTarget, texId);
            try {
                GLUtils.texImage2D(this.texTarget, 0, bit, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Bitmap onBitmapLoaded(Bitmap src, Bitmap scaled) {
        return scaled;
    }

    protected Bitmap createFitBitmap(Bitmap src) {
        return BitmapUtil.createFitBitmap(src, getMeasuredWidth(), getMeasuredHeight());
    }

    private void onload(String url, Bitmap bit, Callback cb) {
        this.value = url;
        if (bit == null || bit.isRecycled()) {
            System.out.println(this.TAG + ", bitmap onloaded is recycled.");
            return;
        }
        if (this.onGenerateColorListener != null) {
            GenerateColor.generate(bit, this.onGenerateColorListener);
        }
        Bitmap bitmap = createFitBitmap(bit);
        Bitmap finalBitmap = onBitmapLoaded(bit, bitmap);
        if (!(finalBitmap.equals(bitmap) || bitmap.equals(bit))) {
            bitmap.recycle();
        }
        bitmap = finalBitmap;
        this.imgWidth = bitmap.getWidth();
        this.imgHeight = bitmap.getHeight();
        this.loadRunnable.bit1 = bit;
        this.loadRunnable.bit2 = bitmap;
        addOnceAction(this.loadRunnable);
        setDirty();
        requestRender();
    }

    public void load(String url, final Callback cb) {
        Downloader.loadImg(this.root.getContext(), url, new Callback() {
            public void onload(Bitmap bit, String url) {
                ImageEx.this.onload(url, bit, cb);
            }
        });
    }

    protected void updateTexture() {
    }

    private void setScaleType() {
        if (this.texTarget != 36197) {
            int tx = 33071;
            int ty = 33071;
            if (this.settings.imageExCfg.scaleType == ScaleType.repeatX) {
                tx = 10497;
            } else if (this.settings.imageExCfg.scaleType == ScaleType.repeatY) {
                ty = 10497;
            } else if (this.settings.imageExCfg.scaleType == ScaleType.repeatXY) {
                ty = 10497;
                tx = 10497;
            }
            GL.glTexParameteri(this.texTarget, 10242, tx);
            GL.glTexParameteri(this.texTarget, 10243, ty);
            return;
        }
        GL.glTexParameterf(this.texTarget, 10242, 33071);
        GL.glTexParameterf(this.texTarget, 10243, 33071);
    }

    public void setOnGenerateColorListener(OnGenerateColorListener l) {
        this.onGenerateColorListener = l;
    }

    public void onDestroy() {
        if (this.texObj != 0) {
            queueEvent(new Runnable() {
                public void run() {
                    ImageEx.this.info.env.texManager.recycleTex(ImageEx.this.texObj);
                }
            });
        }
        super.onDestroy();
    }
}
