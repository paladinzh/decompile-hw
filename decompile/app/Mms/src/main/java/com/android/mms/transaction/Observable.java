package com.android.mms.transaction;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Observable {
    private Iterator<Observer> mIterator;
    private final ArrayList<Observer> mObservers = new ArrayList();

    public void attach(Observer observer) {
        this.mObservers.add(observer);
    }

    public void detach(Observer observer) {
        if (this.mIterator != null) {
            this.mIterator.remove();
        } else {
            this.mObservers.remove(observer);
        }
    }

    public void notifyObservers() {
        this.mIterator = this.mObservers.iterator();
        while (this.mIterator.hasNext()) {
            try {
                ((Observer) this.mIterator.next()).update(this);
            } finally {
                this.mIterator = null;
            }
        }
    }
}
