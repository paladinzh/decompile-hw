package com.android.gallery3d.ui;

import android.database.DataSetObserver;
import android.widget.AdapterView;

public class PhotoShareDataSetObserver extends DataSetObserver {
    private AdapterView<?> mView;

    public PhotoShareDataSetObserver(AdapterView<?> view) {
        this.mView = view;
    }

    public void onChanged() {
        super.onChanged();
        if (this.mView != null) {
            this.mView.requestLayout();
        }
    }

    public void onInvalidated() {
        super.onInvalidated();
        if (this.mView != null) {
            this.mView.invalidate();
        }
    }
}
