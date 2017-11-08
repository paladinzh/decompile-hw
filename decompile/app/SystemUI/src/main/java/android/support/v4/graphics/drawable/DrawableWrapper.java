package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;

public interface DrawableWrapper {
    Drawable getWrappedDrawable();

    void setWrappedDrawable(Drawable drawable);
}
