package com.android.mms.attachment.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.rastermill.FrameSequenceDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.R$styleable;
import com.android.mms.attachment.datamodel.binding.BindableMediaRequest;
import com.android.mms.attachment.datamodel.binding.Binding;
import com.android.mms.attachment.datamodel.binding.BindingBase;
import com.android.mms.attachment.datamodel.media.GifImageResource;
import com.android.mms.attachment.datamodel.media.ImageRequestDescriptor;
import com.android.mms.attachment.datamodel.media.ImageResource;
import com.android.mms.attachment.datamodel.media.MediaRequest;
import com.android.mms.attachment.datamodel.media.MediaResourceManager;
import com.android.mms.attachment.datamodel.media.MediaResourceManager.MediaResourceLoadListener;
import com.android.mms.attachment.utils.ThreadUtil;
import com.android.mms.attachment.utils.UiUtils;
import com.huawei.cspcommon.MLog;
import java.util.HashSet;

public class AsyncImageView extends ImageView implements MediaResourceLoadListener<ImageResource> {
    private int mClipPathHeight;
    private int mClipPathWidth;
    private final int mCornerRadius;
    private AsyncImageViewDelayLoader mDelayLoader;
    private ImageRequestDescriptor mDetachedRequestDescriptor;
    private final Runnable mDisposeRunnable = new Runnable() {
        public void run() {
            if (AsyncImageView.this.mImageRequestBinding.isBound()) {
                AsyncImageView.this.mDetachedRequestDescriptor = (ImageRequestDescriptor) ((BindableMediaRequest) AsyncImageView.this.mImageRequestBinding.getData()).getDescriptor();
            }
            AsyncImageView.this.unbindView();
            AsyncImageView.this.releaseImageResource();
        }
    };
    private boolean mFadeIn;
    public final Binding<BindableMediaRequest<ImageResource>> mImageRequestBinding = BindingBase.createBinding(this);
    protected ImageResource mImageResource;
    private final Drawable mPlaceholderDrawable;
    private final boolean mReveal;
    private final Path mRoundedCornerClipPath;

    public static class AsyncImageViewDelayLoader {
        private final HashSet<AsyncImageView> mAttachedViews = new HashSet();
        private boolean mShouldDelayLoad;

        private void registerView(AsyncImageView view) {
            this.mAttachedViews.add(view);
        }

        private void unregisterView(AsyncImageView view) {
            this.mAttachedViews.remove(view);
        }

        public boolean isDelayLoadingImage() {
            return this.mShouldDelayLoad;
        }
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.obtainStyledAttributes(attrs, R$styleable.AsyncImageView, 0, 0);
        this.mFadeIn = attr.getBoolean(0, true);
        this.mReveal = attr.getBoolean(3, false);
        this.mPlaceholderDrawable = attr.getDrawable(1);
        this.mCornerRadius = attr.getDimensionPixelSize(2, 0);
        this.mRoundedCornerClipPath = new Path();
        attr.recycle();
    }

    public void setImageResourceId(ImageRequestDescriptor descriptor) {
        CharSequence key = descriptor == null ? null : descriptor.getKey();
        if (this.mImageRequestBinding.isBound()) {
            if (!TextUtils.equals(((BindableMediaRequest) this.mImageRequestBinding.getData()).getKey(), key)) {
                unbindView();
            } else {
                return;
            }
        }
        setImage(null);
        resetTransientViewStates();
        if (!TextUtils.isEmpty(key)) {
            maybeSetupPlaceholderDrawable(descriptor);
            requestImage(descriptor.buildAsyncMediaRequest(getContext(), this));
        }
    }

    private void maybeSetupPlaceholderDrawable(ImageRequestDescriptor descriptor) {
        if (!TextUtils.isEmpty(descriptor.getKey()) && this.mPlaceholderDrawable != null) {
            if (!(descriptor.sourceWidth == -1 || descriptor.sourceHeight == -1)) {
                setImageDrawable(PlaceholderInsetDrawable.fromDrawable(new ColorDrawable(0), descriptor.sourceWidth, descriptor.sourceHeight));
            }
            setBackground(this.mPlaceholderDrawable);
        }
    }

    protected void setImage(ImageResource resource) {
        setImage(resource, false);
    }

    protected void setImage(ImageResource resource, boolean isCached) {
        Drawable drawable;
        releaseImageResource();
        ThreadUtil.getMainThreadHandler().removeCallbacks(this.mDisposeRunnable);
        if (resource != null) {
            drawable = resource.getDrawable(getResources());
        } else {
            drawable = null;
        }
        if (drawable != null) {
            this.mImageResource = resource;
            this.mImageResource.addRef();
            setImageDrawable(drawable);
            if (drawable instanceof FrameSequenceDrawable) {
                ((FrameSequenceDrawable) drawable).start();
            }
            if (getVisibility() == 0) {
                if (this.mReveal) {
                    setVisibility(4);
                    UiUtils.revealOrHideViewWithAnimation(false, this, 0, null);
                } else if (this.mFadeIn && !isCached) {
                    setAlpha(0.0f);
                    animate().alpha(ContentUtil.FONT_SIZE_NORMAL).start();
                }
            }
            if (MLog.isLoggable("Mms_app", 2)) {
                if (this.mImageResource instanceof GifImageResource) {
                    MLog.v("AsyncImageView", "setImage size unknown -- it's a GIF");
                } else {
                    MLog.v("AsyncImageView", "setImage size: " + this.mImageResource.getMediaSize() + " width: " + this.mImageResource.getBitmap().getWidth() + " heigh: " + this.mImageResource.getBitmap().getHeight());
                }
            }
        }
        invalidate();
    }

    private void requestImage(BindableMediaRequest<ImageResource> request) {
        this.mImageRequestBinding.bind(request);
        if (this.mDelayLoader == null || !this.mDelayLoader.isDelayLoadingImage()) {
            MediaResourceManager.get().requestMediaResourceAsync(request);
        } else {
            this.mDelayLoader.registerView(this);
        }
    }

    public void onMediaResourceLoaded(MediaRequest<ImageResource> mediaRequest, ImageResource resource, boolean isCached) {
        if (this.mImageResource != resource) {
            setImage(resource, isCached);
        }
    }

    public void onMediaResourceLoadError(MediaRequest<ImageResource> mediaRequest, Exception exception) {
        unbindView();
        setImage(null);
    }

    private void releaseImageResource() {
        Drawable drawable = getDrawable();
        if (drawable instanceof FrameSequenceDrawable) {
            ((FrameSequenceDrawable) drawable).stop();
            ((FrameSequenceDrawable) drawable).destroy();
        }
        if (this.mImageResource != null) {
            this.mImageResource.release();
            this.mImageResource = null;
        }
        setImageDrawable(null);
        setBackground(null);
    }

    private void resetTransientViewStates() {
        clearAnimation();
        setAlpha(ContentUtil.FONT_SIZE_NORMAL);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThreadUtil.getMainThreadHandler().removeCallbacks(this.mDisposeRunnable);
        if (this.mFadeIn) {
            setAlpha(ContentUtil.FONT_SIZE_NORMAL);
        }
        if (!(this.mImageRequestBinding.isBound() || this.mDetachedRequestDescriptor == null)) {
            setImageResourceId(this.mDetachedRequestDescriptor);
        }
        this.mDetachedRequestDescriptor = null;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThreadUtil.getMainThreadHandler().postDelayed(this.mDisposeRunnable, 100);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth < getMinimumWidth() && measuredHeight < getMinimumHeight() && getAdjustViewBounds()) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthSpecMode != 1073741824 || heightSpecMode != 1073741824) {
                int width = measuredWidth;
                int height = measuredHeight;
                int minimumWidth = resolveSize(getMinimumWidth(), getMaxWidth(), widthMeasureSpec);
                int minimumHeight = resolveSize(getMinimumHeight(), getMaxHeight(), heightMeasureSpec);
                float aspectRatio = ((float) measuredWidth) / ((float) measuredHeight);
                if (aspectRatio != 0.0f) {
                    if (measuredWidth < minimumWidth) {
                        height = resolveSize((int) (((float) minimumWidth) / aspectRatio), getMaxHeight(), heightMeasureSpec);
                        width = (int) (((float) height) * aspectRatio);
                    }
                    if (height < minimumHeight) {
                        width = resolveSize((int) (((float) minimumHeight) * aspectRatio), getMaxWidth(), widthMeasureSpec);
                        height = (int) (((float) width) / aspectRatio);
                    }
                    setMeasuredDimension(width, height);
                }
            }
        }
    }

    private static int resolveSize(int desiredSize, int maxSize, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                return Math.min(Math.min(desiredSize, specSize), maxSize);
            case 0:
                return Math.min(desiredSize, maxSize);
            default:
                return specSize;
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCornerRadius > 0) {
            int currentWidth = getWidth();
            int currentHeight = getHeight();
            if (!(this.mClipPathWidth == currentWidth && this.mClipPathHeight == currentHeight)) {
                RectF rect = new RectF(0.0f, 0.0f, (float) currentWidth, (float) currentHeight);
                this.mRoundedCornerClipPath.reset();
                this.mRoundedCornerClipPath.addRoundRect(rect, (float) this.mCornerRadius, (float) this.mCornerRadius, Direction.CW);
                this.mClipPathWidth = currentWidth;
                this.mClipPathHeight = currentHeight;
            }
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.clipPath(this.mRoundedCornerClipPath);
            super.onDraw(canvas);
            canvas.restoreToCount(saveCount);
            return;
        }
        super.onDraw(canvas);
    }

    private void unbindView() {
        if (this.mImageRequestBinding.isBound()) {
            this.mImageRequestBinding.unbind();
            if (this.mDelayLoader != null) {
                this.mDelayLoader.unregisterView(this);
            }
        }
    }
}
