package com.huawei.systemmanager.netassistant.traffic.datasaver;

import java.util.List;

public interface IDataSaver {

    public interface Data {

        public interface Listener {
            void onBlackListStatusChanged(int i, boolean z);

            void onDataSaverStateChange(boolean z);

            void onWhiteListStatusChanged(int i, boolean z);
        }

        List<DataSaverEntry> getList(int i);

        void setDataSaverEnable(boolean z);

        void setWhiteListed(int i, boolean z, String str);
    }

    public interface View {
        void onBlacklistedChanged(int i, boolean z);

        void onDataSaverStateChanged(boolean z);

        void onWhiteListedChanged(int i, boolean z);
    }

    public interface Manager {
        void setDataSaverEnable(boolean z);

        void setWhiteListed(boolean z, int i, String str);
    }
}
