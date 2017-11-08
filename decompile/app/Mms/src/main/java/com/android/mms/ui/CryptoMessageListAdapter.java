package com.android.mms.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.AbsListView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.util.DecryptData;
import com.huawei.mms.crypto.util.DecryptTaskStack;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.HwBackgroundLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class CryptoMessageListAdapter {
    private boolean isScrolling = false;
    private ComposeMessageScrollListener mComposeMessageScrollListener;
    private CryptoMessageViewListener mCryptoMessageViewListener;
    private DecryptTaskStack mDecryptTaskStack;
    private LruCache<Long, String> mEncryptSmsCache = new LruCache(1000);
    private long mMarkeMessageId = Long.MAX_VALUE;
    private HashSet<Long> mSendMsgIds = new HashSet();
    private List<String> processedNetEncryptSms = new ArrayList();

    public interface CryptoMessageViewListener {
    }

    public void putIntoEncryptSmsCache(Long key, String value) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            synchronized (this.mEncryptSmsCache) {
                if (this.mEncryptSmsCache.get(key) == null) {
                    this.mEncryptSmsCache.put(key, value);
                }
            }
        }
    }

    public void onScroll(int visibleItemCount) {
        if (this.mComposeMessageScrollListener != null) {
            this.mComposeMessageScrollListener.onScroll(visibleItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.mComposeMessageScrollListener != null) {
            this.mComposeMessageScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public String getEncryptSms(Long key) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return null;
        }
        String str;
        synchronized (this.mEncryptSmsCache) {
            str = (String) this.mEncryptSmsCache.get(key);
        }
        return str;
    }

    public void removeEncryptCache(Long key) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            synchronized (this.mEncryptSmsCache) {
                this.mEncryptSmsCache.remove(key);
            }
        }
    }

    public void cleanEncryptCache() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            synchronized (this.mEncryptSmsCache) {
                this.mDecryptTaskStack.stopStackRunning();
                this.mEncryptSmsCache.evictAll();
            }
            this.processedNetEncryptSms.clear();
        }
    }

    public boolean isScroll() {
        return this.isScrolling;
    }

    public void setScroll(boolean isScrolling) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            boolean z;
            this.isScrolling = isScrolling;
            if (isScrolling) {
                z = false;
            } else {
                z = true;
            }
            notifyStackRefreshUI(z);
        }
    }

    public void notifyStackRefreshUI(boolean on) {
        if (this.mDecryptTaskStack != null) {
            this.mDecryptTaskStack.notifyRefreshUI(on);
        }
    }

    public void setRefreshHandlerAndNewDecryptStack(Handler refreshHandler) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            if (refreshHandler == null) {
                MLog.e("CryptoMessageListAdapter", "refreshHandler is null!");
            }
            this.mDecryptTaskStack = new DecryptTaskStack(20, refreshHandler);
        }
    }

    public void pushEncryptoSms(long messageId, String messageContent, int encryptType, int subId) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && !TextUtils.isEmpty(messageContent)) {
            this.mDecryptTaskStack.push(new DecryptData(messageId, messageContent, encryptType, subId));
        }
    }

    public void dealwithEncryptoSms(Context context, MessageListItem messageListItem, long date, boolean isLast) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            CryptoMessageListItem listItemCust = messageListItem.getCryptoMessageListItem();
            if (listItemCust != null) {
                listItemCust.updateEncryptSmsStyle(messageListItem, context);
                MessageItem item = messageListItem.getMessageItem();
                if (item.isSms()) {
                    CryptoMessageItem itemCust = item.getCryptoMessageItem();
                    if (itemCust != null) {
                        int eType = itemCust.getEncryptSmsType();
                        if (eType != 0) {
                            long messageId = item.getMessageId();
                            String str = null;
                            String eText = item.getMessageSummary();
                            if (1 == eType && CryptoMessageUtil.couldDecryptSmsForLocal(item, this.mMarkeMessageId)) {
                                String text = (String) this.mEncryptSmsCache.get(Long.valueOf(messageId));
                                if (!TextUtils.isEmpty(text)) {
                                    str = text;
                                } else if (!isScroll()) {
                                    if (isLast) {
                                        str = CryptoMessageServiceProxy.localDecrypt(eText, true);
                                        if (!(TextUtils.isEmpty(str) || MessageListItem.getMsgItemCancled())) {
                                            putIntoEncryptSmsCache(Long.valueOf(messageId), str);
                                        }
                                    } else {
                                        pushEncryptoSms(messageId, eText, eType, 0);
                                    }
                                }
                            } else if (3 == eType && CryptoMessageUtil.couldDecryptSmsForLsne(item, this.mMarkeMessageId)) {
                                if (!this.processedNetEncryptSms.contains(String.valueOf(messageId))) {
                                    this.processedNetEncryptSms.add(String.valueOf(messageId));
                                    changeNetEncryptToLocalAndStore(context, item);
                                }
                            } else if (2 == eType && CryptoMessageUtil.couldDecryptSmsForNetwork(item, this.mMarkeMessageId) && !this.processedNetEncryptSms.contains(String.valueOf(messageId))) {
                                this.processedNetEncryptSms.add(String.valueOf(messageId));
                                changeNetEncryptToLocalAndStore(context, item);
                            }
                            if (TextUtils.isEmpty(str)) {
                                str = context.getString(R.string.mms_encrypt_sms_default_display);
                            }
                            if (MessageUtils.isNeedLayoutRtl()) {
                                str = "‏" + str;
                            }
                            listItemCust.updateMsgTextForEncryptSms(messageListItem, str, date);
                        }
                    }
                }
            }
        }
    }

    public boolean couldDecryptSms(MessageItem item) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return false;
        }
        int encryptionType = item.getCryptoMessageItem().getEncryptSmsType();
        boolean couldDecryptSms = false;
        long msgId = item.getMessageId();
        if (DelaySendManager.getInst().isDelayMsg(msgId, "sms")) {
            this.mSendMsgIds.add(Long.valueOf(item.getMessageId()));
            return true;
        } else if (this.mSendMsgIds.contains(Long.valueOf(msgId))) {
            return true;
        } else {
            if (encryptionType == 0) {
                couldDecryptSms = false;
            } else if (1 == encryptionType) {
                couldDecryptSms = CryptoMessageUtil.couldDecryptSmsForLocal(item, this.mMarkeMessageId);
            } else if (2 == encryptionType) {
                couldDecryptSms = CryptoMessageUtil.couldDecryptSmsForNetwork(item, this.mMarkeMessageId);
            } else if (3 == encryptionType) {
                couldDecryptSms = CryptoMessageUtil.couldDecryptSmsForLsne(item, this.mMarkeMessageId);
            }
            MLog.d("CryptoMessageListAdapter", "couldDecryptSms: encryptionType=" + encryptionType + ", couldDecryptSms=" + couldDecryptSms);
            return couldDecryptSms;
        }
    }

    private void changeNetEncryptToLocalAndStore(final Context context, final MessageItem item) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                CryptoMessageItem itemCust = item.getCryptoMessageItem();
                if (itemCust == null) {
                    MLog.d("CryptoMessageListAdapter", "run: itemCust is null");
                    return;
                }
                String eBody = item.getMessageSummary();
                long messageId = item.getMessageId();
                int eType = itemCust.getEncryptSmsType();
                Object eText = null;
                String str = null;
                int subId;
                if (3 == eType) {
                    str = CryptoMessageServiceProxy.decryptLocalStoredNEMsg(eBody, true);
                    if (TextUtils.isEmpty(str)) {
                        MLog.d("CryptoMessageListAdapter", "run: decrypt failure for LSNE message ");
                        return;
                    }
                    subId = CryptoMessageUtil.getSubIDByImsi(CryptoMessageServiceProxy.getImsiFromLSNEMsg(eBody, true));
                    if (-1 == subId) {
                        MLog.d("CryptoMessageListAdapter", "run: sub id is invalid for LSNE message ");
                        return;
                    }
                    eText = CryptoMessageServiceProxy.localEncrypt(str, subId);
                } else if (2 == eType) {
                    HashMap<String, Integer> map = CryptoMessageServiceProxy.networkDecryptInDB(eBody, true);
                    if (map == null || map.size() != 1) {
                        MLog.d("CryptoMessageListAdapter", "run: decrypt failure for network message");
                        return;
                    }
                    subId = -1;
                    for (Entry<String, Integer> entry : map.entrySet()) {
                        str = (String) entry.getKey();
                        subId = ((Integer) entry.getValue()).intValue();
                    }
                    if (-1 == subId || TextUtils.isEmpty(str)) {
                        MLog.d("CryptoMessageListAdapter", "run: decrypted failure for network message");
                        return;
                    }
                    eText = CryptoMessageServiceProxy.localEncrypt(str, subId);
                }
                if (TextUtils.isEmpty(eText)) {
                    MLog.d("CryptoMessageListAdapter", "run: encrypted the message failed");
                    return;
                }
                CryptoMessageListAdapter.this.putIntoEncryptSmsCache(Long.valueOf(messageId), str);
                ContentValues values = new ContentValues(1);
                values.put("body", eText);
                SqliteWrapper.update(context, ContentUris.withAppendedId(Sms.CONTENT_URI, messageId), values, null, null);
            }
        });
    }

    public void setMarkedMessageId(long msgId) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            MLog.d("CryptoMessageListAdapter", "setMarkedMessageId: msgId=" + msgId);
            if (Long.MIN_VALUE != msgId && msgId < 0) {
                this.mMarkeMessageId = Long.MAX_VALUE;
            } else if (Long.MAX_VALUE == msgId || Long.MIN_VALUE == msgId) {
                this.mMarkeMessageId = msgId;
            } else if (Long.MIN_VALUE == this.mMarkeMessageId || msgId < this.mMarkeMessageId) {
                this.mMarkeMessageId = msgId;
            }
        }
    }

    public void setAdapterForMsgListItem(MessageListItem listItem, MessageListAdapter adapter) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            CryptoMessageListItem cryptoItem = listItem.getCryptoMessageListItem();
            if (cryptoItem != null) {
                cryptoItem.setAdapter(adapter);
            }
        }
    }

    public long getMarkedMessageId() {
        MLog.d("CryptoMessageListAdapter", "getMarkedMessageId: mMarkeMessageId=" + this.mMarkeMessageId);
        return this.mMarkeMessageId;
    }

    public void clearSendIDCache() {
        this.mSendMsgIds.clear();
    }

    public void setmCryptoMessageViewListener(CryptoMessageViewListener mCryptoMessageViewListener) {
        this.mCryptoMessageViewListener = mCryptoMessageViewListener;
    }

    public void setmComposeMessageScrollListener(ComposeMessageScrollListener mComposeMessageScrollListener) {
        this.mComposeMessageScrollListener = mComposeMessageScrollListener;
    }
}
