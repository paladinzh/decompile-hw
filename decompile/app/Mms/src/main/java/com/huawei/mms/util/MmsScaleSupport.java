package com.huawei.mms.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ui.PreferenceUtils;
import com.huawei.mms.ui.SpandTextView;
import java.util.List;

@SuppressLint({"NewApi"})
public class MmsScaleSupport implements OnScaleGestureListener {
    private Context mContext;
    private float mCurrentScale = ContentUtil.FONT_SIZE_NORMAL;
    private long mLastScale = 0;
    private SacleListener mListener;
    private float mScaleLarge = 3.0f;
    private float mScaleSmall = 0.7f;

    public interface SacleListener {
        void onScaleChanged(float f);
    }

    public static class MmsScaleHandler {
        private ScaleGestureDetector mDetector;
        private MmsScaleSupport mListener;

        private MmsScaleHandler(ScaleGestureDetector detector, MmsScaleSupport listener) {
            this.mDetector = detector;
            this.mListener = listener;
        }

        public float getFontScale() {
            return this.mListener.mCurrentScale;
        }

        public void setFontScale(float scale) {
            this.mListener.mCurrentScale = scale;
        }

        public boolean onTouchEvent(MotionEvent event) {
            return this.mDetector.onTouchEvent(event);
        }

        public static MmsScaleHandler create(Context context, SacleListener slistener) {
            MmsScaleSupport listener = new MmsScaleSupport(context, slistener);
            return new MmsScaleHandler(new ScaleGestureDetector(context, listener), listener);
        }
    }

    public static class ScalableTextView {
        private float mOriginFontSzie = 0.0f;
        private TextView mRefTextView;

        public ScalableTextView(TextView r) {
            this.mRefTextView = r;
            this.mOriginFontSzie = r.getTextSize();
        }

        public static ScalableTextView create(View r) {
            return new ScalableTextView((TextView) r);
        }

        public TextView get() {
            return this.mRefTextView;
        }

        public void setText(CharSequence text) {
            this.mRefTextView.setText(text);
        }

        public void setText(CharSequence text, List<TextSpan> textSpan) {
            if (this.mRefTextView instanceof SpandTextView) {
                ((SpandTextView) this.mRefTextView).setText(text, (List) textSpan);
            }
        }

        public void setVisibility(int visibility) {
            this.mRefTextView.setVisibility(visibility);
        }

        public void setPressed(boolean pressed) {
            this.mRefTextView.setPressed(pressed);
        }

        public void setTextSize(float size) {
            this.mOriginFontSzie = size;
            this.mRefTextView.setTextSize(size);
        }

        public CharSequence getText() {
            return this.mRefTextView.getText();
        }
    }

    public MmsScaleSupport(Context context, SacleListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mCurrentScale = PreferenceUtils.getPreferenceFloat(context, "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        onConfigurationChanged(context.getResources().getConfiguration());
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    public boolean onScale(ScaleGestureDetector detector) {
        if (this.mListener == null) {
            return true;
        }
        float scChange = detector.getScaleFactor();
        if (scChange > 1.1f) {
            scChange = 1.1f;
        } else if (scChange < 0.9f) {
            scChange = 0.9f;
        }
        if (Math.abs(scChange - ContentUtil.FONT_SIZE_NORMAL) < 0.04f) {
            return false;
        }
        long current = System.currentTimeMillis();
        if (((float) this.mLastScale) + 300.0f > ((float) current)) {
            return false;
        }
        this.mLastScale = current;
        this.mCurrentScale *= scChange;
        if (this.mCurrentScale > this.mScaleLarge) {
            this.mCurrentScale = this.mScaleLarge;
        } else if (this.mCurrentScale < this.mScaleSmall) {
            this.mCurrentScale = this.mScaleSmall;
        } else if (Float.isNaN(this.mCurrentScale)) {
            this.mCurrentScale = ContentUtil.FONT_SIZE_NORMAL;
        }
        this.mListener.onScaleChanged(this.mCurrentScale);
        PreferenceUtils.saveFontScale(this.mContext, this.mCurrentScale);
        return true;
    }

    public void onConfigurationChanged(Configuration config) {
        float scale = config.fontScale;
        this.mScaleLarge = 3.0f / scale;
        this.mScaleSmall = 0.7f / scale;
    }
}
