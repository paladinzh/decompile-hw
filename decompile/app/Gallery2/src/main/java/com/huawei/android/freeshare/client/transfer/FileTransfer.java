package com.huawei.android.freeshare.client.transfer;

import android.content.Context;

public abstract class FileTransfer {
    private Context mContext;
    protected TransferObserver mTransferObserver;

    public interface TransferObserver {
        void notifyChanged(TransferItem transferItem);
    }

    public abstract boolean cancleTransferringMission();

    public abstract boolean start(Mission mission);

    public FileTransfer(Context context) {
        this.mContext = context;
    }

    public boolean init() {
        return true;
    }

    public boolean destroy() {
        return true;
    }

    public void registerObserver(TransferObserver observer) {
        this.mTransferObserver = observer;
    }

    public boolean isTransferring() {
        return false;
    }

    protected final Context getContext() {
        return this.mContext;
    }
}
