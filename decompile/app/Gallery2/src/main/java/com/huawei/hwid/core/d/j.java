package com.huawei.hwid.core.d;

import android.content.Context;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import java.lang.reflect.Field;

public class j {
    public static int a(Context context, String str, String str2) {
        try {
            int identifier = context.getResources().getIdentifier(str2, str, context.getPackageName());
            if (identifier == 0) {
                Field field = Class.forName(context.getPackageName() + ".R$" + str).getField(str2);
                identifier = Integer.parseInt(field.get(field.getName()).toString());
                if (identifier == 0) {
                    e.b("ResourceLoader", "Error-resourceType=" + str + "--resourceName=" + str2 + "--resourceId =" + identifier);
                }
            }
            return identifier;
        } catch (Throwable e) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e);
            return 0;
        } catch (Throwable e2) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e2);
            return 0;
        } catch (Throwable e22) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e22);
            return 0;
        } catch (Throwable e222) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e222);
            return 0;
        } catch (Throwable e2222) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e2222);
            return 0;
        } catch (Throwable e22222) {
            e.d("ResourceLoader", "!!!! ResourceLoader: reflect resource error-resourceType=" + str + "--resourceName=" + str2, e22222);
            return 0;
        }
    }

    public static int a(Context context, String str) {
        return a(context, "string", str);
    }

    public static int b(Context context, String str) {
        return a(context, "xml", str);
    }

    public static int d(Context context, String str) {
        return a(context, XmlUtils.START_TAG, str);
    }

    public static int e(Context context, String str) {
        return a(context, "id", str);
    }

    public static int f(Context context, String str) {
        return a(context, "menu", str);
    }
}
