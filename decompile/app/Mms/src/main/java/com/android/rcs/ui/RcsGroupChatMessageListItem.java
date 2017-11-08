package com.android.rcs.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.DelaySendManager.UpdateCallback;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransGroupMessageListItem;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.util.HashMap;

public class RcsGroupChatMessageListItem extends AvatarWidget implements SpandTouchMonitor, OnClickListener, CheckableView, OnLongClickListener {
    private static final String[] PHONES_PROJECTION = new String[]{"display_name", "photo_id", "contact_id"};
    private static final String[] SELF_PROJECTION = new String[]{"_id", "display_name"};
    private ImageButton mAudioIcon;
    private SpandTextView mBodyTextView;
    private UpdateCallback mCancelUpdate;
    private TextView mCancleCountView;
    private CheckBox mCheckBox;
    private TextView mContactText;
    private ContentResolver mContentResolver = null;
    private Context mContext;
    private TextView mDateView;
    private ImageView mFailedIndicator;
    private float mFontScale = ContentUtil.FONT_SIZE_NORMAL;
    public RcsFileTransGroupMessageListItem mFtGroupMsgListItem;
    RcsGroupChatComposeMessageFragment mGroupFrg = null;
    private Handler mHandler;
    private boolean mIsDelayMsg = false;
    private boolean mIsMultiChoice = false;
    private ItemTouchListener mItemTouchListener = new ItemTouchListener();
    private LinearLayout mLocLayout;
    private ImageView mLocationImg;
    private TextView mLocationSubTv;
    private TextView mLocationTv;
    private View mMessageBlock;
    private RcsGroupChatMessageItem mMessageItem;
    private boolean mNeedShowTimePhase = false;
    private int mNextYear = -1;
    private TextView mStatusView;
    private RelativeLayout mSuperLayout;
    private TextView mTextTimePhase;
    private TextView mTextViewYear;
    private long mThreadId = 0;
    private Drawable sDefaultContactImage;

    public class GroupChatMsgListItemCallback {
        public void setAudioIconVisibility(int visible) {
            if (RcsGroupChatMessageListItem.this.mAudioIcon != null) {
                RcsGroupChatMessageListItem.this.mAudioIcon.setVisibility(visible);
            }
        }

        public void cancleDelayMsg() {
            if (DelaySendManager.getInst().isDelayMsg(RcsGroupChatMessageListItem.this.mMessageItem.getCancelId(), RcsGroupChatMessageListItem.this.mMessageItem.getMessageType())) {
                DelaySendManager.getInst().setDelayMsgCanceled(RcsGroupChatMessageListItem.this.mMessageItem.getCancelId(), RcsGroupChatMessageListItem.this.mMessageItem.getMessageType(), false);
                RcsGroupChatMessageListItem.this.sendMessage(RcsGroupChatMessageListItem.this.mMessageItem, 1000101);
            }
        }
    }

    private class ItemTouchListener implements IMmsClickListener {
        private ItemTouchListener() {
        }

        public void onDoubleClick(View view) {
            if (DelaySendManager.getInst().isDelayMsg(RcsGroupChatMessageListItem.this.mMessageItem.getCancelId(), RcsGroupChatMessageListItem.this.mMessageItem.getMessageType())) {
                DelaySendManager.getInst().setDelayMsgCanceled(RcsGroupChatMessageListItem.this.mMessageItem.getCancelId(), RcsGroupChatMessageListItem.this.mMessageItem.getMessageType(), false);
                RcsGroupChatMessageListItem.this.sendMessage(RcsGroupChatMessageListItem.this.mMessageItem, 1000101);
            }
        }

        public void onSingleClick(View view) {
        }
    }

    public RcsGroupChatMessageListItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public RcsGroupChatMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setConversationId(long threadId) {
        this.mThreadId = threadId;
    }

    public void bind(RcsGroupChatMessageItem msgItem, int position, String groupId, boolean isMultiChoice, boolean isChecked, boolean isScrolling) {
        this.mIsMultiChoice = isMultiChoice;
        if (this.sDefaultContactImage == null) {
            this.sDefaultContactImage = new BitmapDrawable(getResources(), RcsUtility.toRoundCorner(BitmapFactory.decodeStream(this.mContext.getResources().openRawResource(R.drawable.rcs_avatar40_default_gray)), 10));
        }
        if (this.mContentResolver == null) {
            this.mContentResolver = this.mContext.getContentResolver();
        }
        this.mMessageItem = msgItem;
        LayoutParams superLayoutParams = (LayoutParams) this.mSuperLayout.getLayoutParams();
        if (isNeedShowTimePhase()) {
            this.mTextTimePhase.setVisibility(0);
            this.mTextTimePhase.setText(buildTime(this.mMessageItem.mDate, true));
            superLayoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_item_margin_top);
        } else {
            this.mTextTimePhase.setVisibility(8);
            superLayoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.message_item_margin_top2);
        }
        this.mSuperLayout.setLayoutParams(superLayoutParams);
        bindCommonMessage();
        setLongClickable(false);
        setClickable(false);
        initViewClickState(isMultiChoice, isChecked);
        if (100 == this.mMessageItem.mType || 101 == this.mMessageItem.mType) {
            if (isScrolling) {
                this.mFtGroupMsgListItem.setTag(Boolean.valueOf(isScrolling));
            } else {
                this.mFtGroupMsgListItem.setTag(null);
            }
            this.mBodyTextView.setVisibility(8);
            MLog.i("GroupMessageListItem FileTrans: ", "bind Msg Id = " + msgItem.mFtGroupMsgItem.mMsgId);
            this.mFtGroupMsgListItem.setVisibility(0);
            this.mFtGroupMsgListItem.setConversationId(this.mThreadId);
            this.mFtGroupMsgListItem.setMultiChoice(isMultiChoice);
            this.mFtGroupMsgListItem.bind(msgItem.mFtGroupMsgItem, isMultiChoice);
            if (100 == this.mMessageItem.mType) {
                updateGroupMessageStatus(msgItem.mFtGroupMsgItem);
            } else {
                updateRecvGroupMessageStatus(msgItem.mFtGroupMsgItem);
            }
        } else {
            this.mBodyTextView.setVisibility(0);
            this.mFtGroupMsgListItem.setVisibility(8);
        }
        locationStatus();
    }

    private void locationStatus() {
        if (RcsProfileUtils.getRcsMsgExtType(this.mMessageItem.mCursor) == 6) {
            ViewStub favLocReListItem = (ViewStub) findViewById(R.id.rcs_loc_list_item);
            if (favLocReListItem != null) {
                favLocReListItem.setLayoutResource(R.layout.rcs_item_chat_received_location);
                favLocReListItem.inflate();
            }
            this.mLocationTv = (TextView) findViewById(R.id.location_attach_title);
            if (this.mLocationTv != null) {
                this.mLocationTv.setVisibility(8);
            }
            this.mLocLayout = (LinearLayout) findViewById(R.id.loc_layout_view);
            this.mLocationSubTv = (TextView) findViewById(R.id.location_attach_subtitle);
            this.mLocationImg = (ImageView) findViewById(R.id.location_img);
            if (this.mLocLayout != null) {
                final HashMap<String, String> locInfo = RcsMapLoader.getLocInfo(this.mBodyTextView.getText().toString());
                this.mBodyTextView.setVisibility(8);
                this.mLocLayout.setVisibility(0);
                this.mLocationSubTv.setVisibility(0);
                this.mLocationImg.setVisibility(0);
                this.mLocationSubTv.setText((CharSequence) locInfo.get("subtitle"));
                new MmsClickListener(new IMmsClickListener() {
                    public void onDoubleClick(View view) {
                    }

                    public void onSingleClick(View view) {
                        RcsMapLoaderFactory.getMapLoader(RcsGroupChatMessageListItem.this.mContext).loadMap(RcsGroupChatMessageListItem.this.mContext, locInfo);
                    }
                }).setClickListener(this.mLocLayout);
                this.mLocLayout.setOnLongClickListener(this);
                if (this.mMessageItem.isOutgoingMessage()) {
                    this.mLocationSubTv.setTextColor(this.mContext.getResources().getColor(R.color.text_color_white_important));
                    this.mLocationImg.setImageResource(R.drawable.rcs_map_item_big_send);
                    return;
                }
                return;
            }
            return;
        }
        if (!this.mMessageItem.isFileTransMessage()) {
            this.mBodyTextView.setVisibility(0);
        }
        if (this.mLocationSubTv != null) {
            this.mLocationSubTv.setVisibility(8);
        }
        if (this.mLocLayout != null) {
            this.mLocLayout.setVisibility(8);
        }
        if (this.mLocationImg != null) {
            this.mLocationImg.setVisibility(8);
        }
    }

    private void updateRecvGroupMessageStatus(RcsFileTransGroupMessageItem groupItem) {
        if (groupItem != null && this.mFailedIndicator != null && this.mStatusView != null) {
            this.mFailedIndicator.setVisibility(8);
            if (groupItem.mImAttachmentStatus == 1001) {
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.receive_fail);
            } else if (groupItem.mImAttachmentStatus == 1010) {
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.status_canceled);
            } else if (groupItem.mImAttachmentStatus == 1009) {
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.status_reject);
            } else if (groupItem.mImAttachmentStatus == 1002 && this.mAudioIcon != null && groupItem.isAudioFileType()) {
                this.mAudioIcon.setVisibility(0);
            }
        }
    }

    private void initViewClickState(boolean isMultiChoice, boolean isChecked) {
        this.mMessageBlock.setClickable(false);
        this.mDateView.setClickable(false);
        this.mDateView.setLongClickable(false);
        this.mStatusView.setClickable(false);
        this.mDateView.setLongClickable(false);
        if (this.mIsMultiChoice) {
            this.mCheckBox.setVisibility(0);
            this.mCheckBox.setEnabled(true);
            this.mCheckBox.setChecked(isChecked);
            this.mMessageBlock.setOnLongClickListener(null);
            this.mMessageBlock.setLongClickable(false);
            this.mBodyTextView.setClickable(false);
            this.mBodyTextView.setLongClickable(false);
        } else {
            this.mCheckBox.setEnabled(false);
            this.mCheckBox.setVisibility(8);
            this.mMessageBlock.setLongClickable(true);
            this.mBodyTextView.setClickable(true);
            this.mBodyTextView.setLongClickable(true);
            this.mMessageBlock.setOnLongClickListener(this);
            new MmsClickListener(this.mItemTouchListener).setClickListener(this.mMessageBlock);
            this.mFailedIndicator.setOnClickListener(this);
        }
        if ((this.mGroupFrg == null || !this.mGroupFrg.getLoginStatus() || this.mGroupFrg.getOwnerStatus()) && !RcsGroupChatComposeMessageFragment.isExitRcsGroupEnable()) {
            this.mFailedIndicator.setEnabled(true);
        } else {
            this.mFailedIndicator.setEnabled(false);
        }
    }

    private void bindCommonMessage() {
        this.mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        CharSequence formattedMessage = this.mMessageItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = MessageUtils.formatMessage(this.mMessageItem.mBody, this.mMessageItem.mHighLight, null, this.mFontScale);
            this.mMessageItem.setCachedFormattedMessage(formattedMessage);
        }
        LinkerTextTransfer.getInstance().setSpandText(this.mContext, this.mBodyTextView, formattedMessage, this.mMessageItem);
        String addr = this.mMessageItem.mAddress;
        if (this.mItemType == 8) {
            updateMyAvatarIcon();
        } else {
            updateAvatarIcon(addr);
        }
        if (this.mMessageItem.mType == 1 || this.mMessageItem.mType == 101) {
            setMessageStatusGone();
            updateContactNameText(addr);
        } else if (this.mMessageItem.mType == 4 || this.mMessageItem.mType == 100) {
            this.mStatusView.setVisibility(0);
        }
        setTime(this.mMessageItem.mDate, 1);
        this.mDateView.setText(buildTime(this.mMessageItem.mDate));
        checkDelayMsg();
        updateGroupMessageStatus(this.mMessageItem);
        setChatBodyMaxWidth();
        if (-1 != this.mNextYear) {
            this.mTextViewYear.setVisibility(0);
            this.mTextViewYear.setText(getContext().getResources().getString(R.string.sms_list_item_current_year, new Object[]{Integer.valueOf(this.mNextYear)}));
        } else {
            this.mTextViewYear.setVisibility(8);
        }
        if (1 == this.mMessageItem.mType) {
            this.mStatusView.setVisibility(8);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.sDefaultContactImage == null) {
            this.sDefaultContactImage = new BitmapDrawable(getResources(), RcsUtility.toRoundCorner(BitmapFactory.decodeStream(this.mContext.getResources().openRawResource(R.drawable.rcs_avatar40_default_gray)), 10));
        }
        this.mSuperLayout = (RelativeLayout) findViewById(R.id.mms_layout_view_super_parent);
        this.mBodyTextView = (SpandTextView) findViewById(R.id.text_view);
        this.mDateView = (TextView) findViewById(R.id.date_view);
        this.mStatusView = (TextView) findViewById(R.id.status);
        this.mMessageBlock = findViewById(R.id.message_block);
        this.mAudioIcon = (ImageButton) findViewById(R.id.audio_read_icon);
        this.mCheckBox = (CheckBox) findViewById(R.id.select);
        this.mContactText = (TextView) findViewById(R.id.contact_view);
        this.mFailedIndicator = (ImageView) findViewById(R.id.failed_indicator);
        this.mFtGroupMsgListItem = (RcsFileTransGroupMessageListItem) findViewById(R.id.rcsFtGroupMsgListItem);
        this.mCancleCountView = (TextView) findViewById(R.id.status_cancle_timer);
        this.mBodyTextView.setSpandTouchMonitor(this);
        this.mDateView.setOnClickListener(null);
        this.mTextTimePhase = (TextView) findViewById(R.id.time_phase);
        this.mTextViewYear = (TextView) findViewById(R.id.textview_year);
    }

    private void updateGroupMessageStatus(RcsFileTransGroupMessageItem groupItem) {
        if (groupItem == null) {
            MLog.d("GroupMessageListItem", "update file trans group message item,groupItem is null");
        } else if (this.mStatusView == null || this.mFailedIndicator == null) {
            MLog.d("GroupMessageListItem", "update file trans group message item,mStatuView or mFailedIndicator is null");
        } else if (this.mIsDelayMsg) {
            if (this.mFailedIndicator.getVisibility() == 0) {
                this.mFailedIndicator.setVisibility(8);
            }
        } else {
            if (groupItem.mImAttachmentStatus == 1001) {
                setMessageStatusTextAndColor(R.string.send_failed);
                this.mStatusView.setVisibility(0);
                this.mFailedIndicator.setVisibility(0);
            } else if (groupItem.mImAttachmentStatus == 1010) {
                this.mFailedIndicator.setVisibility(0);
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.status_canceled);
            } else if (groupItem.mImAttachmentStatus == 1009) {
                this.mFailedIndicator.setVisibility(8);
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.status_reject);
            } else if (groupItem.mImAttachmentStatus == 1000 || groupItem.mImAttachmentStatus == 1007) {
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.message_status_sending);
                this.mFailedIndicator.setVisibility(8);
            } else if (groupItem.mImAttachmentStatus == 1002) {
                setMessageStatusGone();
                this.mFailedIndicator.setVisibility(8);
                if (this.mAudioIcon != null && groupItem.isAudioFileType()) {
                    this.mAudioIcon.setVisibility(0);
                }
            } else if (groupItem.mImAttachmentStatus == 1003) {
                this.mStatusView.setVisibility(0);
                setMessageStatusTextAndColor(R.string.message_status_delivered);
                this.mFailedIndicator.setVisibility(8);
            } else {
                setMessageStatusGone();
                this.mFailedIndicator.setVisibility(8);
            }
        }
    }

    private void updateGroupMessageStatus(RcsGroupChatMessageItem groupItem) {
        if (groupItem == null || this.mStatusView == null || this.mFailedIndicator == null) {
            MLog.d("GroupMessageListItem", "updateGroupMessageStatus(),groupItem,mStatusView or mFailedIndicator is null");
            return;
        }
        if (groupItem.mDeliveryStatus != null) {
            MLog.i("GroupMessageListItem FileTrans: ", "updateGroupMessageStatus :" + groupItem.mType);
            if (this.mAudioIcon != null) {
                this.mAudioIcon.setVisibility(8);
            }
            if (groupItem.mType == 4 || groupItem.mType == 100) {
                if (groupItem.mDelivered) {
                    if (groupItem.mType == 4) {
                        this.mStatusView.setVisibility(0);
                        this.mFailedIndicator.setVisibility(8);
                    }
                    setMessageStatusTextAndColor(R.string.message_status_delivered);
                } else if (DeliveryStatus.NONE != groupItem.mDeliveryStatus) {
                    if (DeliveryStatus.FAILED == groupItem.mDeliveryStatus) {
                        if (groupItem.mType == 4) {
                            this.mStatusView.setVisibility(0);
                            this.mFailedIndicator.setVisibility(0);
                        }
                        setMessageStatusTextAndColor(R.string.send_failed);
                    } else if (DeliveryStatus.PENDING == groupItem.mDeliveryStatus) {
                        if (groupItem.mType == 4) {
                            this.mStatusView.setVisibility(0);
                            this.mFailedIndicator.setVisibility(8);
                        }
                        if (!this.mIsDelayMsg) {
                            setMessageStatusTextAndColor(R.string.message_status_sending);
                        }
                    } else if (DeliveryStatus.RECEIVED == groupItem.mDeliveryStatus && groupItem.mType == 4) {
                        setMessageStatusGone();
                        this.mFailedIndicator.setVisibility(8);
                    }
                }
            } else if (groupItem.mType == 1) {
                this.mStatusView.setVisibility(0);
                this.mFailedIndicator.setVisibility(8);
            }
        }
    }

    private void updateContactNameText(String addr) {
        this.mContactText.setVisibility(0);
        final Contact contact = Contact.get(NumberUtils.normalizeNumber(addr), true);
        if (contact != null && contact.existsInDatabase()) {
            this.mContactText.setText(contact.getName());
            this.mContactText.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    try {
                        Intent intent = new Intent("android.intent.action.VIEW", contact.getUri());
                        intent.setFlags(524288);
                        intent.putExtra("phoneNumber", contact.getNumber());
                        RcsGroupChatMessageListItem.this.mContext.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Log.e("GroupMessageListItem", "contact detail activity not found >>> " + ex);
                        QuickContact.showQuickContact(RcsGroupChatMessageListItem.this.getContext(), RcsGroupChatMessageListItem.this, contact.getUri(), 3, new String[]{"vnd.android.cursor.item/contact"});
                    }
                }
            });
        } else if (RcsProfile.isGroupChatNicknameEnabled()) {
            setContactNickname(addr);
        } else {
            setContactPhoneNum(addr);
        }
    }

    private void setContactNickname(final String addr) {
        final String name = RcsProfile.getGroupMemberNickname(addr, (long) this.mMessageItem.mThreadId);
        if (TextUtils.isEmpty(name)) {
            setContactPhoneNum(addr);
            return;
        }
        this.mContactText.setText(name);
        this.mContactText.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                RcsProfile.startContactDetailActivityFromGroupChat(name, addr, RcsGroupChatMessageListItem.this.mContext);
            }
        });
    }

    private void setContactPhoneNum(final String addr) {
        String vAddr = addr;
        this.mContactText.setText(addr);
        this.mContactText.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                RcsProfile.startContactDetailActivityFromGroupChat(addr, addr, RcsGroupChatMessageListItem.this.mContext);
            }
        });
    }

    private void setChatBodyMaxWidth() {
        int maxWidth = ((((MessageUtils.getWindowWidthPixels(getResources()) - ((int) getResources().getDimension(R.dimen.listview_padding_right))) - ((int) getResources().getDimension(R.dimen.listview_status_padding))) - ((int) getResources().getDimension(R.dimen.checkbox_width))) - ((int) getResources().getDimension(R.dimen.time_axis_width))) - ((int) getResources().getDimension(R.dimen.status_padding_bubble_width));
        if (MmsConfig.isExtraHugeEnabled(getResources().getConfiguration().fontScale)) {
            maxWidth -= 35;
        }
        if (!DateFormat.is24HourFormat(getContext())) {
            maxWidth -= (int) getResources().getDimension(R.dimen.message_time_format24_dec);
        }
        if (this.mIsMultiChoice) {
            int width = ((maxWidth - ((int) getResources().getDimension(R.dimen.checkbox_width))) - ((int) getResources().getDimension(R.dimen.rcs_group_chat_message_checkbox_paddingLeft))) - ((int) getResources().getDimension(R.dimen.rcs_group_chat_message_checkbox_paddingRight));
            this.mBodyTextView.setMaxWidth(width);
            if (this.mContactText != null) {
                this.mContactText.setMaxWidth(width);
                return;
            }
            return;
        }
        this.mBodyTextView.setMaxWidth(maxWidth);
        if (this.mContactText != null) {
            this.mContactText.setMaxWidth(maxWidth);
        }
    }

    public void onTouchOutsideSpanText() {
    }

    public void onSpanTextPressed(boolean pressed) {
        if (this.mMessageBlock != null) {
            this.mMessageBlock.setPressed(pressed);
        }
    }

    public void onTouchLink(ClickableSpan span) {
    }

    public boolean isEditTextClickable() {
        return !this.mIsMultiChoice;
    }

    protected int getContentResId() {
        return R.id.mms_layout_view_super_parent;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isEditAble()) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEditAble()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.failed_indicator:
                if (this.mGroupFrg != null) {
                    this.mGroupFrg.reSend(this.mMessageItem);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public boolean isEditAble() {
        return this.mCheckBox != null && this.mCheckBox.getVisibility() == 0;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBox != null) {
            setChecked(true, checked);
            refreshDrawableState();
        }
    }

    public void setChecked(boolean isMultiChoice, boolean checked) {
        this.mIsMultiChoice = isMultiChoice;
        if (this.mIsMultiChoice) {
            this.mCheckBox.setChecked(checked);
        }
    }

    public void toggle() {
    }

    public void setEditAble(boolean editable) {
        if (this.mCheckBox != null) {
            if (editable) {
                this.mCheckBox.setVisibility(0);
                this.mMessageBlock.setLongClickable(false);
                setEnabled(true);
            } else {
                this.mCheckBox.setVisibility(8);
                this.mMessageBlock.setLongClickable(true);
                setEnabled(false);
            }
        }
    }

    public void setEditAble(boolean editable, boolean checked) {
        MLog.d("GroupMessageListItem", "setEditAble group");
        if (!editable) {
            checked = false;
        }
        setChecked(checked);
        setEditAble(editable);
    }

    public boolean onLongClick(View v) {
        return false;
    }

    public boolean onDoubleTapUp(boolean isLink) {
        if (DelaySendManager.getInst().isDelayMsg(this.mMessageItem.getCancelId(), this.mMessageItem.getMessageType())) {
            DelaySendManager.getInst().setDelayMsgCanceled(this.mMessageItem.getCancelId(), this.mMessageItem.getMessageType(), false);
            sendMessage(this.mMessageItem, 1000101);
        } else if (!HwMessageUtils.isSplitOn()) {
            MessageUtils.viewRcsMessageText(this.mContext, this.mMessageItem, null);
        } else if (((Activity) this.mContext) instanceof ConversationList) {
            MessageUtils.viewRcsMessageText(this.mContext, this.mMessageItem, ((ConversationList) this.mContext).getRightFragment());
        }
        return false;
    }

    public void setTextScale(float scale) {
        this.mFontScale = scale;
        float init_fontsize = HwUiStyleUtils.getPopMessageFontSize(getResources());
        if (MmsConfig.isEnableZoomWhenView()) {
            this.mBodyTextView.setTextSize(init_fontsize * scale);
        }
    }

    private void checkDelayMsg() {
        if (this.mMessageItem.isNotDelayMsg() && !this.mIsDelayMsg) {
            setNotDelayMsg();
        } else if (DelaySendManager.getInst().getCancelMsgStatus(this.mMessageItem.getCancelId(), this.mMessageItem.getMessageType(), false) == 0) {
            setNotDelayMsg();
            if (System.currentTimeMillis() - this.mMessageItem.mDate < 6000) {
                createCancelUpdate();
                DelaySendManager.getInst().addUIUpdate(this.mMessageItem.getCancelId(), this.mMessageItem.getMessageType(), false, this.mCancelUpdate);
            }
        } else {
            createCancelUpdate();
            DelaySendManager.getInst().registerUiUpdate(this.mMessageItem.getCancelId(), this.mMessageItem.getMessageType(), false, this.mCancelUpdate);
        }
    }

    private void setNotDelayMsg() {
        this.mIsDelayMsg = false;
        if (this.mMessageItem.mFtGroupMsgItem != null) {
            this.mMessageItem.mFtGroupMsgItem.setMsgDelay(false);
        }
        if (this.mCancleCountView.getVisibility() == 0) {
            this.mCancleCountView.setVisibility(8);
        }
        if (this.mBodyTextView != null) {
            this.mBodyTextView.setIsClickIntercepted(false);
        }
    }

    private void createCancelUpdate() {
        if (this.mCancelUpdate == null) {
            this.mCancelUpdate = new UpdateCallback() {
                public void onUpdate(long count, long id, String msgType) {
                    boolean isSameItem;
                    RcsGroupChatMessageListItem.this.mIsDelayMsg = true;
                    if (RcsGroupChatMessageListItem.this.mBodyTextView != null) {
                        RcsGroupChatMessageListItem.this.mBodyTextView.setIsClickIntercepted(true);
                    }
                    if (RcsGroupChatMessageListItem.this.mMessageItem.mFtGroupMsgItem != null) {
                        RcsGroupChatMessageListItem.this.mMessageItem.mFtGroupMsgItem.setMsgDelay(true);
                    }
                    if (id == RcsGroupChatMessageListItem.this.mMessageItem.getCancelId()) {
                        isSameItem = msgType.equals(RcsGroupChatMessageListItem.this.mMessageItem.getMessageType());
                    } else {
                        isSameItem = false;
                    }
                    if (!isSameItem) {
                        return;
                    }
                    if (count == 0) {
                        RcsGroupChatMessageListItem.this.mCancleCountView.setVisibility(8);
                        RcsGroupChatMessageListItem.this.setMessageStatusTextAndColor(R.string.message_status_sending);
                        RcsGroupChatMessageListItem.this.mIsDelayMsg = false;
                        if (RcsGroupChatMessageListItem.this.mBodyTextView != null) {
                            RcsGroupChatMessageListItem.this.mBodyTextView.setIsClickIntercepted(false);
                        }
                        if (RcsGroupChatMessageListItem.this.mMessageItem.mFtGroupMsgItem != null) {
                            RcsGroupChatMessageListItem.this.mMessageItem.mFtGroupMsgItem.setMsgDelay(false);
                        }
                        return;
                    }
                    if (RcsGroupChatMessageListItem.this.mCancleCountView.getVisibility() == 8) {
                        RcsGroupChatMessageListItem.this.mCancleCountView.setVisibility(0);
                    }
                    if (RcsGroupChatMessageListItem.this.mStatusView.getVisibility() == 8) {
                        RcsGroupChatMessageListItem.this.mStatusView.setVisibility(0);
                    }
                    RcsGroupChatMessageListItem.this.setMessageStatusTextAndColor(R.string.mms_cancel_send_status);
                    RcsGroupChatMessageListItem.this.mCancleCountView.setText(String.valueOf(count));
                }
            };
        }
    }

    public void setMsgListItemHandler(Handler handler) {
        this.mHandler = handler;
        if (this.mFtGroupMsgListItem != null) {
            this.mFtGroupMsgListItem.setGroupMsgListItemCallback(new GroupChatMsgListItemCallback());
        }
    }

    protected void sendMessage(RcsGroupChatMessageItem messageItem, int message) {
        if (this.mHandler != null) {
            Message msg = Message.obtain(this.mHandler, message);
            msg.obj = messageItem;
            msg.sendToTarget();
        }
    }

    public void setFragment(RcsGroupChatComposeMessageFragment gccmf) {
        this.mGroupFrg = gccmf;
    }

    public void setNeedShowTimePhase(boolean needShowTimePhase) {
        this.mNeedShowTimePhase = needShowTimePhase;
    }

    public boolean isNeedShowTimePhase() {
        return this.mNeedShowTimePhase;
    }

    private void setMessageStatusTextAndColor(int resId) {
        if (this.mStatusView != null) {
            switch (resId) {
                case R.string.send_failed:
                case R.string.receive_fail:
                    this.mStatusView.setTextColor(ResEx.self().getConvItemErrorMsgTextColor());
                    hideUnderPopViewVisible(false);
                    break;
                case R.string.message_status_sending:
                case R.string.mms_cancel_send_status:
                    this.mStatusView.setTextColor(ResEx.self().getMsgItemUnderPopColor());
                    hideUnderPopViewVisible(true);
                    break;
                default:
                    this.mStatusView.setTextColor(ResEx.self().getMsgItemUnderPopColor());
                    hideUnderPopViewVisible(false);
                    break;
            }
            this.mStatusView.setText(this.mContext.getString(resId));
        }
    }

    public void setMessageStatusGone() {
        hideUnderPopViewVisible(false);
        this.mStatusView.setVisibility(8);
    }

    public void hideUnderPopViewVisible(boolean hideUnderPopView) {
        this.mDateView.setVisibility(!hideUnderPopView ? 0 : 8);
    }

    public View getMessageBlockSuper() {
        return this.mMessageBlock;
    }
}
