package com.android.mms.transaction;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.CheckableRunnable;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class Transaction extends Observable implements CheckableRunnable {
    protected Context mContext;
    HwCustTransaction mHwCustTransaction = ((HwCustTransaction) HwCustUtils.createObj(HwCustTransaction.class, new Object[0]));
    protected String mId;
    protected int mNetworkType;
    private final int mServiceId;
    protected int mSubId;
    protected TransactionSettings mTransactionSettings;
    protected TransactionState mTransactionState;

    public abstract int getType();

    public Transaction(Context context, int serviceId, TransactionSettings settings) {
        this.mContext = context;
        this.mTransactionState = new TransactionState();
        this.mServiceId = serviceId;
        this.mTransactionSettings = settings;
        if (MessageUtils.isMultiSimEnabled()) {
            this.mSubId = MessageUtils.getPreferredDataSubscription();
        } else {
            this.mSubId = 0;
        }
        this.mNetworkType = -1;
    }

    public TransactionState getState() {
        return this.mTransactionState;
    }

    public void process() {
        ThreadEx.getNetworkExecutor().execute(this);
    }

    public boolean isEquivalent(Transaction transaction) {
        return this.mId.equals(transaction.mId);
    }

    public int getServiceId() {
        return this.mServiceId;
    }

    public TransactionSettings getConnectionSettings() {
        return this.mTransactionSettings;
    }

    public void setConnectionSettings(TransactionSettings settings) {
        this.mTransactionSettings = settings;
    }

    protected byte[] sendPdu(byte[] pdu) throws IOException, MmsException {
        return sendPdu(-1, pdu, this.mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException, MmsException {
        return sendPdu(-1, pdu, mmscUrl);
    }

    protected byte[] sendPdu(long token, byte[] pdu) throws IOException, MmsException {
        return sendPdu(token, pdu, this.mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(long token, byte[] pdu, String mmscUrl) throws IOException, MmsException {
        if (pdu == null) {
            throw new MmsException();
        }
        ensureRouteToHost(mmscUrl, this.mTransactionSettings);
        if (this.mHwCustTransaction == null || !this.mHwCustTransaction.useWifi(this.mContext)) {
            return HttpUtils.httpConnection(this.mContext, token, mmscUrl, pdu, 1, this.mTransactionSettings.isProxySet(), this.mTransactionSettings.getProxyAddress(), this.mTransactionSettings.getProxyPort());
        }
        MLog.d("Mms_TXN", "send Mms via wifi");
        return this.mHwCustTransaction.setPduInWifi(token, pdu, mmscUrl, this.mTransactionSettings);
    }

    protected byte[] getPdu(String url, String uri) throws IOException {
        ensureRouteToHost(url, this.mTransactionSettings);
        return HttpUtils.httpConnection(this.mContext, -1, url, null, 2, this.mTransactionSettings.isProxySet(), this.mTransactionSettings.getProxyAddress(), this.mTransactionSettings.getProxyPort(), uri);
    }

    protected void stopDownload(String uri) throws IOException {
        HttpUtils.httpDisconnection(uri);
    }

    private void ensureRouteToHost(String url, TransactionSettings settings) throws IOException {
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (this.mHwCustTransaction == null || !this.mHwCustTransaction.useWifi(this.mContext)) {
            InetAddress inetAddr;
            if (settings.isProxySet()) {
                String proxyAddr = settings.getProxyAddress();
                try {
                    inetAddr = InetAddress.getByName(proxyAddr);
                    if (inetAddr != null && MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXN", "ensureRouteToHost inetAddr = " + inetAddr);
                    }
                    if (!connMgr.requestRouteToHostAddress(2, inetAddr)) {
                        throw new IOException("Cannot establish route to proxy " + inetAddr);
                    }
                } catch (UnknownHostException e) {
                    throw new IOException("Cannot establish route for " + url + ": Unknown proxy " + proxyAddr);
                }
            } else if (url != null) {
                try {
                    inetAddr = InetAddress.getByName(Uri.parse(url).getHost());
                    if (inetAddr != null && MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXN", "ensureRouteToHost inetAddr = " + inetAddr);
                    }
                    if (!connMgr.requestRouteToHostAddress(2, inetAddr)) {
                        throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
                    }
                } catch (UnknownHostException e2) {
                    throw new IOException("Cannot establish route for " + url + ": Unknown host");
                }
            } else {
                throw new IOException("Cannot establish route for null : Unknown host");
            }
            return;
        }
        MLog.d("Mms_TXN", "ensureRouteToHostInWifi");
        this.mHwCustTransaction.ensureRouteToHostInWifi(url);
    }

    public String toString() {
        return getClass().getName() + ": serviceId=" + this.mServiceId + " subId:" + this.mSubId + ", NetworkType:" + this.mNetworkType + " state:" + this.mTransactionState + " type:" + getType();
    }

    public int getSubscription() {
        return this.mSubId;
    }

    public static int querySubscription(Context context, Uri uri) {
        if (!MessageUtils.isMultiSimEnabled()) {
            return 0;
        }
        int ddsSub = MessageUtils.getPreferredDataSubscription();
        int i = context;
        Cursor cursor = SqliteWrapper.query(i, context.getContentResolver(), uri, new String[]{"sub_id"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    i = cursor.getInt(cursor.getColumnIndexOrThrow("sub_id"));
                    return i;
                }
                cursor.close();
            } catch (IllegalArgumentException e) {
                return ddsSub;
            } finally {
                cursor.close();
            }
        }
        return ddsSub;
    }

    public Uri getUri() {
        return null;
    }

    public long getMaxRunningTime() {
        return 20000;
    }

    public void onTimeout(long runTime) {
        MLog.w("Mms_TXN", toString() + "execute timeout. use " + runTime);
    }
}
