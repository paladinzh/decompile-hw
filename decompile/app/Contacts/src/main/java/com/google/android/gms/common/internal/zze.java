package com.google.android.gms.common.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public abstract class zze {
    public static final zze zzakF = zza((CharSequence) "\t\n\u000b\f\r     　 ᠎ ").zza(zza(' ', ' '));
    public static final zze zzakG = zza((CharSequence) "\t\n\u000b\f\r     　").zza(zza(' ', ' ')).zza(zza(' ', ' '));
    public static final zze zzakH = zza('\u0000', '');
    public static final zze zzakI;
    public static final zze zzakJ = zza('\t', '\r').zza(zza('\u001c', ' ')).zza(zzc(' ')).zza(zzc('᠎')).zza(zza(' ', ' ')).zza(zza(' ', '​')).zza(zza(' ', ' ')).zza(zzc(' ')).zza(zzc('　'));
    public static final zze zzakK = new zze() {
        public boolean zzd(char c) {
            return Character.isDigit(c);
        }
    };
    public static final zze zzakL = new zze() {
        public boolean zzd(char c) {
            return Character.isLetter(c);
        }
    };
    public static final zze zzakM = new zze() {
        public boolean zzd(char c) {
            return Character.isLetterOrDigit(c);
        }
    };
    public static final zze zzakN = new zze() {
        public boolean zzd(char c) {
            return Character.isUpperCase(c);
        }
    };
    public static final zze zzakO = new zze() {
        public boolean zzd(char c) {
            return Character.isLowerCase(c);
        }
    };
    public static final zze zzakP = zza('\u0000', '\u001f').zza(zza('', ''));
    public static final zze zzakQ = zza('\u0000', ' ').zza(zza('', ' ')).zza(zzc('­')).zza(zza('؀', '؃')).zza(zza((CharSequence) "۝܏ ឴឵᠎")).zza(zza(' ', '‏')).zza(zza(' ', ' ')).zza(zza(' ', '⁤')).zza(zza('⁪', '⁯')).zza(zzc('　')).zza(zza('?', '')).zza(zza((CharSequence) "﻿￹￺￻"));
    public static final zze zzakR = zza('\u0000', 'ӹ').zza(zzc('־')).zza(zza('א', 'ת')).zza(zzc('׳')).zza(zzc('״')).zza(zza('؀', 'ۿ')).zza(zza('ݐ', 'ݿ')).zza(zza('฀', '๿')).zza(zza('Ḁ', '₯')).zza(zza('℀', '℺')).zza(zza('ﭐ', '﷿')).zza(zza('ﹰ', '﻿')).zza(zza('｡', 'ￜ'));
    public static final zze zzakS = new zze() {
        public zze zza(zze zze) {
            zzx.zzz(zze);
            return this;
        }

        public boolean zzb(CharSequence charSequence) {
            zzx.zzz(charSequence);
            return true;
        }

        public boolean zzd(char c) {
            return true;
        }
    };
    public static final zze zzakT = new zze() {
        public zze zza(zze zze) {
            return (zze) zzx.zzz(zze);
        }

        public boolean zzb(CharSequence charSequence) {
            return charSequence.length() == 0;
        }

        public boolean zzd(char c) {
            return false;
        }
    };

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$11 */
    static class AnonymousClass11 extends zze {
        final /* synthetic */ char zzakZ;

        AnonymousClass11(char c) {
            this.zzakZ = (char) c;
        }

        public zze zza(zze zze) {
            return !zze.zzd(this.zzakZ) ? super.zza(zze) : zze;
        }

        public boolean zzd(char c) {
            return c == this.zzakZ;
        }
    }

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$2 */
    static class AnonymousClass2 extends zze {
        final /* synthetic */ char zzakU;
        final /* synthetic */ char zzakV;

        AnonymousClass2(char c, char c2) {
            this.zzakU = (char) c;
            this.zzakV = (char) c2;
        }

        public boolean zzd(char c) {
            return c == this.zzakU || c == this.zzakV;
        }
    }

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.common.internal.zze$4 */
    static class AnonymousClass4 extends zze {
        final /* synthetic */ char zzakX;
        final /* synthetic */ char zzakY;

        AnonymousClass4(char c, char c2) {
            this.zzakX = (char) c;
            this.zzakY = (char) c2;
        }

        public boolean zzd(char c) {
            return this.zzakX <= c && c <= this.zzakY;
        }
    }

    /* compiled from: Unknown */
    private static class zza extends zze {
        List<zze> zzala;

        zza(List<zze> list) {
            this.zzala = list;
        }

        public zze zza(zze zze) {
            List arrayList = new ArrayList(this.zzala);
            arrayList.add(zzx.zzz(zze));
            return new zza(arrayList);
        }

        public boolean zzd(char c) {
            for (zze zzd : this.zzala) {
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
        zzakI = zze;
    }

    public static zze zza(char c, char c2) {
        zzx.zzac(c2 >= c);
        return new AnonymousClass4(c, c2);
    }

    public static zze zza(CharSequence charSequence) {
        switch (charSequence.length()) {
            case 0:
                return zzakT;
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
        return new zza(Arrays.asList(new zze[]{this, (zze) zzx.zzz(zze)}));
    }

    public boolean zzb(CharSequence charSequence) {
        int length = charSequence.length();
        do {
            length--;
            if (length < 0) {
                return true;
            }
        } while (zzd(charSequence.charAt(length)));
        return false;
    }

    public abstract boolean zzd(char c);
}
