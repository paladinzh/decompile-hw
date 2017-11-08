package android.support.v17.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

class GuidedActionItemContainer extends NonOverlappingLinearLayoutWithForeground {
    private boolean mFocusOutAllowed;

    public GuidedActionItemContainer(Context context) {
        this(context, null);
    }

    public GuidedActionItemContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuidedActionItemContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFocusOutAllowed = true;
    }

    public View focusSearch(View focused, int direction) {
        if (this.mFocusOutAllowed || !Util.isDescendant(this, focused)) {
            return super.focusSearch(focused, direction);
        }
        View view = super.focusSearch(focused, direction);
        if (Util.isDescendant(this, view)) {
            return view;
        }
        return null;
    }
}
