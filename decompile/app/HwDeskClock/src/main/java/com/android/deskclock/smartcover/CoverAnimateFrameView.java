package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.deskclock.R;
import com.android.util.Log;

public class CoverAnimateFrameView extends FrameLayout {
    private int mAnimationHeight;
    private SmartCoverAnimateTextView mAnimationText;
    private int mAnimationWidth;
    private LayoutParams mFrameLayoutParams;
    private int mOffsetX;
    private float mStartX;
    private Handler mainHandler;

    public CoverAnimateFrameView(Context context) {
        this(context, null);
    }

    public CoverAnimateFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverAnimateFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mainHandler = null;
        this.mAnimationWidth = 900;
        this.mStartX = 0.0f;
        this.mOffsetX = 0;
        this.mAnimationHeight = 210;
        this.mFrameLayoutParams = new LayoutParams(-1, -1);
        LayoutInflater.from(context).inflate(R.layout.cover_animation, this, true);
        this.mAnimationText = (SmartCoverAnimateTextView) findViewById(R.id.coveranimationtv);
        Typeface typeFace = Util.getTextViewTypeFace();
        if (!(typeFace == null || this.mAnimationText == null)) {
            Log.d("CoverAnimateFrameView", "mAnimationText setTypeface");
            this.mAnimationText.setTypeface(typeFace);
        }
        this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.5f);
    }

    public void startAnimal() {
        if (this.mAnimationText != null) {
            this.mAnimationText.startAnimator();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        switch (event.getAction()) {
            case 0:
                this.mStartX = eventX;
                break;
            case 1:
                this.mOffsetX = (int) (eventX - this.mStartX);
                if (this.mOffsetX <= this.mAnimationWidth) {
                    updateStart(0, 0);
                    break;
                }
                this.mainHandler.obtainMessage(2).sendToTarget();
                break;
            case 2:
                this.mOffsetX = (int) (eventX - this.mStartX);
                updateStart(this.mOffsetX, 0);
                break;
        }
        return true;
    }

    private void updateStart(int x, int y) {
        this.mFrameLayoutParams.setMargins(x, y, 0, 0);
        this.mAnimationText.setLayoutParams(this.mFrameLayoutParams);
    }

    public void setMainHandler(Handler handler) {
        this.mainHandler = handler;
    }
}
