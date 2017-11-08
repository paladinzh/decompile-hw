package com.android.rcs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.util.HashMap;

public class RcsMessageListItem {
    static final String[] MMS_REPORT_REQUEST_PROJECTION = new String[]{"address", "d_rpt", "rr"};
    static final String[] MMS_REPORT_STATUS_PROJECTION = new String[]{"address", "delivery_status", "read_status"};
    private IHwCustMessageListItemCallback mCallback;
    private Context mContext;
    private boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private LinearLayout mLocLayout;
    private ImageView mLocationImg;
    private TextView mLocationSubTv;
    private TextView mLocationTv;

    public interface IHwCustMessageListItemCallback {
        boolean isDelayMessage();

        void setBodyTextViewVisibility(int i);

        void setDelayMessageStatus(boolean z);
    }

    public RcsMessageListItem(Context context) {
        this.mContext = context;
    }

    public boolean isRcsSwitchOn() {
        return this.mIsRcsOn;
    }

    public boolean isSameItem(MessageItem oldMsgItem, MessageItem newMsgItem, boolean isSameItem) {
        if (!this.mIsRcsOn || oldMsgItem == null || newMsgItem == null) {
            return isSameItem;
        }
        if (oldMsgItem.mType != null) {
            isSameItem = isSameItem ? oldMsgItem.mType.equals(newMsgItem.mType) : false;
        }
        return isSameItem;
    }

    public boolean bindInCust(MessageItem messageItem, View messageBlock) {
        if (!this.mIsRcsOn) {
            return false;
        }
        if (!(messageItem.getBoxId() == 1 || messageItem.getBoxId() == 0)) {
            if (messageItem.isRcsChat()) {
                messageBlock.setBackgroundResource(R.drawable.message_pop_rcs_send_bg);
            } else {
                messageBlock.setBackgroundResource(R.drawable.message_pop_send_bg);
            }
        }
        return true;
    }

    public void onResendClick(MessageItem messageItem, ImageView resendBtn) {
        if (this.mIsRcsOn) {
            if (messageItem.getRcsMessageItem() == null || !messageItem.isRcsChat() || !messageItem.getRcsMessageItem().isUndeliveredIm()) {
                boolean isLocItem = false;
                if (messageItem.getRcsMessageItem().mRcsMsgExtType == 6) {
                    isLocItem = true;
                }
                if (!isLocItem) {
                    chooseResendMode(messageItem, resendBtn);
                } else if (RcsProfile.isResendImAvailable(messageItem.mAddress)) {
                    RcsTransaction.resendLocationMessage(messageItem.mMsgId, messageItem.mAddress);
                    resendBtn.setVisibility(8);
                } else {
                    ResEx.makeToast((int) R.string.rcs_im_resend_error_message, 0);
                }
            } else if (RcsTransaction.isShowUndeliveredIcon()) {
                AlertDialog d = undeliveredResendDialog(messageItem);
                d.show();
                d.getButton(-3).setVisibility(8);
            }
        }
    }

    public boolean setStatusText(MessageItem messageItem, TextView messageStatus, ImageView failedIndicator) {
        if (!this.mIsRcsOn || messageStatus == null || failedIndicator == null) {
            return false;
        }
        if (messageItem.getRcsMessageItem() != null && messageItem.isRcsChat() && messageItem.getRcsMessageItem().isUndeliveredIm()) {
            return true;
        }
        return messageItem.getRcsMessageItem() != null && messageItem.isRcsChat() && messageItem.getRcsMessageItem().isReadIm();
    }

    private void chooseResendMode(final MessageItem messageItem, final ImageView resendBtn) {
        new Builder(this.mContext).setTitle(R.string.im_resend).setItems(new String[]{this.mContext.getString(R.string.rcs_resend_by_text_message), this.mContext.getString(R.string.rcs_resend_by_chat_message)}, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (RcsMessageListItem.this.mContext instanceof Activity) {
                            ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((Activity) RcsMessageListItem.this.mContext, "Mms_UI_CMF");
                            if (!(fragment == null || fragment.getRcsComposeMessage() == null)) {
                                fragment.getRcsComposeMessage().reSendImBySms(messageItem);
                            }
                        }
                        resendBtn.setVisibility(8);
                        return;
                    case 1:
                        if (RcsProfile.isResendImAvailable(messageItem.mAddress)) {
                            RcsTransaction.resendExtMessage(messageItem.mMsgId, messageItem.mAddress, RcsMessageListItem.this.mContext);
                            resendBtn.setVisibility(8);
                            return;
                        }
                        ResEx.makeToast((int) R.string.rcs_im_resend_error_message, 0);
                        return;
                    default:
                        return;
                }
            }
        }).create().show();
    }

    public boolean isFTMsgItem(MessageItem msgItem) {
        return this.mIsRcsOn ? msgItem instanceof RcsFileTransMessageItem : false;
    }

    public boolean setMsgStatus(MessageItem mMessageItem) {
        if (!this.mIsRcsOn || !(mMessageItem instanceof RcsFileTransMessageItem)) {
            return false;
        }
        if (((RcsFileTransMessageItem) mMessageItem).mIsOutgoing || 1001 == ((RcsFileTransMessageItem) mMessageItem).mImAttachmentStatus || 1010 == ((RcsFileTransMessageItem) mMessageItem).mImAttachmentStatus || 1009 == ((RcsFileTransMessageItem) mMessageItem).mImAttachmentStatus) {
            return true;
        }
        return 1002 == ((RcsFileTransMessageItem) mMessageItem).mImAttachmentStatus && ((RcsFileTransMessageItem) mMessageItem).isAudioFileType();
    }

    public boolean initFailedIndicator(ImageView failedIndicator) {
        if (!this.mIsRcsOn || failedIndicator == null) {
            return false;
        }
        failedIndicator.setImageResource(R.drawable.ic_alert_sms_failed);
        failedIndicator.setClickable(true);
        return true;
    }

    private AlertDialog undeliveredResendDialog(final MessageItem messageItem) {
        OnClickListener dialogListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -2:
                        if (RcsMessageListItem.this.mContext instanceof Activity) {
                            ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((Activity) RcsMessageListItem.this.mContext, "Mms_UI_CMF");
                            if (!(fragment == null || fragment.getRcsComposeMessage() == null)) {
                                fragment.getRcsComposeMessage().sendImBySms(messageItem);
                            }
                        }
                        RcsComposeMessage.markAsSent(messageItem, RcsMessageListItem.this.mContext, false);
                        return;
                    case -1:
                        RcsComposeMessage.markAsSent(messageItem, RcsMessageListItem.this.mContext, false);
                        return;
                    default:
                        return;
                }
            }
        };
        return new Builder(this.mContext).setTitle(R.string.message_status_undelivered).setMessage(R.string.undelivered_message).setPositiveButton(R.string.undelivered_wait_btn, dialogListener).setNegativeButton(R.string.undelivered_resend_btn, dialogListener).setNeutralButton(R.string.undelivered_wait_btn, null).create();
    }

    public void bindCommonMessage(SpandTextView mBodyTextView, MessageListItem listItem) {
        if (this.mIsRcsOn) {
            MessageItem messageItem = listItem.getMessageItem();
            long boxId = (long) messageItem.getBoxId();
            if (messageItem.getRcsMessageItem().mRcsMsgExtType == 6) {
                ViewStub favLocReListItem = (ViewStub) listItem.findViewById(R.id.rcs_loc_list_item);
                if (favLocReListItem != null) {
                    favLocReListItem.setLayoutResource(R.layout.rcs_item_chat_received_location);
                    favLocReListItem.inflate();
                }
                this.mLocationTv = (TextView) listItem.findViewById(R.id.location_attach_title);
                if (this.mLocationTv != null) {
                    this.mLocationTv.setVisibility(8);
                }
                this.mLocLayout = (LinearLayout) listItem.findViewById(R.id.loc_layout_view);
                this.mLocationSubTv = (TextView) listItem.findViewById(R.id.location_attach_subtitle);
                this.mLocationImg = (ImageView) listItem.findViewById(R.id.location_img);
                if (this.mLocationSubTv != null) {
                    final HashMap<String, String> locInfo = RcsMapLoader.getLocInfo(mBodyTextView.getText().toString());
                    mBodyTextView.setVisibility(8);
                    this.mLocLayout.setVisibility(0);
                    this.mLocationSubTv.setVisibility(0);
                    this.mLocationImg.setVisibility(0);
                    this.mLocationSubTv.setText((CharSequence) locInfo.get("subtitle"));
                    new MmsClickListener(new IMmsClickListener() {
                        public void onSingleClick(View view) {
                            RcsMapLoaderFactory.getMapLoader(RcsMessageListItem.this.mContext).loadMap(RcsMessageListItem.this.mContext, locInfo);
                        }

                        public void onDoubleClick(View view) {
                        }
                    }).setClickListener(this.mLocLayout);
                    this.mLocLayout.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            RcsMessageListItem.this.dismiss();
                            return false;
                        }
                    });
                    if (!(boxId == 1 || boxId == 0)) {
                        this.mLocationSubTv.setTextColor(this.mContext.getResources().getColor(R.color.text_color_white_important));
                        this.mLocationImg.setImageResource(R.drawable.rcs_map_item_big_send);
                    }
                }
            } else {
                mBodyTextView.setVisibility(0);
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
        }
    }

    private void dismiss() {
        MLog.d("HwCustMessageListItemImpl", "dismiss for findbugs");
    }

    public void setHwCustCallback(IHwCustMessageListItemCallback callback) {
        this.mCallback = callback;
    }

    public IHwCustMessageListItemCallback getHwCustCallback() {
        return this.mCallback;
    }
}
