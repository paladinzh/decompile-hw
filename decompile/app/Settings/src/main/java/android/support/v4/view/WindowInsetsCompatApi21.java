package android.support.v4.view;

import android.view.WindowInsets;

class WindowInsetsCompatApi21 extends WindowInsetsCompat {
    private final WindowInsets mSource;

    WindowInsetsCompatApi21(WindowInsets source) {
        this.mSource = source;
    }

    public int getSystemWindowInsetLeft() {
        return this.mSource.getSystemWindowInsetLeft();
    }

    public int getSystemWindowInsetTop() {
        return this.mSource.getSystemWindowInsetTop();
    }

    public int getSystemWindowInsetRight() {
        return this.mSource.getSystemWindowInsetRight();
    }

    public int getSystemWindowInsetBottom() {
        return this.mSource.getSystemWindowInsetBottom();
    }

    public boolean isConsumed() {
        return this.mSource.isConsumed();
    }

    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        return new WindowInsetsCompatApi21(this.mSource.replaceSystemWindowInsets(left, top, right, bottom));
    }

    WindowInsets unwrap() {
        return this.mSource;
    }
}
