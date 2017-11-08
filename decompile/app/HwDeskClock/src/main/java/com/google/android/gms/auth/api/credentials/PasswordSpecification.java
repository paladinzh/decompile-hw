package com.google.android.gms.auth.api.credentials;

import android.os.Parcel;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/* compiled from: Unknown */
public final class PasswordSpecification implements SafeParcelable {
    public static final zze CREATOR = new zze();
    public static final PasswordSpecification zzRo = new zza().zzh(12, 16).zzbD("abcdefghijkmnopqrstxyzABCDEFGHJKLMNPQRSTXY3456789").zzf("abcdefghijkmnopqrstxyz", 1).zzf("ABCDEFGHJKLMNPQRSTXY", 1).zzf("3456789", 1).zzlx();
    public static final PasswordSpecification zzRp = new zza().zzh(12, 16).zzbD("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890").zzf("abcdefghijklmnopqrstuvwxyz", 1).zzf("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1).zzf("1234567890", 1).zzlx();
    final int mVersionCode;
    final String zzRq;
    final List<String> zzRr;
    final List<Integer> zzRs;
    final int zzRt;
    final int zzRu;
    private final int[] zzRv = zzlw();
    private final Random zzsT = new SecureRandom();

    /* compiled from: Unknown */
    public static class zza {
        private final List<String> zzRr = new ArrayList();
        private final List<Integer> zzRs = new ArrayList();
        private int zzRt = 12;
        private int zzRu = 16;
        private final TreeSet<Character> zzRw = new TreeSet();

        private void zzly() {
            int i = 0;
            for (Integer intValue : this.zzRs) {
                i = intValue.intValue() + i;
            }
            if (i > this.zzRu) {
                throw new zzb("required character count cannot be greater than the max password size");
            }
        }

        private void zzlz() {
            boolean[] zArr = new boolean[95];
            for (String toCharArray : this.zzRr) {
                for (char c : toCharArray.toCharArray()) {
                    if (zArr[c - 32]) {
                        throw new zzb("character " + c + " occurs in more than one required character set");
                    }
                    zArr[c - 32] = true;
                }
            }
        }

        private TreeSet<Character> zzr(String str, String str2) {
            if (TextUtils.isEmpty(str)) {
                throw new zzb(str2 + " cannot be null or empty");
            }
            TreeSet<Character> treeSet = new TreeSet();
            for (char c : str.toCharArray()) {
                if (PasswordSpecification.zzb(c, 32, 126)) {
                    throw new zzb(str2 + " must only contain ASCII printable characters");
                }
                treeSet.add(Character.valueOf(c));
            }
            return treeSet;
        }

        public zza zzbD(String str) {
            this.zzRw.addAll(zzr(str, "allowedChars"));
            return this;
        }

        public zza zzf(String str, int i) {
            if (i >= 1) {
                this.zzRr.add(PasswordSpecification.zzb(zzr(str, "requiredChars")));
                this.zzRs.add(Integer.valueOf(i));
                return this;
            }
            throw new zzb("count must be at least 1");
        }

        public zza zzh(int i, int i2) {
            if (i < 1) {
                throw new zzb("minimumSize must be at least 1");
            } else if (i <= i2) {
                this.zzRt = i;
                this.zzRu = i2;
                return this;
            } else {
                throw new zzb("maximumSize must be greater than or equal to minimumSize");
            }
        }

        public PasswordSpecification zzlx() {
            if (this.zzRw.isEmpty()) {
                throw new zzb("no allowed characters specified");
            }
            zzly();
            zzlz();
            return new PasswordSpecification(1, PasswordSpecification.zzb(this.zzRw), this.zzRr, this.zzRs, this.zzRt, this.zzRu);
        }
    }

    /* compiled from: Unknown */
    public static class zzb extends Error {
        public zzb(String str) {
            super(str);
        }
    }

    PasswordSpecification(int version, String allowedChars, List<String> requiredCharSets, List<Integer> requiredCharCounts, int minimumSize, int maximumSize) {
        this.mVersionCode = version;
        this.zzRq = allowedChars;
        this.zzRr = Collections.unmodifiableList(requiredCharSets);
        this.zzRs = Collections.unmodifiableList(requiredCharCounts);
        this.zzRt = minimumSize;
        this.zzRu = maximumSize;
    }

    private int zza(char c) {
        return c - 32;
    }

    private static String zzb(Collection<Character> collection) {
        int i = 0;
        char[] cArr = new char[collection.size()];
        Iterator it = collection.iterator();
        while (true) {
            int i2 = i;
            if (!it.hasNext()) {
                return new String(cArr);
            }
            i = i2 + 1;
            cArr[i2] = (char) ((Character) it.next()).charValue();
        }
    }

    private static boolean zzb(int i, int i2, int i3) {
        return i < i2 || i > i3;
    }

    private int[] zzlw() {
        int[] iArr = new int[95];
        Arrays.fill(iArr, -1);
        int i = 0;
        for (String toCharArray : this.zzRr) {
            for (char zza : toCharArray.toCharArray()) {
                iArr[zza(zza)] = i;
            }
            i++;
        }
        return iArr;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }
}
