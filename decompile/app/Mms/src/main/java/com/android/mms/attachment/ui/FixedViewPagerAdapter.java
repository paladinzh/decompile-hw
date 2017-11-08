package com.android.mms.attachment.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.android.mms.attachment.Factory;

public class FixedViewPagerAdapter<T extends PagerViewHolder> extends PagerAdapter {
    private final T[] mViewHolders;

    public FixedViewPagerAdapter(T[] viewHolders) {
        this.mViewHolders = (PagerViewHolder[]) viewHolders.clone();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        PagerViewHolder viewHolder = getViewHolder(position);
        View view = viewHolder.getView(container);
        if (view == null) {
            return null;
        }
        view.setTag(viewHolder);
        container.addView(view);
        return viewHolder;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        View destroyedView = getViewHolder(position).destroyView();
        if (destroyedView != null) {
            container.removeView(destroyedView);
        }
    }

    public int getCount() {
        return this.mViewHolders.length;
    }

    public boolean isViewFromObject(View view, Object object) {
        return view.getTag() == object;
    }

    public T getViewHolder(int i) {
        return getViewHolder(i, true);
    }

    public T getViewHolder(int i, boolean rtlAware) {
        PagerViewHolder[] pagerViewHolderArr = this.mViewHolders;
        if (rtlAware) {
            i = getRtlPosition(i);
        }
        return pagerViewHolderArr[i];
    }

    public Parcelable saveState() {
        Bundle savedViewHolderState = new Bundle(Factory.get().getApplicationContext().getClassLoader());
        for (int i = 0; i < this.mViewHolders.length; i++) {
            savedViewHolderState.putParcelable(getInstanceStateKeyForPage(i), getViewHolder(i).saveState());
        }
        return savedViewHolderState;
    }

    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state instanceof Bundle) {
            Bundle restoredViewHolderState = (Bundle) state;
            ((Bundle) state).setClassLoader(Factory.get().getApplicationContext().getClassLoader());
            for (int i = 0; i < this.mViewHolders.length; i++) {
                getViewHolder(i).restoreState(restoredViewHolderState.getParcelable(getInstanceStateKeyForPage(i)));
            }
            return;
        }
        super.restoreState(state, loader);
    }

    private String getInstanceStateKeyForPage(int i) {
        return getViewHolder(i).getClass().getCanonicalName() + "_savedstate_" + i;
    }

    protected int getRtlPosition(int position) {
        return position;
    }
}
