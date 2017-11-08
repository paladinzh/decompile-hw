package com.android.mms.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.Presenter;
import com.android.mms.ui.PresenterFactory;
import com.android.mms.ui.SlideViewInterface;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.gms.R;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import java.util.Map;

public abstract class MmsPopView extends LinearLayout implements SlideViewInterface {
    private ImageLoadedCallback mImageLoadedCallback;
    protected MessageItem mMessageItem;
    protected MmsPopViewCallback mMmsPopViewClickCallback;
    private Presenter mPresenter;

    public interface MmsPopViewCallback {
        boolean isDelayMsg();

        boolean isInEditMode();

        boolean onDoubleClick();
    }

    private static class ImageLoadedCallback implements ItemLoadedCallback<ImageLoaded> {
        private long mMessageId;
        private final MmsPopView mMmsPopView;

        public ImageLoadedCallback(MmsPopView mmsPopView) {
            this.mMmsPopView = mmsPopView;
            this.mMessageId = mmsPopView.getMessageItem().getMessageId();
        }

        public void reset(MmsPopView mmsPopView) {
            this.mMessageId = mmsPopView.getMessageItem().getMessageId();
        }

        public void onItemLoaded(ImageLoaded imageLoaded, Throwable exception) {
            MessageItem msgItem = this.mMmsPopView.mMessageItem;
            if (msgItem != null && msgItem.getMessageId() == this.mMessageId) {
                if (imageLoaded.mIsVideo) {
                    this.mMmsPopView.setVideoThumbnail(null, imageLoaded.mBitmap);
                } else {
                    this.mMmsPopView.setImage(null, imageLoaded.mBitmap);
                }
            }
        }
    }

    private class ItemClickListener implements IMmsClickListener {
        private ItemClickListener() {
        }

        public void onDoubleClick(View view) {
            boolean isDelayMsg = false;
            if (MmsPopView.this.mMmsPopViewClickCallback != null) {
                isDelayMsg = MmsPopView.this.mMmsPopViewClickCallback.onDoubleClick();
            }
            if (!isDelayMsg) {
                MmsPopView.this.onClick(view);
            }
        }

        public void onSingleClick(View view) {
            boolean isDelayMsg = false;
            if (MmsPopView.this.mMmsPopViewClickCallback != null) {
                isDelayMsg = MmsPopView.this.mMmsPopViewClickCallback.isDelayMsg();
            }
            if (!isDelayMsg) {
                MmsPopView.this.onClick(view);
            }
        }
    }

    public abstract void onClick(View view);

    public void setMmsPopViewClickCallback(MmsPopViewCallback mmsPopViewClickCallback) {
        this.mMmsPopViewClickCallback = mmsPopViewClickCallback;
    }

    public MmsPopView(Context context) {
        super(context);
    }

    public MmsPopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmsPopView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void bind(MessageItem messageItem) {
        this.mMessageItem = messageItem;
        initMmsView();
        setOnLongClickListener(null);
        new MmsClickListener(new ItemClickListener()).setClickListener(this);
    }

    protected void changeBackground() {
        if (this.mMessageItem.isInComingMessage()) {
            if (this.mMessageItem.isNeedChangeDrawableFroImage()) {
                setBackground(crateGroundDrawableFroImage(R.color.message_pop_incoming_bg_color));
            } else {
                setBackgroundResource(R.drawable.message_pop_incoming_bg);
            }
        } else if (this.mMessageItem.isRcsServiceForFavorites()) {
            setBackgroundResource(R.drawable.message_pop_rcs_send_bg);
        } else if (this.mMessageItem.isNeedChangeDrawableFroImage()) {
            setBackground(crateGroundDrawableFroImage(R.color.message_pop_send_bg_color));
        } else {
            setBackgroundResource(R.drawable.message_pop_send_bg);
        }
    }

    public Drawable crateGroundDrawableFroImage(int colorResId) {
        ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(new float[]{60.0f, 60.0f, 60.0f, 60.0f, 60.0f, 60.0f, 60.0f, 60.0f}, null, null));
        int[] size = MessageUtils.getImgWidthAndHeight(100, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, getContext());
        drawable.setIntrinsicWidth(size[0]);
        drawable.setIntrinsicHeight(size[1]);
        drawable.getPaint().setColor(0);
        drawable.getPaint().setAntiAlias(true);
        return drawable;
    }

    protected void changeBackground(boolean hasForegroundImage) {
        if (hasForegroundImage) {
            setBackground(null);
        } else {
            changeBackground();
        }
    }

    private void initMmsView() {
        if (this.mMessageItem.mSlideshow == null) {
            Log.e("MmsPopView", "initMmsView, but mSlideshow is null, this can not be null, must init in ListItem");
            return;
        }
        if (this.mPresenter == null) {
            this.mPresenter = PresenterFactory.getPresenter("MmsThumbnailPresenter", getContext(), this, this.mMessageItem.mSlideshow);
        } else {
            this.mPresenter.setModel(this.mMessageItem.mSlideshow);
            this.mPresenter.setView(this);
        }
        if (this.mImageLoadedCallback == null) {
            this.mImageLoadedCallback = new ImageLoadedCallback(this);
        } else {
            this.mImageLoadedCallback.reset(this);
        }
        this.mPresenter.present(this.mImageLoadedCallback);
    }

    private MessageItem getMessageItem() {
        return this.mMessageItem;
    }

    public void reset() {
    }

    public void setImage(String name, Bitmap bitmap) {
    }

    public boolean setGifImage(String name, Uri uri) {
        return false;
    }

    public void setImageRegionFit(String fit) {
    }

    public void setImageVisibility(boolean visible) {
    }

    public void setSize(int size) {
    }

    public void setVideo(String name, Uri video) {
    }

    public void setVideoThumbnail(String name, Bitmap bitmap) {
    }

    public void setVideoVisibility(boolean visible) {
    }

    public void startVideo() {
    }

    public void stopVideo() {
    }

    public void pauseVideo() {
    }

    public void seekVideo(int seekTo) {
    }

    public void setAudio(Uri audio, String name, Map<String, ?> map) {
    }

    public void startAudio() {
    }

    public void stopAudio() {
    }

    public void pauseAudio() {
    }

    public void seekAudio(int seekTo) {
    }

    public void setText(String name, String text) {
    }

    public void setTextVisibility(boolean visible) {
    }

    public void setVcard(String textSub1, String textSub2) {
    }

    public void setVcalendar(String textSub1, String textSub2) {
    }
}
