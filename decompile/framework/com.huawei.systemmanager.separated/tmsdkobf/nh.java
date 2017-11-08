package tmsdkobf;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/* compiled from: Unknown */
public final class nh {
    private static Class<?> BZ;
    private static Method Ca;
    private static Method Cb;
    private static Method Cc;
    private static Method Cd;
    private static IBinder mRemote;
    private static HashMap<String, IBinder> sCache;

    static {
        try {
            BZ = Class.forName("android.os.ServiceManager");
            Ca = BZ.getDeclaredMethod("getService", new Class[]{String.class});
            Cb = BZ.getDeclaredMethod("addService", new Class[]{String.class, IBinder.class});
            Cc = BZ.getDeclaredMethod("checkService", new Class[]{String.class});
            Cd = BZ.getDeclaredMethod("listServices", new Class[0]);
            AccessibleObject declaredField = BZ.getDeclaredField("sCache");
            declaredField.setAccessible(true);
            sCache = (HashMap) declaredField.get(null);
            declaredField = BZ.getDeclaredField("sServiceManager");
            declaredField.setAccessible(true);
            mRemote = ((IInterface) declaredField.get(null)).asBinder();
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        } catch (Throwable e22) {
            e22.printStackTrace();
        } catch (Throwable e222) {
            e222.printStackTrace();
        } catch (Throwable e2222) {
            e2222.printStackTrace();
        } catch (Throwable e22222) {
            e22222.printStackTrace();
        }
    }

    private static Object a(Method method, Object... objArr) {
        Object obj = null;
        try {
            obj = method.invoke(null, objArr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return obj;
    }

    public static IBinder checkService(String str) {
        return (IBinder) a(Cc, str);
    }

    public static IBinder getService(String str) {
        return (IBinder) a(Ca, str);
    }
}
