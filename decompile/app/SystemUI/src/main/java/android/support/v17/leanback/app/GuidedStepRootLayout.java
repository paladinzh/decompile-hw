package android.support.v17.leanback.app;

import android.content.Context;
import android.support.v17.leanback.widget.Util;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

class GuidedStepRootLayout extends LinearLayout {
    private boolean mFocusOutEnd = false;
    private boolean mFocusOutStart = false;

    public GuidedStepRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GuidedStepRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View focusSearch(View focused, int direction) {
        View newFocus = super.focusSearch(focused, direction);
        if ((direction != 17 && direction != 66) || Util.isDescendant(this, newFocus)) {
            return newFocus;
        }
        if (getLayoutDirection() != 0 ? direction == 66 : direction == 17) {
            if (!this.mFocusOutStart) {
                return focused;
            }
        } else if (!this.mFocusOutEnd) {
            return focused;
        }
        return newFocus;
    }
}
