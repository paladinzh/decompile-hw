package com.android.mms.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import com.android.mms.ui.AsyncDialog;
import com.android.mms.ui.GifView;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.StatisticalHelper;
import java.util.Map;

public class MmsSlideView extends MmsPopView {
    private AsyncDialog mAsyncDialog;
    private GifView mGifView = null;
    private ImageButton mSlideShowButton = null;

    public MmsSlideView(Context context) {
        super(context);
    }

    public MmsSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmsSlideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGifView = (GifView) findViewById(R.id.image_view);
        this.mSlideShowButton = (ImageButton) findViewById(R.id.play_slideshow_button);
    }

    public void onClick(View view) {
        switch (this.mMessageItem.mAttachmentType) {
            case 0:
                viewMmsMessageAttachment();
                return;
            case 1:
                if (!this.mMessageItem.isSms() && !this.mMessageItem.isRcsChat()) {
                    MessageUtils.viewSimpleSlideshow(getContext(), this.mMessageItem.mSlideshow);
                    StatisticalHelper.incrementReportCount(getContext(), 2230);
                    return;
                }
                return;
            case 2:
                StatisticalHelper.incrementReportCount(getContext(), 2233);
                viewMmsMessageAttachment();
                return;
            case 3:
                StatisticalHelper.incrementReportCount(getContext(), 2231);
                viewMmsMessageAttachment();
                return;
            case 4:
                StatisticalHelper.incrementReportCount(getContext(), 2234);
                viewMmsMessageAttachment();
                return;
            default:
                return;
        }
    }

    private AsyncDialog getAsyncDialog() {
        if (this.mAsyncDialog == null) {
            this.mAsyncDialog = new AsyncDialog((Activity) getContext());
        }
        return this.mAsyncDialog;
    }

    public void bind(MessageItem messageItem) {
        this.mGifView.setImageBitmap(null);
        super.bind(messageItem);
        changeBackground();
        drawPlaybackButton();
        setGifDescription();
    }

    public void setImage(String name, Bitmap bitmap) {
        try {
            this.mGifView.setImageBitmap(bitmap, 16.0f);
            changeBackground(bitmap != null);
        } catch (OutOfMemoryError e) {
            changeBackground();
            MLog.e("MmsSlideView", "setImage: out of memory: ", (Throwable) e);
        }
    }

    public void setVideoThumbnail(String name, Bitmap bitmap) {
        try {
            this.mGifView.setImageBitmap(bitmap, 16.0f);
            changeBackground(bitmap != null);
        } catch (OutOfMemoryError e) {
            changeBackground();
            MLog.e("MmsSlideView", "setVideoThumbnail: out of memory: ", (Throwable) e);
        }
    }

    public boolean setGifImage(String name, Uri uri) {
        if (uri == null) {
            try {
                setImage(name, null);
                return false;
            } catch (OutOfMemoryError e) {
                MLog.e("MmsSlideView", "setGifImage: out of memory: ", (Throwable) e);
                changeBackground();
                return false;
            }
        }
        boolean result = this.mGifView.setGifImage(uri, true);
        changeBackground(result);
        return result;
    }

    public void setAudio(Uri audio, String name, Map<String, ?> map) {
        changeBackground();
    }

    private void drawPlaybackButton() {
        int imgRes = -1;
        switch (this.mMessageItem.mAttachmentType) {
            case 2:
                if (!this.mMessageItem.isInComingMessage()) {
                    imgRes = R.drawable.mms_ic_item_video_send;
                    break;
                } else {
                    imgRes = R.drawable.mms_ic_item_video_recv;
                    break;
                }
            case 3:
                imgRes = this.mMessageItem.isInComingMessage() ? R.drawable.mms_ic_item_audio_recv : R.drawable.mms_ic_item_audio_send;
                changeBackground();
                break;
            case 4:
                if (!this.mMessageItem.isInComingMessage()) {
                    imgRes = R.drawable.mms_ic_item_slideshow_send;
                    break;
                } else {
                    imgRes = R.drawable.mms_ic_item_slideshow_recv;
                    break;
                }
        }
        if (imgRes == -1) {
            this.mSlideShowButton.setVisibility(8);
        } else {
            this.mSlideShowButton.setImageResource(imgRes);
            this.mSlideShowButton.setVisibility(0);
            this.mSlideShowButton.setClickable(false);
            this.mSlideShowButton.setLongClickable(false);
        }
        this.mSlideShowButton.setTag(this.mMessageItem);
    }

    private void setGifDescription() {
        Context context = getContext();
        String description = context.getString(R.string.view_slideshow_hint);
        switch (this.mMessageItem.mAttachmentType) {
            case 1:
                description = context.getString(R.string.type_picture);
                break;
        }
        this.mGifView.setContentDescription(description);
    }

    private void viewMmsMessageAttachment() {
        if (!this.mMessageItem.isSms()) {
            MessageUtils.viewMmsMessageAttachment((Activity) getContext(), this.mMessageItem.mMessageUri, this.mMessageItem.mSlideshow, getAsyncDialog());
        }
    }
}
