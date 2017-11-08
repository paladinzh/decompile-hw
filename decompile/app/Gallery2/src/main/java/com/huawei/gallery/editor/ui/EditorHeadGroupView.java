package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.FloatAnimation;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;

public class EditorHeadGroupView extends LinearLayout {
    protected FloatAnimation mAnimation;
    protected SimpleActionItem mLeftActionItem;
    private SimpleActionItem mRedoActionItem;
    protected SimpleActionItem mRightActionItem;
    protected TextView mTextView;
    private SimpleActionItem mUndeActionItem;

    public EditorHeadGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    protected void setAction(SimpleActionItem actionItem, Action action) {
        if (action == null) {
            actionItem.setVisibility(8);
            return;
        }
        actionItem.applyStyle(1);
        actionItem.setAction(action);
    }

    private void setAction(SimpleActionItem actionItem, Action action, boolean enable) {
        if (action == null) {
            actionItem.setVisibility(8);
            return;
        }
        actionItem.applyStyle(1);
        actionItem.setAction(action);
        actionItem.setEnabled(enable);
    }

    protected void setTitle(TextView textView, String title) {
        if (textView != null) {
            if (title == null) {
                textView.setVisibility(8);
            }
            textView.setText(title);
        }
    }

    public void startAnimeUp(boolean isUp, int delta, int duration, int delay) {
        if (this.mAnimation != null) {
            this.mAnimation.forceStop();
            this.mAnimation = null;
        }
        if (isUp) {
            this.mAnimation = new FloatAnimation((float) delta, 0.0f, duration);
        } else {
            this.mAnimation = new FloatAnimation(0.0f, (float) delta, duration);
        }
        this.mAnimation.setInterpolator(EditorAnimation.sInterPolator);
        this.mAnimation.setDelay(delay);
        this.mAnimation.start();
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        boolean more = false;
        if (this.mAnimation != null) {
            this.mAnimation.calculate(System.currentTimeMillis());
            more = true;
            canvas.translate(0.0f, this.mAnimation.get());
            if (!this.mAnimation.isActive()) {
                this.mAnimation = null;
            }
        }
        super.onDraw(canvas);
        if (more) {
            invalidate();
        }
    }

    public void initView(OnClickListener listener) {
        this.mLeftActionItem = (SimpleActionItem) findViewById(R.id.head_select_left);
        this.mRightActionItem = (SimpleActionItem) findViewById(R.id.head_select_right);
        this.mTextView = (TextView) findViewById(R.id.head_select_title);
        this.mUndeActionItem = (SimpleActionItem) findViewById(R.id.head_select_undo);
        this.mRedoActionItem = (SimpleActionItem) findViewById(R.id.head_select_redo);
        this.mLeftActionItem.setOnClickListener(listener);
        this.mRightActionItem.setOnClickListener(listener);
        this.mUndeActionItem.setOnClickListener(listener);
        this.mRedoActionItem.setOnClickListener(listener);
    }

    public void changeActionBar(ActionInfo actionInfo) {
        if (actionInfo != null) {
            setAction(this.mLeftActionItem, actionInfo.getLeftAction());
            setAction(this.mRightActionItem, actionInfo.getRightAction());
            setTitle(this.mTextView, actionInfo.getTitle());
            setAction(this.mRedoActionItem, actionInfo.getRedoAction(), actionInfo.canRedo());
            setAction(this.mUndeActionItem, actionInfo.getUndoAction(), actionInfo.canUndo());
        }
    }
}
