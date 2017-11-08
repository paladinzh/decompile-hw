package com.android.mms.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.text.method.HideReturnsTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.BadTokenException;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.layout.LayoutManager;
import com.android.mms.model.ImageModel;
import com.android.mms.ui.AdaptableSlideViewInterface.OnSizeChangedListener;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class SlideView extends AbsoluteLayout implements AdaptableSlideViewInterface {
    private View mAudioInfoView;
    private MediaPlayer mAudioPlayer;
    private boolean mConformanceMode;
    private Context mContext;
    private GifView mImageView;
    private OnClickListener mImagelistener;
    private boolean mIsPrepared;
    private MediaController mMediaController;
    OnPreparedListener mPreparedListener = new OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            SlideView.this.mIsPrepared = true;
            if (SlideView.this.mSeekWhenPrepared > 0) {
                SlideView.this.mAudioPlayer.seekTo(SlideView.this.mSeekWhenPrepared);
                SlideView.this.mSeekWhenPrepared = 0;
            }
            if (SlideView.this.mStartWhenPrepared) {
                SlideView.this.mAudioPlayer.start();
                SlideView.this.mStartWhenPrepared = false;
                SlideView.this.displayAudioInfo();
            }
            if (SlideView.this.mStopWhenPrepared) {
                SlideView.this.mAudioPlayer.stop();
                SlideView.this.mAudioPlayer.release();
                SlideView.this.mAudioPlayer = null;
                SlideView.this.mStopWhenPrepared = false;
                SlideView.this.hideAudioInfo();
            }
        }
    };
    private ScrollView mScrollText;
    private ScrollView mScrollViewPort;
    private int mSeekWhenPrepared;
    private OnSizeChangedListener mSizeChangedListener;
    private boolean mStartWhenPrepared;
    private boolean mStopWhenPrepared;
    private TextView mTextView;
    private VideoView mVideoView;
    private LinearLayout mViewPort;

    private static class Position {
        public int mLeft;
        public int mTop;

        public Position(int left, int top) {
            this.mTop = top;
            this.mLeft = left;
        }
    }

    public SlideView(Context context) {
        super(context);
        this.mContext = context;
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setImageModle(ImageModel imageModle) {
        hideAudioInfo();
        if (this.mImageView == null) {
            this.mImageView = new GifView(this.mContext);
            this.mImageView.setId(R.id.gif_view);
            this.mImageView.setPadding(5, 5, 5, 5);
            addView(this.mImageView, new LayoutParams(-1, -1, 0, 0));
        }
        this.mImageView.setImageModel(imageModle);
        if (!this.mImageView.hasOnClickListeners()) {
            this.mImageView.setOnClickListener(this.mImagelistener);
        }
    }

    public void setImageClickListener(OnClickListener listener) {
        this.mImagelistener = listener;
    }

    public void setImage(String name, Bitmap bitmap) {
        hideAudioInfo();
        if (this.mImageView == null) {
            this.mImageView = new GifView(this.mContext);
            this.mImageView.setId(R.id.gif_view);
            this.mImageView.setPadding(5, 5, 5, 5);
            addView(this.mImageView, new LayoutParams(-1, -1, 0, 0));
        }
        if (!this.mImageView.hasOnClickListeners()) {
            this.mImageView.setOnClickListener(this.mImagelistener);
        }
        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_bottom_emui);
            } catch (OutOfMemoryError e) {
                MLog.e("SlideView", "setImage: out of memory: ", (Throwable) e);
                return;
            }
        }
        this.mImageView.setVisibility(0);
        this.mImageView.setImageBitmap(bitmap);
    }

    public boolean setGifImage(String name, Uri uri) {
        if (uri == null) {
            setImage(name, null);
            return false;
        }
        hideAudioInfo();
        if (this.mImageView == null) {
            this.mImageView = new GifView(this.mContext);
            this.mImageView.setId(R.id.gif_view);
            this.mImageView.setPadding(5, 5, 5, 5);
            addView(this.mImageView, new LayoutParams(-2, -2, 0, 0));
        }
        if (!this.mImageView.hasOnClickListeners()) {
            this.mImageView.setOnClickListener(this.mImagelistener);
        }
        try {
            return this.mImageView.setOriginalGifImage(uri);
        } catch (OutOfMemoryError e) {
            MLog.e("SlideView", "setGifImage: out of memory: ", (Throwable) e);
            return false;
        }
    }

    public void setImageRegion(int left, int top, int width, int height) {
        if (this.mImageView != null && !this.mConformanceMode) {
            this.mImageView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setImageRegionFit(String fit) {
    }

    public void setVideo(String name, Uri video) {
        hideAudioInfo();
        if (this.mVideoView == null) {
            this.mVideoView = new VideoView(this.mContext);
            addView(this.mVideoView, new LayoutParams(-2, -2, 0, 0));
        }
        this.mVideoView.setVisibility(0);
        this.mVideoView.setVideoURI(video);
    }

    public void setMediaController(MediaController mediaController) {
        this.mMediaController = mediaController;
    }

    private void initAudioInfoView(String name) {
        if (this.mAudioInfoView == null) {
            this.mAudioInfoView = LayoutInflater.from(getContext()).inflate(R.layout.playing_audio_info, null);
            if (!this.mConformanceMode) {
                addView(this.mAudioInfoView, new LayoutParams(-1, 82, 0, getHeight() - 82));
            } else if (this.mViewPort.getOrientation() == 0) {
                addView(this.mAudioInfoView, new LayoutParams(-1, 82, 0, 0));
            } else {
                this.mViewPort.addView(this.mAudioInfoView, new LinearLayout.LayoutParams(-1, 82));
            }
        }
        if (this.mViewPort.getOrientation() == 0) {
            this.mScrollViewPort.setLayoutParams(new LayoutParams(-1, -2, 0, 82));
            this.mViewPort.setMinimumHeight(LayoutManager.getInstance().getLayoutParameters().getHeight() - 164);
        }
        TextView audioName = (TextView) this.mAudioInfoView.findViewById(R.id.name);
        audioName.setText(name);
        audioName.setTextColor(this.mContext.getResources().getColor(R.color.text_color));
        displayAudioInfo();
    }

    private void displayAudioInfo() {
        if (this.mAudioInfoView != null) {
            this.mAudioInfoView.setVisibility(0);
        }
    }

    private void hideAudioInfo() {
        if (this.mAudioInfoView != null) {
            this.mAudioInfoView.setVisibility(8);
        }
    }

    public void setAudio(Uri audio, String name, Map<String, ?> map) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio URI may not be null.");
        }
        if (this.mAudioPlayer != null) {
            this.mAudioPlayer.reset();
            this.mAudioPlayer.release();
            this.mAudioPlayer = null;
        }
        this.mIsPrepared = false;
        this.mStartWhenPrepared = false;
        this.mSeekWhenPrepared = 0;
        this.mStopWhenPrepared = false;
        this.mAudioPlayer = new MediaPlayer();
        try {
            this.mAudioPlayer.setOnPreparedListener(this.mPreparedListener);
            this.mAudioPlayer.setDataSource(this.mContext, audio);
            this.mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            MLog.e("SlideView", "Unexpected IOException.", (Throwable) e);
            this.mAudioPlayer.release();
            this.mAudioPlayer = null;
        }
        initAudioInfoView(name);
    }

    public void setText(String name, String text) {
        if (!this.mConformanceMode) {
            if (this.mScrollText == null) {
                this.mScrollText = new ScrollView(this.mContext);
                this.mScrollText.setScrollBarStyle(50331648);
                addView(this.mScrollText, new LayoutParams(-2, -2, 0, 0));
            }
            if (this.mTextView == null) {
                this.mTextView = new TextView(this.mContext);
                this.mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                this.mScrollText.addView(this.mTextView, new FrameLayout.LayoutParams(-2, -2, 1));
            }
            this.mScrollText.requestFocus();
        }
        this.mTextView.setVisibility(0);
        this.mTextView.setText(SmileyParser.getInstance().addSmileySpans(text, SMILEY_TYPE.MESSAGE_TEXTVIEW));
        this.mTextView.setTextColor(this.mContext.getResources().getColor(R.color.text_color));
        this.mTextView.setTextIsSelectable(true);
        int paddingStartEnd = (int) getResources().getDimension(R.dimen.slide_view_start_end);
        int paddingTopBottom = (int) getResources().getDimension(R.dimen.slide_view_top_bottom);
        this.mTextView.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
    }

    public void setTextRegion(int left, int top, int width, int height) {
        if (this.mScrollText != null && !this.mConformanceMode) {
            this.mScrollText.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setVideoRegion(int left, int top, int width, int height) {
        if (this.mVideoView != null && !this.mConformanceMode) {
            this.mVideoView.setLayoutParams(new LayoutParams(width, height, left, top));
        }
    }

    public void setImageVisibility(boolean visible) {
        int i = 0;
        if (this.mImageView == null) {
            return;
        }
        if (this.mConformanceMode) {
            GifView gifView = this.mImageView;
            if (!visible) {
                i = 8;
            }
            gifView.setVisibility(i);
            return;
        }
        gifView = this.mImageView;
        if (!visible) {
            i = 4;
        }
        gifView.setVisibility(i);
    }

    public void setTextVisibility(boolean visible) {
        int i = 0;
        if (this.mConformanceMode) {
            if (this.mTextView != null) {
                TextView textView = this.mTextView;
                if (!visible) {
                    i = 8;
                }
                textView.setVisibility(i);
            }
        } else if (this.mScrollText != null) {
            ScrollView scrollView = this.mScrollText;
            if (!visible) {
                i = 4;
            }
            scrollView.setVisibility(i);
        }
    }

    public void setVideoVisibility(boolean visible) {
        int i = 0;
        if (this.mVideoView == null) {
            return;
        }
        if (this.mConformanceMode) {
            VideoView videoView = this.mVideoView;
            if (!visible) {
                i = 8;
            }
            videoView.setVisibility(i);
            return;
        }
        videoView = this.mVideoView;
        if (!visible) {
            i = 4;
        }
        videoView.setVisibility(i);
    }

    public void startAudio() {
        if (this.mAudioPlayer == null || !this.mIsPrepared) {
            this.mStartWhenPrepared = true;
            return;
        }
        this.mAudioPlayer.start();
        this.mStartWhenPrepared = false;
        displayAudioInfo();
    }

    public void stopAudio() {
        if (this.mAudioPlayer == null || !this.mIsPrepared) {
            this.mStopWhenPrepared = true;
            return;
        }
        this.mAudioPlayer.stop();
        this.mAudioPlayer.release();
        this.mAudioPlayer = null;
    }

    public void pauseAudio() {
        if (this.mAudioPlayer != null && this.mIsPrepared && this.mAudioPlayer.isPlaying()) {
            this.mAudioPlayer.pause();
        }
        this.mStartWhenPrepared = false;
    }

    public void seekAudio(int seekTo) {
        if (this.mAudioPlayer == null || !this.mIsPrepared) {
            this.mSeekWhenPrepared = seekTo;
        } else {
            this.mAudioPlayer.seekTo(seekTo);
        }
    }

    public void startVideo() {
        if (this.mVideoView != null) {
            this.mVideoView.start();
        }
    }

    public void stopVideo() {
        if (this.mVideoView != null) {
            this.mVideoView.stopPlayback();
        }
    }

    public void pauseVideo() {
        if (this.mVideoView != null) {
            this.mVideoView.pause();
        }
    }

    public void seekVideo(int seekTo) {
        if (this.mVideoView != null && seekTo > 0) {
            this.mVideoView.seekTo(seekTo);
        }
    }

    public void reset() {
        if (this.mScrollText != null) {
            this.mScrollText.setVisibility(8);
        }
        if (this.mImageView != null) {
            this.mImageView.setVisibility(8);
        }
        if (this.mAudioPlayer != null) {
            stopAudio();
        }
        if (this.mVideoView != null) {
            stopVideo();
            this.mVideoView.setVisibility(8);
        }
        if (this.mTextView != null) {
            this.mTextView.setVisibility(8);
        }
        try {
            if (this.mScrollViewPort != null) {
                this.mScrollViewPort.scrollTo(0, 0);
                this.mScrollViewPort.setLayoutParams(new LayoutParams(-1, -1, 0, 0));
            }
        } catch (BadTokenException e) {
            MLog.e("SlideView", "SlideView -> reset() --- error");
        } catch (Exception e2) {
            MLog.e("SlideView", e2.getMessage(), (Throwable) e2);
        }
        if (this.mViewPort != null && this.mViewPort.getOrientation() == 0) {
            this.mViewPort.setMinimumHeight(LayoutManager.getInstance().getLayoutParameters().getHeight());
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mSizeChangedListener != null) {
            this.mSizeChangedListener.onSizeChanged(w, h - 82);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        this.mSizeChangedListener = l;
    }

    public void enableMMSConformanceMode(int textLeft, int textTop, int imageLeft, int imageTop) {
        this.mConformanceMode = true;
        boolean needHorizontal = textTop == imageTop && textLeft != imageLeft;
        if (!hasOnClickListeners()) {
            setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (SlideView.this.mMediaController != null) {
                        SlideView.this.mMediaController.show();
                    }
                }
            });
        }
        if (this.mScrollViewPort == null) {
            this.mScrollViewPort = new ScrollView(this.mContext) {
                private int mBottomY;

                protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                    int i = 0;
                    super.onLayout(changed, left, top, right, bottom);
                    if (getChildCount() > 0) {
                        int childHeight = getChildAt(0).getHeight();
                        int height = getHeight();
                        if (height < childHeight) {
                            i = childHeight - height;
                        }
                        this.mBottomY = i;
                    }
                }

                protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                    if ((t == 0 || t >= this.mBottomY) && SlideView.this.mMediaController != null && !((SlideshowActivity) this.mContext).isFinishing()) {
                        SlideView.this.mMediaController.show();
                    }
                }

                public boolean onTouchEvent(MotionEvent event) {
                    super.onTouchEvent(event);
                    if (getChildCount() <= 0 || getChildAt(0).getHeight() < getHeight()) {
                        return false;
                    }
                    return true;
                }
            };
            this.mScrollViewPort.setScrollBarStyle(0);
            this.mViewPort = new LinearLayout(this.mContext);
            this.mViewPort.setId(R.id.view_port_linearlayout);
            if (needHorizontal) {
                this.mViewPort.setMinimumHeight(LayoutManager.getInstance().getLayoutParameters().getHeight());
                this.mViewPort.setMinimumWidth(LayoutManager.getInstance().getLayoutParameters().getWidth());
                this.mViewPort.setOrientation(0);
            } else {
                this.mViewPort.setOrientation(1);
            }
            this.mViewPort.setGravity(17);
            this.mViewPort.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (SlideView.this.mMediaController != null) {
                        SlideView.this.mMediaController.show();
                    }
                }
            });
            this.mScrollViewPort.addView(this.mViewPort, new LinearLayout.LayoutParams(-1, needHorizontal ? -1 : -2));
            addView(this.mScrollViewPort);
        }
        TreeMap<Position, View> viewsByPosition = new TreeMap(new Comparator<Position>() {
            public int compare(Position p1, Position p2) {
                int l1 = p1.mLeft;
                int t1 = p1.mTop;
                int l2 = p2.mLeft;
                int res = t1 - p2.mTop;
                if (res == 0) {
                    res = l1 - l2;
                }
                if (res == 0) {
                    return -1;
                }
                return res;
            }
        });
        if (textLeft >= 0 && textTop >= 0) {
            this.mTextView = new TextView(this.mContext);
            this.mTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            if (needHorizontal) {
                this.mTextView.setGravity(17);
            }
            this.mTextView.setTextSize(18.0f);
            this.mTextView.setPadding(5, 5, 5, 5);
            this.mTextView.setTextColor(this.mContext.getResources().getColor(R.color.text_color));
            this.mTextView.setAutoLinkMask(15);
            viewsByPosition.put(new Position(textLeft, textTop), this.mTextView);
        }
        if (imageLeft >= 0 && imageTop >= 0) {
            this.mImageView = new GifView(this.mContext);
            this.mImageView.setId(R.id.gif_view);
            this.mImageView.setPadding(5, 5, 5, 5);
            viewsByPosition.put(new Position(imageLeft, imageTop), this.mImageView);
            this.mVideoView = new VideoView(this.mContext);
            viewsByPosition.put(new Position(imageLeft + 1, imageTop), this.mVideoView);
        }
        for (View view : viewsByPosition.values()) {
            if (!(view instanceof VideoView)) {
                int layoutWidth = -1;
                if ((view instanceof TextView) || (view instanceof GifView)) {
                    layoutWidth = -2;
                }
                this.mViewPort.addView(view, new LinearLayout.LayoutParams(layoutWidth, -2));
            } else if (needHorizontal) {
                this.mViewPort.addView(view, new LinearLayout.LayoutParams(-1, LayoutManager.getInstance().getLayoutParameters().getHeight(), ContentUtil.FONT_SIZE_NORMAL));
            } else {
                this.mViewPort.addView(view, new LinearLayout.LayoutParams(-1, LayoutManager.getInstance().getLayoutParameters().getHeight()));
            }
            view.setVisibility(8);
        }
    }

    public void setVideoThumbnail(String name, Bitmap bitmap) {
    }

    public void setSize(int size) {
    }

    public void setVcard(String textSub1, String textSub2) {
        setImage(textSub1, null);
    }

    public void setVcalendar(String time, String title) {
        setImage(time, null);
    }
}
