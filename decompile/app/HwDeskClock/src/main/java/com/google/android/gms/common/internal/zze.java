package com.google.android.gms.common.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public abstract class zze {
    public static final zze zzacH = zza((CharSequence) "\t\n\u000b\f\r     　 ᠎ ").zza(zza(' ', ' '));
    public static final zze zzacI = zza((CharSequence) "\t\n\u000b\f\r     　").zza(zza(' ', ' ')).zza(zza(' ', ' '));
    public static final zze zzacJ = zza('\u0000', '');
    public static final zze zzacK;
    public static final zze zzacL = zza('\t', '\r').zza(zza('\u001c', ' ')).zza(zzc(' ')).zza(zzc('᠎')).zza(zza(' ', ' ')).zza(zza(' ', '​')).zza(zza(' ', ' ')).zza(zzc(' ')).zza(zzc('　'));
    public static final zze zzacM = new zze() {
        public boolean zzd(char c) {
            return Character.isDigit(c);
        }
    };
    public static final zze zzacN = new zze() {
        public boolean zzd(char c) {
            return Character.isLetter(c);
        }
    };
    public static final zze zzacO = new zze() {
        public boolean zzd(char c) {
            return Character.isLetterOrDigit(c);
        }
    };
    public static final zze zzacP = new zze() {
        public boolean zzd(char c) {
            return Character.isUpperCase(c);
        }
    };
    public static final zze zzacQ = new zze() {
        public boolean zzd(char c) {
            return Character.isLowerCase(c);
        }
    };
    public static final zze zzacR = zza('\u0000', '\u001f').zza(zza('', ''));
    public static final zze zzacS = zza('\u0000', ' ').zza(zza('', ' ')).zza(zzc('­')).zza(zza('؀', '؃')).zza(zza((CharSequence) "۝܏ ឴឵᠎")).zza(zza(' ', '‏')).zza(zza(' ', ' ')).zza(zza(' ', '⁤')).zza(zza('⁪', '⁯')).zza(zzc('　')).zza(zza('?', '')).zza(zza((CharSequence) "﻿￹￺￻"));
    public static final zze zzacT = zza('\u0000', 'ӹ').zza(zzc('־')).zza(zza('א', 'ת')).zza(zzc('׳')).zza(zzc('״')).zza(zza('؀', 'ۿ')).zza(zza('ݐ', 'ݿ')).zza(zza('฀', '๿')).zza(zza('Ḁ', '₯')).zza(zza('℀', '℺')).zza(zza('ﭐ', '﷿')).zza(zza('ﹰ', '﻿')).zza(zza('｡', 'ￜ'));
    public static final zze zzacU = new zze() {
        public zze zza(zze zze) {
            zzx.zzv(zze);
            return this;
        }

        public boolean zzd(char c) {
            return true;
        }
    };
    public static final zze zzacV = new zze() {
        public zze zza(zze zze) {
            return (zze) zzx.zzv(zze);
        }

        public boolean zzd(char c) {
            return false;
        }
    };

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$11 */
    static class AnonymousClass11 extends zze {
        final /* synthetic */ char zzadb;

        AnonymousClass11(char c) {
            this.zzadb = (char) c;
        }

        public zze zza(zze zze) {
            return !zze.zzd(this.zzadb) ? super.zza(zze) : zze;
        }

        public boolean zzd(char c) {
            return c == this.zzadb;
        }
    }

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$2 */
    static class AnonymousClass2 extends zze {
        final /* synthetic */ char zzacW;
        final /* synthetic */ char zzacX;

        AnonymousClass2(char c, char c2) {
            this.zzacW = (char) c;
            this.zzacX = (char) c2;
        }

        public boolean zzd(char c) {
            return c == this.zzacW || c == this.zzacX;
        }
    }

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$4 */
    static class AnonymousClass4 extends zze {
        final /* synthetic */ char zzacZ;
        final /* synthetic */ char zzada;

        AnonymousClass4(char c, char c2) {
            this.zzacZ = (char) c;
            this.zzada = (char) c2;
        }

        public boolean zzd(char c) {
            return this.zzacZ <= c && c <= this.zzada;
        }
    }

    /* compiled from: Unknown */
    private static class zza extends zze {
        List<zze> zzadc;

        zza(List<zze> list) {
            this.zzadc = list;
        }

        public zze zza(zze zze) {
            List arrayList = new ArrayList(this.zzadc);
            arrayList.add(zzx.zzv(zze));
            return new zza(arrayList);
        }

        public boolean zzd(char c) {
            for (zze zzd : this.zzadc) {
                if (zzd.zzd(c)) {
                    return true;
                }
            }
            return false;
        }
    }

    static {
        zze zza = zza('0', '9');
        zze zze = zza;
        for (char c : "٠۰߀०০੦૦୦௦౦೦൦๐໐༠၀႐០᠐᥆᧐᭐᮰᱀᱐꘠꣐꤀꩐０".toCharArray()) {
            zze = zze.zza(zza(c, (char) (c + 9)));
        }
        zzacK = zze;
    }

    public static zze zza(char c, char c2) {
        zzx.zzZ(c2 >= c);
        return new AnonymousClass4(c, c2);
    }

    public static zze zza(CharSequence charSequence) {
        switch (charSequence.length()) {
            case 0:
                return zzacV;
            case 1:
                return zzc(charSequence.charAt(0));
            case 2:
                return new AnonymousClass2(charSequence.charAt(0), charSequence.charAt(1));
            default:
                final char[] toCharArray = charSequence.toString().toCharArray();
                Arrays.sort(toCharArray);
                return new zze() {
                    public boolean zzd(char c) {
                        return Arrays.binarySearch(toCharArray, c) >= 0;
                    }
                };
        }
    }

    public static zze zzc(char c) {
        return new AnonymousClass11(c);
    }

    public zze zza(zze zze) {
        return new zza(Arrays.asList(new zze[]{this, (zze) zzx.zzv(zze)}));
    }

    public abstract boolean zzd(char c);
}
