package com.huawei.hwid.c.a.a;

import com.huawei.hwid.core.d.b.e;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;

class b implements PrivilegedAction<Object> {
    final /* synthetic */ boolean a;
    final /* synthetic */ a b;

    b(a aVar, boolean z) {
        this.b = aVar;
        this.a = z;
    }

    public Object run() {
        try {
            Field declaredField = this.b.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
            declaredField.setAccessible(true);
            declaredField.set(this.b, Boolean.valueOf(this.a));
            if (this.a) {
                this.b.dismiss();
            }
        } catch (RuntimeException e) {
            e.d("CustomAlertDialog", "RuntimeException: " + e.getMessage());
        } catch (Exception e2) {
            e.d("CustomAlertDialog", "Exception: " + e2.getMessage());
        }
        return null;
    }
}
