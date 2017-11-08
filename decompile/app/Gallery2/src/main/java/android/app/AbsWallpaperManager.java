package android.app;

import android.graphics.Bitmap;
import android.graphics.Rect;

public abstract class AbsWallpaperManager {

    public interface IBlurWallpaperCallback {
    }

    public Bitmap getBlurBitmap(Rect rect) {
        return null;
    }

    public void setCallback(IBlurWallpaperCallback callback) {
    }
}
