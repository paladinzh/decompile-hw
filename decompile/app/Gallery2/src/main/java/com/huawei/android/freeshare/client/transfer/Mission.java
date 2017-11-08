package com.huawei.android.freeshare.client.transfer;

import com.huawei.android.freeshare.client.Transmission;
import com.huawei.android.freeshare.client.device.DeviceInfo;
import java.util.ArrayList;

public class Mission {
    private static int CURRENT_ID = 0;
    private int mID;
    private ArrayList<TransferItem> mItems;
    private int mState;
    private DeviceInfo mTarget;
    private Transmission mTransmission;

    public Mission() {
        this.mID = getAndIncrementID();
        this.mState = 2;
    }

    public static int getAndIncrementID() {
        int id = CURRENT_ID;
        CURRENT_ID = id + 1;
        return id;
    }

    public Mission(Transmission transmission, DeviceInfo target, String uri, String mimeType) {
        this();
        this.mTransmission = transmission;
        this.mTarget = target;
        this.mItems = new ArrayList(1);
        this.mItems.add(new TransferItem(this, uri, mimeType));
    }

    public final int getState() {
        return this.mState;
    }

    public void upDateState() {
        boolean isAllComplete = true;
        for (TransferItem item : this.mItems) {
            if (item.mStatus == 4) {
                this.mState = 4;
                return;
            } else if (item.mStatus != 5) {
                isAllComplete = false;
            }
        }
        if (isAllComplete) {
            this.mState = 5;
        }
    }

    public final ArrayList<TransferItem> getTransferItems() {
        return this.mItems;
    }

    public final DeviceInfo getTargetDevice() {
        return this.mTarget;
    }

    public final boolean isComplete() {
        return this.mState == 5;
    }

    public boolean isTheSameTarget(Mission another) {
        if (another == null || this.mTarget == null || !this.mTarget.equal(another.getTargetDevice())) {
            return false;
        }
        return true;
    }
}
