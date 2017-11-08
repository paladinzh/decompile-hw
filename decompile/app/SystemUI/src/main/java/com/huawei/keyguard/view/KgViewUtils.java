package com.huawei.keyguard.view;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;

public class KgViewUtils {
    private static int emuiThemdId = 0;
    private static int[] sViewPos = new int[2];

    public static View inflateViewFromPkg(Context context, String pkgName, String identifier) {
        if (context == null) {
            HwLog.e("KgViewUtils", "CameraLayout context is null");
            return null;
        }
        try {
            Context remoteContext = context.createPackageContext(pkgName, 4);
            if (remoteContext == null) {
                throw new Exception("Emtpry remote Context");
            }
            Resources resources = remoteContext.getResources();
            if (resources == null) {
                throw new Exception("Emtpry resources");
            }
            int layoutId = resources.getIdentifier(identifier, null, null);
            if (layoutId == 0) {
                throw new Exception("Emtpry layoutId");
            }
            LayoutInflater cameraInflater = (LayoutInflater) remoteContext.getSystemService("layout_inflater");
            if (cameraInflater != null) {
                return cameraInflater.cloneInContext(remoteContext).inflate(layoutId, null, false);
            }
            throw new Exception("Emtpry inflator");
        } catch (NameNotFoundException e) {
            HwLog.e("KgViewUtils", "inflateWidgetView:NameNotFoundException", e);
            return null;
        } catch (SecurityException e2) {
            HwLog.e("KgViewUtils", "inflateWidgetView:RuntimeException", e2);
            return null;
        } catch (Exception e3) {
            HwLog.e("KgViewUtils", "inflateWidgetView:Exception", e3);
            return null;
        }
    }

    public static boolean isTouchBelowView(MotionEvent ev, View view, boolean belowBottom) {
        boolean z = false;
        if (view == null || view.getVisibility() != 0) {
            return false;
        }
        int py = (int) ev.getY();
        synchronized (sViewPos) {
            view.getLocationInWindow(sViewPos);
            if ((belowBottom ? view.getHeight() : 0) + sViewPos[1] < py) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isTouchInView(MotionEvent ev, View view, int margin) {
        boolean z = true;
        if (view == null || view.getVisibility() != 0) {
            return false;
        }
        int px = (int) ev.getX();
        int py = (int) ev.getY();
        synchronized (sViewPos) {
            view.getLocationInWindow(sViewPos);
            if (sViewPos[0] - margin > px || px >= (view.getWidth() + sViewPos[0]) + margin || sViewPos[1] - margin > py) {
                z = false;
            } else if (py >= (view.getHeight() + sViewPos[1]) + margin) {
                z = false;
            }
        }
        return z;
    }

    public static boolean isViewCanbeTouched(View view) {
        boolean z = true;
        if (view == null || view.getVisibility() != 0) {
            return false;
        }
        int screenHeight = getScreenHeight(view.getContext());
        int viewHeight = view.getHeight();
        if (viewHeight > 180) {
            viewHeight = 180;
        }
        synchronized (KgViewUtils.class) {
            view.getLocationInWindow(sViewPos);
            if (sViewPos[1] + viewHeight > screenHeight) {
                z = false;
            }
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isTouchInEffectView(MotionEvent ev, View view) {
        boolean z = true;
        if (view == null || view.getVisibility() != 0) {
            return false;
        }
        int screenHeight = getScreenHeight(view.getContext());
        int viewHeight = view.getHeight();
        if (viewHeight > 180) {
            viewHeight = 180;
        }
        int px = (int) ev.getX();
        int py = (int) ev.getY();
        synchronized (KgViewUtils.class) {
            view.getLocationInWindow(sViewPos);
            if (sViewPos[1] + viewHeight > screenHeight) {
                return false;
            } else if (sViewPos[0] > px || px >= view.getWidth() + sViewPos[0] || sViewPos[1] > py) {
                z = false;
            } else if (py >= view.getHeight() + sViewPos[1]) {
                z = false;
            }
        }
    }

    private static int getScreenHeight(Context context) {
        Point point = HwUnlockUtils.getPoint(context);
        return point == null ? Integer.MAX_VALUE : point.y;
    }

    public static void setViewVisibility(View view, int visible) {
        if (view != null) {
            view.setVisibility(visible);
        } else {
            HwLog.w("KgViewUtils", "setViewVisibility for null View", new Exception());
        }
    }

    public static void setSelected(View view, boolean selected) {
        if (view != null) {
            view.setSelected(selected);
        } else {
            HwLog.w("KgViewUtils", "setSelected for null View", new Exception());
        }
    }

    public static boolean isCancelAction(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 3 || action == 1) {
            return true;
        }
        return false;
    }

    public static void restoreViewState(View v) {
        if (v != null) {
            if (v.getAlpha() < 0.999f || v.getScaleX() < 0.999f) {
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
                v.setAlpha(1.0f);
            }
        }
    }

    public static boolean isViewVisible(View v) {
        if (v == null || !v.isAttachedToWindow() || v.getVisibility() != 0 || v.getAlpha() <= 0.001f) {
            return false;
        }
        ViewParent vp = v.getParent();
        if (vp instanceof View) {
            return isViewVisible((View) vp);
        }
        return true;
    }

    public static LayoutInflater createLayoutInflater(Context context) {
        if (context == null) {
            HwLog.e("KgViewUtils", "createLayoutInflater with invalide context.");
            return null;
        }
        if (emuiThemdId == 0) {
            emuiThemdId = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
        if (emuiThemdId != 0) {
            return LayoutInflater.from(new ContextThemeWrapper(context, emuiThemdId));
        }
        return LayoutInflater.from(context);
    }
}
