package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import com.android.mms.attachment.datamodel.data.GalleryGridItemData;
import com.android.mms.attachment.ui.AsyncImageView;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.mms.util.StatisticalHelper;

public class GalleryGridItemView extends FrameLayout {
    private int checkBoxWidth;
    private boolean mBoxClickable = true;
    private CheckBox mCheckBox;
    GalleryGridItemData mData = new GalleryGridItemData();
    private Handler mHandler = new Handler(getContext().getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    GalleryGridItemView.this.mCheckBox.setClickable(true);
                    GalleryGridItemView.this.mCheckBox.setEnabled(true);
                    GalleryGridItemView.this.mBoxClickable = true;
                    return;
                default:
                    return;
            }
        }
    };
    private HostInterface mHostInterface;
    private AsyncImageView mImageView;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (GalleryGridItemView.this.mHostInterface != null) {
                if (v instanceof GalleryGridItemView) {
                    GalleryGridItemView.this.mHostInterface.onItemClicked(GalleryGridItemView.this, GalleryGridItemView.this.mData, false);
                } else if (v instanceof CheckBox) {
                    GalleryGridItemView.this.mCheckBox.setClickable(false);
                    GalleryGridItemView.this.mBoxClickable = false;
                    GalleryGridItemView.this.mCheckBox.setEnabled(false);
                    if (GalleryGridItemView.this.mHostInterface.isItemSelected(GalleryGridItemView.this.mData)) {
                        GalleryGridItemView.this.mHandler.sendEmptyMessageDelayed(1001, 300);
                    } else {
                        GalleryGridItemView.this.mHandler.sendEmptyMessageDelayed(1001, 800);
                    }
                    GalleryGridItemView.this.mHostInterface.onCheckBoxClicked(GalleryGridItemView.this, GalleryGridItemView.this.mData, false);
                    StatisticalHelper.incrementReportCount(GalleryGridItemView.this.getContext(), 2256);
                }
            }
        }
    };
    private OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            GalleryGridItemView.this.setTouchDelegate(new TouchDelegate(new Rect(MessageUtils.isNeedLayoutRtl() ? 0 : GalleryGridItemView.this.getWidth() - GalleryGridItemView.this.checkBoxWidth, GalleryGridItemView.this.getHeight() - GalleryGridItemView.this.checkBoxWidth, MessageUtils.isNeedLayoutRtl() ? GalleryGridItemView.this.checkBoxWidth : GalleryGridItemView.this.getWidth(), GalleryGridItemView.this.getHeight()), GalleryGridItemView.this.mCheckBox) {
                public boolean onTouchEvent(MotionEvent event) {
                    if (!GalleryGridItemView.this.mBoxClickable) {
                        return super.onTouchEvent(event);
                    }
                    switch (event.getAction()) {
                        case 0:
                            GalleryGridItemView.this.setPressed(true);
                            break;
                        case 1:
                        case 3:
                            GalleryGridItemView.this.setPressed(false);
                            break;
                    }
                    return super.onTouchEvent(event);
                }
            });
        }
    };
    private View mSelectBackground;
    private View mVideoIcon;

    public interface HostInterface {
        boolean isItemSelected(GalleryGridItemData galleryGridItemData);

        boolean isMultiSelectEnabled();

        void onCheckBoxClicked(View view, GalleryGridItemData galleryGridItemData, boolean z);

        void onItemClicked(View view, GalleryGridItemData galleryGridItemData, boolean z);
    }

    public GalleryGridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.checkBoxWidth = context.getResources().getDimensionPixelSize(R.dimen.gallery_item_view_checkbox_click_width);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mImageView = (AsyncImageView) findViewById(R.id.image);
        this.mVideoIcon = findViewById(R.id.gallery_video_icon);
        this.mVideoIcon.setVisibility(8);
        this.mSelectBackground = findViewById(R.id.gallery_image_select_background);
        this.mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        this.mCheckBox.setOnClickListener(this.mOnClickListener);
        setOnClickListener(this.mOnClickListener);
        OnLongClickListener longClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (v instanceof GalleryGridItemView) {
                    GalleryGridItemView.this.mHostInterface.onItemClicked(GalleryGridItemView.this, GalleryGridItemView.this.mData, true);
                } else if (v instanceof CheckBox) {
                    GalleryGridItemView.this.mHostInterface.onCheckBoxClicked(GalleryGridItemView.this, GalleryGridItemView.this.mData, true);
                }
                return true;
            }
        };
        setOnLongClickListener(longClickListener);
        this.mCheckBox.setOnLongClickListener(longClickListener);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public void bind(Cursor cursor, HostInterface hostInterface) {
        removeOnLayoutChangeListener(this.mOnLayoutChangeListener);
        setTouchDelegate(null);
        int desiredSize = getResources().getDimensionPixelSize(R.dimen.gallery_image_cell_size);
        this.mData.bind(cursor, desiredSize, desiredSize);
        if (!this.mData.isDocumentPickerItem()) {
            addOnLayoutChangeListener(this.mOnLayoutChangeListener);
        }
        this.mHostInterface = hostInterface;
        updateViewState();
    }

    private void updateViewState() {
        int i = 0;
        updateImageView();
        if (!this.mHostInterface.isMultiSelectEnabled() || this.mData.isDocumentPickerItem()) {
            this.mVideoIcon.setVisibility(8);
            this.mCheckBox.setVisibility(8);
            this.mCheckBox.setClickable(false);
            this.mBoxClickable = false;
            return;
        }
        if (TextUtils.isEmpty(this.mData.getContentType())) {
            this.mVideoIcon.setVisibility(8);
        } else if (this.mData.getContentType().startsWith("image")) {
            this.mVideoIcon.setVisibility(8);
        } else {
            this.mVideoIcon.setVisibility(0);
        }
        this.mCheckBox.setVisibility(0);
        this.mCheckBox.setClickable(true);
        this.mBoxClickable = true;
        boolean isSelected = this.mHostInterface.isItemSelected(this.mData);
        this.mCheckBox.setChecked(isSelected);
        View view = this.mSelectBackground;
        if (!isSelected) {
            i = 8;
        }
        view.setVisibility(i);
    }

    private void updateImageView() {
        Context context = getContext();
        if (this.mData.isDocumentPickerItem()) {
            this.mImageView.setScaleType(ScaleType.CENTER);
            this.mImageView.setImageResourceId(null);
            this.mImageView.setImageResource(R.drawable.ic_sms_album);
            setBackgroundColor(getResources().getColor(R.color.gallery_image_album_default_background));
            this.mImageView.setContentDescription(context.getString(R.string.attachment_select_image));
            return;
        }
        this.mImageView.setScaleType(ScaleType.CENTER_CROP);
        setBackgroundColor(getResources().getColor(R.color.gallery_image_default_background));
        this.mImageView.setImageResourceId(this.mData.getImageRequestDescriptor());
        if (this.mData.isVideo()) {
            this.mImageView.setContentDescription(context.getString(R.string.attach_video));
        } else {
            this.mImageView.setContentDescription(context.getString(R.string.attach_image));
        }
    }
}
