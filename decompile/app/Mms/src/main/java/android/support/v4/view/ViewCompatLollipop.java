package android.support.v4.view;

import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;

class ViewCompatLollipop {
    ViewCompatLollipop() {
    }

    public static void setOnApplyWindowInsetsListener(View view, final OnApplyWindowInsetsListener listener) {
        if (listener == null) {
            view.setOnApplyWindowInsetsListener(null);
        } else {
            view.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    return ((WindowInsetsCompatApi21) listener.onApplyWindowInsets(view, new WindowInsetsCompatApi21(windowInsets))).unwrap();
                }
            });
        }
    }

    public static WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        if (!(insets instanceof WindowInsetsCompatApi21)) {
            return insets;
        }
        WindowInsets unwrapped = ((WindowInsetsCompatApi21) insets).unwrap();
        WindowInsets result = v.onApplyWindowInsets(unwrapped);
        if (result != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }

    public static WindowInsetsCompat dispatchApplyWindowInsets(View v, WindowInsetsCompat insets) {
        if (!(insets instanceof WindowInsetsCompatApi21)) {
            return insets;
        }
        WindowInsets unwrapped = ((WindowInsetsCompatApi21) insets).unwrap();
        WindowInsets result = v.dispatchApplyWindowInsets(unwrapped);
        if (result != unwrapped) {
            return new WindowInsetsCompatApi21(result);
        }
        return insets;
    }
}
