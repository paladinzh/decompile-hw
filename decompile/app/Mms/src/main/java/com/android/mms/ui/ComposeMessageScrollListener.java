package com.android.mms.ui;

import android.widget.AbsListView;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageUtil;

public class ComposeMessageScrollListener {
    private MessageListAdapter mListAdapter;
    private int mVisibleItemCount = 0;

    public ComposeMessageScrollListener(MessageListAdapter adapter) {
        this.mListAdapter = adapter;
    }

    public void onScroll(int visibleItemCount) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mVisibleItemCount = visibleItemCount;
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && this.mListAdapter != null) {
            CryptoMessageListAdapter listAdapterCust = this.mListAdapter.getCryptoMessageListAdapter();
            if (listAdapterCust != null) {
                switch (scrollState) {
                    case 0:
                        listAdapterCust.setScroll(false);
                        for (int i = 0; i < this.mVisibleItemCount; i++) {
                            MessageListItem listItem = (MessageListItem) view.getChildAt(i);
                            if (listItem == null) {
                                MLog.d("ComposeMessageScrollListener", "onScrollStateChanged: listItem is null");
                            } else {
                                MessageItem item = listItem.getMessageItem();
                                if (item == null) {
                                    MLog.d("ComposeMessageScrollListener", "onScrollStateChanged: item is null");
                                } else {
                                    long messageId = item.getMessageId();
                                    if (item.isSms()) {
                                        CryptoMessageItem itemCust = item.getCryptoMessageItem();
                                        if (itemCust == null) {
                                            MLog.d("ComposeMessageScrollListener", "onScrollStateChanged: itemCust is null");
                                        } else {
                                            int eType = itemCust.getEncryptSmsType();
                                            if (eType != 0 && listAdapterCust.getEncryptSms(Long.valueOf(messageId)) == null) {
                                                boolean couldDisplay;
                                                MLog.d("ComposeMessageScrollListener", "onScrollStateChanged: messageId=" + messageId + ", eType=" + eType);
                                                long markedMessageId = listAdapterCust.getMarkedMessageId();
                                                if ((1 == eType && CryptoMessageUtil.couldDecryptSmsForLocal(item, markedMessageId)) || (3 == eType && CryptoMessageUtil.couldDecryptSmsForLsne(item, markedMessageId))) {
                                                    couldDisplay = true;
                                                } else if (2 == eType) {
                                                    couldDisplay = CryptoMessageUtil.couldDecryptSmsForNetwork(item, markedMessageId);
                                                } else {
                                                    couldDisplay = false;
                                                }
                                                if (couldDisplay) {
                                                    listAdapterCust.pushEncryptoSms(messageId, item.getMessageSummary(), eType, 0);
                                                }
                                            }
                                        }
                                    } else {
                                        MLog.d("ComposeMessageScrollListener", "onScrollStateChanged: it is not a sms");
                                    }
                                }
                            }
                        }
                        break;
                    case 2:
                        listAdapterCust.setScroll(true);
                        break;
                }
            }
        }
    }
}
