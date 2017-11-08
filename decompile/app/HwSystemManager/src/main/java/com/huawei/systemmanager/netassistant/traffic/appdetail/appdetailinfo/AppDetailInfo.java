package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

public class AppDetailInfo {

    public interface BaseInfo {
        String getSubTitle();

        String getTask();

        String getTitle();

        int getType();

        boolean isChecked();

        boolean isEnable();

        void setChecked(boolean z);
    }

    public static class SimpleBaseInfo implements BaseInfo {
        public int getType() {
            return -1;
        }

        public String getTitle() {
            return null;
        }

        public String getSubTitle() {
            return null;
        }

        public boolean isEnable() {
            return false;
        }

        public boolean isChecked() {
            return false;
        }

        public void setChecked(boolean value) {
        }

        public String getTask() {
            return null;
        }
    }
}
