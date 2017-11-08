package com.huawei.systemmanager.optimize.process;

public interface IDataChangedListener {

    public static class SimpleDataChangeListenr implements IDataChangedListener {
        public void onPackageAdded(String pkgName, boolean protect) {
        }

        public void onPackageRemoved(String pkgName) {
        }

        public void onProtectedAppRefresh() {
        }
    }

    void onPackageAdded(String str, boolean z);

    void onPackageRemoved(String str);

    void onProtectedAppRefresh();
}
