package com.huawei.systemmanager.comm.widget;

import android.content.Context;

public interface CommonSwitcher {

    public static class SimpleSwitcher implements CommonSwitcher {
        private final Context mContext;

        public SimpleSwitcher(Context ctx) {
            this.mContext = ctx;
        }

        public void init() {
        }

        public void refreshState() {
        }

        public boolean getCheckState() {
            return false;
        }

        public void registerStateObserver() {
        }

        public void unRegisterStateObserver() {
        }

        protected final Context getContext() {
            return this.mContext;
        }
    }

    boolean getCheckState();

    void init();

    void refreshState();

    void registerStateObserver();

    void unRegisterStateObserver();
}
