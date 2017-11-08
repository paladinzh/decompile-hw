package com.android.gallery3d.ui;

import android.graphics.RectF;
import android.opengl.Matrix;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.IntArray;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.WeakHashMap;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

public class GLCanvasImpl implements GLCanvas {
    private static final float[] BOX_COORDINATES = new float[]{0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f};
    private WeakHashMap<BasicTexture, Object> mAllTextures = new WeakHashMap();
    private float mAlpha;
    private boolean mBlendEnabled = true;
    private int mBoxCoords;
    int mCountDrawLine;
    int mCountDrawMesh;
    int mCountFillRect;
    int mCountTextureOES;
    int mCountTextureRect;
    private float[] mCropRect = new float[4];
    private int mCustomFlags = 1;
    private final IntArray mDeleteBuffers = new IntArray();
    private final RectF mDrawTextureSourceRect = new RectF();
    private final RectF mDrawTextureTargetRect = new RectF();
    private int[] mFrameBuffer = new int[1];
    private final GL11 mGL;
    private final GLState mGLState;
    private final float[] mMapPointsBuffer = new float[4];
    private final float[] mMatrixValues = new float[16];
    private ConfigState mRecycledRestoreAction;
    private final ArrayList<ConfigState> mRestoreStack = new ArrayList();
    private int mScreenHeight;
    private int mScreenWidth;
    private final ArrayList<RawTexture> mTargetStack = new ArrayList();
    private RawTexture mTargetTexture;
    private final float[] mTempMatrix = new float[32];
    private final float[] mTextureColor = new float[4];
    private int[] mTextureId = new int[1];
    private final float[] mTextureMatrixValues = new float[16];
    private final IntArray mUnboundTextures = new IntArray();

    private static class ConfigState {
        float mAlpha;
        float[] mMatrix;
        ConfigState mNextFree;

        private ConfigState() {
            this.mMatrix = new float[16];
        }

        public void restore(GLCanvasImpl canvas) {
            if (this.mAlpha >= 0.0f) {
                canvas.setAlpha(this.mAlpha);
            }
            if (this.mMatrix[0] != Float.NEGATIVE_INFINITY) {
                System.arraycopy(this.mMatrix, 0, canvas.mMatrixValues, 0, 16);
            }
        }
    }

    private static class GLState {
        private boolean mBlendEnabled = true;
        private final GL11 mGL;
        private boolean mLineSmooth = false;
        private float mLineWidth = WMElement.CAMERASIZEVALUE1B1;
        private int mTexEnvMode = 7681;
        private float mTextureAlpha = WMElement.CAMERASIZEVALUE1B1;
        private int mTextureTarget = 3553;

        public GLState(GL11 gl) {
            this.mGL = gl;
            gl.glDisable(2896);
            gl.glEnable(3024);
            gl.glEnableClientState(32884);
            gl.glEnableClientState(32888);
            gl.glEnable(3553);
            gl.glTexEnvf(8960, 8704, 7681.0f);
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClearStencil(0);
            gl.glEnable(3042);
            gl.glBlendFunc(1, 771);
            gl.glPixelStorei(3317, 2);
        }

        public void setTexEnvMode(int mode) {
            if (this.mTexEnvMode != mode) {
                this.mTexEnvMode = mode;
                this.mGL.glTexEnvf(8960, 8704, (float) mode);
            }
        }

        public void setLineWidth(float width) {
            if (this.mLineWidth != width) {
                this.mLineWidth = width;
                this.mGL.glLineWidth(width);
            }
        }

        public void setTextureAlpha(float alpha) {
            if (this.mTextureAlpha != alpha) {
                this.mTextureAlpha = alpha;
                if (alpha >= 0.95f) {
                    this.mGL.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
                    setTexEnvMode(7681);
                } else {
                    this.mGL.glColor4f(alpha, alpha, alpha, alpha);
                    setTexEnvMode(8448);
                }
            }
        }

        public void setColorMode(int color, float alpha) {
            boolean z = true;
            if (Utils.isOpaque(color) && alpha >= 0.95f) {
                z = false;
            }
            setBlendEnabled(z);
            this.mTextureAlpha = GroundOverlayOptions.NO_DIMENSION;
            setTextureTarget(0);
            float prealpha = (((((float) (color >>> 24)) * alpha) * 65535.0f) / 255.0f) / 255.0f;
            this.mGL.glColor4x(Math.round(((float) ((color >> 16) & 255)) * prealpha), Math.round(((float) ((color >> 8) & 255)) * prealpha), Math.round(((float) (color & 255)) * prealpha), Math.round(255.0f * prealpha));
        }

        public void setTextureTarget(int target) {
            if (this.mTextureTarget != target) {
                if (this.mTextureTarget != 0) {
                    this.mGL.glDisable(this.mTextureTarget);
                }
                this.mTextureTarget = target;
                if (this.mTextureTarget != 0) {
                    this.mGL.glEnable(this.mTextureTarget);
                }
            }
        }

        public void setBlendEnabled(boolean enabled) {
            if (this.mBlendEnabled != enabled) {
                this.mBlendEnabled = enabled;
                if (enabled) {
                    this.mGL.glEnable(3042);
                } else {
                    this.mGL.glDisable(3042);
                }
            }
        }
    }

    public GLCanvasImpl(GL11 gl) {
        this.mGL = gl;
        this.mGLState = new GLState(gl);
        initialize();
    }

    public void setSize(int width, int height) {
        boolean z;
        if (width < 0 || height < 0) {
            z = false;
        } else {
            z = true;
        }
        Utils.assertTrue(z);
        if (this.mTargetTexture == null) {
            this.mScreenWidth = width;
            this.mScreenHeight = height;
        }
        this.mAlpha = WMElement.CAMERASIZEVALUE1B1;
        GL11 gl = this.mGL;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(5889);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, (float) width, 0.0f, (float) height, -20.0f, MapConfig.MAX_ZOOM_INDOOR);
        gl.glMatrixMode(5888);
        gl.glLoadIdentity();
        float[] matrix = this.mMatrixValues;
        Matrix.setIdentityM(matrix, 0);
        if (this.mTargetTexture == null) {
            Matrix.translateM(matrix, 0, 0.0f, (float) height, 0.0f);
            Matrix.scaleM(matrix, 0, WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        }
    }

    public void setAlpha(float alpha) {
        boolean z = false;
        if (alpha >= 0.0f && alpha <= WMElement.CAMERASIZEVALUE1B1) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mAlpha = alpha;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public void multiplyAlpha(float alpha) {
        boolean z = false;
        if (alpha >= 0.0f && alpha <= WMElement.CAMERASIZEVALUE1B1) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mAlpha *= alpha;
    }

    private static ByteBuffer allocateDirectNativeOrderBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    private void initialize() {
        GL11 gl = this.mGL;
        FloatBuffer xyBuffer = allocateDirectNativeOrderBuffer((BOX_COORDINATES.length * 32) / 8).asFloatBuffer();
        xyBuffer.put(BOX_COORDINATES, 0, BOX_COORDINATES.length).position(0);
        int[] name = new int[1];
        GLId.glGenBuffers(1, name, 0);
        this.mBoxCoords = name[0];
        gl.glBindBuffer(34962, this.mBoxCoords);
        gl.glBufferData(34962, xyBuffer.capacity() * 4, xyBuffer, 35044);
        gl.glVertexPointer(2, 5126, 0, 0);
        gl.glTexCoordPointer(2, 5126, 0, 0);
        gl.glClientActiveTexture(33985);
        gl.glTexCoordPointer(2, 5126, 0, 0);
        gl.glClientActiveTexture(33984);
        gl.glEnableClientState(32888);
    }

    public void drawRect(float x, float y, float width, float height, GLPaint paint) {
        GL11 gl = this.mGL;
        this.mGLState.setColorMode(paint.getColor(), this.mAlpha);
        this.mGLState.setLineWidth(paint.getLineWidth());
        saveTransform();
        translate(x, y);
        scale(width, height, WMElement.CAMERASIZEVALUE1B1);
        gl.glLoadMatrixf(this.mMatrixValues, 0);
        gl.glDrawArrays(2, 6, 4);
        restoreTransform();
        this.mCountDrawLine++;
    }

    public void drawLine(float x1, float y1, float x2, float y2, GLPaint paint) {
        GL11 gl = this.mGL;
        this.mGLState.setColorMode(paint.getColor(), this.mAlpha);
        this.mGLState.setLineWidth(paint.getLineWidth());
        saveTransform();
        translate(x1, y1);
        scale(x2 - x1, y2 - y1, WMElement.CAMERASIZEVALUE1B1);
        gl.glLoadMatrixf(this.mMatrixValues, 0);
        gl.glDrawArrays(3, 4, 2);
        restoreTransform();
        this.mCountDrawLine++;
    }

    public void fillRect(float x, float y, float width, float height, int color) {
        this.mGLState.setColorMode(color, this.mAlpha);
        GL11 gl = this.mGL;
        saveTransform();
        translate(x, y);
        scale(width, height, WMElement.CAMERASIZEVALUE1B1);
        gl.glLoadMatrixf(this.mMatrixValues, 0);
        gl.glDrawArrays(5, 0, 4);
        restoreTransform();
        this.mCountFillRect++;
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(this.mMatrixValues, 0, x, y, z);
    }

    public void translate(float x, float y) {
        float[] m = this.mMatrixValues;
        m[12] = m[12] + ((m[0] * x) + (m[4] * y));
        m[13] = m[13] + ((m[1] * x) + (m[5] * y));
        m[14] = m[14] + ((m[2] * x) + (m[6] * y));
        m[15] = m[15] + ((m[3] * x) + (m[7] * y));
    }

    public void scale(float sx, float sy, float sz) {
        Matrix.scaleM(this.mMatrixValues, 0, sx, sy, sz);
    }

    public void rotate(float angle, float x, float y, float z) {
        if (angle != 0.0f) {
            float[] temp = this.mTempMatrix;
            Matrix.setRotateM(temp, 0, angle, x, y, z);
            Matrix.multiplyMM(temp, 16, this.mMatrixValues, 0, temp, 0);
            System.arraycopy(temp, 16, this.mMatrixValues, 0, 16);
        }
    }

    public void multiplyMatrix(float[] matrix, int offset) {
        float[] temp = this.mTempMatrix;
        Matrix.multiplyMM(temp, 0, this.mMatrixValues, 0, matrix, offset);
        System.arraycopy(temp, 0, this.mMatrixValues, 0, 16);
    }

    private void textureRect(float x, float y, float width, float height) {
        GL11 gl = this.mGL;
        saveTransform();
        translate(x, y);
        scale(width, height, WMElement.CAMERASIZEVALUE1B1);
        gl.glLoadMatrixf(this.mMatrixValues, 0);
        gl.glDrawArrays(5, 0, 4);
        restoreTransform();
        this.mCountTextureRect++;
    }

    public void drawMesh(BasicTexture tex, int x, int y, int xyBuffer, int uvBuffer, int indexBuffer, int indexCount) {
        float alpha = this.mAlpha;
        if (bindTexture(tex)) {
            GLState gLState = this.mGLState;
            boolean z = this.mBlendEnabled ? !tex.isOpaque() || alpha < 0.95f : false;
            gLState.setBlendEnabled(z);
            this.mGLState.setTextureAlpha(alpha);
            setTextureCoords(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
            saveTransform();
            translate((float) x, (float) y);
            this.mGL.glLoadMatrixf(this.mMatrixValues, 0);
            this.mGL.glBindBuffer(34962, xyBuffer);
            this.mGL.glVertexPointer(2, 5126, 0, 0);
            this.mGL.glBindBuffer(34962, uvBuffer);
            this.mGL.glTexCoordPointer(2, 5126, 0, 0);
            this.mGL.glBindBuffer(34963, indexBuffer);
            this.mGL.glDrawElements(5, indexCount, 5121, 0);
            this.mGL.glBindBuffer(34962, this.mBoxCoords);
            this.mGL.glVertexPointer(2, 5126, 0, 0);
            this.mGL.glTexCoordPointer(2, 5126, 0, 0);
            restoreTransform();
            this.mCountDrawMesh++;
        }
    }

    private float[] mapPoints(float[] m, int x1, int y1, int x2, int y2) {
        float[] r = this.mMapPointsBuffer;
        float y3 = ((m[1] * ((float) x1)) + (m[5] * ((float) y1))) + m[13];
        float w3 = ((m[3] * ((float) x1)) + (m[7] * ((float) y1))) + m[15];
        r[0] = (((m[0] * ((float) x1)) + (m[4] * ((float) y1))) + m[12]) / w3;
        r[1] = y3 / w3;
        float y4 = ((m[1] * ((float) x2)) + (m[5] * ((float) y2))) + m[13];
        float w4 = ((m[3] * ((float) x2)) + (m[7] * ((float) y2))) + m[15];
        r[2] = (((m[0] * ((float) x2)) + (m[4] * ((float) y2))) + m[12]) / w4;
        r[3] = y4 / w4;
        return r;
    }

    private void drawBoundTexture(BasicTexture texture, int x, int y, int width, int height) {
        TraceController.traceBegin("GLCanvasImpl.drawBoundTexture");
        if (isMatrixRotatedOrFlipped(this.mMatrixValues)) {
            if (texture.hasBorder()) {
                setTextureCoords(WMElement.CAMERASIZEVALUE1B1 / ((float) texture.getTextureWidth()), WMElement.CAMERASIZEVALUE1B1 / ((float) texture.getTextureHeight()), (((float) texture.getWidth()) - WMElement.CAMERASIZEVALUE1B1) / ((float) texture.getTextureWidth()), (((float) texture.getHeight()) - WMElement.CAMERASIZEVALUE1B1) / ((float) texture.getTextureHeight()));
            } else {
                setTextureCoords(0.0f, 0.0f, ((float) texture.getWidth()) / ((float) texture.getTextureWidth()), ((float) texture.getHeight()) / ((float) texture.getTextureHeight()));
            }
            textureRect((float) x, (float) y, (float) width, (float) height);
        } else {
            float[] points = mapPoints(this.mMatrixValues, x, y + height, x + width, y);
            x = (int) (points[0] + 0.5f);
            y = (int) (points[1] + 0.5f);
            width = ((int) (points[2] + 0.5f)) - x;
            height = ((int) (points[3] + 0.5f)) - y;
            if (width > 0 && height > 0) {
                ((GL11Ext) this.mGL).glDrawTexiOES(x, y, 0, width, height);
                this.mCountTextureOES++;
            }
        }
        TraceController.traceEnd();
    }

    public void drawTexture(BasicTexture texture, int x, int y, int width, int height) {
        TraceController.traceBegin("GLCanvasImpl.drawTexture");
        drawTexture(texture, x, y, width, height, this.mAlpha);
        TraceController.traceEnd();
    }

    private void drawTexture(BasicTexture texture, int x, int y, int width, int height, float alpha) {
        boolean z = true;
        if (width > 0 && height > 0) {
            GLState gLState = this.mGLState;
            if (!this.mBlendEnabled) {
                z = false;
            } else if (texture.isOpaque() && alpha >= 0.95f) {
                z = false;
            }
            gLState.setBlendEnabled(z);
            if (bindTexture(texture)) {
                this.mGLState.setTextureAlpha(alpha);
                drawBoundTexture(texture, x, y, width, height);
            }
        }
    }

    public void drawTexture(BasicTexture texture, RectF source, RectF target) {
        boolean z = true;
        if (target.width() > 0.0f && target.height() > 0.0f) {
            this.mDrawTextureSourceRect.set(source);
            this.mDrawTextureTargetRect.set(target);
            source = this.mDrawTextureSourceRect;
            target = this.mDrawTextureTargetRect;
            GLState gLState = this.mGLState;
            if (!this.mBlendEnabled) {
                z = false;
            } else if (texture.isOpaque() && this.mAlpha >= 0.95f) {
                z = false;
            }
            gLState.setBlendEnabled(z);
            if (bindTexture(texture)) {
                convertCoordinate(source, target, texture);
                setTextureCoords(source);
                this.mGLState.setTextureAlpha(this.mAlpha);
                textureRect(target.left, target.top, target.width(), target.height());
            }
        }
    }

    public void drawTexture(BasicTexture texture, float[] mTextureTransform, int x, int y, int w, int h) {
        boolean z = true;
        GLState gLState = this.mGLState;
        if (!this.mBlendEnabled) {
            z = false;
        } else if (texture.isOpaque() && this.mAlpha >= 0.95f) {
            z = false;
        }
        gLState.setBlendEnabled(z);
        if (bindTexture(texture)) {
            setTextureCoords(mTextureTransform);
            this.mGLState.setTextureAlpha(this.mAlpha);
            textureRect((float) x, (float) y, (float) w, (float) h);
        }
    }

    private static void convertCoordinate(RectF source, RectF target, BasicTexture texture) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        int texWidth = texture.getTextureWidth();
        int texHeight = texture.getTextureHeight();
        source.left /= (float) texWidth;
        source.right /= (float) texWidth;
        source.top /= (float) texHeight;
        source.bottom /= (float) texHeight;
        float xBound = ((float) width) / ((float) texWidth);
        if (source.right > xBound) {
            target.right = target.left + ((target.width() * (xBound - source.left)) / source.width());
            source.right = xBound;
        }
        float yBound = ((float) height) / ((float) texHeight);
        if (source.bottom > yBound) {
            target.bottom = target.top + ((target.height() * (yBound - source.top)) / source.height());
            source.bottom = yBound;
        }
    }

    public void drawMixed(BasicTexture from, int toColor, float ratio, int x, int y, int w, int h) {
        drawMixed(from, toColor, ratio, x, y, w, h, this.mAlpha);
    }

    private boolean bindTexture(BasicTexture texture) {
        if (!texture.onBind(this)) {
            return false;
        }
        int target = texture.getTarget();
        this.mGLState.setTextureTarget(target);
        this.mGL.glBindTexture(target, texture.getId());
        return true;
    }

    private void setTextureColor(float r, float g, float b, float alpha) {
        float[] color = this.mTextureColor;
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = alpha;
    }

    private void setMixedColor(int toColor, float ratio, float alpha) {
        float combo = alpha * (WMElement.CAMERASIZEVALUE1B1 - ratio);
        float colorScale = (((float) (toColor >>> 24)) * ((alpha * ratio) / (WMElement.CAMERASIZEVALUE1B1 - combo))) / 65025.0f;
        setTextureColor(((float) ((toColor >>> 16) & 255)) * colorScale, ((float) ((toColor >>> 8) & 255)) * colorScale, ((float) (toColor & 255)) * colorScale, combo);
        GL11 gl = this.mGL;
        gl.glTexEnvfv(8960, 8705, this.mTextureColor, 0);
        gl.glTexEnvf(8960, 34161, 34165.0f);
        gl.glTexEnvf(8960, 34162, 34165.0f);
        gl.glTexEnvf(8960, 34177, 34166.0f);
        gl.glTexEnvf(8960, 34193, 768.0f);
        gl.glTexEnvf(8960, 34185, 34166.0f);
        gl.glTexEnvf(8960, 34201, 770.0f);
        gl.glTexEnvf(8960, 34178, 34166.0f);
        gl.glTexEnvf(8960, 34194, 770.0f);
        gl.glTexEnvf(8960, 34186, 34166.0f);
        gl.glTexEnvf(8960, 34202, 770.0f);
    }

    private void drawMixed(BasicTexture from, int toColor, float ratio, int x, int y, int width, int height, float alpha) {
        if (ratio <= 0.01f) {
            drawTexture(from, x, y, width, height, alpha);
        } else if (ratio >= WMElement.CAMERASIZEVALUE1B1) {
            fillRect((float) x, (float) y, (float) width, (float) height, toColor);
        } else {
            GLState gLState = this.mGLState;
            boolean z = this.mBlendEnabled ? (from.isOpaque() && Utils.isOpaque(toColor)) ? alpha < 0.95f : true : false;
            gLState.setBlendEnabled(z);
            GL11 gl = this.mGL;
            if (bindTexture(from)) {
                this.mGLState.setTexEnvMode(34160);
                setMixedColor(toColor, ratio, alpha);
                drawBoundTexture(from, x, y, width, height);
                this.mGLState.setTexEnvMode(7681);
            }
        }
    }

    private static boolean isMatrixRotatedOrFlipped(float[] matrix) {
        if (Math.abs(matrix[4]) > 1.0E-5f || Math.abs(matrix[1]) > 1.0E-5f || matrix[0] < -1.0E-5f || matrix[5] > 1.0E-5f) {
            return true;
        }
        return false;
    }

    public GL11 getGLInstance() {
        return this.mGL;
    }

    public void clearBuffer(float[] argb) {
        if (argb == null || argb.length != 4) {
            this.mGL.glClearColor(0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        } else {
            this.mGL.glClearColor(argb[1], argb[2], argb[3], argb[0]);
        }
        this.mGL.glClear(16384);
    }

    public void clearBuffer() {
        clearBuffer(null);
    }

    private void setTextureCoords(RectF source) {
        setTextureCoords(source.left, source.top, source.right, source.bottom);
    }

    private void setTextureCoords(float left, float top, float right, float bottom) {
        this.mGL.glMatrixMode(5890);
        this.mTextureMatrixValues[0] = right - left;
        this.mTextureMatrixValues[5] = bottom - top;
        this.mTextureMatrixValues[10] = WMElement.CAMERASIZEVALUE1B1;
        this.mTextureMatrixValues[12] = left;
        this.mTextureMatrixValues[13] = top;
        this.mTextureMatrixValues[15] = WMElement.CAMERASIZEVALUE1B1;
        this.mGL.glLoadMatrixf(this.mTextureMatrixValues, 0);
        this.mGL.glMatrixMode(5888);
    }

    private void setTextureCoords(float[] mTextureTransform) {
        this.mGL.glMatrixMode(5890);
        this.mGL.glLoadMatrixf(mTextureTransform, 0);
        this.mGL.glMatrixMode(5888);
    }

    public boolean unloadTexture(BasicTexture t) {
        synchronized (this.mUnboundTextures) {
            if (t.isLoaded()) {
                this.mUnboundTextures.add(t.mId);
                return true;
            }
            return false;
        }
    }

    public void deleteBuffer(int bufferId) {
        synchronized (this.mUnboundTextures) {
            this.mDeleteBuffers.add(bufferId);
        }
    }

    public void deleteRecycledResources() {
        synchronized (this.mUnboundTextures) {
            IntArray ids = this.mUnboundTextures;
            if (ids.size() > 0) {
                GLId.glDeleteTextures(this.mGL, ids.size(), ids.getInternalArray(), 0);
                ids.clear();
            }
            ids = this.mDeleteBuffers;
            if (ids.size() > 0) {
                GLId.glDeleteBuffers(this.mGL, ids.size(), ids.getInternalArray(), 0);
                ids.clear();
            }
        }
    }

    public void save() {
        save(-1);
    }

    public void save(int saveFlags) {
        ConfigState config = obtainRestoreConfig();
        if ((saveFlags & 1) != 0) {
            config.mAlpha = this.mAlpha;
        } else {
            config.mAlpha = GroundOverlayOptions.NO_DIMENSION;
        }
        if ((saveFlags & 2) != 0) {
            System.arraycopy(this.mMatrixValues, 0, config.mMatrix, 0, 16);
        } else {
            config.mMatrix[0] = Float.NEGATIVE_INFINITY;
        }
        this.mRestoreStack.add(config);
    }

    public void restore() {
        if (this.mRestoreStack.isEmpty()) {
            throw new IllegalStateException();
        }
        ConfigState config = (ConfigState) this.mRestoreStack.remove(this.mRestoreStack.size() - 1);
        config.restore(this);
        freeRestoreConfig(config);
    }

    private void freeRestoreConfig(ConfigState action) {
        action.mNextFree = this.mRecycledRestoreAction;
        this.mRecycledRestoreAction = action;
    }

    private ConfigState obtainRestoreConfig() {
        if (this.mRecycledRestoreAction == null) {
            return new ConfigState();
        }
        ConfigState result = this.mRecycledRestoreAction;
        this.mRecycledRestoreAction = result.mNextFree;
        return result;
    }

    private void saveTransform() {
        System.arraycopy(this.mMatrixValues, 0, this.mTempMatrix, 0, 16);
    }

    private void restoreTransform() {
        System.arraycopy(this.mTempMatrix, 0, this.mMatrixValues, 0, 16);
    }

    private void setRenderTarget(RawTexture texture) {
        GL11ExtensionPack gl11ep = this.mGL;
        if (this.mTargetTexture == null && texture != null) {
            GLId.glGenBuffers(1, this.mFrameBuffer, 0);
            gl11ep.glBindFramebufferOES(36160, this.mFrameBuffer[0]);
        }
        if (this.mTargetTexture != null && texture == null) {
            gl11ep.glBindFramebufferOES(36160, 0);
            gl11ep.glDeleteFramebuffersOES(1, this.mFrameBuffer, 0);
        }
        this.mTargetTexture = texture;
        if (texture == null) {
            setSize(this.mScreenWidth, this.mScreenHeight);
            return;
        }
        setSize(texture.getWidth(), texture.getHeight());
        if (!texture.isLoaded()) {
            texture.prepare(this);
        }
        gl11ep.glFramebufferTexture2DOES(36160, 36064, 3553, texture.getId(), 0);
        checkFramebufferStatus(gl11ep);
    }

    public void endRenderTarget() {
        setRenderTarget((RawTexture) this.mTargetStack.remove(this.mTargetStack.size() - 1));
        restore();
    }

    public int[] getTextureId() {
        return this.mTextureId;
    }

    public float[] getCropRect() {
        return this.mCropRect;
    }

    public void putTexture(BasicTexture basicTexture) {
        synchronized (this.mAllTextures) {
            this.mAllTextures.put(basicTexture, null);
        }
    }

    public void yieldAllTextures() {
        synchronized (this.mAllTextures) {
            for (BasicTexture t : this.mAllTextures.keySet()) {
                t.yield();
            }
        }
    }

    public void invalidateAllTextures() {
        synchronized (this.mAllTextures) {
            for (BasicTexture t : this.mAllTextures.keySet()) {
                t.mState = 0;
                t.setAssociatedCanvas(null);
            }
        }
    }

    public void setCustomFlag(int flag) {
        this.mCustomFlags = flag;
    }

    public int getCustomFlag() {
        return this.mCustomFlags;
    }

    public void beginRenderTarget(RawTexture texture) {
        save();
        this.mTargetStack.add(this.mTargetTexture);
        setRenderTarget(texture);
    }

    private static void checkFramebufferStatus(GL11ExtensionPack gl11ep) {
        int status = gl11ep.glCheckFramebufferStatusOES(36160);
        if (status != 36053) {
            String msg = "";
            switch (status) {
                case 36054:
                    msg = "FRAMEBUFFER_ATTACHMENT";
                    break;
                case 36055:
                    msg = "FRAMEBUFFER_MISSING_ATTACHMENT";
                    break;
                case 36057:
                    msg = "FRAMEBUFFER_INCOMPLETE_DIMENSIONS";
                    break;
                case 36058:
                    msg = "FRAMEBUFFER_FORMATS";
                    break;
                case 36059:
                    msg = "FRAMEBUFFER_DRAW_BUFFER";
                    break;
                case 36060:
                    msg = "FRAMEBUFFER_READ_BUFFER";
                    break;
                case 36061:
                    msg = "FRAMEBUFFER_UNSUPPORTED";
                    break;
            }
            throw new RuntimeException(msg + ":" + Integer.toHexString(status));
        }
    }
}
