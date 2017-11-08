package android.support.v4.content;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;

public class ContextCompat {
    private static final Object sLock = new Object();

    public static int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        if (permission != null) {
            return context.checkPermission(permission, Process.myPid(), Process.myUid());
        }
        throw new IllegalArgumentException("permission is null");
    }
}
