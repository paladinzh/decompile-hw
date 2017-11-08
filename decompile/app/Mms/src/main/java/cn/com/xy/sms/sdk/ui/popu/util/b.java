package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;

/* compiled from: Unknown */
public final class b {
    private int a = 1;
    private int b = 1;
    private int c = 1;
    private int d = 0;
    private float e = 0.0f;
    private float f = 0.0f;
    private float g = 0.0f;
    private float h = 0.0f;
    private int i = 0;
    private int j = 1;
    private int k = 0;
    private int l = 0;
    private int m = 0;
    private int n = 0;
    private int o = 0;
    private int p = 0;
    private int q = 0;
    private int r = -1;
    private int s = 0;

    public static b a(Context context, String str) {
        if (str == null) {
            return null;
        }
        try {
            b bVar = new b();
            try {
                for (String str2 : str.split(";")) {
                    if (str2.startsWith("TL")) {
                        bVar.e = (float) ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("TR")) {
                        bVar.f = (float) ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("BL")) {
                        bVar.g = (float) ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("BR")) {
                        bVar.h = (float) ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("TP")) {
                        bVar.i = Integer.valueOf(str2.substring(2)).intValue();
                    } else if (str2.startsWith("SC")) {
                        bVar.j = Color.parseColor(str2.substring(2));
                    } else if (str2.startsWith("SW")) {
                        bVar.k = Integer.valueOf(str2.substring(2)).intValue();
                    } else if (str2.startsWith("DW")) {
                        bVar.l = Integer.valueOf(str2.substring(2)).intValue();
                    } else if (str2.startsWith("DG")) {
                        bVar.m = Integer.valueOf(str2.substring(2)).intValue();
                    } else if (str2.startsWith("CX")) {
                        bVar.p = ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("CY")) {
                        bVar.q = ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("GT")) {
                        bVar.r = Integer.valueOf(str2.substring(2)).intValue();
                    } else if (str2.startsWith("GR")) {
                        bVar.s = ViewUtil.dp2px(context, Integer.valueOf(str2.substring(2)).intValue());
                    } else if (str2.startsWith("W")) {
                        bVar.n = ViewUtil.dp2px(context, Integer.valueOf(str2.substring(1)).intValue());
                    } else if (str2.startsWith(PartViewParam.HEAD)) {
                        bVar.o = ViewUtil.dp2px(context, Integer.valueOf(str2.substring(1)).intValue());
                    } else if (str2.startsWith("S")) {
                        bVar.a = Color.parseColor(str2.substring(1));
                    } else if (str2.startsWith("C")) {
                        bVar.b = Color.parseColor(str2.substring(1));
                    } else if (str2.startsWith("E")) {
                        bVar.c = Color.parseColor(str2.substring(1));
                    } else if (str2.startsWith("A")) {
                        bVar.d = Integer.valueOf(str2.substring(1)).intValue();
                    }
                }
                return bVar;
            } catch (Throwable th) {
                return bVar;
            }
        } catch (Throwable th2) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final Drawable a() {
        Drawable gradientDrawable;
        try {
            int[] iArr;
            Drawable drawable;
            Orientation orientation = Orientation.LEFT_RIGHT;
            try {
                switch (this.d) {
                    case 0:
                        orientation = Orientation.LEFT_RIGHT;
                        break;
                    case 1:
                        orientation = Orientation.RIGHT_LEFT;
                        break;
                    case 2:
                        orientation = Orientation.TOP_BOTTOM;
                        break;
                    case 3:
                        orientation = Orientation.BOTTOM_TOP;
                        break;
                    case 4:
                        orientation = Orientation.TL_BR;
                        break;
                    case 5:
                        orientation = Orientation.TR_BL;
                        break;
                    case 6:
                        orientation = Orientation.BL_TR;
                        break;
                    case 7:
                        orientation = Orientation.BR_TL;
                        break;
                }
            } catch (Throwable th) {
            }
            if (this.b != 1) {
                if (!(this.a == 1 || this.c == 1)) {
                    iArr = new int[]{this.a, this.b, this.c};
                    if (iArr != null) {
                        try {
                            if (this.a == 1) {
                                drawable = null;
                            } else {
                                drawable = new GradientDrawable();
                                try {
                                    drawable.setColor(this.a);
                                } catch (Throwable th2) {
                                }
                            }
                        } catch (Throwable th3) {
                            drawable = null;
                        }
                    } else {
                        gradientDrawable = new GradientDrawable(orientation, iArr);
                        try {
                            gradientDrawable.setShape(this.i);
                            drawable = gradientDrawable;
                        } catch (Throwable th4) {
                            drawable = gradientDrawable;
                        }
                    }
                    if (drawable != null) {
                        return null;
                    }
                    drawable.setCornerRadii(new float[]{this.e, this.e, this.f, this.f, this.g, this.g, this.h, this.h});
                    if (this.j != 1) {
                        drawable.setStroke(this.k, this.j, (float) this.l, (float) this.m);
                    }
                    if (this.n > 0 && this.o > 0) {
                        drawable.setSize(this.n, this.o);
                    }
                    if (this.p > 0 && this.q > 0) {
                        drawable.setGradientCenter((float) this.p, (float) this.q);
                    }
                    if (this.r >= 0) {
                        drawable.setGradientType(this.r);
                    }
                    if (this.s > 0) {
                        drawable.setGradientRadius((float) this.s);
                    }
                    return drawable;
                }
            }
            if (this.c == 1 || this.a == 1) {
                iArr = null;
                if (iArr != null) {
                    gradientDrawable = new GradientDrawable(orientation, iArr);
                    gradientDrawable.setShape(this.i);
                    drawable = gradientDrawable;
                } else if (this.a == 1) {
                    drawable = null;
                } else {
                    drawable = new GradientDrawable();
                    drawable.setColor(this.a);
                }
                if (drawable != null) {
                    return null;
                }
                drawable.setCornerRadii(new float[]{this.e, this.e, this.f, this.f, this.g, this.g, this.h, this.h});
                if (this.j != 1) {
                    drawable.setStroke(this.k, this.j, (float) this.l, (float) this.m);
                }
                drawable.setSize(this.n, this.o);
                drawable.setGradientCenter((float) this.p, (float) this.q);
                if (this.r >= 0) {
                    drawable.setGradientType(this.r);
                }
                if (this.s > 0) {
                    drawable.setGradientRadius((float) this.s);
                }
                return drawable;
            }
            iArr = new int[]{this.a, this.c};
            if (iArr != null) {
                gradientDrawable = new GradientDrawable(orientation, iArr);
                gradientDrawable.setShape(this.i);
                drawable = gradientDrawable;
            } else if (this.a == 1) {
                drawable = new GradientDrawable();
                drawable.setColor(this.a);
            } else {
                drawable = null;
            }
            if (drawable != null) {
                return null;
            }
            drawable.setCornerRadii(new float[]{this.e, this.e, this.f, this.f, this.g, this.g, this.h, this.h});
            if (this.j != 1) {
                drawable.setStroke(this.k, this.j, (float) this.l, (float) this.m);
            }
            drawable.setSize(this.n, this.o);
            drawable.setGradientCenter((float) this.p, (float) this.q);
            if (this.r >= 0) {
                drawable.setGradientType(this.r);
            }
            if (this.s > 0) {
                drawable.setGradientRadius((float) this.s);
            }
            return drawable;
        } catch (Throwable th5) {
            return null;
        }
    }
}
