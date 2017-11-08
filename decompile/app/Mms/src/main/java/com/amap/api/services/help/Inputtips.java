package com.amap.api.services.help;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.i;
import com.amap.api.services.core.m;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import java.util.ArrayList;
import java.util.List;

public final class Inputtips {
    private Context a;
    private InputtipsListener b;
    private Handler c = t.a();
    private InputtipsQuery d;

    public interface InputtipsListener {
        void onGetInputtips(List<Tip> list, int i);
    }

    public Inputtips(Context context, InputtipsListener inputtipsListener) {
        this.a = context.getApplicationContext();
        this.b = inputtipsListener;
    }

    public Inputtips(Context context, InputtipsQuery inputtipsQuery) {
        this.a = context.getApplicationContext();
        this.d = inputtipsQuery;
    }

    public InputtipsQuery getQuery() {
        return this.d;
    }

    public void setQuery(InputtipsQuery inputtipsQuery) {
        this.d = inputtipsQuery;
    }

    public void setInputtipsListener(InputtipsListener inputtipsListener) {
        this.b = inputtipsListener;
    }

    public void requestInputtipsAsyn() {
        new Thread(this) {
            final /* synthetic */ Inputtips a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.obj = this.a.b;
                obtainMessage.arg1 = 5;
                try {
                    ArrayList a = this.a.a(this.a.d);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("result", a);
                    obtainMessage.setData(bundle);
                    obtainMessage.what = 1000;
                } catch (Throwable e) {
                    i.a(e, "Inputtips", "requestInputtips");
                    obtainMessage.what = e.getErrorCode();
                } finally {
                    this.a.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    private ArrayList<Tip> a(InputtipsQuery inputtipsQuery) throws AMapException {
        q.a(this.a);
        if (inputtipsQuery.getKeyword() != null && !inputtipsQuery.getKeyword().equals("")) {
            return (ArrayList) new m(this.a, inputtipsQuery).a();
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    public void requestInputtips(String str, String str2) throws AMapException {
        requestInputtips(str, str2, null);
    }

    public void requestInputtips(String str, String str2, String str3) throws AMapException {
        q.a(this.a);
        if (str == null || str.equals("")) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        final InputtipsQuery inputtipsQuery = new InputtipsQuery(str, str2);
        inputtipsQuery.setType(str3);
        new Thread(this) {
            final /* synthetic */ Inputtips b;

            public void run() {
                m mVar = new m(this.b.a, inputtipsQuery);
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.obj = this.b.b;
                obtainMessage.arg1 = 5;
                try {
                    ArrayList arrayList = (ArrayList) mVar.a();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("result", arrayList);
                    obtainMessage.setData(bundle);
                    obtainMessage.what = 1000;
                } catch (Throwable e) {
                    i.a(e, "Inputtips", "requestInputtips");
                    obtainMessage.what = e.getErrorCode();
                } finally {
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }
}
