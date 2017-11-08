package cn.com.xy.sms.sdk.ui.popu.util;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

/* compiled from: Unknown */
public final class c extends LinkMovementMethod {
    private static a c = new a();
    private static c d = null;
    private d a;
    private Spannable b = null;

    private c() {
    }

    public static MovementMethod a() {
        if (d == null) {
            d = new c();
        }
        return d;
    }

    private static d a(TextView textView, Spannable spannable, MotionEvent motionEvent) {
        int x = (((int) motionEvent.getX()) - textView.getTotalPaddingLeft()) + textView.getScrollX();
        int y = (((int) motionEvent.getY()) - textView.getTotalPaddingTop()) + textView.getScrollY();
        Layout layout = textView.getLayout();
        x = layout.getOffsetForHorizontal(layout.getLineForVertical(y), (float) x);
        d[] dVarArr = (d[]) spannable.getSpans(x, x, d.class);
        return dVarArr.length <= 0 ? null : dVarArr[0];
    }

    private void a(Spannable spannable) {
        if (spannable != null) {
            if (c != null) {
                spannable.removeSpan(c);
            }
            this.b = null;
        }
    }

    private void a(Spannable spannable, int i, int i2) {
        if (spannable != null && i != -1 && i2 != -1) {
            if (c != null) {
                spannable.setSpan(c, i, i2, 0);
            }
            this.b = spannable;
        }
    }

    public final void b() {
        a(this.b);
        if (this.a != null) {
            this.a.b(false);
            this.a = null;
        }
        this.b = null;
    }

    public final boolean onGenericMotionEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {
        return super.onGenericMotionEvent(textView, spannable, motionEvent);
    }

    public final boolean onKeyDown(TextView textView, Spannable spannable, int i, KeyEvent keyEvent) {
        return super.onKeyDown(textView, spannable, i, keyEvent);
    }

    public final boolean onKeyOther(TextView textView, Spannable spannable, KeyEvent keyEvent) {
        return super.onKeyOther(textView, spannable, keyEvent);
    }

    public final boolean onKeyUp(TextView textView, Spannable spannable, int i, KeyEvent keyEvent) {
        return super.onKeyUp(textView, spannable, i, keyEvent);
    }

    public final boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {
        ViewGroup viewGroup;
        boolean onTouchEvent;
        boolean z = true;
        boolean z2 = false;
        if (motionEvent.getAction() == 0) {
            this.a = a(textView, spannable, motionEvent);
            int spanStart = spannable.getSpanStart(this.a);
            int spanEnd = spannable.getSpanEnd(this.a);
            if (!(spannable == null || spanStart == -1 || spanEnd == -1)) {
                if (c != null) {
                    spannable.setSpan(c, spanStart, spanEnd, 0);
                }
                this.b = spannable;
            }
            if (this.a != null) {
                this.a.b(true);
                if (z) {
                    a(spannable);
                    z2 = z;
                    viewGroup = (ViewGroup) textView.getParent();
                    while (viewGroup != null) {
                        onTouchEvent = viewGroup.onTouchEvent(motionEvent);
                        if (onTouchEvent) {
                            return onTouchEvent;
                        }
                        viewGroup = (ViewGroup) viewGroup.getParent();
                        z2 = onTouchEvent;
                    }
                } else {
                    z2 = z;
                }
                return z2;
            }
        } else if (motionEvent.getAction() != 2) {
            a(spannable);
            if (this.a != null) {
                this.a.b(false);
                a(spannable);
                super.onTouchEvent(textView, spannable, motionEvent);
                z2 = true;
            }
            this.a = null;
        } else {
            d a = a(textView, spannable, motionEvent);
            if (this.a == null || a == this.a) {
                if (this.a == null) {
                }
                if (z) {
                    a(spannable);
                    z2 = z;
                    viewGroup = (ViewGroup) textView.getParent();
                    while (viewGroup != null) {
                        onTouchEvent = viewGroup.onTouchEvent(motionEvent);
                        if (onTouchEvent) {
                            return onTouchEvent;
                        }
                        viewGroup = (ViewGroup) viewGroup.getParent();
                        z2 = onTouchEvent;
                    }
                } else {
                    z2 = z;
                }
                return z2;
            }
            this.a.b(false);
            this.a = null;
            a(spannable);
            if (z) {
                z2 = z;
            } else {
                a(spannable);
                z2 = z;
                viewGroup = (ViewGroup) textView.getParent();
                while (viewGroup != null) {
                    onTouchEvent = viewGroup.onTouchEvent(motionEvent);
                    if (onTouchEvent) {
                        return onTouchEvent;
                    }
                    viewGroup = (ViewGroup) viewGroup.getParent();
                    z2 = onTouchEvent;
                }
            }
            return z2;
        }
        z = z2;
        if (z) {
            a(spannable);
            z2 = z;
            viewGroup = (ViewGroup) textView.getParent();
            while (viewGroup != null) {
                onTouchEvent = viewGroup.onTouchEvent(motionEvent);
                if (onTouchEvent) {
                    return onTouchEvent;
                }
                viewGroup = (ViewGroup) viewGroup.getParent();
                z2 = onTouchEvent;
            }
        } else {
            z2 = z;
        }
        return z2;
    }

    public final boolean onTrackballEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {
        return super.onTrackballEvent(textView, spannable, motionEvent);
    }
}
