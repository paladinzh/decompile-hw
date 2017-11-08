package cn.com.xy.sms.sdk.action;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
final class c {
    c() {
    }

    public static String a(String str) {
        Object futureTask = new FutureTask(new d(str));
        new Thread(futureTask).start();
        return (String) futureTask.get(5000, TimeUnit.MILLISECONDS);
    }
}
