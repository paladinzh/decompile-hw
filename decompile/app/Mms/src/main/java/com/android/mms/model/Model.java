package com.android.mms.model;

import java.util.concurrent.CopyOnWriteArrayList;

public class Model {
    protected CopyOnWriteArrayList<IModelChangedObserver> mModelChangedObservers = new CopyOnWriteArrayList();

    public void registerModelChangedObserver(IModelChangedObserver observer) {
        if (!this.mModelChangedObservers.contains(observer)) {
            this.mModelChangedObservers.add(observer);
            registerModelChangedObserverInDescendants(observer);
        }
    }

    public void unregisterModelChangedObserver(IModelChangedObserver observer) {
        this.mModelChangedObservers.remove(observer);
        unregisterModelChangedObserverInDescendants(observer);
    }

    public void unregisterAllModelChangedObservers() {
        unregisterAllModelChangedObserversInDescendants();
        this.mModelChangedObservers.clear();
    }

    protected void notifyModelChanged(boolean dataChanged) {
        for (IModelChangedObserver observer : this.mModelChangedObservers) {
            observer.onModelChanged(this, dataChanged);
        }
    }

    protected void registerModelChangedObserverInDescendants(IModelChangedObserver observer) {
    }

    protected void unregisterModelChangedObserverInDescendants(IModelChangedObserver observer) {
    }

    protected void unregisterAllModelChangedObserversInDescendants() {
    }
}
