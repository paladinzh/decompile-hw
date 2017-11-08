package com.android.mms.attachment.ui;

import android.view.View;
import android.view.ViewGroup;

public interface PagerViewHolder extends PersistentInstanceState {
    View destroyView();

    View getView(ViewGroup viewGroup);
}
