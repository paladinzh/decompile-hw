package com.android.contacts.widget;

import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.contacts.R$styleable;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.android.app.WallpaperManagerEx;
import com.huawei.cspcommon.util.WallPaperImageHelper;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DialpadHeaderLayout extends RelativeLayout implements IBlurWallpaperCallback {
    private static final String TAG = DialpadHeaderLayout.class.getSimpleName();
    private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS, sWorkQueue, sThreadFactory, new DiscardOldestPolicy());
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + this.mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue(10);
    private int mBackground_change = 1;
    private int mBgRect_bottom = 0;
    private int mBgRect_left = 0;
    private int mBgRect_right = 0;
    private int mBgRect_top = 0;
    private int mBlurBottom = -1;
    private int mBlurLeft = -1;
    private int mBlurRight = -1;
    private int mBlurTop = -1;
    private Handler mHandler;
    private LayoutChangeListener mLayoutChangeListener;
    private float mLeftBottomRadius = 0.0f;
    private float mLeftTopRadius = 0.0f;
    private Rect mRect;
    private float mRightBottomRadius = 0.0f;
    private float mRightTopRadius = 0.0f;
    private WallpaperManager mWallpaperManager;

    private class BlurWallPaperBgTask extends AsyncTask<Void, Void, Drawable> {
        private BlurWallPaperBgTask() {
        }

        protected void onPostExecute(Drawable result) {
            if (result != null) {
                DialpadHeaderLayout.this.setBackground(result);
            } else {
                HwLog.e(DialpadHeaderLayout.TAG, "cann't get background bitmap!!");
            }
        }

        protected Drawable doInBackground(Void... bitmap) {
            return DialpadHeaderLayout.this.setBlurWallpaperBackground();
        }
    }

    public interface LayoutChangeListener {
        void onLayoutChange(int i, int i2, int i3, int i4);
    }

    public DialpadHeaderLayout(Context context) {
        super(context);
        init(context, null);
    }

    public DialpadHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DialpadHeaderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (EmuiVersion.isSupportEmui()) {
            this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
            WallpaperManagerEx.setCallback(this.mWallpaperManager, this);
        }
        initHandler(getContext().getMainLooper());
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.RoundRect);
            this.mLeftTopRadius = typedArray.getDimension(0, 0.0f);
            this.mLeftBottomRadius = typedArray.getDimension(1, 0.0f);
            this.mRightTopRadius = typedArray.getDimension(2, 0.0f);
            this.mRightBottomRadius = typedArray.getDimension(3, 0.0f);
            typedArray.recycle();
            typedArray = context.obtainStyledAttributes(attrs, R$styleable.DialpadHeaderLayout);
            this.mBlurTop = typedArray.getDimensionPixelSize(0, -1);
            this.mBlurBottom = typedArray.getDimensionPixelSize(1, -1);
            this.mBlurLeft = typedArray.getDimensionPixelSize(2, -1);
            this.mBlurRight = typedArray.getDimensionPixelSize(3, -1);
            this.mBgRect_top = typedArray.getDimensionPixelSize(6, 0);
            this.mBgRect_bottom = typedArray.getDimensionPixelSize(7, 0);
            this.mBgRect_left = typedArray.getDimensionPixelSize(4, 0);
            this.mBgRect_right = typedArray.getDimensionPixelSize(5, 0);
            this.mBackground_change = typedArray.getColor(8, 1);
            if (!EmuiVersion.isSupportEmui()) {
                this.mBackground_change = 0;
            }
            if (CommonUtilMethods.isLargeThemeApplied(this.mBackground_change)) {
                setBackground(typedArray.getDrawable(9));
            }
            typedArray.recycle();
        }
    }

    private static boolean rectEquals(Rect src, Rect dest) {
        boolean z = true;
        if (src == null || dest == null) {
            return src == null && dest == null;
        } else {
            if (src.left != dest.left || src.right != dest.right || src.top != dest.top) {
                z = false;
            } else if (src.bottom != dest.bottom) {
                z = false;
            }
            return z;
        }
    }

    private Rect getImageRect() {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        int left = this.mBlurLeft != -1 ? this.mBlurLeft : loc[0];
        int top = this.mBlurTop != -1 ? this.mBlurTop : loc[1];
        int right = this.mBlurRight != -1 ? this.mBlurRight : left + getWidth();
        int bottom = this.mBlurBottom != -1 ? this.mBlurBottom : top + getHeight();
        if (isSpecifiedBgRect((float) this.mBgRect_left, (float) this.mBgRect_right, (float) this.mBgRect_top, (float) this.mBgRect_bottom)) {
            return new Rect(this.mBgRect_left, this.mBgRect_top, this.mBgRect_right, this.mBgRect_bottom);
        }
        return new Rect(left, top, right, bottom);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized Drawable setBlurWallpaperBackground() {
        if (EmuiVersion.isSupportEmui()) {
            Drawable drawable = getBackground();
            getLocationOnScreen(new int[2]);
            Rect rect = getImageRect();
            if ((!rectEquals(this.mRect, rect) || drawable == null) && rect.width() > 0 && rect.height() > 0) {
                this.mRect = rect;
                int colorValue = ImmersionUtils.getPrimaryColor(getContext());
                if (colorValue != -999) {
                    if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
                        drawable = new ColorDrawable(17170445);
                    } else {
                        drawable = new ColorDrawable(colorValue);
                    }
                }
            }
        } else {
            return new ColorDrawable(getResources().getColor(R.color.contact_default_header_background));
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        synchronized (this) {
                            DialpadHeaderLayout.this.mRect = null;
                            DialpadHeaderLayout.this.getBackGroundBitmap(false);
                            DialpadHeaderLayout.this.invalidate();
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void onBlurWallpaperChanged() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !CommonUtilMethods.isLargeThemeApplied(this.mBackground_change)) {
            getBackGroundBitmap(true);
        }
        if (changed && this.mLayoutChangeListener != null) {
            this.mLayoutChangeListener.onLayoutChange(l, t, r, b);
        }
    }

    private void getBackGroundBitmap(boolean canGetFromCache) {
        if (EmuiVersion.isSupportEmui()) {
            Rect rect = getImageRect();
            if (!rectEquals(this.mRect, rect) && rect.width() > 0 && rect.height() > 0) {
                if (canGetFromCache) {
                    Bitmap bitmap = WallPaperImageHelper.getInstance(getContext()).getBitmapOnlyInCache(rect, this.mLeftTopRadius, this.mLeftBottomRadius, this.mRightTopRadius, this.mRightBottomRadius);
                    if (bitmap != null) {
                        this.mRect = rect;
                        setBackground(new BitmapDrawable(getResources(), bitmap));
                    } else if (WallPaperImageHelper.isRectNoRadiusBorder(this.mLeftTopRadius, this.mLeftBottomRadius, this.mRightTopRadius, this.mRightBottomRadius)) {
                        setBackground(setBlurWallpaperBackground());
                        this.mRect = rect;
                    } else {
                        new BlurWallPaperBgTask().executeOnExecutor(sExecutor, new Void[0]);
                    }
                } else if (WallPaperImageHelper.isRectNoRadiusBorder(this.mLeftTopRadius, this.mLeftBottomRadius, this.mRightTopRadius, this.mRightBottomRadius)) {
                    setBackground(setBlurWallpaperBackground());
                    this.mRect = rect;
                } else {
                    new BlurWallPaperBgTask().executeOnExecutor(sExecutor, new Void[0]);
                }
            }
            return;
        }
        setBackgroundColor(getResources().getColor(R.color.contact_default_header_background));
    }

    private boolean isSpecifiedBgRect(float left, float right, float top, float bottom) {
        if (left == 0.0f && right == 0.0f && top == 0.0f && bottom == 0.0f) {
            return false;
        }
        return true;
    }
}
