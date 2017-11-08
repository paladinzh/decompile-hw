package com.amap.api.mapcore;

import android.os.Handler;
import android.os.Message;

/* compiled from: UiSettingsDelegateImp */
class bq extends Handler {
    final /* synthetic */ bp a;

    bq(bp bpVar) {
        this.a = bpVar;
    }

    public void handleMessage(Message message) {
        if (message != null) {
            switch (message.what) {
                case 0:
                    this.a.b.a(this.a.h);
                    break;
                case 1:
                    this.a.b.e(this.a.j);
                    break;
                case 2:
                    this.a.b.d(this.a.i);
                    break;
                case 3:
                    this.a.b.c(this.a.f);
                    break;
                case 4:
                    this.a.b.b(this.a.m);
                    break;
            }
        }
    }
}
