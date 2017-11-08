package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public final class zzsk extends zzso<zzsk> {
    public String[] zzbtT;
    public int[] zzbtU;
    public byte[][] zzbtV;

    public zzsk() {
        zzIW();
    }

    public static zzsk zzB(byte[] bArr) throws zzst {
        return (zzsk) zzsu.mergeFrom(new zzsk(), bArr);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzsk)) {
            return false;
        }
        zzsk zzsk = (zzsk) o;
        if (!zzss.equals(this.zzbtT, zzsk.zzbtT) || !zzss.equals(this.zzbtU, zzsk.zzbtU) || !zzss.zza(this.zzbtV, zzsk.zzbtV)) {
            return false;
        }
        if (this.zzbuj != null && !this.zzbuj.isEmpty()) {
            return this.zzbuj.equals(zzsk.zzbuj);
        }
        if (zzsk.zzbuj == null || zzsk.zzbuj.isEmpty()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((getClass().getName().hashCode() + 527) * 31) + zzss.hashCode(this.zzbtT)) * 31) + zzss.hashCode(this.zzbtU)) * 31) + zzss.zza(this.zzbtV)) * 31;
        if (!(this.zzbuj == null || this.zzbuj.isEmpty())) {
            i = this.zzbuj.hashCode();
        }
        return i + hashCode;
    }

    public /* synthetic */ zzsu mergeFrom(zzsm zzsm) throws IOException {
        return zzO(zzsm);
    }

    public void writeTo(zzsn output) throws IOException {
        int i = 0;
        if (this.zzbtT != null && this.zzbtT.length > 0) {
            for (String str : this.zzbtT) {
                if (str != null) {
                    output.zzn(1, str);
                }
            }
        }
        if (this.zzbtU != null && this.zzbtU.length > 0) {
            for (int zzA : this.zzbtU) {
                output.zzA(2, zzA);
            }
        }
        if (this.zzbtV != null && this.zzbtV.length > 0) {
            while (i < this.zzbtV.length) {
                byte[] bArr = this.zzbtV[i];
                if (bArr != null) {
                    output.zza(3, bArr);
                }
                i++;
            }
        }
        super.writeTo(output);
    }

    public zzsk zzIW() {
        this.zzbtT = zzsx.zzbuB;
        this.zzbtU = zzsx.zzbuw;
        this.zzbtV = zzsx.zzbuC;
        this.zzbuj = null;
        this.zzbuu = -1;
        return this;
    }

    public zzsk zzO(zzsm zzsm) throws IOException {
        while (true) {
            int zzIX = zzsm.zzIX();
            int zzc;
            Object obj;
            switch (zzIX) {
                case 0:
                    return this;
                case 10:
                    zzc = zzsx.zzc(zzsm, 10);
                    zzIX = this.zzbtT != null ? this.zzbtT.length : 0;
                    obj = new String[(zzc + zzIX)];
                    if (zzIX != 0) {
                        System.arraycopy(this.zzbtT, 0, obj, 0, zzIX);
                    }
                    while (zzIX < obj.length - 1) {
                        obj[zzIX] = zzsm.readString();
                        zzsm.zzIX();
                        zzIX++;
                    }
                    obj[zzIX] = zzsm.readString();
                    this.zzbtT = obj;
                    break;
                case 16:
                    zzc = zzsx.zzc(zzsm, 16);
                    zzIX = this.zzbtU != null ? this.zzbtU.length : 0;
                    obj = new int[(zzc + zzIX)];
                    if (zzIX != 0) {
                        System.arraycopy(this.zzbtU, 0, obj, 0, zzIX);
                    }
                    while (zzIX < obj.length - 1) {
                        obj[zzIX] = zzsm.zzJb();
                        zzsm.zzIX();
                        zzIX++;
                    }
                    obj[zzIX] = zzsm.zzJb();
                    this.zzbtU = obj;
                    break;
                case 18:
                    int zzmq = zzsm.zzmq(zzsm.zzJf());
                    zzc = zzsm.getPosition();
                    zzIX = 0;
                    while (zzsm.zzJk() > 0) {
                        zzsm.zzJb();
                        zzIX++;
                    }
                    zzsm.zzms(zzc);
                    zzc = this.zzbtU != null ? this.zzbtU.length : 0;
                    Object obj2 = new int[(zzIX + zzc)];
                    if (zzc != 0) {
                        System.arraycopy(this.zzbtU, 0, obj2, 0, zzc);
                    }
                    while (zzc < obj2.length) {
                        obj2[zzc] = zzsm.zzJb();
                        zzc++;
                    }
                    this.zzbtU = obj2;
                    zzsm.zzmr(zzmq);
                    break;
                case 26:
                    zzc = zzsx.zzc(zzsm, 26);
                    zzIX = this.zzbtV != null ? this.zzbtV.length : 0;
                    obj = new byte[(zzc + zzIX)][];
                    if (zzIX != 0) {
                        System.arraycopy(this.zzbtV, 0, obj, 0, zzIX);
                    }
                    while (zzIX < obj.length - 1) {
                        obj[zzIX] = zzsm.readBytes();
                        zzsm.zzIX();
                        zzIX++;
                    }
                    obj[zzIX] = zzsm.readBytes();
                    this.zzbtV = obj;
                    break;
                default:
                    if (zza(zzsm, zzIX)) {
                        break;
                    }
                    return this;
            }
        }
    }

    protected int zzz() {
        int i;
        int i2;
        int i3;
        int i4 = 0;
        int zzz = super.zzz();
        if (this.zzbtT != null && this.zzbtT.length > 0) {
            i = 0;
            i2 = 0;
            for (String str : this.zzbtT) {
                if (str != null) {
                    i2++;
                    i += zzsn.zzgO(str);
                }
            }
            i3 = (zzz + i) + (i2 * 1);
        } else {
            i3 = zzz;
        }
        if (this.zzbtU != null && this.zzbtU.length > 0) {
            i2 = 0;
            for (int zzz2 : this.zzbtU) {
                i2 += zzsn.zzmx(zzz2);
            }
            i3 = (i3 + i2) + (this.zzbtU.length * 1);
        }
        if (this.zzbtV == null || this.zzbtV.length <= 0) {
            return i3;
        }
        i = 0;
        i2 = 0;
        while (i4 < this.zzbtV.length) {
            byte[] bArr = this.zzbtV[i4];
            if (bArr != null) {
                i2++;
                i += zzsn.zzG(bArr);
            }
            i4++;
        }
        return (i3 + i) + (i2 * 1);
    }
}
