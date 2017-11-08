package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Calendars;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.VCalSmsMessage;
import com.android.mms.VCardSmsMessage;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager.AttachmentDataManagerListener;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager.AttachmentState;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager.AttachmentThreadCallBack;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.IDraftLoaded;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VcardModel;
import com.android.mms.ui.AttachmentTypeSelectorAdapter.AttachmentListItem;
import com.android.mms.util.SignatureUtil;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsRichMessageEditor;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.HandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.utils.RcseMmsExt;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class RichMessageEditor extends LinearLayout implements OnEditorActionListener, OnItemClickListener, OnTouchListener, OnFocusChangeListener {
    private static final int[] CheckedStateSet = new int[]{16842908, 16842919};
    private static final int[] OPS_ALL = new int[]{R.string.preview, R.string.add_slide, R.string.remove_slide, R.string.layout_top, R.string.duration_sec};
    private boolean isNewSlidePage;
    private boolean isSlideDirty;
    private AsyncDialog mAsyncDialog;
    private AttachmentDataManager mAttachmentDataManager;
    private Context mContext;
    private int mCurType;
    private boolean mDelLongClicked;
    private ComposeMessageFragment mFragment;
    private HandlerEx mHandler;
    private HwCustRichMessageEditor mHwCustRichMessageEditor;
    private HwSlideChangeListener mHwSlideListener;
    private HwSlidesListView mHwSlidesView;
    private boolean mIsForwardMms;
    private boolean mIsLoadingDraft;
    private boolean mIsOriginalSize;
    private EditableSlides$RichMessageListener mListener;
    private boolean mLoadDraftState;
    private Runnable mLongClickRunnable;
    private MessageStatusListener mMessageStatuslistener;
    private int mMinPxSize;
    private Handler mMultiAddHandler;
    private OnTouchListener mOnTouchListener;
    private SmileyParser mParser;
    private int mPosition;
    private Queue<Integer> mPostionQueue;
    private RcsRichMessageEditor mRcsRichMessageEditor;
    private int mRestrictedTextLen;
    private ArrayList<RichAttachmentListener> mRichAttachmentListeners;
    private boolean mShowHwSlidePage;
    private EditTextWithSmiley mSmsEditorText;
    private Runnable mStartLongClickRunnable;
    private final TextWatcher mSubjectEditorWatcher;
    private final OnKeyListener mSubjectKeyListener;
    private View mSubjectSeperator;
    private EditTextWithSmiley mSubjectTextEditor;
    private boolean mTakePictureState;
    private final TextWatcher mTextEditorWatcher;
    private Toast mToast;
    private UpdateSubjectReViewListener mUpdateSubjectReviewListener;
    private Uri mUri;
    private WorkingMessage mWorkingMessage;

    public interface UpdateSubjectReViewListener {
        void updateSubjectReView(boolean z);
    }

    public interface RichAttachmentListener {
        void onRichAttachmentChanged(int i);
    }

    private final class DelayedSetEditTextFocusRunnable implements Runnable {
        private DelayedSetEditTextFocusRunnable() {
        }

        public void run() {
            RichMessageEditor.this.setEditTextFocus();
        }
    }

    private final class DelayedSetSmsEditTextFocusRunnable implements Runnable {
        private DelayedSetSmsEditTextFocusRunnable() {
        }

        public void run() {
            RichMessageEditor.this.mSmsEditorText.requestFocus();
        }
    }

    private class HwSlidesListView {
        private LinearLayout mSlidesAnchor = null;

        HwSlidesListView(LinearLayout v) {
            this.mSlidesAnchor = v;
        }

        void setVisible(boolean visibale) {
            this.mSlidesAnchor.setVisibility(visibale ? 0 : 8);
        }

        int addNewSlideView(int position) {
            if (RichMessageEditor.this.mFragment.getActivity().getWindow() == null) {
                return -1;
            }
            HwSlidePage slide = (HwSlidePage) RichMessageEditor.this.mFragment.getActivity().getWindow().getLayoutInflater().inflate(R.layout.hw_slide_page_layout, this.mSlidesAnchor, false);
            if (position >= size()) {
                position = size();
            }
            this.mSlidesAnchor.addView(slide, position);
            slide.setSlideChangeListener(RichMessageEditor.this.mHwSlideListener);
            RichMessageEditor.this.mWorkingMessage.setmHasMmsDraft(true);
            RichMessageEditor.this.mListener.onContentChange();
            return position;
        }

        void removeSlideView(int position) {
            if (position < this.mSlidesAnchor.getChildCount()) {
                this.mSlidesAnchor.removeViewAt(position);
                RichMessageEditor.this.mWorkingMessage.setmHasMmsDraft(true);
                RichMessageEditor.this.mListener.onContentChange();
                if (this.mSlidesAnchor.getChildCount() < 2) {
                    RichMessageEditor.this.mShowHwSlidePage = false;
                }
            }
        }

        void removeAllSlideViews() {
            for (int i = 0; i < this.mSlidesAnchor.getChildCount(); i++) {
                HwSlidePage sp = getPage(i);
            }
            this.mSlidesAnchor.removeAllViews();
            RichMessageEditor.this.mPosition = -1;
            RichMessageEditor.this.mWorkingMessage.setmHasMmsDraft(true);
            RichMessageEditor.this.mListener.onContentChange();
            RichMessageEditor.this.mShowHwSlidePage = false;
        }

        HwSlidePage getPage(int pos) {
            if (this.mSlidesAnchor.getChildCount() <= 0) {
                return null;
            }
            if (pos >= this.mSlidesAnchor.getChildCount()) {
                pos = this.mSlidesAnchor.getChildCount() - 1;
            }
            return (HwSlidePage) this.mSlidesAnchor.getChildAt(pos);
        }

        HwSlidePage getCurrentPage() {
            return getPage(RichMessageEditor.this.mPosition);
        }

        int size() {
            return this.mSlidesAnchor.getChildCount();
        }

        void updateSplites() {
            int all = this.mSlidesAnchor.getChildCount();
            for (int i = 0; i < this.mSlidesAnchor.getChildCount(); i++) {
                ((HwSlidePage) this.mSlidesAnchor.getChildAt(i)).showSlideIndex(i, all);
            }
        }
    }

    private class RichAttachmentThreadCallBack implements AttachmentThreadCallBack {
        private RichAttachmentThreadCallBack() {
        }

        public void onModelFinish(int resultCode, int position, MediaModel mediaModel, int type) {
            if (!RichMessageEditor.this.mAttachmentDataManager.setAttachmentResult(position, type, mediaModel, resultCode)) {
                MLog.d("RichMessageEditor", "onModelFinish setAttachmentResult failed");
            }
        }
    }

    public HandlerEx getHandler() {
        return this.mHandler;
    }

    public RcsRichMessageEditor getRcsRichMessageEditor() {
        return this.mRcsRichMessageEditor;
    }

    public RichMessageEditor(Context context) {
        this(context, null);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsRichMessageEditor == null) {
            this.mRcsRichMessageEditor = new RcsRichMessageEditor();
        }
        if (this.mRcsRichMessageEditor != null) {
            this.mRcsRichMessageEditor.setHwCustRichMessageEditor(context, this);
        }
    }

    public RichMessageEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsRichMessageEditor == null) {
            this.mRcsRichMessageEditor = new RcsRichMessageEditor();
        }
        if (this.mRcsRichMessageEditor != null) {
            this.mRcsRichMessageEditor.setHwCustRichMessageEditor(context, this);
        }
    }

    public RichMessageEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTakePictureState = false;
        this.mHwSlidesView = null;
        this.mSubjectSeperator = null;
        this.mPosition = -1;
        this.isSlideDirty = false;
        this.mParser = null;
        this.mPostionQueue = new LinkedList();
        this.mDelLongClicked = false;
        this.mShowHwSlidePage = false;
        this.mIsOriginalSize = false;
        this.mMinPxSize = 0;
        this.mHandler = new HandlerEx() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        removeMessages(100);
                        onTextChange((CharSequence) msg.obj, msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }

            private void onTextChange(CharSequence s, int start, int before) {
                if (RichMessageEditor.this.mRcsRichMessageEditor != null && RichMessageEditor.this.mRcsRichMessageEditor.isImUIStyle()) {
                    RichMessageEditor.this.mListener.onContentChange();
                }
                if (!MmsConfig.getMultipartSmsEnabled()) {
                    RichMessageEditor.this.mWorkingMessage.setLengthRequiresMms(MessageUtils.isMmsText(RichMessageEditor.this.mWorkingMessage.get7BitText()), true);
                }
                if (RichMessageEditor.this.mWorkingMessage.requiresMms() && !RichMessageEditor.this.mLoadDraftState) {
                    RichMessageEditor.this.mWorkingMessage.ensureSlideshow();
                    SlideshowModel slideShow = RichMessageEditor.this.mWorkingMessage.getSlideshow();
                    if (slideShow != null) {
                        if (slideShow.get(0) != null) {
                            RichMessageEditor.this.mWorkingMessage.syncTextToSlideshow(s.toString());
                        }
                        RichMessageEditor.this.mRestrictedTextLen = -1;
                    } else {
                        return;
                    }
                }
                RichMessageEditor.this.mListener.onContentChange();
            }
        };
        this.mStartLongClickRunnable = new Runnable() {
            public void run() {
                RichMessageEditor.this.mDelLongClicked = true;
                RichMessageEditor.this.mHandler.post(RichMessageEditor.this.mLongClickRunnable);
            }
        };
        this.mLongClickRunnable = new Runnable() {
            public void run() {
                RichMessageEditor richMessageEditor = RichMessageEditor.this;
                EditTextWithSmiley -get19 = (RichMessageEditor.this.mSubjectTextEditor == null || !RichMessageEditor.this.mSubjectTextEditor.hasFocus()) ? RichMessageEditor.this.mSmsEditorText : RichMessageEditor.this.mSubjectTextEditor;
                richMessageEditor.deleteSmiley(-get19);
                if (RichMessageEditor.this.mHwSlidesView != null) {
                    HwSlidePage sp = RichMessageEditor.this.mHwSlidesView.getCurrentPage();
                    if (sp != null) {
                        RichMessageEditor.this.deleteSmiley((EditTextWithSmiley) sp.getMsgEditor());
                    }
                }
                if (RichMessageEditor.this.mDelLongClicked) {
                    RichMessageEditor.this.mHandler.postDelayed(RichMessageEditor.this.mLongClickRunnable, 100);
                }
            }
        };
        this.mIsForwardMms = false;
        this.mIsLoadingDraft = false;
        this.mRestrictedTextLen = -1;
        this.mHwCustRichMessageEditor = (HwCustRichMessageEditor) HwCustUtils.createObj(HwCustRichMessageEditor.class, new Object[0]);
        this.mOnTouchListener = new OnTouchListener() {
            private boolean mIsLongClick = false;
            private boolean mIsMoved = false;
            private Runnable mLongClickRunnable = new Runnable() {
                public void run() {
                    AnonymousClass4.this.mIsLongClick = true;
                }
            };
            private float mStartX;
            private float mStartY;

            public boolean onTouch(View v, MotionEvent event) {
                boolean z = true;
                boolean isConsumed = false;
                switch (event.getAction()) {
                    case 0:
                        if (R.id.embedded_text_editor == v.getId() && RichMessageEditor.this.mSmsEditorText != null) {
                            RichMessageEditor.this.mSmsEditorText.setCursorVisible(true);
                        }
                        this.mStartX = event.getRawX();
                        this.mStartY = event.getRawY();
                        HwBackgroundLoader.getUIHandler().postDelayed(this.mLongClickRunnable, 800);
                        break;
                    case 1:
                        if (!this.mIsMoved || this.mIsLongClick) {
                            RichMessageEditor.this.mListener.onInputManagerShow();
                        }
                        if (this.mIsMoved) {
                            isConsumed = true;
                        }
                        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                        this.mIsMoved = false;
                        this.mIsLongClick = false;
                        break;
                    case 2:
                        float moveX = event.getRawX();
                        float moveY = event.getRawY();
                        if (!this.mIsMoved) {
                            if (Math.abs(moveX - this.mStartX) < ((float) RichMessageEditor.this.mMinPxSize) && Math.abs(moveY - this.mStartY) < ((float) RichMessageEditor.this.mMinPxSize)) {
                                z = false;
                            }
                            this.mIsMoved = z;
                        }
                        if (this.mIsMoved) {
                            HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                            break;
                        }
                        break;
                    case 3:
                        if (this.mIsLongClick) {
                            RichMessageEditor.this.mListener.onInputManagerShow();
                        }
                        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mLongClickRunnable);
                        this.mIsMoved = false;
                        this.mIsLongClick = false;
                        break;
                }
                return isConsumed;
            }
        };
        this.mHwSlideListener = new HwSlideChangeListener() {
            public void onSlideTextChange(HwSlidePage page, String s) {
                RichMessageEditor.this.isSlideDirty = true;
                SlideshowEditor editor = RichMessageEditor.this.mWorkingMessage.getSlideshowEditor();
                if (editor != null) {
                    page.setRestrictedTextLen(-1);
                    if (!(s == null || s.length() == 0)) {
                        int exceededLength = RichMessageEditor.this.getExceedMessageSize(page.getPosition(), s);
                        if (!RcseMmsExt.isRcsMode() && exceededLength > 0) {
                            ResEx.makeToast((int) R.string.change_text_fail, 1);
                            s = RichMessageEditor.this.getSuitableSubString(page.getPosition(), s, exceededLength);
                            page.setRestrictedTextLen(s.length());
                        }
                    }
                    editor.changeText(page.getPosition(), s);
                    RichMessageEditor.this.mListener.onContentChange();
                }
            }

            public void onSlideRemoved(HwSlidePage page) {
                RichMessageEditor.this.isSlideDirty = true;
                RichMessageEditor.this.delSlidePage(page);
            }

            public void onSlideAcitived(HwSlidePage page) {
                RichMessageEditor.this.setPosition(page.getPosition());
            }

            public void onInputManagerShow() {
                RichMessageEditor.this.mListener.onInputManagerShow();
            }
        };
        this.mTextEditorWatcher = new TextWatcher() {
            String beforeString;
            boolean mFirstLoad = true;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                this.beforeString = s.toString();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (RichMessageEditor.this.mRcsRichMessageEditor != null) {
                    RichMessageEditor.this.mRcsRichMessageEditor.onTextChanged(s, start, before, count);
                }
                RichMessageEditor.this.mWorkingMessage.setText(s);
                if (this.mFirstLoad && (RichMessageEditor.this.mIsForwardMms || RichMessageEditor.this.mIsLoadingDraft)) {
                    MLog.i("RichMessageEditor", "onTextChanged first load, mIsForwardMms: " + RichMessageEditor.this.mIsForwardMms + ", mIsLoadingDraft: " + RichMessageEditor.this.mIsLoadingDraft);
                    this.mFirstLoad = false;
                } else if (!(s == null || s.length() == 0)) {
                    int exceededLength = RichMessageEditor.this.getExceedMessageSize(this.beforeString, s.toString());
                    if (!RcseMmsExt.isRcsMode() && exceededLength > 0) {
                        ResEx.makeToast((int) R.string.change_text_fail, 0);
                        s = RichMessageEditor.this.getSuitableSubString(this.beforeString, s.toString(), exceededLength);
                        RichMessageEditor.this.setText(s);
                        RichMessageEditor.this.mRestrictedTextLen = s.length();
                    }
                }
                RichMessageEditor.this.mHandler.sendMessage(RichMessageEditor.this.mHandler.obtainMessage(100, start, before, s));
            }

            public void afterTextChanged(Editable s) {
                try {
                    if (-1 != RichMessageEditor.this.mRestrictedTextLen) {
                        s.delete(RichMessageEditor.this.mRestrictedTextLen, s.length());
                    }
                } catch (IndexOutOfBoundsException ex) {
                    MLog.e("RichMessageEditor", "delete caused IndexOutOfBoundsException: ", (Throwable) ex);
                } catch (Exception e) {
                    MLog.e("RichMessageEditor", "delete Exception: ", (Throwable) e);
                }
            }
        };
        this.mSubjectEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(RichMessageEditor.this.mWorkingMessage.getSubject()) && s.length() > 0) {
                    RichMessageEditor.this.mWorkingMessage.setSubject(new StringBuilder(s).toString(), true);
                    RichMessageEditor.this.mWorkingMessage.ensureSlideshow();
                    SlideshowModel slideShow = RichMessageEditor.this.mWorkingMessage.getSlideshow();
                    if (slideShow != null) {
                        SlideModel sm = slideShow.get(0);
                        if (!(sm == null || sm.hasAudio() || sm.hasImage() || sm.hasVCalendar() || sm.hasVcard() || sm.hasVideo())) {
                            RichMessageEditor.this.mWorkingMessage.syncTextToSlideshow(RichMessageEditor.this.mWorkingMessage.getText().toString());
                        }
                    } else {
                        return;
                    }
                } else if (s.length() == 0) {
                    RichMessageEditor.this.mWorkingMessage.setSubject(null, true);
                } else {
                    RichMessageEditor.this.mWorkingMessage.setSubject(s, false);
                }
                RichMessageEditor.this.mListener.onContentChange();
            }

            public void afterTextChanged(Editable s) {
            }
        };
        this.mSubjectKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 0 || keyCode != 67 || RichMessageEditor.this.mSubjectTextEditor.length() != 0) {
                    return false;
                }
                RichMessageEditor.this.showSubjectEditor(false);
                if (!TextUtils.isEmpty(RichMessageEditor.this.mWorkingMessage.getSubject())) {
                    RichMessageEditor.this.mWorkingMessage.setSubject(null, true);
                }
                RichMessageEditor.this.mHandler.postDelayed(new DelayedSetEditTextFocusRunnable(), 100);
                if (RichMessageEditor.this.mUpdateSubjectReviewListener != null) {
                    RichMessageEditor.this.mUpdateSubjectReviewListener.updateSubjectReView(false);
                }
                return true;
            }
        };
        this.mRichAttachmentListeners = new ArrayList();
        this.mLoadDraftState = false;
        this.mAttachmentDataManager = new AttachmentDataManager(new AttachmentDataManagerListener() {
            public void onResultAttachment(boolean result, int position, int type) {
                RichMessageEditor.this.dispatchAttachmentResult(result, position, type);
            }
        });
        this.mMinPxSize = (int) context.getResources().getDimension(R.dimen.editor_touch_moved_min_size);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsRichMessageEditor == null) {
            this.mRcsRichMessageEditor = new RcsRichMessageEditor();
        }
        if (this.mRcsRichMessageEditor != null) {
            this.mRcsRichMessageEditor.setHwCustRichMessageEditor(context, this);
        }
    }

    public void setFragment(ComposeMessageFragment fragment) {
        this.mFragment = fragment;
        this.mContext = this.mFragment.getContext();
    }

    public void setMessageStatusListener(MessageStatusListener listener) {
        this.mMessageStatuslistener = listener;
    }

    public void setAddMultiHandler(Handler handler) {
        this.mMultiAddHandler = handler;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mParser = SmileyParser.getInstance();
        this.mHwSlidesView = new HwSlidesListView((LinearLayout) findViewById(R.id.slides_list_anchor));
        this.mSmsEditorText = (EditTextWithSmiley) findViewById(R.id.embedded_text_editor);
        this.mSmsEditorText.setOnEditorActionListener(this);
        this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
        this.mSmsEditorText.setOnFocusChangeListener(this);
        InputFilter inputFilter = new LengthFilter(MmsConfig.getMaxTextLimit()) {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (dest == null) {
                    return null;
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                if (MmsConfig.getMaxTextLimit() - dest.length() <= (end - start) - (dend - dstart)) {
                    if (RichMessageEditor.this.mToast == null) {
                        RichMessageEditor.this.mToast = Toast.makeText(RichMessageEditor.this.mContext, R.string.entered_too_many_characters, 0);
                    }
                    RichMessageEditor.this.mToast.show();
                }
                return result;
            }
        };
        if (this.mHwCustRichMessageEditor != null) {
            inputFilter = this.mHwCustRichMessageEditor.createInputFilter(this.mSmsEditorText, inputFilter);
        }
        this.mSmsEditorText.setFilters(new InputFilter[]{inputFilter});
        this.mSmsEditorText.setOnTouchListener(this.mOnTouchListener);
        this.mSmsEditorText.clearFocus();
        if (MmsConfig.isExtraHugeEnabled(getResources().getConfiguration().fontScale)) {
            this.mSmsEditorText.setTextSize(1, 26.1f);
        }
        this.mSubjectSeperator = findViewById(R.id.subject_splite_line);
        this.mSubjectTextEditor = (EditTextWithSmiley) findViewById(R.id.subject);
        this.mSubjectTextEditor.addTextChangedListener(this.mSubjectEditorWatcher);
        this.mSubjectTextEditor.setOnTouchListener(this.mOnTouchListener);
        this.mSubjectTextEditor.setOnFocusChangeListener(this);
        if (MmsConfig.isFiltSubject()) {
            this.mSubjectTextEditor.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(MmsConfig.getMaxSubjectLength(), getContext())});
            return;
        }
        this.mSubjectTextEditor.setFilters(new InputFilter[]{new LengthFilter(MmsConfig.getMaxSubjectLength())});
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (HwMessageUtils.isSplitOn() && this.mFragment != null && this.mFragment.getActivity() != null && this.mFragment.getActivity().getWindow() != null) {
            this.mFragment.getActivity().getWindow().setSoftInputMode(16);
        }
    }

    public void createWorkingMessage(Context context, MessageStatusListener listener) {
        this.mWorkingMessage = WorkingMessage.createEmpty(context, listener);
    }

    public void loadWorkingMessage(Context context, MessageStatusListener listener, Uri uri) {
        WorkingMessage msg = WorkingMessage.load(context, listener, uri);
        if (msg != null) {
            this.mWorkingMessage.discard(ContentUris.parseId(uri));
            this.mWorkingMessage = msg;
            resetCurrentPosition();
        }
    }

    public boolean syncWorkingMessageToUI() {
        this.mHwSlidesView.removeAllSlideViews();
        setPosition(-1);
        CharSequence subject = this.mWorkingMessage.getSubject();
        if (!TextUtils.isEmpty(subject)) {
            setSubject(subject, true);
            showSubjectEditor(true);
        }
        if (this.mWorkingMessage.hasAttachment()) {
            this.mSmsEditorText.setVisibility(8);
            SlideshowModel<SlideModel> slideShow = this.mWorkingMessage.getSlideshow();
            if (slideShow == null || slideShow.size() == 0) {
                setText(this.mWorkingMessage.getText());
                return false;
            }
            syncLocationSlideText(slideShow);
            if (showHwSlidePage(true)) {
                this.mShowHwSlidePage = true;
                for (SlideModel slide : slideShow) {
                    syncSlideModeToSlideView(slide, false);
                }
                this.mHwSlidesView.updateSplites();
                this.mListener.onContentChange();
                HwSlidePage sp = this.mHwSlidesView.getPage(0);
                if (sp != null) {
                    sp.setTextFocus();
                }
                return false;
            }
            SlideModel sm = slideShow.get(0);
            if (!(sm == null || !sm.hasText() || TextUtils.isEmpty(sm.getText().getText()))) {
                this.mSmsEditorText.setText(sm.getText().getText());
            }
            this.mSmsEditorText.setVisibility(0);
            return true;
        }
        setText(this.mWorkingMessage.getText());
        this.mSmsEditorText.setVisibility(0);
        return false;
    }

    private void syncLocationSlideText(SlideshowModel slideShow) {
        if (slideShow == null) {
            MLog.d("RichMessageEditor", "sycLocationSlideText slideshowModel == null");
            return;
        }
        for (int i = 0; i < slideShow.size(); i++) {
            SlideModel slide = slideShow.get(i);
            if (!(!slide.hasLocation() || slide.getImage().getLocationSource() == null || slide.getText() == null)) {
                String locationInfo = (String) slide.getImage().getLocationSource().get("locationinfo");
                String slideText = slide.getText().getText();
                if (slideText.equals(locationInfo)) {
                    slide.getText().setText("");
                } else if (slideText.contains(locationInfo) && slideText.length() > locationInfo.length() + 1) {
                    slide.getText().setText(slideText.substring(locationInfo.length() + 1));
                }
            }
        }
    }

    private SlideViewInterface syncSlideModeToSlideView(SlideModel slide, boolean syncText) {
        this.mPosition = this.mHwSlidesView.addNewSlideView(this.mPosition + 1);
        if (this.mHwSlidesView.size() == 1) {
            switchToRichMode(true, syncText);
        }
        HwSlidePage sp = this.mHwSlidesView.getPage(this.mPosition);
        for (MediaModel media : slide) {
            sp.addAttachment(media.getType());
        }
        sp.setPosition(this.mPosition);
        sp.setText("", slide.getText().getText());
        return null;
    }

    private int createSlide(int position, int type, boolean mainThread) {
        SlideshowEditor editor = this.mWorkingMessage.getSlideshowEditor();
        if (editor == null || !editor.addNewSlide(position)) {
            return -1;
        }
        return position;
    }

    private void showConfirmDialog(final int type, final Uri uri) {
        new Builder(getContext()).setTitle(R.string.add_attchment_failed_replace_title).setCancelable(true).setMessage(R.string.add_attchment_failed_replace_message).setPositiveButton(R.string.yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RichMessageEditor.this.replaceAttachment(type, uri);
            }
        }).setNegativeButton(R.string.no, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RichMessageEditor.this.refreshMediaAttachment(type);
            }
        }).show();
    }

    public void replaceAttachment(int type, Uri uri) {
        this.isSlideDirty = true;
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        if (slideshow != null) {
            SlideModel slideModel = slideshow.get(0);
            if (slideModel != null) {
                String text = "";
                if (slideModel.hasText()) {
                    text = slideModel.getText().getText();
                }
                MmsApp.getApplication().getThumbnailManager().clear();
                this.mHwSlidesView.removeAllSlideViews();
                if (this.mSmsEditorText != null && this.mSmsEditorText.getVisibility() == 8) {
                    this.mSmsEditorText.removeTextChangedListener(this.mTextEditorWatcher);
                    this.mSmsEditorText.setVisibility(0);
                    this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
                    this.mSmsEditorText.setSelection(this.mSmsEditorText.getText().length() > 1 ? this.mSmsEditorText.getText().length() - 1 : 0);
                    this.mSmsEditorText.requestFocus();
                    this.mSmsEditorText.setCursorVisible(true);
                }
                SlideshowEditor editor = this.mWorkingMessage.getSlideshowEditor();
                if (editor != null) {
                    editor.removeAllSlides();
                }
                setPosition(-1);
                String signature = SignatureUtil.getSignature(getContext(), MmsConfig.getDefaultSignatureText());
                if (!TextUtils.isEmpty(text) && text.endsWith(signature)) {
                    appendSignature(true);
                }
                addNewAttachment(this.mFragment.getActivity(), type, uri);
            }
        }
    }

    private boolean checkAppendAttachAvailable(int type, Uri uri) {
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        if (slideshow == null) {
            return false;
        }
        SlideModel slideModel = slideshow.get(0);
        if (slideModel == null) {
            return false;
        }
        switch (type) {
            case 2:
            case 3:
            case 5:
            case 8:
                if (slideModel.size() > 0 && (slideModel.hasVcard() || slideModel.hasVCalendar())) {
                    showConfirmDialog(type, uri);
                    return false;
                }
            case 6:
            case 7:
                if (slideshow.size() > 1 || (slideModel.size() > 0 && (slideModel.hasVcard() || slideModel.hasVCalendar() || slideModel.hasImage() || slideModel.hasAudio() || slideModel.hasVideo()))) {
                    showConfirmDialog(type, uri);
                    return false;
                }
        }
        return true;
    }

    private void updateViews(int position) {
        this.mListener.richToCheckRestrictedMime(true);
        this.mWorkingMessage.correctAttachmentState(true, true);
        SlideshowModel slideShow = this.mWorkingMessage.getSlideshow();
        if (showHwSlidePage(false)) {
            switchToRichMode(true, true);
            HwSlidePage sp = this.mHwSlidesView.getPage(position);
            this.mHwSlidesView.updateSplites();
            if (sp != null) {
                sp.setTextFocus();
            }
        } else if (slideShow != null && slideShow.size() == 1) {
            String text = this.mSmsEditorText.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                this.mWorkingMessage.syncTextToSlideshow(text);
            }
        }
        this.mListener.onContentChange();
        if (this.mMultiAddHandler != null) {
            this.mMultiAddHandler.sendEmptyMessage(1);
        }
    }

    private void updateViews() {
        updateViews(this.mPosition);
    }

    private void switchToRichMode(boolean richmode, boolean syncText) {
        if (richmode) {
            this.mSmsEditorText.removeTextChangedListener(this.mTextEditorWatcher);
            String text = this.mSmsEditorText.getText().toString();
            this.mSmsEditorText.setText("");
            this.mSmsEditorText.setVisibility(8);
            this.mWorkingMessage.setText("");
            this.mHwSlidesView.setVisible(richmode);
            HwSlidePage sp = this.mHwSlidesView.getPage(0);
            if (syncText && sp != null && sp.getText().length() == 0) {
                sp.setText("", text);
                this.mWorkingMessage.syncTextToSlideshow(text);
                return;
            }
            return;
        }
        this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        SlideModel slideModel = slideshow != null ? slideshow.get(0) : null;
        CharSequence smsEditText = null;
        if (syncText && slideModel != null && slideModel.hasText()) {
            smsEditText = slideModel.getText().getText();
        }
        SlideshowEditor slideEditor = this.mWorkingMessage.getSlideshowEditor();
        if (slideEditor != null) {
            slideEditor.removeAllSlides();
        }
        if (this.mHwSlidesView != null) {
            this.mHwSlidesView.removeAllSlideViews();
        }
        this.mPosition = -1;
        this.mSmsEditorText.setVisibility(0);
        this.mSmsEditorText.requestFocus();
        this.mWorkingMessage.removeAttachment(true);
        if (!TextUtils.isEmpty(smsEditText)) {
            this.mSmsEditorText.setText(this.mParser.addSmileySpans(smsEditText, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mSmsEditorText.setSelection(this.mSmsEditorText.length());
        }
        this.mListener.onContentChange();
    }

    private int getExceedMessageSize(int pos, String newText) {
        int oldTextSize = 0;
        int currentMessageSize = 0;
        SlideshowModel slideshowModel = null;
        SlideModel slideModel = null;
        TextModel textModel = null;
        if (this.mWorkingMessage != null) {
            slideshowModel = this.mWorkingMessage.getSlideshow();
        }
        if (slideshowModel != null) {
            slideModel = slideshowModel.get(pos);
        }
        if (slideModel != null) {
            textModel = slideModel.getText();
        }
        if (textModel != null) {
            oldTextSize = MessageUtils.encodeText(textModel.getText()).length;
        }
        int newTextSize = MessageUtils.encodeText(newText).length;
        if (slideshowModel != null) {
            currentMessageSize = slideshowModel.getCurrentMessageSize();
        }
        return ((currentMessageSize - oldTextSize) + newTextSize) - (MmsConfig.getMaxMessageSize() - 4096);
    }

    private int getExceedMessageSize(String oldText, String newText) {
        int currentMessageSize = 0;
        SlideshowModel slideshowModel = null;
        if (this.mWorkingMessage != null) {
            slideshowModel = this.mWorkingMessage.getSlideshow();
        }
        int oldTextSize = MessageUtils.encodeText(oldText, 106).length;
        int newTextSize = MessageUtils.encodeText(newText, 106).length;
        if (slideshowModel != null) {
            currentMessageSize = slideshowModel.getCurrentMessageSize();
        }
        return ((currentMessageSize - oldTextSize) + newTextSize) - (MmsConfig.getMaxMessageSize() - 4096);
    }

    public String getSuitableSubString(int pos, String newText, int exceededLength) {
        byte[] byteTypeOfString = MessageUtils.encodeText(newText);
        int remainLength = byteTypeOfString.length > exceededLength ? byteTypeOfString.length - exceededLength : byteTypeOfString.length;
        byte[] ret = new byte[remainLength];
        System.arraycopy(byteTypeOfString, 0, ret, 0, remainLength);
        String retStr = MessageUtils.decodeByteArray(ret);
        while (retStr.length() != 0 && getExceedMessageSize(pos, retStr) > 0) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
        return retStr;
    }

    public String getSuitableSubString(String oldText, String newText, int exceededLength) {
        byte[] byteTypeOfString = MessageUtils.encodeText(newText, 106);
        int remainLength = byteTypeOfString.length > exceededLength ? byteTypeOfString.length - exceededLength : byteTypeOfString.length;
        byte[] ret = new byte[remainLength];
        System.arraycopy(byteTypeOfString, 0, ret, 0, remainLength);
        String retStr = MessageUtils.decodeByteArray(ret, 106);
        while (retStr.length() != 0 && getExceedMessageSize(oldText, retStr) > 0) {
            retStr = retStr.substring(0, retStr.length() - 1);
        }
        return retStr;
    }

    public boolean isRMESlideDirty() {
        return this.isSlideDirty;
    }

    private void delSlidePage(HwSlidePage page) {
        if (page != null) {
            int pos = page.getPosition();
            SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
            if (slideshow != null) {
                if (slideshow.size() == 1) {
                    switchToRichMode(false, false);
                    refreshMediaAttachment(13);
                } else if (slideshow.get(pos) != null) {
                    SlideshowEditor editor = this.mWorkingMessage.getSlideshowEditor();
                    if (editor != null) {
                        editor.removeSlide(pos);
                        refreshMediaAttachment(13);
                    }
                    this.mHwSlidesView.removeSlideView(pos);
                    this.mHwSlidesView.updateSplites();
                    setPosition(pos == 0 ? 0 : pos - 1);
                    HwSlidePage pg = this.mHwSlidesView.getPage(this.mPosition);
                    if (pg != null) {
                        pg.setTextFocus();
                    }
                    slideshow = this.mWorkingMessage.getSlideshow();
                    if (slideshow != null) {
                        SlideModel sModel = slideshow.get(this.mPosition);
                        if (sModel != null) {
                            if (sModel.size() <= 1 && slideshow.size() == 1) {
                                switchToRichMode(false, true);
                                refreshMediaAttachment(13);
                            } else if (slideshow.size() == 1 && !sModel.hasRoomForAttachment()) {
                                this.mHwSlidesView.removeAllSlideViews();
                                if (sModel.hasText() && !TextUtils.isEmpty(sModel.getText().getText())) {
                                    this.mSmsEditorText.setText(sModel.getText().getText());
                                }
                                this.mSmsEditorText.setVisibility(0);
                                this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
                            }
                        }
                    }
                }
            }
        }
    }

    public void viewAttach(MediaModel mm) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(1);
        String fileName = mm.getSrc();
        intent.putExtra("android.intent.extra.TITLE", fileName);
        String contentType = mm.getContentType();
        Uri dataUri = mm.getUri();
        intent.setDataAndType(dataUri, contentType);
        if (!TextUtils.isEmpty(contentType) && !TextUtils.isEmpty(fileName) && MessageUtils.isEndWithImageExtension(fileName) && TextUtils.equals(contentType, "application/oct-stream")) {
            intent.setDataAndType(dataUri, "image/*");
        }
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            MLog.e("RichMessageEditor", "Unsupported Format,startActivity(intent) error,intent");
            MessageUtils.showErrorDialog(getContext(), getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
            e.printStackTrace();
        }
    }

    public void setPosition(int pos) {
        this.mPosition = pos;
        MLog.d("zfatt", "zfatt setPosition mPosition " + this.mPosition);
    }

    public void resumePosition() {
        HwSlidePage sp = this.mHwSlidesView.getCurrentPage();
        if (sp != null) {
            sp.setTextFocus();
        }
    }

    public CharSequence getSmsText() {
        if (requiresMms()) {
            return null;
        }
        return getText();
    }

    public CharSequence getText() {
        if (this.mSmsEditorText.getVisibility() == 0) {
            return this.mSmsEditorText.getText().toString();
        }
        return null;
    }

    public CharSequence get7BitText() {
        return this.mWorkingMessage.get7BitText();
    }

    public CharSequence getSubjectText() {
        return this.mSubjectTextEditor.getText();
    }

    public void setListener(EditableSlides$RichMessageListener l) {
        this.mListener = l;
    }

    public void onKeyboardStateChanged(boolean isSmsEnabled, boolean isKeyboardOpen) {
        this.mSmsEditorText.setEnabled(isSmsEnabled);
        if (!isSmsEnabled) {
            if (this.mSubjectTextEditor != null) {
                this.mSubjectTextEditor.setFocusableInTouchMode(false);
            }
            this.mSmsEditorText.setFocusableInTouchMode(false);
            this.mSmsEditorText.setHint(R.string.sending_disabled_not_default_app);
        } else if (isKeyboardOpen) {
            if (this.mSubjectTextEditor != null && this.mSubjectTextEditor.getVisibility() == 0) {
                this.mSubjectTextEditor.setFocusableInTouchMode(true);
            }
            this.mSmsEditorText.setFocusableInTouchMode(true);
            if (RcsCommonConfig.isRCSSwitchOn()) {
                this.mSmsEditorText.setHint(R.string.type_to_compose_text_enter_to_send_new_sms);
            } else {
                this.mSmsEditorText.setHint(R.string.type_to_compose_text_enter_to_send);
            }
            if (this.mRcsRichMessageEditor != null) {
                this.mRcsRichMessageEditor.changeHint(this.mSmsEditorText);
            }
        } else {
            if (this.mSubjectTextEditor != null) {
                this.mSubjectTextEditor.setFocusable(false);
            }
            this.mSmsEditorText.setFocusable(false);
            this.mSmsEditorText.setHint(R.string.open_keyboard_to_compose_message);
        }
    }

    public void setSubject(CharSequence text, boolean notify) {
        if (notify) {
            this.mSubjectTextEditor.setText(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mWorkingMessage.setSubject(text, notify);
            return;
        }
        this.mWorkingMessage.setSubject(text, notify);
        this.mSubjectTextEditor.setText(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
    }

    public void setConversation(Conversation conv) {
        this.mWorkingMessage.setConversation(conv);
    }

    public void setIsForwardMms(boolean isForwardMms) {
        this.mIsForwardMms = isForwardMms;
    }

    public void setText(CharSequence text) {
        if (text != null) {
            this.mSmsEditorText.setTextKeepState(this.mParser.addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mSmsEditorText.setSelection(this.mSmsEditorText.length());
            return;
        }
        this.mSmsEditorText.setText("");
    }

    public void discard() {
        this.mWorkingMessage.discard();
    }

    public VcardModel getVcardModel() {
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        if (slideshow != null && slideshow.size() > 0) {
            SlideModel slideModel = slideshow.get(0);
            if (slideModel.size() > 0 && slideModel.hasVcard()) {
                return slideModel.getVcard();
            }
        }
        return null;
    }

    public SlideshowModel getSlideshow() {
        return this.mWorkingMessage.getSlideshow();
    }

    public void removeAttachment(boolean notify) {
        this.mWorkingMessage.removeAttachment(notify);
    }

    public boolean hasTooManyUnSentMsg() {
        return this.mWorkingMessage.hasTooManyUnSentMsg();
    }

    public void setWorkingRecipients(List<String> numbers) {
        this.mWorkingMessage.setWorkingRecipients(numbers);
    }

    public String getWorkingRecipients() {
        return this.mWorkingMessage.getWorkingRecipients();
    }

    public void setHasMultipleRecipients(boolean hasMultipleRecipients, boolean notify) {
        this.mWorkingMessage.setHasMultipleRecipients(hasMultipleRecipients, notify);
    }

    public void setHasEmail(boolean hasEmail, boolean notify) {
        this.mWorkingMessage.setHasEmail(hasEmail, notify);
    }

    public boolean requiresMms() {
        return this.mWorkingMessage.requiresMms();
    }

    public boolean isWorthSaving() {
        return this.mWorkingMessage.isWorthSaving();
    }

    public Conversation getConversation() {
        return this.mWorkingMessage.getConversation();
    }

    public boolean isDiscarded() {
        return this.mWorkingMessage.isDiscarded();
    }

    public void setDiscarded(boolean value) {
        this.mWorkingMessage.setDiscarded(value);
    }

    public void syncWorkingRecipients() {
        this.mWorkingMessage.syncWorkingRecipients();
    }

    public void writeStateToBundle(Bundle bundle) {
        this.mWorkingMessage.writeStateToBundle(bundle);
    }

    public CharSequence getSubject() {
        return this.mWorkingMessage.getSubject();
    }

    public void removeFakeMmsForDraft() {
        this.mWorkingMessage.removeFakeMmsForDraft();
    }

    public void asyncDeleteDraftSmsMessage(Conversation conv) {
        this.mWorkingMessage.asyncDeleteDraftSmsMessage(conv);
    }

    public boolean hasAttachment() {
        return this.mWorkingMessage.hasAttachment();
    }

    public int getSlideSize() {
        SlideshowModel slideShow = this.mWorkingMessage.getSlideshow();
        return slideShow != null ? slideShow.size() : 0;
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(this.mSmsEditorText.getText().toString());
    }

    public boolean hasSubject() {
        return !TextUtils.isEmpty(this.mSubjectTextEditor.getText().toString());
    }

    public boolean hasContentToSend() {
        return (hasAttachment() || hasText() || hasSubject()) ? true : MmsConfig.getSendingBlankSMSEnabled();
    }

    public void setNewMessageDraftSubid(int subid) {
        this.mWorkingMessage.setNewMessageDraftSubid(subid);
    }

    public boolean isEmptyThread(Conversation conv) {
        boolean z = true;
        if (this.mRcsRichMessageEditor == null || !this.mRcsRichMessageEditor.isRcsSwitchOn()) {
            if (conv.getThreadId() > 0) {
                if (conv.getThreadId() <= 0 || conv.getMessageCount() != 0) {
                    z = false;
                } else if (this.mWorkingMessage.hasAttachment() || this.mWorkingMessage.hasSlideshow() || this.mWorkingMessage.hasSubject() || this.mWorkingMessage.hasValidText()) {
                    z = false;
                }
            }
            return z;
        }
        return this.mRcsRichMessageEditor.isEmptyThread(this.mFragment, conv.getThreadId(), conv, this.mWorkingMessage);
    }

    public String getTextCount() {
        return MessageUtils.getTextCount(this.mWorkingMessage.getText());
    }

    public long computeAddRecordSizeLimit() {
        long sizeLimit = (long) (MmsConfig.getMaxMessageSize() - 4096);
        SlideshowModel slideShow = this.mWorkingMessage.getSlideshow();
        if (slideShow != null) {
            return sizeLimit - ((long) slideShow.getCurrentMessageSize());
        }
        return sizeLimit;
    }

    public String getMmsSizeRate() {
        int subjectSize = 0;
        int slideSize = 0;
        if (!TextUtils.isEmpty(this.mWorkingMessage.getSubject())) {
            subjectSize = MessageUtils.encodeText(this.mWorkingMessage.getSubject(), 106).length;
        }
        if (this.mWorkingMessage.getSlideshow() != null) {
            slideSize = this.mWorkingMessage.getSlideshow().getCurrentMessageSize();
        }
        int curSize = (((subjectSize + slideSize) - 1) / Place.TYPE_SUBLOCALITY_LEVEL_2) + 1;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        if (RcsCommonConfig.isRCSSwitchOn() && hasExceedsMmsLimit()) {
            curSize += 4;
        }
        return String.format(getContext().getString(R.string.mms_attach_size), new Object[]{nf.format((long) curSize), nf.format((long) (MmsConfig.getMaxMessageSize() / Place.TYPE_SUBLOCALITY_LEVEL_2))});
    }

    public void sendMessage(String recipients, int subscription) {
        this.mWorkingMessage.send(recipients, subscription);
    }

    public void readStateFromBundle(Bundle bundle) {
        this.mWorkingMessage.readStateFromBundle(bundle);
        resetCurrentPosition();
        refreshMediaAttachment(13);
    }

    public WorkingMessage getWorkingMessage() {
        return this.mWorkingMessage;
    }

    public boolean loadDraft(Conversation conv) {
        if (this.mWorkingMessage.isWorthSaving()) {
            MLog.w("RichMessageEditor", "CMA.loadDraft: called with non-empty working message, bail");
            return false;
        }
        setLoadDraftState(true);
        this.mWorkingMessage = WorkingMessage.loadDraft(this.mContext, this.mMessageStatuslistener, conv, new IDraftLoaded() {
            public void onDraftLoaded(Uri msgUri) {
                if (!RichMessageEditor.this.mWorkingMessage.hasSlideshow()) {
                    RichMessageEditor.this.setTextEditorRequiresMms(MessageUtils.get7BitText(RichMessageEditor.this.mWorkingMessage.getText()));
                }
                if (msgUri != null && "mms".equals(msgUri.getAuthority())) {
                    RichMessageEditor.this.mWorkingMessage.resetMmsDraftUri(msgUri);
                }
                RichMessageEditor.this.mIsLoadingDraft = true;
                RichMessageEditor.this.syncWorkingMessageToUI();
                RichMessageEditor.this.setLoadDraftState(false);
                RichMessageEditor.this.mListener.onDraftLoaded();
                CharSequence s = RichMessageEditor.this.mSmsEditorText.getText();
                String signature = SignatureUtil.getSignature(RichMessageEditor.this.getContext(), MmsConfig.getDefaultSignatureText());
                String lineSeparate = System.getProperty("line.separator", "");
                if (!(RichMessageEditor.this.mWorkingMessage.requiresMms() || TextUtils.isEmpty(s) || TextUtils.isEmpty(signature) || !SignatureUtil.deleteNewlineSymbol(s.toString()).endsWith(signature))) {
                    RichMessageEditor.this.setTextSelection((s.length() - signature.length()) - lineSeparate.length());
                }
                RichMessageEditor.this.resetCurrentPosition();
            }
        });
        this.mWorkingMessage.setConversation(conv);
        return true;
    }

    public void saveDraft(boolean isStopping, boolean recipientsHasNothing, boolean waitingForSubActivity, boolean hasToast) {
        clearInvalidMmsState();
        if (isDiscarded()) {
            LogTag.warn("workingmessage:: saveDraft mDiscarded: true skipping saving draft and bailing", new Object[0]);
            return;
        }
        boolean msgHasNothingToSave = !this.mWorkingMessage.isWorthSaving();
        boolean mmsMsgHasNothingToSave = false;
        if (!(this.mRcsRichMessageEditor == null || this.mRcsRichMessageEditor.getSaveMmsEmailAdress())) {
            mmsMsgHasNothingToSave = this.mWorkingMessage.requiresMms() && !this.mWorkingMessage.hasMmsContentToSave();
        }
        if (waitingForSubActivity || !((msgHasNothingToSave && recipientsHasNothing) || mmsMsgHasNothingToSave)) {
            this.mWorkingMessage.saveDraft(isStopping);
            if (MmsConfig.isSupportDraftWithoutRecipient()) {
                if ((this.mWorkingMessage.hasSmsDraft() || this.mWorkingMessage.hasMmsDraft()) && this.mWorkingMessage.getConversation().getMessageCount() == 0) {
                    StatisticalHelper.incrementReportCount(this.mContext, 2036);
                    if (hasToast) {
                        StatisticalHelper.incrementReportCount(this.mContext, 2205);
                    } else {
                        StatisticalHelper.incrementReportCount(this.mContext, 2206);
                    }
                }
            } else if (hasToast && ((this.mWorkingMessage.hasSmsDraft() || this.mWorkingMessage.hasMmsDraft()) && this.mWorkingMessage.getConversation().getMessageCount() == 0)) {
                StatisticalHelper.incrementReportCount(this.mContext, 2036);
            }
            return;
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.i("RichMessageEditor", "not worth saving, discard WorkingMessage and bail");
        }
        if (!((KeyguardManager) getContext().getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            this.mWorkingMessage.discard();
        }
    }

    private void clearInvalidMmsState() {
        if (TextUtils.isEmpty(getSubjectText()) && !isSubjectEditorVisible()) {
            this.mWorkingMessage.setSubject(null, false);
        }
    }

    public boolean isDraftLoading() {
        WorkingMessage workingMessage = this.mWorkingMessage;
        return WorkingMessage.isDraftLoading();
    }

    public void setDraftStateUnknow() {
        WorkingMessage workingMessage = this.mWorkingMessage;
        WorkingMessage.setDraftStateUnknow();
    }

    public void setWorkingMessageText(String text) {
        if (this.mWorkingMessage != null) {
            this.mWorkingMessage.setText(text);
        }
    }

    public void insertVcardText(final Uri uri) {
        final ProgressDialog waitDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.wait));
        ThreadEx.execute(new Runnable() {
            public void run() {
                VCardSmsMessage.createVNodeBuilder(uri, RichMessageEditor.this.getContext());
                final String insertString = VCardSmsMessage.getDisplayingVcardText(RichMessageEditor.this.getContext());
                HandlerEx -get6 = RichMessageEditor.this.mHandler;
                final ProgressDialog progressDialog = waitDialog;
                -get6.post(new Runnable() {
                    public void run() {
                        if (!RichMessageEditor.this.mFragment.isDetached()) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            if (insertString != null) {
                                EditText editor = RichMessageEditor.this.getEditor();
                                if (editor != null) {
                                    editor.getText().insert(editor.getSelectionEnd(), insertString);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public void insertVcalendarText(final ArrayList<Uri> uriList) {
        final ProgressDialog waitDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.wait));
        ThreadEx.execute(new Runnable() {
            public void run() {
                String BIRTHDAY_ACCOUNT_TYPE = "com.android.huawei.birthday";
                String SELECTION = "account_type = 'com.android.huawei.birthday'";
                long birthdayCalendarId = -1;
                Cursor cursor = null;
                try {
                    cursor = SqliteWrapper.query(RichMessageEditor.this.mContext, Calendars.CONTENT_URI, new String[]{"_id"}, "account_type = 'com.android.huawei.birthday'", null, null);
                    if (cursor == null || !cursor.moveToFirst()) {
                        birthdayCalendarId = -1;
                    } else {
                        birthdayCalendarId = cursor.getLong(0);
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            MLog.w("RichMessageEditor", "insertVcalendarText cursor close error ");
                        }
                    }
                } catch (RuntimeException e2) {
                    MLog.w("RichMessageEditor", "insertVcalendarText query. RuntimeException");
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e3) {
                            MLog.w("RichMessageEditor", "insertVcalendarText cursor close error ");
                        }
                    }
                } catch (Exception e4) {
                    MLog.w("RichMessageEditor", "insertVcalendarText query Exception ", e4);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e5) {
                            MLog.w("RichMessageEditor", "insertVcalendarText cursor close error ");
                        }
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e6) {
                            MLog.w("RichMessageEditor", "insertVcalendarText cursor close error ");
                        }
                    }
                }
                final String vCalText = VCalSmsMessage.getVCalText(uriList, RichMessageEditor.this.mContext, birthdayCalendarId);
                HandlerEx -get6 = RichMessageEditor.this.mHandler;
                final ProgressDialog progressDialog = waitDialog;
                -get6.postDelayed(new Runnable() {
                    public void run() {
                        if (!RichMessageEditor.this.mFragment.isDetached()) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            if (vCalText != null) {
                                EditText editor = RichMessageEditor.this.getEditor();
                                if (editor != null) {
                                    editor.getText().insert(editor.getSelectionEnd(), vCalText);
                                }
                            }
                        }
                    }
                }, 400);
            }
        });
    }

    private EditText getEditor() {
        if (this.mSubjectTextEditor != null && this.mSubjectTextEditor.hasFocus()) {
            return this.mSubjectTextEditor;
        }
        if (this.mPosition == -1) {
            return this.mSmsEditorText;
        }
        if (this.mHwSlidesView.size() > 0) {
            return this.mHwSlidesView.getCurrentPage().getMsgEditor();
        }
        return this.mSmsEditorText;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (MmsConfig.isExtraHugeEnabled(newConfig.fontScale)) {
            this.mSmsEditorText.setTextSize(1, 26.1f);
        }
    }

    public void getFocus(boolean request) {
        if (request && this.mSubjectTextEditor != null && !this.mSubjectTextEditor.hasFocus()) {
            this.mSmsEditorText.requestFocus();
        }
    }

    public boolean isSubjectEditorVisible() {
        return this.mSubjectTextEditor.getVisibility() == 0;
    }

    public void showSubjectEditor(boolean show) {
        int i;
        int i2 = 0;
        this.mSubjectTextEditor.setOnKeyListener(show ? this.mSubjectKeyListener : null);
        if (show) {
            this.mSubjectTextEditor.setText(this.mParser.addSmileySpans(this.mWorkingMessage.getSubject(), SMILEY_TYPE.MESSAGE_EDITTEXT));
        }
        EditTextWithSmiley editTextWithSmiley = this.mSubjectTextEditor;
        if (show) {
            i = 0;
        } else {
            i = 8;
        }
        editTextWithSmiley.setVisibility(i);
        View view = this.mSubjectSeperator;
        if (!show) {
            i2 = 8;
        }
        view.setVisibility(i2);
    }

    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    private void deleteSmiley(EditTextWithSmiley textEditor) {
        if (textEditor.getSelectionStart() == 0 && textEditor.getText().length() == 0) {
            textEditor.doEmptyDelete();
        } else {
            textEditor.onKeyDown(67, new KeyEvent(0, 67));
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AttachmentListItem item = (AttachmentListItem) ((SmileyFaceSelectorAdapter) parent.getAdapter()).getItem(position);
        String smiley = item.getTitle();
        if (smiley != null) {
            int index = item.getCommand();
            boolean isDelete;
            if (this.mSubjectTextEditor == null || !this.mSubjectTextEditor.hasFocus()) {
                EditTextWithSmiley textEditor = this.mSmsEditorText;
                if (this.mHwSlidesView != null) {
                    HwSlidePage sp = this.mHwSlidesView.getCurrentPage();
                    if (sp != null) {
                        textEditor = (EditTextWithSmiley) sp.getMsgEditor();
                    }
                }
                if (RcsCommonConfig.isRCSSwitchOn()) {
                    isDelete = index == 20;
                    if (this.mRcsRichMessageEditor != null) {
                        isDelete = this.mRcsRichMessageEditor.isDelete(index, isDelete);
                    }
                    if (isDelete) {
                        deleteSmiley(textEditor);
                        return;
                    }
                    textEditor.getText().insert(textEditor.getSelectionEnd(), this.mParser.addSmileySpans(smiley, SMILEY_TYPE.MESSAGE_EDITTEXT));
                } else {
                    textEditor.getText().insert(textEditor.getSelectionEnd(), smiley);
                    SmileyParser.setRecentEmojiTexts(smiley);
                }
                if (this.mRcsRichMessageEditor != null && this.mRcsRichMessageEditor.isRcsSwitchOn()) {
                    textEditor.getText().insert(textEditor.getSelectionEnd(), " ");
                }
                textEditor.requestFocus();
            } else if (RcsCommonConfig.isRCSSwitchOn()) {
                isDelete = index == 20;
                if (this.mRcsRichMessageEditor != null) {
                    isDelete = this.mRcsRichMessageEditor.isDelete(index, isDelete);
                }
                if (isDelete) {
                    if (this.mSubjectTextEditor.length() == 0) {
                        showSubjectEditor(false);
                        this.mSmsEditorText.requestFocus();
                    } else {
                        this.mSubjectTextEditor.onKeyDown(67, new KeyEvent(0, 67));
                    }
                    return;
                }
                this.mSubjectTextEditor.getText().insert(this.mSubjectTextEditor.getSelectionEnd(), this.mParser.addSmileySpans(smiley, SMILEY_TYPE.MESSAGE_EDITTEXT));
                this.mSubjectTextEditor.requestFocus();
            } else {
                this.mSubjectTextEditor.getText().insert(this.mSubjectTextEditor.getSelectionEnd(), smiley);
                SmileyParser.setRecentEmojiTexts(smiley);
                this.mSubjectTextEditor.requestFocus();
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                v.setBackgroundResource(R.drawable.ic_sms_delete_checked);
                if (this.mSubjectTextEditor == null || !this.mSubjectTextEditor.hasFocus()) {
                    this.mHandler.postDelayed(this.mStartLongClickRunnable, 500);
                    return true;
                } else if (this.mSubjectTextEditor.length() == 0) {
                    showSubjectEditor(false);
                    this.mSmsEditorText.requestFocus();
                    return true;
                } else {
                    this.mHandler.postDelayed(this.mStartLongClickRunnable, 500);
                    return true;
                }
            case 1:
            case 3:
                v.setBackgroundResource(R.drawable.ic_sms_delete);
                if (this.mHandler.hasCallbacks(this.mStartLongClickRunnable)) {
                    this.mHandler.removeCallbacks(this.mStartLongClickRunnable);
                }
                if (!this.mDelLongClicked) {
                    EditTextWithSmiley editTextWithSmiley = (this.mSubjectTextEditor == null || !this.mSubjectTextEditor.hasFocus()) ? this.mSmsEditorText : this.mSubjectTextEditor;
                    deleteSmiley(editTextWithSmiley);
                    if (this.mHwSlidesView != null) {
                        HwSlidePage sp = this.mHwSlidesView.getCurrentPage();
                        if (sp != null) {
                            deleteSmiley((EditTextWithSmiley) sp.getMsgEditor());
                        }
                    }
                }
                this.mDelLongClicked = false;
                return true;
            case 2:
                return true;
            default:
                this.mDelLongClicked = false;
                return true;
        }
    }

    public void resetMessage(Conversation conv) {
        if (this.mFragment == null || this.mFragment.getRcsComposeMessage() == null || !this.mFragment.getRcsComposeMessage().hasMmsDraftBeforeSendFt()) {
            MmsApp.getApplication().getThumbnailManager().clear();
            this.mHwSlidesView.removeAllSlideViews();
            this.mHwSlidesView.setVisible(false);
            setPosition(-1);
            if (this.mSmsEditorText.hasFocus()) {
                this.mSmsEditorText.clearFocus();
            }
            showSubjectEditor(false);
            this.mSmsEditorText.removeTextChangedListener(this.mTextEditorWatcher);
            this.mSubjectTextEditor.removeTextChangedListener(this.mSubjectEditorWatcher);
            TextKeyListener.clear(this.mSmsEditorText.getText());
            TextKeyListener.clear(this.mSubjectTextEditor.getText());
            if (this.mSmsEditorText.getVisibility() != 0) {
                this.mSmsEditorText.setVisibility(0);
            }
            this.mHandler.postDelayed(new DelayedSetSmsEditTextFocusRunnable(), 200);
            this.mSmsEditorText.addTextChangedListener(this.mTextEditorWatcher);
            this.mSubjectTextEditor.addTextChangedListener(this.mSubjectEditorWatcher);
        } else {
            this.mFragment.getRcsComposeMessage().resetHasDraftBeforeSendFt();
            MLog.d("RichMessageEditor", "resetMessage(),has mms draft before send file,not cleared MMS interface");
        }
        if (this.mRcsRichMessageEditor == null || !this.mRcsRichMessageEditor.getRcsSaveDraftWhenFt()) {
            this.mWorkingMessage.clearConversation(conv, false);
        } else {
            this.mRcsRichMessageEditor.setRcsLoadDraftFt(true);
            this.mRcsRichMessageEditor.setRcsSaveDraftWhenFt(false);
        }
        boolean discard = this.mWorkingMessage.isDiscarded();
        if (this.mFragment != null) {
            this.mWorkingMessage = WorkingMessage.createEmpty(this.mContext, this.mMessageStatuslistener);
            this.mWorkingMessage.setDiscarded(discard);
            this.mWorkingMessage.setConversation(conv);
        }
    }

    public void insertPhrase(CharSequence phrase) {
        EditText editor = getEditor();
        if (editor != null) {
            SpannableStringBuilder newText = new SpannableStringBuilder(editor.getText());
            int where = editor.getSelectionEnd();
            if (where > newText.length()) {
                where = newText.length();
            }
            newText.insert(where, phrase);
            where += phrase.length();
            editor.setText(newText);
            int txtLen = editor.length();
            if (where <= txtLen) {
                txtLen = where;
            }
            editor.setSelection(txtLen);
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (hasFocus() || isPressed()) {
            mergeDrawableStates(drawableState, CheckedStateSet);
        }
        return drawableState;
    }

    public void handleAddAttachmentError(int error, int mediaTypeStringId) {
        if (this.mFragment != null && !this.mFragment.isDetached()) {
            handleAddAttachmentError(this.mFragment.getActivity(), error, mediaTypeStringId);
        }
    }

    public void handleAddAttachmentError(final Activity activity, final int error, final int mediaTypeStringId) {
        if (error != 0) {
            MLog.d("RichMessageEditor", "handleAddAttachmentError: " + error);
            if (activity == null) {
                MLog.d("RichMessageEditor", "handleAddAttachmentError activity is null. ");
            } else {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        String title;
                        String message;
                        Resources res = RichMessageEditor.this.getResources();
                        String mediaType = res.getString(mediaTypeStringId);
                        switch (error) {
                            case -4:
                                title = res.getString(R.string.failed_to_resize_image);
                                message = res.getString(R.string.resize_image_error_information);
                                break;
                            case -3:
                                title = res.getString(R.string.unsupported_media_format_Toast, new Object[]{mediaType});
                                message = res.getString(R.string.select_different_media, new Object[]{mediaType});
                                break;
                            case -2:
                                title = res.getString(R.string.exceed_message_size_limitation, new Object[]{mediaType});
                                message = res.getString(R.string.failed_to_add_media_Toast, new Object[]{mediaType});
                                break;
                            case -1:
                                RichMessageEditor.this.checkSlidePageErr();
                                if (!(RichMessageEditor.this.mFragment.getActivity() instanceof HwBaseActivity) || !((HwBaseActivity) RichMessageEditor.this.mFragment.getActivity()).ismHasSmsPermissionsForUser()) {
                                    return;
                                }
                                return;
                            default:
                                throw new IllegalArgumentException("unknown error " + error);
                        }
                        MessageUtils.showErrorDialog(activity, title, message);
                    }
                });
            }
        }
    }

    private void checkSlidePageErr() {
        int i = 0;
        HwSlidePage sp = this.mHwSlidesView.getCurrentPage();
        if (sp != null) {
            sp.removeAttachment(this.mCurType);
            if (this.isNewSlidePage) {
                SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
                if (slideshow != null) {
                    SlideModel sModel = slideshow.get(this.mPosition);
                    if (sModel != null) {
                        if (sModel.size() > 1 || slideshow.size() != 1) {
                            SlideshowEditor slideEditor = this.mWorkingMessage.getSlideshowEditor();
                            if (slideEditor != null) {
                                slideEditor.removeSlide(this.mPosition);
                            }
                            this.mHwSlidesView.removeSlideView(this.mPosition);
                            this.mHwSlidesView.updateSplites();
                            if (this.mPosition != 0) {
                                i = this.mPosition - 1;
                            }
                            setPosition(i);
                            HwSlidePage pg = this.mHwSlidesView.getPage(this.mPosition);
                            if (pg != null) {
                                pg.setTextFocus();
                            }
                        } else {
                            switchToRichMode(false, false);
                        }
                    }
                }
            }
        }
    }

    private void removeErrorSlideModel(int position) {
        int i = 0;
        SlideshowModel slideshowModel = this.mWorkingMessage.getSlideshow();
        if (slideshowModel != null && slideshowModel.size() != 0) {
            SlideModel sModel = slideshowModel.get(position);
            if (sModel != null) {
                if (sModel.size() > 1 || slideshowModel.size() != 1) {
                    SlideshowEditor slideEditor = this.mWorkingMessage.getSlideshowEditor();
                    if (slideEditor != null) {
                        slideEditor.removeSlide(position);
                    }
                    this.mHwSlidesView.removeSlideView(position);
                    this.mHwSlidesView.updateSplites();
                    if (position != 0) {
                        i = position - 1;
                    }
                    setPosition(i);
                    HwSlidePage pg = this.mHwSlidesView.getPage(position);
                    if (pg != null) {
                        pg.setTextFocus();
                    }
                    return;
                }
                switchToRichMode(false, false);
            }
        }
    }

    public void appendSignature(boolean isCheckAppendSignature) {
        String signature = SignatureUtil.getSignature(getContext(), MmsConfig.getDefaultSignatureText());
        if (isCheckAppendSignature && this.mSmsEditorText != null && isContainSignature(this.mSmsEditorText.getText().toString(), signature)) {
            String temp = this.mSmsEditorText.getText().toString();
            if (temp.indexOf(signature) != -1) {
                this.mSmsEditorText.setText(this.mParser.addSmileySpans(temp.substring(0, temp.indexOf(signature)) + signature, SMILEY_TYPE.MESSAGE_EDITTEXT));
                this.mSmsEditorText.requestFocus();
            }
            return;
        }
        if (!TextUtils.isEmpty(signature)) {
            this.mSmsEditorText.setText(this.mParser.addSmileySpans(this.mSmsEditorText.getText() + System.getProperty("line.separator") + signature, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mSmsEditorText.requestFocus();
            this.mSmsEditorText.setSelection(0);
        }
    }

    public boolean isContainSignature(String content, String signature) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(signature) || !content.endsWith(signature)) {
            return false;
        }
        return true;
    }

    public boolean isContainSignature() {
        if (this.mContext == null || this.mWorkingMessage == null) {
            return false;
        }
        String signature = SignatureUtil.getSignature(this.mContext, MmsConfig.getDefaultSignatureText());
        CharSequence richEditorText = this.mWorkingMessage.getText();
        if (TextUtils.isEmpty(richEditorText)) {
            return false;
        }
        return isContainSignature(richEditorText.toString(), signature);
    }

    public void setEditTextFocus() {
        if (this.mHwSlidesView.size() <= 0) {
            this.mSmsEditorText.requestFocus();
            return;
        }
        HwSlidePage page = this.mHwSlidesView.getPage(this.mPosition);
        if (page != null) {
            page.setTextFocus();
        }
    }

    public void setSmsEditTextFocus() {
        this.mSmsEditorText.requestFocus();
    }

    public void setSubjectEditFocus() {
        this.mSubjectTextEditor.setSelection(0);
        this.mSubjectTextEditor.requestFocus();
    }

    AsyncDialog getAsyncDialog() {
        return getAsyncDialog(this.mFragment.getActivity());
    }

    AsyncDialog getAsyncDialog(Activity activity) {
        if (this.mAsyncDialog == null) {
            this.mAsyncDialog = new AsyncDialog(activity);
        } else if (this.mAsyncDialog.getActivity() != activity) {
            this.mAsyncDialog = new AsyncDialog(activity);
        }
        return this.mAsyncDialog;
    }

    private String[] getOperationStrings() {
        ArrayList<Integer> res = getOperations();
        int length = res.size();
        String[] retStrs = new String[length];
        for (int i = 0; i < length; i++) {
            SlideshowModel slideshow;
            switch (((Integer) res.get(i)).intValue()) {
                case R.string.remove_slide:
                case R.string.add_slide:
                case R.string.preview:
                    retStrs[i] = this.mContext.getString(((Integer) res.get(i)).intValue());
                    break;
                case R.string.duration_sec:
                    slideshow = this.mWorkingMessage.getSlideshow();
                    if (slideshow == null) {
                        break;
                    }
                    NumberFormat.getIntegerInstance().setGroupingUsed(false);
                    SlideModel slide = slideshow.get(this.mPosition);
                    retStrs[i] = getResources().getString(R.string.duration_sec, new Object[]{nf.format((long) (slide.getDuration() / 1000))});
                    break;
                case R.string.layout_top:
                    slideshow = this.mWorkingMessage.getSlideshow();
                    if (slideshow != null) {
                        if (slideshow.getLayout().getLayoutType() != 1) {
                            if (slideshow.getLayout().getLayoutType() != 0) {
                                break;
                            }
                            retStrs[i] = this.mContext.getString(R.string.layout_bottom);
                            break;
                        }
                        retStrs[i] = this.mContext.getString(R.string.layout_top);
                        break;
                    }
                    break;
                default:
                    break;
            }
        }
        return retStrs;
    }

    private ArrayList<Integer> getOperations() {
        ArrayList<Integer> menuItems = new ArrayList();
        if (this.mHwSlidesView == null || this.mHwSlidesView.size() == 0) {
            menuItems.add(Integer.valueOf(R.string.add_slide));
            return menuItems;
        }
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        if (!(slideshow == null || this.mHwSlidesView.size() == 0)) {
            SlideModel model = slideshow.get(0);
            if (model == null) {
                return menuItems;
            }
            if (model.hasVCalendar() || model.hasVcard()) {
                menuItems.add(Integer.valueOf(R.string.add_slide));
                menuItems.add(Integer.valueOf(R.string.remove_slide));
            } else {
                menuItems.add(Integer.valueOf(R.string.preview));
                HwSlidePage sp = this.mHwSlidesView.getPage(this.mPosition);
                if (sp != null && sp.isItemsFocused()) {
                    menuItems.add(Integer.valueOf(R.string.add_slide));
                    menuItems.add(Integer.valueOf(R.string.remove_slide));
                    menuItems.add(Integer.valueOf(R.string.duration_sec));
                }
            }
        }
        return menuItems;
    }

    public void showSlideOptionsDialog() {
        Builder builder = new Builder(this.mContext);
        builder.setIcon(R.drawable.csp_icon_card_cdma);
        builder.setTitle(getResources().getString(R.string.slide_options));
        builder.setItems(getOperationStrings(), new OnClickListener() {
            private int getOperationByMenuSeq(int menuSeq) {
                ArrayList<Integer> menus = RichMessageEditor.this.getOperations();
                if (menuSeq >= menus.size() || menuSeq < 0) {
                    return -1;
                }
                return getOperationByResId(((Integer) menus.get(menuSeq)).intValue());
            }

            private int getOperationByResId(int resId) {
                for (int i = 0; i < RichMessageEditor.OPS_ALL.length; i++) {
                    if (RichMessageEditor.OPS_ALL[i] == resId) {
                        return i;
                    }
                }
                return -1;
            }

            public void onClick(DialogInterface dialog, int which) {
                switch (getOperationByMenuSeq(which)) {
                    case 0:
                        RichMessageEditor.this.mFragment.setNeedSaveDraftStatus(false);
                        RichMessageEditor.this.mWorkingMessage.saveAsMms(false, false);
                        MessageUtils.viewMmsMessageAttachment(RichMessageEditor.this.mFragment, RichMessageEditor.this.mWorkingMessage.getMessageUri(), RichMessageEditor.this.mWorkingMessage.getSlideshow(), RichMessageEditor.this.getAsyncDialog());
                        break;
                    case 1:
                        MessageUtils.setIsMediaPanelInScrollingStatus(false);
                        StatisticalHelper.incrementReportCount(RichMessageEditor.this.mContext, 2102);
                        SlideshowModel slideshow = RichMessageEditor.this.mWorkingMessage.getSlideshow();
                        if (slideshow != null) {
                            SlideModel model = slideshow.get(0);
                            if (model != null) {
                                if (!model.hasVcard()) {
                                    if (model.hasVCalendar()) {
                                        Toast.makeText(RichMessageEditor.this.mContext, R.string.can_not_add_slide_with_vcalendar, 1).show();
                                        break;
                                    }
                                }
                                Toast.makeText(RichMessageEditor.this.mContext, R.string.can_not_add_slide_with_vcard, 1).show();
                                break;
                            }
                        }
                        RichMessageEditor.this.mShowHwSlidePage = true;
                        if (RichMessageEditor.this.mHwSlidesView != null) {
                            if (RichMessageEditor.this.mHwSlidesView.size() == 0) {
                                RichMessageEditor richMessageEditor;
                                if (RichMessageEditor.this.mWorkingMessage.ensureSlideshow()) {
                                    RichMessageEditor.this.isNewSlidePage = true;
                                }
                                if (RichMessageEditor.this.mPosition < 0) {
                                    richMessageEditor = RichMessageEditor.this;
                                    richMessageEditor.mPosition = richMessageEditor.mPosition + 1;
                                }
                                int count = (slideshow == null || slideshow.size() <= 1) ? 1 : slideshow.size();
                                RichMessageEditor.this.mPosition = RichMessageEditor.this.mHwSlidesView.size();
                                while (RichMessageEditor.this.mPosition < count) {
                                    RichMessageEditor.this.mHwSlidesView.addNewSlideView(RichMessageEditor.this.mPosition);
                                    richMessageEditor = RichMessageEditor.this;
                                    richMessageEditor.mPosition = richMessageEditor.mPosition + 1;
                                }
                                richMessageEditor = RichMessageEditor.this;
                                richMessageEditor.mPosition = richMessageEditor.mPosition - 1;
                                RichMessageEditor.this.updateViews();
                                RichMessageEditor.this.refreshMediaAttachment(13);
                            }
                            HwSlidePage sp = RichMessageEditor.this.mHwSlidesView.getPage(RichMessageEditor.this.mPosition);
                            int position = RichMessageEditor.this.createSlide(RichMessageEditor.this.mPosition + 1, RichMessageEditor.this.mCurType, true);
                            if (position != -1) {
                                RichMessageEditor.this.mPosition = position;
                                if (RichMessageEditor.this.mHwSlidesView != null) {
                                    RichMessageEditor.this.mHwSlidesView.addNewSlideView(RichMessageEditor.this.mPosition);
                                }
                                RichMessageEditor.this.refreshMediaAttachment(13);
                                RichMessageEditor.this.updateViews();
                                RichMessageEditor.this.mWorkingMessage.saveAsMms(true);
                                RichMessageEditor.this.mHwSlidesView.getPage(RichMessageEditor.this.mPosition).setTextFocus();
                                RichMessageEditor.this.mFragment.hideLandscapeMediaPicker(true);
                                break;
                            }
                            if (sp != null) {
                                sp.setTextFocus();
                            }
                            return;
                        }
                        return;
                        break;
                    case 2:
                        RichMessageEditor.this.delSlidePage(RichMessageEditor.this.mHwSlidesView.getPage(RichMessageEditor.this.mPosition));
                        RichMessageEditor.this.mFragment.hideLandscapeMediaPicker(true);
                        break;
                    case 3:
                        RichMessageEditor.this.showLayoutSelectorDialog();
                        break;
                    case 4:
                        RichMessageEditor.this.showDurationDialog();
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showLayoutSelectorDialog() {
        Builder builder = new Builder(this.mContext);
        builder.setIcon(R.drawable.csp_icon_card_gsm);
        builder.setTitle(getResources().getString(R.string.layout_selector_title));
        builder.setAdapter(new LayoutSelectorAdapter(this.mContext), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SlideshowModel slideshow = RichMessageEditor.this.mWorkingMessage.getSlideshow();
                SlideshowEditor editor = RichMessageEditor.this.mWorkingMessage.getSlideshowEditor();
                if (slideshow != null && editor != null) {
                    int i;
                    HwSlidePage page;
                    switch (which) {
                        case 0:
                            if (slideshow.getLayout().getLayoutType() != 1) {
                                editor.changeLayout(1);
                                for (i = 0; i < RichMessageEditor.this.mHwSlidesView.size(); i++) {
                                    page = RichMessageEditor.this.mHwSlidesView.getPage(i);
                                }
                                break;
                            }
                            break;
                        case 1:
                            if (slideshow.getLayout().getLayoutType() != 0) {
                                editor.changeLayout(0);
                                for (i = 0; i < RichMessageEditor.this.mHwSlidesView.size(); i++) {
                                    page = RichMessageEditor.this.mHwSlidesView.getPage(i);
                                }
                                break;
                            }
                            break;
                    }
                    HwSlidePage pg = RichMessageEditor.this.mHwSlidesView.getPage(RichMessageEditor.this.mPosition);
                    if (pg != null) {
                        pg.setTextFocus();
                    }
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private String[] getSlideTimesStrings(int arrayId) {
        String[] strings = getResources().getStringArray(arrayId);
        try {
            String language = Locale.getDefault().getLanguage();
            if (!"ar".equals(language) ? "fa".equals(language) : true) {
                int len = strings.length;
                NumberFormat nf = NumberFormat.getIntegerInstance();
                for (int i = 0; i < len; i++) {
                    strings[i] = changeNumberToLocal(strings[i], nf);
                }
            }
        } catch (NumberFormatException e) {
            MLog.w("RichMessageEditor", "changeStringNumberToLocal NumberFormatException " + Locale.getDefault());
        } catch (Exception e2) {
            MLog.w("RichMessageEditor", "changeStringNumberToLocal FAIL " + Locale.getDefault());
        }
        return strings;
    }

    private String changeNumberToLocal(String str, NumberFormat nf) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int len = str.length();
        int idx = 0;
        while (idx < len) {
            char c = str.charAt(idx);
            if (c < '0' || c > '9') {
                break;
            }
            idx++;
        }
        if (idx == 0) {
            return str;
        }
        String prefix = str.substring(0, idx);
        String surfix = str.substring(idx, len);
        int number = Integer.parseInt(prefix);
        StringBuffer sb = new StringBuffer();
        sb.append(nf.format((long) number)).append(surfix);
        return sb.toString();
    }

    public void showDurationDialog() {
        Builder builder = new Builder(this.mContext);
        builder.setIcon(R.drawable.csp_icon_card_cdma);
        String title = getResources().getString(R.string.duration_selector_title);
        SlideshowModel slideshow = this.mWorkingMessage.getSlideshow();
        if (slideshow != null) {
            boolean isSpanish = "es".equals(Locale.getDefault().getLanguage());
            NumberFormat nf = NumberFormat.getIntegerInstance();
            if (isSpanish) {
                builder.setTitle(title + " " + nf.format((long) (this.mPosition + 1)) + "/" + nf.format((long) slideshow.size()));
            } else {
                builder.setTitle(title + nf.format((long) (this.mPosition + 1)) + "/" + nf.format((long) slideshow.size()));
            }
            builder.setItems(getSlideTimesStrings(R.array.select_dialog_items), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which < 0 || which >= 10) {
                        SlideshowModel slideshow = RichMessageEditor.this.mWorkingMessage.getSlideshow();
                        if (slideshow != null) {
                            Intent intent = new Intent(RichMessageEditor.this.getContext(), EditSlideDurationActivity.class);
                            intent.putExtra("slide_index", RichMessageEditor.this.mPosition);
                            intent.putExtra("slide_total", slideshow.size());
                            intent.putExtra("dur", slideshow.get(RichMessageEditor.this.mPosition).getDuration() / 1000);
                            RichMessageEditor.this.mFragment.startActivityForResult(intent, 12);
                        }
                    } else {
                        SlideshowEditor editor = RichMessageEditor.this.mWorkingMessage.getSlideshowEditor();
                        if (editor != null) {
                            editor.changeDuration(RichMessageEditor.this.mPosition, (which + 1) * 1000);
                        }
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void changeSlideDuration(int duration) {
        SlideshowEditor editor = this.mWorkingMessage.getSlideshowEditor();
        if (editor != null) {
            editor.changeDuration(this.mPosition, duration);
        }
    }

    public void setTextSelection(int pos) {
        if (pos > this.mSmsEditorText.length()) {
            pos = this.mSmsEditorText.length();
        }
        this.mSmsEditorText.setSelection(pos);
    }

    private void setTextEditorRequiresMms(CharSequence text) {
        if (!TextUtils.isEmpty(text) && !MmsConfig.getMultipartSmsEnabled()) {
            boolean z;
            int msgCount = SmsMessage.calculateLength(text, false)[0];
            WorkingMessage workingMessage = this.mWorkingMessage;
            if (msgCount >= MmsConfig.getSmsToMmsTextThreshold()) {
                z = true;
            } else {
                z = false;
            }
            workingMessage.setLengthRequiresMms(z, false);
        }
    }

    public int getLineNumber() {
        if (this.mSmsEditorText.getVisibility() == 0) {
            return this.mSmsEditorText.getLineCount();
        }
        return 0;
    }

    public void setLengthRequiresMms(boolean mmsRequired, boolean notify) {
        this.mWorkingMessage.setLengthRequiresMms(mmsRequired, notify);
    }

    public void setRcsSaveDraftWhenFt(boolean rcsSaveDraftWhenFt) {
        if (this.mRcsRichMessageEditor != null) {
            this.mRcsRichMessageEditor.setRcsSaveDraftWhenFt(rcsSaveDraftWhenFt);
        }
    }

    public void changeRcsEditorHint() {
        if (this.mRcsRichMessageEditor != null) {
            this.mRcsRichMessageEditor.changeHint();
        }
    }

    public boolean getRcsLoadDraftFt() {
        if (this.mRcsRichMessageEditor != null) {
            return this.mRcsRichMessageEditor.getRcsLoadDraftFt();
        }
        return false;
    }

    public void removeSlide(int pos) {
        delSlidePage(this.mHwSlidesView.getPage(pos));
    }

    public void setUpdateSubjectReViewListener(UpdateSubjectReViewListener updateSubjectReviewListener) {
        this.mUpdateSubjectReviewListener = updateSubjectReviewListener;
    }

    public void removeSlide(SlideModel slideModel) {
        if (slideModel != null) {
            boolean removeResult = false;
            if (this.mWorkingMessage.getSlideshowEditor() != null) {
                removeResult = this.mWorkingMessage.getSlideshowEditor().removeSlide(slideModel);
            }
            if (removeResult) {
                this.isSlideDirty = true;
                resetSlideshowModel();
                if (slideModel.hasImage()) {
                    refreshMediaAttachment(2);
                    if (slideModel.hasLocation()) {
                        refreshMediaAttachment(8);
                    }
                } else if (slideModel.hasAudio()) {
                    refreshMediaAttachment(3);
                } else if (slideModel.hasVideo()) {
                    refreshMediaAttachment(5);
                }
            }
        }
    }

    public void removeSlide(SlideModel slideModel, int type) {
        if (slideModel != null) {
            boolean removeResult = false;
            boolean isDeletedSlide = isDeletedSingleSlid();
            switch (type) {
                case 2:
                case 8:
                    if (slideModel.hasImage()) {
                        String imageSourceBuild = slideModel.getImage().getSourceBuild();
                        removeResult = slideModel.removeImage();
                        if (removeResult && this.mWorkingMessage.getSlideshow() != null) {
                            this.mWorkingMessage.getSlideshow().removeImageSourceBuilds(imageSourceBuild);
                            break;
                        }
                    }
                    break;
                case 3:
                    if (slideModel.hasAudio()) {
                        removeResult = slideModel.removeAudio();
                        break;
                    }
                    break;
                case 5:
                    if (slideModel.hasVideo()) {
                        String videoSourceBuild = slideModel.getVideo().getSourceBuild();
                        removeResult = slideModel.removeVideo();
                        if (removeResult && this.mWorkingMessage.getSlideshow() != null) {
                            this.mWorkingMessage.getSlideshow().removeImageSourceBuilds(videoSourceBuild);
                            break;
                        }
                    }
                    break;
                case 6:
                    if (slideModel.hasVcard()) {
                        removeResult = slideModel.removeVcard();
                        break;
                    }
                    break;
                case 7:
                    if (slideModel.hasVCalendar()) {
                        removeResult = slideModel.removeVCalendar();
                        break;
                    }
                    break;
            }
            if (removeResult) {
                this.isSlideDirty = true;
                if (isDeletedSlide) {
                    switchToRichMode(false, true);
                    removeSlide(slideModel);
                } else if (slideModel.hasRoomForAttachment() && !showHwSlidePage(false)) {
                    if (slideModel.getText() == null) {
                        removeSlide(slideModel);
                    } else if (TextUtils.isEmpty(slideModel.getText().getText())) {
                        removeSlide(slideModel);
                    }
                }
                resetSlideshowModel();
                refreshMediaAttachment(type);
                this.mListener.onContentChange();
            }
        }
    }

    public void removeImageAttachment(Uri imageUri, int contentType) {
        if (imageUri == null) {
            MLog.d("RichMessageEditor", "removeImageAttachment failed, params is error.");
            return;
        }
        int imagePosition = getModelPosition(imageUri);
        if (imagePosition > -1) {
            this.isSlideDirty = true;
            SlideshowModel slideshowModel = getSlideshow();
            if (!(slideshowModel == null || slideshowModel.get(imagePosition) == null)) {
                removeSlide(slideshowModel.get(imagePosition), contentType);
            }
        }
    }

    private boolean isDeletedSingleSlid() {
        SlideshowModel slideshowModel = getSlideshow();
        if (slideshowModel == null || slideshowModel.size() != 1) {
            return false;
        }
        return true;
    }

    public void addRichAttachmentListener(RichAttachmentListener richattachmentlistener) {
        if (!this.mRichAttachmentListeners.contains(richattachmentlistener)) {
            this.mRichAttachmentListeners.add(richattachmentlistener);
        }
    }

    public void removeRichAttachmentListener(RichAttachmentListener richattachmentlistener) {
        if (this.mRichAttachmentListeners.contains(richattachmentlistener)) {
            this.mRichAttachmentListeners.remove(richattachmentlistener);
        }
    }

    private int getModelPosition(Uri imageUri) {
        int position = -1;
        SlideshowModel slideshowModel = getSlideshow();
        if (imageUri == null || slideshowModel == null) {
            return -1;
        }
        int i = 0;
        while (i < slideshowModel.size()) {
            SlideModel slideModel = slideshowModel.get(i);
            if (slideModel == null || !slideModel.hasImage() || !imageUri.getPath().equals(slideModel.getImage().getSourceBuild())) {
                if (slideModel != null && slideModel.hasVideo() && imageUri.getPath().equals(slideModel.getVideo().getSourceBuild())) {
                    position = i;
                    break;
                }
                i++;
            } else {
                position = i;
                break;
            }
        }
        return position;
    }

    private void resetSlideshowModel() {
        if (getSlideshow() != null && getSlideSize() == 0) {
            removeAttachment(true);
            resetCurrentPosition();
        }
    }

    private void resetCurrentPosition() {
        this.mPosition = getSlideSize() == 0 ? -1 : getSlideSize() - 1;
    }

    private void refreshMediaAttachment(int changedType) {
        for (RichAttachmentListener richAttachmentListener : this.mRichAttachmentListeners) {
            richAttachmentListener.onRichAttachmentChanged(changedType);
        }
    }

    public void setLoadDraftState(boolean loadDraftState) {
        this.mLoadDraftState = loadDraftState;
    }

    public void setTakePictureState(boolean state) {
        this.mTakePictureState = state;
    }

    public boolean getShowHwSlidePage() {
        return this.mShowHwSlidePage;
    }

    public boolean showHwSlidePage(boolean fromdraft) {
        SlideshowModel slideshowModel = getSlideshow();
        if (slideshowModel == null || slideshowModel.size() <= 1) {
            MLog.d("RichMessageEditor", "showHwSlidePage slideshowModel == null || slideshowModel.size() <=1");
            return false;
        }
        int i = 1;
        while (i < slideshowModel.size()) {
            SlideModel slideModel = slideshowModel.get(i);
            if (slideModel == null || slideModel.getText() == null || TextUtils.isEmpty(slideModel.getText().getText())) {
                i++;
            } else {
                MLog.d("RichMessageEditor", "showHwSlidePage hastext");
                return true;
            }
        }
        if (!fromdraft) {
            return this.mShowHwSlidePage;
        }
        for (i = 0; i < slideshowModel.size(); i++) {
            slideModel = slideshowModel.get(i);
            if (slideModel == null ? false : slideModel.hasRoomForAttachment()) {
                return true;
            }
            if (i > 0 && slideModel != null && slideModel.getText() != null && !TextUtils.isEmpty(slideModel.getText().getText())) {
                return true;
            }
        }
        MLog.d("RichMessageEditor", "showHwSlidePage only slidePage");
        return false;
    }

    public void setNewAttachment(Uri uri, int type, boolean append) {
        this.mCurType = type;
        this.mUri = uri;
        setNewAttachment(this.mFragment.getActivity(), uri, type, append);
    }

    public void setNewAttachment(ArrayList<Uri> uriLists, int type, final boolean append) {
        if (uriLists == null || uriLists.size() == 0) {
            MLog.d("RichMessageEditor", "setNewAttachmnet uriLists is invisible");
            return;
        }
        for (int i = 0; i < uriLists.size(); i++) {
            final Uri uri = (Uri) uriLists.get(i);
            final String mimType = inferMimeTypeForUri(this.mContext, uri);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (TextUtils.isEmpty(mimType)) {
                        MLog.w("RichMessageEditor", "setNewAttachment list uri type is null.");
                        RichMessageEditor.this.setNewAttachment(RichMessageEditor.this.mFragment.getActivity(), uri, 2, append);
                        return;
                    }
                    if (mimType.startsWith("video")) {
                        RichMessageEditor.this.setNewAttachment(RichMessageEditor.this.mFragment.getActivity(), uri, 5, append);
                    } else if (mimType.startsWith("image")) {
                        RichMessageEditor.this.setNewAttachment(RichMessageEditor.this.mFragment.getActivity(), uri, 2, append);
                    } else {
                        MLog.w("RichMessageEditor", "setNewAttachment list uri type is error.");
                        RichMessageEditor.this.setNewAttachment(RichMessageEditor.this.mFragment.getActivity(), uri, 2, append);
                    }
                }
            }, ((long) i) * 150);
        }
    }

    private String inferMimeTypeForUri(Context context, Uri uri) {
        String type = null;
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            type = context.getContentResolver().getType(uri);
        } else if ("file".equals(scheme)) {
            MLog.d("RichMessageEditor", "inferMimeTypeForUri->uri is file.");
        }
        if (type == null) {
            MLog.w("RichMessageEditor", "inferMimeTypeForUri->Unable to determine MIME type for uri=" + uri);
        }
        return type;
    }

    public void setNewAttachment(Activity activity, Uri uri, int type, boolean append) {
        this.isNewSlidePage = false;
        if (this.mWorkingMessage.ensureSlideshow()) {
            this.isNewSlidePage = true;
            if (this.mPosition < 0) {
                this.mPosition++;
            }
        }
        if (checkAppendAttachAvailable(type, uri)) {
            addNewAttachment(activity, type, uri);
        }
    }

    private void addNewAttachment(Activity activity, int type, Uri uri) {
        if (this.mWorkingMessage.ensureSlideshow()) {
            this.isNewSlidePage = true;
            if (this.mPosition < 0) {
                this.mPosition++;
            }
        }
        if (this.mPosition < 0) {
            this.mPosition++;
        }
        int addPosition = this.mAttachmentDataManager.getCanAddPosition(this.mPosition);
        if (addPosition >= getSlideSize()) {
            this.mAttachmentDataManager.addAttachment(addPosition, type, uri);
            AttachmentThreadManager.addAttachment(activity, addPosition, type, uri, this.mWorkingMessage, new RichAttachmentThreadCallBack());
            return;
        }
        SlideModel slideModel = getSlideshow().get(addPosition);
        while (slideModel != null && !slideModel.hasRoomForAttachment()) {
            addPosition++;
            slideModel = getSlideshow().get(addPosition);
        }
        this.mAttachmentDataManager.addAttachment(addPosition, type, uri);
        AttachmentThreadManager.addAttachment(activity, addPosition, type, uri, this.mWorkingMessage, new RichAttachmentThreadCallBack());
    }

    private void dispatchAttachmentResult(boolean result, final int position, final int type) {
        if (!result) {
            MLog.d("RichMessageEditor", "result set failed");
            if (this.mAttachmentDataManager.removeAttachmentState(position, type) == null) {
            }
        } else if (this.mAttachmentDataManager.isCanAddAttachment(position)) {
            AttachmentState resultAttachmentState = this.mAttachmentDataManager.removeAttachmentState(position, type);
            if (resultAttachmentState == null) {
                MLog.d("RichMessageEditor", "result can't add , resultAttachmentState is null");
                return;
            }
            final int resultCode = resultAttachmentState.getResultCode();
            int resultState = resultAttachmentState.getCurrentState();
            final MediaModel resultMediaModel = resultAttachmentState.getMediaModel();
            if (resultCode == 0 && resultState == 3) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        RichMessageEditor.this.addResultSlide(position, type, resultMediaModel);
                    }
                });
            } else if (resultCode != 0) {
                MLog.d("RichMessageEditor", "dispatchAttachmentResult create attachment failed.");
                this.mHandler.post(new Runnable() {
                    public void run() {
                        RichMessageEditor.this.handleErrorResult(RichMessageEditor.this.mContext, resultCode, type);
                    }
                });
            } else {
                MLog.d("RichMessageEditor", "dispatchAttachmentResult createState is not ATTACHMENT_STATE_LOADED.");
            }
            this.mAttachmentDataManager.notifyResultContinue();
        } else {
            MLog.d("RichMessageEditor", "result can't add ,postion is not small");
        }
    }

    private void addResultSlide(int position, int type, MediaModel mediaModel) {
        int result = 0;
        int resultPosition = -1;
        if (mediaModel == null) {
            handleErrorResult(getContext(), -1, type);
            refreshMediaAttachment(type);
            return;
        }
        try {
            SlideshowModel slideshowModel = this.mWorkingMessage.getSlideshow();
            if (slideshowModel == null) {
                MLog.d("RichMessageEditor", "addResultSlide slideshowModel is null.");
                return;
            }
            SlideModel slideModel;
            if (slideshowModel.size() <= position) {
                int createPosition;
                if (slideshowModel.get(this.mPosition) == null || !slideshowModel.get(this.mPosition).hasRoomForAttachment()) {
                    createPosition = createSlide(slideshowModel.size(), type, true);
                } else {
                    createPosition = this.mPosition;
                }
                if (createPosition < 0) {
                    MLog.d("RichMessageEditor", "addResultSlide createSlide failed");
                    refreshMediaAttachment(type);
                    return;
                }
                resultPosition = createPosition;
                slideModel = slideshowModel.get(createPosition);
                if (slideModel == null) {
                    MLog.d("RichMessageEditor", "addResultSlide slideModel is null.");
                    refreshMediaAttachment(type);
                    return;
                }
                slideModel.add(mediaModel);
                if (type == 3 || type == 5) {
                    slideModel.updateDuration(mediaModel.getDuration());
                }
                if (showHwSlidePage(false) && this.mHwSlidesView != null && resultPosition >= this.mHwSlidesView.size()) {
                    this.mHwSlidesView.addNewSlideView(resultPosition);
                    this.mHwSlidesView.getPage(resultPosition).setPosition(resultPosition);
                }
                this.isSlideDirty = true;
                updateAttachmentResult(resultPosition, type);
            } else {
                slideModel = slideshowModel.get(position);
                if (slideModel == null) {
                    MLog.d("RichMessageEditor", "addResultSlide slideModel is null.");
                    refreshMediaAttachment(type);
                    return;
                }
                resultPosition = position;
                if (slideModel.hasRoomForAttachment(type)) {
                    slideModel.add(mediaModel);
                    if (type == 3 || type == 5) {
                        slideModel.updateDuration(mediaModel.getDuration());
                    }
                    MLog.d("RichMessageEditor", "addResultSlide add SlidePage success, add success.");
                    if (showHwSlidePage(false) && this.mHwSlidesView != null && position >= this.mHwSlidesView.size()) {
                        MLog.d("RichMessageEditor", "addResultSlide add SlidePage success, addView.");
                        this.mHwSlidesView.addNewSlideView(position);
                        this.mHwSlidesView.getPage(position).setPosition(position);
                    }
                    this.isSlideDirty = true;
                    updateAttachmentResult(position, type);
                } else {
                    MLog.e("RichMessageEditor", "addResultSlide don't has room,need create");
                }
            }
            this.mPosition = resultPosition;
            this.isNewSlidePage = false;
            if (this.mAttachmentDataManager.getSize() == 0) {
                refreshMediaAttachment(type);
            }
            handleErrorResult(this.mContext, result, type);
        } catch (ExceedMessageSizeException e) {
            result = -2;
            if (null != null) {
                removeErrorSlideModel(resultPosition);
            }
        } catch (UnsupportContentTypeException e2) {
            result = -3;
            if (null != null) {
                removeErrorSlideModel(resultPosition);
            }
        } catch (ContentRestrictionException e3) {
            result = -1;
            if (null != null) {
                removeErrorSlideModel(resultPosition);
            }
        }
    }

    private void updateAttachmentResult(int position, int type) {
        updateViews(position);
        if (this.mRcsRichMessageEditor != null && this.mRcsRichMessageEditor.checkftToMms()) {
            this.mRcsRichMessageEditor.setFTtoMmsSendMessageModeAndDeleteChat();
        }
        if (type == 2 || type == 5) {
            SlideshowModel slideShowModel = getSlideshow();
            if (slideShowModel != null) {
                SlideModel slideModel = slideShowModel.get(position);
                if (slideModel != null) {
                    if (slideModel.hasVideo() || slideModel.hasImage()) {
                        slideShowModel.addImageSourceBuild(slideModel);
                    }
                }
            }
        }
    }

    private void handleErrorResult(Context context, int errorCode, int type) {
        if (errorCode != 0) {
            if (context == null) {
                MLog.d("RichMessageEditor", "handleErrorResult context is null. ");
                return;
            }
            String message;
            int i;
            int resTypeId = -1;
            switch (type) {
                case 2:
                case 8:
                    resTypeId = R.string.type_picture;
                    break;
                case 3:
                    resTypeId = R.string.type_audio;
                    break;
                case 5:
                    resTypeId = R.string.type_video;
                    break;
                case 6:
                    resTypeId = R.string.type_vcard;
                    break;
                case 7:
                    resTypeId = R.string.type_vCalendar;
                    break;
                default:
                    errorCode = -3;
                    break;
            }
            Resources res = getResources();
            String mediaType = res.getString(resTypeId);
            switch (errorCode) {
                case -4:
                    message = res.getString(R.string.resize_image_error_information);
                    break;
                case -3:
                    message = res.getString(R.string.attachment_unsupport_toast, new Object[]{mediaType});
                    break;
                case -2:
                    NumberFormat nf = NumberFormat.getIntegerInstance();
                    message = String.format(res.getString(R.string.attachment_add_toolarge_toast), new Object[]{nf.format((long) (MmsConfig.getMaxMessageSize() / Place.TYPE_SUBLOCALITY_LEVEL_2))});
                    break;
                case -1:
                    message = res.getString(R.string.attachment_add_failed, new Object[]{mediaType});
                    if ((this.mFragment.getActivity() instanceof HwBaseActivity) && !((HwBaseActivity) this.mFragment.getActivity()).ismHasSmsPermissionsForUser()) {
                        return;
                    }
                default:
                    throw new IllegalArgumentException("unknown error " + errorCode);
            }
            if (errorCode == -3) {
                i = 1;
            } else {
                i = 0;
            }
            Toast.makeText(context, message, i).show();
        }
    }

    public EditTextWithSmiley getMSmsEditorText() {
        return this.mSmsEditorText;
    }

    public void setFullSizeFlag(boolean flag) {
        this.mIsOriginalSize = flag;
    }

    public boolean getFullSizeFlag() {
        return this.mIsOriginalSize;
    }

    public ArrayList<String> getWorkingMessageSlideImageSourceBuilds() {
        return this.mWorkingMessage.getSlideshow().getImageSourceBuilds();
    }

    public String[] getRecipientNumbers() {
        return getConversation().getRecipients().getNumbers();
    }

    public boolean hasExceedsMmsLimit() {
        return this.mWorkingMessage == null ? false : this.mWorkingMessage.hasExceedsMmsLimit();
    }
}
