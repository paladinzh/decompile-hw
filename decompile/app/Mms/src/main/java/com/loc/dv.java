package com.loc;

import android.os.Handler;
import android.os.Message;

/* compiled from: Unknown */
final class dv extends Handler {
    private /* synthetic */ du a;

    dv(du duVar) {
        this.a = duVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void handleMessage(Message message) {
        try {
            switch (message.what) {
                case 0:
                    break;
                case 1:
                    if (this.a.a.A != null) {
                        this.a.a.A.a((String) message.obj);
                        break;
                    }
                    break;
            }
        } catch (Throwable th) {
        }
    }
}
