package com.fyusion.sdk.share;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;
import com.a.a.a.j;
import com.a.a.n.b;
import com.a.a.n.c;
import com.a.a.s;
import com.fyusion.sdk.common.AuthenticationException;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.share.exception.ConnectionException;
import com.fyusion.sdk.share.exception.NoInternetPermissionSatisfiedException;
import com.fyusion.sdk.share.exception.NoNetworkConnectionException;
import com.fyusion.sdk.share.exception.ServerException;
import com.fyusion.sdk.share.exception.UserAuthenticationException;
import com.fyusion.sdk.share.exception.UserEmailExistsException;
import com.fyusion.sdk.share.exception.UserEmailInvalidException;
import com.fyusion.sdk.share.exception.UserEmailMissingException;
import com.fyusion.sdk.share.exception.UserExistsException;
import com.fyusion.sdk.share.exception.UserLoginCredentialsException;
import com.fyusion.sdk.share.exception.UserLoginNoAccountException;
import com.fyusion.sdk.share.exception.UserLongException;
import com.fyusion.sdk.share.exception.UserMissingException;
import com.fyusion.sdk.share.exception.UserPasswordMissingException;
import com.fyusion.sdk.share.exception.UserPasswordShortException;
import com.fyusion.sdk.share.exception.UserShortException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class FyuseUserManager {
    public static final int THIRD_PARTY_SERVICE_CUSTOM_1 = 4;
    public static final int THIRD_PARTY_SERVICE_FACEBOOK = 1;
    public static final int THIRD_PARTY_SERVICE_GOOGLE = 2;
    public static final int THIRD_PARTY_SERVICE_TWITTER = 3;
    private static final String a = FyuseUserManager.class.getSimpleName();
    private String b;
    private String c;
    private String d;
    private String e;
    private Bitmap f;
    private String g;
    private String h;
    private int i = 0;
    private FyuseUserListener j;

    private static void a() throws AuthenticationException {
        if (a.a().f("share")) {
            throw new AuthenticationException("share component is disabled.");
        }
    }

    private void a(String str) throws JSONException {
        if (str != null) {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("error") && jSONObject.getInt("error") > 0) {
                if (this.j != null) {
                    if (jSONObject.has("k")) {
                        switch (jSONObject.getInt("k")) {
                            case 0:
                                this.j.onUserError(new UserLoginNoAccountException(jSONObject.getString("email")));
                                break;
                            case 1:
                                this.j.onUserError(new UserLoginCredentialsException(jSONObject.getString("email")));
                                break;
                            case 11:
                                this.j.onUserError(new UserPasswordShortException());
                                break;
                            case 12:
                                this.j.onUserError(new UserPasswordShortException());
                                break;
                            case 21:
                            case 23:
                            case 24:
                                this.j.onUserError(new UserEmailInvalidException(jSONObject.getString("email")));
                                break;
                            case 22:
                                this.j.onUserError(new UserEmailExistsException(jSONObject.getString("email")));
                                break;
                            case 31:
                                this.j.onUserError(new UserShortException(jSONObject.getString("username")));
                                break;
                            case 32:
                                this.j.onUserError(new UserLongException(jSONObject.getString("username")));
                                break;
                            case 33:
                                this.j.onUserError(new UserExistsException(jSONObject.getString("username")));
                                break;
                            default:
                                if (!jSONObject.has("msg")) {
                                    this.j.onUserError(new UserAuthenticationException(jSONObject.getString("k")));
                                    break;
                                } else {
                                    this.j.onUserError(new UserAuthenticationException(jSONObject.getString("msg")));
                                    break;
                                }
                        }
                    } else if (jSONObject.has("msg")) {
                        this.j.onSDKError(new FyuseSDKException(jSONObject.getString("msg")));
                    } else {
                        this.j.onSDKError(new FyuseSDKException());
                    }
                }
            } else if (jSONObject.has("success") && jSONObject.getInt("success") != 0) {
                if (jSONObject.has("a") && !"0".equals(jSONObject.getString("a"))) {
                    a.a().g(jSONObject.getString("a"));
                    if (this.j != null) {
                        FyuseUser fyuseUser = new FyuseUser();
                        if (jSONObject.has("m")) {
                            fyuseUser.c(jSONObject.getString("m"));
                        }
                        if (jSONObject.has("f")) {
                            fyuseUser.b(jSONObject.getString("f"));
                        }
                        if (jSONObject.has("e")) {
                            fyuseUser.a(jSONObject.getString("e"));
                        }
                        if (jSONObject.has("g")) {
                            fyuseUser.d(jSONObject.getString("g"));
                        }
                        this.j.onSuccess(fyuseUser);
                    }
                } else if (this.j != null) {
                    this.j.onSDKError(new ServerException());
                }
            } else {
                if (this.j != null) {
                    this.j.onSDKError(new ServerException());
                }
            }
        }
    }

    private void b() {
        b.a().a(FyuseSDK.getContext(), new j(this, 1, "https://api.fyu.se/1.0/auth/register?app=" + b.a().c() + "&key=" + b.a().b() + "&did=" + a.h() + "&access_token=" + a.a().m() + "&os=1", new c<String>(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(String str) {
                try {
                    this.a.b(str);
                } catch (JSONException e) {
                    if (this.a.j != null) {
                        this.a.j.onSDKError(new ServerException("Login error"));
                    }
                }
            }
        }, new b(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(s sVar) {
            }
        }) {
            final /* synthetic */ FyuseUserManager a;

            protected Map<String, String> m() {
                Map<String, String> hashMap = new HashMap();
                if (!(this.a.b == null || this.a.b.isEmpty())) {
                    hashMap.put("username", this.a.b);
                }
                if (!(this.a.c == null || this.a.c.isEmpty())) {
                    hashMap.put("name", this.a.c);
                }
                if (!(this.a.e == null || this.a.e.isEmpty())) {
                    hashMap.put("email", this.a.e);
                }
                if (!(this.a.d == null || this.a.d.isEmpty())) {
                    hashMap.put("password", this.a.d);
                }
                hashMap.put("id", a.h());
                hashMap.put("os", "android");
                hashMap.put("key2", "what");
                return hashMap;
            }
        });
    }

    private void b(String str) throws JSONException {
        if (str != null) {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("error") && jSONObject.getInt("error") > 0) {
                if (this.j != null) {
                    if (jSONObject.has("k")) {
                        switch (jSONObject.getInt("k")) {
                            case 0:
                                this.j.onUserError(new UserLoginNoAccountException(jSONObject.getString("email")));
                                break;
                            case 1:
                                this.j.onUserError(new UserLoginCredentialsException(jSONObject.getString("email")));
                                break;
                            case 11:
                                this.j.onUserError(new UserPasswordShortException());
                                break;
                            case 12:
                                this.j.onUserError(new UserPasswordShortException());
                                break;
                            case 21:
                            case 23:
                            case 24:
                                this.j.onUserError(new UserEmailInvalidException(jSONObject.getString("email")));
                                break;
                            case 22:
                                this.j.onUserError(new UserEmailExistsException(jSONObject.getString("email")));
                                break;
                            case 31:
                                this.j.onUserError(new UserShortException(jSONObject.getString("username")));
                                break;
                            case 32:
                                this.j.onUserError(new UserLongException(jSONObject.getString("username")));
                                break;
                            case 33:
                                this.j.onUserError(new UserExistsException(jSONObject.getString("username")));
                                break;
                            default:
                                if (!jSONObject.has("msg")) {
                                    this.j.onUserError(new UserAuthenticationException(jSONObject.getString("k")));
                                    break;
                                } else {
                                    this.j.onUserError(new UserAuthenticationException(jSONObject.getString("msg")));
                                    break;
                                }
                        }
                    } else if (jSONObject.has("msg")) {
                        this.j.onSDKError(new FyuseSDKException(jSONObject.getString("msg")));
                    } else {
                        this.j.onSDKError(new FyuseSDKException());
                    }
                }
            } else if (jSONObject.has("password")) {
                if (this.j != null) {
                    this.j.onUserError(new UserAuthenticationException(jSONObject.getString("password")));
                }
            } else if (jSONObject.has("success") && jSONObject.getInt("success") != 0) {
                if (jSONObject.has("a") && !"0".equals(jSONObject.getString("a"))) {
                    a.a().g(jSONObject.getString("a"));
                    if (this.j != null) {
                        FyuseUser fyuseUser = new FyuseUser();
                        if (jSONObject.has("m")) {
                            fyuseUser.c(jSONObject.getString("m"));
                        }
                        if (jSONObject.has("f")) {
                            fyuseUser.b(jSONObject.getString("f"));
                        }
                        if (jSONObject.has("e")) {
                            fyuseUser.a(jSONObject.getString("e"));
                        }
                        if (jSONObject.has("g")) {
                            fyuseUser.d(jSONObject.getString("g"));
                        }
                        if (this.f != null) {
                            e();
                        }
                        this.j.onSuccess(fyuseUser);
                    }
                } else if (this.j != null) {
                    this.j.onSDKError(new ServerException());
                }
            } else {
                if (this.j != null) {
                    this.j.onSDKError(new ServerException());
                }
            }
        }
    }

    private void c() {
        b.a().a(FyuseSDK.getContext(), new j(this, 1, "https://api.fyu.se/1.1/auth/register?app=" + b.a().c() + "&key=" + b.a().b() + "&did=" + a.h() + "&access_token=" + a.a().m() + "&os=1", new c<String>(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(String str) {
                try {
                    this.a.b(str);
                } catch (JSONException e) {
                    if (this.a.j != null) {
                        this.a.j.onSDKError(new ServerException("Login error"));
                    }
                }
            }
        }, new b(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(s sVar) {
            }
        }) {
            final /* synthetic */ FyuseUserManager a;

            protected Map<String, String> m() {
                Map<String, String> hashMap = new HashMap();
                if (!(this.a.b == null || this.a.b.isEmpty())) {
                    hashMap.put("username", this.a.b);
                }
                if (!(this.a.c == null || this.a.c.isEmpty())) {
                    hashMap.put("name", this.a.c);
                }
                if (!(this.a.e == null || this.a.e.isEmpty())) {
                    hashMap.put("email", this.a.e);
                }
                if (!(this.a.g == null || this.a.g.isEmpty())) {
                    hashMap.put("token", this.a.g);
                }
                if (!(this.a.h == null || this.a.h.isEmpty())) {
                    hashMap.put("tid", this.a.h);
                }
                switch (this.a.i) {
                    case 1:
                        hashMap.put("type", "fb");
                        break;
                    case 2:
                        hashMap.put("type", "go");
                        break;
                    case 3:
                        hashMap.put("type", "tw");
                        break;
                    case 4:
                        hashMap.put("type", "c1");
                        break;
                }
                hashMap.put("id", a.h());
                hashMap.put("os", "android");
                hashMap.put("key2", "what");
                return hashMap;
            }
        });
    }

    private void d() {
        b.a().a(FyuseSDK.getContext(), new j(this, 1, "https://api.fyu.se/1.0/auth/login?app=" + b.a().c() + "&key=" + b.a().b() + "&did=" + a.h() + "&access_token=" + a.a().m() + "&os=1", new c<String>(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(String str) {
                try {
                    this.a.b(str);
                } catch (JSONException e) {
                    if (this.a.j != null) {
                        this.a.j.onSDKError(new ServerException("Login error"));
                    }
                }
            }
        }, new b(this) {
            final /* synthetic */ FyuseUserManager a;

            {
                this.a = r1;
            }

            public void a(s sVar) {
            }
        }) {
            final /* synthetic */ FyuseUserManager a;

            protected Map<String, String> m() {
                Map<String, String> hashMap = new HashMap();
                if (!(this.a.e == null || this.a.e.isEmpty())) {
                    hashMap.put("email", this.a.e);
                }
                if (!(this.a.d == null || this.a.d.isEmpty())) {
                    hashMap.put("pass", this.a.d);
                }
                hashMap.put("id", a.h());
                hashMap.put("os", "android");
                return hashMap;
            }
        });
    }

    private void e() {
        if (this.f == null) {
            b.a().a(FyuseSDK.getContext(), new j(this, 1, "https://sdk.fyu.se/api/2.0/user/details?app=" + b.a().c() + "&key=" + b.a().b() + "&did=" + a.h() + "&access_token=" + a.a().m() + "&os=1", new c<String>(this) {
                final /* synthetic */ FyuseUserManager a;

                {
                    this.a = r1;
                }

                public void a(String str) {
                    try {
                        this.a.a(str);
                    } catch (JSONException e) {
                        if (this.a.j != null) {
                            this.a.j.onSDKError(new ServerException("Login error"));
                        }
                    }
                }
            }, new b(this) {
                final /* synthetic */ FyuseUserManager a;

                {
                    this.a = r1;
                }

                public void a(s sVar) {
                }
            }) {
                final /* synthetic */ FyuseUserManager a;

                protected Map<String, String> m() {
                    Map<String, String> hashMap = new HashMap();
                    if (!(this.a.b == null || this.a.b.isEmpty())) {
                        hashMap.put("username", this.a.b);
                    }
                    if (!(this.a.c == null || this.a.c.isEmpty())) {
                        hashMap.put("name", this.a.c);
                    }
                    if (!(this.a.e == null || this.a.e.isEmpty())) {
                        hashMap.put("email", this.a.e);
                    }
                    hashMap.put("os", "android");
                    return hashMap;
                }
            });
            return;
        }
        String str = "https://sdk.fyu.se/api/2.0/user/details?app=" + b.a().c() + "&key=" + b.a().b() + "&did=" + a.h() + "&access_token=" + a.a().m();
        c cVar = new c();
        cVar.a(this.f);
        List arrayList = new ArrayList();
        if (!(this.b == null || this.b.isEmpty())) {
            arrayList.add(new Pair("username", this.b));
        }
        if (!(this.c == null || this.c.isEmpty())) {
            arrayList.add(new Pair("name", this.c));
        }
        if (!(this.e == null || this.e.isEmpty())) {
            arrayList.add(new Pair("email", this.e));
        }
        if (!(this.d == null || this.d.isEmpty())) {
            arrayList.add(new Pair("password", this.d));
        }
        cVar.a(arrayList);
        try {
            str = (String) cVar.execute(new String[]{str, null, null}).get();
            if (str != null) {
                try {
                    a(str);
                } catch (JSONException e) {
                    if (this.j != null) {
                        this.j.onSDKError(new ConnectionException("1 Login error"));
                    }
                }
            } else if (this.j != null) {
                this.j.onSDKError(new ConnectionException("2 Login error"));
            }
        } catch (Exception e2) {
            if (this.j != null) {
                this.j.onSDKError(new ConnectionException("3 Login error"));
            }
        }
    }

    public static FyuseUserManager init() throws FyuseSDKException {
        if (FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted()) {
            throw new FyuseSDKException("Context is null");
        }
        try {
            Object obj;
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) FyuseSDK.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected()) {
                    obj = 1;
                    if (obj == null) {
                        a();
                        if (a.a().m() == null) {
                            if (!a.a().b()) {
                                throw new AuthenticationException("FyuseSDK not properly initialized");
                            } else if (a.a().c()) {
                                a.a().f();
                            }
                        }
                        return new FyuseUserManager();
                    }
                    throw new NoNetworkConnectionException();
                }
            }
            obj = null;
            if (obj == null) {
                throw new NoNetworkConnectionException();
            }
            a();
            if (a.a().m() == null) {
                if (!a.a().b()) {
                    throw new AuthenticationException("FyuseSDK not properly initialized");
                } else if (a.a().c()) {
                    a.a().f();
                }
            }
            return new FyuseUserManager();
        } catch (SecurityException e) {
            throw new NoInternetPermissionSatisfiedException(e.getMessage());
        }
    }

    public void loginThirdPartyUser() throws UserAuthenticationException {
        if (this.g == null || this.g.isEmpty()) {
            if (this.j == null) {
                throw new UserMissingException("token");
            }
            this.j.onUserError(new UserMissingException("thirdPartyToken"));
        } else if (this.i >= 1 && this.i <= 4) {
            if (this.h != null && !this.h.isEmpty()) {
                c();
            } else if (this.j == null) {
                throw new UserMissingException("UID");
            } else {
                this.j.onUserError(new UserPasswordMissingException("thirdPartyUID"));
            }
        } else if (this.j == null) {
            throw new UserMissingException("service");
        } else {
            this.j.onUserError(new UserPasswordMissingException("thirdPartyService"));
        }
    }

    public void loginUser() throws UserAuthenticationException {
        if (this.e == null || this.e.isEmpty()) {
            if (this.j == null) {
                throw new UserEmailMissingException("email");
            }
            this.j.onUserError(new UserEmailMissingException("email"));
        } else if (this.d != null && !this.d.isEmpty()) {
            d();
        } else if (this.j == null) {
            throw new UserPasswordMissingException("password");
        } else {
            this.j.onUserError(new UserPasswordMissingException("password"));
        }
    }

    @Deprecated
    public FyuseUserManager registerApp(String str, String str2) {
        return this;
    }

    public void registerUser() throws UserAuthenticationException {
        if (this.e == null || this.e.isEmpty()) {
            if (this.j == null) {
                throw new UserEmailMissingException("email");
            }
            this.j.onUserError(new UserEmailMissingException("email"));
        } else if (this.b == null || this.b.isEmpty()) {
            if (this.j == null) {
                throw new UserMissingException("username");
            }
            this.j.onUserError(new UserMissingException("username"));
        } else if (this.c == null || this.c.isEmpty()) {
            if (this.j == null) {
                throw new UserMissingException("name");
            }
            this.j.onUserError(new UserMissingException("name"));
        } else if (this.d != null && !this.d.isEmpty()) {
            b();
        } else if (this.j == null) {
            throw new UserPasswordMissingException("password");
        } else {
            this.j.onUserError(new UserPasswordMissingException("password"));
        }
    }

    public FyuseUserManager setEmail(String str) {
        this.e = str;
        return this;
    }

    public FyuseUserManager setListener(FyuseUserListener fyuseUserListener) {
        this.j = fyuseUserListener;
        return this;
    }

    public FyuseUserManager setName(String str) {
        this.c = str;
        return this;
    }

    public FyuseUserManager setPassword(String str) {
        this.d = str;
        return this;
    }

    public FyuseUserManager setProfileBitmap(Bitmap bitmap) {
        this.f = bitmap;
        return this;
    }

    public FyuseUserManager setThirdPartyService(int i) {
        this.i = i;
        return this;
    }

    public FyuseUserManager setToken(String str) {
        this.g = str;
        return this;
    }

    public FyuseUserManager setUID(String str) {
        this.h = str;
        return this;
    }

    public FyuseUserManager setUsername(String str) {
        this.b = str;
        return this;
    }

    public void updateDetails() {
        e();
    }
}
