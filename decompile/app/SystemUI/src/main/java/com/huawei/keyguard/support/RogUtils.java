package com.huawei.keyguard.support;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.view.IWindowManager.Stub;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;

public class RogUtils {
    public static Rect checkRectSize(Context context, Rect r) {
        int width = SystemProperties.getInt("persist.sys.rog.width", 0);
        if (width <= 0) {
            return r;
        }
        int height = SystemProperties.getInt("persist.sys.rog.height", 0);
        Point oPoint = resetPointWidthHeight(getOriginDisplayPoint(context));
        if (height <= 0 || oPoint.x <= 0 || oPoint.y <= 0) {
            return r;
        }
        if (oPoint.x == width && oPoint.y == height) {
            return r;
        }
        return new Rect((r.left * width) / oPoint.x, (r.top * height) / oPoint.y, (r.right * width) / oPoint.x, (r.bottom * height) / oPoint.y);
    }

    private static Point resetPointWidthHeight(Point point) {
        if (point.x <= point.y) {
            return point;
        }
        int cacheX = point.x;
        point.x = point.y;
        point.y = cacheX;
        return point;
    }

    public static Point getOriginDisplayPoint(Context context) {
        Point initialSize = new Point();
        try {
            Stub.asInterface(ServiceManager.getService("window")).getInitialDisplaySize(0, initialSize);
            return initialSize;
        } catch (RemoteException e) {
            HwLog.e("RogUtils", "getOriginDisplayPoint Fail", e);
            return HwUnlockUtils.getPoint(context);
        } catch (Exception e2) {
            HwLog.e("RogUtils", "getOriginDisplayPoint Fail", e2);
            return HwUnlockUtils.getPoint(context);
        }
    }
}
