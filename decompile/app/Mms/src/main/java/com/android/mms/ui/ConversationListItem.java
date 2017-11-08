package com.android.mms.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsConversationListItem;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.ui.TextViewSnippet;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import java.util.List;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConversationListItem extends AvatarWidget implements UpdateListener, CheckableView {
    public static final StyleSpan STYLE_BOLD = new StyleSpan(1);
    public static final StyleSpan STYLE_NORMAL = new StyleSpan(0);
    private static final boolean isMultySimEnabled = MessageUtils.isMultiSimEnabled();
    private static List<View> mConversationListItems = new Vector();
    private static final ResEx sRes = ResEx.self();
    private static boolean sSupportHwMsg = false;
    private final boolean IS_MIRROR_LANGUAGE = MessageUtils.isNeedLayoutRtl();
    private View mAttachmentView;
    private CheckBox mCheckBox;
    private Conversation mConversation;
    private TextView mCountAndDraftView;
    private CryptoConversationListItem mCryptoConversationListItem = new CryptoConversationListItem();
    private TextView mDateView;
    private FromLayout mFromLayoutEx;
    private TextView mFromView;
    private boolean mHasAttachment = false;
    private boolean mHasError = false;
    private HwCustConversationListItem mHwCustConversationListItem;
    private boolean mLastError = false;
    private TextView mMsgCount;
    private int mNumberType = 0;
    Paint mPaint;
    private ImageView mPinupView;
    private RcsConversationListItem mRcsConversationListItem;
    private String mSnippet;
    private ImageView mStepInImg;
    private RelativeLayout mSubjectLayout;
    private TextView mSubjectView;
    private ImageView mSubscriptionNetworkTypeView;
    private TextView mUnreadCountView;

    public void setSnippet(String snippet) {
        this.mSnippet = snippet;
    }

    public String getSnippet() {
        return this.mSnippet;
    }

    public static final void setSupportHwMsg(boolean support) {
        sSupportHwMsg = support;
    }

    public ConversationListItem(Context context) {
        super(context);
        setWillNotDraw(false);
        this.mHwCustConversationListItem = (HwCustConversationListItem) HwCustUtils.createObj(HwCustConversationListItem.class, new Object[]{context});
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsConversationListItem == null) {
            this.mRcsConversationListItem = new RcsConversationListItem(context);
        }
    }

    public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        this.mHwCustConversationListItem = (HwCustConversationListItem) HwCustUtils.createObj(HwCustConversationListItem.class, new Object[]{context});
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsConversationListItem == null) {
            this.mRcsConversationListItem = new RcsConversationListItem(context);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFromView = (TextView) findViewById(R.id.from);
        this.mSubjectView = (TextView) findViewById(R.id.subject);
        this.mCountAndDraftView = (TextView) findViewById(R.id.count_and_draft);
        this.mUnreadCountView = (TextView) findViewById(R.id.unread_count);
        this.mAttachmentView = findViewById(R.id.attachment);
        this.mSubscriptionNetworkTypeView = (ImageView) findViewById(R.id.subscriptionnetworktype);
        this.mPinupView = (ImageView) findViewById(R.id.put_mms_onTop);
        this.mStepInImg = (ImageView) findViewById(R.id.step_in_img);
        this.mSubjectLayout = (RelativeLayout) findViewById(R.id.subject_layout);
        this.mFromLayoutEx = (FromLayout) findViewById(R.id.from_layout_ex);
        this.mMsgCount = (TextView) findViewById(R.id.msg_count);
        this.mDateView = (TextView) findViewById(R.id.date_view);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.FILL_AND_STROKE);
        int color = HwUiStyleUtils.getControlColor(this.mContext.getResources());
        if (color != 0) {
            this.mPaint.setColor(color);
        } else {
            this.mPaint.setColor(getResources().getColor(R.color.incoming_msg_text_color));
        }
        adaptAttrForHugeFont();
        this.mFromView.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                ConversationListItem.this.mFromLayoutEx.resizeLayout();
            }
        });
        if (this.mRcsConversationListItem != null) {
            this.mRcsConversationListItem.initCustViewStub(this);
        }
        if (this.mHwCustConversationListItem != null) {
            this.mHwCustConversationListItem.initlizeThumbnailAttachment(this);
        }
    }

    public void setSubscriptionNetworkTypeIcon(Conversation conv) {
        if (!MessageUtils.isMultiSimEnabled() || this.mSubscriptionNetworkTypeView == null) {
            return;
        }
        if (conv.hasDraft() || !isCommonItem()) {
            this.mSubscriptionNetworkTypeView.setVisibility(8);
            return;
        }
        int subId = conv.getSubId();
        if (subId == 0 || subId == 1) {
            this.mSubscriptionNetworkTypeView.setImageDrawable(ResEx.self().getCardIcon(subId));
        } else {
            MLog.v("Mms_threadcache", "mSubId invalid! Set mSubcription view invisible!");
            this.mSubscriptionNetworkTypeView.setVisibility(8);
        }
    }

    private final void updateFromView(boolean unreadMode, Context context) {
        String fromName;
        Object fromPurpose = null;
        if (isHwNotifactionsItem()) {
            fromName = getResources().getString(R.string.mms_hw_notification);
        } else if (isNotifactionsItem()) {
            fromName = getResources().getString(R.string.mms_common_notification);
        } else if (this.mNumberType == 3) {
            fromName = this.mConversation.getFromNumberForHw(getContext(), this.mSnippet, this);
            if (TextUtils.isEmpty(fromName)) {
                fromName = this.mConversation.getRecipients().formatNames(", ");
                if (!TextUtils.isEmpty(fromName)) {
                    fromName = this.mConversation.getRecipients().formatNoNameContactNumber(", ");
                }
            }
        } else {
            fromName = this.mConversation.getRecipients().formatNames(", ");
            if (!TextUtils.isEmpty(fromName)) {
                fromName = this.mConversation.getRecipients().formatNoNameContactNumber(", ");
            }
            fromPurpose = this.mConversation.getRecipients().getPurpose();
        }
        if (this.mRcsConversationListItem != null && this.mRcsConversationListItem.isRcsSwitchOn()) {
            fromName = this.mRcsConversationListItem.getGroupDefaultName(this.mContext, fromName, this.mConversation, getResources());
        }
        if (fromName != null) {
            int textColor = (unreadMode || isHwNotifactionsItem() || isNotifactionsItem()) ? sRes.getConvItemNewMsgColor() : sRes.getConvItemNormalColor();
            updateTextColor(this.mFromView, textColor);
            if (!TextUtils.isEmpty(fromPurpose)) {
                this.mFromView.setText(TextViewSnippet.getPurposeSpannableStringBuilder(fromName, fromPurpose, this.IS_MIRROR_LANGUAGE));
            } else if (TextUtils.isEmpty(fromName)) {
                this.mFromView.setText("");
                Radar.reportChr(0, 1312, "thread " + this.mConversation.getThreadId() + " has a empty name.start repaire it");
                if (context != null && (context instanceof ConversationList)) {
                    Fragment fragment = ((ConversationList) context).getFragment();
                    if (fragment != null && (fragment instanceof BaseConversationListFragment)) {
                        ((BaseConversationListFragment) fragment).addToEmptyNameThreadList(this.mConversation.getThreadId());
                    }
                }
            } else {
                this.mFromView.setText(fromName);
            }
        }
    }

    private void updateVisibility(View v, int visibility) {
        if (visibility != v.getVisibility()) {
            v.setVisibility(visibility);
        }
    }

    private void updateTextColor(TextView text, int color) {
        ColorStateList colors = ColorStateList.valueOf(color);
        if (text.getTextColors() != colors) {
            text.setTextColor(colors);
        }
    }

    public void onUpdate(Contact updated) {
        if (Conversation.isContactChanged(updated, this.mConversation)) {
            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                public void run() {
                    boolean z = false;
                    if (ConversationListItem.this.mConversation == null) {
                        MLog.e("ConversationListItem", "update contact while conversation = null " + this);
                        return;
                    }
                    MLog.v("ConversationListItem", "update contact: " + this);
                    ConversationListItem conversationListItem = ConversationListItem.this;
                    if (ConversationListItem.this.mConversation.getUnreadMessageCount() > 0) {
                        z = true;
                    }
                    conversationListItem.updateFromView(z, ConversationListItem.this.getContext());
                    ContactList contactList = ConversationListItem.this.mConversation.getRecipients();
                    if (contactList != null && contactList.size() == 1) {
                        ConversationListItem.this.updateSingleAvatarView(contactList);
                    }
                }
            });
        }
    }

    public final void bind(Context context, Conversation conversation, boolean isEtralHuge, boolean isEditMode, boolean isSelect, boolean isNotificationList, boolean isScrolling) {
        this.mSplitContext = context;
        updateIconStyle(false);
        this.mConversation = conversation;
        boolean isRcsGroupChat = false;
        if (this.mRcsConversationListItem != null) {
            isRcsGroupChat = this.mRcsConversationListItem.isGroupChat(this.mConversation);
        }
        ContactList contactList = this.mConversation.getRecipients();
        int unreadMsgSize = this.mConversation.getUnreadMessageCount();
        this.mHasError = this.mConversation.hasError();
        this.mLastError = this.mConversation.lastMessageIsError();
        this.mHasAttachment = this.mConversation.hasAttachment();
        boolean unreadMode = unreadMsgSize > 0;
        int numberType = this.mConversation.getNumberType();
        if (isNotificationList) {
            if (sSupportHwMsg && numberType == 1) {
                this.mNumberType = 3;
            } else {
                this.mNumberType = 0;
            }
        } else if (numberType == 1) {
            this.mNumberType = 1;
        } else if (numberType == 2) {
            this.mNumberType = 2;
        } else {
            this.mNumberType = 0;
        }
        boolean isCommonItem = isCommonItem();
        if (isCommonItem) {
            this.mStepInImg.setVisibility(8);
            this.mDateView.setVisibility(0);
            this.mSubjectLayout.setVisibility(0);
        } else {
            this.mStepInImg.setVisibility(0);
            this.mDateView.setVisibility(8);
            this.mSubjectLayout.setVisibility(8);
        }
        switch (this.mNumberType) {
            case 1:
                updateHuaweiNotificationAvatarIcon();
                break;
            case 2:
                updateNotificationAvatarIcon();
                break;
            default:
                updateAvatarIcon(contactList, isRcsGroupChat, isScrolling, this.mConversation, this.mNumberType);
                break;
        }
        setEditAble(isEditMode, isSelect ? isCommonItem : false);
        updateDraftView(isEditMode);
        updatePinupView();
        if (unreadMsgSize > 0) {
            this.mUnreadCountView.setText(getResources().getString(R.string.mms_message_num_count, new Object[]{Integer.valueOf(unreadMsgSize)}));
            updateVisibility(this.mUnreadCountView, 0);
        } else {
            updateVisibility(this.mUnreadCountView, 8);
        }
        if (this.mRcsConversationListItem != null) {
            this.mRcsConversationListItem.bindCustView(this, this.mConversation);
        }
        setSubscriptionNetworkTypeIcon(conversation);
        updateFromView(unreadMode, context);
        updateUnreadIcon(unreadMode);
        updateMsgCount();
        updateSubjectView();
        if (isCommonItem) {
            View view = this.mAttachmentView;
            int attachmentVisiblity = (!this.mHasAttachment || this.mLastError) ? 8 : this.mHwCustConversationListItem == null ? 0 : this.mHwCustConversationListItem.getAttachmentVisiblity(0);
            updateVisibility(view, attachmentVisiblity);
        } else {
            updateVisibility(this.mAttachmentView, 8);
        }
        if (!(this.mRcsConversationListItem == null || this.mConversation.getHwCust() == null)) {
            RcsConversationListItem rcsConversationListItem = this.mRcsConversationListItem;
            boolean z = (isEditAble() || !isCommonItem() || this.mHasError) ? true : !this.mConversation.getHwCust().hasUndeliveredMsg();
            rcsConversationListItem.showUndeliveredView(z);
        }
        this.mDateView.setText(buildTime(conversation.getDate()));
        this.mCryptoConversationListItem.updateEncryptSmsImgVisible(this, getSnippet(), this.mSubjectView, this.mHasError ? this.mLastError : false);
        if (this.mHwCustConversationListItem != null) {
            this.mHwCustConversationListItem.showThumbnailAttachment(this, this.mConversation.getHwCustConversationObj(), this.mHasAttachment);
        }
    }

    private void updateMsgCount() {
        int msgCount = this.mConversation.getMessageCount();
        int msgUnreadCount = this.mConversation.getUnreadMessageCount();
        if (!MmsConfig.isShowTotalCount() || msgCount <= 1 || !isCommonItem() || msgUnreadCount > 0) {
            updateVisibility(this.mMsgCount, 8);
            return;
        }
        updateVisibility(this.mMsgCount, 0);
        this.mMsgCount.setText(getResources().getString(R.string.mms_message_num_count, new Object[]{Integer.valueOf(msgCount)}));
        updateTextColor(this.mMsgCount, sRes.getConvItemCountTextColor());
    }

    public final void updateDraftViewWhenSplit() {
        updateDraftView(true);
    }

    private final void updateSubjectView() {
        SpannableStringBuilder subjectText = new SpannableStringBuilder();
        if (this.mHasError && this.mLastError) {
            updateTextColor(this.mSubjectView, sRes.getConvItemErrorMsgTextColor());
            this.mSubjectView.setText(getResources().getString(R.string.mms_has_error_message));
        } else if (this.mConversation.getHwCust() == null || !this.mConversation.getHwCust().hasUndeliveredMsg() || this.mConversation.hasDraft()) {
            updateTextColor(this.mSubjectView, sRes.getConvItemNormalColor());
            if (TextUtils.isEmpty(this.mSnippet)) {
                subjectText.append("");
                ResEx.setMarqueeText(this.mSubjectView, subjectText);
                return;
            }
            if (this.mRcsConversationListItem == null || !this.mRcsConversationListItem.isRcsSwitchOn()) {
                SmileyParser.getInstance().addSmileySpans(this.mSnippet, SMILEY_TYPE.CONV_LIST_TEXTVIEW, subjectText);
            } else {
                this.mRcsConversationListItem.addConversationSmileySpans(this.mSnippet, SMILEY_TYPE.CONV_LIST_TEXTVIEW, subjectText, this.mConversation, SmileyParser.getInstance());
            }
            if (this.mRcsConversationListItem != null && this.mRcsConversationListItem.isRcsSwitchOn() && Conversation.isGroupConversation(this.mContext, this.mConversation.getThreadId())) {
                try {
                    JSONObject obj = new JSONObject(subjectText.toString());
                    subjectText.clear();
                    if (obj.has("edittext")) {
                        subjectText.append(obj.getString("edittext"));
                    }
                    JSONArray att = obj.getJSONArray("groupChatBody");
                    if (obj.has("groupChatBody")) {
                        boolean z;
                        if (att == null || att.length() <= 0) {
                            z = false;
                        } else {
                            z = true;
                        }
                        this.mHasAttachment = z;
                    }
                } catch (JSONException e) {
                    MLog.e("ConversationListItem", "updateSubjectView occur JSONException");
                }
            }
            ResEx.setMarqueeText(this.mSubjectView, subjectText);
        } else {
            updateTextColor(this.mSubjectView, sRes.getConvItemErrorMsgTextColor());
            this.mSubjectView.setText(getResources().getString(R.string.message_status_undelivered));
        }
    }

    private final void updateDraftView(boolean isEditMode) {
        int unreadMsgSize = this.mConversation.getUnreadMessageCount();
        if (isCommonItem() && this.mConversation.hasDraft() && !this.mLastError && unreadMsgSize == 0) {
            updateVisibility(this.mCountAndDraftView, 0);
            this.mCountAndDraftView.setText(getResources().getString(R.string.has_draft_hint));
            return;
        }
        updateVisibility(this.mCountAndDraftView, 8);
    }

    private final void updatePinupView() {
        if (isCommonItem() && this.mConversation != null) {
            if (this.mConversation.getPriority() == 2) {
                this.mPinupView.setVisibility(0);
            } else if (this.mConversation.getPriority() == 0 || this.mConversation.getPriority() == 1) {
                this.mPinupView.setVisibility(8);
            }
        }
    }

    public final void unbind() {
        if (this.mHwCustConversationListItem != null) {
            this.mHwCustConversationListItem.release();
        }
        this.mConversation = null;
    }

    public long getItemId() {
        return this.mConversation.getThreadId();
    }

    public CheckBox getCheckBox() {
        if (this.mCheckBox == null) {
            this.mCheckBox = createCheckBox();
        }
        return this.mCheckBox;
    }

    private CheckBox createCheckBox() {
        ViewStub stub = (ViewStub) findViewById(R.id.stub_checkbox);
        if (stub == null) {
            return null;
        }
        return (CheckBox) stub.inflate();
    }

    public void setEditAble(boolean editAble) {
        boolean z = false;
        if (!isCommonItem()) {
            this.mStepInImg.setVisibility(editAble ? 8 : 0);
        }
        editAble &= isCommonItem();
        if (this.mCheckBox == null && editAble) {
            getCheckBox();
        }
        if (this.mCheckBox != null) {
            if (editAble) {
                this.mCheckBox.setVisibility(0);
                this.mStepInImg.setVisibility(8);
            } else {
                this.mCheckBox.setVisibility(8);
            }
            if (!(this.mRcsConversationListItem == null || this.mConversation == null || this.mConversation.getHwCust() == null)) {
                RcsConversationListItem rcsConversationListItem = this.mRcsConversationListItem;
                if (editAble || this.mHasError) {
                    z = true;
                } else if (!this.mConversation.getHwCust().hasUndeliveredMsg()) {
                    z = true;
                }
                rcsConversationListItem.showUndeliveredView(z);
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

    public boolean isEditAble() {
        boolean z = false;
        if (!isCommonItem()) {
            return false;
        }
        if (this.mCheckBox != null && this.mCheckBox.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    public void setChecked(boolean checked) {
        if (isCommonItem()) {
            if (this.mCheckBox == null && checked) {
                getCheckBox();
            }
            if (this.mCheckBox != null) {
                this.mCheckBox.setChecked(checked);
                refreshDrawableState();
            }
        }
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Contact.getChangeMoniter().removeListener(this);
    }

    protected int getContentResId() {
        return R.id.content;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MmsConfig.isExtraHugeEnabled()) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MessageUtils.dipToPx(this.mContext, 88.0f), Integer.MIN_VALUE);
        } else if (MmsConfig.isHugeEnabled()) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MessageUtils.dipToPx(this.mContext, 67.0f), Integer.MIN_VALUE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void adaptAttrForHugeFont() {
        if (MmsConfig.isExtraHugeEnabled()) {
            MessageUtils.setLayout(this.mContext, this, 0, 88);
            MessageUtils.setTextSize(this.mFromView, 21.5f, 2);
            MessageUtils.setTextSize(this.mCountAndDraftView, 18.85f, 2);
            MessageUtils.setTextSize(this.mSubjectView, 18.85f, 2);
            MessageUtils.setTextSize(this.mAttachmentView, 18.85f, 2);
            MessageUtils.setTextSize(this.mDateView, 18.85f, 2);
            requestLayout();
        } else if (MmsConfig.isHugeEnabled()) {
            MessageUtils.setLayout(this.mContext, this, 0, 67);
            MessageUtils.setTextSize(this.mSubjectView, 13.0f, 2);
            MessageUtils.setTextSize(this.mAttachmentView, 13.0f, 2);
            MessageUtils.setTextSize(this.mCountAndDraftView, 13.0f, 2);
            MessageUtils.setTextSize(this.mDateView, 13.0f, 2);
            requestLayout();
        }
    }

    public boolean isNotifactionsItem() {
        return this.mNumberType == 2;
    }

    public boolean isHwNotifactionsItem() {
        return this.mNumberType == 1;
    }

    public boolean isCommonItem() {
        return this.mNumberType == 0 || this.mNumberType == 3;
    }

    public static void cacheConversationViews(final Context context, int delay) {
        synchronized (mConversationListItems) {
            mConversationListItems.clear();
        }
        HwBackgroundLoader.getBackgroundHandler().postDelayed(new Runnable() {
            public void run() {
                LayoutInflater inflator = (LayoutInflater) new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)).getSystemService("layout_inflater");
                if (inflator != null) {
                    ConversationListItem.clearConvListItemCache();
                    for (int i = 0; i < 10; i++) {
                        View v = inflator.inflate(R.layout.conversation_list_item, null);
                        synchronized (ConversationListItem.mConversationListItems) {
                            ConversationListItem.mConversationListItems.add(v);
                        }
                    }
                }
            }
        }, (long) delay);
    }

    public static View getCachedConversationItem() {
        View view;
        synchronized (mConversationListItems) {
            view = mConversationListItems.size() > 0 ? (View) mConversationListItems.remove(0) : null;
        }
        return view;
    }

    public static void clearConvListItemCache() {
        synchronized (mConversationListItems) {
            mConversationListItems.clear();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Contact.addListener(this);
    }

    public boolean needSetBackground() {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mFromLayoutEx.resizeLayout(true);
    }
}
