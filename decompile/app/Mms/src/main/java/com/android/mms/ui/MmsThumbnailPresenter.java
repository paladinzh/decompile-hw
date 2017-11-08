package com.android.mms.ui;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.Model;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VCalendarModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.model.VideoModel;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ItemLoadedFuture;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.huawei.cspcommon.MLog;
import java.util.List;

public class MmsThumbnailPresenter extends Presenter {
    private static final String TAG = "MmsThumbnailPresenter";
    protected Handler mHandler = new Handler();
    private ItemLoadedCallback<ImageLoaded> mImageLoadedCallback = new ItemLoadedCallback<ImageLoaded>() {
        public void onItemLoaded(ImageLoaded imageLoaded, Throwable exception) {
            if (exception == null) {
                if (MmsThumbnailPresenter.this.mItemLoadedFuture != null) {
                    synchronized (MmsThumbnailPresenter.this.mItemLoadedFuture) {
                        MmsThumbnailPresenter.this.mItemLoadedFuture.setIsDone(true);
                    }
                }
                MmsThumbnailPresenter.this.loadImage(imageLoaded, exception);
            }
        }
    };
    private ItemLoadedFuture mItemLoadedFuture;
    protected int mLocation = 0;
    private ItemLoadedCallback mOnLoadedCallback;

    public void setLocation(int location) {
        this.mLocation = location;
    }

    public int getLocation() {
        return this.mLocation;
    }

    public MmsThumbnailPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
    }

    public void present(ItemLoadedCallback callback) {
        this.mOnLoadedCallback = callback;
        SlideModel slide = ((SlideshowModel) this.mModel).get(this.mLocation);
        if (slide != null) {
            presentFirstSlide((SlideViewInterface) this.mView, slide);
        }
    }

    protected void presentFirstSlide(SlideViewInterface view, SlideModel slide) {
        view.reset();
        if (SlideViewInterface.mIsShowAttachmentSize) {
            view.setSize(((SlideshowModel) this.mModel).getCurrentMessageSize() + 4096);
        }
        if (slide.hasImage()) {
            presentImageThumbnail(view, slide.getImage());
        } else if (slide.hasVideo()) {
            presentVideoThumbnail(view, slide.getVideo());
        } else if (slide.hasAudio()) {
            presentAudioThumbnail(view, slide.getAudio());
        } else if (slide.hasVcard()) {
            presentVcardThumbnail(view, slide.getVcard());
        } else if (slide.hasVCalendar()) {
            presentVCalendarThumbnail(view, slide.getVCalendar());
        }
    }

    protected void loadImage(ImageLoaded imageLoaded, Throwable exception) {
        if (this.mOnLoadedCallback != null) {
            this.mOnLoadedCallback.onItemLoaded(imageLoaded, exception);
        } else if (this.mModel == null) {
            MLog.e(TAG, "Load image error, slideshowmodel is null.");
        } else {
            SlideModel slide = ((SlideshowModel) this.mModel).get(this.mLocation);
            if (slide != null) {
                if (slide.hasVideo() && imageLoaded.mIsVideo) {
                    ((SlideViewInterface) this.mView).setVideoThumbnail(null, imageLoaded.mBitmap);
                } else if (slide.hasImage() && !imageLoaded.mIsVideo) {
                    ((SlideViewInterface) this.mView).setImage(null, imageLoaded.mBitmap);
                }
            }
        }
    }

    protected void presentVideoThumbnail(SlideViewInterface view, VideoModel video) {
        this.mItemLoadedFuture = video.loadThumbnailBitmap(this.mImageLoadedCallback);
    }

    protected void presentImageThumbnail(SlideViewInterface view, ImageModel image) {
        if (!"image/gif".equalsIgnoreCase(image.getContentType())) {
            this.mItemLoadedFuture = image.loadThumbnailBitmap(this.mImageLoadedCallback);
        } else if (!view.setGifImage(image.getSrc(), image.getUri())) {
            image.loadThumbnailBitmap(this.mImageLoadedCallback);
        }
    }

    protected void presentAudioThumbnail(SlideViewInterface view, AudioModel audio) {
        view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras());
    }

    protected void presentTextThumbnail(SlideViewInterface view, TextModel text) {
        view.setText("", text.getText());
    }

    protected void presentVcardThumbnail(SlideViewInterface view, VcardModel vcard) {
        String vCardName = null;
        String strNum = null;
        List<VCardDetailNode> nodes = vcard.getVcardDetailList();
        if (vcard.getVcardSize() <= 1) {
            for (VCardDetailNode node : nodes) {
                if (vCardName != null) {
                    strNum = node.getValue();
                    break;
                }
                vCardName = node.getValue();
            }
        } else {
            String[] names = new String[nodes.size()];
            int i = 0;
            for (VCardDetailNode node2 : nodes) {
                int i2 = i + 1;
                names[i] = node2.getName();
                i = i2;
            }
            vCardName = TextUtils.join(",", names);
        }
        view.setVcard(vCardName, strNum);
    }

    public void onModelChanged(Model model, boolean dataChanged) {
    }

    public void cancelBackgroundLoading() {
        SlideModel slide = ((SlideshowModel) this.mModel).get(0);
        if (slide != null && slide.hasImage()) {
            slide.getImage().cancelThumbnailLoading();
        }
    }

    protected void presentVCalendarThumbnail(SlideViewInterface view, VCalendarModel vcalendar) {
        view.setVcalendar(vcalendar.getTime(), vcalendar.getTitle());
    }
}
