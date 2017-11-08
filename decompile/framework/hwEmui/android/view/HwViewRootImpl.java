package android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.view.WindowManager.LayoutParams;
import android.vkey.SettingsHelper;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.HwSettings.System;

public class HwViewRootImpl implements IHwViewRootImpl {
    static final boolean isHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static HwViewRootImpl mInstance = null;
    private boolean isDecorPointerEvent = false;
    Point mDisplayPoint;
    int mHitRegionToMax = 20;

    protected HwViewRootImpl() {
    }

    public static synchronized HwViewRootImpl getDefault() {
        HwViewRootImpl hwViewRootImpl;
        synchronized (HwViewRootImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewRootImpl();
            }
            hwViewRootImpl = mInstance;
        }
        return hwViewRootImpl;
    }

    public void clearDisplayPoint() {
        this.mDisplayPoint = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean filterDecorPointerEvent(Context context, MotionEvent event, int action, LayoutParams windowattr, Display disp) {
        boolean z = true;
        if (context == null || !isHwNaviBar || disp == null || windowattr == null || SettingsHelper.isTouchPlusOn(context)) {
            return false;
        }
        boolean ret = this.isDecorPointerEvent;
        if (action == 0) {
            Point pt = getDisplayPoint(disp);
            this.mHitRegionToMax = (int) (((double) context.getResources().getDimensionPixelSize(17104920)) / 3.5d);
            if (pt.y > pt.x) {
                boolean z2;
                if (event.getRawY() > ((float) (pt.y - this.mHitRegionToMax))) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.isDecorPointerEvent = z2;
            } else {
                this.isDecorPointerEvent = event.getRawX() > ((float) (pt.x - this.mHitRegionToMax));
            }
            if (!this.isDecorPointerEvent || Global.getInt(context.getContentResolver(), System.NAVIGATIONBAR_IS_MIN, 0) == 0) {
                z = false;
            } else if ((windowattr.privateFlags & 1024) != 0) {
                z = false;
            }
            this.isDecorPointerEvent = z;
            ret = this.isDecorPointerEvent;
        } else if (action == 3) {
            this.isDecorPointerEvent = false;
        } else if (action == 1) {
            this.isDecorPointerEvent = false;
        }
        return ret;
    }

    private Point getDisplayPoint(Display disp) {
        if (this.mDisplayPoint == null) {
            Point pt = new Point();
            disp.getRealSize(pt);
            this.mDisplayPoint = pt;
        }
        return this.mDisplayPoint;
    }

    public boolean shouldQueueInputEvent(InputEvent event, Context context, View view) {
        int i = 0;
        if (!(event instanceof MotionEvent)) {
            return true;
        }
        boolean isSystemWideTouchType;
        if (((MotionEvent) event).getToolType(0) == 7) {
            isSystemWideTouchType = true;
        } else {
            isSystemWideTouchType = false;
        }
        if (isSystemWideTouchType) {
            i = FingerSenseSettings.isFingerSenseEnabled(context.getContentResolver());
        }
        if (i == 0) {
            return true;
        }
        boolean isViewCapturingSystemWideTouchTypes = false;
        if (view != null) {
            Context viewContext = view.getContext();
            if (viewContext instanceof Activity) {
                Window window = ((Activity) viewContext).getWindow();
                if (window != null) {
                    isViewCapturingSystemWideTouchTypes = (window.getAttributes().flags & 4096) != 0;
                }
            } else if (viewContext instanceof ContextThemeWrapper) {
                isViewCapturingSystemWideTouchTypes = ((ContextThemeWrapper) viewContext).getBaseContext() instanceof InputMethodService;
            }
        }
        return isViewCapturingSystemWideTouchTypes;
    }
}
