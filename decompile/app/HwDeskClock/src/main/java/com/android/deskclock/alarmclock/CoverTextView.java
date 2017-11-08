package com.android.deskclock.alarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.Utils;
import com.huawei.cust.HwCustUtils;

public class CoverTextView extends TextView {
    private String drawStr;
    private float dx;
    private float dy;
    private FontMetrics fm;
    private float fmHeight;
    private int height;
    private long lastTime;
    private int[] mColorsLand;
    private Handler mHandler;
    private LinearInterpolator mLinearInterpolator;
    private int mOrientation;
    private Paint mPaint;
    private boolean mStart;
    private Matrix matrix;
    private Shader shader;
    private int width;

    private void init() {
        this.mPaint.setTextAlign(Align.CENTER);
        float coverCloseTextSize = (float) getResources().getDimensionPixelSize(R.dimen.cover_close_textSize);
        HwCustCoverAdapter mCover = (HwCustCoverAdapter) HwCustUtils.createObj(HwCustCoverAdapter.class, new Object[0]);
        if (mCover != null && mCover.isAdapterCoverEnable()) {
            coverCloseTextSize = mCover.getCoverCloseTextSize(getContext(), R.dimen.cover_close_textSize);
        }
        this.mPaint.setTextSize(coverCloseTextSize);
        this.mPaint.setTypeface(Utils.getmRobotoXianBlackTypeface());
        this.mPaint.setColor(getResources().getColor(R.color.tips_clock_closealarm));
        this.mPaint.setShader(this.shader);
        this.drawStr = getResources().getString(R.string.tips_clock_closealarm);
        this.fm = this.mPaint.getFontMetrics();
        this.fmHeight = (this.fm.ascent + this.fm.descent) / 2.0f;
        this.height = getHeight();
        this.width = getWidth();
        ellipsizeDrawStr();
    }

    public CoverTextView(Context context) {
        this(context, null);
    }

    public CoverTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        this.mPaint = new Paint();
        this.dx = 0.0f;
        this.lastTime = -1;
        this.mStart = true;
        this.matrix = new Matrix();
        this.mColorsLand = new int[]{getResources().getColor(R.color.tips_clock_closealarm), getResources().getColor(R.color.tips_clock_closealarm), getResources().getColor(R.color.tips_clock_closealarm_light)};
        this.shader = new LinearGradient(0.0f, 0.0f, 200.0f, 0.0f, this.mColorsLand, new float[]{0.0f, 0.7f, 1.0f}, TileMode.MIRROR);
        this.mOrientation = 0;
        this.dy = 0.0f;
        this.mLinearInterpolator = new LinearInterpolator();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if (CoverTextView.this.mHandler.hasMessages(1)) {
                            CoverTextView.this.mHandler.removeMessages(1);
                        }
                        CoverTextView.this.invalidate();
                        return;
                    default:
                        return;
                }
            }
        };
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.SliderView, defStyle, 0);
        try {
            this.mOrientation = typedArray.getInt(0, 0);
            if (this.mOrientation == 0) {
                init();
            }
        } finally {
            typedArray.recycle();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w >> 1;
        this.height = h >> 1;
        if (this.mOrientation == 0) {
            this.drawStr = getResources().getString(R.string.tips_clock_closealarm);
            ellipsizeDrawStr();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void ellipsizeDrawStr() {
        if (!(this.mPaint == null || this.drawStr == null || getWidth() == 0 || ((int) this.mPaint.measureText(this.drawStr)) <= getWidth())) {
            this.drawStr = (String) TextUtils.ellipsize(this.drawStr, getPaint(), (float) getWidth(), TruncateAt.END);
        }
    }

    protected void onDraw(Canvas canvas) {
        long now = System.currentTimeMillis();
        if (this.lastTime == -1) {
            this.lastTime = now;
        }
        if (this.mOrientation == 0) {
            float dt = ((float) ((now - this.lastTime) % 3000)) / 3000.0f;
            this.dx = ((float) this.width) + (this.mLinearInterpolator.getInterpolation(dt) * ((float) (((int) this.mPaint.measureText(this.drawStr)) + 40)));
            this.dy = 0.0f;
            if (this.mStart) {
                this.matrix.setTranslate(this.dx, this.dy);
                this.mHandler.sendEmptyMessageDelayed(1, 60);
            } else {
                this.matrix.setTranslate(0.0f, 0.0f);
            }
            this.shader.setLocalMatrix(this.matrix);
            canvas.drawText(this.drawStr, (float) this.width, ((float) this.height) - this.fmHeight, this.mPaint);
        }
        super.onDraw(canvas);
    }
}
