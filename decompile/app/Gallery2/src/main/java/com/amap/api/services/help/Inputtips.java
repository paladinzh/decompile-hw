package com.amap.api.services.help;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.d;
import com.amap.api.services.core.h;
import com.amap.api.services.core.i;
import com.amap.api.services.core.l;
import com.amap.api.services.core.p;
import java.util.ArrayList;
import java.util.List;

public final class Inputtips {
    private Context a;
    private InputtipsListener b;
    private Handler c = p.a();

    public interface InputtipsListener {
        void onGetInputtips(List<Tip> list, int i);
    }

    public Inputtips(Context context, InputtipsListener inputtipsListener) {
        this.a = context.getApplicationContext();
        this.b = inputtipsListener;
    }

    public void requestInputtips(final String str, final String str2) throws AMapException {
        if (str == null || str.equals("")) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        l.a(this.a);
        new Thread(this) {
            final /* synthetic */ Inputtips c;

            public void run() {
                h hVar = new h(this.c.a, new i(str, str2));
                Message obtainMessage = p.a().obtainMessage();
                obtainMessage.obj = this.c.b;
                obtainMessage.arg1 = 5;
                try {
                    ArrayList arrayList = (ArrayList) hVar.g();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("result", arrayList);
                    obtainMessage.setData(bundle);
                    obtainMessage.what = 0;
                } catch (Throwable e) {
                    d.a(e, "Inputtips", "requestInputtips");
                    obtainMessage.what = e.getErrorCode();
                } finally {
                    this.c.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }
}
