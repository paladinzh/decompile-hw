package com.android.mms.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.layout.LayoutManager;
import com.android.mms.model.ImageModel;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwSimpleImageLoader;
import com.huawei.mms.util.ResEx;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GifView extends ImageView {
    private static final int SLIDESHOW_BOUNDS_LIMIT = MmsConfig.getMaxImageWidth();
    private static String TAG = "GifView";
    private Runnable mDelayInvalidater = new Runnable() {
        public void run() {
            if (!GifView.this.mIsPaused) {
                GifView.this.invalidate();
            }
        }
    };
    private boolean mGifImgNeedAddCorner = false;
    private boolean mIsGifIMG;
    private boolean mIsMeasured;
    private boolean mIsPaused;
    private Movie mMovie;
    private int mMovieHeight = 0;
    private long mMovieStart;
    private int mMovieWidth = 0;
    private Path mPath = null;
    private float mScale = ContentUtil.FONT_SIZE_NORMAL;
    private int mScreenHeight;
    private int mScreenWidth;
    private float mStartX = 0.0f;
    private float mStartY = 0.0f;
    int mTargetHeight = 0;
    int mTargetWidth = 0;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageBitmap(Bitmap bm) {
        setImageBitmap(bm, 16.0f);
    }

    public void setImageBitmap(Bitmap bm, float cornerRadius) {
        resetView();
        if (bm != null) {
            super.setImageBitmap(ResEx.getRoundedCornerBitmap(bm, (float) MessageUtils.dipToPx(this.mContext, cornerRadius)));
        }
    }

    public void setImageModel(ImageModel imageModel) {
        Pair<Integer, Integer> scaleLimit = getSlideSmoothScaleLimt(imageModel);
        if (scaleLimit != null) {
            setImageBitmap(imageModel.getBitmap(((Integer) scaleLimit.first).intValue(), ((Integer) scaleLimit.second).intValue()), 0.0f);
        }
    }

    public boolean setGifImage(Uri uri, boolean needAddCorner) {
        resetView();
        if (uri == null) {
            return false;
        }
        setLayerType(1, null);
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getContentResolver().openInputStream(uri);
            byte[] array = streamToBytes(inputStream);
            this.mMovie = Movie.decodeByteArray(array, 0, array.length);
            this.mMovieWidth = this.mMovie.width();
            this.mMovieHeight = this.mMovie.height();
            if (this.mMovieWidth <= 0 || this.mMovieHeight <= 0) {
                super.setImageURI(uri);
            } else {
                int[] widthAndHeight = MessageUtils.getImgWidthAndHeight(this.mMovieWidth, this.mMovieHeight, getContext());
                this.mTargetWidth = widthAndHeight[0];
                this.mTargetHeight = widthAndHeight[1];
                this.mIsGifIMG = true;
                this.mIsPaused = false;
                this.mGifImgNeedAddCorner = needAddCorner;
                this.mPath = getGifCanvasClipPath();
                super.setImageBitmap(Bitmap.createBitmap(this.mTargetWidth, this.mTargetHeight, Config.ALPHA_8));
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    MLog.e(TAG, "setGifImage: failed colse the inputStream");
                }
            }
            return true;
        } catch (RuntimeException e2) {
            MLog.e(TAG, "has a runtime exception");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    MLog.e(TAG, "setGifImage: failed colse the inputStream");
                }
            }
            return false;
        } catch (Exception e4) {
            MLog.e(TAG, "can't parse the uri :uri");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    MLog.e(TAG, "setGifImage: failed colse the inputStream");
                }
            }
            return false;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    MLog.e(TAG, "setGifImage: failed colse the inputStream");
                }
            }
        }
    }

    public boolean setGifImage(Uri uri) {
        return setGifImage(uri, false);
    }

    public boolean setOriginalGifImage(Uri uri) {
        resetView();
        if (uri == null) {
            return false;
        }
        setLayerType(1, null);
        InputStream inputStream = null;
        try {
            float scale;
            inputStream = this.mContext.getContentResolver().openInputStream(uri);
            byte[] array = streamToBytes(inputStream);
            this.mMovie = Movie.decodeByteArray(array, 0, array.length);
            this.mMovieWidth = this.mMovie.width();
            this.mMovieHeight = this.mMovie.height();
            float scale_w = ((float) SLIDESHOW_BOUNDS_LIMIT) / ((float) this.mMovieWidth);
            float scale_h = ((float) SLIDESHOW_BOUNDS_LIMIT) / ((float) this.mMovieHeight);
            if (scale_w < scale_h) {
                scale = scale_w;
            } else {
                scale = scale_h;
            }
            if (((double) scale) > WeightedLatLng.DEFAULT_INTENSITY) {
                scale = ContentUtil.FONT_SIZE_NORMAL;
            }
            this.mMovieWidth = (int) (((float) this.mMovieWidth) * scale);
            this.mMovieHeight = (int) (((float) this.mMovieHeight) * scale);
            this.mGifImgNeedAddCorner = false;
            if (this.mMovieWidth <= 0 || this.mMovieHeight <= 0) {
                super.setImageURI(uri);
            } else {
                this.mIsGifIMG = true;
                this.mIsPaused = false;
                super.setImageBitmap(Bitmap.createBitmap(this.mMovieWidth, this.mMovieHeight, Config.ALPHA_8));
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    MLog.e(TAG, "can't close InputStream :is");
                }
            }
            return true;
        } catch (Exception e2) {
            MLog.e(TAG, "can't parse the uri :uri");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    MLog.e(TAG, "can't close InputStream :is");
                }
            }
            return false;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    MLog.e(TAG, "can't close InputStream :is");
                }
            }
        }
    }

    private byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(Place.TYPE_SUBLOCALITY_LEVEL_2);
        byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
        while (true) {
            try {
                int len = is.read(buffer);
                if (len < 0) {
                    break;
                }
                os.write(buffer, 0, len);
            } catch (IOException e) {
            }
        }
        return os.toByteArray();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mScreenWidth = getMeasuredWidth();
        this.mScreenHeight = getMeasuredHeight();
        if (this.mIsGifIMG && this.mIsMeasured) {
            measureScale();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mIsGifIMG) {
            super.onDraw(canvas);
        } else if (!this.mIsGifIMG || this.mIsMeasured) {
            if (this.mGifImgNeedAddCorner) {
                if (this.mPath == null) {
                    this.mPath = getGifCanvasClipPath();
                }
                canvas.clipPath(this.mPath);
            }
            long now = SystemClock.uptimeMillis();
            if (this.mMovieStart == 0) {
                this.mMovieStart = now;
            }
            int dur = this.mMovie.duration();
            if (dur == 0) {
                dur = AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST;
            }
            this.mMovie.setTime((int) ((now - this.mMovieStart) % ((long) dur)));
            if (this.mScale != ContentUtil.FONT_SIZE_NORMAL) {
                canvas.scale(this.mScale, this.mScale);
            }
            if (this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mMovie.draw(canvas, this.mStartX, this.mStartY);
            } else {
                int saveCount = canvas.getSaveCount();
                canvas.save();
                canvas.translate(((float) this.mPaddingLeft) / this.mScale, ((float) this.mPaddingTop) / this.mScale);
                this.mMovie.draw(canvas, this.mStartX, this.mStartY);
                canvas.restoreToCount(saveCount);
            }
            if (!this.mIsPaused) {
                postDelayed(this.mDelayInvalidater, 100);
            }
        } else {
            measureScale();
            this.mIsMeasured = true;
            invalidate();
        }
    }

    private Path getGifCanvasClipPath() {
        RectF rectF = new RectF(new Rect(0, 0, this.mTargetWidth, this.mTargetHeight));
        int roundPx = MessageUtils.dipToPx(getContext(), 16.0f);
        Path path = new Path();
        path.addRoundRect(rectF, (float) roundPx, (float) roundPx, Direction.CCW);
        return path;
    }

    private void measureScale() {
        int drawWidth = (this.mScreenWidth - this.mPaddingLeft) - this.mPaddingRight;
        int drawHeight = (this.mScreenHeight - this.mPaddingTop) - this.mPaddingBottom;
        this.mMovieWidth = this.mMovie.width();
        this.mMovieHeight = this.mMovie.height();
        if (!(drawWidth == 0 || drawHeight == 0 || this.mMovieWidth == 0 || this.mMovieHeight == 0)) {
            this.mScale = Math.max(((float) drawHeight) / ((float) this.mMovieHeight), ((float) drawWidth) / ((float) this.mMovieWidth));
        }
        if (this.mScale != 0.0f) {
            this.mStartX = ((((float) drawWidth) - (((float) this.mMovieWidth) * this.mScale)) / 2.0f) / this.mScale;
            this.mStartY = ((((float) drawHeight) - (((float) this.mMovieHeight) * this.mScale)) / 2.0f) / this.mScale;
            return;
        }
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
    }

    public void resetView() {
        super.setImageDrawable(null);
        this.mMovie = null;
        this.mIsGifIMG = false;
        this.mIsPaused = true;
        this.mIsMeasured = false;
        this.mMovieWidth = 0;
        this.mMovieHeight = 0;
        this.mScreenWidth = 0;
        this.mScreenHeight = 0;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mIsGifIMG) {
            if (visibility == 0) {
                this.mIsPaused = false;
                invalidate();
            } else {
                this.mIsPaused = true;
            }
        }
    }

    public Pair<Integer, Integer> getSlideSmoothScaleLimt(ImageModel imageModel) {
        if (imageModel == null) {
            return null;
        }
        float scale;
        int width = imageModel.getWidth();
        int height = imageModel.getHeight();
        int limit_w = LayoutManager.getInstance().getLayoutParameters().getImageWidth();
        int limit_h = LayoutManager.getInstance().getLayoutParameters().getImageHeight();
        int slideshow_bounds_limit = limit_w < limit_h ? limit_w : limit_h;
        int slideshow_bounds_limit_max = MessageUtils.dipToPx(this.mContext, 240.0f);
        if (slideshow_bounds_limit > slideshow_bounds_limit_max) {
            slideshow_bounds_limit = slideshow_bounds_limit_max;
        }
        int slideshow_bounds_limit_min = getDisplsyPxByDensity(this.mContext, SLIDESHOW_BOUNDS_LIMIT);
        if (slideshow_bounds_limit < slideshow_bounds_limit_min) {
            slideshow_bounds_limit = slideshow_bounds_limit_min;
        }
        float scale_w = ((float) slideshow_bounds_limit) / ((float) width);
        float scale_h = ((float) slideshow_bounds_limit) / ((float) height);
        if (scale_w < scale_h) {
            scale = scale_w;
        } else {
            scale = scale_h;
        }
        if (((double) scale) > WeightedLatLng.DEFAULT_INTENSITY) {
            scale = ContentUtil.FONT_SIZE_NORMAL;
        }
        return new Pair(Integer.valueOf((int) (((float) width) * scale)), Integer.valueOf((int) (((float) height) * scale)));
    }

    private int getDisplsyPxByDensity(Context context, int display) {
        if (context == null) {
            return display;
        }
        float curDislpayDensity = context.getResources().getDisplayMetrics().density;
        if (curDislpayDensity == 3.0f) {
            return display;
        }
        return (int) ((((float) display) * curDislpayDensity) / 3.0f);
    }

    public void loadImageAsync(ImageModel imageModel, HwSimpleImageLoader imageLoader, ItemLoadedCallback<ImageLoaded> callback) {
        Pair<Integer, Integer> scaleLimit = getSlideSmoothScaleLimt(imageModel);
        if (scaleLimit != null && imageLoader != null) {
            imageLoader.loadImage(imageModel.getUri(), false, callback, ((Integer) scaleLimit.first).intValue(), ((Integer) scaleLimit.second).intValue());
        }
    }
}
