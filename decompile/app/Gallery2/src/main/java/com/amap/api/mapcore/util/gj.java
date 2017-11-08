package com.amap.api.mapcore.util;

import android.content.Context;
import java.io.File;
import java.lang.reflect.Constructor;

/* compiled from: InstanceFactory */
public class gj {
    public static <T> T a(Context context, fh fhVar, String str, Class cls, Class[] clsArr, Object[] objArr) throws ex {
        T a = a(b(context, fhVar), str, clsArr, objArr);
        if (a != null) {
            return a;
        }
        a = a(cls, clsArr, objArr);
        if (a != null) {
            return a;
        }
        throw new ex("获取对象错误");
    }

    public static boolean a(Context context, fh fhVar) {
        try {
            File file = new File(gn.b(context, fhVar.a(), fhVar.b()));
            if (file.exists()) {
                return true;
            }
            gn.a(context, file, fhVar);
            return false;
        } catch (Throwable th) {
            gs.a(th, "IFactory", "isdowned");
            return false;
        }
    }

    private static gk b(Context context, fh fhVar) {
        gk gkVar = null;
        try {
            if (a(context, fhVar)) {
                gkVar = gl.a().a(context, fhVar);
            }
        } catch (Throwable th) {
            gs.a(th, "IFactory", "gIns1");
        }
        return gkVar;
    }

    private static boolean a(gk gkVar) {
        if (gkVar != null && gkVar.a() && gkVar.d) {
            return true;
        }
        return false;
    }

    private static <T> T a(gk gkVar, String str, Class[] clsArr, Object[] objArr) {
        try {
            if (a(gkVar)) {
                Class loadClass = gkVar.loadClass(str);
                if (loadClass != null) {
                    Constructor declaredConstructor = loadClass.getDeclaredConstructor(clsArr);
                    declaredConstructor.setAccessible(true);
                    return declaredConstructor.newInstance(objArr);
                }
            }
        } catch (Throwable th) {
            gs.a(th, "IFactory", "getWrap");
        }
        return null;
    }

    private static <T> T a(Class cls, Class[] clsArr, Object[] objArr) {
        if (cls == null) {
            return null;
        }
        try {
            Constructor declaredConstructor = cls.getDeclaredConstructor(clsArr);
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance(objArr);
        } catch (Throwable th) {
            gs.a(th, "IFactory", "gIns2()");
            return null;
        }
    }
}
