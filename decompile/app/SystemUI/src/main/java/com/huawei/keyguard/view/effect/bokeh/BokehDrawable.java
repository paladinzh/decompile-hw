package com.huawei.keyguard.view.effect.bokeh;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.BitmapUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BokehDrawable extends BitmapDrawable {
    private static float BOKEH_ALPHA_FROM = 0.0f;
    private static float BOKEH_ALPHA_TO = 1.0f;
    private static float BOKEH_SCALE_FROM = 1.1f;
    private static float BOKEH_SCALE_TO = 1.0f;
    private String mBitmapPath;
    private int mBlurAlpha;
    private Bitmap mBlurBitmap;
    private Bitmap[] mBmpFlares;
    private ColorMatrixColorFilter[] mColorFilters;
    private Context mContext;
    private boolean mDrawBlur;
    private boolean mDrawFlare;
    private float mFraction;
    private boolean mInDrawing;
    private final Paint mPaint;
    private List<Pos> mPositions;
    private Method mSetBitmapMethod;
    private int mVibColor;
    private SparseIntArray mWallPaperColorsInfo;

    private class BokehTask extends AsyncTask<Bitmap, Integer, Void> {
        Bitmap blurBitmap;
        Palette palette;
        List<Pos> pos;

        private BokehTask() {
            this.pos = new ArrayList();
        }

        protected Void doInBackground(Bitmap... params) {
            initData(params[0]);
            return null;
        }

        private BokehTask initData(Bitmap bmp) {
            try {
                if (BokehDrawable.this.mDrawBlur) {
                    createBlur(bmp);
                }
                if (BokehDrawable.this.mDrawFlare || KeyguardCfg.isCurveScreen(BokehDrawable.this.mContext)) {
                    initPallete(bmp);
                }
                BokehDrawable.this.mWallPaperColorsInfo = BitmapUtils.getColorInfo(BokehDrawable.this.mContext, bmp);
            } catch (IllegalArgumentException e) {
                HwLog.e("BokehDrawable", "create BokehDrawable fail: ", e);
            }
            return this;
        }

        private void createBlur(Bitmap bmp) {
            int sw = (int) (((float) bmp.getWidth()) * 0.15f);
            int sh = (int) (((float) bmp.getHeight()) * 0.15f);
            if (sw <= 0 || sh <= 0) {
                Log.w("BokehDrawable", "scale bitmap size err " + sw + ", " + sh);
                return;
            }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, sw, sh, false);
            this.blurBitmap = BitmapUtils.blurBitmap(BokehDrawable.this.mContext, scaledBitmap, scaledBitmap, 25.0f);
        }

        private void initPallete(Bitmap bmp) {
            this.palette = Palette.from(bmp).generate();
            if (BokehDrawable.this.mDrawFlare) {
                int[] positions = BokehHelper.getBokehPositionList(bmp);
                if (positions != null) {
                    int bitmapWidth = bmp.getWidth();
                    for (int position : positions) {
                        this.pos.add(new Pos(position % bitmapWidth, position / bitmapWidth, (((float) (Math.random() + 0.75d)) * 2.25f) * (((float) bitmapWidth) / 1080.0f)));
                    }
                }
            }
        }

        protected void onPostExecute(Void aVoid) {
            setbackData();
        }

        private void setbackData() {
            Log.w("BokehDrawable", "onPostExecute " + this.blurBitmap + ", " + this.pos);
            if (BokehDrawable.this.mDrawBlur) {
                BokehDrawable.this.mBlurBitmap = this.blurBitmap;
            }
            if (BokehDrawable.this.mDrawFlare) {
                filters = new ColorMatrixColorFilter[3];
                bmpFlares = new Bitmap[3];
                Resources rs = BokehDrawable.this.mContext.getResources();
                bmpFlares[0] = BitmapFactory.decodeResource(rs, R$drawable.bokeh_a);
                bmpFlares[1] = BitmapFactory.decodeResource(rs, R$drawable.bokeh_b);
                bmpFlares[2] = BitmapFactory.decodeResource(rs, R$drawable.bokeh_c);
                filters[0] = BokehHelper.createColorMatrix(this.palette.getVibrantColor(-1));
                filters[1] = BokehHelper.createColorMatrix(this.palette.getLightVibrantColor(-1));
                filters[2] = BokehHelper.createColorMatrix(this.palette.getDarkVibrantColor(-1));
                BokehDrawable.this.mColorFilters = filters;
                BokehDrawable.this.mPositions = this.pos;
                BokehDrawable.this.mBmpFlares = bmpFlares;
            }
            noticePictureParsed();
            BokehDrawable.this.invalidateSelf();
        }

        private void noticePictureParsed() {
            if (KeyguardCfg.isCurveScreen(BokehDrawable.this.mContext)) {
                int darkVibrantColor;
                int cType = BokehDrawable.this.getAvgColor();
                int defColor = cType == 2 ? -1077952577 : -1090486785;
                BokehDrawable bokehDrawable = BokehDrawable.this;
                if (this.palette != null) {
                    darkVibrantColor = this.palette.getDarkVibrantColor(defColor);
                } else {
                    darkVibrantColor = defColor;
                }
                bokehDrawable.mVibColor = darkVibrantColor;
                if (BokehDrawable.this.mVibColor != defColor && (BokehDrawable.this.mVibColor & -16777216) < 0) {
                    BokehDrawable.this.mVibColor = (BokehDrawable.this.mVibColor & 16777215) | -1090519040;
                }
                HwLog.w("BokehDrawable", "cType is " + cType + "; defcolor: " + Integer.toHexString(defColor) + " vibColor: " + Integer.toHexString(BokehDrawable.this.mVibColor));
                AppHandler.sendMessage(23, BokehDrawable.this.mVibColor, 0, null);
            }
        }
    }

    private static class Pos {
        int colorIndex = KeyguardUtils.nextInt(3);
        int flareType = KeyguardUtils.nextInt(3);
        float scale;
        int x;
        int y;

        Pos(int x, int y, float scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }

    public Bitmap getBlurBitmap() {
        return this.mBlurBitmap;
    }

    public BokehDrawable(Context ctx, Bitmap bitmap) {
        this(ctx, ctx.getResources(), bitmap);
    }

    public BokehDrawable(Context ctx, Resources resources, Bitmap bitmap) {
        super(resources, bitmap);
        this.mDrawBlur = true;
        this.mDrawFlare = false;
        this.mPaint = new Paint();
        this.mBmpFlares = null;
        this.mPositions = new ArrayList();
        this.mBlurBitmap = null;
        this.mInDrawing = false;
        this.mBlurAlpha = 0;
        this.mVibColor = 0;
        this.mWallPaperColorsInfo = null;
        this.mBitmapPath = null;
        this.mContext = ctx;
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
        this.mSetBitmapMethod = getSetBitmapMethod();
    }

    public SparseIntArray getColorInfo() {
        return this.mWallPaperColorsInfo;
    }

    public int getAvgColor() {
        if (this.mWallPaperColorsInfo != null) {
            return this.mWallPaperColorsInfo.get(5, 2);
        }
        return 2;
    }

    public int getTouchColor() {
        int i = -1077952577;
        if (this.mVibColor != -1077952577 && this.mVibColor != -1090486785) {
            return this.mVibColor;
        }
        int cType = getAvgColor();
        HwLog.w("BokehDrawable", "defColor from " + cType);
        if (cType != 2) {
            i = -1090486785;
        }
        return i;
    }

    public float getBokehValue() {
        return this.mFraction;
    }

    public void setBokehValue(float bokehValue) {
        if (this.mDrawBlur) {
            if (bokehValue < 0.0f) {
                bokehValue = 0.0f;
            } else if (bokehValue > 1.0f) {
                bokehValue = 1.0f;
            }
            this.mFraction = bokehValue;
            if (this.mFraction > 0.999f) {
                this.mFraction = 1.0f;
            } else if (this.mFraction < 0.001f) {
                this.mFraction = 0.0f;
            }
            this.mBlurAlpha = (int) (this.mFraction * 255.0f);
        }
    }

    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        setBokehValue(((float) level) / 100.0f);
        return true;
    }

    public void draw(Canvas canvas) {
        if (this.mDrawFlare || this.mDrawBlur) {
            this.mInDrawing = true;
            canvas.save();
            Bitmap bmp = getBitmap();
            float scale = BOKEH_SCALE_FROM + ((BOKEH_SCALE_TO - BOKEH_SCALE_FROM) * this.mFraction);
            canvas.scale(scale, scale, (float) (bmp.getWidth() >> 1), (float) (bmp.getHeight() >> 1));
            if (this.mFraction < 0.999f || this.mBlurBitmap == null) {
                super.draw(canvas);
            }
            if (this.mBlurBitmap == null) {
                this.mInDrawing = false;
                drawScrim(canvas);
                return;
            }
            if (this.mFraction > 0.001f) {
                if (this.mDrawBlur) {
                    drawBlur(canvas);
                }
                if (this.mDrawFlare) {
                    drawFlares(canvas, this.mFraction);
                }
            }
            drawScrim(canvas);
            return;
        }
        super.draw(canvas);
        canvas.save();
        drawScrim(canvas);
    }

    private void drawScrim(Canvas canvas) {
        boolean drawMask;
        int colorB;
        canvas.restore();
        int style = KeyguardTheme.getInst().getLockStyle();
        if (style == 7 || style == 8) {
            drawMask = true;
        } else {
            drawMask = false;
        }
        int color = drawMask ? 436207616 : 0;
        if (this.mBlurAlpha > 1) {
            colorB = (this.mBlurAlpha >>> 1) << 24;
        } else {
            colorB = 0;
        }
        if (colorB > color) {
            color = colorB;
        }
        if (color != 0) {
            canvas.drawColor(color);
        }
        this.mInDrawing = false;
    }

    private void drawFlares(Canvas canvas, float fraction) {
        if (this.mPositions.size() > 0) {
            canvas.save();
            this.mPaint.setAlpha(this.mBlurAlpha);
            for (int i = 0; i < this.mPositions.size(); i++) {
                this.mPaint.setColorFilter(this.mColorFilters[((Pos) this.mPositions.get(i)).colorIndex]);
                int size = (int) (((((Pos) this.mPositions.get(i)).scale * ((float) this.mBmpFlares[0].getWidth())) / 12.0f) * ((fraction * 0.5f) + 0.5f));
                int xPos = ((Pos) this.mPositions.get(i)).x - (size / 2);
                int yPos = ((Pos) this.mPositions.get(i)).y - (size / 2);
                canvas.drawBitmap(this.mBmpFlares[((Pos) this.mPositions.get(i)).flareType], null, new Rect(xPos, yPos, xPos + size, yPos + size), this.mPaint);
            }
            this.mPaint.setColorFilter(null);
            canvas.restore();
        }
    }

    private void drawBlur(Canvas canvas) {
        if (this.mBlurBitmap == null) {
            Log.i("BokehDrawable", "skip drawBlur");
            return;
        }
        Bitmap bmp = getBitmap();
        int alpha = getAlpha();
        setBitmapInner(this.mBlurBitmap, this.mBlurAlpha);
        super.draw(canvas);
        setBitmapInner(bmp, alpha);
    }

    public Method getSetBitmapMethod() {
        Method method = null;
        try {
            method = BitmapDrawable.class.getDeclaredMethod("setBitmap", new Class[]{Bitmap.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public void invalidateSelf() {
        if (!this.mInDrawing) {
            super.invalidateSelf();
        }
    }

    private void setBitmapInner(Bitmap bmp, int alpha) {
        try {
            this.mSetBitmapMethod.invoke(this, new Object[]{bmp});
            setAlpha(alpha);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        }
    }

    private void initFlare(Bitmap bmp, boolean async) {
        if (async) {
            new BokehTask().executeOnExecutor(GlobalContext.getSerialExecutor(), new Bitmap[]{bmp});
            return;
        }
        new BokehTask().initData(bmp).setbackData();
    }

    public static final BokehDrawable create(Context context, Bitmap bmp) {
        return create(context, bmp, true, false);
    }

    public static final BokehDrawable create(Context context, Bitmap bmp, boolean withFlare) {
        return create(context, bmp, true, true);
    }

    public static final BokehDrawable create(Context context, Bitmap bmp, boolean withFlare, boolean async) {
        if (bmp == null || bmp.isRecycled()) {
            return null;
        }
        BokehDrawable retDrawable = new BokehDrawable(context, bmp);
        if (withFlare) {
            retDrawable.initFlare(bmp, async);
        }
        return retDrawable;
    }

    public void releaseFyuseRotateBitmap() {
        if (HwFyuseUtils.isSupport3DFyuse() && HwFyuseUtils.isFyuseTypeFile(this.mBitmapPath)) {
            Bitmap bitmap = getBitmap();
            if (!(bitmap == null || bitmap.isRecycled())) {
                bitmap.recycle();
            }
        }
    }

    public void setBitmapPath(String bitmapPath) {
        this.mBitmapPath = bitmapPath;
    }

    public boolean isFyuseDrawble() {
        if (TextUtils.isEmpty(this.mBitmapPath) || !HwFyuseUtils.isSupport3DFyuse()) {
            return false;
        }
        return HwFyuseUtils.isFyuseTypeFile(this.mBitmapPath);
    }
}
