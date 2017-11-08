package com.huawei.timekeeper;

import android.database.Observable;
import java.util.ArrayList;

public class TimeObservable extends Observable<TimeObserver> {
    protected void dispatchTick(TimeTickInfo info) {
        synchronized (this.mObservers) {
            for (TimeObserver observer : this.mObservers) {
                observer.onTimeTick(info);
            }
        }
    }

    protected void dispatchFinish() {
        ArrayList<TimeObserver> tmpList = new ArrayList();
        synchronized (this.mObservers) {
            tmpList.addAll(this.mObservers);
            for (TimeObserver observer : tmpList) {
                observer.onTimeFinish();
            }
        }
        tmpList.clear();
    }

    protected boolean isObserverRegistered(TimeObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        boolean contains;
        synchronized (this.mObservers) {
            contains = this.mObservers.contains(observer);
        }
        return contains;
    }
}
