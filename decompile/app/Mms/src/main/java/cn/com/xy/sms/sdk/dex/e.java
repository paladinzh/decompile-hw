package cn.com.xy.sms.sdk.dex;

/* compiled from: Unknown */
final class e implements Runnable {
    private final /* synthetic */ Throwable a;

    e(Throwable th) {
        this.a = th;
    }

    public final void run() {
        try {
            String str = "";
            String str2 = "";
            StackTraceElement[] stackTrace = this.a.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                str = stackTrace[0].getClassName();
                str2 = stackTrace[0].getMethodName();
            }
            Class classBymap = DexUtil.getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                classBymap.getMethod("saveLogException", new Class[]{String.class, String.class, Throwable.class}).invoke(classBymap, new Object[]{str, str2, this.a});
            }
        } catch (Throwable th) {
        }
    }
}
