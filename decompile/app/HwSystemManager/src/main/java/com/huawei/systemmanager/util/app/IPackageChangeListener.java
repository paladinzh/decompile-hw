package com.huawei.systemmanager.util.app;

public interface IPackageChangeListener {

    public static class DefListener implements IPackageChangeListener {
        public void onPackagedAdded(String pkgName) {
        }

        public void onPackageRemoved(String pkgName) {
        }

        public void onPackageChanged(String pkgName) {
        }

        public void onExternalChanged(String[] packages, boolean available) {
        }
    }

    void onExternalChanged(String[] strArr, boolean z);

    void onPackageChanged(String str);

    void onPackageRemoved(String str);

    void onPackagedAdded(String str);
}
