package com.android.contacts.list;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import com.google.android.gms.R;

public class GroupListItemView extends LinearLayout implements Checkable {
    private CheckBox mCheckBox;
    private boolean mIsChecked;

    public GroupListItemView(Context context) {
        super(context);
    }

    public GroupListItemView(Context context, AttributeSet arrts) {
        super(context, arrts);
    }

    public GroupListItemView(Context context, AttributeSet arrts, int style) {
        super(context, arrts, style);
    }

    public void showCheckBox() {
        if (this.mCheckBox == null) {
            this.mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        }
        this.mCheckBox.setVisibility(0);
    }

    public void hideCheckBox() {
        if (this.mCheckBox != null) {
            this.mCheckBox.setVisibility(8);
        }
    }

    public void toggle() {
        this.mIsChecked = !this.mIsChecked;
        this.mCheckBox.setChecked(this.mIsChecked);
    }

    public CheckBox getCheckBox() {
        return this.mCheckBox;
    }

    public void setCheckedState(boolean checked) {
        if (this.mIsChecked != checked) {
            toggle();
        }
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public void setChecked(boolean checked) {
    }
}
