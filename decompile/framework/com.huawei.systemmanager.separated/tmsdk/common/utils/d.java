package tmsdk.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/* compiled from: Unknown */
public final class d {
    private static boolean KX = false;
    private static a KY = new g();

    public static void P(boolean z) {
        KX = z;
        KY = !KX ? new g() : new e();
    }

    public static void a(String str, Object obj, Throwable th) {
        KY.b(str, e(obj), th);
    }

    public static void b(String str, Object obj, Throwable th) {
        KY.a(str, e(obj), th);
    }

    public static void c(String str, Object obj) {
        KY.r(str, e(obj));
    }

    public static void d(String str, Object obj) {
        KY.s(str, e(obj));
    }

    private static String e(Object obj) {
        return obj != null ? !(obj instanceof String) ? !(obj instanceof Throwable) ? obj.toString() : getStackTraceString((Throwable) obj) : (String) obj : null;
    }

    public static void e(String str, Object obj) {
        KY.d(str, e(obj));
    }

    public static void f(String str, Object obj) {
        KY.x(str, e(obj));
    }

    public static void g(String str, Object obj) {
        KY.w(str, e(obj));
    }

    public static String getStackTraceString(Throwable th) {
        if (th == null) {
            return "(Null stack trace)";
        }
        Writer stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        printWriter.flush();
        String stringWriter2 = stringWriter.toString();
        printWriter.close();
        return stringWriter2;
    }

    public static void h(String str, Object obj) {
        KY.h(str, e(obj));
    }

    public static boolean isEnable() {
        return KX;
    }
}
