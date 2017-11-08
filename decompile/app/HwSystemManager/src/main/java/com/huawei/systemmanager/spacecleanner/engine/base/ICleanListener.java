package com.huawei.systemmanager.spacecleanner.engine.base;

import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;

public interface ICleanListener {
    public static final ICleanListener sEmptyListener = new SimpleListener();

    public static class SimpleListener implements ICleanListener {
        public void onCleanStart() {
        }

        public void onCleanProgressChange(int progress, String info) {
        }

        public void onItemUpdate(Trash trash) {
        }

        public void onCleanEnd(boolean canceled, long cleanedTrashSize) {
        }
    }

    void onCleanEnd(boolean z, long j);

    void onCleanProgressChange(int i, String str);

    void onCleanStart();

    void onItemUpdate(Trash trash);
}
