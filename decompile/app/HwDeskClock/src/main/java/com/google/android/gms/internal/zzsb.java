package com.google.android.gms.internal;

import com.android.deskclock.alarmclock.MetaballPath;
import com.android.deskclock.alarmclock.PortCallPanelView;
import java.io.IOException;
import java.util.Arrays;

/* compiled from: Unknown */
public interface zzsb {

    /* compiled from: Unknown */
    public static final class zza extends zzrr<zza> {
        public int[] zzbcA;
        public long[] zzbcB;
        public String[] zzbcy;
        public String[] zzbcz;

        public zza() {
            zzDA();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza zza = (zza) o;
            return (zzrv.equals(this.zzbcy, zza.zzbcy) && zzrv.equals(this.zzbcz, zza.zzbcz) && zzrv.equals(this.zzbcA, zza.zzbcA) && zzrv.equals(this.zzbcB, zza.zzbcB)) ? zza((zzrr) zza) : false;
        }

        public int hashCode() {
            return ((((((((zzrv.hashCode(this.zzbcy) + 527) * 31) + zzrv.hashCode(this.zzbcz)) * 31) + zzrv.hashCode(this.zzbcA)) * 31) + zzrv.hashCode(this.zzbcB)) * 31) + zzDm();
        }

        protected int zzB() {
            int i;
            int i2;
            int i3;
            int i4 = 0;
            int zzB = super.zzB();
            if (this.zzbcy != null && this.zzbcy.length > 0) {
                i = 0;
                i2 = 0;
                for (String str : this.zzbcy) {
                    if (str != null) {
                        i2++;
                        i += zzrq.zzfy(str);
                    }
                }
                i3 = (zzB + i) + (i2 * 1);
            } else {
                i3 = zzB;
            }
            if (this.zzbcz != null && this.zzbcz.length > 0) {
                i2 = 0;
                zzB = 0;
                for (String str2 : this.zzbcz) {
                    if (str2 != null) {
                        zzB++;
                        i2 += zzrq.zzfy(str2);
                    }
                }
                i3 = (i3 + i2) + (zzB * 1);
            }
            if (this.zzbcA != null && this.zzbcA.length > 0) {
                i2 = 0;
                for (int zzB2 : this.zzbcA) {
                    i2 += zzrq.zzls(zzB2);
                }
                i3 = (i3 + i2) + (this.zzbcA.length * 1);
            }
            if (this.zzbcB == null || this.zzbcB.length <= 0) {
                return i3;
            }
            i = 0;
            while (i4 < this.zzbcB.length) {
                i += zzrq.zzY(this.zzbcB[i4]);
                i4++;
            }
            return (i3 + i) + (this.zzbcB.length * 1);
        }

        public zza zzB(zzrp zzrp) throws IOException {
            while (true) {
                int zzCV = zzrp.zzCV();
                int zzb;
                Object obj;
                int zzll;
                Object obj2;
                switch (zzCV) {
                    case 0:
                        return this;
                    case 10:
                        zzb = zzsa.zzb(zzrp, 10);
                        zzCV = this.zzbcy != null ? this.zzbcy.length : 0;
                        obj = new String[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcy, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.readString();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.readString();
                        this.zzbcy = obj;
                        break;
                    case 18:
                        zzb = zzsa.zzb(zzrp, 18);
                        zzCV = this.zzbcz != null ? this.zzbcz.length : 0;
                        obj = new String[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcz, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.readString();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.readString();
                        this.zzbcz = obj;
                        break;
                    case 24:
                        zzb = zzsa.zzb(zzrp, 24);
                        zzCV = this.zzbcA != null ? this.zzbcA.length : 0;
                        obj = new int[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcA, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.zzCY();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.zzCY();
                        this.zzbcA = obj;
                        break;
                    case 26:
                        zzll = zzrp.zzll(zzrp.zzDc());
                        zzb = zzrp.getPosition();
                        zzCV = 0;
                        while (zzrp.zzDh() > 0) {
                            zzrp.zzCY();
                            zzCV++;
                        }
                        zzrp.zzln(zzb);
                        zzb = this.zzbcA != null ? this.zzbcA.length : 0;
                        obj2 = new int[(zzCV + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzbcA, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = zzrp.zzCY();
                            zzb++;
                        }
                        this.zzbcA = obj2;
                        zzrp.zzlm(zzll);
                        break;
                    case 32:
                        zzb = zzsa.zzb(zzrp, 32);
                        zzCV = this.zzbcB != null ? this.zzbcB.length : 0;
                        obj = new long[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcB, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.zzCX();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.zzCX();
                        this.zzbcB = obj;
                        break;
                    case 34:
                        zzll = zzrp.zzll(zzrp.zzDc());
                        zzb = zzrp.getPosition();
                        zzCV = 0;
                        while (zzrp.zzDh() > 0) {
                            zzrp.zzCX();
                            zzCV++;
                        }
                        zzrp.zzln(zzb);
                        zzb = this.zzbcB != null ? this.zzbcB.length : 0;
                        obj2 = new long[(zzCV + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzbcB, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = zzrp.zzCX();
                            zzb++;
                        }
                        this.zzbcB = obj2;
                        zzrp.zzlm(zzll);
                        break;
                    default:
                        if (zza(zzrp, zzCV)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public zza zzDA() {
            this.zzbcy = zzsa.zzbcv;
            this.zzbcz = zzsa.zzbcv;
            this.zzbcA = zzsa.zzbcq;
            this.zzbcB = zzsa.zzbcr;
            this.zzbcd = null;
            this.zzbco = -1;
            return this;
        }

        public void zza(zzrq zzrq) throws IOException {
            int i = 0;
            if (this.zzbcy != null && this.zzbcy.length > 0) {
                for (String str : this.zzbcy) {
                    if (str != null) {
                        zzrq.zzb(1, str);
                    }
                }
            }
            if (this.zzbcz != null && this.zzbcz.length > 0) {
                for (String str2 : this.zzbcz) {
                    if (str2 != null) {
                        zzrq.zzb(2, str2);
                    }
                }
            }
            if (this.zzbcA != null && this.zzbcA.length > 0) {
                for (int zzz : this.zzbcA) {
                    zzrq.zzz(3, zzz);
                }
            }
            if (this.zzbcB != null && this.zzbcB.length > 0) {
                while (i < this.zzbcB.length) {
                    zzrq.zzb(4, this.zzbcB[i]);
                    i++;
                }
            }
            super.zza(zzrq);
        }

        public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
            return zzB(zzrp);
        }
    }

    /* compiled from: Unknown */
    public static final class zzb extends zzrr<zzb> {
        public String version;
        public int zzbcC;
        public String zzbcD;

        public zzb() {
            zzDB();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzb)) {
                return false;
            }
            zzb zzb = (zzb) o;
            if (this.zzbcC != zzb.zzbcC) {
                return false;
            }
            if (this.zzbcD != null) {
                if (!this.zzbcD.equals(zzb.zzbcD)) {
                    return false;
                }
            } else if (zzb.zzbcD != null) {
                return false;
            }
            if (this.version != null) {
                if (!this.version.equals(zzb.version)) {
                    return false;
                }
            } else if (zzb.version != null) {
                return false;
            }
            return zza((zzrr) zzb);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zzbcD != null ? this.zzbcD.hashCode() : 0) + ((this.zzbcC + 527) * 31)) * 31;
            if (this.version != null) {
                i = this.version.hashCode();
            }
            return ((hashCode + i) * 31) + zzDm();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (this.zzbcC != 0) {
                zzB += zzrq.zzB(1, this.zzbcC);
            }
            if (!this.zzbcD.equals("")) {
                zzB += zzrq.zzl(2, this.zzbcD);
            }
            return this.version.equals("") ? zzB : zzB + zzrq.zzl(3, this.version);
        }

        public zzb zzC(zzrp zzrp) throws IOException {
            while (true) {
                int zzCV = zzrp.zzCV();
                switch (zzCV) {
                    case 0:
                        return this;
                    case 8:
                        zzCV = zzrp.zzCY();
                        switch (zzCV) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case MetaballPath.POINT_NUM /*4*/:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case PortCallPanelView.DEFAUT_RADIUS /*25*/:
                            case 26:
                                this.zzbcC = zzCV;
                                break;
                            default:
                                break;
                        }
                    case 18:
                        this.zzbcD = zzrp.readString();
                        break;
                    case 26:
                        this.version = zzrp.readString();
                        break;
                    default:
                        if (zza(zzrp, zzCV)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public zzb zzDB() {
            this.zzbcC = 0;
            this.zzbcD = "";
            this.version = "";
            this.zzbcd = null;
            this.zzbco = -1;
            return this;
        }

        public void zza(zzrq zzrq) throws IOException {
            if (this.zzbcC != 0) {
                zzrq.zzz(1, this.zzbcC);
            }
            if (!this.zzbcD.equals("")) {
                zzrq.zzb(2, this.zzbcD);
            }
            if (!this.version.equals("")) {
                zzrq.zzb(3, this.version);
            }
            super.zza(zzrq);
        }

        public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
            return zzC(zzrp);
        }
    }

    /* compiled from: Unknown */
    public static final class zzc extends zzrr<zzc> {
        public byte[] zzbcE;
        public byte[][] zzbcF;
        public boolean zzbcG;

        public zzc() {
            zzDC();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzc)) {
                return false;
            }
            zzc zzc = (zzc) o;
            return (Arrays.equals(this.zzbcE, zzc.zzbcE) && zzrv.zza(this.zzbcF, zzc.zzbcF) && this.zzbcG == zzc.zzbcG) ? zza((zzrr) zzc) : false;
        }

        public int hashCode() {
            return (((!this.zzbcG ? 1237 : 1231) + ((((Arrays.hashCode(this.zzbcE) + 527) * 31) + zzrv.zza(this.zzbcF)) * 31)) * 31) + zzDm();
        }

        protected int zzB() {
            int i = 0;
            int zzB = super.zzB();
            if (!Arrays.equals(this.zzbcE, zzsa.zzbcx)) {
                zzB += zzrq.zzb(1, this.zzbcE);
            }
            if (this.zzbcF != null && this.zzbcF.length > 0) {
                int i2 = 0;
                int i3 = 0;
                while (i < this.zzbcF.length) {
                    byte[] bArr = this.zzbcF[i];
                    if (bArr != null) {
                        i3++;
                        i2 += zzrq.zzC(bArr);
                    }
                    i++;
                }
                zzB = (zzB + i2) + (i3 * 1);
            }
            return !this.zzbcG ? zzB : zzB + zzrq.zzc(3, this.zzbcG);
        }

        public zzc zzD(zzrp zzrp) throws IOException {
            while (true) {
                int zzCV = zzrp.zzCV();
                switch (zzCV) {
                    case 0:
                        return this;
                    case 10:
                        this.zzbcE = zzrp.readBytes();
                        break;
                    case 18:
                        int zzb = zzsa.zzb(zzrp, 18);
                        zzCV = this.zzbcF != null ? this.zzbcF.length : 0;
                        Object obj = new byte[(zzb + zzCV)][];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcF, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.readBytes();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.readBytes();
                        this.zzbcF = obj;
                        break;
                    case 24:
                        this.zzbcG = zzrp.zzCZ();
                        break;
                    default:
                        if (zza(zzrp, zzCV)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public zzc zzDC() {
            this.zzbcE = zzsa.zzbcx;
            this.zzbcF = zzsa.zzbcw;
            this.zzbcG = false;
            this.zzbcd = null;
            this.zzbco = -1;
            return this;
        }

        public void zza(zzrq zzrq) throws IOException {
            if (!Arrays.equals(this.zzbcE, zzsa.zzbcx)) {
                zzrq.zza(1, this.zzbcE);
            }
            if (this.zzbcF != null && this.zzbcF.length > 0) {
                for (byte[] bArr : this.zzbcF) {
                    if (bArr != null) {
                        zzrq.zza(2, bArr);
                    }
                }
            }
            if (this.zzbcG) {
                zzrq.zzb(3, this.zzbcG);
            }
            super.zza(zzrq);
        }

        public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
            return zzD(zzrp);
        }
    }

    /* compiled from: Unknown */
    public static final class zzd extends zzrr<zzd> {
        public String tag;
        public long zzbcH;
        public long zzbcI;
        public int zzbcJ;
        public int zzbcK;
        public boolean zzbcL;
        public zze[] zzbcM;
        public zzb zzbcN;
        public byte[] zzbcO;
        public byte[] zzbcP;
        public byte[] zzbcQ;
        public zza zzbcR;
        public String zzbcS;
        public long zzbcT;
        public zzc zzbcU;
        public byte[] zzbcV;
        public int zzbcW;
        public int[] zzbcX;

        public zzd() {
            zzDD();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzd)) {
                return false;
            }
            zzd zzd = (zzd) o;
            if (this.zzbcH != zzd.zzbcH || this.zzbcI != zzd.zzbcI) {
                return false;
            }
            if (this.tag != null) {
                if (!this.tag.equals(zzd.tag)) {
                    return false;
                }
            } else if (zzd.tag != null) {
                return false;
            }
            if (this.zzbcJ != zzd.zzbcJ || this.zzbcK != zzd.zzbcK || this.zzbcL != zzd.zzbcL || !zzrv.equals(this.zzbcM, zzd.zzbcM)) {
                return false;
            }
            if (this.zzbcN != null) {
                if (!this.zzbcN.equals(zzd.zzbcN)) {
                    return false;
                }
            } else if (zzd.zzbcN != null) {
                return false;
            }
            if (!Arrays.equals(this.zzbcO, zzd.zzbcO) || !Arrays.equals(this.zzbcP, zzd.zzbcP) || !Arrays.equals(this.zzbcQ, zzd.zzbcQ)) {
                return false;
            }
            if (this.zzbcR != null) {
                if (!this.zzbcR.equals(zzd.zzbcR)) {
                    return false;
                }
            } else if (zzd.zzbcR != null) {
                return false;
            }
            if (this.zzbcS != null) {
                if (!this.zzbcS.equals(zzd.zzbcS)) {
                    return false;
                }
            } else if (zzd.zzbcS != null) {
                return false;
            }
            if (this.zzbcT != zzd.zzbcT) {
                return false;
            }
            if (this.zzbcU != null) {
                if (!this.zzbcU.equals(zzd.zzbcU)) {
                    return false;
                }
            } else if (zzd.zzbcU != null) {
                return false;
            }
            return (Arrays.equals(this.zzbcV, zzd.zzbcV) && this.zzbcW == zzd.zzbcW && zzrv.equals(this.zzbcX, zzd.zzbcX)) ? zza((zzrr) zzd) : false;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((((this.zzbcS != null ? this.zzbcS.hashCode() : 0) + (((this.zzbcR != null ? this.zzbcR.hashCode() : 0) + (((((((((this.zzbcN != null ? this.zzbcN.hashCode() : 0) + (((((!this.zzbcL ? 1237 : 1231) + (((((((this.tag != null ? this.tag.hashCode() : 0) + ((((((int) (this.zzbcH ^ (this.zzbcH >>> 32))) + 527) * 31) + ((int) (this.zzbcI ^ (this.zzbcI >>> 32)))) * 31)) * 31) + this.zzbcJ) * 31) + this.zzbcK) * 31)) * 31) + zzrv.hashCode(this.zzbcM)) * 31)) * 31) + Arrays.hashCode(this.zzbcO)) * 31) + Arrays.hashCode(this.zzbcP)) * 31) + Arrays.hashCode(this.zzbcQ)) * 31)) * 31)) * 31) + ((int) (this.zzbcT ^ (this.zzbcT >>> 32)))) * 31;
            if (this.zzbcU != null) {
                i = this.zzbcU.hashCode();
            }
            return ((((((((hashCode + i) * 31) + Arrays.hashCode(this.zzbcV)) * 31) + this.zzbcW) * 31) + zzrv.hashCode(this.zzbcX)) * 31) + zzDm();
        }

        protected int zzB() {
            int i;
            int i2 = 0;
            int zzB = super.zzB();
            if (this.zzbcH != 0) {
                zzB += zzrq.zzd(1, this.zzbcH);
            }
            if (!this.tag.equals("")) {
                zzB += zzrq.zzl(2, this.tag);
            }
            if (this.zzbcM != null && this.zzbcM.length > 0) {
                i = zzB;
                for (zzrx zzrx : this.zzbcM) {
                    if (zzrx != null) {
                        i += zzrq.zzc(3, zzrx);
                    }
                }
                zzB = i;
            }
            if (!Arrays.equals(this.zzbcO, zzsa.zzbcx)) {
                zzB += zzrq.zzb(6, this.zzbcO);
            }
            if (this.zzbcR != null) {
                zzB += zzrq.zzc(7, this.zzbcR);
            }
            if (!Arrays.equals(this.zzbcP, zzsa.zzbcx)) {
                zzB += zzrq.zzb(8, this.zzbcP);
            }
            if (this.zzbcN != null) {
                zzB += zzrq.zzc(9, this.zzbcN);
            }
            if (this.zzbcL) {
                zzB += zzrq.zzc(10, this.zzbcL);
            }
            if (this.zzbcJ != 0) {
                zzB += zzrq.zzB(11, this.zzbcJ);
            }
            if (this.zzbcK != 0) {
                zzB += zzrq.zzB(12, this.zzbcK);
            }
            if (!Arrays.equals(this.zzbcQ, zzsa.zzbcx)) {
                zzB += zzrq.zzb(13, this.zzbcQ);
            }
            if (!this.zzbcS.equals("")) {
                zzB += zzrq.zzl(14, this.zzbcS);
            }
            if (this.zzbcT != 180000) {
                zzB += zzrq.zze(15, this.zzbcT);
            }
            if (this.zzbcU != null) {
                zzB += zzrq.zzc(16, this.zzbcU);
            }
            if (this.zzbcI != 0) {
                zzB += zzrq.zzd(17, this.zzbcI);
            }
            if (!Arrays.equals(this.zzbcV, zzsa.zzbcx)) {
                zzB += zzrq.zzb(18, this.zzbcV);
            }
            if (this.zzbcW != 0) {
                zzB += zzrq.zzB(19, this.zzbcW);
            }
            if (this.zzbcX == null || this.zzbcX.length <= 0) {
                return zzB;
            }
            i = 0;
            while (i2 < this.zzbcX.length) {
                i += zzrq.zzls(this.zzbcX[i2]);
                i2++;
            }
            return (zzB + i) + (this.zzbcX.length * 2);
        }

        public zzd zzDD() {
            this.zzbcH = 0;
            this.zzbcI = 0;
            this.tag = "";
            this.zzbcJ = 0;
            this.zzbcK = 0;
            this.zzbcL = false;
            this.zzbcM = zze.zzDE();
            this.zzbcN = null;
            this.zzbcO = zzsa.zzbcx;
            this.zzbcP = zzsa.zzbcx;
            this.zzbcQ = zzsa.zzbcx;
            this.zzbcR = null;
            this.zzbcS = "";
            this.zzbcT = 180000;
            this.zzbcU = null;
            this.zzbcV = zzsa.zzbcx;
            this.zzbcW = 0;
            this.zzbcX = zzsa.zzbcq;
            this.zzbcd = null;
            this.zzbco = -1;
            return this;
        }

        public zzd zzE(zzrp zzrp) throws IOException {
            while (true) {
                int zzCV = zzrp.zzCV();
                int zzb;
                Object obj;
                switch (zzCV) {
                    case 0:
                        return this;
                    case 8:
                        this.zzbcH = zzrp.zzCX();
                        break;
                    case 18:
                        this.tag = zzrp.readString();
                        break;
                    case 26:
                        zzb = zzsa.zzb(zzrp, 26);
                        zzCV = this.zzbcM != null ? this.zzbcM.length : 0;
                        obj = new zze[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcM, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = new zze();
                            zzrp.zza(obj[zzCV]);
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = new zze();
                        zzrp.zza(obj[zzCV]);
                        this.zzbcM = obj;
                        break;
                    case 50:
                        this.zzbcO = zzrp.readBytes();
                        break;
                    case 58:
                        if (this.zzbcR == null) {
                            this.zzbcR = new zza();
                        }
                        zzrp.zza(this.zzbcR);
                        break;
                    case 66:
                        this.zzbcP = zzrp.readBytes();
                        break;
                    case 74:
                        if (this.zzbcN == null) {
                            this.zzbcN = new zzb();
                        }
                        zzrp.zza(this.zzbcN);
                        break;
                    case 80:
                        this.zzbcL = zzrp.zzCZ();
                        break;
                    case 88:
                        this.zzbcJ = zzrp.zzCY();
                        break;
                    case 96:
                        this.zzbcK = zzrp.zzCY();
                        break;
                    case 106:
                        this.zzbcQ = zzrp.readBytes();
                        break;
                    case 114:
                        this.zzbcS = zzrp.readString();
                        break;
                    case 120:
                        this.zzbcT = zzrp.zzDb();
                        break;
                    case 130:
                        if (this.zzbcU == null) {
                            this.zzbcU = new zzc();
                        }
                        zzrp.zza(this.zzbcU);
                        break;
                    case 136:
                        this.zzbcI = zzrp.zzCX();
                        break;
                    case 146:
                        this.zzbcV = zzrp.readBytes();
                        break;
                    case 152:
                        zzCV = zzrp.zzCY();
                        switch (zzCV) {
                            case 0:
                            case 1:
                            case 2:
                                this.zzbcW = zzCV;
                                break;
                            default:
                                break;
                        }
                    case 160:
                        zzb = zzsa.zzb(zzrp, 160);
                        zzCV = this.zzbcX != null ? this.zzbcX.length : 0;
                        obj = new int[(zzb + zzCV)];
                        if (zzCV != 0) {
                            System.arraycopy(this.zzbcX, 0, obj, 0, zzCV);
                        }
                        while (zzCV < obj.length - 1) {
                            obj[zzCV] = zzrp.zzCY();
                            zzrp.zzCV();
                            zzCV++;
                        }
                        obj[zzCV] = zzrp.zzCY();
                        this.zzbcX = obj;
                        break;
                    case 162:
                        int zzll = zzrp.zzll(zzrp.zzDc());
                        zzb = zzrp.getPosition();
                        zzCV = 0;
                        while (zzrp.zzDh() > 0) {
                            zzrp.zzCY();
                            zzCV++;
                        }
                        zzrp.zzln(zzb);
                        zzb = this.zzbcX != null ? this.zzbcX.length : 0;
                        Object obj2 = new int[(zzCV + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzbcX, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = zzrp.zzCY();
                            zzb++;
                        }
                        this.zzbcX = obj2;
                        zzrp.zzlm(zzll);
                        break;
                    default:
                        if (zza(zzrp, zzCV)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public void zza(zzrq zzrq) throws IOException {
            int i = 0;
            if (this.zzbcH != 0) {
                zzrq.zzb(1, this.zzbcH);
            }
            if (!this.tag.equals("")) {
                zzrq.zzb(2, this.tag);
            }
            if (this.zzbcM != null && this.zzbcM.length > 0) {
                for (zzrx zzrx : this.zzbcM) {
                    if (zzrx != null) {
                        zzrq.zza(3, zzrx);
                    }
                }
            }
            if (!Arrays.equals(this.zzbcO, zzsa.zzbcx)) {
                zzrq.zza(6, this.zzbcO);
            }
            if (this.zzbcR != null) {
                zzrq.zza(7, this.zzbcR);
            }
            if (!Arrays.equals(this.zzbcP, zzsa.zzbcx)) {
                zzrq.zza(8, this.zzbcP);
            }
            if (this.zzbcN != null) {
                zzrq.zza(9, this.zzbcN);
            }
            if (this.zzbcL) {
                zzrq.zzb(10, this.zzbcL);
            }
            if (this.zzbcJ != 0) {
                zzrq.zzz(11, this.zzbcJ);
            }
            if (this.zzbcK != 0) {
                zzrq.zzz(12, this.zzbcK);
            }
            if (!Arrays.equals(this.zzbcQ, zzsa.zzbcx)) {
                zzrq.zza(13, this.zzbcQ);
            }
            if (!this.zzbcS.equals("")) {
                zzrq.zzb(14, this.zzbcS);
            }
            if (this.zzbcT != 180000) {
                zzrq.zzc(15, this.zzbcT);
            }
            if (this.zzbcU != null) {
                zzrq.zza(16, this.zzbcU);
            }
            if (this.zzbcI != 0) {
                zzrq.zzb(17, this.zzbcI);
            }
            if (!Arrays.equals(this.zzbcV, zzsa.zzbcx)) {
                zzrq.zza(18, this.zzbcV);
            }
            if (this.zzbcW != 0) {
                zzrq.zzz(19, this.zzbcW);
            }
            if (this.zzbcX != null && this.zzbcX.length > 0) {
                while (i < this.zzbcX.length) {
                    zzrq.zzz(20, this.zzbcX[i]);
                    i++;
                }
            }
            super.zza(zzrq);
        }

        public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
            return zzE(zzrp);
        }
    }

    /* compiled from: Unknown */
    public static final class zze extends zzrr<zze> {
        private static volatile zze[] zzbcY;
        public String key;
        public String value;

        public zze() {
            zzDF();
        }

        public static zze[] zzDE() {
            if (zzbcY == null) {
                synchronized (zzrv.zzbcn) {
                    if (zzbcY == null) {
                        zzbcY = new zze[0];
                    }
                }
            }
            return zzbcY;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zze)) {
                return false;
            }
            zze zze = (zze) o;
            if (this.key != null) {
                if (!this.key.equals(zze.key)) {
                    return false;
                }
            } else if (zze.key != null) {
                return false;
            }
            if (this.value != null) {
                if (!this.value.equals(zze.value)) {
                    return false;
                }
            } else if (zze.value != null) {
                return false;
            }
            return zza((zzrr) zze);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.key != null ? this.key.hashCode() : 0) + 527) * 31;
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return ((hashCode + i) * 31) + zzDm();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (!this.key.equals("")) {
                zzB += zzrq.zzl(1, this.key);
            }
            return this.value.equals("") ? zzB : zzB + zzrq.zzl(2, this.value);
        }

        public zze zzDF() {
            this.key = "";
            this.value = "";
            this.zzbcd = null;
            this.zzbco = -1;
            return this;
        }

        public zze zzF(zzrp zzrp) throws IOException {
            while (true) {
                int zzCV = zzrp.zzCV();
                switch (zzCV) {
                    case 0:
                        return this;
                    case 10:
                        this.key = zzrp.readString();
                        break;
                    case 18:
                        this.value = zzrp.readString();
                        break;
                    default:
                        if (zza(zzrp, zzCV)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public void zza(zzrq zzrq) throws IOException {
            if (!this.key.equals("")) {
                zzrq.zzb(1, this.key);
            }
            if (!this.value.equals("")) {
                zzrq.zzb(2, this.value);
            }
            super.zza(zzrq);
        }

        public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
            return zzF(zzrp);
        }
    }
}
