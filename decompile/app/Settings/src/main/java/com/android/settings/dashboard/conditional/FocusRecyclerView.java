package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class FocusRecyclerView extends RecyclerView {
    private FocusListener mListener;

    public interface FocusListener {
        void onWindowFocusChanged(boolean z);
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mListener != null) {
            this.mListener.onWindowFocusChanged(hasWindowFocus);
        }
    }

    public void setListener(FocusListener listener) {
        this.mListener = listener;
    }
}
