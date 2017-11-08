package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import java.util.ArrayList;
import org.json.JSONObject;

/* compiled from: InputtipsHandler */
public class m extends b<InputtipsQuery, ArrayList<Tip>> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public m(Context context, InputtipsQuery inputtipsQuery) {
        super(context, inputtipsQuery);
    }

    protected ArrayList<Tip> d(String str) throws AMapException {
        try {
            return n.m(new JSONObject(str));
        } catch (Throwable e) {
            i.a(e, "InputtipsHandler", "paseJSON");
            return null;
        }
    }

    public String g() {
        return h.a() + "/assistant/inputtips?";
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&keywords=").append(b(((InputtipsQuery) this.a).getKeyword()));
        String city = ((InputtipsQuery) this.a).getCity();
        if (!n.i(city)) {
            stringBuffer.append("&city=").append(b(city));
        }
        city = ((InputtipsQuery) this.a).getType();
        if (!n.i(city)) {
            stringBuffer.append("&type=").append(b(city));
        }
        if (((InputtipsQuery) this.a).getCityLimit()) {
            stringBuffer.append("&citylimit=true");
        } else {
            stringBuffer.append("&citylimit=false");
        }
        stringBuffer.append("&key=").append(aj.f(this.d));
        stringBuffer.append("&language=").append(h.c());
        return stringBuffer.toString();
    }
}
