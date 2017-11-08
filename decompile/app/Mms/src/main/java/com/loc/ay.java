package com.loc;

import android.content.Context;
import java.lang.reflect.Constructor;

/* compiled from: InstanceFactory */
public class ay {
    public static <T> T a(Context context, v vVar, String str, Class cls, Class[] clsArr, Object[] objArr) throws l {
        bb a;
        try {
            a = bb.a(context, vVar, ba.a(context, vVar.a(), vVar.b()), ba.a(context), null, context.getClassLoader());
        } catch (Throwable th) {
            aa.a(th, "InstanceFactory", "getInstance");
            a = null;
        }
        if (a != null) {
            try {
                if (a.a() && a.a) {
                    Class loadClass = a.loadClass(str);
                    if (loadClass != null) {
                        return loadClass.getConstructor(clsArr).newInstance(objArr);
                    }
                }
            } catch (Throwable th2) {
                aa.a(th2, "InstanceFactory", "getInstance()");
            } catch (Throwable th22) {
                aa.a(th22, "InstanceFactory", "getInstance()");
            } catch (Throwable th222) {
                aa.a(th222, "InstanceFactory", "getInstance()");
            } catch (Throwable th2222) {
                aa.a(th2222, "InstanceFactory", "getInstance()");
            } catch (Throwable th22222) {
                aa.a(th22222, "InstanceFactory", "getInstance()");
            } catch (Throwable th222222) {
                aa.a(th222222, "InstanceFactory", "getInstance()");
            } catch (Throwable th2222222) {
                aa.a(th2222222, "InstanceFactory", "getInstance()");
            }
        }
        try {
            Constructor constructor = cls.getConstructor(clsArr);
            constructor.setAccessible(true);
            return constructor.newInstance(objArr);
        } catch (Throwable th22222222) {
            aa.a(th22222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        } catch (Throwable th222222222) {
            aa.a(th222222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        } catch (Throwable th2222222222) {
            aa.a(th2222222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        } catch (Throwable th22222222222) {
            aa.a(th22222222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        } catch (Throwable th222222222222) {
            aa.a(th222222222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        } catch (Throwable th2222222222222) {
            aa.a(th2222222222222, "InstanceFactory", "getInstance()");
            throw new l("获取对象错误");
        }
    }
}
