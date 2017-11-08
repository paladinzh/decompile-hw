package com.android.mms.attachment.ui;

import android.os.Parcelable;

public interface PersistentInstanceState {
    void restoreState(Parcelable parcelable);

    Parcelable saveState();
}
