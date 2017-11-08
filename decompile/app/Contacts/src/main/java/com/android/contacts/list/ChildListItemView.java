package com.android.contacts.list;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.gms.R;

public class ChildListItemView extends ContactListItemView {
    private int mGroupPosition = -1;

    public ChildListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int paddingEnd = context.getResources().getDimensionPixelSize(R.dimen.contact_right_margin);
        if (this.mIsMirror) {
            this.mContentPaddingLeft = paddingEnd;
        } else {
            this.mContentPaddingRight = paddingEnd;
        }
    }

    public void setCheckedState(boolean checked) {
        if (this.mIsChecked != checked) {
            toggle();
        }
    }

    public void setChecked(boolean checked) {
    }

    public void toggle() {
        super.toggle();
        this.mCheckBox.setChecked(this.mIsChecked);
    }

    public void setGroupPosition(int aGroupPosition) {
        this.mGroupPosition = aGroupPosition;
    }
}
