package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.help.Tip;
import java.util.ArrayList;
import org.json.JSONObject;

/* compiled from: InputtipsHandler */
public class h extends r<i, ArrayList<Tip>> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public h(Context context, i iVar) {
        super(context, iVar);
    }

    protected ArrayList<Tip> a(String str) throws AMapException {
        try {
            return j.o(new JSONObject(str));
        } catch (Throwable e) {
            d.a(e, "InputtipsHandler", "paseJSON");
            return null;
        }
    }

    public String b() {
        return c.a() + "/assistant/inputtips?";
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&keywords=").append(c(((i) this.a).a));
        String str = ((i) this.a).b;
        if (!j.h(str)) {
            stringBuffer.append("&city=").append(c(str));
        }
        stringBuffer.append("&key=").append(w.f(this.d));
        stringBuffer.append("&language=").append(c.b());
        return stringBuffer.toString();
    }
}
