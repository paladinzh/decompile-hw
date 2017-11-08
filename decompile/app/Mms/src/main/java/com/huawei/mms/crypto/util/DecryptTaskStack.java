package com.huawei.mms.crypto.util;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class DecryptTaskStack {
    private boolean isRunning = true;
    private final LinkedList<DecryptData> mDecryptDataList;
    private volatile boolean mRefreshUI;
    private Thread mWorkerThread;
    private int maxTaskSize = 10;
    private Handler refreshHandler;

    public DecryptTaskStack(int maxSize, Handler refreshHandler) {
        this.maxTaskSize = maxSize;
        this.refreshHandler = refreshHandler;
        this.mDecryptDataList = new LinkedList();
        this.mRefreshUI = false;
        this.mWorkerThread = new Thread(new Runnable() {
            public void run() {
                while (DecryptTaskStack.this.isRunning) {
                    DecryptData decryptData = null;
                    synchronized (DecryptTaskStack.this.mDecryptDataList) {
                        if (DecryptTaskStack.this.mDecryptDataList.size() == 0) {
                            try {
                                DecryptTaskStack.this.mDecryptDataList.wait();
                            } catch (InterruptedException e) {
                                MLog.w("DecryptTaskStack", "run: interrupted exception happened");
                            }
                        }
                        if (DecryptTaskStack.this.mDecryptDataList.size() > 0) {
                            decryptData = (DecryptData) DecryptTaskStack.this.mDecryptDataList.removeFirst();
                        }
                    }
                    if (!DecryptTaskStack.this.isRunning) {
                        return;
                    }
                    if (decryptData != null) {
                        int eType = decryptData.getEncryptType();
                        if (eType != 0) {
                            String eText = decryptData.getMessageContent();
                            Object text = null;
                            if (1 == eType) {
                                text = CryptoMessageServiceProxy.localDecrypt(eText, true);
                            } else if (3 == eType) {
                                text = DecryptTaskStack.this.handleLSNEMessages(eText);
                            } else if (2 == eType) {
                                text = DecryptTaskStack.this.handleNetworkMessages(eText);
                            }
                            if (TextUtils.isEmpty(text)) {
                                continue;
                            } else if (DecryptTaskStack.this.isRunning) {
                                decryptData.setEncryptType(0);
                                decryptData.setMessageContent(text);
                                if (DecryptTaskStack.this.mRefreshUI) {
                                    DecryptTaskStack.this.sendMessage(decryptData, 1);
                                }
                            } else {
                                return;
                            }
                        }
                        continue;
                    }
                }
            }
        }, "DecryptTaskStack");
        this.mWorkerThread.setDaemon(true);
        this.mWorkerThread.start();
    }

    public void notifyRefreshUI(boolean on) {
        this.mRefreshUI = on;
    }

    private String handleNetworkMessages(String eText) {
        HashMap<String, Integer> map = CryptoMessageServiceProxy.networkDecryptInDB(eText, true);
        if (map == null || map.size() != 1) {
            return null;
        }
        Object result = null;
        int subId = -1;
        for (Entry<String, Integer> entry : map.entrySet()) {
            String result2 = (String) entry.getKey();
            subId = ((Integer) entry.getValue()).intValue();
        }
        if (-1 == subId || TextUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }

    private String handleLSNEMessages(String eText) {
        String text = CryptoMessageServiceProxy.decryptLocalStoredNEMsg(eText, true);
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        return text;
    }

    public void clear() {
        synchronized (this.mDecryptDataList) {
            this.mDecryptDataList.clear();
            this.mDecryptDataList.notifyAll();
        }
    }

    public void stopStackRunning() {
        this.isRunning = false;
        this.mRefreshUI = false;
        clear();
    }

    public void push(DecryptData data) {
        synchronized (this.mDecryptDataList) {
            if (this.mDecryptDataList.contains(data)) {
                this.mDecryptDataList.remove(data);
                this.mDecryptDataList.addFirst(data);
            } else if (this.mDecryptDataList.size() >= this.maxTaskSize) {
                this.mDecryptDataList.removeLast();
                this.mDecryptDataList.addFirst(data);
            } else {
                this.mDecryptDataList.addFirst(data);
            }
            this.mDecryptDataList.notifyAll();
        }
    }

    private void sendMessage(DecryptData data, int message) {
        if (this.refreshHandler != null) {
            Message msg = Message.obtain(this.refreshHandler, message);
            msg.obj = data;
            msg.sendToTarget();
        }
    }
}
