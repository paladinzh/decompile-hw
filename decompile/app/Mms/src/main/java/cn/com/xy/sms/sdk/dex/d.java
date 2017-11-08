package cn.com.xy.sms.sdk.dex;

/* compiled from: Unknown */
final class d implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ Object[] d;

    d(String str, String str2, String str3, Object[] objArr) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = objArr;
    }

    public final void run() {
        try {
            Class classBymap = DexUtil.getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                classBymap.getMethod("saveLogOut", new Class[]{String.class, String.class, String.class, Object[].class}).invoke(classBymap, new Object[]{this.a, this.b, this.c, DexUtil.handlerParamsToJSONObjectIfNeed(this.d)});
            }
        } catch (Throwable th) {
        }
    }
}
