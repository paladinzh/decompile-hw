package com.google.android.gms.common.server.response;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.server.converter.ConverterWrapper;
import com.google.android.gms.internal.zzlj;
import com.google.android.gms.internal.zzls;
import com.google.android.gms.internal.zzlt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public abstract class FastJsonResponse {

    /* compiled from: Unknown */
    public interface zza<I, O> {
        I convertBack(O o);
    }

    /* compiled from: Unknown */
    public static class Field<I, O> implements SafeParcelable {
        public static final zza CREATOR = new zza();
        private final int mVersionCode;
        protected final int zzaeS;
        protected final boolean zzaeT;
        protected final int zzaeU;
        protected final boolean zzaeV;
        protected final String zzaeW;
        protected final int zzaeX;
        protected final Class<? extends FastJsonResponse> zzaeY;
        protected final String zzaeZ;
        private FieldMappingDictionary zzafa;
        private zza<I, O> zzafb;

        Field(int versionCode, int typeIn, boolean typeInArray, int typeOut, boolean typeOutArray, String outputFieldName, int safeParcelableFieldId, String concreteTypeName, ConverterWrapper wrappedConverter) {
            this.mVersionCode = versionCode;
            this.zzaeS = typeIn;
            this.zzaeT = typeInArray;
            this.zzaeU = typeOut;
            this.zzaeV = typeOutArray;
            this.zzaeW = outputFieldName;
            this.zzaeX = safeParcelableFieldId;
            if (concreteTypeName != null) {
                this.zzaeY = SafeParcelResponse.class;
                this.zzaeZ = concreteTypeName;
            } else {
                this.zzaeY = null;
                this.zzaeZ = null;
            }
            if (wrappedConverter != null) {
                this.zzafb = wrappedConverter.zzoZ();
            } else {
                this.zzafb = null;
            }
        }

        public I convertBack(O output) {
            return this.zzafb.convertBack(output);
        }

        public int describeContents() {
            zza zza = CREATOR;
            return 0;
        }

        public int getVersionCode() {
            return this.mVersionCode;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Field\n");
            stringBuilder.append("            versionCode=").append(this.mVersionCode).append('\n');
            stringBuilder.append("                 typeIn=").append(this.zzaeS).append('\n');
            stringBuilder.append("            typeInArray=").append(this.zzaeT).append('\n');
            stringBuilder.append("                typeOut=").append(this.zzaeU).append('\n');
            stringBuilder.append("           typeOutArray=").append(this.zzaeV).append('\n');
            stringBuilder.append("        outputFieldName=").append(this.zzaeW).append('\n');
            stringBuilder.append("      safeParcelFieldId=").append(this.zzaeX).append('\n');
            stringBuilder.append("       concreteTypeName=").append(zzpm()).append('\n');
            if (zzpl() != null) {
                stringBuilder.append("     concreteType.class=").append(zzpl().getCanonicalName()).append('\n');
            }
            stringBuilder.append("          converterName=").append(this.zzafb != null ? this.zzafb.getClass().getCanonicalName() : "null").append('\n');
            return stringBuilder.toString();
        }

        public void writeToParcel(Parcel out, int flags) {
            zza zza = CREATOR;
            zza.zza(this, out, flags);
        }

        public void zza(FieldMappingDictionary fieldMappingDictionary) {
            this.zzafa = fieldMappingDictionary;
        }

        public int zzpb() {
            return this.zzaeS;
        }

        public int zzpc() {
            return this.zzaeU;
        }

        public boolean zzph() {
            return this.zzaeT;
        }

        public boolean zzpi() {
            return this.zzaeV;
        }

        public String zzpj() {
            return this.zzaeW;
        }

        public int zzpk() {
            return this.zzaeX;
        }

        public Class<? extends FastJsonResponse> zzpl() {
            return this.zzaeY;
        }

        String zzpm() {
            return this.zzaeZ != null ? this.zzaeZ : null;
        }

        public boolean zzpn() {
            return this.zzafb != null;
        }

        ConverterWrapper zzpo() {
            return this.zzafb != null ? ConverterWrapper.zza(this.zzafb) : null;
        }

        public Map<String, Field<?, ?>> zzpp() {
            zzx.zzv(this.zzaeZ);
            zzx.zzv(this.zzafa);
            return this.zzafa.zzcx(this.zzaeZ);
        }
    }

    private void zza(StringBuilder stringBuilder, Field field, Object obj) {
        if (field.zzpb() == 11) {
            stringBuilder.append(((FastJsonResponse) field.zzpl().cast(obj)).toString());
        } else if (field.zzpb() != 7) {
            stringBuilder.append(obj);
        } else {
            stringBuilder.append("\"");
            stringBuilder.append(zzls.zzcA((String) obj));
            stringBuilder.append("\"");
        }
    }

    private void zza(StringBuilder stringBuilder, Field field, ArrayList<Object> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            Object obj = arrayList.get(i);
            if (obj != null) {
                zza(stringBuilder, field, obj);
            }
        }
        stringBuilder.append("]");
    }

    public String toString() {
        Map zzpd = zzpd();
        StringBuilder stringBuilder = new StringBuilder(100);
        for (String str : zzpd.keySet()) {
            Field field = (Field) zzpd.get(str);
            if (zza(field)) {
                Object zza = zza(field, zzb(field));
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(",");
                } else {
                    stringBuilder.append("{");
                }
                stringBuilder.append("\"").append(str).append("\":");
                if (zza != null) {
                    switch (field.zzpc()) {
                        case 8:
                            stringBuilder.append("\"").append(zzlj.zzi((byte[]) zza)).append("\"");
                            break;
                        case 9:
                            stringBuilder.append("\"").append(zzlj.zzj((byte[]) zza)).append("\"");
                            break;
                        case 10:
                            zzlt.zza(stringBuilder, (HashMap) zza);
                            break;
                        default:
                            if (!field.zzph()) {
                                zza(stringBuilder, field, zza);
                                break;
                            }
                            zza(stringBuilder, field, (ArrayList) zza);
                            break;
                    }
                }
                stringBuilder.append("null");
            }
        }
        if (stringBuilder.length() <= 0) {
            stringBuilder.append("{}");
        } else {
            stringBuilder.append("}");
        }
        return stringBuilder.toString();
    }

    protected <O, I> I zza(Field<I, O> field, Object obj) {
        return field.zzafb == null ? obj : field.convertBack(obj);
    }

    protected boolean zza(Field field) {
        return field.zzpc() != 11 ? zzcu(field.zzpj()) : !field.zzpi() ? zzcv(field.zzpj()) : zzcw(field.zzpj());
    }

    protected Object zzb(Field field) {
        String zzpj = field.zzpj();
        if (field.zzpl() == null) {
            return zzct(field.zzpj());
        }
        zzx.zza(zzct(field.zzpj()) == null, "Concrete field shouldn't be value object: %s", field.zzpj());
        Map zzpe = !field.zzpi() ? zzpe() : zzpf();
        if (zzpe != null) {
            return zzpe.get(zzpj);
        }
        try {
            return getClass().getMethod("get" + Character.toUpperCase(zzpj.charAt(0)) + zzpj.substring(1), new Class[0]).invoke(this, new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Object zzct(String str);

    protected abstract boolean zzcu(String str);

    protected boolean zzcv(String str) {
        throw new UnsupportedOperationException("Concrete types not supported");
    }

    protected boolean zzcw(String str) {
        throw new UnsupportedOperationException("Concrete type arrays not supported");
    }

    public abstract Map<String, Field<?, ?>> zzpd();

    public HashMap<String, Object> zzpe() {
        return null;
    }

    public HashMap<String, Object> zzpf() {
        return null;
    }
}
