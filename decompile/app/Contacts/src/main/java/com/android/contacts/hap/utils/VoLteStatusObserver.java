package com.android.contacts.hap.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import com.android.contacts.util.HwLog;

public class VoLteStatusObserver extends ContentObserver {
    private CallBack mCallBack = null;
    private Context mContext = null;
    private boolean mHasLTEObserverChange = false;

    public interface CallBack {
        void updateItemsStatus();
    }

    public VoLteStatusObserver(Context context, CallBack callBack) {
        super(new Handler());
        this.mContext = context;
        this.mCallBack = callBack;
    }

    public void registerObserver() {
        if (!(this.mContext == null || this.mHasLTEObserverChange)) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), true, this);
            this.mHasLTEObserverChange = true;
        }
    }

    public void unregisterObserver() {
        if (this.mContext != null && this.mHasLTEObserverChange) {
            this.mContext.getContentResolver().unregisterContentObserver(this);
            this.mHasLTEObserverChange = false;
        }
    }

    public void onChange(boolean selfChange) {
        if (HwLog.HWFLOW) {
            HwLog.i("VoLteStatusObserver", "mLTESwitchObserver onChange, selfChange = " + selfChange);
        }
        if (VtLteUtils.isVtLteOn(this.mContext)) {
            this.mCallBack.updateItemsStatus();
        }
    }
}
