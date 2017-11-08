package com.android.systemui;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Matrix4f;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.HwSecureWaterMark;
import android.util.Log;
import android.view.SurfaceHolder;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.MemUtils;
import com.android.systemui.utils.analyze.PerformanceCheck;
import com.android.systemui.wallpaper.HwWallpaperMask;
import com.android.systemui.wallpaper.HwWallpaperUtil;
import com.huawei.android.app.WallpaperManagerEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.Buffer;

public class HwImageWallpaper extends ImageWallpaper {
    private static boolean DEBUG_HW_WALLPAPER = true;
    private static boolean DEBUG_USE_ORIGINAL = false;
    private long BITMAP_DELAY;
    private long GC_DELAY;
    private Handler mHandler = null;

    class HwDrawableEngine extends DrawableEngine {
        private Point mBgPosLand = new Point(-1, -1);
        private Point mBgPosPort = new Point(-1, -1);
        private Rect mDest = new Rect();
        private boolean mIsFixedScreen = true;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (SecurityCodeCheck.isValidIntentAndAction(intent)) {
                    String action = intent.getAction();
                    HwLog.i("HwImageWallpaper", "onReceive, intent:" + Proguard.get(intent));
                    if ("android.intent.action.WALLPAPER_CHANGED".equals(action)) {
                        redrawWallpaper();
                    } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                        HwLog.i("HwImageWallpaper", "handle ACTION_SERVICE_STATE_CHANGED: " + HwDrawableEngine.this.mWaterMarkEnable + ", " + HwDrawableEngine.this.mWaterMarkReady);
                        if (HwDrawableEngine.this.mWaterMarkEnable && !HwDrawableEngine.this.mWaterMarkReady && HwSecureWaterMark.isWatermarkReady()) {
                            HwDrawableEngine.this.mWaterMarkReady = true;
                            redrawWallpaper();
                        }
                    }
                }
            }

            private void redrawWallpaper() {
                if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                    HwLog.i("HwImageWallpaper", "redrawWallpaper");
                }
                if (HwDrawableEngine.this.mBackground != null) {
                    HwDrawableEngine.this.mBackground.recycle();
                    HwDrawableEngine.this.mBackground = null;
                    HwDrawableEngine.this.mBackgroundWidth = -1;
                    HwDrawableEngine.this.mBackgroundHeight = -1;
                    HwImageWallpaper.this.mWallpaperManager.forgetLoadedWallpaper();
                }
                HwDrawableEngine.this.mLastSurfaceWidth = -1;
                HwDrawableEngine.this.mLastSurfaceHeight = -1;
                HwDrawableEngine.this.mLastRotation = -1;
                HwDrawableEngine.this.drawFrame();
            }
        };
        private Rect mSrc = new Rect();
        private final boolean mWaterMarkEnable = HwSecureWaterMark.isWatermarkEnable();
        private boolean mWaterMarkReady = false;

        HwDrawableEngine() {
            super();
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.WALLPAPER_CHANGED");
            if (this.mWaterMarkEnable) {
                filter.addAction("android.intent.action.SERVICE_STATE");
            }
            HwImageWallpaper.this.registerReceiver(this.mReceiver, filter);
        }

        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        public void onDestroy() {
            if (this.mReceiver != null) {
                HwImageWallpaper.this.unregisterReceiver(this.mReceiver);
            }
            super.onDestroy();
        }

        protected void dump(String prefix, FileDescriptor fd, PrintWriter out, String[] args) {
            super.dump(prefix, fd, out, args);
            out.print(prefix);
            out.println(" WallpaperManagerEx.StartPoints:");
            out.print(prefix);
            out.print("  PortPos=");
            out.print(this.mBgPosPort);
            out.print("  LandPos=");
            out.print(this.mBgPosLand);
            out.print(prefix);
            out.println(" HwAdjustDrawingRectangle:");
            out.print(prefix);
            out.print("  SrcRect=");
            out.print(this.mSrc);
            out.print("  DestRect=");
            out.print(this.mDest);
            if (this.mBackground != null) {
                out.print("  bg width=" + this.mBackground.getWidth() + ", height=" + this.mBackground.getHeight());
            }
            out.print("  surface width=" + getSurfaceHolder().getSurfaceFrame().width() + ", height=" + getSurfaceHolder().getSurfaceFrame().height());
        }

        void drawWallpaperWithCanvas(SurfaceHolder sh, int w, int h, int left, int top) {
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "drawWallpaperWithCanvas");
            }
            if (isPreparedForDrawing()) {
                Canvas c = sh.lockCanvas();
                if (c != null) {
                    try {
                        if (this.mBackground != null) {
                            c.drawBitmap(this.mBackground, this.mSrc, this.mDest, null);
                        }
                        sh.unlockCanvasAndPost(c);
                    } catch (Throwable th) {
                        sh.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        boolean drawWallpaperWithOpenGL(SurfaceHolder sh, int w, int h, int left, int top) {
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "drawWallpaperWithOpenGL:w=" + w + ", h=" + h + ", left=" + left + ", top=" + top);
            }
            if (!isPreparedForDrawing()) {
                return false;
            }
            if (!initGL(sh)) {
                return false;
            }
            Rect frame = sh.getSurfaceFrame();
            Matrix4f ortho = new Matrix4f();
            ortho.loadOrtho(0.0f, (float) frame.width(), (float) frame.height(), 0.0f, -1.0f, 1.0f);
            HwLog.i("HwImageWallpaper", "createBitmap Huawei Modify Begin");
            Buffer triangleVertices = createMesh(this.mDest.left, this.mDest.top, (float) this.mDest.right, (float) this.mDest.bottom);
            try {
                Bitmap bm = Bitmap.createBitmap(this.mBackground, this.mSrc.left, this.mSrc.top, this.mSrc.right - this.mSrc.left, this.mSrc.bottom - this.mSrc.top);
                int texture = loadTexture(bm);
                if (bm != this.mBackground) {
                    bm.recycle();
                }
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
            } catch (IllegalArgumentException ex) {
                HwLog.e("HwImageWallpaper", "createBitmap catch exception: " + ex.getMessage() + " of src Rect: " + this.mSrc);
                finishGL();
                return super.drawWallpaperWithOpenGL(sh, w, h, left, top);
            }
        }

        public void updateWallpaperStartPoints(WallpaperManager wallpaperManager) {
            try {
                int[] offsets = WallpaperManagerEx.getWallpaperStartingPoints(wallpaperManager);
                if (offsets != null) {
                    this.mBgPosPort.set(offsets[0], offsets[1]);
                    this.mBgPosLand.set(offsets[2], offsets[3]);
                }
                HwLog.d("HwImageWallpaper", "updateWallpaperStartPoints:" + this.mBgPosPort.x + "," + this.mBgPosPort.y + "," + this.mBgPosLand.x + "," + this.mBgPosLand.y);
            } catch (Exception e) {
                HwLog.e("HwImageWallpaper", "updateWallpaperStartPoints method fail");
                e.printStackTrace();
            }
        }

        public void updateSurfaceSizeHw(SurfaceHolder surfaceHolder) {
            surfaceHolder.setFixedSize(getDesiredMinimumWidth(), getDesiredMinimumHeight());
        }

        public Bitmap addAdditionalBitmap(Context ctx, Bitmap bitmap) {
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "addAdditionalBitmap");
            }
            PerformanceCheck.radarIfCallingNotInWorkThread();
            return addWaterMarkToWallpaper(HwWallpaperMask.tryUpdateWallpaperWithMask(ctx, bitmap));
        }

        public void beforeDrawing(Rect inputRect, Bitmap bitmap) {
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "beforeDrawing");
            }
            HwImageWallpaper.this.mHandler.removeMessages(2);
            updateWallpaperDrawingArea(inputRect, bitmap);
        }

        public void afterDrawing() {
            if (!HwWallpaperUtil.isPerformancePreferred()) {
                if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                    HwLog.i("HwImageWallpaper", "no performance preferred ,release wallpaper bitmap");
                }
                HwImageWallpaper.this.mHandler.removeMessages(2);
                HwImageWallpaper.this.mHandler.sendEmptyMessageDelayed(2, HwImageWallpaper.this.BITMAP_DELAY);
            }
            postDelayedGC(HwImageWallpaper.this.GC_DELAY);
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "afterDrawing finish");
            }
        }

        private boolean isPreparedForDrawing() {
            if (this.mSurfaceValid && this.mBackground != null && !this.mBackground.isRecycled()) {
                return true;
            }
            HwLog.w("HwImageWallpaper", "isPreparedForDrawing false");
            return false;
        }

        private void updateWallpaperDrawingArea(Rect inputRect, Bitmap bitmap) {
            if (bitmap == null) {
                HwLog.e("HwImageWallpaper", "in updateWallpaperDrawingArea, bitmap is null!");
                return;
            }
            this.mIsFixedScreen = HwWallpaperUtil.isFixedScreen(HwImageWallpaper.this.getApplicationContext(), bitmap.getWidth(), bitmap.getHeight());
            this.mSrc = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Point size = HwWallpaperUtil.getPoint(HwImageWallpaper.this.getApplication());
            boolean isLandScreen = SystemUiUtil.isLandscape();
            if (this.mIsFixedScreen) {
                int left;
                int top;
                int right;
                int bottom;
                int max = size.x > size.y ? size.x : size.y;
                if (bitmap.getWidth() != max || bitmap.getHeight() != max) {
                    if (this.mBackground.getWidth() < this.mBackground.getHeight()) {
                        if (isLandScreen) {
                            int clipHeight = (size.y * bitmap.getWidth()) / size.x;
                            left = 0;
                            top = (bitmap.getHeight() - clipHeight) / 2;
                            right = bitmap.getWidth();
                            bottom = top + clipHeight;
                        } else {
                            left = 0;
                            top = 0;
                            right = bitmap.getWidth();
                            bottom = bitmap.getHeight();
                        }
                    } else if (isLandScreen) {
                        left = 0;
                        top = 0;
                        right = this.mBackground.getWidth();
                        bottom = this.mBackground.getHeight();
                    } else {
                        int clipWidth = (size.x * this.mBackground.getHeight()) / size.y;
                        left = (this.mBackground.getWidth() - clipWidth) / 2;
                        top = 0;
                        right = left + clipWidth;
                        bottom = this.mBackground.getHeight();
                    }
                } else if (isLandScreen) {
                    left = 0;
                    top = this.mBgPosLand.y != -1 ? this.mBgPosLand.y : (bitmap.getHeight() - size.y) / 2;
                    right = size.x;
                    bottom = top + size.y;
                } else {
                    left = this.mBgPosPort.x != -1 ? this.mBgPosPort.x : (bitmap.getWidth() - size.x) / 2;
                    top = 0;
                    right = left + size.x;
                    bottom = size.y;
                }
                Rect src = new Rect(left, top, right, bottom);
                Rect dest = new Rect(0, 0, size.x, size.y);
                this.mSrc = src;
                this.mDest = dest;
            } else {
                int xOffset = inputRect.left;
                int yOffset = inputRect.top;
                if (isLandScreen) {
                    if (this.mBgPosLand.y == -1) {
                        yOffset = (size.y - bitmap.getHeight()) / 2;
                    } else {
                        yOffset = this.mBgPosLand.y * -1;
                    }
                    this.mSrc.top = -yOffset;
                    this.mSrc.bottom = bitmap.getHeight() - this.mSrc.top;
                    this.mDest = new Rect(0, 0, this.mSrc.width(), this.mSrc.height());
                } else {
                    this.mDest = this.mSrc;
                }
            }
            HwLog.i("HwImageWallpaper", "fixed: " + this.mIsFixedScreen + ", isLand:" + isLandScreen + " sizeXY:" + size.x + "*" + size.y + " mSrc:" + this.mSrc.left + "*" + this.mSrc.top + " - " + this.mSrc.right + "*" + this.mSrc.bottom + " mDest:" + this.mDest.left + "*" + this.mDest.top + " - " + this.mDest.right + "*" + this.mDest.bottom + " Bitmap:" + bitmap.getWidth() + "*" + bitmap.getHeight() + " mBgPosLand:" + this.mBgPosLand.x + "*" + this.mBgPosLand.y + " mBgPosPort:" + this.mBgPosPort.x + "*" + this.mBgPosPort.y);
        }

        private void releaseWallpaper() {
            HwLog.i("HwImageWallpaper", "releaseWallpaper");
            trimMemory(15);
            postDelayedGC(HwImageWallpaper.this.GC_DELAY);
        }

        private void postDelayedGC(long ms) {
            HwImageWallpaper.this.mHandler.removeMessages(3);
            HwImageWallpaper.this.mHandler.sendEmptyMessageDelayed(3, ms);
        }

        private Bitmap addWaterMarkToWallpaper(Bitmap wallpaper) {
            if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                HwLog.i("HwImageWallpaper", "addWaterMarkToWallpaper");
            }
            if (!this.mWaterMarkEnable) {
                return wallpaper;
            }
            if (wallpaper == null || wallpaper.isRecycled()) {
                Log.w("HwImageWallpaper", "addWaterMarkToWallpaper invalid bitmap state");
                return wallpaper;
            }
            if (this.mWaterMarkReady || HwSecureWaterMark.isWatermarkReady()) {
                this.mWaterMarkReady = true;
                Bitmap newBitMap = HwSecureWaterMark.addWatermark(wallpaper);
                if (newBitMap != null) {
                    if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                        Log.d("HwImageWallpaper", "addWaterMarkToWallpaper success!");
                    }
                    return newBitMap;
                } else if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                    Log.w("HwImageWallpaper", "addWaterMarkToWallpaper failed because of null return!");
                }
            } else if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                Log.d("HwImageWallpaper", "addWaterMarkToWallpaper watermark not ready!");
            }
            return wallpaper;
        }
    }

    private final class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (HwImageWallpaper.DEBUG_HW_WALLPAPER) {
                        HwLog.i("HwImageWallpaper", "MainHandler:handleMessage POST_MEMORY_RELEASE_BITMAP");
                    }
                    if (HwImageWallpaper.this.mEngine != null && (HwImageWallpaper.this.mEngine instanceof HwDrawableEngine)) {
                        ((HwDrawableEngine) HwImageWallpaper.this.mEngine).releaseWallpaper();
                        return;
                    }
                    return;
                case 3:
                    MemUtils.triggerAsyncGC();
                    return;
                default:
                    HwLog.e("HwImageWallpaper", "MainHandler:handleMessage un-support code: " + msg.what);
                    return;
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mHandler = new MainHandler();
        this.GC_DELAY = (long) getResources().getInteger(R.integer.config_active_gc_delay);
        this.BITMAP_DELAY = (long) getResources().getInteger(R.integer.config_release_wallpaper_memory_delay);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public Engine onCreateEngine() {
        if (DEBUG_USE_ORIGINAL) {
            return super.onCreateEngine();
        }
        this.mEngine = new HwDrawableEngine();
        return this.mEngine;
    }
}
