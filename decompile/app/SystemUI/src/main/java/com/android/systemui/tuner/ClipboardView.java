package com.android.systemui.tuner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View.DragShadowBuilder;
import android.widget.ImageView;
import com.android.systemui.R;

public class ClipboardView extends ImageView implements OnPrimaryClipChangedListener {
    private final ClipboardManager mClipboardManager;
    private ClipData mCurrentClip;

    public ClipboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mClipboardManager = (ClipboardManager) context.getSystemService(ClipboardManager.class);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startListening();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopListening();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == 0 && this.mCurrentClip != null) {
            startPocketDrag();
        }
        return super.onTouchEvent(ev);
    }

    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case 3:
                this.mClipboardManager.setPrimaryClip(event.getClipData());
                break;
            case 4:
            case 6:
                break;
            case 5:
                setBackgroundDragTarget(true);
                break;
        }
        setBackgroundDragTarget(false);
        return true;
    }

    private void setBackgroundDragTarget(boolean isTarget) {
        setBackgroundColor(isTarget ? 1308622847 : 0);
    }

    public void startPocketDrag() {
        startDragAndDrop(this.mCurrentClip, new DragShadowBuilder(this), null, 256);
    }

    public void startListening() {
        this.mClipboardManager.addPrimaryClipChangedListener(this);
        onPrimaryClipChanged();
    }

    public void stopListening() {
        this.mClipboardManager.removePrimaryClipChangedListener(this);
    }

    public void onPrimaryClipChanged() {
        this.mCurrentClip = this.mClipboardManager.getPrimaryClip();
        setImageResource(this.mCurrentClip != null ? R.drawable.clipboard_full : R.drawable.clipboard_empty);
    }
}
