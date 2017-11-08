package com.huawei.systemmanager.spacecleanner.engine;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.List;

public interface ITrashScanListener {
    public static final ITrashScanListener EMPTY_LISTENER = new SimleListener();

    public static class SimleListener implements ITrashScanListener {
        public void onScanStart(int scannerType) {
        }

        public void onScanProgressChange(int scannerType, int progress, String info, long normalTrashSize, int normalProgress) {
        }

        public void onTrashFound(int scanType, Trash trash, long size) {
        }

        public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
        }
    }

    public static class MultiListener implements ITrashScanListener {
        private final List<ITrashScanListener> mListeners = Lists.newArrayList();

        public void addListener(ITrashScanListener l) {
            synchronized (this.mListeners) {
                this.mListeners.add(l);
            }
        }

        public void removeListener(ITrashScanListener l) {
            synchronized (this.mListeners) {
                this.mListeners.remove(l);
            }
        }

        public List<ITrashScanListener> getListeners() {
            List newArrayList;
            synchronized (this.mListeners) {
                newArrayList = Lists.newArrayList(this.mListeners);
            }
            return newArrayList;
        }

        public void onScanStart(int scannerType) {
            for (ITrashScanListener l : getListeners()) {
                l.onScanStart(scannerType);
            }
        }

        public void onScanProgressChange(int scannerType, int progress, String info, long normalTrashSize, int normalProgress) {
            for (ITrashScanListener l : getListeners()) {
                l.onScanProgressChange(scannerType, progress, info, normalTrashSize, normalProgress);
            }
        }

        public void onTrashFound(int scanType, Trash trash, long size) {
            for (ITrashScanListener l : getListeners()) {
                l.onTrashFound(scanType, trash, size);
            }
        }

        public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
            for (ITrashScanListener l : getListeners()) {
                l.onScanEnd(scannerType, supportTrashType, canceled);
            }
        }
    }

    void onScanEnd(int i, int i2, boolean z);

    void onScanProgressChange(int i, int i2, String str, long j, int i3);

    void onScanStart(int i);

    void onTrashFound(int i, Trash trash, long j);
}
