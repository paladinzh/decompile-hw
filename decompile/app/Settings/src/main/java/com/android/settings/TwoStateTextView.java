package com.android.settings;

import android.widget.TextView;

public class TwoStateTextView {
    int mCheckedTextId;
    int mCurrentTextId;
    TextView mTextView;
    int mUnCheckedTextId;

    public TwoStateTextView(TextView view, int checkedTxtId, int uncheckedTxtId) {
        this.mTextView = view;
        this.mCheckedTextId = checkedTxtId;
        this.mUnCheckedTextId = uncheckedTxtId;
        setChecked(false);
    }

    public void setChecked(boolean isChecked) {
        if (this.mTextView != null) {
            if (isChecked) {
                this.mTextView.setText(this.mCheckedTextId);
                this.mCurrentTextId = this.mCheckedTextId;
            } else {
                this.mTextView.setText(this.mUnCheckedTextId);
                this.mCurrentTextId = this.mUnCheckedTextId;
            }
        }
    }

    public void setEnabled(boolean enable) {
        if (this.mTextView != null) {
            this.mTextView.setEnabled(enable);
        }
    }
}
