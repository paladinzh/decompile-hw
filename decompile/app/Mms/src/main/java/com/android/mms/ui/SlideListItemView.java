package com.android.mms.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.Map;

public class SlideListItemView extends LinearLayout implements SlideViewInterface {
    private ImageView mAttachmentIcon;
    private TextView mAttachmentName;
    private GifView mImagePreview;
    private TextView mTextPreview;

    public SlideListItemView(Context context) {
        super(context);
    }

    public SlideListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTextPreview = (TextView) findViewById(R.id.text_preview);
        this.mTextPreview.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        this.mImagePreview = (GifView) findViewById(R.id.image_preview);
        this.mAttachmentName = (TextView) findViewById(R.id.attachment_name);
        this.mAttachmentIcon = (ImageView) findViewById(R.id.attachment_icon);
    }

    public void startAudio() {
    }

    public void startVideo() {
    }

    public void setAudio(Uri audio, String name, Map<String, ?> map) {
        if (name != null) {
            this.mAttachmentName.setText(name);
            this.mAttachmentName.setVisibility(0);
            this.mAttachmentIcon.setImageResource(R.drawable.ic_mms_music);
            this.mAttachmentIcon.setVisibility(0);
            return;
        }
        this.mAttachmentName.setText("");
        this.mAttachmentName.setVisibility(8);
        this.mAttachmentIcon.setImageDrawable(null);
        this.mAttachmentIcon.setVisibility(8);
    }

    public void setImage(String name, Bitmap bitmap) {
        if (name != null) {
            this.mAttachmentName.setText(name);
            this.mAttachmentName.setVisibility(0);
        } else {
            this.mAttachmentName.setText("");
            this.mAttachmentName.setVisibility(8);
        }
        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_bottom_emui);
            } catch (OutOfMemoryError e) {
                MLog.e("SlideListItemView", "setImage: out of memory: ", (Throwable) e);
                return;
            }
        }
        this.mImagePreview.setImageBitmap(bitmap);
    }

    public boolean setGifImage(String name, Uri uri) {
        if (uri == null) {
            return false;
        }
        try {
            setImage(name, null);
            this.mImagePreview.setImageURI(uri);
            return true;
        } catch (OutOfMemoryError e) {
            MLog.e("SlideListItemView", "setGifImage: out of memory: ", (Throwable) e);
            return false;
        }
    }

    public void setImageRegionFit(String fit) {
    }

    public void setImageVisibility(boolean visible) {
    }

    public void setText(String name, String text) {
        this.mTextPreview.setText(text);
        this.mTextPreview.setVisibility(TextUtils.isEmpty(text) ? 8 : 0);
    }

    public void setTextVisibility(boolean visible) {
    }

    public void setVideo(String name, Uri video) {
        if (name != null) {
            this.mAttachmentName.setText(name);
            this.mAttachmentName.setVisibility(0);
            this.mAttachmentIcon.setImageResource(R.drawable.emo_im_embarrassed);
            this.mAttachmentIcon.setVisibility(0);
        } else {
            this.mAttachmentName.setText("");
            this.mAttachmentName.setVisibility(8);
            this.mAttachmentIcon.setImageDrawable(null);
            this.mAttachmentIcon.setVisibility(8);
        }
        try {
            Bitmap bitmap = MessageUtils.createVideoThumbnail(this.mContext, video);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_default_avatar);
            }
            this.mImagePreview.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            MLog.e("SlideListItemView", "setVideo: out of memory: ", (Throwable) e);
        }
    }

    public void setVideoThumbnail(String name, Bitmap thumbnail) {
        this.mImagePreview.setImageBitmap(thumbnail);
    }

    public void setVideoVisibility(boolean visible) {
    }

    public void stopAudio() {
    }

    public void stopVideo() {
    }

    public void reset() {
    }

    public void pauseAudio() {
    }

    public void pauseVideo() {
    }

    public void seekAudio(int seekTo) {
    }

    public void seekVideo(int seekTo) {
    }

    public void setSize(int size) {
    }

    public void setVcard(String textSub1, String textSub2) {
        setImage(textSub1, null);
    }

    public void setVcalendar(String textSub1, String textSub2) {
        setImage(textSub1, null);
    }
}
