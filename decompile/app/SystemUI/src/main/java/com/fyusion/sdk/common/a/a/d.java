package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Objects;
import org.json.JSONObject;

/* compiled from: Unknown */
class d implements Cloneable, Comparable<d> {
    public String e;
    public long f;
    public String g;
    public long h;
    public String i;
    public String j;

    d() {
    }

    public d(String str) {
        this.e = str;
        this.f = f.b();
    }

    public d(String str, String str2) {
        this.e = str;
        this.g = str2;
        this.f = f.b();
    }

    public static d a(d dVar, String str) {
        CloneNotSupportedException cloneNotSupportedException;
        d dVar2 = null;
        try {
            d dVar3 = (d) dVar.clone();
            try {
                dVar3.e = str;
                return dVar3;
            } catch (CloneNotSupportedException e) {
                CloneNotSupportedException cloneNotSupportedException2 = e;
                dVar2 = dVar3;
                cloneNotSupportedException = cloneNotSupportedException2;
            }
        } catch (CloneNotSupportedException e2) {
            cloneNotSupportedException = e2;
            cloneNotSupportedException.printStackTrace();
            return dVar2;
        }
    }

    static d a(JSONObject jSONObject, d dVar) {
        try {
            if (!jSONObject.isNull("n")) {
                dVar.e = jSONObject.getString("n");
            }
            dVar.f = jSONObject.optLong("ts");
            if (!jSONObject.isNull("uid")) {
                dVar.g = jSONObject.getString("uid");
            }
            dVar.h = jSONObject.optLong("dr", 0);
            if (!jSONObject.isNull("st")) {
                dVar.i = jSONObject.getString("st");
            }
            if (!jSONObject.isNull("m")) {
                dVar.j = jSONObject.getString("m");
            }
            d dVar2 = dVar;
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting JSON to an Event", e);
            }
            dVar2 = null;
        }
        if (!(dVar2 == null || dVar2.e == null)) {
            if (dVar2.e.length() > 0) {
                return dVar2;
            }
        }
        return null;
    }

    static d b(JSONObject jSONObject) {
        try {
            String string = jSONObject.isNull("n") ? null : jSONObject.getString("n");
            if (string != null) {
                Object obj = -1;
                switch (string.hashCode()) {
                    case -2014348979:
                        if (string.equals("RECORDING_END")) {
                            obj = 10;
                            break;
                        }
                        break;
                    case -1667346370:
                        if (string.equals("CAMERA_CLOSE")) {
                            obj = 7;
                            break;
                        }
                        break;
                    case -1515159428:
                        if (string.equals("ST_LOAD_ST")) {
                            obj = 14;
                            break;
                        }
                        break;
                    case -1350744454:
                        if (string.equals("ST_CLOSE")) {
                            obj = 19;
                            break;
                        }
                        break;
                    case -514814511:
                        if (string.equals("RECORDING")) {
                            obj = 8;
                            break;
                        }
                        break;
                    case -118222296:
                        if (string.equals("VIEW_START")) {
                            obj = 2;
                            break;
                        }
                        break;
                    case -53424348:
                        if (string.equals("CAMERA_OPEN")) {
                            obj = 6;
                            break;
                        }
                        break;
                    case 2657:
                        if (string.equals("ST")) {
                            obj = 12;
                            break;
                        }
                        break;
                    case 3675:
                        if (string.equals("sn")) {
                            obj = 13;
                            break;
                        }
                        break;
                    case 2575037:
                        if (string.equals("TILT")) {
                            obj = null;
                            break;
                        }
                        break;
                    case 2634405:
                        if (string.equals("VIEW")) {
                            obj = 1;
                            break;
                        }
                        break;
                    case 78862271:
                        if (string.equals("SHARE")) {
                            obj = 20;
                            break;
                        }
                        break;
                    case 172755160:
                        if (string.equals("ST_SF_OPEN")) {
                            obj = 17;
                            break;
                        }
                        break;
                    case 243170262:
                        if (string.equals("ST_STR_OPEN")) {
                            obj = 18;
                            break;
                        }
                        break;
                    case 562020088:
                        if (string.equals("ST_STR_APS")) {
                            obj = 16;
                            break;
                        }
                        break;
                    case 1120794199:
                        if (string.equals("VIEW_LOOP_CLOSE")) {
                            obj = 4;
                            break;
                        }
                        break;
                    case 1253989460:
                        if (string.equals("RECORDING_START")) {
                            obj = 9;
                            break;
                        }
                        break;
                    case 1529579958:
                        if (string.equals("ST_SF_APS")) {
                            obj = 15;
                            break;
                        }
                        break;
                    case 1691835506:
                        if (string.equals("PROCESSOR")) {
                            obj = 11;
                            break;
                        }
                        break;
                    case 1979749409:
                        if (string.equals("VIEW_END")) {
                            obj = 3;
                            break;
                        }
                        break;
                    case 1980544805:
                        if (string.equals("CAMERA")) {
                            obj = 5;
                            break;
                        }
                        break;
                }
                switch (obj) {
                    case null:
                        return k.a(jSONObject);
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        return l.a(jSONObject);
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        return a.a(jSONObject);
                    case 11:
                        return h.a(jSONObject);
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                        return j.a(jSONObject);
                    case 20:
                        return i.a(jSONObject);
                    default:
                        break;
                }
            }
            return a(jSONObject, new d());
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting JSON to an Event", e);
            }
            return null;
        }
    }

    public int a(@NonNull d dVar) {
        int compare = Long.compare(this.f, dVar.f);
        if (compare != 0) {
            return compare;
        }
        if (!(this.g == null || dVar.g == null)) {
            compare = this.g.compareTo(dVar.g);
            if (compare != 0) {
                return compare;
            }
        }
        if (!(this.e == null || dVar.e == null)) {
            compare = this.e.compareTo(dVar.e);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    public String a() {
        return f.a(this.e, this.g);
    }

    void a(@NonNull StringBuilder stringBuilder) {
    }

    void a(@NonNull StringBuilder stringBuilder, @NonNull String str, long j) {
        b(stringBuilder.append(','), str, j);
    }

    void a(@NonNull StringBuilder stringBuilder, @NonNull String str, @NonNull String str2) {
        b(stringBuilder.append(','), str, str2);
    }

    void b(@NonNull StringBuilder stringBuilder) {
        stringBuilder.append('{');
        b(stringBuilder, "n", this.e);
        a(stringBuilder, "ts", this.f);
        if (this.g != null) {
            a(stringBuilder, "uid", this.g);
        }
        if (this.i != null) {
            a(stringBuilder, "st", this.i);
        }
        if (this.j != null) {
            a(stringBuilder, "m", this.j);
        }
        if ((this.h <= 0 ? 1 : null) == null) {
            a(stringBuilder, "dr", this.h);
        }
        a(stringBuilder);
        stringBuilder.append('}');
    }

    void b(@NonNull StringBuilder stringBuilder, @NonNull String str, long j) {
        stringBuilder.append('\"').append(str).append("\":").append(j);
    }

    void b(@NonNull StringBuilder stringBuilder, @NonNull String str, @NonNull String str2) {
        stringBuilder.append('\"').append(str).append("\":\"").append(str2).append('\"');
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public /* synthetic */ int compareTo(@NonNull Object obj) {
        return a((d) obj);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        d dVar = (d) obj;
        if (this.f == dVar.f && Objects.equals(this.e, dVar.e)) {
            if (!Objects.equals(this.g, dVar.g)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.e, Long.valueOf(this.f), this.g});
    }
}
