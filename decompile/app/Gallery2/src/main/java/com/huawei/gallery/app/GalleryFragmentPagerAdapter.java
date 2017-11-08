package com.huawei.gallery.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.util.TraceController;

public abstract class GalleryFragmentPagerAdapter extends PagerAdapter {
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    private final FragmentManager mFragmentManager;

    public abstract Fragment getItem(int i);

    public abstract boolean isValid(Fragment fragment);

    public GalleryFragmentPagerAdapter(FragmentManager fm) {
        this.mFragmentManager = fm;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        TraceController.beginSection("GalleryFragmentPagerAdapter.instantiateItem");
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        String name = makeFragmentName(container.getId(), getItemId(position));
        Fragment fragment = this.mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            this.mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            this.mCurTransaction.add(container.getId(), fragment, name);
        }
        if (fragment != this.mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
        TraceController.endSection();
        return fragment;
    }

    public int getItemPosition(Object object) {
        if (isValid((Fragment) object)) {
            return -1;
        }
        return -2;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        TraceController.beginSection("GalleryFragmentPagerAdapter.destroyItem");
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        this.mCurTransaction.detach((Fragment) object);
        TraceController.endSection();
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment object2;
        if (object == null) {
            object2 = this.mFragmentManager.findFragmentByTag(makeFragmentName(container.getId(), getItemId(position)));
        }
        Fragment fragment = object2;
        if (fragment != this.mCurrentPrimaryItem) {
            if (this.mCurrentPrimaryItem != null) {
                this.mCurrentPrimaryItem.setMenuVisibility(false);
                this.mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            this.mCurrentPrimaryItem = fragment;
        }
    }

    public void finishUpdate(ViewGroup container) {
        if (this.mCurTransaction != null) {
            this.mCurTransaction.commitAllowingStateLoss();
            this.mCurTransaction = null;
            this.mFragmentManager.executePendingTransactions();
        }
    }

    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    protected static String makeFragmentName(int viewId, long id) {
        return "hwgallery:switcher:" + viewId + ":" + id;
    }
}
