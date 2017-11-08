package com.amap.api.mapcore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;

/* compiled from: MapOverlayViewGroup */
class az extends ViewGroup {
    private ab a;

    /* compiled from: MapOverlayViewGroup */
    public static class a extends LayoutParams {
        public FPoint a = null;
        public int b = 0;
        public int c = 0;
        public int d = 51;

        public a(int i, int i2, FPoint fPoint, int i3, int i4, int i5) {
            super(i, i2);
            this.a = fPoint;
            this.b = i3;
            this.c = i4;
            this.d = i5;
        }

        public a(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public a(LayoutParams layoutParams) {
            super(layoutParams);
        }
    }

    public az(Context context) {
        super(context);
    }

    public az(Context context, ab abVar) {
        super(context);
        this.a = abVar;
        setBackgroundColor(-1);
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt != null) {
                if (childAt.getLayoutParams() instanceof a) {
                    a(childAt, (a) childAt.getLayoutParams());
                } else {
                    a(childAt, childAt.getLayoutParams());
                }
            }
        }
    }

    private void a(View view, LayoutParams layoutParams) {
        int[] iArr = new int[2];
        a(view, layoutParams.width, layoutParams.height, iArr);
        if (view instanceof ar) {
            a(view, iArr[0], iArr[1], 20, (this.a.I().y - 80) - iArr[1], 51);
            return;
        }
        a(view, iArr[0], iArr[1], 0, 0, 51);
    }

    private void a(View view, a aVar) {
        int[] iArr = new int[2];
        a(view, aVar.width, aVar.height, iArr);
        if (view instanceof bs) {
            a(view, iArr[0], iArr[1], getWidth() - iArr[0], getHeight(), aVar.d);
        } else if (view instanceof as) {
            a(view, iArr[0], iArr[1], getWidth() - iArr[0], iArr[1], aVar.d);
        } else if (view instanceof r) {
            a(view, iArr[0], iArr[1], 0, 0, aVar.d);
        } else if (aVar.a != null) {
            IPoint iPoint = new IPoint();
            this.a.c().map2Win(aVar.a.x, aVar.a.y, iPoint);
            iPoint.x += aVar.b;
            iPoint.y += aVar.c;
            a(view, iArr[0], iArr[1], iPoint.x, iPoint.y, aVar.d);
            if (view.getVisibility() == 0) {
                a();
            }
        }
    }

    protected void a() {
    }

    private void a(View view, int i, int i2, int[] iArr) {
        if (view instanceof ListView) {
            View view2 = (View) view.getParent();
            if (view2 != null) {
                iArr[0] = view2.getWidth();
                iArr[1] = view2.getHeight();
            }
        }
        if (i <= 0 || i2 <= 0) {
            view.measure(0, 0);
        }
        if (i == -2) {
            iArr[0] = view.getMeasuredWidth();
        } else if (i != -1) {
            iArr[0] = i;
        } else {
            iArr[0] = getMeasuredWidth();
        }
        if (i2 == -2) {
            iArr[1] = view.getMeasuredHeight();
        } else if (i2 != -1) {
            iArr[1] = i2;
        } else {
            iArr[1] = getMeasuredHeight();
        }
    }

    private void a(View view, int i, int i2, int i3, int i4, int i5) {
        int i6 = i5 & 7;
        int i7 = i5 & 112;
        if (i6 == 5) {
            i3 -= i;
        } else if (i6 == 1) {
            i3 -= i / 2;
        }
        if (i7 == 80) {
            i4 -= i2;
        } else if (i7 == 17) {
            i4 -= i2 / 2;
        } else if (i7 == 16) {
            i4 = (i4 / 2) - (i2 / 2);
        }
        view.layout(i3, i4, i3 + i, i4 + i2);
    }
}
