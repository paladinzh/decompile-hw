package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.AccountCheckHandler;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.ui.CryptoSpandTextView;
import com.huawei.mms.ui.CryptoSpandTextView.ClickCallback;
import com.huawei.mms.ui.MsimSmsEncryptSetting;
import com.huawei.mms.ui.SmsEncryptSetting;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.CommonGatherLinks;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.util.HashMap;

public class CryptoMessageListItem {
    private ImageView mEncryptSmsImg;
    private MessageListAdapter mListAdapter = null;

    private class SpandTextViewClickCallBack implements ClickCallback {
        private Context mLoaclContext;
        private MessageListItem mlistItem;

        public SpandTextViewClickCallBack(Context context, MessageListItem listItem) {
            this.mlistItem = listItem;
            this.mLoaclContext = context;
        }

        public void onClickDone() {
            CryptoMessageListItem.this.onMessageBodyClicked(this.mLoaclContext, this.mlistItem);
        }
    }

    private static class StartCryptoSettingListener implements OnClickListener {
        Context mLocalContext;

        public StartCryptoSettingListener(Context context) {
            this.mLocalContext = context;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            MLog.d("CryptoMessageListItem", "StartCryptoSettingListener button pressed");
            if (MessageUtils.isMultiSimEnabled()) {
                this.mLocalContext.startActivity(new Intent(this.mLocalContext, MsimSmsEncryptSetting.class));
            } else {
                this.mLocalContext.startActivity(new Intent(this.mLocalContext, SmsEncryptSetting.class));
            }
            dialog.dismiss();
        }
    }

    public void updateMsgTextForEncryptSms(MessageListItem messageListItem, String msgBody, long date) {
        if (!(!CryptoMessageUtil.isCryptoSmsEnabled() || TextUtils.isEmpty(msgBody) || messageListItem == null)) {
            MessageItem msgItem = messageListItem.getMessageItem();
            CharSequence formattedMessage = MessageUtils.formatMessage(msgBody, msgItem.mSubId, msgItem.mHighlight, msgItem.mTextContentType, messageListItem.getTextScale());
            if (!TextUtils.isEmpty(formattedMessage)) {
                messageListItem.mBodyTextView.setText(formattedMessage, CommonGatherLinks.getTextSpans(HwMessageUtils.getAddrFromTMRManager(msgBody), HwMessageUtils.getTimePosition(msgBody), msgBody, messageListItem.getContext(), msgItem.mDate));
            }
        }
    }

    public void setMsgBodyClickListener(Context context, View view1, MessageListItem listItem) {
        if (!(!CryptoMessageUtil.isCryptoSmsEnabled() || listItem == null || view1 == null)) {
            CryptoSpandTextView cryptoSpan1 = ((SpandTextView) view1).getCryptoSpandTextView();
            cryptoSpan1.setSmsType(listItem.getMessageItem().getCryptoMessageItem().getEncryptSmsType());
            cryptoSpan1.setClickCallback(new SpandTextViewClickCallBack(context, listItem));
        }
    }

    public void setAdapter(MessageListAdapter adapter) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mListAdapter = adapter;
        }
    }

    public void onMessageBodyClicked(final Context context, MessageListItem listItem) {
        if (this.mListAdapter == null) {
            MLog.d("CryptoMessageListItem", "onMessageBodyClicked: adapter is null");
            return;
        }
        CryptoMessageListAdapter adapterCust = this.mListAdapter.getCryptoMessageListAdapter();
        if (adapterCust == null) {
            MLog.d("CryptoMessageListItem", "onMessageBodyClicked: adapterCust is null");
            return;
        }
        MessageItem messageItem = listItem.getMessageItem();
        if (messageItem == null) {
            MLog.d("CryptoMessageListItem", "onMessageBodyClicked: messageItem is null");
        } else if (messageItem.isSms()) {
            String msgBody = messageItem.getMessageSummary();
            if (TextUtils.isEmpty(msgBody)) {
                MLog.d("CryptoMessageListItem", "onMessageBodyClicked: message body is empty");
                return;
            }
            CryptoMessageItem itemCust = messageItem.getCryptoMessageItem();
            if (itemCust == null) {
                MLog.d("CryptoMessageListItem", "onMessageBodyClicked: itemCust is null");
                return;
            }
            int eType = itemCust.getEncryptSmsType();
            adapterCust.notifyStackRefreshUI(true);
            if (eType != 0) {
                if (1 == eType) {
                    StatisticalHelper.incrementReportCount(context, 2196);
                    MLog.d("CryptoMessageListItem", "onMessageBodyClicked: local encrypted message was clicked");
                    final String result = CryptoMessageServiceProxy.localDecrypt(msgBody, true);
                    if (!adapterCust.couldDecryptSms(messageItem) || TextUtils.isEmpty(result)) {
                        final String account = CryptoMessageUtil.getAccountOrImsi(msgBody, 1);
                        final Handler refreshHandler = new Handler() {
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case 1:
                                        if (TextUtils.isEmpty(result)) {
                                            Toast.makeText(context, R.string.encrypted_esms_decrypt_failure_hint, 1).show();
                                            return;
                                        }
                                        CryptoMessageUtil.addAccountState(account, 1);
                                        CryptoMessageListItem.this.markMessageIdAndNotifyChange(null);
                                        return;
                                    default:
                                        MLog.d("CryptoMessageListItem", "Unknown message: " + msg.what);
                                        return;
                                }
                            }
                        };
                        AccountManager.getInstance().checkHwIDPassword(context, account, new AccountCheckHandler() {
                            public void onFinish(Bundle arg0) {
                                CryptoMessageListItem.this.sendMessage(refreshHandler, 1);
                            }

                            public void onError(ErrorStatus arg0) {
                                MLog.d("CryptoMessageListItem", "check account error!");
                            }
                        });
                    } else {
                        markMessageIdAndNotifyChange(messageItem);
                    }
                } else if (3 == eType) {
                    StatisticalHelper.incrementReportCount(context, 2196);
                    MLog.d("CryptoMessageListItem", "onMessageBodyClicked: local stored network encrypted message was clicked");
                    if (!isAnyCardPresent()) {
                        new Builder(context).setTitle(R.string.mms_remind_title).setMessage(R.string.encrypted_esms_card_absent_hint).setPositiveButton(R.string.encrypted_esms_user_know, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int buttonId) {
                                MLog.d("CryptoMessageListItem", "onMessageBodyClicked: there is not any card present");
                            }
                        }).show();
                    } else if (adapterCust.couldDecryptSms(messageItem)) {
                        Toast.makeText(context, R.string.encrypted_esms_decrypt_failure_hint, 1).show();
                    } else {
                        int reason = CryptoMessageUtil.decryptNetworkSmsFailureReason(messageItem);
                        if (reason == 0) {
                            MLog.d("CryptoMessageListItem", "error happened when there is no reason!");
                        } else if (2 == reason) {
                            MLog.d("CryptoMessageListItem", "imsi does not match");
                            new Builder(context).setTitle(R.string.mms_remind_title).setMessage(R.string.encrypted_network_card_wrong).setPositiveButton(17039370, null).show();
                        } else if (1 == reason) {
                            MLog.d("CryptoMessageListItem", "the card with the imsi is not activated");
                            new Builder(context).setTitle(R.string.mms_remind_title).setMessage(R.string.encrypted_esms_switch_on_text).setPositiveButton(R.string.esms_switch_on, new StartCryptoSettingListener(context)).setNegativeButton(R.string.no, null).show();
                        } else {
                            MLog.d("CryptoMessageListItem", "unkown failure reason ");
                        }
                    }
                } else if (2 == eType) {
                    StatisticalHelper.incrementReportCount(context, 2196);
                    MLog.d("CryptoMessageListItem", "onMessageBodyClicked: network encrypted message was clicked");
                    HashMap<String, Integer> map = CryptoMessageServiceProxy.networkDecryptInDB(msgBody, true);
                    if (map == null || map.size() != 1) {
                        Toast.makeText(context, R.string.encrypted_esms_decrypt_failure_hint, 1).show();
                    }
                }
            }
        } else {
            MLog.d("CryptoMessageListItem", "onMessageBodyClicked: it is not a sms");
        }
    }

    public void updateEncryptSmsStyle(MessageListItem messageListItem, Context context) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && messageListItem != null) {
            this.mEncryptSmsImg = createEncryptSmsImg(messageListItem);
            if (this.mEncryptSmsImg != null) {
                MessageItem mMessageItem = messageListItem.getMessageItem();
                boolean isVisible = mMessageItem.getCryptoMessageItem().isEncryptSms(mMessageItem);
                int paddingTop = messageListItem.mMessageBlock.getPaddingTop();
                int paddingBottom = messageListItem.mMessageBlock.getPaddingBottom();
                int paddingEnd = messageListItem.mMessageBlock.getPaddingEnd();
                int paddingStart = messageListItem.mMessageBlock.getPaddingStart();
                if (isVisible) {
                    this.mEncryptSmsImg.setVisibility(0);
                    if (messageListItem.getShowBubbleMode() == 0) {
                        if (mMessageItem.isInComingMessage()) {
                            messageListItem.mMessageBlock.setBackground(context.getDrawable(R.drawable.message_pop_incoming_bg));
                        } else {
                            messageListItem.mMessageBlock.setBackground(context.getDrawable(R.drawable.encrypted_message_pop_send_bg));
                        }
                    }
                } else {
                    this.mEncryptSmsImg.setVisibility(8);
                    if (messageListItem.getShowBubbleMode() == 0) {
                        if (mMessageItem.isInComingMessage()) {
                            messageListItem.mMessageBlock.setBackground(context.getDrawable(R.drawable.message_pop_incoming_bg));
                        } else if (mMessageItem.isRcsChat()) {
                            messageListItem.mMessageBlock.setBackground(context.getDrawable(R.drawable.message_pop_rcs_send_bg));
                        } else {
                            messageListItem.mMessageBlock.setBackground(context.getDrawable(R.drawable.message_pop_send_bg));
                        }
                    }
                }
                messageListItem.mMessageBlock.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom);
            }
        }
    }

    private ImageView createEncryptSmsImg(MessageListItem messageListItem) {
        if (messageListItem == null) {
            return null;
        }
        if (this.mEncryptSmsImg != null) {
            return this.mEncryptSmsImg;
        }
        ViewStub stub = (ViewStub) messageListItem.findViewById(R.id.encrypt_mms_image_view_stub);
        if (stub == null) {
            MLog.d("CryptoMessageListItem", "can not find encrypt_mms_image_view_stub !");
            return null;
        }
        stub.setLayoutResource(R.layout.encrypt_sms_image_view);
        return (ImageView) stub.inflate();
    }

    private void markMessageIdAndNotifyChange(MessageItem messageItem) {
        CryptoMessageListAdapter cryptoMessageListAdapter = this.mListAdapter.getCryptoMessageListAdapter();
        if (cryptoMessageListAdapter != null) {
            if (messageItem != null) {
                long markedMessageId = cryptoMessageListAdapter.getMarkedMessageId();
                if (Sms.isOutgoingFolder(messageItem.getBoxId()) && Long.MIN_VALUE != markedMessageId && messageItem.getMessageId() >= markedMessageId) {
                    return;
                }
            }
            cryptoMessageListAdapter.setMarkedMessageId(Long.MIN_VALUE);
            this.mListAdapter.notifyDataSetChanged();
            MLog.d("CryptoMessageListItem", "markMessageIdAndNotifyChange notify");
        }
    }

    private void sendMessage(Handler handler, int message) {
        if (handler != null) {
            Message.obtain(handler, message).sendToTarget();
        }
    }

    private boolean isAnyCardPresent() {
        if (MessageUtils.isMultiSimEnabled()) {
            int cardOneState = MmsApp.getDefaultTelephonyManager().getSimState(0);
            int cardTwoState = MmsApp.getDefaultTelephonyManager().getSimState(1);
            MLog.d("CryptoMessageListItem", "isAnyCardPresent: cardOneState=" + cardOneState + ", cardTwoState=" + cardTwoState);
            if (1 == cardOneState && 1 == cardTwoState) {
                return false;
            }
            if (cardOneState != 0) {
                return (cardTwoState == 0 && 1 == cardOneState) ? false : true;
            } else {
                if (1 == cardTwoState) {
                    return false;
                }
            }
        }
        int state = MmsApp.getDefaultTelephonyManager().getSimState();
        MLog.d("CryptoMessageListItem", "isAnyCardPresent: state=" + state);
        if (1 == state) {
            return false;
        }
    }
}
