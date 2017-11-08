package com.google.android.gms.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.internal.fb.a;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
public class fh extends fb implements SafeParcelable {
    public static final fi CREATOR = new fi();
    private final fe CC;
    private final Parcel CJ;
    private final int CK = 2;
    private int CL;
    private int CM;
    private final String mClassName;
    private final int wj;

    fh(int i, Parcel parcel, fe feVar) {
        this.wj = i;
        this.CJ = (Parcel) er.f(parcel);
        this.CC = feVar;
        if (this.CC != null) {
            this.mClassName = this.CC.eD();
        } else {
            this.mClassName = null;
        }
        this.CL = 2;
    }

    private void a(StringBuilder stringBuilder, int i, Object obj) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                stringBuilder.append(obj);
                return;
            case 7:
                stringBuilder.append("\"").append(fp.ap(obj.toString())).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(fk.d((byte[]) obj)).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(fk.e((byte[]) obj));
                stringBuilder.append("\"");
                return;
            case 10:
                fq.a(stringBuilder, (HashMap) obj);
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown type = " + i);
        }
    }

    private void a(StringBuilder stringBuilder, a<?, ?> aVar, Parcel parcel, int i) {
        switch (aVar.em()) {
            case 0:
                b(stringBuilder, (a) aVar, a(aVar, Integer.valueOf(com.google.android.gms.common.internal.safeparcel.a.g(parcel, i))));
                return;
            case 1:
                b(stringBuilder, (a) aVar, a(aVar, com.google.android.gms.common.internal.safeparcel.a.i(parcel, i)));
                return;
            case 2:
                b(stringBuilder, (a) aVar, a(aVar, Long.valueOf(com.google.android.gms.common.internal.safeparcel.a.h(parcel, i))));
                return;
            case 3:
                b(stringBuilder, (a) aVar, a(aVar, Float.valueOf(com.google.android.gms.common.internal.safeparcel.a.j(parcel, i))));
                return;
            case 4:
                b(stringBuilder, (a) aVar, a(aVar, Double.valueOf(com.google.android.gms.common.internal.safeparcel.a.k(parcel, i))));
                return;
            case 5:
                b(stringBuilder, (a) aVar, a(aVar, com.google.android.gms.common.internal.safeparcel.a.l(parcel, i)));
                return;
            case 6:
                b(stringBuilder, (a) aVar, a(aVar, Boolean.valueOf(com.google.android.gms.common.internal.safeparcel.a.c(parcel, i))));
                return;
            case 7:
                b(stringBuilder, (a) aVar, a(aVar, com.google.android.gms.common.internal.safeparcel.a.m(parcel, i)));
                return;
            case 8:
            case 9:
                b(stringBuilder, (a) aVar, a(aVar, com.google.android.gms.common.internal.safeparcel.a.p(parcel, i)));
                return;
            case 10:
                b(stringBuilder, (a) aVar, a(aVar, c(com.google.android.gms.common.internal.safeparcel.a.o(parcel, i))));
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown field out type = " + aVar.em());
        }
    }

    private void a(StringBuilder stringBuilder, String str, a<?, ?> aVar, Parcel parcel, int i) {
        stringBuilder.append("\"").append(str).append("\":");
        if (aVar.ex()) {
            a(stringBuilder, aVar, parcel, i);
        } else {
            b(stringBuilder, aVar, parcel, i);
        }
    }

    private void a(StringBuilder stringBuilder, HashMap<String, a<?, ?>> hashMap, Parcel parcel) {
        HashMap c = c((HashMap) hashMap);
        stringBuilder.append('{');
        int o = com.google.android.gms.common.internal.safeparcel.a.o(parcel);
        Object obj = null;
        while (parcel.dataPosition() < o) {
            int n = com.google.android.gms.common.internal.safeparcel.a.n(parcel);
            Entry entry = (Entry) c.get(Integer.valueOf(com.google.android.gms.common.internal.safeparcel.a.S(n)));
            if (entry != null) {
                if (obj != null) {
                    stringBuilder.append(",");
                }
                a(stringBuilder, (String) entry.getKey(), (a) entry.getValue(), parcel, n);
                obj = 1;
            }
        }
        if (parcel.dataPosition() == o) {
            stringBuilder.append('}');
            return;
        }
        throw new com.google.android.gms.common.internal.safeparcel.a.a("Overread allowed size end=" + o, parcel);
    }

    private void b(StringBuilder stringBuilder, a<?, ?> aVar, Parcel parcel, int i) {
        if (aVar.es()) {
            stringBuilder.append("[");
            switch (aVar.em()) {
                case 0:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.r(parcel, i));
                    break;
                case 1:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.t(parcel, i));
                    break;
                case 2:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.s(parcel, i));
                    break;
                case 3:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.u(parcel, i));
                    break;
                case 4:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.v(parcel, i));
                    break;
                case 5:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.w(parcel, i));
                    break;
                case 6:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.q(parcel, i));
                    break;
                case 7:
                    fj.a(stringBuilder, com.google.android.gms.common.internal.safeparcel.a.x(parcel, i));
                    break;
                case 8:
                case 9:
                case 10:
                    throw new UnsupportedOperationException("List of type BASE64, BASE64_URL_SAFE, or STRING_MAP is not supported");
                case 11:
                    Parcel[] A = com.google.android.gms.common.internal.safeparcel.a.A(parcel, i);
                    int length = A.length;
                    for (int i2 = 0; i2 < length; i2++) {
                        if (i2 > 0) {
                            stringBuilder.append(",");
                        }
                        A[i2].setDataPosition(0);
                        a(stringBuilder, aVar.ez(), A[i2]);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown field type out.");
            }
            stringBuilder.append("]");
            return;
        }
        switch (aVar.em()) {
            case 0:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.g(parcel, i));
                return;
            case 1:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.i(parcel, i));
                return;
            case 2:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.h(parcel, i));
                return;
            case 3:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.j(parcel, i));
                return;
            case 4:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.k(parcel, i));
                return;
            case 5:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.l(parcel, i));
                return;
            case 6:
                stringBuilder.append(com.google.android.gms.common.internal.safeparcel.a.c(parcel, i));
                return;
            case 7:
                stringBuilder.append("\"").append(fp.ap(com.google.android.gms.common.internal.safeparcel.a.m(parcel, i))).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(fk.d(com.google.android.gms.common.internal.safeparcel.a.p(parcel, i))).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(fk.e(com.google.android.gms.common.internal.safeparcel.a.p(parcel, i)));
                stringBuilder.append("\"");
                return;
            case 10:
                Bundle o = com.google.android.gms.common.internal.safeparcel.a.o(parcel, i);
                Set<String> keySet = o.keySet();
                keySet.size();
                stringBuilder.append("{");
                int i3 = 1;
                for (String str : keySet) {
                    if (i3 == 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append("\"").append(str).append("\"");
                    stringBuilder.append(":");
                    stringBuilder.append("\"").append(fp.ap(o.getString(str))).append("\"");
                    i3 = 0;
                }
                stringBuilder.append("}");
                return;
            case 11:
                Parcel z = com.google.android.gms.common.internal.safeparcel.a.z(parcel, i);
                z.setDataPosition(0);
                a(stringBuilder, aVar.ez(), z);
                return;
            default:
                throw new IllegalStateException("Unknown field type out");
        }
    }

    private void b(StringBuilder stringBuilder, a<?, ?> aVar, Object obj) {
        if (aVar.er()) {
            b(stringBuilder, (a) aVar, (ArrayList) obj);
        } else {
            a(stringBuilder, aVar.el(), obj);
        }
    }

    private void b(StringBuilder stringBuilder, a<?, ?> aVar, ArrayList<?> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }
            a(stringBuilder, aVar.el(), arrayList.get(i));
        }
        stringBuilder.append("]");
    }

    public static HashMap<String, String> c(Bundle bundle) {
        HashMap<String, String> hashMap = new HashMap();
        for (String str : bundle.keySet()) {
            hashMap.put(str, bundle.getString(str));
        }
        return hashMap;
    }

    private static HashMap<Integer, Entry<String, a<?, ?>>> c(HashMap<String, a<?, ?>> hashMap) {
        HashMap<Integer, Entry<String, a<?, ?>>> hashMap2 = new HashMap();
        for (Entry entry : hashMap.entrySet()) {
            hashMap2.put(Integer.valueOf(((a) entry.getValue()).eu()), entry);
        }
        return hashMap2;
    }

    protected Object ak(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    protected boolean al(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    public int describeContents() {
        fi fiVar = CREATOR;
        return 0;
    }

    public Parcel eF() {
        switch (this.CL) {
            case 0:
                this.CM = b.p(this.CJ);
                b.D(this.CJ, this.CM);
                break;
            case 1:
                b.D(this.CJ, this.CM);
                break;
            default:
                return this.CJ;
        }
        this.CL = 2;
        return this.CJ;
    }

    fe eG() {
        switch (this.CK) {
            case 0:
                return null;
            case 1:
                return this.CC;
            case 2:
                return this.CC;
            default:
                throw new IllegalStateException("Invalid creation type: " + this.CK);
        }
    }

    public HashMap<String, a<?, ?>> en() {
        return this.CC != null ? this.CC.ao(this.mClassName) : null;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public String toString() {
        er.b(this.CC, (Object) "Cannot convert to JSON on client side.");
        Parcel eF = eF();
        eF.setDataPosition(0);
        StringBuilder stringBuilder = new StringBuilder(100);
        a(stringBuilder, this.CC.ao(this.mClassName), eF);
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        fi fiVar = CREATOR;
        fi.a(this, out, flags);
    }
}
