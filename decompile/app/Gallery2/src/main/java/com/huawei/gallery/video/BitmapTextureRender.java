package com.huawei.gallery.video;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import junit.framework.Assert;

class BitmapTextureRender {
    private static BorderKey sBorderKey = new BorderKey();
    private static HashMap<BorderKey, Bitmap> sBorderLines = new HashMap();
    private int mBorder = 1;
    private float[] mMVPMatrix = new float[16];
    private int mProgram;
    private float[] mSTMatrix = new float[16];
    private Bitmap mTexture;
    private int mTextureHeight;
    private int mTextureID = -123456;
    private int mTextureWidth;
    private FloatBuffer mTriangleVertices;
    private int maPositionHandle;
    private int maTextureHandle;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int muSamplerHandle;

    private static class BorderKey implements Cloneable {
        public Config config;
        public boolean isVertical;
        public int length;

        private BorderKey() {
        }

        public int hashCode() {
            int x = this.config.hashCode() ^ this.length;
            return this.isVertical ? x : -x;
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (!(object instanceof BorderKey)) {
                return false;
            }
            BorderKey o = (BorderKey) object;
            if (this.isVertical == o.isVertical && this.config == o.config && this.length == o.length) {
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

    public BitmapTextureRender(Bitmap bitmap, int width, int height) {
        Assert.assertTrue(bitmap != null);
        this.mTexture = bitmap;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        this.mTextureWidth = Utils.nextPowerOf2(bitmapWidth);
        this.mTextureHeight = Utils.nextPowerOf2(bitmapHeight);
        float btmW = WMElement.CAMERASIZEVALUE1B1;
        float btmH = WMElement.CAMERASIZEVALUE1B1;
        float sw = ((float) bitmapWidth) / ((float) width);
        float sh = ((float) bitmapHeight) / ((float) height);
        if (sw > sh) {
            btmH = sh / sw;
        } else {
            btmW = sw / sh;
        }
        float texW = ((float) bitmapWidth) / ((float) this.mTextureWidth);
        float texH = ((float) bitmapHeight) / ((float) this.mTextureHeight);
        float[] triangleVerticesData = new float[]{-btmW, -btmH, 0.0f, 0.0f, texH, btmW, -btmH, 0.0f, texW, texH, -btmW, btmH, 0.0f, 0.0f, 0.0f, btmW, btmH, 0.0f, texW, 0.0f};
        GalleryLog.d("TextureRender", String.format("bitmap(w=%s,h=%s) texture(w=%s,h=%s)", new Object[]{Integer.valueOf(bitmapWidth), Integer.valueOf(bitmapHeight), Integer.valueOf(this.mTextureWidth), Integer.valueOf(this.mTextureHeight)}));
        this.mTriangleVertices = ByteBuffer.allocateDirect(triangleVerticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTriangleVertices.put(triangleVerticesData).position(0);
        Matrix.setIdentityM(this.mSTMatrix, 0);
    }

    public void uploadTexture() {
        GLES20.glUseProgram(this.mProgram);
        checkGlError("glUseProgram");
        GLES20.glActiveTexture(33984);
        checkGlError("glActiveTexture");
        uploadToCanvas();
        bindTexture();
    }

    private void uploadToCanvas() {
        Bitmap bitmap = this.mTexture;
        if (bitmap != null) {
            int bWidth = bitmap.getWidth();
            int bHeight = bitmap.getHeight();
            int texWidth = this.mTextureWidth;
            int texHeight = this.mTextureHeight;
            boolean z = bWidth <= texWidth && bHeight <= texHeight;
            Assert.assertTrue(z);
            setTextureParameters();
            if (bWidth == texWidth && bHeight == texHeight) {
                initializeTexture(bitmap);
                return;
            }
            int format = GLUtils.getInternalFormat(bitmap);
            int type = GLUtils.getType(bitmap);
            Config config = bitmap.getConfig();
            initializeTextureSize(format, type);
            texSubImage2D(this.mBorder, this.mBorder, bitmap, format, type);
            if (this.mBorder > 0) {
                texSubImage2D(0, 0, getBorderLine(true, config, texHeight), format, type);
                texSubImage2D(0, 0, getBorderLine(false, config, texWidth), format, type);
            }
            if (this.mBorder + bWidth < texWidth) {
                texSubImage2D(this.mBorder + bWidth, 0, getBorderLine(true, config, texHeight), format, type);
            }
            if (this.mBorder + bHeight < texHeight) {
                texSubImage2D(0, this.mBorder + bHeight, getBorderLine(false, config, texWidth), format, type);
                return;
            }
            return;
        }
        throw new RuntimeException("Texture load fail, no bitmap");
    }

    public void setTextureParameters() {
        bindTexture();
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glTexParameterf(3553, 10241, 9729.0f);
        GLES20.glTexParameterf(3553, 10240, 9729.0f);
    }

    private void bindTexture() {
        GLES20.glBindTexture(3553, this.mTextureID);
        checkGlError("glBindTexture");
    }

    public void initializeTextureSize(int format, int type) {
        bindTexture();
        GLES20.glTexImage2D(3553, 0, format, this.mTextureWidth, this.mTextureHeight, 0, format, type, null);
    }

    public void initializeTexture(Bitmap bitmap) {
        bindTexture();
        GLUtils.texImage2D(3553, 0, bitmap, 0);
    }

    public void texSubImage2D(int xOffset, int yOffset, Bitmap bitmap, int format, int type) {
        bindTexture();
        GLUtils.texSubImage2D(3553, 0, xOffset, yOffset, bitmap, format, type);
    }

    private static Bitmap getBorderLine(boolean vertical, Config config, int length) {
        BorderKey key = sBorderKey;
        key.isVertical = vertical;
        key.config = config;
        key.length = length;
        Bitmap bitmap = (Bitmap) sBorderLines.get(key);
        if (bitmap == null) {
            bitmap = vertical ? Bitmap.createBitmap(1, length, config) : Bitmap.createBitmap(length, 1, config);
            sBorderLines.put(key.clone(), bitmap);
        }
        return bitmap;
    }

    public void drawFrame() {
        checkGlError("onDrawFrame start");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        GLES20.glClear(16640);
        GLES20.glUseProgram(this.mProgram);
        checkGlError("glUseProgram");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.mTextureID);
        this.mTriangleVertices.position(0);
        GLES20.glVertexAttribPointer(this.maPositionHandle, 3, 5126, false, 20, this.mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(this.maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        this.mTriangleVertices.position(3);
        GLES20.glVertexAttribPointer(this.maTextureHandle, 2, 5126, false, 20, this.mTriangleVertices);
        checkGlError("glVertexAttribPointer muTextureMatrixHandle");
        GLES20.glEnableVertexAttribArray(this.maTextureHandle);
        checkGlError("glEnableVertexAttribArray muTextureMatrixHandle");
        Matrix.setIdentityM(this.mMVPMatrix, 0);
        GLES20.glUniform1i(this.muSamplerHandle, 0);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muSTMatrixHandle, 1, false, this.mSTMatrix, 0);
        GLES20.glDrawArrays(5, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();
    }

    public void surfaceCreated() {
        this.mProgram = createProgram("uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n  gl_Position = uMVPMatrix * aPosition;\n  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n", "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n");
        if (this.mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        this.maPositionHandle = getAttributeLocation(this.mProgram, "aPosition");
        this.maTextureHandle = getAttributeLocation(this.mProgram, "aTextureCoord");
        this.muMVPMatrixHandle = getUniformLocation(this.mProgram, "uMVPMatrix");
        this.muSTMatrixHandle = getUniformLocation(this.mProgram, "uSTMatrix");
        this.muSamplerHandle = getUniformLocation(this.mProgram, "sTexture");
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        this.mTextureID = textures[0];
        bindTexture();
        GLES20.glTexParameterf(3553, 10241, 9728.0f);
        GLES20.glTexParameterf(3553, 10240, 9729.0f);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("glTexParameter");
    }

    private int getAttributeLocation(int program, String attribute) {
        int handle = GLES20.glGetAttribLocation(program, attribute);
        checkGlError("glGetAttribLocation " + attribute);
        if (handle != -1) {
            return handle;
        }
        throw new RuntimeException("Could not get attrib location for " + attribute);
    }

    private int getUniformLocation(int program, String uniform) {
        int handle = GLES20.glGetUniformLocation(program, uniform);
        checkGlError("glGetUniformLocation " + uniform);
        if (handle != -1) {
            return handle;
        }
        throw new RuntimeException("Could not get uniform location for " + uniform);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("BitmapTextureRender(2.0) creat video glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        GalleryLog.e("TextureRender", "BitmapTextureRender(2.0)  Could not compile shader " + shaderType + ":");
        GalleryLog.e("TextureRender", "BitmapTextureRender(2.0) " + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        if (vertexShader == 0) {
            GalleryLog.e("TextureRender", "BitmapTextureRender(2.0)  Could not load shader " + vertexSource);
            return 0;
        }
        int pixelShader = loadShader(35632, fragmentSource);
        if (pixelShader == 0) {
            GalleryLog.e("TextureRender", "BitmapTextureRender(2.0)  Could not load shader " + fragmentSource);
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            GalleryLog.e("TextureRender", "BitmapTextureRender(2.0) Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
        if (linkStatus[0] != 1) {
            GalleryLog.e("TextureRender", "BitmapTextureRender(2.0) Could not link program: ");
            GalleryLog.e("TextureRender", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            GalleryLog.e("TextureRender", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
