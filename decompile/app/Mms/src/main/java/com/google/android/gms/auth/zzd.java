package com.google.android.gms.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.internal.zzl;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.zze;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/* compiled from: Unknown */
public class zzd {
    public static final int CHANGE_TYPE_ACCOUNT_ADDED = 1;
    public static final int CHANGE_TYPE_ACCOUNT_REMOVED = 2;
    public static final int CHANGE_TYPE_ACCOUNT_RENAMED_FROM = 3;
    public static final int CHANGE_TYPE_ACCOUNT_RENAMED_TO = 4;
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final String KEY_ANDROID_PACKAGE_NAME = (VERSION.SDK_INT < 14 ? "androidPackageName" : "androidPackageName");
    public static final String KEY_CALLER_UID = (VERSION.SDK_INT < 11 ? "callerUid" : "callerUid");
    public static final String KEY_REQUEST_ACTIONS = "request_visible_actions";
    @Deprecated
    public static final String KEY_REQUEST_VISIBLE_ACTIVITIES = "request_visible_actions";
    public static final String KEY_SUPPRESS_PROGRESS_SCREEN = "suppressProgressScreen";
    private static final ComponentName zzVe = new ComponentName("com.google.android.gms", "com.google.android.gms.auth.GetToken");
    private static final ComponentName zzVf = new ComponentName("com.google.android.gms", "com.google.android.gms.recovery.RecoveryService");

    /* compiled from: Unknown */
    private interface zza<T> {
        T zzan(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException;
    }

    zzd() {
    }

    public static void clearToken(Context context, final String token) throws GooglePlayServicesAvailabilityException, GoogleAuthException, IOException {
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        zzad(context);
        final Bundle bundle = new Bundle();
        String str = context.getApplicationInfo().packageName;
        bundle.putString("clientPackageName", str);
        if (!bundle.containsKey(KEY_ANDROID_PACKAGE_NAME)) {
            bundle.putString(KEY_ANDROID_PACKAGE_NAME, str);
        }
        zza(context, zzVe, new zza<Void>() {
            public /* synthetic */ Object zzan(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                return zzao(iBinder);
            }

            public Void zzao(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                Bundle bundle = (Bundle) zzd.zzm(com.google.android.gms.internal.zzas.zza.zza(iBinder).zza(token, bundle));
                String string = bundle.getString("Error");
                if (bundle.getBoolean("booleanResult")) {
                    return null;
                }
                throw new GoogleAuthException(string);
            }
        });
    }

    public static List<AccountChangeEvent> getAccountChangeEvents(Context context, final int eventIndex, final String accountName) throws GoogleAuthException, IOException {
        zzx.zzh(accountName, "accountName must be provided");
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        zzad(context);
        return (List) zza(context, zzVe, new zza<List<AccountChangeEvent>>() {
            public /* synthetic */ Object zzan(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                return zzap(iBinder);
            }

            public List<AccountChangeEvent> zzap(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                return ((AccountChangeEventsResponse) zzd.zzm(com.google.android.gms.internal.zzas.zza.zza(iBinder).zza(new AccountChangeEventsRequest().setAccountName(accountName).setEventIndex(eventIndex)))).getEvents();
            }
        });
    }

    public static String getAccountId(Context ctx, String accountName) throws GoogleAuthException, IOException {
        zzx.zzh(accountName, "accountName must be provided");
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        zzad(ctx);
        return getToken(ctx, accountName, "^^_account_id_^^", new Bundle());
    }

    public static String getToken(Context context, Account account, String scope) throws IOException, UserRecoverableAuthException, GoogleAuthException {
        return getToken(context, account, scope, new Bundle());
    }

    public static String getToken(Context context, Account account, String scope, Bundle extras) throws IOException, UserRecoverableAuthException, GoogleAuthException {
        return zzc(context, account, scope, extras).getToken();
    }

    @Deprecated
    public static String getToken(Context context, String accountName, String scope) throws IOException, UserRecoverableAuthException, GoogleAuthException {
        return getToken(context, new Account(accountName, "com.google"), scope);
    }

    @Deprecated
    public static String getToken(Context context, String accountName, String scope, Bundle extras) throws IOException, UserRecoverableAuthException, GoogleAuthException {
        return getToken(context, new Account(accountName, "com.google"), scope, extras);
    }

    @RequiresPermission("android.permission.MANAGE_ACCOUNTS")
    @Deprecated
    public static void invalidateToken(Context context, String token) {
        AccountManager.get(context).invalidateAuthToken("com.google", token);
    }

    private static <T> T zza(Context context, ComponentName componentName, zza<T> zza) throws IOException, GoogleAuthException {
        ServiceConnection zza2 = new com.google.android.gms.common.zza();
        zzl zzau = zzl.zzau(context);
        if (zzau.zza(componentName, zza2, "GoogleAuthUtil")) {
            try {
                T zzan = zza.zzan(zza2.zzoJ());
                zzau.zzb(componentName, zza2, "GoogleAuthUtil");
                return zzan;
            } catch (Throwable e) {
                Log.i("GoogleAuthUtil", "Error on service connection.", e);
                throw new IOException("Error on service connection.", e);
            } catch (Throwable th) {
                zzau.zzb(componentName, zza2, "GoogleAuthUtil");
            }
        } else {
            throw new IOException("Could not bind to service.");
        }
    }

    private static void zzad(Context context) throws GoogleAuthException {
        try {
            zze.zzad(context.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            throw new GooglePlayServicesAvailabilityException(e.getConnectionStatusCode(), e.getMessage(), e.getIntent());
        } catch (GooglePlayServicesNotAvailableException e2) {
            throw new GoogleAuthException(e2.getMessage());
        }
    }

    public static TokenData zzc(Context context, final Account account, final String str, Bundle bundle) throws IOException, UserRecoverableAuthException, GoogleAuthException {
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        zzad(context);
        final Bundle bundle2 = bundle != null ? new Bundle(bundle) : new Bundle();
        String str2 = context.getApplicationInfo().packageName;
        bundle2.putString("clientPackageName", str2);
        if (TextUtils.isEmpty(bundle2.getString(KEY_ANDROID_PACKAGE_NAME))) {
            bundle2.putString(KEY_ANDROID_PACKAGE_NAME, str2);
        }
        bundle2.putLong("service_connection_start_time_millis", SystemClock.elapsedRealtime());
        return (TokenData) zza(context, zzVe, new zza<TokenData>() {
            public TokenData zzam(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                Bundle bundle = (Bundle) zzd.zzm(com.google.android.gms.internal.zzas.zza.zza(iBinder).zza(account, str, bundle2));
                TokenData zzc = TokenData.zzc(bundle, "tokenDetails");
                if (zzc != null) {
                    return zzc;
                }
                String string = bundle.getString("Error");
                Intent intent = (Intent) bundle.getParcelable("userRecoveryIntent");
                com.google.android.gms.auth.firstparty.shared.zzd zzbY = com.google.android.gms.auth.firstparty.shared.zzd.zzbY(string);
                if (com.google.android.gms.auth.firstparty.shared.zzd.zza(zzbY)) {
                    throw new UserRecoverableAuthException(string, intent);
                } else if (com.google.android.gms.auth.firstparty.shared.zzd.zzc(zzbY)) {
                    throw new IOException(string);
                } else {
                    throw new GoogleAuthException(string);
                }
            }

            public /* synthetic */ Object zzan(IBinder iBinder) throws RemoteException, IOException, GoogleAuthException {
                return zzam(iBinder);
            }
        });
    }

    static void zzi(Intent intent) {
        if (intent != null) {
            try {
                Intent.parseUri(intent.toUri(1), 1);
                return;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Parameter callback contains invalid data. It must be serializable using toUri() and parseUri().");
            }
        }
        throw new IllegalArgumentException("Callback cannot be null.");
    }

    private static <T> T zzm(T t) throws IOException {
        if (t != null) {
            return t;
        }
        Log.w("GoogleAuthUtil", "Binder call returned null.");
        throw new IOException("Service unavailable.");
    }
}
