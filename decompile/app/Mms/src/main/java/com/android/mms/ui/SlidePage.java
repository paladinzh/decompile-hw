package com.android.mms.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditTextWithSmiley;
import java.util.Map;

public class SlidePage extends LinearLayout implements SlideViewInterface {
    private int mAttachmentType;
    private final OnCreateContextMenuListener mAudioMenuCreateListener;
    private TextView mAudioNameView;
    private int mAudioType;
    private View mAudioView;
    private Runnable mDeleteEmptyRunnable;
    private EditTextWithSmiley mEditTextBottom;
    private EditTextWithSmiley mEditTextCurrent;
    private EditTextWithSmiley mEditTextTop;
    private boolean mHasVcalendar;
    private boolean mHasVideo;
    private final OnCreateContextMenuListener mImageMenuCreateListener;
    private int mImageType;
    private GifView mImageView;
    private TextView mIndexText;
    private SlideChangeListener mListener;
    private InputFilter mMaxTextLimitInputFilter;
    private OnFocusChangeListener mOnFocusChangeListener;
    private boolean mOnTextChangedListenerEnabled;
    private OnTouchListener mOnTouchListener;
    private SmileyParser mParser;
    private RelativeLayout mPicVideoLayout;
    private int mPosition;
    private HwThumbnailPresenter mPresenter;
    private int mRestrictedTextLen;
    private LinearLayout mSlideAttachLayout;
    private View mSpliteView;
    private TextView mTextSub1;
    private TextView mTextSub2;
    private LinearLayout mTextSubLayout;
    private TextWatcher mTextWatcher;
    private Toast mToast;
    private ImageView mVideoSign;

    public SlidePage(Context context) {
        this(context, null);
    }

    public SlidePage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidePage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPosition = -1;
        this.mRestrictedTextLen = -1;
        this.mPresenter = null;
        this.mImageType = 0;
        this.mAudioType = 0;
        this.mOnTextChangedListenerEnabled = true;
        this.mParser = null;
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (SlidePage.this.mOnTextChangedListenerEnabled && SlidePage.this.mListener != null) {
                    SlidePage.this.mListener.onSlideTextChange(SlidePage.this, s.toString());
                }
            }

            public void afterTextChanged(Editable s) {
                try {
                    if (-1 != SlidePage.this.mRestrictedTextLen) {
                        s.delete(SlidePage.this.mRestrictedTextLen, s.length());
                    }
                } catch (IndexOutOfBoundsException ex) {
                    MLog.e("SlidePage", "delete caused IndexOutOfBoundsException: ", (Throwable) ex);
                } catch (Exception e) {
                    MLog.e("SlidePage", "delete Exception: ", (Throwable) e);
                }
            }
        };
        this.mOnTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                SlidePage.this.mListener.onInputManagerShow();
                return false;
            }
        };
        this.mDeleteEmptyRunnable = new Runnable() {
            public void run() {
                if (!SlidePage.this.removeAudio() && !SlidePage.this.removeImage()) {
                    SlidePage.this.mListener.onSlideRemoved(SlidePage.this);
                }
            }
        };
        this.mOnFocusChangeListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    SlidePage.this.mListener.onSlideAcitived(SlidePage.this);
                }
            }
        };
        this.mMaxTextLimitInputFilter = new LengthFilter(MmsConfig.getMaxTextLimit()) {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dest == null) {
                    return null;
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                int length = dest.length();
                if (source != null) {
                    length += source.length();
                }
                if (length >= MmsConfig.getMaxTextLimit()) {
                    if (SlidePage.this.mToast == null) {
                        SlidePage.this.mToast = Toast.makeText(SlidePage.this.getContext(), R.string.entered_too_many_characters, 0);
                    }
                    SlidePage.this.mToast.show();
                }
                return result;
            }
        };
        this.mImageMenuCreateListener = new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                menu.setHeaderTitle(R.string.message_options);
                OnMenuItemClickListener l = new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 0:
                            case 1:
                                SlidePage.this.displayImageAttach();
                                return true;
                            case 2:
                                SlidePage.this.deleteImageAttach();
                                return true;
                            default:
                                return true;
                        }
                    }
                };
                if (SlidePage.this.hasVideo()) {
                    menu.add(0, 1, 0, R.string.play).setOnMenuItemClickListener(l);
                } else if (!SlidePage.this.hasVcalendar()) {
                    menu.add(0, 0, 0, R.string.view).setOnMenuItemClickListener(l);
                }
                menu.add(0, 2, 0, R.string.delete_message).setOnMenuItemClickListener(l);
            }
        };
        this.mAudioMenuCreateListener = new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                menu.setHeaderTitle(R.string.message_options);
                OnMenuItemClickListener l = new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 3:
                                SlidePage.this.displayAudioAttach();
                                return true;
                            case 4:
                                SlidePage.this.deleteAudioAttach();
                                return true;
                            default:
                                return true;
                        }
                    }
                };
                menu.add(0, 3, 0, R.string.play).setOnMenuItemClickListener(l);
                menu.add(0, 4, 0, R.string.delete_message).setOnMenuItemClickListener(l);
            }
        };
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mImageView = (GifView) findViewById(R.id.image);
        this.mVideoSign = (ImageView) findViewById(R.id.video_sign);
        this.mAudioView = findViewById(R.id.audio);
        this.mAudioNameView = (TextView) findViewById(R.id.audio_name);
        this.mTextSub1 = (TextView) findViewById(R.id.attach_text1);
        this.mTextSub2 = (TextView) findViewById(R.id.attach_text2);
        this.mPicVideoLayout = (RelativeLayout) findViewById(R.id.slide_pic_video_layout);
        this.mSlideAttachLayout = (LinearLayout) findViewById(R.id.slide_attach_layout);
        this.mTextSubLayout = (LinearLayout) findViewById(R.id.attach_text_view);
        this.mEditTextTop = (EditTextWithSmiley) findViewById(R.id.text_message_top);
        this.mEditTextBottom = (EditTextWithSmiley) findViewById(R.id.text_message_bottom);
        this.mEditTextTop.setFilters(new InputFilter[]{this.mMaxTextLimitInputFilter});
        this.mEditTextBottom.setFilters(new InputFilter[]{this.mMaxTextLimitInputFilter});
        this.mParser = SmileyParser.getInstance();
        this.mIndexText = (TextView) findViewById(R.id.page_number_text);
        this.mSpliteView = findViewById(R.id.slides_splite_line);
        this.mEditTextBottom.setOnTouchListener(this.mOnTouchListener);
        this.mEditTextBottom.setEmptyDeleter(this.mDeleteEmptyRunnable);
        this.mEditTextBottom.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mEditTextTop.setOnTouchListener(this.mOnTouchListener);
        this.mEditTextTop.setEmptyDeleter(this.mDeleteEmptyRunnable);
        this.mEditTextTop.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mSlideAttachLayout.setOnCreateContextMenuListener(this.mImageMenuCreateListener);
        this.mSlideAttachLayout.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    SlidePage.this.mListener.onSlideAcitived(SlidePage.this);
                }
            }
        });
        this.mAudioView.setOnCreateContextMenuListener(this.mAudioMenuCreateListener);
        this.mSlideAttachLayout.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 67) {
                    return false;
                }
                SlidePage.this.deleteImageAttach();
                return true;
            }
        });
        this.mAudioView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 67) {
                    return false;
                }
                SlidePage.this.deleteAudioAttach();
                return true;
            }
        });
    }

    private void displayImageAttach() {
        this.mListener.onSlideView(this, this.mImageType);
    }

    private void deleteImageAttach() {
        removeImage();
    }

    private void displayAudioAttach() {
        this.mListener.onSlideView(this, this.mAudioType);
    }

    private void deleteAudioAttach() {
        removeAudio();
    }

    public boolean removeAudio() {
        if (this.mAudioView.getVisibility() != 0) {
            return false;
        }
        this.mAudioView.setVisibility(8);
        this.mListener.onAudioRemoved(this);
        return true;
    }

    public boolean removeImage() {
        if (this.mPicVideoLayout.getVisibility() != 0) {
            return false;
        }
        this.mSlideAttachLayout.setVisibility(8);
        this.mPicVideoLayout.setVisibility(8);
        this.mTextSubLayout.setVisibility(8);
        this.mVideoSign.setVisibility(8);
        this.mImageView.setImageBitmap(null);
        this.mListener.onAttachmentRemoved(this);
        return true;
    }

    public void startAudio() {
    }

    public void startVideo() {
    }

    public void setImage(String name, Bitmap bitmap) {
        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_bottom_emui);
            } catch (OutOfMemoryError e) {
                MLog.e("SlidePage", "setImage: out of memory: ", (Throwable) e);
                return;
            }
        }
        this.mImageView.setImageBitmap(bitmap);
        this.mSlideAttachLayout.setVisibility(0);
        this.mPicVideoLayout.setVisibility(0);
    }

    public void setVcard(String textSub1, String textSub2) {
        this.mTextSub1.setText(textSub1);
        this.mTextSub2.setText(textSub2);
        this.mImageView.setImageResource(R.drawable.mms_editor_vcard);
        this.mSlideAttachLayout.setVisibility(0);
        this.mPicVideoLayout.setVisibility(0);
        this.mTextSubLayout.setVisibility(0);
    }

    public void setVcalendar(String textSub1, String textSub2) {
        this.mTextSub1.setText(textSub1);
        this.mTextSub2.setText(textSub2);
        this.mImageView.setImageResource(R.drawable.mms_editor_vcalendar);
        this.mSlideAttachLayout.setVisibility(0);
        this.mPicVideoLayout.setVisibility(0);
        this.mTextSubLayout.setVisibility(0);
    }

    public void setAudio(Uri audio, String name, Map<String, ?> map) {
        this.mAudioView.setVisibility(0);
        this.mAudioNameView.setText(name);
        if (this.mAttachmentType == 0 || this.mAttachmentType == 1) {
            this.mAttachmentType = 3;
        }
    }

    public boolean setGifImage(String name, Uri uri) {
        if (uri == null) {
            setImage(name, null);
            return false;
        }
        this.mSlideAttachLayout.setVisibility(0);
        this.mPicVideoLayout.setVisibility(0);
        return this.mImageView.setGifImage(uri);
    }

    public void setImageRegionFit(String fit) {
    }

    public void setImageVisibility(boolean visible) {
    }

    public void setText(String name, String text) {
        this.mOnTextChangedListenerEnabled = false;
        if (!(text == null || text.equals(this.mEditTextCurrent.getText().toString()))) {
            this.mEditTextCurrent.setText(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mEditTextCurrent.setSelection(this.mEditTextCurrent.length());
        }
        this.mOnTextChangedListenerEnabled = true;
    }

    public void setTextVisibility(boolean visible) {
    }

    public void setVideo(String name, Uri video) {
        try {
            Bitmap bitmap = MessageUtils.createVideoThumbnail(this.mContext, video);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_default_avatar);
            }
            this.mImageView.setImageBitmap(bitmap);
            this.mSlideAttachLayout.setVisibility(0);
            this.mPicVideoLayout.setVisibility(0);
        } catch (OutOfMemoryError e) {
            MLog.e("SlidePage", "setVideo: out of memory: ", (Throwable) e);
        }
    }

    public void setVideoThumbnail(String name, Bitmap bitmap) {
        if (bitmap == null) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csp_default_avatar);
            } catch (OutOfMemoryError e) {
                MLog.e("SlidePage", "setVideoThumbnail: out of memory: ", (Throwable) e);
                return;
            }
        }
        this.mImageView.setImageBitmap(bitmap);
        this.mSlideAttachLayout.setVisibility(0);
        this.mPicVideoLayout.setVisibility(0);
        this.mVideoSign.setVisibility(0);
    }

    public void setVideoVisibility(boolean visible) {
    }

    public void stopAudio() {
    }

    public void stopVideo() {
    }

    public void reset() {
        this.mImageView.setImageDrawable(null);
        this.mImageView.resetView();
        this.mAudioView.setVisibility(8);
        this.mOnTextChangedListenerEnabled = false;
        this.mEditTextCurrent.setText("");
        this.mOnTextChangedListenerEnabled = true;
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

    public boolean hasVcalendar() {
        return this.mHasVcalendar;
    }

    public boolean hasVideo() {
        return this.mHasVideo;
    }
}
