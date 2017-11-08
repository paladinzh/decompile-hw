package com.google.android.gms.auth.api.signin.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.internal.zzx;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONException;

/* compiled from: Unknown */
public class zzq {
    private static final Lock zzYa = new ReentrantLock();
    private static zzq zzYb;
    private final Lock zzYc = new ReentrantLock();
    private final SharedPreferences zzYd;

    zzq(Context context) {
        this.zzYd = context.getSharedPreferences("com.google.android.gms.signin", 0);
    }

    public static zzq zzaf(Context context) {
        zzx.zzz(context);
        zzYa.lock();
        try {
            if (zzYb == null) {
                zzYb = new zzq(context.getApplicationContext());
            }
            zzq zzq = zzYb;
            return zzq;
        } finally {
            zzYa.unlock();
        }
    }

    private String zzs(String str, String str2) {
        return str + ":" + str2;
    }

    void zza(GoogleSignInAccount googleSignInAccount, GoogleSignInOptions googleSignInOptions) {
        zzx.zzz(googleSignInAccount);
        zzx.zzz(googleSignInOptions);
        String zzmL = googleSignInAccount.zzmL();
        zzr(zzs("googleSignInAccount", zzmL), googleSignInAccount.zzmM());
        zzr(zzs("googleSignInOptions", zzmL), googleSignInOptions.zzmI());
    }

    void zza(SignInAccount signInAccount, SignInConfiguration signInConfiguration) {
        zzx.zzz(signInAccount);
        zzx.zzz(signInConfiguration);
        String userId = signInAccount.getUserId();
        SignInAccount zzbP = zzbP(userId);
        if (!(zzbP == null || zzbP.zzmV() == null)) {
            zzbU(zzbP.zzmV().zzmL());
        }
        zzr(zzs("signInConfiguration", userId), signInConfiguration.zzmI());
        zzr(zzs("signInAccount", userId), signInAccount.zzmI());
        if (signInAccount.zzmV() != null) {
            zza(signInAccount.zzmV(), signInConfiguration.zznm());
        }
    }

    public void zzb(GoogleSignInAccount googleSignInAccount, GoogleSignInOptions googleSignInOptions) {
        zzx.zzz(googleSignInAccount);
        zzx.zzz(googleSignInOptions);
        zzr("defaultGoogleSignInAccount", googleSignInAccount.zzmL());
        zza(googleSignInAccount, googleSignInOptions);
    }

    public void zzb(SignInAccount signInAccount, SignInConfiguration signInConfiguration) {
        zzx.zzz(signInAccount);
        zzx.zzz(signInConfiguration);
        zznq();
        zzr("defaultSignInAccount", signInAccount.getUserId());
        if (signInAccount.zzmV() != null) {
            zzr("defaultGoogleSignInAccount", signInAccount.zzmV().zzmL());
        }
        zza(signInAccount, signInConfiguration);
    }

    SignInAccount zzbP(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Object zzbS = zzbS(zzs("signInAccount", str));
        if (TextUtils.isEmpty(zzbS)) {
            return null;
        }
        try {
            SignInAccount zzbM = SignInAccount.zzbM(zzbS);
            if (zzbM.zzmV() != null) {
                GoogleSignInAccount zzbQ = zzbQ(zzbM.zzmV().zzmL());
                if (zzbQ != null) {
                    zzbM.zza(zzbQ);
                }
            }
            return zzbM;
        } catch (JSONException e) {
            return null;
        }
    }

    GoogleSignInAccount zzbQ(String str) {
        GoogleSignInAccount googleSignInAccount = null;
        if (TextUtils.isEmpty(str)) {
            return googleSignInAccount;
        }
        String zzbS = zzbS(zzs("googleSignInAccount", str));
        if (zzbS != null) {
            try {
                googleSignInAccount = GoogleSignInAccount.zzbH(zzbS);
            } catch (JSONException e) {
                return googleSignInAccount;
            }
        }
        return googleSignInAccount;
    }

    GoogleSignInOptions zzbR(String str) {
        GoogleSignInOptions googleSignInOptions = null;
        if (TextUtils.isEmpty(str)) {
            return googleSignInOptions;
        }
        String zzbS = zzbS(zzs("googleSignInOptions", str));
        if (zzbS != null) {
            try {
                googleSignInOptions = GoogleSignInOptions.zzbJ(zzbS);
            } catch (JSONException e) {
                return googleSignInOptions;
            }
        }
        return googleSignInOptions;
    }

    protected String zzbS(String str) {
        this.zzYc.lock();
        try {
            String string = this.zzYd.getString(str, null);
            return string;
        } finally {
            this.zzYc.unlock();
        }
    }

    void zzbT(String str) {
        if (!TextUtils.isEmpty(str)) {
            SignInAccount zzbP = zzbP(str);
            zzbV(zzs("signInAccount", str));
            zzbV(zzs("signInConfiguration", str));
            if (!(zzbP == null || zzbP.zzmV() == null)) {
                zzbU(zzbP.zzmV().zzmL());
            }
        }
    }

    void zzbU(String str) {
        if (!TextUtils.isEmpty(str)) {
            zzbV(zzs("googleSignInAccount", str));
            zzbV(zzs("googleSignInOptions", str));
        }
    }

    protected void zzbV(String str) {
        this.zzYc.lock();
        try {
            this.zzYd.edit().remove(str).apply();
        } finally {
            this.zzYc.unlock();
        }
    }

    public GoogleSignInAccount zzno() {
        return zzbQ(zzbS("defaultGoogleSignInAccount"));
    }

    public GoogleSignInOptions zznp() {
        return zzbR(zzbS("defaultGoogleSignInAccount"));
    }

    public void zznq() {
        String zzbS = zzbS("defaultSignInAccount");
        zzbV("defaultSignInAccount");
        zznr();
        zzbT(zzbS);
    }

    public void zznr() {
        String zzbS = zzbS("defaultGoogleSignInAccount");
        zzbV("defaultGoogleSignInAccount");
        zzbU(zzbS);
    }

    protected void zzr(String str, String str2) {
        this.zzYc.lock();
        try {
            this.zzYd.edit().putString(str, str2).apply();
        } finally {
            this.zzYc.unlock();
        }
    }
}
