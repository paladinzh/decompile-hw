package com.google.android.gms.auth.api.signin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzmq;
import com.google.android.gms.internal.zzmt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class GoogleSignInAccount implements SafeParcelable {
    public static final Creator<GoogleSignInAccount> CREATOR = new zzb();
    public static zzmq zzWO = zzmt.zzsc();
    private static Comparator<Scope> zzWV = new Comparator<Scope>() {
        public /* synthetic */ int compare(Object obj, Object obj2) {
            return zza((Scope) obj, (Scope) obj2);
        }

        public int zza(Scope scope, Scope scope2) {
            return scope.zzpb().compareTo(scope2.zzpb());
        }
    };
    final int versionCode;
    List<Scope> zzVs;
    private String zzWP;
    private String zzWQ;
    private Uri zzWR;
    private String zzWS;
    private long zzWT;
    private String zzWU;
    private String zzWk;
    private String zzyv;

    GoogleSignInAccount(int versionCode, String id, String idToken, String email, String displayName, Uri photoUrl, String serverAuthCode, long expirationTimeSecs, String obfuscatedIdentifier, List<Scope> grantedScopes) {
        this.versionCode = versionCode;
        this.zzyv = id;
        this.zzWk = idToken;
        this.zzWP = email;
        this.zzWQ = displayName;
        this.zzWR = photoUrl;
        this.zzWS = serverAuthCode;
        this.zzWT = expirationTimeSecs;
        this.zzWU = obfuscatedIdentifier;
        this.zzVs = grantedScopes;
    }

    public static GoogleSignInAccount zza(@Nullable String str, @Nullable String str2, @Nullable String str3, @Nullable String str4, @Nullable Uri uri, @Nullable Long l, @NonNull String str5, @NonNull Set<Scope> set) {
        if (l == null) {
            l = Long.valueOf(zzWO.currentTimeMillis() / 1000);
        }
        return new GoogleSignInAccount(2, str, str2, str3, str4, uri, null, l.longValue(), zzx.zzcM(str5), new ArrayList((Collection) zzx.zzz(set)));
    }

    @Nullable
    public static GoogleSignInAccount zzbH(@Nullable String str) throws JSONException {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        JSONObject jSONObject = new JSONObject(str);
        Object optString = jSONObject.optString("photoUrl", null);
        Uri parse = TextUtils.isEmpty(optString) ? null : Uri.parse(optString);
        long parseLong = Long.parseLong(jSONObject.getString("expirationTime"));
        Set hashSet = new HashSet();
        JSONArray jSONArray = jSONObject.getJSONArray("grantedScopes");
        int length = jSONArray.length();
        for (int i = 0; i < length; i++) {
            hashSet.add(new Scope(jSONArray.getString(i)));
        }
        return zza(jSONObject.optString("id"), jSONObject.optString("tokenId", null), jSONObject.optString(Scopes.EMAIL, null), jSONObject.optString("displayName", null), parse, Long.valueOf(parseLong), jSONObject.getString("obfuscatedIdentifier"), hashSet).zzbI(jSONObject.optString("serverAuthCode", null));
    }

    private JSONObject zzmJ() {
        JSONObject jSONObject = new JSONObject();
        try {
            if (getId() != null) {
                jSONObject.put("id", getId());
            }
            if (getIdToken() != null) {
                jSONObject.put("tokenId", getIdToken());
            }
            if (getEmail() != null) {
                jSONObject.put(Scopes.EMAIL, getEmail());
            }
            if (getDisplayName() != null) {
                jSONObject.put("displayName", getDisplayName());
            }
            if (getPhotoUrl() != null) {
                jSONObject.put("photoUrl", getPhotoUrl().toString());
            }
            if (getServerAuthCode() != null) {
                jSONObject.put("serverAuthCode", getServerAuthCode());
            }
            jSONObject.put("expirationTime", this.zzWT);
            jSONObject.put("obfuscatedIdentifier", zzmL());
            JSONArray jSONArray = new JSONArray();
            Collections.sort(this.zzVs, zzWV);
            for (Scope zzpb : this.zzVs) {
                jSONArray.put(zzpb.zzpb());
            }
            jSONObject.put("grantedScopes", jSONArray);
            return jSONObject;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return obj instanceof GoogleSignInAccount ? ((GoogleSignInAccount) obj).zzmI().equals(zzmI()) : false;
    }

    @Nullable
    public String getDisplayName() {
        return this.zzWQ;
    }

    @Nullable
    public String getEmail() {
        return this.zzWP;
    }

    @NonNull
    public Set<Scope> getGrantedScopes() {
        return new HashSet(this.zzVs);
    }

    @Nullable
    public String getId() {
        return this.zzyv;
    }

    @Nullable
    public String getIdToken() {
        return this.zzWk;
    }

    @Nullable
    public Uri getPhotoUrl() {
        return this.zzWR;
    }

    @Nullable
    public String getServerAuthCode() {
        return this.zzWS;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }

    public boolean zzb() {
        return !(((zzWO.currentTimeMillis() / 1000) > (this.zzWT - 300) ? 1 : ((zzWO.currentTimeMillis() / 1000) == (this.zzWT - 300) ? 0 : -1)) < 0);
    }

    public GoogleSignInAccount zzbI(String str) {
        this.zzWS = str;
        return this;
    }

    public String zzmI() {
        return zzmJ().toString();
    }

    public long zzmK() {
        return this.zzWT;
    }

    @NonNull
    public String zzmL() {
        return this.zzWU;
    }

    public String zzmM() {
        JSONObject zzmJ = zzmJ();
        zzmJ.remove("serverAuthCode");
        return zzmJ.toString();
    }
}
