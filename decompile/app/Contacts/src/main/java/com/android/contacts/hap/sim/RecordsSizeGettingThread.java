package com.android.contacts.hap.sim;

import com.android.contacts.util.HwLog;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;

/* compiled from: IIccPhoneBookAdapter */
class RecordsSizeGettingThread extends Thread {
    private int mEfid;
    private boolean mIsDone;
    private int[] mResult;
    private int mSubscription = -1;
    private Object mSyncObject;
    private int mType;

    public RecordsSizeGettingThread(int type, int subscription, int efid, Object syncObject) {
        this.mType = type;
        this.mSubscription = subscription;
        this.mEfid = efid;
        this.mSyncObject = syncObject;
    }

    public void run() {
        try {
            int i;
            if (SimFactoryManager.isDualSim()) {
                if (this.mType == 0) {
                    this.mResult = IIccPhoneBookManagerEx.getDefault().getRecordsSize(this.mSubscription);
                } else if (1 == this.mType) {
                    this.mResult = IIccPhoneBookManagerEx.getDefault().getAdnRecordsSizeOnSubscription(this.mEfid, this.mSubscription);
                }
                if (this.mResult != null) {
                    HwLog.e("IIccPhoneBookAdapter", "get adn records size returns null.");
                } else {
                    HwLog.i("IIccPhoneBookAdapter", "get adn records size returns proper result." + this.mResult.length);
                    while (i < this.mResult.length) {
                        try {
                            HwLog.i("IIccPhoneBookAdapter", "Simrecords " + i + "= " + this.mResult[i]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.mIsDone = true;
                synchronized (this.mSyncObject) {
                    this.mSyncObject.notifyAll();
                }
            }
            if (this.mType == 0) {
                this.mResult = IIccPhoneBookManagerEx.getDefault().getRecordsSize();
            } else if (1 == this.mType) {
                this.mResult = IIccPhoneBookManagerEx.getDefault().getAdnRecordsSize(this.mEfid);
            }
            if (this.mResult != null) {
                HwLog.i("IIccPhoneBookAdapter", "get adn records size returns proper result." + this.mResult.length);
                for (i = 0; i < this.mResult.length; i++) {
                    HwLog.i("IIccPhoneBookAdapter", "Simrecords " + i + "= " + this.mResult[i]);
                }
            } else {
                HwLog.e("IIccPhoneBookAdapter", "get adn records size returns null.");
            }
            this.mIsDone = true;
            synchronized (this.mSyncObject) {
                this.mSyncObject.notifyAll();
            }
        } catch (Exception e2) {
            HwLog.w("IIccPhoneBookAdapter", "get records size failed", e2);
            this.mResult = null;
        }
    }

    public boolean isDone() {
        return this.mIsDone;
    }

    public int[] getResult() {
        return this.mResult;
    }
}
