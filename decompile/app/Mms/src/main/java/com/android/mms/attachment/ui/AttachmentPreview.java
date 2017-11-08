package com.android.mms.attachment.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import com.android.mms.attachment.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.android.mms.model.SlideshowModel;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.RichMessageEditor;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class AttachmentPreview extends HorizontalScrollView {
    private int mAnimatedHeight = -1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    AttachmentPreview.this.mMultiAttachmentLayout.setScrollEndState(false);
                    return;
                case 1002:
                    AttachmentPreview.this.setLanuchMeasureChild(false);
                    return;
                default:
                    return;
            }
        }
    };
    private Runnable mHideRunnable;
    private boolean mLaunchMeasureChild = false;
    private MultiAttachmentLayout mMultiAttachmentLayout = null;
    private Paint mPaint;
    private Path mPath = new Path();
    private boolean mPendingFirstUpdate;
    private boolean mPendingHideCanceled;
    private RcsGroupChatRichMessageEditor mRcsGroupChatRichMessageEditor;
    private RichMessageEditor mRichMessageEditor;

    public AttachmentPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRichMessageEditor(RichMessageEditor richMessageEditor, OnAttachmentClickListener attachmentClickListener) {
        this.mRichMessageEditor = richMessageEditor;
        if (this.mMultiAttachmentLayout != null) {
            this.mMultiAttachmentLayout.setOnAttachmentClickListener(attachmentClickListener);
        } else {
            MLog.d("AttachmentPreview", "MultiAttachmentLayout set OnAttachmentClickListener failed");
        }
    }

    public void setRcsGroupChatRichMessageEditor(RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor, OnAttachmentClickListener attachmentClickListener) {
        this.mRcsGroupChatRichMessageEditor = rcsGroupChatRichMessageEditor;
        if (this.mMultiAttachmentLayout != null) {
            this.mMultiAttachmentLayout.setOnAttachmentClickListener(attachmentClickListener);
        } else {
            MLog.d("AttachmentPreview", "MultiAttachmentLayout set OnAttachmentClickListener failed");
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMultiAttachmentLayout = (MultiAttachmentLayout) findViewById(R.id.attachment_content);
        this.mPendingFirstUpdate = true;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.FILL_AND_STROKE);
        this.mPaint.setColor(getContext().getResources().getColor(R.color.mediapicker_view_split_bg_color));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void cancelPendingHide() {
        this.mPendingHideCanceled = true;
    }

    public void hideAttachmentPreview(boolean isFullScreen) {
        if (getVisibility() != 8) {
            if (this.mPendingHideCanceled) {
                MLog.d("AttachmentPreview", "hideAttachmentPreview pendingHide canceled");
            }
            this.mPendingHideCanceled = false;
            this.mMultiAttachmentLayout.removeAllViews();
            setVisibility(8);
            return;
        }
        this.mMultiAttachmentLayout.removeAllViews();
        setVisibility(8);
    }

    public boolean onAttachmentsChanged(SlideshowModel slideshowModel, final boolean isFullScreen) {
        this.mPendingFirstUpdate = false;
        if (this.mLaunchMeasureChild) {
            this.mHandler.sendEmptyMessageDelayed(1002, 1000);
        }
        if (slideshowModel == null || !slideshowModel.canShowPreview()) {
            this.mHideRunnable = new Runnable() {
                public void run() {
                    AttachmentPreview.this.mHideRunnable = null;
                    AttachmentPreview.this.hideAttachmentPreview(isFullScreen);
                    AttachmentPreview.this.mMultiAttachmentLayout.clearPreviewViews();
                }
            };
            this.mHideRunnable.run();
            return false;
        }
        cancelPendingHide();
        if (getVisibility() != 0) {
            setVisibility(0);
        }
        if (this.mMultiAttachmentLayout.getVisibility() != 0) {
            this.mMultiAttachmentLayout.setVisibility(0);
        }
        this.mMultiAttachmentLayout.bindAttachments(slideshowModel.getSlideModels());
        refreshAttachmentScroll();
        return true;
    }

    public boolean onAttachmentsChanged(boolean isFullScreen) {
        this.mPendingFirstUpdate = false;
        if (this.mLaunchMeasureChild) {
            this.mHandler.sendEmptyMessageDelayed(1002, 1000);
        }
        cancelPendingHide();
        if (getVisibility() != 0) {
            setVisibility(0);
        }
        if (this.mMultiAttachmentLayout.getVisibility() != 0) {
            this.mMultiAttachmentLayout.setVisibility(0);
        }
        this.mMultiAttachmentLayout.bindRcsAttachments(this.mRcsGroupChatRichMessageEditor.getMediaModelData());
        refreshAttachmentScroll();
        return true;
    }

    private void scrollToEnd() {
        if (this.mMultiAttachmentLayout != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (MessageUtils.isNeedLayoutRtl()) {
                        AttachmentPreview.this.fullScroll(17);
                    } else {
                        AttachmentPreview.this.fullScroll(66);
                    }
                }
            });
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPath.reset();
        int splitWidth = (this.mMultiAttachmentLayout.getMeasuredWidth() + getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preview_padding_end_max)) + getContext().getResources().getDimensionPixelOffset(R.dimen.attachment_preview_padding_end);
        int screenWdith = getContext().getResources().getDisplayMetrics().widthPixels;
        if (splitWidth <= screenWdith) {
            splitWidth = screenWdith;
        }
        int splitHeight = getContext().getResources().getDimensionPixelSize(R.dimen.meidapicker_view_split_height);
        this.mPath.moveTo(0.0f, 0.0f);
        this.mPath.lineTo((float) splitWidth, 0.0f);
        this.mPath.lineTo((float) splitWidth, (float) splitHeight);
        this.mPath.lineTo(0.0f, (float) splitHeight);
        canvas.drawPath(this.mPath, this.mPaint);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshAttachmentScroll();
    }

    public void refreshAttachmentScroll() {
        if (!(this.mMultiAttachmentLayout == null || !this.mMultiAttachmentLayout.getScrollEndState() || this.mLaunchMeasureChild)) {
            scrollToEnd();
            this.mHandler.removeMessages(1001);
            this.mHandler.sendEmptyMessageDelayed(1001, 200);
        }
    }

    public void setMultiAttachmentScrollState(boolean scrollEndState) {
        if (this.mMultiAttachmentLayout != null) {
            this.mMultiAttachmentLayout.setScrollEndState(scrollEndState);
        }
    }

    public void setLanuchMeasureChild(boolean isLaunch) {
        this.mLaunchMeasureChild = isLaunch;
    }
}
