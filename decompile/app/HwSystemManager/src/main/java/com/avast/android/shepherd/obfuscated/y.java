package com.avast.android.shepherd.obfuscated;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class y {

    /* compiled from: Unknown */
    public static final class a {
        private int a;
        private List<String> b;
        private boolean c;

        public a a(int i) {
            this.a = i;
            return this;
        }

        public a a(Set<String> set) {
            if (set == null) {
                return this;
            }
            if (this.b == null) {
                this.b = new ArrayList();
            }
            this.b.addAll(set);
            return this;
        }

        public a a(boolean z) {
            this.c = z;
            return this;
        }

        public String a() {
            JSONObject jSONObject = new JSONObject();
            try {
                if (this.a > 0) {
                    jSONObject.put("googlePlayServices", this.a);
                }
            } catch (JSONException e) {
            }
            if (!(this.b == null || this.b.isEmpty())) {
                JSONArray jSONArray = new JSONArray();
                for (String str : this.b) {
                    if (!TextUtils.isEmpty(str)) {
                        jSONArray.put(str);
                    }
                }
                try {
                    jSONObject.put("appMarket", jSONArray);
                } catch (JSONException e2) {
                }
            }
            try {
                jSONObject.put("unknownSources", this.c);
            } catch (JSONException e3) {
            }
            return jSONObject.length() <= 0 ? null : jSONObject.toString();
        }
    }

    public static a a() {
        return new a();
    }
}
