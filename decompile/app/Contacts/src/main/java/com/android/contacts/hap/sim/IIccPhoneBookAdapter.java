package com.android.contacts.hap.sim;

import android.os.RemoteException;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.internal.telephony.HwTelephonyFactory;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;
import java.util.Arrays;

public class IIccPhoneBookAdapter {
    private static int[] sCachedFirstSimAdnRecordsSize;
    private static int[] sCachedSecondSimAdnRecordsSize;
    private int mSubscription = -1;

    public IIccPhoneBookAdapter(int subscription) {
        this.mSubscription = subscription;
    }

    public int[] getRecordsSize() {
        Object syncObject = new Object();
        RecordsSizeGettingThread thread = new RecordsSizeGettingThread(0, this.mSubscription, 0, syncObject);
        thread.start();
        if (HwLog.HWFLOW) {
            HwLog.i("IIccPhoneBookAdapter", "getRecordsSize start ");
        }
        synchronized (syncObject) {
            try {
                syncObject.wait(500000);
            } catch (InterruptedException e) {
            }
        }
        if (thread.isDone()) {
            if (HwLog.HWFLOW) {
                HwLog.i("IIccPhoneBookAdapter", "getRecordsSize end ");
            }
            return thread.getResult();
        }
        try {
            thread.interrupt();
        } catch (Exception e2) {
        }
        HwLog.e("IIccPhoneBookAdapter", "getRecordSize() does not return any result for 500 seconds, hence we are throwing unsupported exception");
        return null;
    }

    public int getAlphaEncodedLength(String oldData) {
        try {
            return Integer.valueOf(HwTelephonyFactory.getHwUiccManager().getAlphaTagEncodingLength(oldData)).intValue();
        } catch (Exception e) {
            HwLog.e("IIccPhoneBookAdapter", "getAlphaEncodedLength() throwing exception");
            ExceptionCapture.captureAlphaEncodedException("getAlphaEncodedLength() throwing exception", e);
            try {
                return IIccPhoneBookManagerEx.getDefault().getAlphaTagEncodingLength(oldData);
            } catch (RemoteException e2) {
                HwLog.w("IIccPhoneBookAdapter", "IIccPhoneBookManagerEx.getDefault().getAlphaTagEncodingLength() failed", e2);
                ExceptionCapture.captureAlphaEncodedException("IIccPhoneBookManagerEx.getDefault().getAlphaTagEncodingLength() failed", e2);
                return -1;
            } catch (Exception e3) {
                return -1;
            }
        }
    }

    public static int[] getCachedAdnRecordsSize(int slotId) {
        if (slotId == -1 || slotId == 0) {
            if (sCachedFirstSimAdnRecordsSize != null && sCachedFirstSimAdnRecordsSize.length > 0) {
                return Arrays.copyOf(sCachedFirstSimAdnRecordsSize, sCachedFirstSimAdnRecordsSize.length);
            }
        } else if (slotId == 1 && sCachedSecondSimAdnRecordsSize != null && sCachedSecondSimAdnRecordsSize.length > 0) {
            return Arrays.copyOf(sCachedSecondSimAdnRecordsSize, sCachedSecondSimAdnRecordsSize.length);
        }
        return new int[0];
    }

    private static void setCachedAdnRecordsSize(int slotId, int[] result) {
        if (slotId == -1 || slotId == 0) {
            sCachedFirstSimAdnRecordsSize = result;
        } else if (slotId == 1) {
            sCachedSecondSimAdnRecordsSize = result;
        }
    }

    public int[] getAdnRecordsSize() throws UnsupportedException {
        Integer type = (Integer) RefelctionUtils.getStaticVariableValue("com.huawei.internal.telephony.uicc.IccConstantsEx", "EF_ADN");
        Object syncObject = new Object();
        RecordsSizeGettingThread thread = new RecordsSizeGettingThread(1, this.mSubscription, type.intValue(), syncObject);
        thread.start();
        if (HwLog.HWFLOW) {
            HwLog.i("IIccPhoneBookAdapter", "getAdnRecordsSize start");
        }
        synchronized (syncObject) {
            try {
                syncObject.wait(60000);
            } catch (InterruptedException e) {
            }
        }
        if (thread.isDone()) {
            setCachedAdnRecordsSize(this.mSubscription, thread.getResult());
            if (HwLog.HWFLOW) {
                HwLog.i("IIccPhoneBookAdapter", "getAdnRecordsSize end ");
            }
            return thread.getResult();
        }
        try {
            thread.interrupt();
        } catch (Exception e2) {
        }
        HwLog.e("IIccPhoneBookAdapter", "getAdnRecordsSize() does not return any result for 60 seconds, hence we are returning null");
        return null;
    }

    public int getAvailableSimExt1FreeSpace(int mSubscription) {
        try {
            HwLog.i("IIccPhoneBookAdapter", "getAvailableSimExt1FreeSpace");
            return IIccPhoneBookManagerEx.getDefault().getSpareExt1Count(mSubscription);
        } catch (Exception e) {
            HwLog.i("IIccPhoneBookAdapter", "getAvailableSimExt1FreeSpace failed");
            return -1;
        }
    }
}
