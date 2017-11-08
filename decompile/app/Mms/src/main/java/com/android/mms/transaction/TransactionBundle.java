package com.android.mms.transaction;

import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.internal.telephony.uicc.IccUtils;

public class TransactionBundle {
    private final Bundle mBundle;

    private TransactionBundle(int transactionType) {
        this.mBundle = new Bundle();
        this.mBundle.putInt(NumberInfo.TYPE_KEY, transactionType);
    }

    public TransactionBundle(int transactionType, String uri) {
        this(transactionType);
        this.mBundle.putString("uri", uri);
    }

    public TransactionBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public int getTransactionType() {
        return this.mBundle.getInt(NumberInfo.TYPE_KEY);
    }

    public String getUri() {
        return this.mBundle.getString("uri");
    }

    public byte[] getPushData() {
        return this.mBundle.getByteArray("mms-push-data");
    }

    public String getMmscUrl() {
        return this.mBundle.getString("mmsc-url");
    }

    public String getProxyAddress() {
        return this.mBundle.getString("proxy-address");
    }

    public int getProxyPort() {
        return this.mBundle.getInt("proxy-port");
    }

    public int getButtonDownloadClickCount() {
        return this.mBundle.getInt("button_download_click_count");
    }

    public String toString() {
        return "transactionType: " + getTransactionType() + " uri: " + getUri() + " pushData: " + IccUtils.bytesToHexString(getPushData()) + " mmscUrl: " + getMmscUrl() + " proxyAddress: " + getProxyAddress() + " proxyPort: " + getProxyPort();
    }
}
