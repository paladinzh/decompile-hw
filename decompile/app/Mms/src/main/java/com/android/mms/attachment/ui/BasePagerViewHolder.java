package com.android.mms.attachment.ui;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

public abstract class BasePagerViewHolder implements PagerViewHolder {
    protected Parcelable mSavedState;
    protected View mView;

    protected abstract View createView(ViewGroup viewGroup);

    public Parcelable saveState() {
        savePendingState();
        return this.mSavedState;
    }

    public void restoreState(Parcelable restoredState) {
        if (restoredState != null) {
            this.mSavedState = restoredState;
            restorePendingState();
        }
    }

    public View destroyView() {
        savePendingState();
        View retView = this.mView;
        this.mView = null;
        return retView;
    }

    public View getView(ViewGroup container) {
        if (this.mView == null) {
            this.mView = createView(container);
            restorePendingState();
        }
        return this.mView;
    }

    private void savePendingState() {
        if (this.mView != null && (this.mView instanceof PersistentInstanceState)) {
            this.mSavedState = ((PersistentInstanceState) this.mView).saveState();
        }
    }

    private void restorePendingState() {
        if (this.mView != null && (this.mView instanceof PersistentInstanceState) && this.mSavedState != null) {
            ((PersistentInstanceState) this.mView).restoreState(this.mSavedState);
        }
    }
}
