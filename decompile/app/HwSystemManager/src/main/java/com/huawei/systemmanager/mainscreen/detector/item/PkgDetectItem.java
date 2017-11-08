package com.huawei.systemmanager.mainscreen.detector.item;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public abstract class PkgDetectItem<E> extends DetectItem {
    protected final Map<String, E> mApps = HsmCollections.newArrayMap();

    protected abstract String getPkgFromCustomItem(E e);

    protected PkgDetectItem() {
    }

    protected void init(Collection<E> itemList) {
        if (addPkgsToApps(itemList) <= 0) {
            setState(1);
        } else {
            setState(2);
        }
    }

    public void refresh() {
        HwLog.i(getTag(), "do refresh start");
        HsmPackageManager hsmPackageManager = HsmPackageManager.getInstance();
        synchronized (this.mApps) {
            Iterator<String> it = this.mApps.keySet().iterator();
            while (it.hasNext()) {
                String pkgName = (String) it.next();
                if (hsmPackageManager.getPkgInfo(pkgName) == null) {
                    HwLog.i(getTag(), "refresh pkg is removed, pkg:" + pkgName);
                    it.remove();
                }
            }
        }
        refreshState();
    }

    public boolean romvePkg(String pkg) {
        boolean changed;
        synchronized (this.mApps) {
            changed = this.mApps.remove(pkg) != null;
        }
        if (changed) {
            HwLog.i(getTag(), "remove pkg:" + pkg);
            refreshState();
        }
        return changed;
    }

    protected int addPkgsToApps(Collection<E> itemList) {
        int size;
        removeAppNotInstalled(Lists.newArrayList((Iterable) itemList));
        synchronized (this.mApps) {
            for (E item : itemList) {
                this.mApps.put(getPkgFromCustomItem(item), item);
            }
            size = this.mApps.size();
        }
        return size;
    }

    protected void refreshState() {
        if (getAppCount() == 0) {
            setState(3);
        } else {
            setState(2);
        }
    }

    protected final ArrayList<E> converToCustomItem() {
        ArrayList<E> itemList;
        synchronized (this.mApps) {
            itemList = Lists.newArrayListWithCapacity(this.mApps.size());
            for (E item : this.mApps.values()) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    public final int getAppCount() {
        int size;
        synchronized (this.mApps) {
            size = this.mApps.size();
        }
        return size;
    }

    protected void removeAppNotInstalled(Collection<E> c) {
        if (!HsmCollections.isEmpty(c)) {
            HsmPackageManager hsmPackageManager = HsmPackageManager.getInstance();
            Iterator<E> it = c.iterator();
            while (it.hasNext()) {
                String pkgName = getPkgFromCustomItem(it.next());
                if (hsmPackageManager.getPkgInfo(pkgName) == null) {
                    HwLog.i(getTag(), "removeAppNotInstalled pkgName:" + pkgName);
                    it.remove();
                }
            }
        }
    }
}
