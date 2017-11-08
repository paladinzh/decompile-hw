package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.setupwizardlib.R$styleable;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;
import java.util.ArrayList;

public abstract class AbstractItemHierarchy implements ItemHierarchy {
    private int mId = 0;
    private ArrayList<Observer> mObservers = new ArrayList();

    public AbstractItemHierarchy(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SuwAbstractItem);
        this.mId = a.getResourceId(R$styleable.SuwAbstractItem_android_id, 0);
        a.recycle();
    }

    public int getId() {
        return this.mId;
    }

    public void registerObserver(Observer observer) {
        this.mObservers.add(observer);
    }

    public void notifyChanged() {
        for (Observer observer : this.mObservers) {
            observer.onChanged(this);
        }
    }
}
