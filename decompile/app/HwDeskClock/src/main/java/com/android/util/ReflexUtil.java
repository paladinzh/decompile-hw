package com.android.util;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.vrsystem.IVRListener;
import android.vrsystem.VRSystemServiceManager;
import com.huawei.android.app.ActionBarEx;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflexUtil {
    public static boolean isMethodExist(String _class, String _method) {
        if (_class == null || _method == null) {
            return false;
        }
        try {
            for (Method method : Class.forName(_class).getMethods()) {
                if (method.getName().equals(_method)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            Log.e("ReflexUtil", "isMethodExist 1 : ClassNotFoundException = " + e.getMessage());
            return false;
        }
    }

    public static Object executeStaticMethod(String _class, String _method, Class[] _methodArgType, Object[] _methodArg) {
        if (_class == null || _method == null || _methodArgType == null || _methodArg == null) {
            return null;
        }
        try {
            Class cls = Class.forName(_class);
            Object obj = cls.getMethod(_method, _methodArgType).invoke(cls, _methodArg);
            if (obj == null) {
                obj = "";
            }
            return obj;
        } catch (NoSuchMethodException e) {
            Log.e("ReflexUtil", "executeStaticMethod : NoSuchMethodException = " + e.getMessage());
            return "";
        } catch (ClassNotFoundException e2) {
            Log.e("ReflexUtil", "executeStaticMethod : ClassNotFoundException = " + e2.getMessage());
            return "";
        } catch (IllegalArgumentException e3) {
            Log.e("ReflexUtil", "executeStaticMethod : IllegalArgumentException = " + e3.getMessage());
            return "";
        } catch (IllegalAccessException e4) {
            Log.e("ReflexUtil", "executeStaticMethod : IllegalAccessException = " + e4.getMessage());
            return "";
        } catch (InvocationTargetException e5) {
            Log.e("ReflexUtil", "executeStaticMethod : InvocationTargetException = " + e5.getMessage());
            return "";
        } catch (Throwable th) {
            return "";
        }
    }

    public static void fixInputMethodManagerLeak(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
        if (imm != null) {
            Class<?> clazz = imm.getClass();
            try {
                Method method = clazz.getMethod("windowDismissed", new Class[]{IBinder.class});
                if (context.getWindow().peekDecorView() != null) {
                    method.invoke(clazz, new Object[]{context.getWindow().peekDecorView().getWindowToken()});
                }
            } catch (NoSuchMethodException e) {
                Log.w("ReflexUtil", "fixInputMethodManagerLeak : NoSuchMethodException = " + e.getMessage());
            } catch (IllegalArgumentException e2) {
                Log.w("ReflexUtil", "fixInputMethodManagerLeak : IllegalArgumentException = " + e2.getMessage());
            } catch (IllegalAccessException e3) {
                Log.w("ReflexUtil", "fixInputMethodManagerLeak : IllegalAccessException = " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                Log.w("ReflexUtil", "InvocationTargetException : IllegalAccessException = " + e4.getMessage());
            }
        }
    }

    public static boolean isVRMode() {
        return VRSystemServiceManager.getInstance().isVRMode();
    }

    public static void registerVRListener(Context context, IVRListener listener) {
        if (context != null) {
            VRSystemServiceManager.getInstance().registerVRListener(context, listener);
        }
    }

    public static void unregisterVRListener(Context context, IVRListener listener) {
        if (context != null) {
            VRSystemServiceManager.getInstance().unregisterVRListener(context, listener);
        }
    }

    public static void setActionBarExId(Tab tab, int id) {
        if (isMethodExist(ActionBarEx.class.getName(), "setTabViewId")) {
            ActionBarEx.setTabViewId(tab, id);
        }
    }
}
