package com.android.dialer.greeting.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import com.google.android.gms.R;

public class ChoiceView extends FrameLayout implements Checkable {
    private boolean mIsMultiMode;
    private CheckedTextView mTextView;
    private CheckedTextView mTextView2;

    public ChoiceView(Context context) {
        this(context, null);
    }

    public ChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.singleormultichoice, this);
        this.mTextView = (CheckedTextView) findViewById(16908308);
        this.mTextView2 = (CheckedTextView) findViewById(16908309);
    }

    public void changeMode(boolean isMultiMode) {
        int i;
        int i2 = 0;
        this.mIsMultiMode = isMultiMode;
        CheckedTextView checkedTextView = this.mTextView;
        if (this.mIsMultiMode) {
            i = 8;
        } else {
            i = 0;
        }
        checkedTextView.setVisibility(i);
        CheckedTextView checkedTextView2 = this.mTextView2;
        if (!this.mIsMultiMode) {
            i2 = 8;
        }
        checkedTextView2.setVisibility(i2);
    }

    public void setText(String text) {
        getTextView().setText(text);
    }

    public void setTextViewEnabled(boolean enabled) {
        getTextView().setEnabled(enabled);
    }

    public void setChecked(boolean checked) {
        getTextView().setChecked(checked);
    }

    public boolean isChecked() {
        return getTextView().isChecked();
    }

    public void toggle() {
        getTextView().toggle();
    }

    public CheckedTextView getTextView() {
        return this.mIsMultiMode ? this.mTextView2 : this.mTextView;
    }
}
