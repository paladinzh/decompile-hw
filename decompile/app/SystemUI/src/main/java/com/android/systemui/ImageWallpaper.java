package com.android.systemui;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.os.Trace;
import android.renderscript.Matrix4f;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import com.android.systemui.utils.analyze.MemUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class ImageWallpaper extends WallpaperService {
    DrawableEngine mEngine;
    boolean mIsHwAccelerated;
    WallpaperManager mWallpaperManager;

    class DrawableEngine extends Engine {
        Bitmap mBackground;
        int mBackgroundHeight = -1;
        int mBackgroundWidth = -1;
        private Display mDefaultDisplay;
        protected int mDisplayHeightAtLastSurfaceSizeUpdate = 1920;
        protected int mDisplayWidthAtLastSurfaceSizeUpdate = 1080;
        EGL10 mEgl;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        private int mLastRequestedHeight = -1;
        private int mLastRequestedWidth = -1;
        int mLastRotation = -1;
        int mLastSurfaceHeight = -1;
        int mLastSurfaceWidth = -1;
        int mLastXTranslation;
        int mLastYTranslation;
        private AsyncTask<Void, Void, Bitmap> mLoader;
        private boolean mNeedsDrawAfterLoadingWallpaper;
        boolean mOffsetsChanged;
        private int mRotationAtLastSurfaceSizeUpdate = -1;
        float mScale = 1.0f;
        boolean mSurfaceValid;
        private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
        boolean mVisible = true;
        float mXOffset = 0.5f;
        float mYOffset = 0.5f;

        public DrawableEngine() {
            super(ImageWallpaper.this);
            setFixedSizeAllowed(true);
        }

        public void trimMemory(int level) {
            if (level < 10 || level > 15 || this.mBackground == null) {
                Log.w("ImageWallpaper", "trimMemory condition not ready!");
                return;
            }
            Log.i("ImageWallpaper", "trimMemory release wallpaper bitmap");
            this.mBackground.recycle();
            this.mBackground = null;
            this.mBackgroundWidth = -1;
            this.mBackgroundHeight = -1;
            ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.mDefaultDisplay = ((WindowManager) ImageWallpaper.this.getSystemService(WindowManager.class)).getDefaultDisplay();
            setOffsetNotificationsEnabled(false);
            updateSurfaceSize(surfaceHolder, getDefaultDisplayInfo(), false);
        }

        public void onDestroy() {
            super.onDestroy();
            this.mBackground = null;
            ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
        }

        boolean updateSurfaceSize(SurfaceHolder surfaceHolder, DisplayInfo displayInfo, boolean forDraw) {
            boolean hasWallpaper = true;
            if (this.mBackgroundWidth <= 0 || this.mBackgroundHeight <= 0) {
                ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                loadWallpaper(forDraw);
                hasWallpaper = false;
            }
            int surfaceWidth = Math.max(displayInfo.logicalWidth, this.mBackgroundWidth);
            int surfaceHeight = Math.max(displayInfo.logicalHeight, this.mBackgroundHeight);
            updateSurfaceSizeHw(surfaceHolder);
            return hasWallpaper;
        }

        public void onVisibilityChanged(boolean visible) {
            if (this.mVisible != visible) {
                this.mVisible = visible;
                if (visible) {
                    drawFrame();
                }
            }
        }

        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixels, int yPixels) {
            if (!(this.mXOffset == xOffset && this.mYOffset == yOffset)) {
                this.mXOffset = xOffset;
                this.mYOffset = yOffset;
                this.mOffsetsChanged = true;
            }
            drawFrame();
        }

        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.mLastSurfaceHeight = -1;
            this.mLastSurfaceWidth = -1;
            this.mSurfaceValid = false;
        }

        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            this.mLastSurfaceHeight = -1;
            this.mLastSurfaceWidth = -1;
            this.mSurfaceValid = true;
        }

        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            drawFrame();
        }

        private DisplayInfo getDefaultDisplayInfo() {
            this.mDefaultDisplay.getDisplayInfo(this.mTmpDisplayInfo);
            return this.mTmpDisplayInfo;
        }

        void drawFrame() {
            if (this.mSurfaceValid) {
                try {
                    Trace.traceBegin(8, "drawWallpaper");
                    DisplayInfo displayInfo = getDefaultDisplayInfo();
                    int newRotation = displayInfo.rotation;
                    if (newRotation != this.mLastRotation) {
                        if (updateSurfaceSize(getSurfaceHolder(), displayInfo, true)) {
                            this.mRotationAtLastSurfaceSizeUpdate = newRotation;
                            this.mDisplayWidthAtLastSurfaceSizeUpdate = displayInfo.logicalWidth;
                            this.mDisplayHeightAtLastSurfaceSizeUpdate = displayInfo.logicalHeight;
                        } else {
                            Trace.traceEnd(8);
                            if (!ImageWallpaper.this.mIsHwAccelerated) {
                                this.mBackground = null;
                                ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                            }
                            return;
                        }
                    }
                    SurfaceHolder sh = getSurfaceHolder();
                    Rect frame = sh.getSurfaceFrame();
                    int dw = frame.width();
                    int dh = frame.height();
                    boolean surfaceDimensionsChanged = dw == this.mLastSurfaceWidth ? dh != this.mLastSurfaceHeight : true;
                    boolean redrawNeeded = surfaceDimensionsChanged || newRotation != this.mLastRotation;
                    if (!redrawNeeded && !this.mOffsetsChanged) {
                        Trace.traceEnd(8);
                        if (!ImageWallpaper.this.mIsHwAccelerated) {
                            this.mBackground = null;
                            ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                        }
                    } else if (this.mBackground == null || this.mBackground.isRecycled()) {
                        ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                        loadWallpaper(true);
                        Trace.traceEnd(8);
                        if (!ImageWallpaper.this.mIsHwAccelerated) {
                            this.mBackground = null;
                            ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                        }
                    } else {
                        this.mScale = Math.max(1.0f, Math.max(((float) dw) / ((float) this.mBackground.getWidth()), ((float) dh) / ((float) this.mBackground.getHeight())));
                        int availw = dw - ((int) (((float) this.mBackground.getWidth()) * this.mScale));
                        int availh = dh - ((int) (((float) this.mBackground.getHeight()) * this.mScale));
                        int xPixels = availw / 2;
                        int yPixels = availh / 2;
                        int availwUnscaled = dw - this.mBackground.getWidth();
                        int availhUnscaled = dh - this.mBackground.getHeight();
                        if (availwUnscaled < 0) {
                            xPixels += (int) ((((float) availwUnscaled) * (this.mXOffset - 0.5f)) + 0.5f);
                        }
                        if (availhUnscaled < 0) {
                            yPixels += (int) ((((float) availhUnscaled) * (this.mYOffset - 0.5f)) + 0.5f);
                        }
                        this.mOffsetsChanged = false;
                        if (surfaceDimensionsChanged) {
                            this.mLastSurfaceWidth = dw;
                            this.mLastSurfaceHeight = dh;
                        }
                        if (!redrawNeeded && xPixels == this.mLastXTranslation && yPixels == this.mLastYTranslation) {
                            Trace.traceEnd(8);
                            if (!ImageWallpaper.this.mIsHwAccelerated) {
                                this.mBackground = null;
                                ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                            }
                            return;
                        }
                        this.mLastXTranslation = xPixels;
                        this.mLastYTranslation = yPixels;
                        this.mLastRotation = newRotation;
                        beforeDrawing(new Rect(xPixels, yPixels, availw, availh), this.mBackground);
                        if (!ImageWallpaper.this.mIsHwAccelerated) {
                            drawWallpaperWithCanvas(sh, availw, availh, xPixels, yPixels);
                        } else if (!drawWallpaperWithOpenGL(sh, availw, availh, xPixels, yPixels)) {
                            drawWallpaperWithCanvas(sh, availw, availh, xPixels, yPixels);
                        }
                        afterDrawing();
                        Trace.traceEnd(8);
                        if (!ImageWallpaper.this.mIsHwAccelerated) {
                            this.mBackground = null;
                            ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                        }
                    }
                } catch (Throwable th) {
                    Trace.traceEnd(8);
                    if (!ImageWallpaper.this.mIsHwAccelerated) {
                        this.mBackground = null;
                        ImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                    }
                }
            }
        }

        private void loadWallpaper(boolean needsDraw) {
            this.mNeedsDrawAfterLoadingWallpaper |= needsDraw;
            if (this.mLoader == null) {
                this.mLoader = new AsyncTask<Void, Void, Bitmap>() {
                    protected Bitmap doInBackground(Void... params) {
                        Throwable exception = null;
                        Bitmap bitmap = null;
                        try {
                            bitmap = ImageWallpaper.this.mWallpaperManager.getBitmap();
                        } catch (Throwable e) {
                            exception = e;
                        }
                        if (exception != null) {
                            Log.w("ImageWallpaper", "Unable to load wallpaper!", exception);
                            try {
                                ImageWallpaper.this.mWallpaperManager.clear();
                            } catch (IOException ex) {
                                Log.w("ImageWallpaper", "Unable reset to default wallpaper!", ex);
                            }
                            try {
                                bitmap = ImageWallpaper.this.mWallpaperManager.getBitmap();
                            } catch (Throwable e2) {
                                Log.w("ImageWallpaper", "Unable to load default wallpaper!", e2);
                                MemUtils.logCurrentMemoryInfo();
                            }
                        }
                        if (bitmap == null || bitmap.isRecycled()) {
                            return bitmap;
                        }
                        bitmap = DrawableEngine.this.addAdditionalBitmap(ImageWallpaper.this.getApplicationContext(), bitmap);
                        DrawableEngine.this.updateWallpaperStartPoints(ImageWallpaper.this.mWallpaperManager);
                        return bitmap;
                    }

                    protected void onPostExecute(Bitmap b) {
                        DrawableEngine.this.mBackground = null;
                        DrawableEngine.this.mBackgroundWidth = -1;
                        DrawableEngine.this.mBackgroundHeight = -1;
                        if (b != null) {
                            DrawableEngine.this.mBackground = b;
                            DrawableEngine.this.mBackgroundWidth = DrawableEngine.this.mBackground.getWidth();
                            DrawableEngine.this.mBackgroundHeight = DrawableEngine.this.mBackground.getHeight();
                        }
                        DrawableEngine.this.updateSurfaceSize(DrawableEngine.this.getSurfaceHolder(), DrawableEngine.this.getDefaultDisplayInfo(), false);
                        if (DrawableEngine.this.mNeedsDrawAfterLoadingWallpaper) {
                            DrawableEngine.this.drawFrame();
                        }
                        DrawableEngine.this.mLoader = null;
                        DrawableEngine.this.mNeedsDrawAfterLoadingWallpaper = false;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }

        protected void dump(String prefix, FileDescriptor fd, PrintWriter out, String[] args) {
            super.dump(prefix, fd, out, args);
            out.print(prefix);
            out.println("ImageWallpaper.DrawableEngine:");
            out.print(prefix);
            out.print(" mBackground=");
            out.print(this.mBackground);
            out.print(" mBackgroundWidth=");
            out.print(this.mBackgroundWidth);
            out.print(" mBackgroundHeight=");
            out.println(this.mBackgroundHeight);
            out.print(prefix);
            out.print(" mLastRotation=");
            out.print(this.mLastRotation);
            out.print(" mLastSurfaceWidth=");
            out.print(this.mLastSurfaceWidth);
            out.print(" mLastSurfaceHeight=");
            out.println(this.mLastSurfaceHeight);
            out.print(prefix);
            out.print(" mXOffset=");
            out.print(this.mXOffset);
            out.print(" mYOffset=");
            out.println(this.mYOffset);
            out.print(prefix);
            out.print(" mVisible=");
            out.print(this.mVisible);
            out.print(" mOffsetsChanged=");
            out.println(this.mOffsetsChanged);
            out.print(prefix);
            out.print(" mLastXTranslation=");
            out.print(this.mLastXTranslation);
            out.print(" mLastYTranslation=");
            out.print(this.mLastYTranslation);
            out.print(" mScale=");
            out.println(this.mScale);
            out.print(prefix);
            out.print(" mLastRequestedWidth=");
            out.print(this.mLastRequestedWidth);
            out.print(" mLastRequestedHeight=");
            out.println(this.mLastRequestedHeight);
            out.print(prefix);
            out.println(" DisplayInfo at last updateSurfaceSize:");
            out.print(prefix);
            out.print("  rotation=");
            out.print(this.mRotationAtLastSurfaceSizeUpdate);
            out.print("  width=");
            out.print(this.mDisplayWidthAtLastSurfaceSizeUpdate);
            out.print("  height=");
            out.println(this.mDisplayHeightAtLastSurfaceSizeUpdate);
        }

        void drawWallpaperWithCanvas(SurfaceHolder sh, int w, int h, int left, int top) {
            Canvas c = sh.lockCanvas();
            if (c != null) {
                try {
                    float right = ((float) left) + (((float) this.mBackground.getWidth()) * this.mScale);
                    float bottom = ((float) top) + (((float) this.mBackground.getHeight()) * this.mScale);
                    if (w < 0 || h < 0) {
                        c.save(2);
                        c.clipRect((float) left, (float) top, right, bottom, Op.DIFFERENCE);
                        c.drawColor(-16777216);
                        c.restore();
                    }
                    if (this.mBackground != null) {
                        c.drawBitmap(this.mBackground, null, new RectF((float) left, (float) top, right, bottom), null);
                    }
                    sh.unlockCanvasAndPost(c);
                } catch (Throwable th) {
                    sh.unlockCanvasAndPost(c);
                }
            }
        }

        boolean drawWallpaperWithOpenGL(SurfaceHolder sh, int w, int h, int left, int top) {
            Log.i("ImageWallpaper", "Google drawWallpaperWithOpenGL:w=" + w + ", h=" + h + ", left=" + left + ", top=" + top + ", frame=" + sh.getSurfaceFrame());
            if (!initGL(sh)) {
                return false;
            }
            float right = ((float) left) + (((float) this.mBackground.getWidth()) * this.mScale);
            float bottom = ((float) top) + (((float) this.mBackground.getHeight()) * this.mScale);
            Rect frame = sh.getSurfaceFrame();
            Matrix4f ortho = new Matrix4f();
            ortho.loadOrtho(0.0f, (float) frame.width(), (float) frame.height(), 0.0f, -1.0f, 1.0f);
            Buffer triangleVertices = createMesh(left, top, right, bottom);
            int texture = loadTexture(this.mBackground);
            int program = buildProgram("attribute vec4 position;\nattribute vec2 texCoords;\nvarying vec2 outTexCoords;\nuniform mat4 projection;\n\nvoid main(void) {\n    outTexCoords = texCoords;\n    gl_Position = projection * position;\n}\n\n", "precision mediump float;\n\nvarying vec2 outTexCoords;\nuniform sampler2D texture;\n\nvoid main(void) {\n    gl_FragColor = texture2D(texture, outTexCoords);\n}\n\n");
            int attribPosition = GLES20.glGetAttribLocation(program, "position");
            int attribTexCoords = GLES20.glGetAttribLocation(program, "texCoords");
            int uniformTexture = GLES20.glGetUniformLocation(program, "texture");
            int uniformProjection = GLES20.glGetUniformLocation(program, "projection");
            checkGlError();
            GLES20.glViewport(0, 0, frame.width(), frame.height());
            GLES20.glBindTexture(3553, texture);
            GLES20.glUseProgram(program);
            GLES20.glEnableVertexAttribArray(attribPosition);
            GLES20.glEnableVertexAttribArray(attribTexCoords);
            GLES20.glUniform1i(uniformTexture, 0);
            GLES20.glUniformMatrix4fv(uniformProjection, 1, false, ortho.getArray(), 0);
            checkGlError();
            if (w > 0 || h > 0) {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GLES20.glClear(16384);
            }
            triangleVertices.position(0);
            GLES20.glVertexAttribPointer(attribPosition, 3, 5126, false, 20, triangleVertices);
            triangleVertices.position(3);
            GLES20.glVertexAttribPointer(attribTexCoords, 3, 5126, false, 20, triangleVertices);
            GLES20.glDrawArrays(5, 0, 4);
            boolean status = this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
            checkEglError();
            finishGL(texture, program);
            return status;
        }

        FloatBuffer createMesh(int left, int top, float right, float bottom) {
            float[] verticesData = new float[]{(float) left, bottom, 0.0f, 0.0f, 1.0f, right, bottom, 0.0f, 1.0f, 1.0f, (float) left, (float) top, 0.0f, 0.0f, 0.0f, right, (float) top, 0.0f, 1.0f, 0.0f};
            FloatBuffer triangleVertices = ByteBuffer.allocateDirect(verticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            triangleVertices.put(verticesData).position(0);
            return triangleVertices;
        }

        int loadTexture(Bitmap bitmap) {
            int[] textures = new int[1];
            GLES20.glActiveTexture(33984);
            GLES20.glGenTextures(1, textures, 0);
            checkGlError();
            int texture = textures[0];
            GLES20.glBindTexture(3553, texture);
            checkGlError();
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
            checkGlError();
            return texture;
        }

        int buildProgram(String vertex, String fragment) {
            int vertexShader = buildShader(vertex, 35633);
            if (vertexShader == 0) {
                return 0;
            }
            int fragmentShader = buildShader(fragment, 35632);
            if (fragmentShader == 0) {
                return 0;
            }
            int program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            checkGlError();
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
            int[] status = new int[1];
            GLES20.glGetProgramiv(program, 35714, status, 0);
            if (status[0] == 1) {
                return program;
            }
            Log.d("ImageWallpaperGL", "Error while linking program:\n" + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            return 0;
        }

        private int buildShader(String source, int type) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, source);
            checkGlError();
            GLES20.glCompileShader(shader);
            checkGlError();
            int[] status = new int[1];
            GLES20.glGetShaderiv(shader, 35713, status, 0);
            if (status[0] == 1) {
                return shader;
            }
            Log.d("ImageWallpaperGL", "Error while compiling shader:\n" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        void checkEglError() {
            int error = this.mEgl.eglGetError();
            if (error != 12288) {
                Log.w("ImageWallpaperGL", "EGL error = " + GLUtils.getEGLErrorString(error));
            }
        }

        void checkGlError() {
            int error = GLES20.glGetError();
            if (error != 0) {
                Log.w("ImageWallpaperGL", "GL error = 0x" + Integer.toHexString(error), new Throwable());
            }
        }

        void finishGL(int texture, int program) {
            GLES20.glDeleteTextures(1, new int[]{texture}, 0);
            GLES20.glDeleteProgram(program);
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEgl.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEgl.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEgl.eglTerminate(this.mEglDisplay);
        }

        void finishGL() {
            this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            this.mEgl.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEgl.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEgl.eglTerminate(this.mEglDisplay);
        }

        boolean initGL(SurfaceHolder surfaceHolder) {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
            }
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
                this.mEglConfig = chooseEglConfig();
                if (this.mEglConfig == null) {
                    throw new RuntimeException("eglConfig not initialized");
                }
                this.mEglContext = createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                if (this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    throw new RuntimeException("createContext failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
                }
                EGLSurface tmpSurface = this.mEgl.eglCreatePbufferSurface(this.mEglDisplay, this.mEglConfig, new int[]{12375, 1, 12374, 1, 12344});
                this.mEgl.eglMakeCurrent(this.mEglDisplay, tmpSurface, tmpSurface, this.mEglContext);
                int[] maxSize = new int[1];
                Rect frame = surfaceHolder.getSurfaceFrame();
                GLES20.glGetIntegerv(3379, maxSize, 0);
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                this.mEgl.eglDestroySurface(this.mEglDisplay, tmpSurface);
                if (frame.width() > maxSize[0] || frame.height() > maxSize[0]) {
                    this.mEgl.eglDestroyContext(this.mEglDisplay, this.mEglContext);
                    this.mEgl.eglTerminate(this.mEglDisplay);
                    Log.e("ImageWallpaperGL", "requested  texture size " + frame.width() + "x" + frame.height() + " exceeds the support maximum of " + maxSize[0] + "x" + maxSize[0]);
                    return false;
                }
                this.mEglSurface = this.mEgl.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, surfaceHolder, null);
                if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    int error = this.mEgl.eglGetError();
                    if (error == 12299 || error == 12291) {
                        Log.e("ImageWallpaperGL", "createWindowSurface returned " + GLUtils.getEGLErrorString(error) + ".");
                        return false;
                    }
                    throw new RuntimeException("createWindowSurface failed " + GLUtils.getEGLErrorString(error));
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    throw new RuntimeException("eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
                }
            }
            throw new RuntimeException("eglInitialize failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
        }

        EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
            return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
        }

        private EGLConfig chooseEglConfig() {
            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            if (!this.mEgl.eglChooseConfig(this.mEglDisplay, getConfig(), configs, 1, configsCount)) {
                throw new IllegalArgumentException("eglChooseConfig failed " + GLUtils.getEGLErrorString(this.mEgl.eglGetError()));
            } else if (configsCount[0] > 0) {
                return configs[0];
            } else {
                return null;
            }
        }

        private int[] getConfig() {
            return new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12327, 12344, 12344};
        }

        public void updateWallpaperStartPoints(WallpaperManager wallpaperManager) {
        }

        public void updateSurfaceSizeHw(SurfaceHolder surfaceHolder) {
        }

        public Bitmap addAdditionalBitmap(Context ctx, Bitmap bitmap) {
            return bitmap;
        }

        public void beforeDrawing(Rect inputRect, Bitmap bitmap) {
        }

        public void afterDrawing() {
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mWallpaperManager = (WallpaperManager) getSystemService("wallpaper");
        if (!isEmulator()) {
            this.mIsHwAccelerated = ActivityManager.isHighEndGfx();
        }
    }

    public void onTrimMemory(int level) {
        if (this.mEngine != null) {
            this.mEngine.trimMemory(level);
        }
    }

    private static boolean isEmulator() {
        return "1".equals(SystemProperties.get("ro.kernel.qemu", "0"));
    }

    public Engine onCreateEngine() {
        this.mEngine = new DrawableEngine();
        return this.mEngine;
    }
}
