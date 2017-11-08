package com.huawei.keyguard.dynamiclockscreen;

import android.app.ActivityThread;
import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources.NotFoundException;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.keyguard.R$array;
import com.huawei.keyguard.util.OsUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicUnlockUtils {
    private static Map<String, String[]> sDynamicWhiteMap = new HashMap();

    public static final void init(Context context) {
        if (context != null) {
            try {
                List<String> dynamicWhiteList = Arrays.asList(context.getResources().getStringArray(R$array.dynamic_lock_white_list));
                if (dynamicWhiteList.size() > 0) {
                    sDynamicWhiteMap = parthWhiteList(dynamicWhiteList);
                }
            } catch (NotFoundException e) {
                Log.w("DynamicUnlockUtils", "R.array.dynamic_lock_white_list init error.");
            } catch (Exception e2) {
                Log.w("DynamicUnlockUtils", "R.array.dynamic_lock_white_list init error.");
            }
        }
    }

    private static Map<String, String[]> parthWhiteList(List<String> whiteList) {
        Map<String, String[]> dynamicWhiteMap = new HashMap();
        for (String item : whiteList) {
            String[] itemArray = item.split("/");
            if (!(itemArray.length != 3 || TextUtils.isEmpty(itemArray[0]) || TextUtils.isEmpty(itemArray[1]) || TextUtils.isEmpty(itemArray[2]))) {
                Log.d("DynamicUnlockUtils", "itemArray[0] is " + itemArray[0] + "itemArray[1] is " + itemArray[1] + "itemArray[2] is " + itemArray[2]);
                dynamicWhiteMap.put(itemArray[0], new String[]{itemArray[1], itemArray[2]});
            }
        }
        return dynamicWhiteMap;
    }

    public static Class<?> loadClass(Context context, String packageName) {
        Log.d("DynamicUnlockUtils", "packageName is " + packageName);
        Log.d("DynamicUnlockUtils", "sDynamicWhiteMap is " + sDynamicWhiteMap.toString());
        String[] dynamicEngine = (String[]) sDynamicWhiteMap.get(packageName);
        if (dynamicEngine == null || dynamicEngine.length != 2) {
            Log.w("DynamicUnlockUtils", "dynamicEngine ==null is null.");
            return null;
        }
        Class<?> cls = null;
        try {
            PackageInfo packageInfo = AppGlobals.getPackageManager().getPackageInfo(packageName, 64, OsUtils.getCurrentUser());
            Log.d("DynamicUnlockUtils", "packageInfo != null is " + (packageInfo != null));
            if (packageInfo != null) {
                Signature[] sinatures = packageInfo.signatures;
                String str = "DynamicUnlockUtils";
                StringBuilder append = new StringBuilder().append("sinatures != null && sinatures.length > 0 is ");
                boolean z = sinatures != null && sinatures.length > 0;
                Log.d(str, append.append(z).toString());
                if (sinatures != null && sinatures.length > 0) {
                    String md5 = getMessageDigest(sinatures[0].toByteArray());
                    Log.d("DynamicUnlockUtils", "md5 is " + md5);
                    Log.d("DynamicUnlockUtils", "dynamicEngine[1] is " + dynamicEngine[1]);
                    if (dynamicEngine[1].equals(md5)) {
                        final Field field = ActivityThread.class.getDeclaredField("mPackages");
                        AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                field.setAccessible(true);
                                return null;
                            }
                        });
                        Object filedValue = field.get(ActivityThread.currentActivityThread());
                        Log.d("DynamicUnlockUtils", "filedValue is " + filedValue);
                        if (filedValue != null) {
                            Log.d("DynamicUnlockUtils", "result is " + ((ArrayMap) filedValue).remove(packageName));
                        }
                        Context packageContext = context.getApplicationContext().createPackageContextAsUser(packageName, 3, new UserHandle(OsUtils.getCurrentUser()));
                        Log.d("DynamicUnlockUtils", "dynamicEngine[0] is " + dynamicEngine[0]);
                        cls = packageContext.getClassLoader().loadClass(dynamicEngine[0]);
                    }
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
        } catch (RemoteException e5) {
            e5.printStackTrace();
        } catch (NameNotFoundException e6) {
            e6.printStackTrace();
        }
        return cls;
    }

    public static Object excuteEngineMethod(Object targetClass, String methodName) {
        return excuteEngineMethod(targetClass, methodName, null, null);
    }

    public static Object excuteEngineMethod(Object targetClass, String methodName, Class[] parameterTypes, Object[] parameterValues) {
        if (targetClass == null || methodName == null) {
            return null;
        }
        try {
            Log.d("DynamicUnlockUtils", "excuteEngineMethod :" + methodName);
            return targetClass.getClass().getMethod(methodName, parameterTypes).invoke(targetClass, parameterValues);
        } catch (NoSuchMethodException e) {
            Log.w("DynamicUnlockUtils", "Error info :", e);
            return null;
        } catch (IllegalAccessException e2) {
            Log.w("DynamicUnlockUtils", "Error info :", e2);
            return null;
        } catch (IllegalArgumentException e3) {
            Log.w("DynamicUnlockUtils", "Error info :", e3);
            return null;
        } catch (InvocationTargetException e4) {
            Log.w("DynamicUnlockUtils", "Error info :", e4);
            return null;
        }
    }

    private static final String getMessageDigest(byte[] byteArray) {
        try {
            char[] charsArray = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(byteArray);
            char[] digestChars = new char[(digestLength * 2)];
            int k = 0;
            for (int m : messageDigest.digest()) {
                int n = k + 1;
                digestChars[k] = charsArray[(m >>> 4) & 15];
                k = n + 1;
                digestChars[n] = charsArray[m & 15];
            }
            return new String(digestChars);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
