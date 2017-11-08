package com.avast.android.sdk.engine.internal;

import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/* compiled from: Unknown */
public class NativeRegistration {

    /* compiled from: Unknown */
    public enum a {
        REGISTRATION_ERROR_WRONG_INSTANCE,
        REGISTRATION_ERROR_LIBRARY_FILE_DOESNT_EXIST,
        REGISTRATION_ERROR_NOTHING_TO_REGISTER,
        REGISTRATION_ERROR_UNKNOWN,
        REGISTRATION_ERROR_JNI_CLASS_NOT_ACQUIRED,
        REGISTRATION_ERROR_JNI_NO_FREE_HANDLES,
        REGISTRATION_ERROR_JNI_STRING_ARRAYS_MISMATCH,
        REGISTRATION_ERROR_JNI_STRING_ARRAYS_CONVERSION,
        REGISTRATION_ERROR_JNI_LIBRARY_FILE_PATH,
        REGISTRATION_ERROR_JNI_LIBRARY_LOAD,
        REGISTRATION_ERROR_JNI_FUNCTION_NOT_FOUND,
        REGISTRATION_OK
    }

    public static int a(Object obj) throws InvalidParameterException {
        if (obj != null) {
            return unregisterClassJni(obj);
        }
        throw new InvalidParameterException("Parameter can't be null");
    }

    public static synchronized a a(Class<? extends Object> cls, Object obj, String str, String[] strArr) {
        synchronized (NativeRegistration.class) {
            if (cls.equals(obj.getClass())) {
                File file = new File(str);
                if (strArr != null) {
                    if (strArr.length != 0 && file.exists() && file.isDirectory()) {
                        List<Method> linkedList = new LinkedList();
                        Method[] declaredMethods = cls.getDeclaredMethods();
                        for (Method method : declaredMethods) {
                            if (Modifier.isNative(method.getModifiers())) {
                                linkedList.add(method);
                            }
                        }
                        if (linkedList.isEmpty()) {
                            return a.REGISTRATION_ERROR_NOTHING_TO_REGISTER;
                        }
                        LinkedList linkedList2 = new LinkedList();
                        LinkedList linkedList3 = new LinkedList();
                        for (Method method2 : linkedList) {
                            linkedList2.add(method2.getName());
                            linkedList3.add(a(method2));
                        }
                        switch (registerClassJni(str, strArr, obj, (String[]) linkedList2.toArray(new String[linkedList2.size()]), (String[]) linkedList3.toArray(new String[linkedList3.size()]))) {
                            case 0:
                                return a.REGISTRATION_OK;
                            case 1:
                                return a.REGISTRATION_ERROR_JNI_CLASS_NOT_ACQUIRED;
                            case 2:
                                return a.REGISTRATION_ERROR_JNI_LIBRARY_FILE_PATH;
                            case 3:
                                return a.REGISTRATION_ERROR_JNI_LIBRARY_LOAD;
                            case 4:
                                return a.REGISTRATION_ERROR_JNI_FUNCTION_NOT_FOUND;
                            case 5:
                                return a.REGISTRATION_ERROR_JNI_STRING_ARRAYS_MISMATCH;
                            case 6:
                                return a.REGISTRATION_ERROR_JNI_STRING_ARRAYS_CONVERSION;
                            case 7:
                                return a.REGISTRATION_ERROR_JNI_NO_FREE_HANDLES;
                            default:
                                return a.REGISTRATION_ERROR_UNKNOWN;
                        }
                    }
                }
                return a.REGISTRATION_ERROR_LIBRARY_FILE_DOESNT_EXIST;
            }
            return a.REGISTRATION_ERROR_WRONG_INSTANCE;
        }
    }

    private static String a(String str) {
        if (str.equalsIgnoreCase("int")) {
            return "I";
        }
        if (str.equalsIgnoreCase("void")) {
            return "V";
        }
        if (str.equalsIgnoreCase("byte")) {
            return ConstValues.B_VERSION_CHAR;
        }
        if (str.equalsIgnoreCase("short")) {
            return "S";
        }
        if (str.equalsIgnoreCase("double")) {
            return "D";
        }
        if (str.equalsIgnoreCase("long")) {
            return "J";
        }
        if (str.equalsIgnoreCase("float")) {
            return "F";
        }
        if (str.equalsIgnoreCase("boolean")) {
            return "Z";
        }
        if (str.equalsIgnoreCase("char")) {
            return "C";
        }
        return ("L" + str.replace(".", "/")) + SqlMarker.SQL_END;
    }

    private static String a(Method method) {
        String canonicalName;
        String str = "(";
        for (Class cls : method.getParameterTypes()) {
            canonicalName = cls.getCanonicalName();
            if (cls.isArray()) {
                str = str + "[";
                if (canonicalName.endsWith("[]")) {
                    canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
                }
            }
            str = str + a(canonicalName);
        }
        str = str + ")";
        Class returnType = method.getReturnType();
        canonicalName = returnType.getCanonicalName();
        if (returnType.isArray()) {
            str = str + "[";
            if (canonicalName.endsWith("[]")) {
                canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
            }
        }
        return str + a(canonicalName);
    }

    private static native int registerClassJni(String str, String[] strArr, Object obj, String[] strArr2, String[] strArr3);

    private static native int unregisterClassJni(Object obj);
}
