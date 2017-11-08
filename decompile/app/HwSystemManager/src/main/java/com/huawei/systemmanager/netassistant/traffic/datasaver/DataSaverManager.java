package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.content.Context;
import android.os.AsyncTask;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.Data.Listener;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.Manager;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.View;
import java.lang.ref.WeakReference;

public class DataSaverManager implements Manager, Listener {
    private DataSaverDataCenter mDataCenter;
    private View mView;

    static class DataSaverSwitchSetterTask extends AsyncTask<Boolean, Void, Void> {
        private final WeakReference<DataSaverDataCenter> dataCenterWeakReference;

        public DataSaverSwitchSetterTask(DataSaverDataCenter dataCenter) {
            this.dataCenterWeakReference = new WeakReference(dataCenter);
        }

        protected Void doInBackground(Boolean... params) {
            DataSaverDataCenter dataCenter = (DataSaverDataCenter) this.dataCenterWeakReference.get();
            if (!(dataCenter == null || params == null || params[0] == null)) {
                dataCenter.setDataSaverEnable(params[0].booleanValue());
            }
            return null;
        }
    }

    static class WhiteListSetterTask extends AsyncTask<Object, Void, Void> {
        private final WeakReference<DataSaverDataCenter> dataCenterWeakReference;

        public WhiteListSetterTask(DataSaverDataCenter dataCenter) {
            this.dataCenterWeakReference = new WeakReference(dataCenter);
        }

        protected Void doInBackground(Object... params) {
            DataSaverDataCenter dataCenter = (DataSaverDataCenter) this.dataCenterWeakReference.get();
            if (!(dataCenter == null || params == null || params[0] == null || params[1] == null || params[2] == null)) {
                dataCenter.setWhiteListed(((Integer) params[1]).intValue(), ((Boolean) params[0]).booleanValue(), (String) params[2]);
            }
            return null;
        }
    }

    public DataSaverManager(Context context, View view) {
        this.mDataCenter = new DataSaverDataCenter(context);
        this.mView = view;
    }

    private void asyncSetDataSaverEnable(Boolean enable) {
        new DataSaverSwitchSetterTask(this.mDataCenter).execute(new Boolean[]{enable});
    }

    public void setDataSaverEnable(boolean enable) {
        asyncSetDataSaverEnable(Boolean.valueOf(enable));
    }

    public void setWhiteListed(boolean whiteListed, int uid, String packageName) {
        new WhiteListSetterTask(this.mDataCenter).execute(new Object[]{Boolean.valueOf(whiteListed), Integer.valueOf(uid), packageName});
    }

    public void release() {
        this.mDataCenter.release();
        this.mView = null;
    }

    public void onDataSaverStateChange(boolean enable) {
        this.mView.onDataSaverStateChanged(enable);
    }

    public void onWhiteListStatusChanged(int uid, boolean whitelisted) {
        this.mView.onWhiteListedChanged(uid, whitelisted);
    }

    public void onBlackListStatusChanged(int uid, boolean blacklisted) {
        this.mView.onBlacklistedChanged(uid, blacklisted);
    }

    public DataSaverDataCenter getDataCenter() {
        return this.mDataCenter;
    }

    public void registerListener() {
        this.mDataCenter.registerListner(this);
    }

    public void unRegisterListener() {
        this.mDataCenter.unregisterListener(this);
    }
}
