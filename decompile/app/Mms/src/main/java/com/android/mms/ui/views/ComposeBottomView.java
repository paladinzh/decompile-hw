package com.android.mms.ui.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.telephony.HwTelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.views.RcsComposeBottomView;
import com.android.rcs.ui.views.RcsComposeBottomView.ICustComposeBottomViewCallback;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwComposeBottomEditView;
import com.huawei.mms.ui.HwComposeBottomEditView.ScrollableCallback;
import com.huawei.mms.ui.HwMultiSimSendButton;
import com.huawei.mms.ui.control.HwComposeBottomEditViewControl;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwDualCardNameHelper.HwCardNameChangedListener;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwTelephony;
import com.huawei.mms.util.HwTelephony.HwSimStateListener;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.ui.RcsAudioMessage;
import com.huawei.rcs.utils.RcsProfile;

public class ComposeBottomView implements OnClickListener, HwCardNameChangedListener {
    private LinearLayout mAttachmentEditorViewSuperLayout;
    private int mBottomMaxHeightPortrait;
    private ScrollView mBottomScroller;
    private boolean mBottomScrollerCanScroll = false;
    private int mCard1State = 0;
    private int mCard2State = 0;
    private View mComposeBottomGroup;
    private int mComposeLayoutMarginTop;
    private View mComposeMessageEditLayout;
    private HwComposeBottomEditViewControl mComposeMessageEditLayoutControl;
    private Context mContext;
    private LinearLayout mDuoquButtonMenu;
    private IComposeBottomViewHolder mHolder = null;
    private RcsComposeBottomView mHwCust;
    private int mIsCardStateValid = -1;
    private TextView mIsMmsTextView;
    private TextView mIsMmsTextViewMultiSim;
    private TextView mIsMmsTextViewSingleSim;
    private boolean mIsRcsMode = false;
    private LinearLayout mMmsMessageEditLayout;
    private View mMultiSimModelLayout;
    private int mMultiSimModelLayoutHeight;
    private RcsAudioMessage mRcsAudioMessage;
    private Button mRcsPickAudio;
    private HwMultiSimSendButton mSendButtonDual1;
    private HwMultiSimSendButton mSendButtonDual2;
    private TextView mSendButtonSms;
    private boolean mSendMessageEnable = false;
    private BroadcastReceiver mSimChangeReceiver = null;
    private int mSingleCardSubId = 0;
    private View mSingleSimModelLayout;
    private TextView mTextCounter;
    private TextView mTextCounterMultiSim;
    private TextView mTextCounterSingleSim;

    public interface IBottomHolder {
        View findViewById(int i);

        int getActionBarShowHeight();

        Resources getResources();

        boolean hasExceedsMmsLimit();

        boolean hideAttachmentsView();

        void hideKeyboard();

        void hidePanel();

        boolean isInNewMessageMode();

        void onMultiSimUpdateSendButton();

        void onSendButtonClick(int i);

        void onSimStateChanged(int i, int i2);

        void updateFullScreenButtonState(int i);
    }

    public interface IComposeBottomViewHolder extends IBottomHolder {
        int getAttachementViewHeight();

        boolean updateSendButtonStateSimple(TextView textView, boolean z, boolean z2);
    }

    private class CustComposeBottomViewCallback implements ICustComposeBottomViewCallback {
        private CustComposeBottomViewCallback() {
        }

        public void setSmsTextCountVisible(boolean isVisible) {
            if (ComposeBottomView.this.mTextCounter == null) {
                return;
            }
            if (isVisible) {
                ComposeBottomView.this.mTextCounter.setVisibility(0);
            } else {
                ComposeBottomView.this.mTextCounter.setVisibility(8);
            }
        }

        public void setMmsTextViewVisible(boolean isVisible) {
            if (ComposeBottomView.this.mIsMmsTextView == null) {
                return;
            }
            if (!isVisible) {
                ComposeBottomView.this.mIsMmsTextView.setVisibility(8);
            } else if (MmsConfig.getMmsEnabled()) {
                ComposeBottomView.this.mIsMmsTextView.setVisibility(0);
            }
        }
    }

    public ComposeBottomView(Context context) {
        this.mContext = context;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsComposeBottomView();
        }
        if (this.mHwCust != null) {
            this.mHwCust.setHwCustCallback(new CustComposeBottomViewCallback());
        }
        this.mComposeMessageEditLayoutControl = new HwComposeBottomEditViewControl();
    }

    private Resources getResources() {
        return this.mHolder.getResources();
    }

    public int getMultiSimModelLayoutHeight() {
        if (this.mMultiSimModelLayout == null || this.mMultiSimModelLayout.getVisibility() != 0) {
            return 0;
        }
        return this.mMultiSimModelLayoutHeight;
    }

    public void init(View parentView, IComposeBottomViewHolder callback) {
        if (parentView != null) {
            this.mHolder = callback;
            this.mComposeMessageEditLayout = parentView;
            this.mMultiSimModelLayout = parentView.findViewById(R.id.button_with_counter);
            this.mMultiSimModelLayoutHeight = ((int) getResources().getDimension(R.dimen.mms_dialpad_dial_button_height)) + ((int) getResources().getDimension(R.dimen.mms_multi_send_button_padding_bottom));
            this.mMmsMessageEditLayout = (LinearLayout) parentView.findViewById(R.id.mms_message_edit_layout);
            this.mSingleSimModelLayout = parentView.findViewById(R.id.button_singlesim_model_parent);
            this.mComposeLayoutMarginTop = (int) this.mContext.getResources().getDimension(R.dimen.mms_actionbar_margin_top);
            ((HwComposeBottomEditView) this.mComposeMessageEditLayout).setComposeLayoutMarginTop(this.mComposeLayoutMarginTop);
            this.mSendButtonSms = (TextView) parentView.findViewById(R.id.send_button_sms);
            this.mSingleSimModelLayout.setOnClickListener(this);
            setSendMessageButtonEnable(false);
            boolean canClickable = false;
            if (this.mRcsAudioMessage != null) {
                canClickable = this.mRcsAudioMessage.getClickStatus(false);
            }
            this.mSingleSimModelLayout.setClickable(canClickable);
            this.mSendButtonDual1 = (HwMultiSimSendButton) parentView.findViewById(R.id.send_button_dual_1);
            this.mSendButtonDual2 = (HwMultiSimSendButton) parentView.findViewById(R.id.send_button_dual_2);
            this.mSendButtonDual1.setOnClickListener(this);
            this.mSendButtonDual2.setOnClickListener(this);
            this.mSendButtonDual1.setContentDescription(this.mContext.getResources().getString(R.string.send_by_card1_hint));
            this.mSendButtonDual2.setContentDescription(this.mContext.getResources().getString(R.string.send_by_card2_hint));
            this.mSendButtonDual1.setLeftDrawables(R.drawable.message_send_card1_selector);
            this.mSendButtonDual2.setLeftDrawables(R.drawable.message_send_card2_selector);
            this.mTextCounterMultiSim = (TextView) parentView.findViewById(R.id.text_counter_multisim_model);
            this.mIsMmsTextViewMultiSim = (TextView) parentView.findViewById(R.id.isMms_multisim_model);
            this.mTextCounterSingleSim = (TextView) parentView.findViewById(R.id.text_counter);
            this.mIsMmsTextViewSingleSim = (TextView) parentView.findViewById(R.id.isMms);
            registerSimChangeMonitor();
            if (MessageUtils.isMultiSimEnabled()) {
                HwDualCardNameHelper.self().addCardNameChangedListener(this);
            }
            if (isMultiCardsValid()) {
                this.mTextCounterSingleSim.setVisibility(8);
                this.mIsMmsTextViewSingleSim.setVisibility(8);
                this.mSendButtonSms.setVisibility(8);
            }
            this.mAttachmentEditorViewSuperLayout = (LinearLayout) this.mHolder.findViewById(R.id.attachment_editor_view_super_layout);
            this.mRcsPickAudio = (Button) this.mHolder.findViewById(R.id.rcs_pick_audio);
            resetLayoutParams(true);
            View bottomPanel = this.mHolder.findViewById(R.id.bottom_panel);
            if (ResEx.self().isUseThemeBackground(this.mContext)) {
                bottomPanel.setBackgroundResource(R.drawable.csp_bottom_emui);
            }
            this.mBottomScroller = (ScrollView) this.mHolder.findViewById(R.id.attachment_editor_scroll_view);
            this.mComposeMessageEditLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (top < 0 || (top >= ComposeBottomView.this.mHolder.getActionBarShowHeight() + ComposeBottomView.this.mComposeLayoutMarginTop && oldBottom >= 0 && bottom >= 0)) {
                        boolean canScrollVertically = !ComposeBottomView.this.mBottomScroller.canScrollVertically(1) ? ComposeBottomView.this.mBottomScroller.canScrollVertically(-1) : true;
                        if (!ComposeBottomView.this.mBottomScrollerCanScroll || canScrollVertically) {
                            ComposeBottomView.this.mBottomScrollerCanScroll = canScrollVertically;
                            if (!(oldTop == top && oldBottom == bottom)) {
                                ComposeBottomView.this.resetBottomScrollerHeight(false);
                            }
                            return;
                        }
                        ComposeBottomView.this.mBottomScrollerCanScroll = false;
                        ComposeBottomView.this.resetBottomScrollerHeight(true);
                    }
                }
            });
            this.mDuoquButtonMenu = (LinearLayout) parentView.findViewById(R.id.duoqu_button_menu);
        }
    }

    public void setComposeButtomGroupView(View view) {
        this.mComposeBottomGroup = view;
    }

    private void resetLayoutParams(boolean updateButtonWidth) {
        if (updateButtonWidth) {
            int btnWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.mms_multi_btn_width);
            int btnHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.mms_dialpad_dial_button_height);
            HwMessageUtils.setBtnLayoutParam(this.mContext, this.mSendButtonDual1, btnWidth, btnHeight);
            HwMessageUtils.setBtnLayoutParam(this.mContext, this.mSendButtonDual2, btnWidth, btnHeight);
        }
        this.mBottomMaxHeightPortrait = getResources().getDimensionPixelOffset(R.dimen.mms_bottom_layout_max_height_portrait);
    }

    public void onActivityConfigurationChanged(Configuration newConfig) {
        boolean isMultiSimActive = true;
        if (!(1 == this.mCard1State && 1 == this.mCard2State)) {
            isMultiSimActive = false;
        }
        resetLayoutParams(isMultiSimActive);
    }

    public void resetBottomScrollerHeight(boolean needReset) {
        if (this.mHolder.isInNewMessageMode()) {
            MLog.v("MMS_SendMsgButton", "resetBottomScrollerHeight isInNewMessageMode, return!!");
        }
        if (this.mComposeBottomGroup == null || this.mComposeMessageEditLayout == null) {
            MLog.v("MMS_SendMsgButton", "resetBottomScrollerHeight mComposeBottomGroup or mComposeMessageEditLayout is null, return!!");
            return;
        }
        int actionbarHeight = this.mHolder.getActionBarShowHeight();
        this.mComposeMessageEditLayoutControl.setActionBarHeight(this.mComposeMessageEditLayout, actionbarHeight);
        int bottomGroupHeight = this.mComposeBottomGroup.getMeasuredHeight();
        this.mComposeMessageEditLayoutControl.setBottomGroupHeight(this.mComposeMessageEditLayout, bottomGroupHeight);
        this.mComposeMessageEditLayoutControl.setmAttchmentDraftViewHeight(this.mComposeMessageEditLayout, this.mHolder.getAttachementViewHeight());
        LayoutParams params = (LayoutParams) this.mComposeMessageEditLayout.getLayoutParams();
        if (needReset) {
            params.height = -2;
            this.mComposeMessageEditLayout.setLayoutParams(params);
            this.mComposeMessageEditLayout.requestLayout();
            return;
        }
        int editLayoutHeight = this.mComposeMessageEditLayout.getMeasuredHeight();
        int maxEditLayoutHeight = (bottomGroupHeight - actionbarHeight) - this.mComposeLayoutMarginTop;
        if (editLayoutHeight >= maxEditLayoutHeight) {
            params.height = maxEditLayoutHeight;
            this.mComposeMessageEditLayout.setLayoutParams(params);
            this.mComposeMessageEditLayout.requestLayout();
            params.height = -2;
            return;
        }
        int composeBottomLayoutPaddingTop = (int) getResources().getDimension(R.dimen.compose_bottom_layout_padding_top_bottom);
        int editViewMarginBottom = (int) getResources().getDimension(R.dimen.edit_view_margin_bottom);
        int minEditLayoutHeight = ((((int) getResources().getDimension(R.dimen.mms_edit_layout_min_height)) + ((int) getResources().getDimension(R.dimen.compose_bottom_layout_padding_bottom_new))) + composeBottomLayoutPaddingTop) + editViewMarginBottom;
        if (editLayoutHeight < minEditLayoutHeight) {
            params.height = minEditLayoutHeight;
            this.mComposeMessageEditLayout.setLayoutParams(params);
            this.mComposeMessageEditLayout.requestLayout();
            params.height = -2;
        }
    }

    public void onActivityDestroy() {
        unregisterSimChangeMoniter();
        if (MessageUtils.isMultiSimEnabled()) {
            HwDualCardNameHelper.self().removeCardNameChangedListener(this);
        }
    }

    public void updateSendButtonView(boolean visible, boolean isPreparedForSending) {
        boolean isVoWifi = !MessageUtils.isQcomPlatform() ? MessageUtils.isAirplanModeOn(this.mContext) ? HwTelephonyManager.getDefault().getImsDomain() == 1 : false : MessageUtils.isAirplanModeOn(this.mContext) ? MessageUtils.isWifiCallEnabled() : false;
        if (isVoWifi) {
            MLog.v("MMS_SendMsgButton", "Device is airplane mode on and it is registered vowifi");
        }
        boolean isMultiSimActive = (1 == this.mCard1State || isVoWifi) && 1 == this.mCard2State;
        if (visible) {
            if (1 != this.mIsCardStateValid) {
                updateIccCardState();
            }
            if (isMultiSimActive) {
                setSingleSimModelLayoutVisibility(false);
                setMultiSimModelLayoutVisibility(true);
                this.mTextCounter = this.mTextCounterMultiSim;
                this.mIsMmsTextView = this.mIsMmsTextViewMultiSim;
                String[] multiSimModel = new String[]{"", ""};
                for (int i = 0; i < 2; i++) {
                    multiSimModel[i] = HwDualCardNameHelper.self().readCardName(i);
                }
                this.mSendButtonDual1.setVisibility(isPreparedForSending ? 0 : 8);
                this.mSendButtonDual1.setEnabled(isPreparedForSending);
                this.mSendButtonDual1.setText(multiSimModel[0]);
                this.mSendButtonDual1.setClickable(isPreparedForSending);
                this.mSendButtonDual2.setVisibility(isPreparedForSending ? 0 : 8);
                this.mSendButtonDual2.setEnabled(isPreparedForSending);
                this.mSendButtonDual2.setText(multiSimModel[1]);
                this.mSendButtonDual2.setClickable(isPreparedForSending);
                this.mHolder.onMultiSimUpdateSendButton();
                this.mHolder.updateFullScreenButtonState(118);
            } else {
                boolean cardEnabled;
                setMultiSimModelLayoutVisibility(false);
                setSingleSimModelLayoutVisibility(true);
                this.mSendButtonSms.setVisibility(0);
                this.mTextCounter = this.mTextCounterSingleSim;
                this.mIsMmsTextView = this.mIsMmsTextViewSingleSim;
                if (MessageUtils.isMultiSimEnabled()) {
                    this.mSingleCardSubId = 1 == this.mCard2State ? 1 : 0;
                    cardEnabled = 1 == this.mCard1State || 1 == this.mCard2State;
                } else {
                    cardEnabled = 1 == this.mCard1State;
                }
                if (this.mHwCust != null) {
                    cardEnabled = this.mHolder.updateSendButtonStateSimple(this.mSendButtonSms, cardEnabled, isPreparedForSending);
                }
                boolean hasExceedsMmsLimit = !this.mIsRcsMode ? this.mHolder.hasExceedsMmsLimit() : false;
                boolean canClickable = (cardEnabled || isVoWifi) && isPreparedForSending && !hasExceedsMmsLimit;
                this.mSingleSimModelLayout.setClickable(this.mRcsAudioMessage.getClickStatus(canClickable));
                boolean z = (cardEnabled || isVoWifi) && isPreparedForSending && !hasExceedsMmsLimit;
                setSendMessageButtonEnable(z);
                if ((cardEnabled || isVoWifi) && isPreparedForSending && !hasExceedsMmsLimit) {
                    this.mHolder.updateFullScreenButtonState(130);
                } else {
                    this.mHolder.updateFullScreenButtonState(119);
                }
            }
            return;
        }
        setSingleSimModelLayoutVisibility(!isMultiSimActive);
        setMultiSimModelLayoutVisibility(false);
        this.mHolder.updateFullScreenButtonState(118);
        if (!isMultiSimActive) {
            this.mTextCounterSingleSim.setVisibility(8);
            this.mIsMmsTextViewSingleSim.setVisibility(8);
            this.mSendButtonSms.setVisibility(0);
            setSendMessageButtonEnable(false);
            canClickable = false;
            if (this.mRcsAudioMessage != null) {
                canClickable = this.mRcsAudioMessage.getClickStatus(false);
            }
            this.mSingleSimModelLayout.setClickable(canClickable);
        }
    }

    public void setSendButtonEnabled(int resId, boolean enabled) {
        if (R.id.send_button_sms == resId && this.mSendButtonSms.getVisibility() == 0) {
            setSendMessageButtonEnable(enabled);
        } else if (R.id.send_button_dual_1 == resId && this.mSendButtonDual1.getVisibility() == 0) {
            this.mSendButtonDual1.setEnabled(enabled);
        } else if (R.id.send_button_dual_2 == resId && this.mSendButtonDual2.getVisibility() == 0) {
            this.mSendButtonDual2.setEnabled(enabled);
        }
    }

    public void updateSmsTextCount(String textCount) {
        if (this.mSingleSimModelLayout.getVisibility() != 8 || this.mMultiSimModelLayout.getVisibility() != 8) {
            if (this.mIsMmsTextView != null) {
                this.mIsMmsTextView.setVisibility(8);
            }
            if (TextUtils.isEmpty(textCount)) {
                if (this.mTextCounter != null) {
                    this.mTextCounter.setVisibility(4);
                }
            } else if (this.mTextCounter != null) {
                this.mTextCounter.setVisibility(0);
                this.mTextCounter.setText(textCount);
            }
            if (this.mHwCust != null) {
                this.mHwCust.updateSmsTextCount();
            }
        }
    }

    public void updateMmsCapacitySize(String rate) {
        if (this.mSingleSimModelLayout.getVisibility() != 8 || this.mMultiSimModelLayout.getVisibility() != 8) {
            if (this.mTextCounter != null) {
                this.mTextCounter.setVisibility(8);
            }
            if (this.mIsMmsTextView != null && MmsConfig.getMmsEnabled()) {
                this.mIsMmsTextView.setVisibility(0);
                this.mIsMmsTextView.setText(rate);
            }
            if (this.mHwCust != null) {
                this.mHwCust.updateMmsCapacitySize();
            }
        }
    }

    public boolean isMultiCardsValid() {
        if (1 != this.mIsCardStateValid) {
            updateIccCardState();
        }
        if (1 == this.mCard1State && 1 == this.mCard2State) {
            return true;
        }
        return false;
    }

    public boolean isOneCardValid() {
        if (1 != this.mIsCardStateValid) {
            updateIccCardState();
        }
        if (1 == this.mCard1State || 1 == this.mCard2State) {
            return true;
        }
        return false;
    }

    public int getSingleCardSubId() {
        return this.mSingleCardSubId;
    }

    public void onClick(View v) {
        int subscription = 0;
        if (v == this.mSendButtonDual1 || v == this.mSendButtonDual2 || v == this.mSingleSimModelLayout) {
            if (v == this.mSendButtonDual1) {
                subscription = 0;
            } else if (v == this.mSendButtonDual2) {
                subscription = 1;
            } else if (v == this.mSingleSimModelLayout) {
                subscription = this.mSingleCardSubId;
            }
            boolean switchresult = false;
            if (this.mRcsAudioMessage != null) {
                switchresult = this.mRcsAudioMessage.switchCurrentView();
            }
            if (switchresult) {
                int resId = R.drawable.ic_send_message_disable;
                if (3 == this.mRcsAudioMessage.getCurrentView()) {
                    resId = this.mIsRcsMode ? R.drawable.ic_send_message_rcs : R.drawable.ic_send_message;
                }
                this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(this.mContext, this.mRcsAudioMessage.getViewBackGroud(resId)));
                if (this.mRcsAudioMessage.getCurrentView() == 2) {
                    this.mAttachmentEditorViewSuperLayout.setVisibility(8);
                    this.mRcsPickAudio.setVisibility(0);
                    this.mHolder.hideAttachmentsView();
                    this.mHolder.hideKeyboard();
                    this.mHolder.hidePanel();
                } else {
                    this.mAttachmentEditorViewSuperLayout.setVisibility(0);
                    this.mRcsPickAudio.setVisibility(8);
                }
                return;
            }
            this.mHolder.onSendButtonClick(subscription);
        }
    }

    public void switchToAudioView() {
        boolean switchresult = false;
        if (this.mRcsAudioMessage != null && this.mRcsAudioMessage.getCurrentView() == 2) {
            switchresult = this.mRcsAudioMessage.switchCurrentView();
        }
        if (switchresult) {
            this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(this.mContext, this.mRcsAudioMessage.getViewBackGroud(R.drawable.ic_send_message_disable)));
            if (this.mRcsAudioMessage.getCurrentView() == 1) {
                this.mAttachmentEditorViewSuperLayout.setVisibility(0);
                this.mRcsPickAudio.setVisibility(8);
            }
        }
    }

    public void updateshowEditor() {
        this.mAttachmentEditorViewSuperLayout.setVisibility(0);
        this.mRcsPickAudio.setVisibility(8);
    }

    private void updateIccCardState() {
        if (MessageUtils.isMultiSimEnabled()) {
            this.mCard1State = MessageUtils.getIccCardStatus(0);
            this.mCard2State = MessageUtils.getIccCardStatus(1);
        } else {
            this.mCard1State = MessageUtils.getIccCardStatus();
        }
        this.mIsCardStateValid = 1;
    }

    private void registerSimChangeMonitor() {
        if (this.mSimChangeReceiver == null) {
            this.mSimChangeReceiver = HwTelephony.registeSimChange(this.mContext, new HwSimStateListener() {
                public void onSimStateChanged(int simState) {
                    HwDualCardNameHelper.self().clearAndResetCurrentCardName(-1);
                    ComposeBottomView.this.mIsCardStateValid = 0;
                    ComposeBottomView.this.updateIccCardState();
                    ComposeBottomView.this.mHolder.onSimStateChanged(ComposeBottomView.this.mCard1State, ComposeBottomView.this.mCard2State);
                }

                public void onSimStateChanged(int simState, int subId) {
                    HwDualCardNameHelper.self().clearAndResetCurrentCardName(subId);
                    ComposeBottomView.this.mIsCardStateValid = 0;
                    ComposeBottomView.this.updateIccCardState();
                    ComposeBottomView.this.mHolder.onSimStateChanged(ComposeBottomView.this.mCard1State, ComposeBottomView.this.mCard2State);
                }
            });
        }
    }

    private void unregisterSimChangeMoniter() {
        if (this.mSimChangeReceiver != null) {
            this.mContext.unregisterReceiver(this.mSimChangeReceiver);
            this.mSimChangeReceiver = null;
        }
    }

    public void setRecommendMessageButton(int defaultCard) {
        if (defaultCard == 0) {
            this.mSendButtonDual1.setCurSimIndicatorVisibility(0);
            this.mSendButtonDual2.setCurSimIndicatorVisibility(8);
        } else if (defaultCard == 1) {
            this.mSendButtonDual2.setCurSimIndicatorVisibility(0);
            this.mSendButtonDual1.setCurSimIndicatorVisibility(8);
        } else {
            this.mSendButtonDual2.setCurSimIndicatorVisibility(8);
            this.mSendButtonDual1.setCurSimIndicatorVisibility(8);
        }
    }

    public void onCardNameChanged(String[] cardNames) {
        updateIccCardState();
        this.mHolder.onSimStateChanged(this.mCard1State, this.mCard2State);
    }

    private void setSendMessageButtonEnable(boolean enable) {
        RcsAudioMessage rcsAudioMessage;
        if (!enable) {
            if (this.mRcsPickAudio == null || this.mRcsPickAudio.getVisibility() != 0) {
                boolean isRcsConnected = RcsProfile.isRcsServiceEnabledAndUserLogin();
                if (this.mIsRcsMode) {
                    if (isRcsConnected) {
                        rcsAudioMessage = this.mRcsAudioMessage;
                        RcsAudioMessage.setCurrentView(1);
                    } else {
                        rcsAudioMessage = this.mRcsAudioMessage;
                        RcsAudioMessage.setCurrentView(3);
                    }
                }
            } else {
                rcsAudioMessage = this.mRcsAudioMessage;
                RcsAudioMessage.setCurrentView(2);
            }
            setSendButtonResource();
        } else if (this.mRcsPickAudio == null || this.mRcsPickAudio.getVisibility() != 0) {
            rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(3);
            this.mSendMessageEnable = true;
            this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(this.mContext, this.mIsRcsMode ? R.drawable.ic_send_message_rcs : R.drawable.ic_send_message));
            this.mSendButtonSms.setContentDescription(this.mContext.getString(R.string.send));
        } else {
            rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(2);
            setSendButtonResource();
        }
        this.mSendButtonSms.setEnabled(enable);
    }

    private void setSendButtonResource() {
        this.mSendMessageEnable = false;
        int resId = R.drawable.ic_send_message_disable;
        if (this.mRcsAudioMessage != null) {
            resId = this.mRcsAudioMessage.getViewBackGroud(R.drawable.ic_send_message_disable);
        }
        this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(this.mContext, resId));
        this.mSendButtonSms.setContentDescription(this.mContext.getString(R.string.send_button_disable));
    }

    public void setEditorViewSuperLayoutPaddingValues(boolean chooseButtonVisible) {
        int paddingStart;
        if (chooseButtonVisible) {
            paddingStart = getResources().getDimensionPixelSize(R.dimen.edit_view_padding_start_with_choose_btton);
        } else {
            paddingStart = getResources().getDimensionPixelSize(R.dimen.edit_view_padding_start_without_choose_btton);
        }
        this.mAttachmentEditorViewSuperLayout.setPaddingRelative(paddingStart, 0, getResources().getDimensionPixelSize(R.dimen.edit_view_padding_end), 0);
    }

    public void setCurrentMessageMode(boolean isRcsMode) {
        this.mIsRcsMode = isRcsMode;
    }

    public boolean getCurrentMessageMode() {
        return this.mIsRcsMode;
    }

    public void setRcsAudioMessage(RcsAudioMessage rcsAudioMessage) {
        this.mRcsAudioMessage = rcsAudioMessage;
        this.mRcsAudioMessage.setPickAudioButton(this.mRcsPickAudio);
    }

    private void setMultiSimModelLayoutVisibility(boolean visible) {
        this.mMultiSimModelLayout.setVisibility(visible ? 0 : 8);
        if (this.mDuoquButtonMenu != null) {
            int i;
            LayoutParams layoutParams = (LayoutParams) this.mDuoquButtonMenu.getLayoutParams();
            Resources resources = getResources();
            if (visible) {
                i = R.dimen.duoqu_menu_button_margin_bottom_multi_sim_mode;
            } else {
                i = R.dimen.duoqu_menu_button_margin_bottom_single_sim_mode;
            }
            layoutParams.bottomMargin = resources.getDimensionPixelSize(i);
        }
    }

    private void setSingleSimModelLayoutVisibility(boolean visible) {
        int i = 0;
        this.mSingleSimModelLayout.setVisibility(visible ? 0 : 8);
        if (this.mMmsMessageEditLayout != null) {
            LinearLayout linearLayout = this.mMmsMessageEditLayout;
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.emui_5_screen_padding);
            int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.compose_bottom_layout_padding_top_bottom);
            if (!visible) {
                i = getResources().getDimensionPixelSize(R.dimen.emui_5_screen_padding);
            }
            linearLayout.setPaddingRelative(dimensionPixelSize, dimensionPixelSize2, i, getResources().getDimensionPixelSize(R.dimen.compose_bottom_layout_padding_bottom_new));
        }
    }

    public boolean isMessageInRcsMode() {
        return this.mIsRcsMode;
    }

    public boolean isSendMessageEnable() {
        return this.mSendMessageEnable;
    }

    public void setScrollableCallback(ScrollableCallback scrollCallback) {
        this.mComposeMessageEditLayoutControl.setScrollableCallback(this.mComposeMessageEditLayout, scrollCallback);
    }
}
