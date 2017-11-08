package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Sms;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsListItemHolder;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsUIHolder;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.model.SmilHelper;
import com.android.mms.transaction.MmsConnectionManager;
import com.android.mms.transaction.SmsReceiver;
import com.android.mms.transaction.TransactionService;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageItem.OnMmsTextLoadCallBack;
import com.android.mms.ui.MessageItem.PduLoadedCallback;
import com.android.mms.ui.views.MmsPopView.MmsPopViewCallback;
import com.android.mms.ui.views.MmsViewSuperLayout;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.ItemLayoutCallback;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.LinkerTextTransfer;
import com.android.rcs.ui.RcsMessageListItem;
import com.android.rcs.ui.RcsMessageListItem.IHwCustMessageListItemCallback;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.IListItem;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.DelaySendManager.UpdateCallback;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.utils.RcsTransaction;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

public class MessageListItem extends AvatarWidget implements OnLongClickListener, CheckableView, OnClickListener, SpandTouchMonitor, ISmartSmsListItemHolder, IListItem {
    private static float MULTI_CHOICE_MMS_ALPHA = 0.5f;
    private static float MULTI_RESET_CHOICE_MMS_ALPHA = ContentUtil.FONT_SIZE_NORMAL;
    private static boolean mIsMessageItemCancled = false;
    private ImageButton mAudioIcon;
    public SpandTextView mBodyTextView;
    private TextView mBottomButton;
    private LinearLayout mBottomButtonLayout;
    private ImageView mBottomDivider;
    private ViewStub mBottomStubView;
    private int mButtonClickCount;
    private SpannableStringBuilder mCachedLinkingMsg;
    private UpdateCallback mCancelUpdate;
    private TextView mCancleCountView;
    private CheckBox mCheckBox;
    private ArrayList<MmsClickListener> mClickListenerList = new ArrayList();
    private HashMap<View, OnClickListener> mClickWidgets = new HashMap();
    private CryptoMessageListItem mCryptoMessageListItem = new CryptoMessageListItem();
    protected TextView mDateView;
    private ImageView mDeliveredIndicator;
    private TextView mDownloadingView;
    private TextView mExpireTime;
    private ImageView mFailedIndicator;
    private boolean mFlingState = false;
    private float mFontScale = ContentUtil.FONT_SIZE_NORMAL;
    private TextView mGroupFaildMsgStatus;
    private TextView mGroupSendStatusView;
    private Handler mHandler;
    private HwCustMessageListItem mHwCustMessageListItem;
    private boolean mIsDarkThemeOn = false;
    private boolean mIsDelayMsg = false;
    private boolean mIsMultiChoice = false;
    boolean mIsMultiSimActive = false;
    private ItemLayoutCallback<MessageItem> mItemLayoutCallback;
    private ItemTouchListener mItemTouchListener = new ItemTouchListener();
    private ListView mListView;
    private DialogInterface.OnClickListener mListenerDoCallCarrier;
    private DialogInterface.OnClickListener mListenerDoGotoSmscEditor;
    private DialogInterface.OnClickListener mListenerDoResend;
    private ImageView mLockedIndicator;
    private HashMap<View, OnLongClickListener> mLongClickWidgets = new HashMap();
    public View mMessageBlock;
    public View mMessageBlockSuper;
    private MessageItem mMessageItem;
    private MessageResendListener mMessageResendListener;
    private TextView mMessageStatus;
    private HashMap<View, MmsClickListener> mMmsClickWidgets = new HashMap();
    private LinearLayout mMmsMsgLayout;
    private MmsPopViewCallback mMmsPopViewClickCallback = new MmsPopViewCallback() {
        public boolean onDoubleClick() {
            if (!DelaySendManager.getInst().isDelayMsg(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients)) {
                return false;
            }
            DelaySendManager.getInst().setDelayMsgCanceled(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients);
            MessageListItem.this.sendMessage(MessageListItem.this.mMessageItem, 1000105);
            return true;
        }

        public boolean isDelayMsg() {
            return MessageListItem.this.mIsDelayMsg;
        }

        public boolean isInEditMode() {
            return MessageListItem.this.mIsMultiChoice;
        }
    };
    private boolean mMultiRecipients;
    protected boolean mNeedHideUnderPopView = false;
    private boolean mNeedShowTimePhase = false;
    private int mNextYear = -1;
    private OnMmsTextLoadCallBack mOnMmsTextLoadCallBack = new OnMmsTextLoadCallBack() {
        public void onCallBack() {
            MessageListItem.this.updateMessageBlockVisibility();
        }
    };
    private OptimizedResendDialog mOptimizedResendDialog;
    private int mPosition;
    private RcsMessageListItem mRcsMessageListItem;
    private AlertDialog mResendDialog;
    boolean mSameItem = false;
    private int mShowTimeStampFormat;
    private MmsViewSuperLayout mSlideshowModelView;
    private SmartSmsBubbleManager mSmartSmsBubble = null;
    private SpandTextView mSubjectTextView;
    private ImageView mSubscriptionNetworkTypeView;
    private RelativeLayout mSuperLayout;
    TextSpanLinkingCache mTextSpanLinkingCache;
    private TextView mTextTimePhase;
    private TextView mTextViewYear;
    protected boolean mUserChooosed = false;

    private class BottomClickListener implements IMmsClickListener {
        private BottomClickListener() {
        }

        public void onDoubleClick(View view) {
            if (DelaySendManager.getInst().isDelayMsg(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients)) {
                DelaySendManager.getInst().setDelayMsgCanceled(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients);
                MessageListItem.this.sendMessage(MessageListItem.this.mMessageItem, 1000105);
            }
        }

        public void onSingleClick(View view) {
            MessageListItem.this.sendMessage(MessageListItem.this.mMessageItem, 1000104);
        }
    }

    private class ItemTouchListener implements IMmsClickListener {
        private ItemTouchListener() {
        }

        public void onDoubleClick(View view) {
            if (DelaySendManager.getInst().isDelayMsg(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients)) {
                DelaySendManager.getInst().setDelayMsgCanceled(MessageListItem.this.mMessageItem.getCancelId(), MessageListItem.this.mMessageItem.mType, MessageListItem.this.mMessageItem.mIsMultiRecipients);
                MessageListItem.this.sendMessage(MessageListItem.this.mMessageItem, 1000105);
            }
        }

        public void onSingleClick(View view) {
            if (!MessageListItem.this.mIsDelayMsg) {
                MessageListItem.this.processSingleClick(view);
            }
        }
    }

    private class MessageListItemCallback implements IHwCustMessageListItemCallback {
        private MessageListItemCallback() {
        }

        public void setBodyTextViewVisibility(int visibility) {
            if (MessageListItem.this.mBodyTextView != null) {
                MessageListItem.this.mBodyTextView.setVisibility(visibility);
            }
        }

        public boolean isDelayMessage() {
            return MessageListItem.this.mIsDelayMsg;
        }

        public void setDelayMessageStatus(boolean isDelay) {
            MessageListItem.this.mIsDelayMsg = isDelay;
            MessageListItem.this.mBodyTextView.setIsClickIntercepted(MessageListItem.this.mIsDelayMsg);
        }
    }

    private final class MessageResendListener implements OnClickListener {
        private boolean mIsGroupMessage;

        private MessageResendListener() {
            this.mIsGroupMessage = false;
        }

        private void setGroupMessage(boolean isGroupMessage) {
            this.mIsGroupMessage = isGroupMessage;
        }

        public void onClick(View v) {
            if ((MessageListItem.this.mContext instanceof ComposeMessageActivity) || (HwMessageUtils.isSplitOn() && (MessageListItem.this.mContext instanceof ConversationList))) {
                StatisticalHelper.incrementReportCount(MessageListItem.this.mContext, 2239);
                if (!MessageUtils.isNeedShowToastWhenNetIsNotAvailable(MessageListItem.this.mContext, MessageListItem.this.mMessageItem)) {
                    if (MessageListItem.this.mRcsMessageListItem != null && MessageListItem.this.mMessageItem.isRcsChat()) {
                        MessageListItem.this.mRcsMessageListItem.onResendClick(MessageListItem.this.mMessageItem, MessageListItem.this.mFailedIndicator);
                        MLog.d("MSG_APP_MessageListItem", "resend rcs chat msg");
                    } else if (MessageListItem.this.mResendDialog == null || !MessageListItem.this.mResendDialog.isShowing()) {
                        MessageListItem.this.mOptimizedResendDialog = new OptimizedResendDialog(MessageListItem.this.mContext, MessageListItem.this.mMessageItem);
                        if (this.mIsGroupMessage || !MessageListItem.this.mOptimizedResendDialog.isNeedUserRepairSelf()) {
                            MessageListItem.this.mResendDialog = new Builder(MessageListItem.this.mContext).setCancelable(true).setTitle(MessageListItem.this.getResources().getString(R.string.mms_resend_content)).setPositiveButton(R.string.resend, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MessageListItem.this.setViewClickListener(MessageListItem.this.mFailedIndicator, null);
                                    MessageListItem.this.mFailedIndicator.setVisibility(8);
                                    if (MessageResendListener.this.mIsGroupMessage) {
                                        MessageListItem.this.resendAllFailedMsgInGroupMessageItem();
                                    } else if (MessageListItem.this.mContext instanceof ComposeMessageActivity) {
                                        ((ComposeMessageActivity) MessageListItem.this.mContext).reSend(MessageListItem.this.mMessageItem);
                                    } else if (MessageListItem.this.mContext instanceof ConversationList) {
                                        ((ConversationList) MessageListItem.this.mContext).reSend(MessageListItem.this.mMessageItem);
                                    }
                                    StatisticalHelper.incrementReportCount(MessageListItem.this.mContext, 2240);
                                }
                            }).setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
                        } else {
                            MessageListItem.this.mListenerDoCallCarrier = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MessageListItem.this.setViewClickListener(MessageListItem.this.mFailedIndicator, null);
                                    MessageListItem.this.mFailedIndicator.setVisibility(8);
                                    MessageListItem.this.mOptimizedResendDialog.callCarrier();
                                    StatisticalHelper.incrementReportCount(MessageListItem.this.mContext, 2242);
                                }
                            };
                            MessageListItem.this.mListenerDoResend = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MLog.d("MSG_APP_MessageListItem", "do resend");
                                    MessageListItem.this.setViewClickListener(MessageListItem.this.mFailedIndicator, null);
                                    MessageListItem.this.mFailedIndicator.setVisibility(8);
                                    if (MessageListItem.this.mContext instanceof ComposeMessageActivity) {
                                        ((ComposeMessageActivity) MessageListItem.this.mContext).reSend(MessageListItem.this.mMessageItem);
                                    } else if (MessageListItem.this.mContext instanceof ConversationList) {
                                        ((ConversationList) MessageListItem.this.mContext).reSend(MessageListItem.this.mMessageItem);
                                    }
                                }
                            };
                            MessageListItem.this.mListenerDoGotoSmscEditor = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MessageListItem.this.setViewClickListener(MessageListItem.this.mFailedIndicator, null);
                                    MessageListItem.this.mFailedIndicator.setVisibility(8);
                                    MessageListItem.this.mOptimizedResendDialog.goHwSmscEditorActivity();
                                    StatisticalHelper.incrementReportCount(MessageListItem.this.mContext, 2241);
                                }
                            };
                            MessageListItem.this.mResendDialog = MessageListItem.this.mOptimizedResendDialog.getOptResendDailog(MessageListItem.this.mListenerDoCallCarrier, MessageListItem.this.mListenerDoResend, MessageListItem.this.mListenerDoGotoSmscEditor);
                        }
                        MessageListItem.this.mResendDialog.show();
                    } else {
                        MLog.d("MSG_APP_MessageListItem", "mResendDialog is showing return");
                    }
                }
            }
        }
    }

    public RcsMessageListItem getRcsMessageListItem() {
        return this.mRcsMessageListItem;
    }

    public void setCacheLinkingMessage(SpannableStringBuilder message) {
        this.mCachedLinkingMsg = message;
    }

    public void setItemLayoutCallback(ItemLayoutCallback<MessageItem> callBack) {
        this.mItemLayoutCallback = callBack;
    }

    public MessageListItem(Context context) {
        super(context);
        this.mHwCustMessageListItem = (HwCustMessageListItem) HwCustUtils.createObj(HwCustMessageListItem.class, new Object[]{context});
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsMessageListItem = new RcsMessageListItem(context);
        }
        if (this.mRcsMessageListItem != null) {
            this.mRcsMessageListItem.setHwCustCallback(new MessageListItemCallback());
        }
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHwCustMessageListItem = (HwCustMessageListItem) HwCustUtils.createObj(HwCustMessageListItem.class, new Object[]{context});
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsMessageListItem = new RcsMessageListItem(context);
        }
        if (this.mRcsMessageListItem != null) {
            this.mRcsMessageListItem.setHwCustCallback(new MessageListItemCallback());
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSubjectTextView = (SpandTextView) findViewById(R.id.subject_view);
        this.mSubjectTextView.setSpandTouchMonitor(this);
        this.mBodyTextView = (SpandTextView) findViewById(R.id.text_view);
        this.mBodyTextView.setSpandTouchMonitor(this);
        this.mCheckBox = (CheckBox) findViewById(R.id.select);
        this.mDateView = (TextView) findViewById(R.id.date_view);
        this.mLockedIndicator = (ImageView) findViewById(R.id.locked_indicator);
        this.mDeliveredIndicator = (ImageView) findViewById(R.id.delivered_indicator);
        this.mMessageStatus = (TextView) findViewById(R.id.status);
        this.mExpireTime = (TextView) findViewById(R.id.expireTime);
        this.mSuperLayout = (RelativeLayout) findViewById(R.id.mms_layout_view_super_parent);
        this.mMessageBlock = findViewById(R.id.message_block);
        this.mAudioIcon = (ImageButton) findViewById(R.id.audio_read_icon);
        this.mMessageBlockSuper = findViewById(R.id.message_block_super);
        this.mSubscriptionNetworkTypeView = (ImageView) findViewById(R.id.subscriptionnetworktype);
        this.mGroupFaildMsgStatus = (TextView) findViewById(R.id.failed_status_num);
        this.mFailedIndicator = (ImageView) findViewById(R.id.failed_indicator);
        this.mGroupSendStatusView = (TextView) findViewById(R.id.group_num_status);
        this.mCancleCountView = (TextView) findViewById(R.id.cancle_status_timer);
        this.mBottomStubView = (ViewStub) findViewById(R.id.mms_bottom_view_stub);
        this.mDownloadingView = (TextView) findViewById(R.id.downloading_view);
        this.mMmsMsgLayout = (LinearLayout) findViewById(R.id.mms_layout_msg);
        this.mTextViewYear = (TextView) findViewById(R.id.textview_year);
        this.mTextTimePhase = (TextView) findViewById(R.id.time_phase);
        this.mSlideshowModelView = (MmsViewSuperLayout) findViewById(R.id.slide_show_mode_view);
    }

    public void initTextColor(boolean incoming) {
        Resources res = getContext().getResources();
        int mms_sub_text_color = res.getColor(R.color.text_color_black_sub_1);
        int text_color_black = res.getColor(R.drawable.text_color_black);
        int text_color_white = res.getColor(R.color.text_color_white);
        int incoming_msg_text_color = res.getColor(R.color.incoming_msg_text_color);
        int label_downloading = res.getColor(R.color.label_downloading);
        this.mDateView.setTextColor(mms_sub_text_color);
        this.mMessageStatus.setTextColor(mms_sub_text_color);
        if (incoming) {
            this.mSubjectTextView.setTextColor(text_color_black);
            if (!this.mIsDarkThemeOn) {
                this.mBodyTextView.setTextColor(text_color_black);
            }
            this.mBodyTextView.setLinkTextColor(incoming_msg_text_color);
            this.mDownloadingView.setTextColor(label_downloading);
            return;
        }
        this.mGroupSendStatusView.setTextColor(mms_sub_text_color);
        this.mBodyTextView.setLinkTextColor(text_color_white);
    }

    public void bind(MessageItem msgItem, boolean convHasMultiRecipients, int position, boolean isMultiSimActive, String address) {
        if (msgItem != null) {
            this.mIsMultiSimActive = isMultiSimActive;
            boolean z = this.mMessageItem != null && this.mMessageItem.mMsgId == msgItem.mMsgId;
            this.mSameItem = z;
            if (this.mRcsMessageListItem != null) {
                this.mSameItem = this.mRcsMessageListItem.isSameItem(this.mMessageItem, msgItem, this.mSameItem);
            }
            this.mMessageItem = msgItem;
            this.mMessageItem.setOnMmsTextLoadCallBack(this.mOnMmsTextLoadCallBack);
            this.mPosition = position;
            this.mMultiRecipients = convHasMultiRecipients;
            if (this.mItemType == 8) {
                updateMyAvatarIcon();
            } else if (this.mMessageItem.mAddress != null) {
                updateAvatarIcon(this.mMessageItem.mAddress);
            } else if (address != null) {
                updateAvatarIcon(address);
            } else {
                updateAvatarIcon(null, false);
            }
            if (this.mRcsMessageListItem != null) {
                this.mRcsMessageListItem.bindInCust(this.mMessageItem, this.mMessageBlock);
            }
            setLongClickable(false);
            setClickable(false);
            LayoutParams superLayoutParams = (LayoutParams) this.mSuperLayout.getLayoutParams();
            if (isNeedShowTimePhase()) {
                this.mTextTimePhase.setVisibility(0);
                this.mTextTimePhase.setText(buildTime(this.mMessageItem.mDate, true));
                superLayoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_item_margin_top3);
            } else {
                this.mTextTimePhase.setVisibility(8);
                superLayoutParams.topMargin = 0;
            }
            this.mSuperLayout.setLayoutParams(superLayoutParams);
            switch (this.mMessageItem.mMessageType) {
                case 130:
                    bindNotifInd(isMultiSimActive);
                    setItemClickListener();
                    bindMsgBodyAfter();
                    break;
                default:
                    int showBubbleMode = getShowBubbleMode();
                    if (showBubbleMode != 0) {
                        setLockedIconVisibility(msgItem.mLocked);
                        bindRichBubbleView(showBubbleMode);
                        break;
                    }
                    bindCommonMessage(this.mSameItem, this.mIsMultiSimActive);
                    setItemClickListener();
                    bindMsgBodyAfter();
                    if (this.mSmartSmsBubble != null) {
                        this.mSmartSmsBubble.hideBubbleView();
                        break;
                    }
                    break;
            }
            updateMessageBlockVisibility();
            updateMessageBlockSuperMagin();
            this.mCryptoMessageListItem.setMsgBodyClickListener(getContext(), this.mBodyTextView, this);
            msgItem.registerListItem(this);
        }
    }

    private void bindHeadTimeArea() {
        if (!this.mSameItem) {
            this.mDateView.setText(buildTime(this.mMessageItem.mDate));
        }
        if (MessageUtils.isMultiSimEnabled()) {
            setSubscriptionNetworkTypeIcon(this.mMessageItem, this.mIsMultiSimActive);
        }
    }

    private void bindMsgBodyAfter() {
        if (!this.mMessageItem.isRcsChat()) {
            this.mTextSpanLinkingCache.updateTextSpanable(this);
        }
        if (SmilHelper.getOctStream()) {
            Toast.makeText(this.mContext, this.mContext.getString(R.string.unsupported_media_format_Toast, new Object[]{"application/oct-stream"}), 0).show();
            SmilHelper.setOctStream(false);
        }
        setChatBodyMaxWidth(this.mMessageItem);
        setSubjectAndBodyLines();
        setMsgStatus(this.mMessageItem);
    }

    public void unbind() {
        this.mBodyTextView.setVisibility(0);
        if (this.mBottomButtonLayout != null) {
            setViewLongClickListener(this.mBottomButtonLayout, null);
        }
        for (MmsClickListener clickListener : this.mClickListenerList) {
            clickListener.removeClickListener();
        }
    }

    public MessageItem getMessageItem() {
        return this.mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        this.mHandler = handler;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void bindNotifInd(boolean isMultiSimActive) {
        boolean autoDownload;
        this.mDateView.setText(buildTime(this.mMessageItem.mDate));
        this.mBodyTextView.setText("");
        setViewVisibility(this.mMmsMsgLayout, 8);
        setExpireTimeVisibility();
        this.mMessageItem.setOnPduLoaded(new PduLoadedCallback() {
            public void onPduLoaded(MessageItem messageItem) {
                MessageListItem.this.setExpireTimeVisibility();
            }
        });
        setSubscriptionNetworkTypeIcon(this.mMessageItem, isMultiSimActive);
        DownloadManager downloadManager = DownloadManager.getInstance();
        int status = downloadManager.getStateWithTimeCheck(this.mMessageItem.mMessageUri);
        if (MessageUtils.isMultiSimEnabled()) {
            autoDownload = downloadManager.isAuto(this.mMessageItem.mSubId);
        } else {
            autoDownload = downloadManager.isAuto();
        }
        switch (status) {
            case 0:
                boolean dataSuspended = MmsApp.getDefaultTelephonyManager().getDataState() == 3;
                if (autoDownload) {
                    if (dataSuspended) {
                    }
                }
                break;
            case 129:
            case 136:
                setDownloadButtonClickListener(true);
                break;
            default:
                if (!this.mIsMultiChoice) {
                    setLongClickable(true);
                }
                setDownloadButtonClickListener(false);
                break;
        }
        setLockedIconVisibility(this.mMessageItem.mLocked);
    }

    private void saveMmsDownloadMode(String uri, MmsConnectionManager mmsConnectionManager, boolean downloading, boolean manualDownloadMode) {
        if (mmsConnectionManager == null) {
            if (downloading) {
                manualDownloadMode = false;
            } else {
                manualDownloadMode = true;
            }
            MessageListAdapter.saveConnectionManagerToMap(uri, false, downloading, manualDownloadMode, null);
        }
    }

    private void saveMmsDownloadingStatus(String uri, boolean isUserStop, boolean downloading, boolean manualDownloadMode) {
        MessageListAdapter.saveConnectionManagerToMap(uri, isUserStop, downloading, manualDownloadMode, MessageListAdapter.getMmsTransactionCleintFromMap(uri));
    }

    private void setMmsDownloadingView(String uri) {
        int i;
        boolean isDownloading = MessageListAdapter.getDownloadingStatusFromMap(uri);
        if (MmsConfig.isEnableCancelAutoRetrieve() || MessageListAdapter.getManualDownloadFromMap(uri)) {
            setBottomViewVisible(2);
            if (this.mSlideshowModelView != null) {
                this.mSlideshowModelView.removeAllViews();
                this.mSlideshowModelView.hide();
            }
            if (isDownloading) {
                this.mBottomButton.setText(R.string.cancel_download);
            }
        } else {
            setBottomViewInvisible(2);
        }
        if (isDownloading && this.mSlideshowModelView != null) {
            this.mSlideshowModelView.hide();
        }
        TextView textView = this.mDownloadingView;
        if (isDownloading) {
            i = 0;
        } else {
            i = 8;
        }
        textView.setVisibility(i);
    }

    private void setDownloadButtonClickListener(boolean downloading) {
        String uri = this.mMessageItem.mMessageUri.toString();
        saveMmsDownloadMode(uri, MessageListAdapter.getConnectionManagerFromMap(uri), downloading, false);
        setMmsDownloadingView(uri);
        this.mBodyTextView.setVisibility(8);
        if (this.mBottomButtonLayout != null) {
        }
    }

    private String buildTimestampLine(String timestamp) {
        if (!this.mMultiRecipients || this.mMessageItem.isMe() || TextUtils.isEmpty(this.mMessageItem.mContact)) {
            return timestamp;
        }
        return this.mContext.getString(R.string.message_timestamp_format, new Object[]{this.mMessageItem.mContact, timestamp});
    }

    private void bindCommonMessage(boolean sameItem, final boolean isMultiSimActive) {
        Object message = null;
        if (!this.mMessageItem.isRcsChat()) {
            message = this.mTextSpanLinkingCache.getCacheByKey(MessageListAdapter.getKey(this.mMessageItem.mType, this.mMessageItem.mMsgId));
        }
        if (TextUtils.isEmpty(message)) {
            setCacheLinkingMessage(null);
        } else {
            setCacheLinkingMessage(message);
        }
        checkDelayMsg();
        if (this.mDownloadingView != null && this.mDownloadingView.getVisibility() == 0) {
            this.mDownloadingView.setVisibility(8);
        }
        if (this.mExpireTime.getVisibility() == 0) {
            this.mExpireTime.setVisibility(8);
        }
        setViewVisibility(this.mMmsMsgLayout, 0);
        if (!sameItem) {
            this.mBodyTextView.rsetSpanList();
        }
        this.mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        boolean cust = this.mMessageItem.isRcsChat();
        boolean isSms = this.mMessageItem.isSms();
        boolean haveLoadedPdu = isSms || this.mMessageItem.mSlideshow != null;
        haveLoadedPdu = !haveLoadedPdu ? cust : true;
        CharSequence formattedMessage = this.mMessageItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = MessageUtils.formatMessage(this.mMessageItem.mBody, this.mMessageItem.mSubId, this.mMessageItem.mHighlight, this.mMessageItem.mTextContentType, this.mFontScale);
            this.mMessageItem.setCachedFormattedMessage(formattedMessage);
        }
        setBodyTextContent(sameItem, haveLoadedPdu, formattedMessage);
        updateSubjectView();
        debugBodyTextContent();
        if (!sameItem || haveLoadedPdu) {
            this.mDateView.setText(buildTime(this.mMessageItem.mDate));
        }
        if (this.mTextViewYear != null) {
            if (-1 != this.mNextYear) {
                this.mTextViewYear.setVisibility(0);
                this.mTextViewYear.setText(getContext().getResources().getString(R.string.sms_list_item_current_year, new Object[]{Integer.valueOf(this.mNextYear)}));
            } else {
                this.mTextViewYear.setVisibility(8);
            }
        }
        if (!isSms || this.mMessageItem.mGroupAllCnt <= 1) {
            setBottomViewInvisible(1);
        } else {
            setBottomViewVisible(1);
        }
        if (MessageUtils.isMultiSimEnabled()) {
            setSubscriptionNetworkTypeIcon(this.mMessageItem, isMultiSimActive);
        }
        if (isSms) {
            this.mMessageItem.setOnPduLoaded(null);
        } else if (this.mMessageItem.mSlideshow == null) {
            if (this.mMessageItem.isRcsChat()) {
                this.mSlideshowModelView.removeAllViews();
                this.mSlideshowModelView.hide();
            } else {
                this.mSlideshowModelView.bind(this.mMessageItem);
            }
            this.mMessageItem.setOnPduLoaded(new PduLoadedCallback() {
                public void onPduLoaded(MessageItem messageItem) {
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("MSG_APP_MessageListItem", "PduLoadedCallback in MessageListItem for item: " + MessageListItem.this.mPosition);
                    }
                    if (messageItem != null && MessageListItem.this.mMessageItem != null && messageItem.getMessageId() == MessageListItem.this.mMessageItem.getMessageId()) {
                        MessageListItem.this.mMessageItem.setCachedFormattedMessage(null);
                        MessageListItem.this.setChatBodyMaxWidth(MessageListItem.this.mMessageItem);
                        MessageListItem.this.setSubjectAndBodyLines();
                        MessageListItem.this.bindCommonMessage(true, isMultiSimActive);
                        MessageListItem.this.setItemClickListener();
                    }
                }
            });
        } else {
            this.mSlideshowModelView.setOnMmsPopViewDoubleClickCallback(this.mMmsPopViewClickCallback);
            this.mSlideshowModelView.bind(this.mMessageItem);
        }
        boolean isFailedByUndelivered = false;
        if (this.mMessageItem.getRcsMessageItem() != null) {
            isFailedByUndelivered = this.mMessageItem.getRcsMessageItem().isUndeliveredIm();
        }
        if ((this.mMessageItem.isOutgoingMessage() && ((this.mMessageItem.isFailedMessage() || this.mMessageItem.mDeliveryStatus == DeliveryStatus.FAILED) && this.mMessageItem.mGroupAllCnt <= 1)) || r3) {
            if (this.mRcsMessageListItem == null || !this.mRcsMessageListItem.isFTMsgItem(this.mMessageItem)) {
                this.mFailedIndicator.setVisibility(0);
            } else {
                this.mFailedIndicator.setVisibility(8);
            }
            if (this.mIsMultiChoice || !MmsConfig.isSmsEnabled(getContext())) {
                setViewClickListener(this.mFailedIndicator, null);
            } else {
                if (this.mMessageResendListener == null) {
                    this.mMessageResendListener = new MessageResendListener();
                }
                this.mMessageResendListener.setGroupMessage(false);
                setViewClickListener(this.mFailedIndicator, this.mMessageResendListener);
            }
        } else if (this.mFailedIndicator != null) {
            this.mFailedIndicator.setVisibility(8);
        }
        this.mDateView.setVisibility(!this.mNeedHideUnderPopView ? 0 : 8);
        drawRightStatusIndicator(this.mMessageItem);
        if (this.mRcsMessageListItem != null) {
            this.mRcsMessageListItem.bindCommonMessage(this.mBodyTextView, this);
        }
        if (haveLoadedPdu) {
            requestLayout();
        }
        if (!isSms && this.mMessageItem.mSlideshow != null && this.mItemLayoutCallback != null) {
            this.mItemLayoutCallback.onItemLayout(this.mMessageItem, null);
        }
    }

    private void setBodyTextContent(boolean sameItem, boolean haveLoadedPdu, CharSequence formattedMessage) {
        if (TextUtils.isEmpty(formattedMessage)) {
            this.mBodyTextView.setVisibility(8);
        } else {
            this.mBodyTextView.setVisibility(0);
            if (!sameItem || haveLoadedPdu) {
                if (TextUtils.isEmpty(this.mCachedLinkingMsg) || !HwMessageUtils.containsSpannableStringBuilder(this.mCachedLinkingMsg, formattedMessage)) {
                    LinkerTextTransfer.getInstance().setSpandText(this.mContext, this.mBodyTextView, formattedMessage, this.mMessageItem);
                    if (this.mHwCustMessageListItem != null) {
                        this.mHwCustMessageListItem.highlightWord(this.mBodyTextView, getBoxType());
                    }
                } else {
                    this.mBodyTextView.setText(this.mCachedLinkingMsg, BufferType.SPANNABLE);
                    if (this.mHwCustMessageListItem != null) {
                        this.mHwCustMessageListItem.highlightWord(this.mBodyTextView, getBoxType());
                    }
                }
            }
        }
        if (this.mBodyTextView.getHwCust() != null && this.mBodyTextView.getHwCust().isRcsSwitchOn()) {
            this.mBodyTextView.getHwCust().clearSpanList(this.mMessageItem);
        }
    }

    private void debugBodyTextContent() {
    }

    private int getBoxType() {
        boolean isIncoming = this.mMessageItem.isInComingMessage();
        int i;
        if (this.mMessageItem.isSms()) {
            if (isIncoming) {
                i = 0;
            } else {
                i = 1;
            }
            return i;
        }
        if (isIncoming) {
            i = 2;
        } else {
            i = 3;
        }
        return i;
    }

    public void onClick(View v) {
    }

    protected void sendMessage(MessageItem messageItem, int message) {
        if (this.mHandler != null) {
            Message msg = Message.obtain(this.mHandler, message);
            msg.obj = messageItem;
            msg.sendToTarget();
        }
    }

    private void drawRightStatusIndicator(MessageItem msgItem) {
        setLockedIconVisibility(msgItem.mLocked);
    }

    public void setChecked(boolean isMultiChoice, boolean checked) {
        this.mIsMultiChoice = isMultiChoice;
        if (this.mBottomButtonLayout != null) {
            this.mBottomButtonLayout.setEnabled(!this.mIsMultiChoice);
        }
        if (this.mIsMultiChoice) {
            this.mSuperLayout.setSelected(checked);
            this.mCheckBox.setChecked(checked);
            return;
        }
        this.mSuperLayout.setSelected(false);
    }

    public void setMultiChoice(boolean isMultiChoice) {
        this.mIsMultiChoice = isMultiChoice;
    }

    public void setSubscriptionNetworkTypeIcon(MessageItem msgItem, boolean isMultiSimActive) {
        if (isMultiSimActive) {
            if (this.mSubscriptionNetworkTypeView.getVisibility() == 8) {
                this.mSubscriptionNetworkTypeView.setVisibility(0);
            }
            if (HwDualCardNameHelper.isCardIdValid(msgItem.mSubId)) {
                int resId;
                String description = "";
                if (msgItem.mSubId == 0) {
                    resId = R.drawable.icon_card_sim1;
                    description = getContext().getString(R.string.folder_sim_1);
                } else {
                    resId = R.drawable.icon_card_sim2;
                    description = getContext().getString(R.string.folder_sim_2);
                }
                this.mSubscriptionNetworkTypeView.setImageResource(resId);
                this.mSubscriptionNetworkTypeView.setContentDescription(description);
                this.mSubscriptionNetworkTypeView.setVisibility(0);
            } else {
                MLog.v("MSG_APP_MessageListItem", "mSubId invalid! Set mSubcription view invisible!");
                this.mSubscriptionNetworkTypeView.setVisibility(8);
            }
        }
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void onTouchOutsideSpanText() {
        if (!(DelaySendManager.getInst().isDelayMsg(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients) || this.mMessageItem.mAttachmentType == 1)) {
            sendMessage(this.mMessageItem, 1000102);
        }
    }

    public void onSpanTextPressed(boolean pressed) {
        if (this.mMessageBlock != null) {
            this.mMessageBlock.setPressed(pressed);
        }
    }

    public boolean isEditTextClickable() {
        return !this.mIsMultiChoice;
    }

    public void setTextScale(float scale) {
        this.mFontScale = scale;
        if (this.mMessageItem != null) {
            this.mMessageItem.setCachedFormattedMessage(null);
        }
        float init_fontsize = HwUiStyleUtils.getPopMessageFontSize(getResources());
        if (MmsConfig.isEnableZoomWhenView()) {
            this.mBodyTextView.setTextSize(init_fontsize * scale);
            this.mSubjectTextView.setTextSize(init_fontsize * scale);
        }
    }

    public float getTextScale() {
        return this.mFontScale;
    }

    public void setMsgStatus(MessageItem mMessageItem) {
        if (this.mAudioIcon != null) {
            this.mAudioIcon.setVisibility(8);
        }
        this.mMessageStatus.setVisibility(0);
        if (this.mCancleCountView.getVisibility() == 0) {
            if (this.mFailedIndicator.getVisibility() == 0) {
                this.mFailedIndicator.setVisibility(8);
            }
            if (this.mGroupFaildMsgStatus.getVisibility() == 0) {
                this.mGroupFaildMsgStatus.setVisibility(8);
            }
            this.mGroupSendStatusView.setVisibility(8);
            return;
        }
        String uri = "";
        if (mMessageItem.mMessageUri != null) {
            uri = mMessageItem.mMessageUri.toString();
        }
        if (this.mRcsMessageListItem != null) {
            this.mRcsMessageListItem.initFailedIndicator(this.mFailedIndicator);
        }
        setViewVisibility(this.mFailedIndicator, 8);
        if (mMessageItem.isMms()) {
            if (mMessageItem.isFailedMmsMessage() || mMessageItem.isManualFailedMmsMessage(uri)) {
                if (mMessageItem.isInComingMessage()) {
                    setMessageStatusTextAndColor(R.string.download_failed);
                } else {
                    setMessageStatusTextAndColor(R.string.send_failed);
                    setViewVisibility(this.mFailedIndicator, 0);
                }
            } else if (mMessageItem.isSending()) {
                if (!mIsMessageItemCancled) {
                    setMessageStatusTextAndColor(R.string.message_status_sending);
                }
            } else if (mMessageItem.mBoxId == 2) {
                setMessageStatusTextAndColor("", false, ResEx.self().getMsgItemUnderPopColor());
                if (this.mHwCustMessageListItem != null) {
                    this.mHwCustMessageListItem.showMmsReportMoreStatus(MmsApp.getApplication().getApplicationContext(), mMessageItem, this.mMessageStatus);
                }
            } else {
                setMessageStatusGone();
            }
        } else if (!setConvMultiSmsMsgStatus()) {
            if (this.mRcsMessageListItem != null && this.mRcsMessageListItem.isFTMsgItem(mMessageItem) && this.mRcsMessageListItem.setMsgStatus(mMessageItem)) {
                if (mMessageItem instanceof RcsFileTransMessageItem) {
                    setRcsFTMessageStatus((RcsFileTransMessageItem) mMessageItem);
                }
                return;
            }
            this.mMessageStatus.setVisibility(0);
            if (this.mRcsMessageListItem != null && this.mRcsMessageListItem.setStatusText(mMessageItem, this.mMessageStatus, this.mFailedIndicator)) {
                setRcsMessageStatus(mMessageItem);
            } else if (mMessageItem.isFailedSmsMessage()) {
                setMessageStatusTextAndColor(R.string.send_failed);
                setViewVisibility(this.mFailedIndicator, 0);
            } else if (mMessageItem.isSendingSmsMessage()) {
                if (!mIsMessageItemCancled) {
                    setMessageStatusTextAndColor(R.string.message_status_sending);
                }
            } else if (mMessageItem.isSentAndReceivedSmsMessage()) {
                setMessageStatusTextAndColor(R.string.message_status_delivered);
            } else {
                setMessageStatusGone();
            }
        }
    }

    public void setChatBodyMaxWidth(MessageItem mMessageItem) {
        if (mMessageItem.mAddress != null) {
            int maxWidth;
            int widthPixels = MessageUtils.getWindowWidthPixels(getResources());
            if (MmsConfig.getSupportSmartSmsFeature() && isNotifyComposeMessage() && 1 == mMessageItem.mBoxId && mMessageItem.mType.equals("sms")) {
                maxWidth = (widthPixels - ((int) getResources().getDimension(R.dimen.duoqu_54dp))) - ((int) getResources().getDimension(R.dimen.avatar_view_width_height_conversation_item));
            } else {
                maxWidth = (widthPixels - (((int) getResources().getDimension(R.dimen.message_block_margin_screen)) * 2)) - (((int) getResources().getDimension(R.dimen.message_block_padding_start_end)) * 2);
            }
            if (this.mIsMultiChoice) {
                maxWidth = (maxWidth - ((int) getResources().getDimension(R.dimen.checkbox_wapper_width))) + ((int) getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_select));
            }
            this.mBodyTextView.setMaxWidth(maxWidth);
            this.mSubjectTextView.setMaxWidth(maxWidth);
            this.mExpireTime.setMaxWidth(maxWidth);
        }
    }

    public void setCheckBoxEnable(boolean value) {
        this.mCheckBox.setEnabled(value);
        setActivated(!value);
        if (value) {
            setAlpha(MULTI_RESET_CHOICE_MMS_ALPHA);
        } else {
            setAlpha(MULTI_CHOICE_MMS_ALPHA);
        }
    }

    private boolean setConvMultiSmsMsgStatus() {
        if (this.mMessageItem.mGroupAllCnt <= 1) {
            setViewVisibility(this.mGroupFaildMsgStatus, 8);
            if (this.mGroupSendStatusView != null) {
                this.mGroupSendStatusView.setVisibility(8);
            }
            return false;
        }
        int msgSendCnt = this.mMessageItem.mGroupSentCnt + this.mMessageItem.mGroupFailCnt;
        setViewVisibility(this.mGroupFaildMsgStatus, 8);
        this.mGroupSendStatusView.setVisibility(8);
        setMessageStatusGone();
        this.mFailedIndicator.setVisibility(8);
        setViewClickListener(this.mFailedIndicator, null);
        if (msgSendCnt < this.mMessageItem.mGroupAllCnt) {
            this.mMessageStatus.setVisibility(0);
            setMessageStatusTextAndColor(R.string.message_status_sending);
            this.mGroupSendStatusView.setVisibility(0);
            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            this.mGroupSendStatusView.setText("(" + format.format((long) (msgSendCnt + 1)) + "/" + format.format((long) this.mMessageItem.mGroupAllCnt) + ")");
        } else if (this.mMessageItem.mGroupFailCnt != 0) {
            this.mGroupFaildMsgStatus.setVisibility(0);
            this.mGroupFaildMsgStatus.setText(this.mContext.getString(R.string.send_fail_msg_number, new Object[]{Integer.valueOf(this.mMessageItem.mGroupFailCnt)}));
            this.mFailedIndicator.setVisibility(0);
            this.mFailedIndicator.setClickable(true);
            if (this.mMessageResendListener == null) {
                this.mMessageResendListener = new MessageResendListener();
            }
            this.mMessageResendListener.setGroupMessage(true);
            setViewClickListener(this.mFailedIndicator, this.mMessageResendListener);
        }
        return true;
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEditAble()) {
            return true;
        }
        if (!this.mUserChooosed) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == 1) {
            this.mUserChooosed = false;
        }
        return true;
    }

    public boolean onLongClick(View v) {
        return false;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBox != null) {
            if (this.mRcsMessageListItem == null || !this.mRcsMessageListItem.isRcsSwitchOn()) {
                setChecked(true, checked);
            } else {
                setChecked(this.mIsMultiChoice, checked);
            }
            refreshDrawableState();
        }
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public void toggle() {
        setChecked(!isChecked());
        refreshDrawableState();
    }

    public void setEditAble(boolean editable) {
        if (this.mCheckBox != null) {
            if (editable) {
                this.mCheckBox.setVisibility(0);
                for (View v : this.mClickWidgets.keySet()) {
                    v.setClickable(false);
                }
                for (View v2 : this.mLongClickWidgets.keySet()) {
                    v2.setLongClickable(false);
                }
                for (Entry<View, MmsClickListener> key : this.mMmsClickWidgets.entrySet()) {
                    ((MmsClickListener) key.getValue()).removeClickListener();
                }
                setEnabled(true);
            } else {
                this.mCheckBox.setVisibility(8);
                Set<Entry<View, OnClickListener>> entrySet = this.mClickWidgets.entrySet();
                Set<Entry<View, OnLongClickListener>> set = this.mLongClickWidgets.entrySet();
                for (Entry<View, OnClickListener> entry : entrySet) {
                    ((View) entry.getKey()).setOnClickListener((OnClickListener) entry.getValue());
                }
                for (Entry<View, OnLongClickListener> entry2 : set) {
                    ((View) entry2.getKey()).setOnLongClickListener((OnLongClickListener) entry2.getValue());
                }
                for (Entry<View, MmsClickListener> key2 : this.mMmsClickWidgets.entrySet()) {
                    ((MmsClickListener) key2.getValue()).setClickListener((View) key2.getKey());
                }
                setEnabled(false);
            }
            updateIconStyle(editable);
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
        return this.mCheckBox != null && this.mCheckBox.getVisibility() == 0;
    }

    private void setViewClickListener(View v, OnClickListener listener) {
        this.mClickWidgets.put(v, listener);
        if (isEditAble()) {
            v.setClickable(false);
        } else {
            v.setOnClickListener(listener);
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

    private void setMmsClickListener(View v, MmsClickListener listener) {
        this.mMmsClickWidgets.put(v, listener);
        if (isEditAble()) {
            listener.removeClickListener();
        } else {
            listener.setClickListener(v);
        }
    }

    private void updateSubjectView() {
        boolean hasSubject;
        if (TextUtils.isEmpty(this.mMessageItem.mSubject)) {
            hasSubject = false;
        } else {
            hasSubject = true;
        }
        if (!hasSubject || this.mMessageItem.isSms()) {
            this.mSubjectTextView.setText("");
            this.mSubjectTextView.setVisibility(8);
            return;
        }
        SmileyParser parser = SmileyParser.getInstance();
        SpannableStringBuilder buf = new SpannableStringBuilder();
        CharSequence smilizedSubject = parser.addSmileySpans(this.mMessageItem.mSubject, SMILEY_TYPE.MESSAGE_EDITTEXT, this.mFontScale);
        buf.append(TextUtils.replace(this.mContext.getResources().getString(R.string.inline_subject_new), new String[]{"%s"}, new CharSequence[]{smilizedSubject}));
        if (this.mMessageItem.mHighlight != null) {
            Matcher m = this.mMessageItem.mHighlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(1), m.start(), m.end(), 0);
            }
        }
        if (this.mSubjectTextView != null) {
            this.mSubjectTextView.setVisibility(0);
            this.mSubjectTextView.setText(buf);
            if (this.mHwCustMessageListItem != null) {
                this.mHwCustMessageListItem.highlightWord(this.mSubjectTextView, getBoxType());
            }
            setViewLongClickListener(this.mSubjectTextView, this);
            setViewClickListener(this.mSubjectTextView, this);
        }
    }

    public void setSubjectAndBodyLines() {
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

    private void startDownloadMms() {
        String uri = this.mMessageItem.mMessageUri.toString();
        saveMmsDownloadingStatus(uri, false, true, true);
        MLog.i("MSG_APP_MessageListItem", "Mms start download." + uri + " = " + MessageListAdapter.getDownloadingStatusFromMap(uri));
        if (this.mBottomButtonLayout != null) {
            this.mBottomButton.setText(R.string.cancel_download);
            this.mBottomButton.setVisibility(0);
        }
        Intent intent = new Intent(this.mContext, TransactionService.class);
        intent.putExtra("uri", uri);
        intent.putExtra(NumberInfo.TYPE_KEY, 1);
        int i = this.mButtonClickCount + 1;
        this.mButtonClickCount = i;
        intent.putExtra("button_download_click_count", i);
        this.mContext.startService(intent);
    }

    private void setExpireTimeVisibility() {
        if (this.mMessageItem.mMessageSize < 1 || this.mMessageItem.mTimestamp == null) {
            this.mExpireTime.setVisibility(8);
            return;
        }
        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        String msgSizeText = new StringBuffer().append(this.mContext.getString(R.string.message_size_label)).append(format.format((long) ((this.mMessageItem.mMessageSize + Place.TYPE_SUBLOCALITY_LEVEL_1) / Place.TYPE_SUBLOCALITY_LEVEL_2))).append(this.mContext.getString(R.string.kilobyte)).append("\n").toString();
        this.mExpireTime.setVisibility(0);
        this.mExpireTime.setText(buildTimestampLine(msgSizeText + this.mMessageItem.mTimestamp));
    }

    public boolean onDoubleTapUp(boolean isLink) {
        if (this.mMessageItem == null) {
            return false;
        }
        if (DelaySendManager.getInst().isDelayMsg(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients)) {
            DelaySendManager.getInst().setDelayMsgCanceled(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients);
            sendMessage(this.mMessageItem, 1000105);
        } else if (!isLink) {
            Activity activity = getActivityContext();
            if (activity instanceof ComposeMessageActivity) {
                ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag(activity, "Mms_UI_CMF");
                if (fragment != null) {
                    fragment.setmCryptoToastIsShow(true);
                }
            }
            if (this.mMessageItem.getCryptoMessageItem().isEncryptSms(this.mMessageItem)) {
                return true;
            }
            if (!HwMessageUtils.isSplitOn()) {
                MessageUtils.viewMessageText(getContext(), this.mMessageItem);
            } else if (getActivityContext() instanceof ConversationList) {
                MessageUtils.viewMessageText(getContext(), this.mMessageItem, ((ConversationList) getActivityContext()).getRightFragment());
            }
        }
        return true;
    }

    public void onTouchLink(ClickableSpan span) {
    }

    private void setItemClickListener() {
        ArrayList<View> clickViews = new ArrayList();
        clickViews.add(this.mMessageBlock);
        int i = 0;
        for (View v : clickViews) {
            if (v != null) {
                if (this.mIsMultiChoice) {
                    v.setClickable(false);
                } else if (this.mIsDelayMsg) {
                    if (this.mClickListenerList.size() <= i) {
                        this.mClickListenerList.add(i, new MmsClickListener(this.mItemTouchListener));
                    }
                    ((MmsClickListener) this.mClickListenerList.get(i)).setClickListener(v);
                } else {
                    v.setOnClickListener(this);
                }
                setViewLongClickListener(v, this);
                i++;
            }
        }
    }

    private void checkDelayMsg() {
        if (this.mMessageItem.isNotDelayMsg() && !this.mIsDelayMsg) {
            setNotDelayMsg();
        } else if (DelaySendManager.getInst().getCancelMsgStatus(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients) == 0) {
            setNotDelayMsg();
            if (System.currentTimeMillis() - this.mMessageItem.mDate < 6000) {
                createCancelUpdate();
                DelaySendManager.getInst().addUIUpdate(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients, this.mCancelUpdate);
            }
        } else {
            createCancelUpdate();
            DelaySendManager.getInst().registerUiUpdate(this.mMessageItem.getCancelId(), this.mMessageItem.mType, this.mMessageItem.mIsMultiRecipients, this.mCancelUpdate);
        }
    }

    private void setNotDelayMsg() {
        this.mIsDelayMsg = false;
        this.mBodyTextView.setIsClickIntercepted(false);
        if (this.mCancleCountView.getVisibility() == 0) {
            this.mCancleCountView.setVisibility(8);
        }
    }

    private void createCancelUpdate() {
        if (this.mCancelUpdate == null) {
            this.mCancelUpdate = new UpdateCallback() {
                public void onUpdate(long count, long id, String msgType) {
                    boolean isSameMsg;
                    MessageListItem.this.mIsDelayMsg = true;
                    MessageListItem.this.mBodyTextView.setIsClickIntercepted(true);
                    if (id == MessageListItem.this.mMessageItem.getCancelId()) {
                        isSameMsg = msgType.equals(MessageListItem.this.mMessageItem.mType);
                    } else {
                        isSameMsg = false;
                    }
                    if (!isSameMsg) {
                        return;
                    }
                    if (count == 0) {
                        MessageListItem.this.mCancleCountView.setVisibility(8);
                        MessageListItem.this.setMessageStatusTextAndColor(R.string.message_status_sending);
                        MessageListItem.this.mIsDelayMsg = false;
                        MessageListItem.this.mBodyTextView.setIsClickIntercepted(false);
                        MessageListItem.this.updateGroupCount(true);
                        return;
                    }
                    if (MessageListItem.this.mCancleCountView.getVisibility() == 8) {
                        MessageListItem.this.mCancleCountView.setVisibility(0);
                        if (MessageListItem.this.mMessageStatus.getVisibility() == 8) {
                            MessageListItem.this.mMessageStatus.setVisibility(0);
                        }
                        MessageListItem.this.setMessageStatusTextAndColor(R.string.mms_cancel_send_status);
                        MessageListItem.this.updateGroupCount(false);
                    }
                    NumberFormat nf = NumberFormat.getIntegerInstance();
                    nf.setGroupingUsed(false);
                    MessageListItem.this.mCancleCountView.setText(nf.format(count));
                }
            };
        }
    }

    private void processSingleClick(View view) {
        sendMessage(this.mMessageItem, 1000102);
    }

    public Activity getActivityContext() {
        if (this.mContext == null) {
            Log.e("MSG_APP_MessageListItem", "XIAO YUAN need Activity for doAction option param");
            return null;
        } else if (this.mContext instanceof Activity) {
            return (Activity) this.mContext;
        } else {
            Log.e("MSG_APP_MessageListItem", "XIAO YUAN need Activity for doAction option param,but mContext is not Activity.");
            return null;
        }
    }

    public boolean onSmartSmsEvent(short eventType) {
        return false;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public void setFlingState(boolean flingState) {
        this.mFlingState = flingState;
    }

    public boolean isScrollFing() {
        return this.mFlingState;
    }

    public boolean isNotifyComposeMessage() {
        if (this.mContext == null || !(this.mContext instanceof ComposeMessageActivity)) {
            return isNotifyComposeMessageWhenSplit();
        }
        ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((Activity) this.mContext, "Mms_UI_CMF");
        if (fragment == null) {
            return false;
        }
        ComposeMessageFragment smartSmsHolder = fragment;
        return fragment.isNotifyComposeMessage();
    }

    private boolean isNotifyComposeMessageWhenSplit() {
        if (HwMessageUtils.isSplitOn() && (this.mContext instanceof ConversationList)) {
            Fragment f = ((ConversationList) this.mContext).getRightFragment();
            if (f instanceof ComposeMessageFragment) {
                return ((ISmartSmsUIHolder) f).isNotifyComposeMessage();
            }
        }
        return false;
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public boolean showDefaultListItem() {
        bindCommonMessage(this.mSameItem, this.mIsMultiSimActive);
        bindMsgBodyAfter();
        return false;
    }

    public boolean bindCommonItem() {
        bindCommonMessage(this.mSameItem, this.mIsMultiSimActive);
        setChatBodyMaxWidth(this.mMessageItem);
        return false;
    }

    public boolean bindItemAfter(boolean reBindMsg) {
        if (reBindMsg) {
            bindCommonMessage(this.mSameItem, this.mIsMultiSimActive);
        }
        bindMsgBodyAfter();
        return false;
    }

    public void setRichViewLongClick(View richItemView) {
        if (richItemView != null) {
            setViewLongClickListener(richItemView, this);
        }
    }

    public int getShowBubbleMode() {
        if (MmsConfig.getSupportSmartSmsFeature() && isNotifyComposeMessage() && 1 == this.mMessageItem.mBoxId && this.mMessageItem.mType.equals("sms")) {
            return SmartSmsSdkUtil.getBubbleStyle(this.mContext);
        }
        return 0;
    }

    private void bindRichBubbleView(int showBubbleMode) {
        if (this.mSmartSmsBubble == null) {
            this.mSmartSmsBubble = new SmartSmsBubbleManager(this, this);
        }
        if (showBubbleMode == 2) {
            bindHeadTimeArea();
        }
        this.mSmartSmsBubble.bindBubbleView(this.mMessageItem, showBubbleMode);
        if (-1 != this.mNextYear) {
            this.mTextViewYear.setVisibility(0);
            this.mTextViewYear.setText(getContext().getResources().getString(R.string.sms_list_item_current_year, new Object[]{Integer.valueOf(this.mNextYear)}));
            return;
        }
        this.mTextViewYear.setVisibility(8);
    }

    public void itemLayoutCallBack() {
        if (this.mItemLayoutCallback != null) {
            this.mItemLayoutCallback.onItemLayout(this.mMessageItem, null);
        }
    }

    public SmartSmsBubbleManager getSmartSmsBubble() {
        return this.mSmartSmsBubble;
    }

    private void setBottomViewVisible(int id) {
        boolean z;
        this.mBottomStubView.setVisibility(0);
        this.mBottomButtonLayout = (LinearLayout) findViewById(R.id.bottom_button_layout);
        this.mBottomDivider = (ImageView) findViewById(R.id.mms_divider);
        this.mBottomDivider.setVisibility(0);
        this.mBottomButton = (TextView) findViewById(R.id.bottom_button);
        LinearLayout linearLayout = this.mBottomButtonLayout;
        if (this.mIsMultiChoice) {
            z = false;
        } else {
            z = true;
        }
        linearLayout.setEnabled(z);
        setMessageBlockPaddingValues(true);
        int textId = 0;
        int textColorId = 0;
        int dividerDrawableId = 0;
        switch (id) {
            case 1:
                MmsClickListener mmsClickListener = new MmsClickListener(new BottomClickListener());
                textId = R.string.msg_detail;
                textColorId = R.drawable.text_color_white_send;
                dividerDrawableId = R.drawable.pop_button_line_bg_detail_send;
                setMmsClickListener(this.mBottomButtonLayout, mmsClickListener);
                break;
            case 2:
                OnClickListener clickListener = new OnClickListener() {
                    public void onClick(View v) {
                        String uri = MessageListItem.this.mMessageItem.mMessageUri.toString();
                        if (MessageListAdapter.getConnectionManagerFromMap(uri) == null) {
                            MLog.i("MSG_APP_MessageListItem", "MmsConnectionManager is null, this mms has been downloaded.");
                        } else if (MmsConfig.getMmsEnabled()) {
                            boolean isDownloading = MessageListAdapter.getDownloadingStatusFromMap(uri);
                            MessageListItem.this.mBodyTextView.setVisibility(8);
                            if (isDownloading) {
                                MessageListItem.this.saveMmsDownloadingStatus(uri, true, false, true);
                                MLog.i("MSG_APP_MessageListItem", "Mms download cancel." + uri + " = " + MessageListAdapter.getDownloadingStatusFromMap(uri));
                                MessageListItem.this.mDownloadingView.setVisibility(8);
                                if (MessageListItem.this.mBottomButtonLayout != null) {
                                    MessageListItem.this.mBottomButton.setText(R.string.download);
                                }
                                TransactionService.startMe(MessageListItem.this.mContext, uri, 4);
                                DownloadManager.getInstance().isNeedUpdateTime = true;
                                DownloadManager.getInstance().markState(MessageListItem.this.mMessageItem.mMessageUri, 128);
                                DownloadManager.getInstance().isNeedUpdateTime = false;
                            } else if (MessageUtils.isUsingVoWifi(MessageListItem.this.getContext()) || !MessageUtils.isNeedShowToastWhenNetIsNotAvailable(MessageListItem.this.getContext(), MessageListItem.this.mMessageItem)) {
                                MessageListItem.this.startDownloadMms();
                            }
                        } else {
                            Toast.makeText(MessageListItem.this.mContext, R.string.mms_not_supported, 0).show();
                        }
                    }
                };
                textId = R.string.download;
                textColorId = R.color.incoming_msg_text_color;
                dividerDrawableId = R.drawable.pop_button_line_bg_detail_recv;
                setViewClickListener(this.mBottomButtonLayout, clickListener);
                break;
        }
        this.mBottomDivider.setBackgroundResource(dividerDrawableId);
        this.mBottomButton.setText(textId);
        this.mBottomButton.setTextColor(getResources().getColor(textColorId));
    }

    private void setBottomViewInvisible(int id) {
        if (this.mBottomStubView.getVisibility() == 0) {
            this.mBottomStubView.setVisibility(8);
            this.mBottomDivider.setVisibility(8);
            if (id != 1 || this.mMmsClickWidgets.get(this.mBottomButtonLayout) == null) {
                this.mBottomButtonLayout.setOnClickListener(null);
            } else {
                ((MmsClickListener) this.mMmsClickWidgets.get(this.mBottomButtonLayout)).removeClickListener();
            }
            setMessageBlockPaddingValues(false);
        }
    }

    private void updateGroupCount(boolean isVisible) {
        if (this.mMultiRecipients && !this.mMessageItem.isMms()) {
            if (isVisible) {
                int msgSendCnt = this.mMessageItem.mGroupFailCnt + this.mMessageItem.mGroupSentCnt;
                this.mGroupSendStatusView.setVisibility(0);
                NumberFormat format = NumberFormat.getIntegerInstance();
                format.setGroupingUsed(false);
                this.mGroupSendStatusView.setText("(" + format.format((long) (msgSendCnt + 1)) + "/" + format.format((long) this.mMessageItem.mGroupAllCnt) + ")");
            } else {
                this.mGroupSendStatusView.setVisibility(8);
                this.mGroupSendStatusView.setText("");
            }
        }
    }

    protected int getContentResId() {
        return R.id.mms_layout_view_super_parent;
    }

    public void setTextSpanLinkingCache(TextSpanLinkingCache textSpanLinkingCache) {
        this.mTextSpanLinkingCache = textSpanLinkingCache;
    }

    public CryptoMessageListItem getCryptoMessageListItem() {
        return this.mCryptoMessageListItem;
    }

    public static void setMsgItemCancled(boolean cancle) {
        mIsMessageItemCancled = cancle;
    }

    public static boolean getMsgItemCancled() {
        return mIsMessageItemCancled;
    }

    public void setSearchString(String aSearchString) {
        if (this.mHwCustMessageListItem != null) {
            this.mHwCustMessageListItem.setSearchString(aSearchString);
        }
    }

    public void setItemText(MessageItem msgItem) {
        this.mMessageItem = msgItem;
        LinkerTextTransfer.getInstance().setSpandText(this.mContext, this.mBodyTextView, MessageUtils.formatMessage(msgItem.mBody, msgItem.mSubId, msgItem.mHighlight, msgItem.mTextContentType, this.mFontScale), msgItem);
        if (!this.mMessageItem.isRcsChat() && this.mTextSpanLinkingCache != null) {
            this.mTextSpanLinkingCache.updateTextSpanable(this, true);
        }
    }

    public long getMsgItemID() {
        return this.mMessageItem == null ? 0 : this.mMessageItem.mMsgId;
    }

    private void setLockedIconVisibility(boolean visible) {
        if (visible) {
            this.mLockedIndicator.setVisibility(0);
        } else {
            this.mLockedIndicator.setVisibility(8);
        }
    }

    public SpannableStringBuilder getCachedLinkingMsg() {
        if (this.mTextSpanLinkingCache == null || this.mMessageItem == null) {
            return null;
        }
        return this.mTextSpanLinkingCache.getCacheByKey(MessageListAdapter.getKey(this.mMessageItem.mType, this.mMessageItem.mMsgId));
    }

    public void setNeedShowTimePhase(boolean needShowTimePhase) {
        this.mNeedShowTimePhase = needShowTimePhase;
    }

    public boolean isNeedShowTimePhase() {
        return this.mNeedShowTimePhase;
    }

    public void setAudioIconVisibility(int visibility) {
        if (this.mAudioIcon != null) {
            this.mAudioIcon.setVisibility(visibility);
        }
    }

    private void setMessageStatusTextAndColor(int resId) {
        if (this.mMessageStatus != null) {
            switch (resId) {
                case R.string.download_failed:
                case R.string.send_failed:
                case R.string.receive_fail:
                    this.mMessageStatus.setTextColor(ResEx.self().getConvItemErrorMsgTextColor());
                    hideUnderPopViewVisible(false);
                    break;
                case R.string.message_status_sending:
                case R.string.mms_cancel_send_status:
                    this.mMessageStatus.setTextColor(ResEx.self().getMsgItemUnderPopColor());
                    hideUnderPopViewVisible(true);
                    break;
                default:
                    this.mMessageStatus.setTextColor(ResEx.self().getMsgItemUnderPopColor());
                    hideUnderPopViewVisible(false);
                    break;
            }
            this.mMessageStatus.setText(this.mContext.getString(resId));
        }
    }

    private void setMessageStatusTextAndColor(String text, boolean hideUnderPopView, int color) {
        if (this.mMessageStatus != null) {
            this.mMessageStatus.setTextColor(color);
            hideUnderPopViewVisible(hideUnderPopView);
            this.mMessageStatus.setText(text);
        }
    }

    private void setMessageStatusGone() {
        hideUnderPopViewVisible(false);
        this.mMessageStatus.setVisibility(8);
    }

    private void hideUnderPopViewVisible(boolean hideUnderPopView) {
        this.mNeedHideUnderPopView = hideUnderPopView;
        this.mDateView.setVisibility(!hideUnderPopView ? 0 : 8);
    }

    private void setRcsFTMessageStatus(RcsFileTransMessageItem mMessageItem) {
        if (mMessageItem.mIsOutgoing) {
            this.mMessageStatus.setVisibility(0);
            if (1000 == mMessageItem.mImAttachmentStatus || 1007 == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.message_status_sending);
            } else if (1009 == mMessageItem.mImAttachmentStatus || 1001 == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.send_failed);
            } else if (1010 == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.status_canceled);
            } else if (1003 == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.message_status_delivered);
            } else if (1004 == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.status_read);
            } else if (Place.TYPE_PREMISE == mMessageItem.mImAttachmentStatus) {
                setMessageStatusTextAndColor(R.string.message_status_undelivered);
            } else if (Place.TYPE_ROOM == mMessageItem.mImAttachmentStatus) {
                setMessageStatusGone();
            } else {
                setMessageStatusGone();
            }
        } else if (1001 == mMessageItem.mImAttachmentStatus) {
            this.mMessageStatus.setVisibility(0);
            setMessageStatusTextAndColor(R.string.receive_fail);
        } else if (1010 == mMessageItem.mImAttachmentStatus) {
            this.mMessageStatus.setVisibility(0);
            setMessageStatusTextAndColor(R.string.status_canceled);
        } else if (1002 == mMessageItem.mImAttachmentStatus) {
            this.mMessageStatus.setVisibility(8);
            if (this.mAudioIcon != null && mMessageItem.isAudioFileType()) {
                this.mAudioIcon.setVisibility(0);
            }
        } else if (1009 == mMessageItem.mImAttachmentStatus) {
            this.mMessageStatus.setVisibility(0);
            setMessageStatusTextAndColor(R.string.status_reject);
        }
    }

    private void setRcsMessageStatus(MessageItem messageItem) {
        if (messageItem.getRcsMessageItem() != null && messageItem.isRcsChat() && messageItem.getRcsMessageItem().isUndeliveredIm()) {
            if (RcsTransaction.isShowUndeliveredIcon()) {
                this.mMessageStatus.setVisibility(0);
                setMessageStatusTextAndColor(R.string.message_status_undelivered);
                this.mFailedIndicator.setImageResource(R.drawable.rcs_ic_alert_chat_undelivered);
                this.mFailedIndicator.setClickable(true);
                this.mFailedIndicator.setVisibility(0);
            } else {
                setMessageStatusTextAndColor(R.string.message_status_undelivered);
            }
        }
        if (messageItem.getRcsMessageItem() != null && messageItem.isRcsChat() && messageItem.getRcsMessageItem().isReadIm()) {
            setMessageStatusTextAndColor(R.string.status_read);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateMessageBlockVisibility() {
        int i = 0;
        if (!(this.mMessageBlock == null || this.mMessageItem == null || getShowBubbleMode() != 0)) {
            View view = this.mMessageBlock;
            if (!(this.mMessageItem.hasText() || this.mMessageItem.isSms())) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    public View getMessageBlockSuper() {
        return this.mMessageBlockSuper;
    }

    public ViewGroup getRichBubbleLayoutParent() {
        return this.mSmartSmsBubble != null ? this.mSmartSmsBubble.getRichBubbleLayoutParent() : null;
    }

    public boolean isShowingRich() {
        return this.mSmartSmsBubble != null ? this.mSmartSmsBubble.isShowingRich() : false;
    }

    private void resendAllFailedMsgInGroupMessageItem() {
        if (this.mContext != null) {
            ContentValues values = new ContentValues(2);
            values.put(NumberInfo.TYPE_KEY, Integer.valueOf(6));
            values.put("date", Long.valueOf(System.currentTimeMillis()));
            StringBuilder selection = new StringBuilder();
            selection.append("group_id");
            selection.append("='").append(this.mMessageItem.mUid).append("'");
            selection.append(" and type='5'");
            MLog.d("MSG_APP_MessageListItem", "move failed message to queueBox count: " + SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), Sms.CONTENT_URI, values, selection.toString(), null));
            this.mContext.sendBroadcast(new Intent("com.android.mms.transaction.SEND_MESSAGE", null, this.mContext, SmsReceiver.class));
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEditAble()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void setMessageBlockPaddingValues(boolean bottomViewIsShown) {
        int i;
        int paddingStartEnd = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_block_padding_start_end);
        int paddingTopBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_block_padding_top_bottom);
        View view = this.mMessageBlock;
        if (bottomViewIsShown) {
            i = 0;
        } else {
            i = paddingTopBottom;
        }
        view.setPaddingRelative(paddingStartEnd, paddingTopBottom, paddingStartEnd, i);
    }

    private void updateMessageBlockSuperMagin() {
        if (this.mItemType == 7) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mMessageBlockSuper.getLayoutParams();
            if (this.mIsMultiChoice) {
                layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.message_block_margin_screen_multichoose));
            } else {
                layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.message_block_margin_screen));
            }
        }
    }

    public void clearModelChangeObservers() {
        if (this.mMessageItem != null) {
            this.mMessageItem.clearModelChangeObservers();
        }
    }

    public void setDarkThemeStatus(boolean isDarkThemeOn) {
        this.mIsDarkThemeOn = isDarkThemeOn;
    }
}
