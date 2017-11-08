package com.android.mms.attachment.ui.mediapicker.camerafocus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import java.util.ArrayList;
import java.util.List;

public class RenderOverlay extends FrameLayout {
    private List<Renderer> mClients;
    private int[] mPosition = new int[2];
    private RenderView mRenderView;
    private List<Renderer> mTouchClients;

    interface Renderer {
        void draw(Canvas canvas);

        boolean handlesTouch();

        void layout(int i, int i2, int i3, int i4);

        boolean onTouchEvent(MotionEvent motionEvent);

        void setOverlay(RenderOverlay renderOverlay);
    }

    private class RenderView extends View {
        private Renderer mTouchTarget;

        public RenderView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        public boolean onTouchEvent(MotionEvent evt) {
            if (this.mTouchTarget != null) {
                return this.mTouchTarget.onTouchEvent(evt);
            }
            if (RenderOverlay.this.mTouchClients == null) {
                return false;
            }
            boolean res = false;
            for (Renderer client : RenderOverlay.this.mTouchClients) {
                res |= client.onTouchEvent(evt);
            }
            return res;
        }

        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            RenderOverlay.this.adjustPosition();
            super.onLayout(changed, left, top, right, bottom);
            if (RenderOverlay.this.mClients != null) {
                for (Renderer renderer : RenderOverlay.this.mClients) {
                    renderer.layout(left, top, right, bottom);
                }
            }
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (RenderOverlay.this.mClients != null) {
                boolean redraw = false;
                for (Renderer renderer : RenderOverlay.this.mClients) {
                    renderer.draw(canvas);
                    redraw = !redraw ? ((OverlayRenderer) renderer).isVisible() : true;
                }
                if (redraw) {
                    invalidate();
                }
            }
        }
    }

    public RenderOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRenderView = new RenderView(context);
        addView(this.mRenderView, new LayoutParams(-1, -1));
        this.mClients = new ArrayList(10);
        this.mTouchClients = new ArrayList(10);
        setWillNotDraw(false);
        addRenderer(new PieRenderer(context));
    }

    public PieRenderer getPieRenderer() {
        for (Renderer renderer : this.mClients) {
            if (renderer instanceof PieRenderer) {
                return (PieRenderer) renderer;
            }
        }
        return null;
    }

    public void addRenderer(Renderer renderer) {
        this.mClients.add(renderer);
        renderer.setOverlay(this);
        if (renderer.handlesTouch()) {
            this.mTouchClients.add(0, renderer);
        }
        renderer.layout(getLeft(), getTop(), getRight(), getBottom());
    }

    public boolean dispatchTouchEvent(MotionEvent m) {
        return false;
    }

    private void adjustPosition() {
        getLocationInWindow(this.mPosition);
    }

    public void update() {
        this.mRenderView.invalidate();
    }
}
