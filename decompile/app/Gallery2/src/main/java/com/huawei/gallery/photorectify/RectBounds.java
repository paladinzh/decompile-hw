package com.huawei.gallery.photorectify;

import android.graphics.PointF;
import android.graphics.RectF;
import com.android.gallery3d.util.GalleryUtils;

public class RectBounds {
    private static final /* synthetic */ int[] -com-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues = null;
    private static final int TOUCH_TOLERANCE = GalleryUtils.dpToPixel(20);
    public PointF bottomLeft = new PointF();
    public PointF bottomRight = new PointF();
    public PointF topLeft = new PointF();
    public PointF topRight = new PointF();

    enum TouchPos {
        RECT_POINT_TOPLEFT,
        RECT_POINT_TOPRIGHT,
        RECT_POINT_BOTTOMRIGHT,
        RECT_POINT_BOTTOMLEFT,
        RECT_POINT_INNER
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues() {
        if (-com-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues != null) {
            return -com-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues;
        }
        int[] iArr = new int[TouchPos.values().length];
        try {
            iArr[TouchPos.RECT_POINT_BOTTOMLEFT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TouchPos.RECT_POINT_BOTTOMRIGHT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TouchPos.RECT_POINT_INNER.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[TouchPos.RECT_POINT_TOPLEFT.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[TouchPos.RECT_POINT_TOPRIGHT.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues = iArr;
        return iArr;
    }

    RectBounds() {
    }

    public void offset(float x, float y) {
        this.topLeft.offset(x, y);
        this.topRight.offset(x, y);
        this.bottomLeft.offset(x, y);
        this.bottomRight.offset(x, y);
    }

    public void set(float topLeftX, float topLeftY, float topRightX, float topRightY, float bottomRightX, float bottomRightY, float bottomLeftX, float bottomLeftY) {
        this.topLeft.x = topLeftX;
        this.topLeft.y = topLeftY;
        this.topRight.x = topRightX;
        this.topRight.y = topRightY;
        this.bottomRight.x = bottomRightX;
        this.bottomRight.y = bottomRightY;
        this.bottomLeft.x = bottomLeftX;
        this.bottomLeft.y = bottomLeftY;
    }

    public void set(float[] bounds) {
        if (bounds != null && bounds.length == 8) {
            this.topLeft = new PointF(bounds[0], bounds[1]);
            this.topRight = new PointF(bounds[2], bounds[3]);
            this.bottomRight = new PointF(bounds[4], bounds[5]);
            this.bottomLeft = new PointF(bounds[6], bounds[7]);
        }
    }

    public void set(RectF bounds) {
        if (bounds != null) {
            this.topLeft = new PointF(bounds.left, bounds.top);
            this.topRight = new PointF(bounds.right, bounds.top);
            this.bottomRight = new PointF(bounds.right, bounds.bottom);
            this.bottomLeft = new PointF(bounds.left, bounds.bottom);
        }
    }

    public float[] getRectBounds() {
        return new float[]{this.topLeft.x, this.topLeft.y, this.topRight.x, this.topRight.y, this.bottomRight.x, this.bottomRight.y, this.bottomLeft.x, this.bottomLeft.y};
    }

    public static boolean isConvex(RectBounds rb) {
        PointF[] triangle = new PointF[3];
        for (int i = 0; i < triangle.length; i++) {
            triangle[i] = new PointF(0.0f, 0.0f);
        }
        PointF[] points = new PointF[]{rb.topLeft, rb.topRight, rb.bottomRight, rb.bottomLeft};
        for (int p = 0; p < points.length; p++) {
            int p0 = p % points.length;
            int p1 = (p + 1) % points.length;
            int p2 = (p + 2) % points.length;
            triangle[0].set(points[p0].x, points[p0].y);
            triangle[1].set(points[p1].x, points[p1].y);
            triangle[2].set(points[p2].x, points[p2].y);
            if (!isCW(triangle)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCW(PointF[] vertice) {
        long square = 0;
        for (int v = 0; v < vertice.length; v++) {
            int v_curr = v % vertice.length;
            int v_next = (v + 1) % vertice.length;
            square = (long) (((float) square) + ((vertice[v_curr].x * vertice[v_next].y) - (vertice[v_next].x * vertice[v_curr].y)));
        }
        if (square > 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void convexProcess(TouchPos touchPos, RectBounds rbReal, PointF defaultPoint) {
        if (!(touchPos == null || defaultPoint == null || isConvex(rbReal))) {
            switch (-getcom-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues()[touchPos.ordinal()]) {
                case 1:
                    this.bottomLeft.set(defaultPoint);
                    break;
                case 2:
                    this.bottomRight.set(defaultPoint);
                    break;
                case 3:
                    this.topLeft.set(defaultPoint);
                    break;
                case 4:
                    this.topRight.set(defaultPoint);
                    break;
            }
        }
    }

    public TouchPos getTouchPosition(float x, float y) {
        PointF[] rectPoint = new PointF[]{this.topLeft, this.topRight, this.bottomRight, this.bottomLeft};
        int minDistance = TOUCH_TOLERANCE;
        TouchPos tp = null;
        for (int i = 0; i < rectPoint.length; i++) {
            int distance = checkTouchDistance((int) x, (int) y, rectPoint[i]);
            if (distance > 0 && distance < minDistance) {
                minDistance = distance;
                tp = TouchPos.values()[i];
            }
        }
        return tp;
    }

    private int checkTouchDistance(int x, int y, PointF point) {
        int distance = (int) Math.sqrt(Math.pow((double) (((float) x) - point.x), 2.0d) + Math.pow((double) (((float) y) - point.y), 2.0d));
        if (distance < TOUCH_TOLERANCE) {
            return distance;
        }
        return 0;
    }

    public void moveRectBounds(float x, float y, TouchPos tp) {
        if (tp != null) {
            PointF point = new PointF(x, y);
            switch (-getcom-huawei-gallery-photorectify-RectBounds$TouchPosSwitchesValues()[tp.ordinal()]) {
                case 1:
                    this.bottomLeft.set(point);
                    break;
                case 2:
                    this.bottomRight.set(point);
                    break;
                case 3:
                    this.topLeft.set(point);
                    break;
                case 4:
                    this.topRight.set(point);
                    break;
            }
        }
    }

    public boolean contains(float x, float y) {
        PointF A = this.topLeft;
        PointF B = this.topRight;
        PointF C = this.bottomRight;
        PointF D = this.bottomLeft;
        int a = (int) (((B.x - A.x) * (y - A.y)) - ((B.y - A.y) * (x - A.x)));
        int b = (int) (((C.x - B.x) * (y - B.y)) - ((C.y - B.y) * (x - B.x)));
        int c = (int) (((D.x - C.x) * (y - C.y)) - ((D.y - C.y) * (x - C.x)));
        int d = (int) (((A.x - D.x) * (y - D.y)) - ((A.y - D.y) * (x - D.x)));
        if ((a <= 0 || b <= 0 || c <= 0 || d <= 0) && (a >= 0 || b >= 0 || c >= 0 || d >= 0)) {
            return false;
        }
        return true;
    }
}
