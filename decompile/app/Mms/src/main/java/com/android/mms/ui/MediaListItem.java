package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VcardModel;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.android.mms.util.VcardMessageHelper;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.ui.RiskUrlThreadPool;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.CommonGatherLinks;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.MmsPduUtils;
import com.huawei.mms.util.MmsScaleSupport.ScalableTextView;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.mms.util.TextSpan;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MediaListItem extends RelativeLayout implements SpandTouchMonitor, CheckableView, OnLongClickListener, OnClickListener {
    private ScalableTextView mAudioDuration;
    private ScalableTextView mAudioName;
    private ImageView mAudioPlayButton;
    private ScalableTextView mAudioPosition;
    private View mAudioView;
    private CheckBox mCheckBox;
    private List<TextSpan> mContentSpans = null;
    private long mDate;
    float mFontScale = ContentUtil.FONT_SIZE_NORMAL;
    private View mHeaderView;
    private ImageLoadedCallback mImageLoadedCallback;
    private GifView mImageView;
    private MediaItem mMediaItem;
    private final OnCreateContextMenuListener mMediaMenuCreateListener = new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (MediaListItem.this.mMediaItem.mMedia != null) {
                final MediaModel fMediaSelected = MediaListItem.this.mMediaItem.mMedia;
                menu.setHeaderTitle(fMediaSelected.getSrc());
                OnMenuItemClickListener l = new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 0:
                                try {
                                    Intent intent = new Intent("android.intent.action.VIEW");
                                    intent.addFlags(1);
                                    intent.setDataAndType(fMediaSelected.getUri(), fMediaSelected.getContentType());
                                    MediaListItem.this.getContext().startActivity(intent);
                                } catch (Exception e) {
                                    MessageUtils.showErrorDialog((Activity) MediaListItem.this.mContext, MediaListItem.this.getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
                                    e.printStackTrace();
                                }
                                return true;
                            case 1:
                                MediaListItem.this.saveMediaItem(MediaListItem.this.getContext(), fMediaSelected);
                                return true;
                            default:
                                return true;
                        }
                    }
                };
                if (!(fMediaSelected.isText() || fMediaSelected.isImage())) {
                    menu.add(0, 0, 0, R.string.view).setOnMenuItemClickListener(l);
                    menu.add(0, 1, 0, R.string.save).setOnMenuItemClickListener(l);
                }
            }
        }
    };
    private View mPageDivider;
    private ScalableTextView mPageText;
    private int mPostion;
    private Handler mProcessBarHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean mediaPlayerIsNull = false;
            switch (msg.what) {
                case 1:
                    if (MediaListItem.this.mMediaItem.isAudioMode() && MediaListItem.this.mMediaItem.mAudioPlaying) {
                        if (MediaListItem.this.mSlideSmoothShowFragment == null || MediaListItem.this.mSlideSmoothShowFragment.mMediaPlayer == null) {
                            mediaPlayerIsNull = true;
                        }
                        int pos = mediaPlayerIsNull ? MediaListItem.this.mMediaItem.mAudioDuration : MediaListItem.this.mSlideSmoothShowFragment.mMediaPlayer.getCurrentPosition();
                        int posPercent = MediaListItem.this.setProgress(pos, mediaPlayerIsNull ? MediaListItem.this.mMediaItem.mAudioDuration : MediaListItem.this.mSlideSmoothShowFragment.mMediaPlayer.getDuration());
                        if (MediaListItem.this.mMediaItem.mAudioPlaying) {
                            sendMessageDelayed(obtainMessage(1), 500);
                        }
                        if (((double) posPercent) >= 950.0d || MediaListItem.this.mMediaItem.mAudioDuration - pos < VTMCDataCache.MAXSIZE) {
                            MediaListItem.this.setProgress(1, 1);
                            sendEmptyMessage(2);
                            return;
                        }
                        return;
                    } else if (MediaListItem.this.mMediaItem.isAudioMode() && !MediaListItem.this.mMediaItem.mAudioPlaying && MediaListItem.this.mProgress.getProgress() < 1000 && MediaListItem.this.mProgress.getProgress() > 0) {
                        MediaListItem.this.setProgress(1, 1);
                        MediaListItem.this.doAudioStoppedDelayed();
                        return;
                    } else if (MediaListItem.this.mMediaItem.isAudioMode() && !MediaListItem.this.mMediaItem.mAudioPlaying && MediaListItem.this.mProgress.getProgress() == 1000) {
                        MediaListItem.this.doAudioStoppedDelayed();
                        return;
                    } else {
                        return;
                    }
                case 2:
                    if (MediaListItem.this.mMediaItem.isAudioMode() && MediaListItem.this.mMediaItem.mAudioPlaying) {
                        MediaListItem.this.doAudioStopped();
                        removeMessages(1);
                        removeMessages(2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ProgressBar mProgress;
    private ImageView mSaveBtn;
    private SlideSmootShowFragment mSlideSmoothShowFragment = null;
    private ScalableTextView mSubjectView;
    private ScalableTextView mTextView;
    private ImageView mVideoImage;
    private ImageView mVideoPlayButton;
    private View mVideoView;

    private class ImageLoadedCallback implements ItemLoadedCallback<ImageLoaded> {
        private int mLastPosition;

        private ImageLoadedCallback() {
        }

        public void reset(int position) {
            this.mLastPosition = position;
        }

        public void onItemLoaded(ImageLoaded imageLoaded, Throwable exception) {
            if (this.mLastPosition == MediaListItem.this.mPostion && !imageLoaded.mIsVideo) {
                MediaListItem.this.mImageView.setImageBitmap(imageLoaded.mBitmap, 0.0f);
            }
        }
    }

    public static class SaveButton extends ImageView {
        public SaveButton(Context context) {
            super(context);
        }

        public SaveButton(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SaveButton(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    setAlpha(0.5f);
                    break;
                case 1:
                case 3:
                case 10:
                    setAlpha(ContentUtil.FONT_SIZE_NORMAL);
                    break;
            }
            return super.onTouchEvent(event);
        }
    }

    public MediaListItem(Context context) {
        super(context);
    }

    public MediaListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(SlideSmootShowFragment fragment) {
        if (fragment == null) {
            MLog.e("MediaListItem", "null fragment");
            return;
        }
        this.mSlideSmoothShowFragment = fragment;
        this.mDate = getSlideTime();
    }

    public void setMediaItem(MediaItem mediaitem, boolean isEditMode) {
        boolean contentChanged = mediaitem != this.mMediaItem;
        this.mMediaItem = mediaitem;
        this.mSaveBtn.setVisibility(8);
        this.mHeaderView.setVisibility(8);
        this.mSubjectView.setVisibility(8);
        this.mPageDivider.setVisibility(8);
        this.mVideoView.setVisibility(8);
        this.mAudioView.setVisibility(8);
        this.mTextView.setVisibility(8);
        this.mImageView.setImageBitmap(null);
        if (this.mMediaItem != null) {
            SpannableStringBuilder buf;
            if (this.mMediaItem.isHeaderMode()) {
                if (!isEditMode) {
                    this.mHeaderView.setVisibility(0);
                    if (!this.mMediaItem.isSubjectEmpty()) {
                        buf = new SpannableStringBuilder();
                        CharSequence smilizedSubject = SmileyParser.getInstance().addSmileySpans(this.mMediaItem.mSubject, SMILEY_TYPE.MESSAGE_EDITTEXT);
                        buf.append(TextUtils.replace(getResources().getString(R.string.inline_subject_new), new String[]{"%s"}, new CharSequence[]{smilizedSubject}));
                        this.mSubjectView.setVisibility(0);
                        this.mSubjectView.setText(buf);
                    }
                }
            } else if (this.mMediaItem.mItemMode == 1) {
                this.mPageDivider.setVisibility(0);
                this.mPageText.setText(this.mMediaItem.getPageText());
            } else if (this.mMediaItem.mItemMode == 5) {
                this.mVideoView.setVisibility(0);
                this.mVideoImage.setImageBitmap(ResEx.getRoundedCornerBitmap(MessageUtils.createVideoThumbnail(this.mContext, this.mMediaItem.mMedia.getUri()), (float) MessageUtils.dipToPx(this.mContext, 0.0f)));
                this.mVideoPlayButton.setImageResource(R.drawable.mms_ic_item_video);
            } else if (this.mMediaItem.mItemMode == 4) {
                this.mAudioView.setVisibility(0);
                this.mAudioName.setText(this.mMediaItem.mMedia.getSrc());
                this.mAudioDuration.setText(stringForTime(this.mMediaItem.mAudioDuration));
                this.mProgress.setEnabled(true);
                this.mProgress.setMax(1000);
                int progressPos = 0;
                int progressDur = 0;
                if (this.mMediaItem.mAudioPlaying) {
                    this.mAudioPlayButton.setImageResource(R.drawable.mms_stop_btn);
                    this.mProcessBarHandler.sendEmptyMessage(1);
                    if (!(this.mSlideSmoothShowFragment == null || this.mSlideSmoothShowFragment.mMediaPlayer == null)) {
                        progressPos = this.mSlideSmoothShowFragment.mMediaPlayer.getCurrentPosition();
                        progressDur = this.mSlideSmoothShowFragment.mMediaPlayer.getDuration();
                    }
                } else {
                    this.mAudioPlayButton.setImageResource(R.drawable.mms_play_btn);
                    progressDur = this.mMediaItem.mAudioDuration;
                }
                setProgress(progressPos, progressDur);
                if (this.mMediaItem.mIsAutoPlayAudio) {
                    playAudioControl();
                    this.mMediaItem.setAutoPlayAudio(false);
                }
            } else if (this.mMediaItem.mItemMode == 3) {
                try {
                    this.mImageView.setVisibility(0);
                    ImageModel imageModel = this.mMediaItem.mMedia;
                    if (this.mImageLoadedCallback == null) {
                        MediaListItem mediaListItem = this;
                        this.mImageLoadedCallback = new ImageLoadedCallback();
                    }
                    this.mImageLoadedCallback.reset(this.mPostion);
                    if ("image/gif".equalsIgnoreCase(imageModel.getContentType())) {
                        if (!(this.mImageView.setOriginalGifImage(imageModel.getUri()) || this.mSlideSmoothShowFragment == null)) {
                            this.mImageView.loadImageAsync(imageModel, this.mSlideSmoothShowFragment.getImageLoader(), this.mImageLoadedCallback);
                        }
                    } else if (this.mSlideSmoothShowFragment != null) {
                        this.mImageView.loadImageAsync(imageModel, this.mSlideSmoothShowFragment.getImageLoader(), this.mImageLoadedCallback);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e2) {
                    MLog.e("MediaListItem", " set Image out of memory ");
                }
            } else if (this.mMediaItem.mItemMode == 2 && !isEditMode) {
                String content = ((TextModel) this.mMediaItem.mMedia).getText();
                if (TextUtils.isEmpty(content)) {
                    this.mTextView.setVisibility(8);
                    return;
                }
                this.mTextView.setVisibility(0);
                buf = new SpannableStringBuilder();
                buf.append(SmileyParser.getInstance().addSmileySpans(content, SMILEY_TYPE.MESSAGE_TEXTVIEW));
                if (contentChanged || this.mContentSpans == null) {
                    this.mContentSpans = null;
                    if (content.length() <= 1000) {
                        final int[] addrPosInBody = HwMessageUtils.getAddrFromTMRManager(content);
                        final int[] datePosInBody = HwMessageUtils.getTimePosition(content);
                        this.mContentSpans = CommonGatherLinks.getTextSpans(addrPosInBody, datePosInBody, content, getContext(), this.mDate);
                        if (HwMessageUtils.getRiskUrlEnable(getContext())) {
                            new AsyncTask<String, Integer, String>() {
                                protected String doInBackground(String... arg) {
                                    MediaListItem.this.mContentSpans = CommonGatherLinks.getTextSpans(addrPosInBody, datePosInBody, HwMessageUtils.spanStringToPosition(HwMessageUtils.getRiskUrlPosString(MediaListItem.this.getContext(), arg[0])), arg[0], MediaListItem.this.getContext(), MediaListItem.this.mDate);
                                    return null;
                                }

                                protected void onPostExecute(String result) {
                                    MediaListItem.this.mTextView.setText(buf, MediaListItem.this.mContentSpans);
                                }
                            }.executeOnExecutor(RiskUrlThreadPool.getDefault(), new String[]{content});
                        }
                    }
                }
                this.mTextView.setText(buf, this.mContentSpans);
                this.mTextView.setTextSize(15.0f);
            }
        }
    }

    public void onClick(View v) {
        if (this.mCheckBox.getVisibility() != 0) {
            switch (v.getId()) {
                case R.id.image_item:
                    if (this.mSlideSmoothShowFragment != null) {
                        this.mSlideSmoothShowFragment.viewImagesInGallery(this.mMediaItem.mIndex);
                        break;
                    }
                    break;
                case R.id.save_btn:
                    saveMediaItem(getContext(), this.mMediaItem.mMedia);
                    StatisticalHelper.reportEvent(getContext(), 2235, this.mMediaItem.mMedia.getContentType());
                    break;
                case R.id.play_audio_button:
                    playAudioControl();
                    break;
                case R.id.video_play_button:
                    if (this.mSlideSmoothShowFragment != null) {
                        this.mSlideSmoothShowFragment.stopAudio();
                    }
                    viewMedia();
                    break;
            }
        }
    }

    public void viewMedia() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(1);
        intent.putExtra("SingleItemOnly", true);
        MediaModel mm = this.mMediaItem.mMedia;
        String contentType = mm.getContentType();
        Uri dataUri = mm.getUri();
        if ("application/vnd.oma.drm.message".equals(contentType) || "application/vnd.oma.drm.content".equals(contentType)) {
            contentType = MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(dataUri);
        }
        intent.setDataAndType(dataUri, contentType);
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            MessageUtils.showErrorDialog((Activity) getContext(), getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
            e.printStackTrace();
        }
    }

    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.text_item:
                if (this.mMediaItem.mMedia != null && this.mSlideSmoothShowFragment != null) {
                    MessageUtils.viewText(getContext(), ((TextModel) this.mMediaItem.mMedia).getText());
                    break;
                }
                return false;
                break;
            case R.id.play_audio_button:
            case R.id.video_image:
            case R.id.video_play_button:
                return v.showContextMenu();
        }
        return false;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCheckBox = (CheckBox) findViewById(R.id.select);
        this.mSaveBtn = (ImageView) findViewById(R.id.save_btn);
        this.mHeaderView = findViewById(R.id.header);
        this.mSaveBtn.setBackgroundResource(R.drawable.mms_ic_save);
        this.mSaveBtn.setOnClickListener(this);
        this.mPageDivider = findViewById(R.id.page_devider);
        this.mPageText = ScalableTextView.create(findViewById(R.id.page_text));
        SpandTextView tview = (SpandTextView) findViewById(R.id.subject_view);
        this.mSubjectView = ScalableTextView.create(tview);
        tview.setSpandTouchMonitor(new SpandTouchMonitor() {
            public void onTouchOutsideSpanText() {
            }

            public void onSpanTextPressed(boolean pressed) {
            }

            public boolean isEditTextClickable() {
                return true;
            }

            public boolean onDoubleTapUp(boolean isLink) {
                CharSequence str = MediaListItem.this.mSubjectView.getText();
                if (!(TextUtils.isEmpty(str) || MediaListItem.this.mSlideSmoothShowFragment == null)) {
                    MessageUtils.viewText(MediaListItem.this.getContext(), str.toString());
                }
                return true;
            }

            public void onTouchLink(ClickableSpan span) {
            }
        });
        tview = (SpandTextView) findViewById(R.id.text_item);
        tview.setTextColor(getResources().getColor(R.drawable.text_color_black));
        tview.setSpandTouchMonitor(this);
        tview.setOnLongClickListener(this);
        this.mTextView = ScalableTextView.create(tview);
        this.mImageView = (GifView) findViewById(R.id.image_item);
        this.mVideoView = ((ViewStub) findViewById(R.id.video_item)).inflate();
        this.mVideoPlayButton = (ImageView) findViewById(R.id.video_play_button);
        this.mVideoImage = (ImageView) findViewById(R.id.video_image);
        this.mAudioView = ((ViewStub) findViewById(R.id.audio_item)).inflate();
        this.mAudioName = ScalableTextView.create(findViewById(R.id.audio_name));
        this.mAudioPlayButton = (ImageView) findViewById(R.id.play_audio_button);
        this.mAudioPosition = ScalableTextView.create(findViewById(R.id.audio_position));
        this.mAudioDuration = ScalableTextView.create(findViewById(R.id.audio_duration));
        this.mProgress = (ProgressBar) findViewById(R.id.play_audio_progress);
        checkWidgetClickEvent(this.mVideoPlayButton, true);
        checkWidgetClickEvent(this.mAudioPlayButton, true);
        checkWidgetClickEvent(this.mImageView, true);
        checkTextSize();
        if (MmsConfig.isSmsEnabled(getContext())) {
            this.mSaveBtn.getBackground().setAlpha(255);
            return;
        }
        this.mSaveBtn.setEnabled(false);
        this.mSaveBtn.getBackground().setAlpha(85);
    }

    private void checkClickEvent() {
        boolean clickable = !isEditAble();
        if (clickable) {
            setOnCreateContextMenuListener(this.mMediaMenuCreateListener);
        } else {
            setClickable(false);
            setLongClickable(false);
        }
        checkWidgetClickEvent(this.mVideoImage, clickable);
        checkWidgetClickEvent(this.mImageView, clickable);
    }

    private void checkWidgetClickEvent(View v, boolean checkable) {
        if (v != null) {
            if (checkable) {
                v.setOnClickListener(this);
                v.setOnLongClickListener(this);
            } else {
                v.setClickable(false);
                v.setLongClickable(false);
            }
        }
    }

    private void checkTextSize() {
        this.mAudioName.setTextSize(14.0f);
        this.mPageText.setTextSize(9.0f);
        this.mSubjectView.setTextSize(14.0f);
    }

    public void setPosition(int pos) {
        this.mPostion = pos;
    }

    public boolean canBeSaved() {
        return this.mMediaItem.canBeSaved();
    }

    private void playAudioControl() {
        if (this.mSlideSmoothShowFragment != null) {
            if (this.mMediaItem.mAudioPlaying) {
                this.mSlideSmoothShowFragment.stopAudio();
            } else {
                this.mSlideSmoothShowFragment.stopAudio();
                stopExternalAudio();
                stopFM_Music();
                this.mSlideSmoothShowFragment.playAudio(this.mMediaItem);
                this.mProcessBarHandler.sendEmptyMessage(1);
                this.mAudioPlayButton.setImageResource(R.drawable.mms_stop_btn);
            }
            this.mSlideSmoothShowFragment.mListAdapter.notifyDataSetChanged();
        }
    }

    private String stringForTime(int timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;
        String result = "";
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
        }
        return formatter.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
    }

    private int setProgress(int pos, int dur) {
        int realpos = 0;
        if (dur > 0) {
            realpos = (pos * 1000) / dur;
        }
        this.mProgress.setProgress(realpos);
        this.mAudioPosition.setText(stringForTime(pos));
        return realpos;
    }

    private void doAudioStopped() {
        MLog.i("MediaListItem", "doAudioStopped()");
        this.mMediaItem.audioStopped();
        this.mAudioPlayButton.setImageResource(R.drawable.mms_play_btn);
        setProgress(0, this.mMediaItem.mAudioDuration);
        if (this.mSlideSmoothShowFragment != null) {
            this.mSlideSmoothShowFragment.stopAudio();
        }
    }

    private void doAudioStoppedDelayed() {
        this.mProcessBarHandler.postDelayed(new Runnable() {
            public void run() {
                MediaListItem.this.doAudioStopped();
            }
        }, 0);
    }

    public void stopExternalAudio() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        getContext().sendBroadcast(i);
    }

    public void stopFM_Music() {
        getContext().sendBroadcast(new Intent("com.huawei.android.FMRadio.fmradioservicecommand.stop"), "com.android.huawei.permission.OUTSIDE_STOP_FM");
    }

    private boolean saveMediaItem(Context context, MediaModel fMediaSelected) {
        if (((long) fMediaSelected.getMediaSize()) > ComposeMessageFragment.getAvailableSpace()) {
            Toast.makeText(context, R.string.hint_no_enough_space2, 0).show();
            return true;
        }
        PduPart pp = null;
        if (this.mSlideSmoothShowFragment != null) {
            pp = MmsPduUtils.getPduPartForName(this.mSlideSmoothShowFragment.mModel, fMediaSelected.getSrc());
        }
        if (pp != null) {
            if (!TextUtils.isEmpty(fMediaSelected.getSrc())) {
                pp.setFilename(fMediaSelected.getSrc().getBytes(Charset.defaultCharset()));
            }
            return MmsPduUtils.copyPartAndShowResult(getContext(), pp);
        }
        MLog.e("MediaListItem", "!!Copy null part");
        return false;
    }

    public static boolean viewVcardDetail(VcardModel mediaModel, Context context) {
        VcardMessageHelper vCardMessageHelper = createMmsVcardMessageHelper(mediaModel, context);
        if (vCardMessageHelper == null) {
            return false;
        }
        vCardMessageHelper.viewVcardDetail();
        return true;
    }

    private static VcardMessageHelper createMmsVcardMessageHelper(VcardModel vcardModel, Context context) {
        try {
            return new VcardMessageHelper(context, vcardModel.getData(), vcardModel.getVcardDetailList(), vcardModel.getBitmap());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onTouchOutsideSpanText() {
    }

    public void onSpanTextPressed(boolean pressed) {
        this.mTextView.setPressed(pressed);
    }

    public boolean isEditTextClickable() {
        return true;
    }

    public void updateMsgText() {
        if (this.mMediaItem != null) {
            SpannableStringBuilder buf = new SpannableStringBuilder();
            SmileyParser parser = SmileyParser.getInstance();
            if (this.mMediaItem.mItemMode == 2 && this.mMediaItem.mMedia != null) {
                CharSequence content = ((TextModel) this.mMediaItem.mMedia).getText();
                if (!TextUtils.isEmpty(content)) {
                    this.mTextView.setVisibility(0);
                    buf.append(parser.addSmileySpans(content, SMILEY_TYPE.MESSAGE_TEXTVIEW, this.mFontScale));
                    this.mTextView.setText(buf, this.mContentSpans);
                }
            }
            buf.clear();
            if (this.mMediaItem.isHeaderMode() && !this.mMediaItem.isSubjectEmpty()) {
                CharSequence smilizedSubject = parser.addSmileySpans(this.mMediaItem.mSubject, SMILEY_TYPE.MESSAGE_EDITTEXT, this.mFontScale);
                buf.append(TextUtils.replace(getResources().getString(R.string.inline_subject_new), new String[]{"%s"}, new CharSequence[]{smilizedSubject}));
                this.mSubjectView.setText(buf);
            }
        }
    }

    public void setTextScale(float scale) {
        this.mFontScale = scale;
        updateMsgText();
        float init_fontsize = HwUiStyleUtils.getScalableFontSize(getResources());
        if (MmsConfig.isEnableZoomWhenView()) {
            this.mTextView.setTextSize(init_fontsize * scale);
            this.mPageText.setTextSize(init_fontsize * scale);
            this.mAudioName.setTextSize(init_fontsize * scale);
        }
    }

    public void setEditAble(boolean editAble) {
        int i;
        editAble = editAble ? canBeSaved() : false;
        CheckBox checkBox = this.mCheckBox;
        if (editAble) {
            i = 0;
        } else {
            i = 8;
        }
        checkBox.setVisibility(i);
        if (editAble) {
            this.mSaveBtn.setVisibility(8);
        } else if (this.mMediaItem.canBeSaved()) {
            this.mSaveBtn.setVisibility(0);
        }
        checkClickEvent();
    }

    public void setEditAble(boolean editable, boolean checked) {
        setEditAble(editable);
        if (!editable) {
            checked = false;
        }
        setChecked(checked);
        requestDisallowInterceptTouchEvent(editable);
    }

    public boolean isEditAble() {
        return this.mCheckBox != null && this.mCheckBox.getVisibility() == 0;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBox != null) {
            this.mCheckBox.setChecked(checked);
            refreshDrawableState();
        }
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public void hideText(boolean isEditMode) {
        if (!(this.mTextView == null || this.mTextView.get() == null)) {
            if (isEditMode) {
                this.mTextView.setVisibility(8);
            } else if (this.mMediaItem.isTextMode() && !TextUtils.isEmpty(this.mTextView.get().getText())) {
                this.mTextView.setVisibility(0);
            }
        }
        if (this.mSubjectView != null && this.mSubjectView.get() != null) {
            if (isEditMode) {
                this.mSubjectView.setVisibility(8);
            } else if (!TextUtils.isEmpty(this.mSubjectView.get().getText())) {
                this.mSubjectView.setVisibility(0);
            }
        }
    }

    private long getSlideTime() {
        long currentTime = System.currentTimeMillis();
        if (this.mSlideSmoothShowFragment == null) {
            return currentTime;
        }
        MessageItem msgItem = this.mSlideSmoothShowFragment.getMessageItem();
        if (msgItem == null || 0 == msgItem.mDate) {
            return currentTime;
        }
        return msgItem.mDate;
    }

    public boolean onDoubleTapUp(boolean isLink) {
        if (this.mMediaItem.mMedia == null || this.mSlideSmoothShowFragment == null) {
            return false;
        }
        MessageUtils.viewText(getContext(), ((TextModel) this.mMediaItem.mMedia).getText());
        return true;
    }

    public void onTouchLink(ClickableSpan span) {
    }
}
