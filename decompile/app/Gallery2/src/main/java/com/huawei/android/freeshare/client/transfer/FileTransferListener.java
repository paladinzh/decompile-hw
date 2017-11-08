package com.huawei.android.freeshare.client.transfer;

public interface FileTransferListener {
    void onProgressUpdate(String str, int i);

    void onTransferFinish(String str, boolean z);
}
