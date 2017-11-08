package com.android.mms.ui;

import android.content.Context;
import android.os.Handler;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.MediaModel.MediaAction;
import com.android.mms.model.Model;
import com.android.mms.model.RegionMediaModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VideoModel;
import com.android.mms.ui.AdaptableSlideViewInterface.OnSizeChangedListener;
import com.android.mms.util.ItemLoadedCallback;

public class SlideshowPresenter extends Presenter {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "SlideshowPresenter";
    protected final Handler mHandler = new Handler();
    protected float mHeightTransformRatio = ContentUtil.FONT_SIZE_NORMAL;
    protected int mLocation = 0;
    protected final int mSlideNumber;
    private final OnSizeChangedListener mViewSizeChangedListener = new OnSizeChangedListener() {
        public void onSizeChanged(int width, int height) {
            if (SlideshowPresenter.this.mModel instanceof SlideshowModel) {
                LayoutModel layout = ((SlideshowModel) SlideshowPresenter.this.mModel).getLayout();
                SlideshowPresenter.this.mWidthTransformRatio = SlideshowPresenter.this.getWidthTransformRatio(width, layout.getLayoutWidth());
                SlideshowPresenter.this.mHeightTransformRatio = SlideshowPresenter.this.getHeightTransformRatio(height, layout.getLayoutHeight());
                float ratio = SlideshowPresenter.this.mWidthTransformRatio > SlideshowPresenter.this.mHeightTransformRatio ? SlideshowPresenter.this.mWidthTransformRatio : SlideshowPresenter.this.mHeightTransformRatio;
                SlideshowPresenter.this.mWidthTransformRatio = ratio;
                SlideshowPresenter.this.mHeightTransformRatio = ratio;
            }
        }
    };
    protected float mWidthTransformRatio = ContentUtil.FONT_SIZE_NORMAL;

    public SlideshowPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
        if (this.mModel instanceof SlideshowModel) {
            this.mSlideNumber = ((SlideshowModel) this.mModel).size();
        } else {
            this.mSlideNumber = 0;
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setOnSizeChangedListener(this.mViewSizeChangedListener);
        }
    }

    private float getWidthTransformRatio(int width, int layoutWidth) {
        if (width > 0) {
            return ((float) layoutWidth) / ((float) width);
        }
        return ContentUtil.FONT_SIZE_NORMAL;
    }

    private float getHeightTransformRatio(int height, int layoutHeight) {
        if (height > 0) {
            return ((float) layoutHeight) / ((float) height);
        }
        return ContentUtil.FONT_SIZE_NORMAL;
    }

    private int transformWidth(int width) {
        return (int) (((float) width) / this.mWidthTransformRatio);
    }

    private int transformHeight(int height) {
        return (int) (((float) height) / this.mHeightTransformRatio);
    }

    public void present(ItemLoadedCallback callback) {
        if (this.mView instanceof SlideViewInterface) {
            presentSlide((SlideViewInterface) this.mView, ((SlideshowModel) this.mModel).get(this.mLocation));
        }
    }

    protected void presentSlide(SlideViewInterface view, SlideModel model) {
        view.reset();
        for (MediaModel media : model) {
            if (media instanceof RegionMediaModel) {
                presentRegionMedia(view, (RegionMediaModel) media, true);
            } else if (media.isAudio()) {
                presentAudio(view, (AudioModel) media, true);
            }
        }
    }

    protected void presentRegionMedia(SlideViewInterface view, RegionMediaModel rMedia, boolean dataChanged) {
        RegionModel r = rMedia.getRegion();
        if (rMedia.isText()) {
            presentText(view, (TextModel) rMedia, r, dataChanged);
        } else if (rMedia.isImage()) {
            presentImage(view, (ImageModel) rMedia, r, dataChanged);
        } else if (rMedia.isVideo()) {
            presentVideo(view, (VideoModel) rMedia, r, dataChanged);
        }
    }

    protected void presentAudio(SlideViewInterface view, AudioModel audio, boolean dataChanged) {
        if (dataChanged) {
            view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras());
        }
        MediaAction action = audio.getCurrentAction();
        if (action == MediaAction.START) {
            view.startAudio();
        } else if (action == MediaAction.PAUSE) {
            view.pauseAudio();
        } else if (action == MediaAction.STOP) {
            view.stopAudio();
        } else if (action == MediaAction.SEEK) {
            view.seekAudio(audio.getSeekTo());
        }
    }

    protected void presentText(SlideViewInterface view, TextModel text, RegionModel r, boolean dataChanged) {
        if (dataChanged) {
            view.setText(text.getSrc(), text.getText());
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setTextRegion(transformWidth(r.getLeft()), transformHeight(r.getTop()), transformWidth(r.getWidth()), transformHeight(r.getHeight()));
        }
        view.setTextVisibility(text.isVisible());
    }

    protected void presentImage(SlideViewInterface view, ImageModel image, RegionModel r, boolean dataChanged) {
        int transformedWidth = transformWidth(r.getWidth());
        int transformedHeight = transformWidth(r.getHeight());
        if (dataChanged) {
            if ("image/gif".equalsIgnoreCase(image.getContentType())) {
                if (!view.setGifImage(image.getSrc(), image.getUri())) {
                    if (view instanceof SlideView) {
                        ((SlideView) view).setImageModle(image);
                    } else {
                        view.setImage(image.getSrc(), image.getBitmap(transformedWidth, transformedHeight));
                    }
                }
            } else if (view instanceof SlideView) {
                ((SlideView) view).setImageModle(image);
            } else {
                view.setImage(image.getSrc(), image.getBitmap(transformedWidth, transformedHeight));
            }
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setImageRegion(transformWidth(r.getLeft()), transformHeight(r.getTop()), transformedWidth, transformedHeight);
        }
        view.setImageRegionFit(r.getFit());
        view.setImageVisibility(image.isVisible());
    }

    protected void presentVideo(SlideViewInterface view, VideoModel video, RegionModel r, boolean dataChanged) {
        if (dataChanged) {
            view.setVideo(video.getSrc(), video.getUri());
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setVideoRegion(transformWidth(r.getLeft()), transformHeight(r.getTop()), transformWidth(r.getWidth()), transformHeight(r.getHeight()));
        }
        view.setVideoVisibility(video.isVisible());
        MediaAction action = video.getCurrentAction();
        if (action == MediaAction.START) {
            view.startVideo();
        } else if (action == MediaAction.PAUSE) {
            view.pauseVideo();
        } else if (action == MediaAction.STOP) {
            view.stopVideo();
        } else if (action == MediaAction.SEEK) {
            view.seekVideo(video.getSeekTo());
        }
    }

    public void setLocation(int location) {
        this.mLocation = location;
    }

    public int getLocation() {
        return this.mLocation;
    }

    public void goBackward() {
        if (this.mLocation > 0) {
            this.mLocation--;
        }
    }

    public void goForward() {
        if (this.mLocation < this.mSlideNumber - 1) {
            this.mLocation++;
        }
    }

    public void onModelChanged(final Model model, final boolean dataChanged) {
        if (this.mView instanceof SlideViewInterface) {
            final SlideViewInterface view = this.mView;
            if (!(model instanceof SlideshowModel)) {
                if (model instanceof SlideModel) {
                    if (((SlideModel) model).isVisible()) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                SlideshowPresenter.this.presentSlide(view, (SlideModel) model);
                            }
                        });
                    } else {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                SlideshowPresenter.this.goForward();
                            }
                        });
                    }
                } else if (model instanceof MediaModel) {
                    if (model instanceof RegionMediaModel) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                SlideshowPresenter.this.presentRegionMedia(view, (RegionMediaModel) model, dataChanged);
                            }
                        });
                    } else if (((MediaModel) model).isAudio() && (model instanceof AudioModel)) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                SlideshowPresenter.this.presentAudio(view, (AudioModel) model, dataChanged);
                            }
                        });
                    }
                } else if (model instanceof RegionModel) {
                }
            }
        }
    }

    public void cancelBackgroundLoading() {
    }
}
