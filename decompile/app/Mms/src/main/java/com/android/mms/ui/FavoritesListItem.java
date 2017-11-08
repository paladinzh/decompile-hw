package com.android.mms.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ui.MessageItem.PduLoadedCallback;
import com.android.mms.ui.views.MmsViewSuperLayout;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsFavoritesListItem;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.IListItem;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

public class FavoritesListItem extends AvatarWidget implements OnClickListener, OnLongClickListener, CheckableView, PduLoadedCallback, SpandTouchMonitor, IListItem {
    private static final int[] CheckedStateSet = new int[]{16842912};
    private static float MULTI_CHOICE_MMS_ALPHA = 0.5f;
    private static float MULTI_RESET_CHOICE_MMS_ALPHA = ContentUtil.FONT_SIZE_NORMAL;
    public SpandTextView mBodyTextView;
    private CheckBox mCheckBox;
    private HashMap<View, OnClickListener> mClickWidgets = new HashMap();
    private float mFontScale = ContentUtil.FONT_SIZE_NORMAL;
    private Handler mHandler;
    private RcsFavoritesListItem mHwCust = null;
    private HashMap<View, OnLongClickListener> mLongClickWidgets = new HashMap();
    public View mMessageBlock;
    public View mMessageBlockSuper;
    private MessageItem mMessageItem;
    private TextView mMsgTarget;
    private MmsViewSuperLayout mSlideshowModelView;
    private SpandTextView mSubjectTextView;
    private TextView mTimeView;
    protected Runnable mUserChooosedRunner = null;

    public FavoritesListItem(Context context) {
        super(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsFavoritesListItem(context);
        }
    }

    public FavoritesListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsFavoritesListItem(context);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSubjectTextView = (SpandTextView) findViewById(R.id.subject_view);
        this.mSubjectTextView.setVisibility(8);
        this.mSubjectTextView.setSpandTouchMonitor(this);
        this.mBodyTextView = (SpandTextView) findViewById(R.id.text_view);
        this.mBodyTextView.setSpandTouchMonitor(this);
        this.mMsgTarget = (TextView) findViewById(R.id.msg_type_target);
        if (this.mHwCust != null) {
            this.mHwCust.onFinishInflate(this);
        }
        this.mMessageBlockSuper = findViewById(R.id.message_block_super);
        this.mMessageBlock = findViewById(R.id.message_block);
        this.mCheckBox = (CheckBox) findViewById(R.id.select);
        this.mTimeView = (TextView) findViewById(R.id.time_view);
        this.mSlideshowModelView = (MmsViewSuperLayout) findViewById(R.id.slide_show_mode_view);
    }

    public void bind(MessageItem msgItem, boolean convHasMultiRecipients, int position) {
        boolean sameItem = false;
        if (msgItem != null) {
            if (this.mMessageItem != null && this.mMessageItem.mMsgId == msgItem.mMsgId) {
                sameItem = true;
            }
            if (!(sameItem || this.mMessageItem == null)) {
                this.mMessageItem.setOnPduLoaded(null);
            }
            this.mMessageItem = msgItem;
            setViewClickListener(this.mMessageBlock, this);
            setViewLongClickListener(this.mMessageBlock, this);
            switch (this.mMessageItem.mMessageType) {
                case 130:
                    bindNotifInd();
                    break;
                default:
                    bindCommonMessage(sameItem);
                    if (this.mHwCust != null) {
                        this.mHwCust.bind(msgItem, this.mBodyTextView, this.mMessageBlock);
                        break;
                    }
                    break;
            }
            refreshDrawableState();
            msgItem.registerListItem(this);
        }
    }

    public MessageItem getMessageItem() {
        return this.mMessageItem;
    }

    private void bindNotifInd() {
        initSubject();
        initMessageAddress();
        this.mBodyTextView.setVisibility(8);
    }

    protected void bindCommonMessage(boolean sameItem) {
        initMessageBody();
        initSubject();
        setSubjectAndBodyLines();
        initMessageAddress();
        initAvatarView();
        initTimeView();
        if (this.mMessageItem.isSms()) {
            this.mMessageItem.setOnPduLoaded(null);
        } else {
            initMmsView();
        }
        if (this.mHwCust != null) {
            this.mHwCust.bindCommonMessage(this.mBodyTextView, this);
        }
        requestLayout();
    }

    private void initTimeView() {
        if (this.mMessageItem.isSms() || this.mMessageItem.isInComingMessage()) {
            this.mTimeView.setText(buildTime(this.mMessageItem.mDate));
        } else {
            this.mTimeView.setText(buildTime(this.mMessageItem.mDate / 1000));
        }
    }

    private void initAvatarView() {
        if (this.mMessageItem.mAddress != null && this.mMessageItem.isInComingMessage()) {
            updateAvatarIcon(this.mMessageItem.mAddress);
        } else if (!this.mMessageItem.isInComingMessage()) {
            updateMyAvatarIcon();
        }
    }

    private void initMmsView() {
        if (this.mMessageItem.mSlideshow == null) {
            this.mSlideshowModelView.bind(this.mMessageItem);
            this.mMessageItem.setOnPduLoaded(this);
            return;
        }
        this.mSlideshowModelView.bind(this.mMessageItem);
    }

    public void onPduLoaded(MessageItem messageItem) {
        if (messageItem != null && this.mMessageItem != null && messageItem.getMessageId() == this.mMessageItem.getMessageId()) {
            MLog.d("FavMessageListItem", "FavoritesListItem onPduLoaded-callback");
            this.mMessageItem.setCachedFormattedMessage(null);
            bindCommonMessage(true);
        }
    }

    public void refreshAddressText() {
        if (this.mMessageItem != null) {
            this.mMessageItem.setContactList(null);
            initMessageAddress();
        }
    }

    private void initMessageAddress() {
        boolean haveLoadedPdu = true;
        if (!this.mMessageItem.isSms() && this.mMessageItem.mSlideshow == null) {
            haveLoadedPdu = false;
        }
        if (haveLoadedPdu) {
            String targart;
            if (this.mMessageItem.isInComingMessage()) {
                String msgAddress = "";
                if (MessageUtils.isNeedLayoutRtl()) {
                    msgAddress = new StringBuffer().append('‪').append(this.mMessageItem.getMsgAddress(getContext())).append('‬').toString();
                } else {
                    msgAddress = this.mMessageItem.getMsgAddress(getContext());
                }
                targart = msgAddress;
            } else {
                targart = getContext().getString(R.string.messagelist_sender_self);
            }
            this.mMsgTarget.setText(targart);
            changeSmsPopStyle();
        }
    }

    private void changeSmsPopStyle() {
        if (this.mMessageItem.hasText() || this.mMessageItem.isSms()) {
            this.mMessageBlock.setVisibility(0);
            if (this.mMessageItem.isInComingMessage()) {
                this.mBodyTextView.setTextColor(ResEx.self().getMsgItemTextColorRecv());
                this.mSubjectTextView.setTextColor(ResEx.self().getMsgItemTextColorRecv());
                this.mBodyTextView.setLinkTextColor(getResources().getColor(R.color.incoming_msg_text_color));
                this.mMessageBlock.setBackground(this.mContext.getResources().getDrawable(R.drawable.message_pop_incoming_bg));
            } else {
                this.mBodyTextView.setTextColor(ResEx.self().getMsgItemTextColorSend());
                this.mSubjectTextView.setTextColor(ResEx.self().getMsgItemTextColorSend());
                this.mBodyTextView.setLinkTextColor(ResEx.self().getMsgItemTextColorSend());
                if (this.mMessageItem.mSmsServiceCenterForFavorites == null || !this.mMessageItem.mSmsServiceCenterForFavorites.startsWith("rcs")) {
                    this.mMessageBlock.setBackground(this.mContext.getResources().getDrawable(R.drawable.message_pop_send_bg));
                } else {
                    this.mMessageBlock.setBackground(this.mContext.getResources().getDrawable(R.drawable.message_pop_rcs_send_bg));
                }
            }
            return;
        }
        this.mMessageBlock.setVisibility(8);
    }

    private void initMessageBody() {
        boolean textLoaded = true;
        this.mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        if (!this.mMessageItem.isSms() && this.mMessageItem.mSlideshow == null) {
            textLoaded = false;
        }
        if (textLoaded) {
            CharSequence formattedMessage = this.mMessageItem.getCachedFormattedMessage();
            if (formattedMessage == null) {
                formattedMessage = MessageUtils.formatMessage(this.mMessageItem.mBody, this.mMessageItem.mHighlight, this.mMessageItem.mTextContentType, this.mFontScale);
                this.mMessageItem.setCachedFormattedMessage(formattedMessage);
            }
            if (TextUtils.isEmpty(formattedMessage)) {
                this.mBodyTextView.setVisibility(8);
            } else {
                this.mBodyTextView.setText(formattedMessage, this.mMessageItem.mMsgtextSpan);
                this.mBodyTextView.setVisibility(0);
            }
        }
    }

    private void initSubject() {
        boolean hasSubject;
        if (TextUtils.isEmpty(this.mMessageItem.mSubject)) {
            hasSubject = false;
        } else {
            hasSubject = true;
        }
        if (hasSubject) {
            CharSequence subject = MessageUtils.formatMessage(this.mMessageItem.mSubject, this.mMessageItem.mHighlight, this.mMessageItem.mTextContentType, this.mFontScale);
            SpannableStringBuilder buf = new SpannableStringBuilder().append(TextUtils.replace(this.mContext.getResources().getString(R.string.inline_subject_new), new String[]{"%s"}, new CharSequence[]{subject}));
            this.mSubjectTextView.setVisibility(0);
            this.mSubjectTextView.setText(buf);
            return;
        }
        this.mSubjectTextView.setText("");
        this.mSubjectTextView.setVisibility(8);
    }

    public void setMsgListItemHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void onClick(View v) {
    }

    public boolean onLongClick(View v) {
        return v.showContextMenu();
    }

    protected void sendMessage(MessageItem messageItem, int message) {
        if (this.mHandler != null) {
            Message msg = Message.obtain(this.mHandler, message);
            msg.obj = messageItem;
            msg.sendToTarget();
        }
    }

    private void setViewLongClickListener(View v, OnLongClickListener listener) {
        this.mLongClickWidgets.put(v, listener);
        if (isEditAble()) {
            v.setLongClickable(false);
        } else {
            v.setOnLongClickListener(listener);
        }
    }

    private void setViewClickListener(View v, OnClickListener listener) {
        this.mClickWidgets.put(v, listener);
        if (isEditAble()) {
            v.setClickable(false);
        } else {
            v.setOnClickListener(listener);
        }
    }

    public boolean isMms() {
        return this.mMessageItem.isMms();
    }

    public boolean isEditAble() {
        return this.mCheckBox != null && this.mCheckBox.getVisibility() == 0;
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBox != null) {
            this.mCheckBox.setChecked(checked);
            refreshDrawableState();
        }
    }

    public void toggle() {
        setChecked(!isChecked());
        refreshDrawableState();
    }

    public void setEditAble(boolean editAble) {
        if (this.mCheckBox != null) {
            if (editAble) {
                this.mCheckBox.setVisibility(0);
                for (View v : this.mClickWidgets.keySet()) {
                    v.setClickable(false);
                }
                for (View v2 : this.mLongClickWidgets.keySet()) {
                    v2.setLongClickable(false);
                }
                setEnabled(true);
            } else {
                this.mCheckBox.setVisibility(8);
                for (Entry<View, OnClickListener> entry : this.mClickWidgets.entrySet()) {
                    ((View) entry.getKey()).setOnClickListener((OnClickListener) entry.getValue());
                }
                for (Entry<View, OnLongClickListener> entry2 : this.mLongClickWidgets.entrySet()) {
                    ((View) entry2.getKey()).setOnLongClickListener((OnLongClickListener) entry2.getValue());
                }
                setEnabled(false);
            }
        }
    }

    public void setEditAble(boolean editable, boolean checked) {
        if (!editable) {
            checked = false;
        }
        setChecked(checked);
        setEditAble(editable);
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void onTouchOutsideSpanText() {
        if (this.mMessageItem.mAttachmentType != 1) {
            sendMessage(this.mMessageItem, 1000102);
        }
    }

    public void onSpanTextPressed(boolean pressed) {
        this.mMessageBlock.setPressed(pressed);
    }

    public boolean isEditTextClickable() {
        return !isEditAble();
    }

    public boolean setTextScale(float scale) {
        boolean changed = Math.abs(this.mFontScale - scale) > 0.01f;
        this.mFontScale = scale;
        float fontsize = HwUiStyleUtils.getFavouritesScalableFontSize(getResources()) * scale;
        this.mBodyTextView.setTextSize(2, fontsize);
        this.mSubjectTextView.setTextSize(2, fontsize);
        this.mMsgTarget.setTextSize(2, 10.0f * scale);
        return changed;
    }

    public void setCheckboxEnable(boolean value) {
        this.mCheckBox.setEnabled(value);
        setActivated(!value);
        if (value) {
            setAlpha(MULTI_RESET_CHOICE_MMS_ALPHA);
        } else {
            setAlpha(MULTI_CHOICE_MMS_ALPHA);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEditAble()) {
            return true;
        }
        if (this.mUserChooosedRunner == null) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == 1) {
            HwBackgroundLoader.getUIHandler().post(this.mUserChooosedRunner);
            this.mUserChooosedRunner = null;
        }
        return true;
    }

    protected int getContentResId() {
        return R.id.content;
    }

    public boolean onDoubleTapUp(boolean isLink) {
        MessageUtils.viewMessageText(getContext(), this.mMessageItem);
        return true;
    }

    public void onTouchLink(ClickableSpan span) {
    }

    public void setItemText(MessageItem msgItem) {
        this.mBodyTextView.setText(MessageUtils.formatMessage(msgItem.mBody, msgItem.mSubId, msgItem.mHighlight, msgItem.mTextContentType, this.mFontScale), msgItem.mMsgtextSpan);
    }

    public long getMsgItemID() {
        return this.mMessageItem == null ? 0 : this.mMessageItem.mMsgId;
    }

    public View getMessageBlockSuper() {
        return this.mMessageBlockSuper;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEditAble()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void setSubjectAndBodyLines() {
        if (this.mMessageItem.mAddress != null) {
            boolean hasSubject = !TextUtils.isEmpty(this.mMessageItem.mSubject);
            if (this.mMessageItem.mAttachmentType <= 1 || this.mMessageItem.isFirstSlideVcardOrVcalendar()) {
                this.mSubjectTextView.setSingleLine(false);
                this.mBodyTextView.setMaxLines(Integer.MAX_VALUE);
            } else if (hasSubject) {
                this.mSubjectTextView.setSingleLine(true);
                this.mBodyTextView.setMaxLines(2);
            } else {
                this.mBodyTextView.setMaxLines(3);
            }
        }
    }

    protected int getConversationListTimeMode(Calendar cal) {
        return super.getConversationListTimeMode(cal);
    }

    protected int getFavoritesTimeMode(Calendar cal) {
        return super.getFavoritesTimeMode(cal);
    }

    protected int getMessageListItemTimeMode(Calendar cal, boolean showWeek) {
        return super.getMessageListItemTimeMode(cal, showWeek);
    }

    protected int getNoticeListItemTimeMode(Calendar cal, boolean showWeek) {
        return super.getNoticeListItemTimeMode(cal, showWeek);
    }

    protected void setTime(long temestamp, int mode) {
        super.setTime(temestamp, mode);
    }

    protected void updateUnreadIcon(boolean unreadMode) {
        super.updateUnreadIcon(unreadMode);
    }
}
