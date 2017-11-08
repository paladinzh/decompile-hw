package com.huawei.mms.util;

import android.content.Context;
import android.os.Handler;
import com.android.mms.MmsApp;
import com.android.mms.transaction.SmsReceiver;
import com.android.mms.transaction.TransactionService;
import com.huawei.rcs.util.RcsDelaySendManager;
import java.util.ArrayList;
import java.util.Iterator;

public class DelaySendManager {
    private static RcsDelaySendManager sHwCust = new RcsDelaySendManager();
    private static DelaySendManager sInstance;
    private ArrayList<DelayMsg> mDelayMsgList = new ArrayList();
    private Handler mHandler = HwBackgroundLoader.getUIHandler();
    private Runnable mRunnable;
    private ArrayList<TimerUpdate> mUIUpdateList = new ArrayList();

    public interface UpdateCallback {
        void onUpdate(long j, long j2, String str);
    }

    private static class DelayMsg {
        long mId;
        boolean mIsGroup;
        String mMsgType;
        int mStatus = 1;
        long mTime = 0;

        DelayMsg(long id, String msgType, boolean isGroup) {
            this.mId = id;
            this.mMsgType = msgType;
            this.mIsGroup = isGroup;
        }

        public boolean isSameMsgItem(long id, String msgType, boolean isGroup) {
            return this.mId == id && this.mMsgType.equals(msgType) && (this.mIsGroup == isGroup || id < 0);
        }
    }

    private static class TimerUpdate {
        UpdateCallback mCallback;
        long mId;
        boolean mIsGroup;
        String mMsgType;
        boolean mNeedUpdate;
        long mTime = 6000;

        public TimerUpdate(long id, String msgType, boolean isGroup, UpdateCallback callback) {
            this.mCallback = callback;
            this.mId = id;
            this.mMsgType = msgType;
            this.mIsGroup = isGroup;
            this.mNeedUpdate = true;
        }

        public boolean isSameMsgItem(long id, String msgType, boolean isGroup) {
            return this.mId == id && this.mMsgType.equals(msgType) && (this.mIsGroup == isGroup || id < 0);
        }
    }

    public static DelaySendManager getInst() {
        DelaySendManager delaySendManager;
        synchronized (DelaySendManager.class) {
            if (sInstance == null) {
                sInstance = new DelaySendManager();
            }
            delaySendManager = sInstance;
        }
        return delaySendManager;
    }

    public void registerUiUpdate(long id, String msgType, boolean isGroup, UpdateCallback callback) {
        Throwable th;
        synchronized (this.mUIUpdateList) {
            try {
                TimerUpdate uiUpdate;
                TimerUpdate uiUpdate2 = null;
                for (TimerUpdate update : this.mUIUpdateList) {
                    try {
                        if (update.isSameMsgItem(id, msgType, isGroup)) {
                            uiUpdate = update;
                        } else {
                            uiUpdate = uiUpdate2;
                        }
                        uiUpdate2 = uiUpdate;
                    } catch (Throwable th2) {
                        th = th2;
                        uiUpdate = uiUpdate2;
                    }
                }
                if (uiUpdate2 == null) {
                    uiUpdate = new TimerUpdate(id, msgType, isGroup, callback);
                    this.mUIUpdateList.add(uiUpdate);
                    resetDelayMsgTime(id, msgType, isGroup);
                } else {
                    uiUpdate2.mCallback = callback;
                    uiUpdate = uiUpdate2;
                }
                if (uiUpdate.mNeedUpdate) {
                    uiUpdate.mCallback.onUpdate(uiUpdate.mTime / 1000, uiUpdate.mId, uiUpdate.mMsgType);
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void resetDelayMsgTime(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                if (msg.isSameMsgItem(id, msgType, isGroup)) {
                    msg.mTime = 6000;
                }
            }
        }
    }

    private DelaySendManager() {
    }

    public void addDelayMsg(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            DelayMsg delayMsg = new DelayMsg(id, msgType, isGroup);
            delayMsg.mTime = 6000;
            this.mDelayMsgList.add(delayMsg);
        }
        synchronized (this.mUIUpdateList) {
            for (TimerUpdate update : this.mUIUpdateList) {
                if (update.isSameMsgItem(id, msgType, isGroup)) {
                    update.mNeedUpdate = true;
                    update.mTime = 6000;
                }
            }
        }
        startRunnable();
    }

    private void startRunnable() {
        if (this.mRunnable == null) {
            this.mRunnable = new Runnable() {
                public void run() {
                    DelaySendManager.this.processDelayMessages();
                    DelaySendManager.this.updateUITimer();
                    if (DelaySendManager.this.isDelayListEmpty() && DelaySendManager.this.isUiUpdateListEmpty()) {
                        DelaySendManager.this.mHandler.removeCallbacks(DelaySendManager.this.mRunnable);
                        DelaySendManager.this.mRunnable = null;
                        return;
                    }
                    DelaySendManager.this.mHandler.postDelayed(DelaySendManager.this.mRunnable, 500);
                }
            };
            this.mHandler.postDelayed(this.mRunnable, 500);
        }
    }

    private boolean isDelayListEmpty() {
        boolean isEmpty;
        synchronized (this.mDelayMsgList) {
            isEmpty = this.mDelayMsgList.isEmpty();
        }
        return isEmpty;
    }

    private boolean isUiUpdateListEmpty() {
        boolean isEmpty;
        synchronized (this.mUIUpdateList) {
            isEmpty = this.mUIUpdateList.isEmpty();
        }
        return isEmpty;
    }

    private void processDelayMessages() {
        synchronized (this.mDelayMsgList) {
            Iterator<DelayMsg> iterator = this.mDelayMsgList.iterator();
            while (iterator.hasNext()) {
                DelayMsg delayMsg = (DelayMsg) iterator.next();
                delayMsg.mTime -= 500;
                if (delayMsg.mTime == 0 && delayMsg.mStatus == 1) {
                    boolean isSms = delayMsg.mId > 0;
                    iterator.remove();
                    if (sHwCust == null || !sHwCust.isNeedSendDelayRcsMsg(delayMsg.mMsgType)) {
                        sendDelayedMsg(isSms);
                    } else {
                        sHwCust.sendDelayRcsMsg(getContext(), delayMsg.mId, delayMsg.mMsgType, null);
                    }
                }
            }
        }
    }

    private void updateUITimer() {
        synchronized (this.mUIUpdateList) {
            Iterator<TimerUpdate> iterator = this.mUIUpdateList.iterator();
            while (iterator.hasNext()) {
                TimerUpdate update = (TimerUpdate) iterator.next();
                update.mTime -= 500;
                if (update.mNeedUpdate) {
                    update.mNeedUpdate = false;
                } else {
                    update.mNeedUpdate = true;
                    update.mCallback.onUpdate(update.mTime / 1000, update.mId, update.mMsgType);
                }
                if (update.mTime <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    public boolean isDelayMsg(long id, String msgType) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                boolean equals;
                if (msg.mId == id) {
                    equals = msgType.equals(msg.mMsgType);
                    continue;
                } else {
                    equals = false;
                    continue;
                }
                if (equals) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isDelayMsg(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                if (msg.isSameMsgItem(id, msgType, isGroup)) {
                    return true;
                }
            }
            return false;
        }
    }

    public int getCancelMsgStatus(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                if (msg.isSameMsgItem(id, msgType, isGroup)) {
                    int i = msg.mStatus;
                    return i;
                }
            }
            return 0;
        }
    }

    public void removeDelayMsg(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                if (msg.isSameMsgItem(id, msgType, isGroup)) {
                    this.mDelayMsgList.remove(msg);
                    break;
                }
            }
        }
        synchronized (this.mUIUpdateList) {
            for (TimerUpdate update : this.mUIUpdateList) {
                if (update.isSameMsgItem(id, msgType, isGroup)) {
                    this.mUIUpdateList.remove(update);
                    break;
                }
            }
        }
    }

    private Context getContext() {
        return MmsApp.getApplication().getApplicationContext();
    }

    private void sendDelayedMsg(boolean isSms) {
        Context context = getContext();
        if (isSms) {
            SmsReceiver.broadcastForSendSms(context);
        } else {
            TransactionService.startMe(context);
        }
    }

    public void setDelayMsgCanceled(long id, String msgType, boolean isGroup) {
        synchronized (this.mDelayMsgList) {
            for (DelayMsg msg : this.mDelayMsgList) {
                if (msg.isSameMsgItem(id, msgType, isGroup)) {
                    msg.mStatus = 2;
                    return;
                }
            }
        }
    }

    public void addUIUpdate(long id, String msgType, boolean isGroup, UpdateCallback callback) {
        TimerUpdate update = new TimerUpdate(id, msgType, isGroup, callback);
        synchronized (this.mUIUpdateList) {
            this.mUIUpdateList.add(update);
        }
        final long j = id;
        final String str = msgType;
        final boolean z = isGroup;
        final TimerUpdate timerUpdate = update;
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                DelayMsg delayMsg = null;
                synchronized (DelaySendManager.this.mDelayMsgList) {
                    for (DelayMsg msg : DelaySendManager.this.mDelayMsgList) {
                        if (msg.isSameMsgItem(j, str, z)) {
                            delayMsg = msg;
                            break;
                        }
                    }
                }
                if (delayMsg == null) {
                    synchronized (DelaySendManager.this.mUIUpdateList) {
                        if (DelaySendManager.this.mUIUpdateList.contains(timerUpdate)) {
                            DelaySendManager.this.mUIUpdateList.remove(timerUpdate);
                        }
                    }
                }
            }
        }, 300);
    }
}
