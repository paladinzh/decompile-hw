package android.support.v4.view;

import android.view.View;
import android.view.ViewParent;

class ViewCompatBase {
    ViewCompatBase() {
    }

    static boolean isAttachedToWindow(View view) {
        return view.getWindowToken() != null;
    }

    static void offsetTopAndBottom(View view, int offset) {
        int currentTop = view.getTop();
        view.offsetTopAndBottom(offset);
        if (offset != 0) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                int absOffset = Math.abs(offset);
                ((View) parent).invalidate(view.getLeft(), currentTop - absOffset, view.getRight(), (view.getHeight() + currentTop) + absOffset);
                return;
            }
            view.invalidate();
        }
    }

    static void offsetLeftAndRight(View view, int offset) {
        int currentLeft = view.getLeft();
        view.offsetLeftAndRight(offset);
        if (offset != 0) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                int absOffset = Math.abs(offset);
                ((View) parent).invalidate(currentLeft - absOffset, view.getTop(), (view.getWidth() + currentLeft) + absOffset, view.getBottom());
                return;
            }
            view.invalidate();
        }
    }
}
