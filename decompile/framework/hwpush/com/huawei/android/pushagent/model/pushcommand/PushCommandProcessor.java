package com.huawei.android.pushagent.model.pushcommand;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NetEventInfo;
import com.huawei.android.pushagent.datatype.pushmessage.NewDeviceRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewDeviceRegisterRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import defpackage.ae;
import defpackage.ag;
import defpackage.an;
import defpackage.ao;
import defpackage.ap;
import defpackage.au;
import defpackage.aw;
import defpackage.az;
import defpackage.bi;
import defpackage.bj;
import defpackage.bq;
import defpackage.bt;
import defpackage.bv;
import defpackage.g;
import defpackage.i;
import defpackage.m;
import defpackage.o;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONObject;

public class PushCommandProcessor extends o {
    private static final Object LOCK = new Object();
    private static List bA = new ArrayList();
    private String[] bB;
    private boolean bC = false;
    private List bD = new ArrayList();
    private ArrayList bE = new ArrayList();

    enum MSG_REACH_ERROR_CODE {
        SUCCESS,
        FAIL,
        UNSUPORT,
        BLACK_SCREEN
    }

    public PushCommandProcessor(Context context) {
        if (!bv.ct()) {
            aw.d("PushLog2841", "not support ctrlsocket v2 ");
        } else if (1 == bv.cr()) {
            aw.d("PushLog2841", "push is in socket ctrl model, only white packages app can use push");
            this.bC = true;
            this.bB = bv.cq();
        } else {
            aw.d("PushLog2841", "all apps can use push");
            this.bC = false;
            this.bB = new String[0];
        }
    }

    private void C(Context context) {
        if (-1 == au.G(context)) {
            aw.e("PushLog2841", "sendAllMessagetoServer have no net work");
        } else if (ChannelMgr.aX().hasConnection()) {
            aw.d("PushLog2841", "sendAllMessagetoServer get the client");
            ArrayList A = ao.A(context);
            for (Entry key : new bt(context, "pclient_unRegist_info_v2").getAll().entrySet()) {
                A.add(new UnRegisterReqMessage(bj.decrypter((String) key.getKey())));
            }
            if (A != null) {
                aw.d("PushLog2841", "send all client registerToken message to Sever count is " + A.size());
            }
            if (A.size() > 0) {
                Iterator it = A.iterator();
                while (it.hasNext()) {
                    try {
                        ChannelMgr.aX().a((PushMessage) it.next());
                    } catch (Throwable e) {
                        aw.d("PushLog2841", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
                    }
                }
                return;
            }
            aw.i("PushLog2841", "no more client need register and unregister");
        } else {
            aw.e("PushLog2841", "sendAllMessagetoServer have no channel or no connection");
        }
    }

    private void D(Context context) {
        long currentTimeMillis = System.currentTimeMillis();
        long av = ae.l(context).av();
        Iterator it = this.bE.iterator();
        while (it.hasNext()) {
            i iVar = (i) it.next();
            if (currentTimeMillis - iVar.o() > av) {
                int ordinal = MSG_REACH_ERROR_CODE.FAIL.ordinal();
                if (!au.l(context, iVar.n())) {
                    ordinal = MSG_REACH_ERROR_CODE.UNSUPORT.ordinal();
                }
                a(context, iVar, ordinal);
                it.remove();
            }
        }
        if (!this.bE.isEmpty()) {
            bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
        }
    }

    private byte a(Context context, byte[] bArr, byte[] bArr2, int i, PushDataRspMessage pushDataRspMessage) {
        byte b = (byte) 0;
        try {
            aw.d("PushLog2841", "enter deposeMessageBySelf");
            if (bArr2 == null) {
                aw.e("PushLog2841", "enter deposeMessageBySelf msg is null!");
                return (byte) 0;
            }
            String str = new String(bArr2, "UTF-8");
            if (TextUtils.isEmpty(str)) {
                aw.e("PushLog2841", "enter deposeMessageBySelf jsonStr is null!");
                return (byte) 0;
            }
            if (bv.ct()) {
                bv.c(2, 180);
            } else {
                au.ctrlSockets(2, 180);
            }
            try {
                JSONObject jSONObject = new JSONObject(str);
                if (jSONObject.has("app")) {
                    aw.d("PushLog2841", "jsonStr has a mapping for app");
                    try {
                        int i2 = jSONObject.getInt("app");
                        if (1 == i2) {
                            ap.i(context, str);
                            return (byte) 0;
                        }
                        aw.i("PushLog2841", "the app value is not 1! it is " + i2);
                    } catch (Throwable e) {
                        aw.d("PushLog2841", e.toString(), e);
                    }
                } else {
                    aw.i("PushLog2841", "jsonStr does not  have a mapping for app");
                }
                String str2 = "";
                if (jSONObject.has("msgContent")) {
                    JSONObject jSONObject2 = jSONObject.getJSONObject("msgContent");
                    if (jSONObject2 != null && jSONObject2.has("dispPkgName")) {
                        str2 = jSONObject2.getString("dispPkgName");
                    }
                }
                aw.i("PushLog2841", "dispkgName is " + str2);
                if (str2 == null || str2.trim().length() == 0) {
                    return (byte) 0;
                }
                if (au.p(context, str2)) {
                    bt btVar = new bt(context, "push_notify_switch");
                    if (btVar.containsKey(str2) && btVar.getBoolean(str2, false)) {
                        aw.d("PushLog2841", "closePush_Notify, dispkgName is " + str2);
                        return (byte) 6;
                    } else if ("com.huawei.android.pushagent".equals(str2) || ao.d(context, str2, String.valueOf(ActivityManager.getCurrentUser()))) {
                        aw.d("PushLog2841", "send selfShow message");
                        if (!au.q(context, str2)) {
                            return "com.huawei.android.pushagent".equals(str2) ? (byte) 8 : (byte) 9;
                        } else {
                            byte b2;
                            if ("com.huawei.android.pushagent".equals(str2) || au.q(context, "com.huawei.android.pushagent")) {
                                b2 = (byte) 0;
                            } else {
                                aw.i("PushLog2841", "PushAPK is not allowed notify");
                                b2 = (byte) 7;
                            }
                            try {
                                a(context, bArr, bArr2, i, str2);
                                return b2;
                            } catch (Exception e2) {
                                b = b2;
                                aw.d("PushLog2841", " depose failed, maybe old selfShow message");
                                return b;
                            }
                        }
                    } else {
                        aw.i("PushLog2841", str2 + " is not registed in current user");
                        return (byte) 5;
                    }
                }
                aw.i("PushLog2841", str2 + " is not installed in current user");
                return (byte) 2;
            } catch (Exception e3) {
                aw.d("PushLog2841", " depose failed, maybe old selfShow message");
                return b;
            }
        } catch (Throwable e4) {
            aw.d("PushLog2841", e4.toString(), e4);
            return b;
        }
    }

    private void a(Context context, Intent intent) {
        PushMessage pushMessage = (PushMessage) intent.getSerializableExtra("push_msg");
        if (pushMessage == null) {
            aw.i("PushLog2841", "msg is null");
            return;
        }
        switch (pushMessage.k()) {
            case (byte) -96:
                a(context, (PushDataReqMessage) pushMessage);
                au.a(context, 100);
                return;
            case (byte) -91:
                b(context, (DecoupledPushMessage) pushMessage);
                return;
            case (byte) -90:
                try {
                    ChannelMgr.aX().a(new DecoupledPushMessage((byte) -89));
                } catch (Throwable e) {
                    aw.d("PushLog2841", "send serverToAgentMsgRsp error:" + e.getMessage(), e);
                }
                an.bI().a(context, (DecoupledPushMessage) pushMessage);
                return;
            case (byte) -45:
            case (byte) -33:
                NewDeviceRegisterRspMessage newDeviceRegisterRspMessage = (NewDeviceRegisterRspMessage) pushMessage;
                if (newDeviceRegisterRspMessage.aK() == (byte) 0) {
                    aw.i("PushLog2841", "PushCommandProcessor device register success");
                    ag.a(context, new g("cloudpush_arrayOfNetEventTime", String.class, ""));
                    ChannelMgr.h(context).j(context);
                    C(context);
                    return;
                }
                aw.e("PushLog2841", "PushCommandProcessor device register fail:" + newDeviceRegisterRspMessage.aK());
                return;
            case (byte) -41:
                a(context, (UnRegisterRspMessage) pushMessage);
                return;
            case (byte) -37:
                return;
            case (byte) -35:
                a(context, (RegisterTokenRspMessage) pushMessage);
                au.a(context, 100);
                return;
            default:
                return;
        }
    }

    private static void a(Context context, Intent intent, int i) {
        boolean supportsMultipleUsers = UserManager.supportsMultipleUsers();
        aw.i("PushLog2841", "isSupportsMultipleUsers: " + supportsMultipleUsers);
        if (supportsMultipleUsers) {
            if (-1000 == i) {
                i = ActivityManager.getCurrentUser();
            }
            aw.i("PushLog2841", "userId is: " + i);
            context.sendBroadcastAsUser(intent, new UserHandle(i));
            return;
        }
        context.sendBroadcast(intent);
    }

    private void a(Context context, PushDataReqMessage pushDataReqMessage) {
        aw.d("PushLog2841", "enter rspPushMessage");
        byte[] aD = pushDataReqMessage.aD();
        String str = "";
        if (aD == null) {
            aw.e("PushLog2841", "token is null, error!");
            return;
        }
        String str2;
        String str3;
        try {
            str = new String(aD, "UTF-8");
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        }
        String str4 = "";
        byte[] aM = pushDataReqMessage.aM();
        byte[] aP = pushDataReqMessage.aP();
        int e2 = e(pushDataReqMessage.aN());
        if (aP == null || aP.length <= 0) {
            str2 = ao.z(context).bw != null ? (String) ao.z(context).bw.get(str) : str4;
        } else {
            if (aP.length == pushDataReqMessage.aO()) {
                try {
                    str3 = new String(aP, "UTF-8");
                } catch (UnsupportedEncodingException e3) {
                    aw.e("PushLog2841", "UnsupportedEncodingException occur");
                    str3 = str4;
                }
                aw.d("PushLog2841", "rspPushMessage from srv response pkgname is :" + str3);
            } else {
                str3 = str4;
            }
            str2 = str3;
        }
        String f = au.f(aM);
        aw.d("PushLog2841", "rspPushMessage token =" + bi.w(str) + " pkgname=" + str2 + " msgId=" + f);
        PushDataRspMessage pushDataRspMessage = new PushDataRspMessage(aM, (byte) 0);
        if (au.H(context).equals(str)) {
            if (l(f)) {
                aw.i("PushLog2841", "msgId duplicate, do not show it");
            } else {
                k(f);
                pushDataRspMessage = new PushDataRspMessage(aM, a(context, aD, pushDataReqMessage.aE(), e2, pushDataRspMessage));
            }
        } else if (str2 != null) {
            int parseInt;
            if (-1000 == e2) {
                str3 = au.q(str);
                aw.i("PushLog2841", "tokenUserId is: " + str3);
                try {
                    parseInt = Integer.parseInt(str3);
                } catch (NumberFormatException e4) {
                    parseInt = 0;
                }
            } else {
                parseInt = e2;
            }
            if (au.c(context, str2, parseInt)) {
                boolean z = true;
                if ("com.huawei.android.pushagent".equals(context.getPackageName()) || "android".equals(context.getPackageName())) {
                    z = new bt(context, "PushAppNotifiCfg").getBoolean(str2, true);
                }
                if (!ao.d(context, str2, String.valueOf(parseInt))) {
                    aw.i("PushLog2841", str2 + " is not registed,user id is " + parseInt);
                    a(new PushDataRspMessage(aM, (byte) 5));
                    return;
                } else if (l(f)) {
                    aw.i("PushLog2841", "msgId duplicate, do not sent it to other apps");
                } else if (z) {
                    k(f);
                    aw.i("PushLog2841", "isCtrlSocket:" + this.bC);
                    if (!this.bC) {
                        a(context, str2, aD, pushDataReqMessage.aE(), parseInt, f);
                    } else if (this.bB == null || this.bB.length <= 0) {
                        aw.i("PushLog2841", "whitePackages is empty, push message's owner is not white app, send it when screen on");
                        this.bD.add(new m(str2, aD, pushDataReqMessage.aE(), parseInt, f));
                        h(context, str, f);
                    } else {
                        Object obj = null;
                        for (Object equals : this.bB) {
                            if (str2.equals(equals)) {
                                obj = 1;
                                break;
                            }
                        }
                        if (obj != null) {
                            a(context, str2, aD, pushDataReqMessage.aE(), parseInt, f);
                        } else {
                            aw.i("PushLog2841", "push message's owner is not white app, send it when screen on");
                            this.bD.add(new m(str2, aD, pushDataReqMessage.aE(), parseInt, f));
                            h(context, str, f);
                        }
                    }
                } else {
                    aw.d("PushLog2841", "The push is closed for " + str2);
                }
            } else {
                aw.e("PushLog2841", "pkgName" + str2 + " not exist in local");
                pushDataRspMessage = new PushDataRspMessage(aM, (byte) 2);
            }
        } else {
            aw.e("PushLog2841", "pkgName is null");
            pushDataRspMessage = new PushDataRspMessage(aM, (byte) 2);
        }
        a(pushDataRspMessage);
    }

    private void a(Context context, RegisterTokenRspMessage registerTokenRspMessage) {
        if (registerTokenRspMessage == null) {
            aw.e("PushLog2841", "responseRegisterToken have a wrong parm");
            return;
        }
        String packageName = registerTokenRspMessage.getPackageName();
        String aS = registerTokenRspMessage.aS();
        aw.d("PushLog2841", "pushSrv response register token to " + packageName);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(aS)) {
            aw.e("PushLog2841", "pushSrv response registerToken a invalid message ");
            return;
        }
        bt btVar = new bt(context, "pclient_request_info");
        if (registerTokenRspMessage.aK() == (byte) 1) {
            aw.e("PushLog2841", "responseRegisterToken FAILED:" + registerTokenRspMessage.aK());
            if (!TextUtils.isEmpty(packageName)) {
                for (String str : btVar.getAll().keySet()) {
                    if (str.startsWith(packageName)) {
                        btVar.z(str);
                    }
                }
                return;
            }
            return;
        }
        String b = au.b(packageName, au.q(aS));
        if (btVar.containsKey(b)) {
            btVar.z(b);
        } else {
            aw.i("PushLog2841", "not found record in pclient_request_info after token response, so remove all similar packagenames.");
            for (String str2 : btVar.getAll().keySet()) {
                if (str2.startsWith(packageName)) {
                    btVar.z(str2);
                }
            }
        }
        if (bv.ct()) {
            bv.A(packageName);
        }
        ao.e(context, aS, b);
        if (btVar.getAll().size() == 0) {
            bq.h(context, new Intent("com.huawei.action.CONNECT_PUSHSRV_POLLINGSRV").setPackage(context.getPackageName()));
            PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV_POLLINGSRV"));
        }
        f(context, packageName, aS);
    }

    private void a(Context context, UnRegisterRspMessage unRegisterRspMessage) {
        aw.i("PushLog2841", "unregister token from pushsrv success");
        if (unRegisterRspMessage == null) {
            aw.e("PushLog2841", "responseUnregisterToken have an wrong param");
            return;
        }
        String aS = unRegisterRspMessage.aS();
        String str = "";
        if (ao.z(context).bw != null) {
            str = (String) ao.z(context).bw.get(aS);
            aw.d("PushLog2841", "packageNameWithUserId " + str);
        }
        new bt(context, "push_notify_key").z(str);
        ao.g(context, aS);
        az.t(context, aS);
        PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV_POLLINGSRV"));
    }

    private void a(Context context, i iVar, int i) {
        if (context == null || iVar == null) {
            aw.i("PushLog2841", "sendReachHiAnalytics, context=" + context + ",cachedMsg=" + iVar);
            return;
        }
        String m = iVar.m();
        long j = 0;
        if (MSG_REACH_ERROR_CODE.SUCCESS.ordinal() == i) {
            j = System.currentTimeMillis() - iVar.o();
        }
        String str = "|";
        au.a(context, 23101, new StringBuffer(iVar.l()).append("|").append(au.getVersion(context)).append("|").append(m).append("|").append(i).append("|").append(j).toString());
    }

    private static void a(Context context, String str, Intent intent, int i) {
        boolean supportsMultipleUsers = UserManager.supportsMultipleUsers();
        aw.i("PushLog2841", "isSupportsMultipleUsers: " + supportsMultipleUsers);
        if (supportsMultipleUsers) {
            if (-1000 == i) {
                String q = au.q(str);
                aw.i("PushLog2841", "tokenUserId is: " + q);
                try {
                    i = Integer.parseInt(q);
                } catch (NumberFormatException e) {
                    i = 0;
                }
            }
            if (i == 0) {
                aw.d("PushLog2841", "send msg to owner.");
                context.sendBroadcast(intent);
                return;
            }
            aw.d("PushLog2841", "send msg to user: " + i);
            context.sendBroadcastAsUser(intent, new UserHandle(i));
            return;
        }
        context.sendBroadcast(intent);
    }

    private synchronized void a(Context context, String str, Boolean bool) {
        bt btVar = new bt(context, "pushConfig");
        btVar.a("cloudpush_ConnectStatus", bool.booleanValue());
        btVar.a(str, Long.valueOf(System.currentTimeMillis()));
    }

    private void a(Context context, String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        String str3;
        Throwable e;
        Throwable th;
        if (bv.ct()) {
            bv.c(2, 180);
        } else {
            au.ctrlSockets(2, 180);
        }
        Intent intent = new Intent("com.huawei.android.push.intent.RECEIVE");
        intent.setPackage(str).putExtra("msg_data", bArr2).putExtra("device_token", bArr).putExtra("msgIdStr", bj.encrypter(str2)).setFlags(32);
        String str4 = "";
        try {
            str3 = new String(bArr, "UTF-8");
            try {
                if (this.bE.size() >= 50) {
                    this.bE.remove(0);
                }
                i iVar = new i();
                iVar.c(str2);
                iVar.d(str3.substring(16, str3.length()));
                iVar.e(str);
                iVar.a(System.currentTimeMillis());
                this.bE.add(iVar);
            } catch (IndexOutOfBoundsException e2) {
                e = e2;
                aw.d("PushLog2841", e.toString(), e);
                a(context, str3, intent, i);
                PushService.a(new Intent("com.huawei.android.push.intent.MSG_BROAD_TO_APP").putExtra("appName", str));
                aw.i("PushLog2841", "broadcast pushDataRspMessage to " + str + " over");
                bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
            } catch (Exception e3) {
                e = e3;
                aw.d("PushLog2841", e.toString(), e);
                a(context, str3, intent, i);
                PushService.a(new Intent("com.huawei.android.push.intent.MSG_BROAD_TO_APP").putExtra("appName", str));
                aw.i("PushLog2841", "broadcast pushDataRspMessage to " + str + " over");
                bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
            }
        } catch (Throwable e4) {
            th = e4;
            str3 = str4;
            e = th;
            aw.d("PushLog2841", e.toString(), e);
            a(context, str3, intent, i);
            PushService.a(new Intent("com.huawei.android.push.intent.MSG_BROAD_TO_APP").putExtra("appName", str));
            aw.i("PushLog2841", "broadcast pushDataRspMessage to " + str + " over");
            bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
        } catch (Throwable e42) {
            th = e42;
            str3 = str4;
            e = th;
            aw.d("PushLog2841", e.toString(), e);
            a(context, str3, intent, i);
            PushService.a(new Intent("com.huawei.android.push.intent.MSG_BROAD_TO_APP").putExtra("appName", str));
            aw.i("PushLog2841", "broadcast pushDataRspMessage to " + str + " over");
            bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
        }
        a(context, str3, intent, i);
        PushService.a(new Intent("com.huawei.android.push.intent.MSG_BROAD_TO_APP").putExtra("appName", str));
        aw.i("PushLog2841", "broadcast pushDataRspMessage to " + str + " over");
        bq.b(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), ae.l(context).av());
    }

    private void a(Context context, byte[] bArr, byte[] bArr2, int i, String str) {
        try {
            int currentUser = ActivityManager.getCurrentUser();
            if (au.b(context, str, currentUser) || "com.huawei.android.pushagent".equals(str.trim())) {
                aw.d("PushLog2841", "try to send selfshow msg to push client ,package " + str + " to depose selfshow msg");
                Intent intent = new Intent("com.huawei.intent.action.PUSH");
                intent.putExtra("selfshow_info", bArr2);
                intent.putExtra("selfshow_token", bArr);
                intent.setFlags(32);
                if (!au.p(context, "com.huawei.android.pushagent") || !au.r(context, "com.huawei.android.pushagent")) {
                    String b = au.b(str, String.valueOf(currentUser));
                    aw.d("PushLog2841", "packageNameWithUserId:" + b);
                    Object decrypter = bj.decrypter(new bt(context, "push_notify_key").getString(b));
                    if (TextUtils.isEmpty(decrypter)) {
                        f(context, str, ao.h(context, b));
                        intent.putExtra("extra_encrypt_data", bj.c(str, bj.decrypter(new bt(context, "push_notify_key").getString(b)).getBytes("UTF-8")));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            aw.e("PushLog2841", e.toString());
                        }
                    } else {
                        intent.putExtra("extra_encrypt_data", bj.c(str, decrypter.getBytes("UTF-8")));
                    }
                    intent.setPackage(str);
                    a(context, intent, i);
                } else if (au.q(context, "com.huawei.android.pushagent")) {
                    intent.setPackage("com.huawei.android.pushagent");
                    intent.putExtra("extra_encrypt_data", bj.c("com.huawei.android.pushagent", au.bL().getBytes("UTF-8")));
                    a(context, intent, i);
                } else {
                    intent.setPackage(str);
                    a(context, intent, i);
                }
            }
        } catch (Exception e2) {
            aw.e("PushLog2841", "deposeSelfShowMsg error:" + e2.toString());
        }
    }

    private static void a(bt btVar, int i, boolean z) {
        aw.d("PushLog2841", "enter netEventStatistics, netType is " + i + ",connected is" + z);
        String string = btVar.getString("cloudpush_arrayOfNetEventTime");
        String str = "";
        if (!TextUtils.isEmpty(string) || string.equals("null")) {
            String[] split = string.split("\\,");
            int length = split.length - 1;
            StringBuffer stringBuffer = new StringBuffer();
            if (length == 16) {
                for (int i2 = 0; i2 < 15; i2++) {
                    stringBuffer.append(split[i2 + 1] + ",");
                }
                string = stringBuffer.toString();
            } else if (length < 16) {
                string = string + ",";
            }
            btVar.f("cloudpush_arrayOfNetEventTime", string + (z ? null + "-1-" + Long.toString(System.currentTimeMillis()) : null + "-0-" + Long.toString(System.currentTimeMillis())));
        }
        string = str;
        if (z) {
        }
        btVar.f("cloudpush_arrayOfNetEventTime", string + (z ? null + "-1-" + Long.toString(System.currentTimeMillis()) : null + "-0-" + Long.toString(System.currentTimeMillis())));
    }

    private void a(PushDataRspMessage pushDataRspMessage) {
        if (pushDataRspMessage == null) {
            aw.e("PushLog2841", "rspMsg or msgId is null");
            return;
        }
        try {
            ChannelMgr.aX().a((IPushMessage) pushDataRspMessage);
            aw.i("PushLog2841", "rspPushMessage the response msg is :" + pushDataRspMessage.aC() + ",msgId:" + au.f(pushDataRspMessage.aM()) + ",flag:" + au.e(pushDataRspMessage.aR()));
        } catch (Throwable e) {
            aw.d("PushLog2841", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
        }
    }

    private void b(Context context, Intent intent) {
        bq.h(context, new Intent("com.huawei.intent.action.PUSH_OFF").setPackage(context.getPackageName()).putExtra("Remote_Package_Name", context.getPackageName()));
        String stringExtra = intent.getStringExtra("pkg_name");
        String stringExtra2 = intent.getStringExtra("userid");
        aw.d("PushLog2841", "PushCommandProcessor: get the packageName: " + stringExtra + "; userid is " + stringExtra2);
        if (TextUtils.isEmpty(stringExtra)) {
            aw.e("PushLog2841", "PushCommandProcessor: get the wrong package name from the Client!");
            return;
        }
        String str;
        int currentUser = ActivityManager.getCurrentUser();
        if (TextUtils.isEmpty(stringExtra2)) {
            stringExtra2 = String.valueOf(currentUser);
            aw.i("PushLog2841", "userid is null, use default:" + stringExtra2);
            str = stringExtra2;
        } else {
            try {
                currentUser = Integer.parseInt(stringExtra2);
                str = stringExtra2;
            } catch (NumberFormatException e) {
                str = stringExtra2;
            }
        }
        if (au.c(context, stringExtra, currentUser)) {
            String b = au.b(stringExtra, str);
            bt btVar = new bt(context, "pclient_unRegist_info_v2");
            for (Entry entry : btVar.getAll().entrySet()) {
                if (b.equals((String) entry.getValue())) {
                    aw.d("PushLog2841", b + " need to register again");
                    btVar.z((String) entry.getKey());
                    break;
                }
            }
            if (ao.d(context, stringExtra, str)) {
                aw.d("PushLog2841", "PushCommandProcessor: this package:" + stringExtra + " have already registered ");
                f(context, stringExtra, ao.h(context, b));
                return;
            }
            g(context, stringExtra, str);
            return;
        }
        aw.e("PushLog2841", "rec register toke request , but the packageName:" + stringExtra + " was not install !!");
    }

    private void b(Context context, DecoupledPushMessage decoupledPushMessage) {
        if (decoupledPushMessage == null) {
            aw.e("PushLog2841", "decoupledPushMessage is null");
            return;
        }
        try {
            JSONObject aG = decoupledPushMessage.aG();
            aw.d("PushLog2841", "parseDecoupledPushTokenMsg:" + bi.b(aG));
            if (!aG.has("cmdid")) {
                aw.d("PushLog2841", "unknown DecoupledPushMessage");
            } else if (aG.getInt("cmdid") != 221) {
                aw.w("PushLog2841", "decoupledPushMessage cmdid is not NEW_CMD_DEVICE_TOKEN_REG_RSP");
            } else {
                int i = aG.getInt("result");
                byte b = (byte) i;
                a(context, new RegisterTokenRspMessage(b, aG.getString("token"), aG.getString("packageName")));
                au.a(context, 100);
            }
        } catch (Throwable e) {
            aw.d("PushLog2841", "parseDecoupledPushTokenMsg error:" + e.getMessage(), e);
        }
    }

    private void c(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("pkg_name");
        if (TextUtils.isEmpty(stringExtra)) {
            aw.d("PushLog2841", "packagename is null, cannot deregister");
            return;
        }
        aw.d("PushLog2841", "responseClientUnRegistration: packagename = " + stringExtra);
        String stringExtra2 = intent.getStringExtra("device_token");
        if (intent.getBooleanExtra("isTokenEncrypt", false)) {
            stringExtra2 = bj.decrypter(stringExtra2);
        }
        String str = "";
        str = au.b(stringExtra, TextUtils.isEmpty(stringExtra2) ? String.valueOf(ActivityManager.getCurrentUser()) : au.q(stringExtra2));
        new bt(context, "pclient_request_info").z(str);
        if (TextUtils.isEmpty(stringExtra2)) {
            aw.d("PushLog2841", "token is null, cannot deregister");
            return;
        }
        String h = ao.h(context, str);
        if (stringExtra2.equals(h)) {
            if (bv.ct()) {
                bv.B(stringExtra);
            }
            az.m(context, h, str);
            j(context, str);
            ao.f(context, str);
            return;
        }
        aw.i("PushLog2841", "token not match, cannot deregister. token is " + bi.w(stringExtra2) + ", local token is " + bi.w(h));
    }

    private void d(Context context, Intent intent) {
        String str = "";
        Uri data = intent.getData();
        if (data != null) {
            str = data.getSchemeSpecificPart();
        }
        aw.d("PushLog2841", "responseAddPackage pkgName= " + str);
        if (!TextUtils.isEmpty(str)) {
            int currentUser = ActivityManager.getCurrentUser();
            String b = au.b(str, String.valueOf(currentUser));
            boolean d = ao.d(context, str, String.valueOf(currentUser));
            aw.d("PushLog2841", "responseAddPackage,isRegistered:" + d);
            if (!d) {
                return;
            }
            if (au.b(context, str, currentUser)) {
                g(context, str, String.valueOf(currentUser));
                return;
            }
            String h = ao.h(context, b);
            Intent intent2 = new Intent();
            intent2.putExtra("pkg_name", str);
            intent2.putExtra("device_token", h);
            c(context, intent);
        }
    }

    private int e(byte[] bArr) {
        if (bArr != null && bArr.length > 0) {
            try {
                int i = new JSONObject(new String(bArr, "UTF-8")).getInt("userType");
                aw.i("PushLog2841", "userType:" + i);
                switch (i) {
                    case 0:
                        return ActivityManager.getCurrentUser();
                    case Reporter.ACTIVITY_CREATE /*1*/:
                        return 0;
                }
            } catch (Throwable e) {
                aw.d("PushLog2841", "getMsgUserIdFromExtMsgs error:" + e.getMessage(), e);
            }
        }
        return -1000;
    }

    private void e(Context context, Intent intent) {
        String str = "";
        Uri data = intent.getData();
        if (data != null) {
            str = data.getSchemeSpecificPart();
        }
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.DATA_REMOVED", true);
        aw.d("PushLog2841", "ACTION_PACKAGE_REMOVED : isRemoveData=" + booleanExtra + " remove pkgName:" + str);
        if (booleanExtra) {
            aw.d("PushLog2841", "responseRemovePackage pkgName= " + str);
            int currentUser = ActivityManager.getCurrentUser();
            if (au.b(context, str, currentUser)) {
                aw.d("PushLog2841", "received pkgRemove action, but pkg:" + str + " is exist and have " + "com.huawei.android.push.intent.REGISTRATION" + ", register again");
                if (ao.d(context, str, String.valueOf(currentUser))) {
                    g(context, str, String.valueOf(currentUser));
                    return;
                }
                return;
            }
            String h = ao.h(context, au.b(str, String.valueOf(currentUser)));
            Intent intent2 = new Intent();
            intent2.putExtra("pkg_name", str);
            intent2.putExtra("device_token", h);
            c(context, intent2);
        }
    }

    private void f(Context context, Intent intent) {
        if (context != null && intent != null) {
            Object stringExtra = intent.getStringExtra("msgIdStr");
            if (!TextUtils.isEmpty(stringExtra)) {
                String decrypter = bj.decrypter(stringExtra);
                if (!TextUtils.isEmpty(decrypter)) {
                    aw.i("PushLog2841", "enter collectAndReportHiAnalytics, msgId is " + decrypter);
                    Iterator it = this.bE.iterator();
                    while (it.hasNext()) {
                        i iVar = (i) it.next();
                        if (decrypter.equals(iVar.l())) {
                            if (ae.l(context).g(iVar.n())) {
                                a(context, iVar, MSG_REACH_ERROR_CODE.SUCCESS.ordinal());
                                it.remove();
                            }
                            if (this.bE.isEmpty()) {
                                bq.w(context, "com.huawei.android.push.intent.MSG_RSP_TIMEOUT");
                            }
                        }
                    }
                    if (this.bE.isEmpty()) {
                        bq.w(context, "com.huawei.android.push.intent.MSG_RSP_TIMEOUT");
                    }
                }
            }
        }
    }

    private static void f(Context context, String str, String str2) {
        if (str != null && str2 != null) {
            try {
                Intent flags = new Intent("com.huawei.android.push.intent.REGISTRATION").setPackage(str).putExtra("device_token", str2.getBytes("UTF-8")).putExtra("belongId", ae.l(context).p()).setFlags(32);
                Object j = au.j(context, str, str2);
                if (!TextUtils.isEmpty(j)) {
                    flags.putExtra("extra_encrypt_key", j);
                }
                aw.i("PushLog2841", "send registerToken to:" + str);
                a(context, str2, flags, -1000);
            } catch (Throwable e) {
                aw.d("PushLog2841", e.toString(), e);
            }
        }
    }

    private void g(Context context, Intent intent) {
        boolean z = true;
        if (context == null || intent == null) {
            aw.e("PushLog2841", "enableReceiveNotifyMsg, context or intent is null");
            return;
        }
        try {
            Object decrypter = bj.decrypter(intent.getStringExtra("enalbeFlag"));
            if (TextUtils.isEmpty(decrypter)) {
                aw.i("PushLog2841", "pkgAndFlag is empty");
                return;
            }
            String[] split = decrypter.split("#");
            if (2 != split.length) {
                aw.i("PushLog2841", "pkgAndFlag is invalid");
                return;
            }
            String str = split[0];
            boolean booleanValue = Boolean.valueOf(split[1]).booleanValue();
            aw.d("PushLog2841", "pkg:" + str + ",flag:" + booleanValue);
            bt btVar = new bt(context, "push_notify_switch");
            if (booleanValue) {
                z = false;
            }
            btVar.a(str, z);
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        }
    }

    private void g(Context context, String str, String str2) {
        aw.i("PushLog2841", "begin to get token from pushSrv, packagename = " + str + ",userId is " + str2);
        String E = au.E(context);
        if (E == null) {
            aw.e("PushLog2841", "have no device,when sendRegisterToken");
            return;
        }
        IPushMessage registerTokenReqMessage;
        bt btVar = new bt(context, "pclient_request_info");
        if (TextUtils.isEmpty(str2) || str2.matches("[0]+")) {
            str2 = "";
        }
        boolean supportsMultipleUsers = UserManager.supportsMultipleUsers();
        aw.i("PushLog2841", "isSupportsMultipleUsers: " + supportsMultipleUsers);
        if (!supportsMultipleUsers || TextUtils.isEmpty(str2)) {
            btVar.f(str, "true");
            registerTokenReqMessage = new RegisterTokenReqMessage(E, au.o(context, str));
        } else {
            aw.i("PushLog2841", "begin to get token from server, userid is: " + str2);
            btVar.f(au.b(str, str2), "true");
            registerTokenReqMessage = new DecoupledPushMessage((byte) -92);
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("cmdid", -36);
                jSONObject.put("packageName", str);
                jSONObject.put("usrid", str2);
            } catch (Throwable e) {
                aw.d("PushLog2841", "create DecoupledPushMessage params error:" + e.toString(), e);
            }
            ((DecoupledPushMessage) registerTokenReqMessage).a(jSONObject);
        }
        if (ChannelMgr.aX().hasConnection()) {
            try {
                ChannelMgr.aX().a(registerTokenReqMessage);
            } catch (Throwable e2) {
                aw.d("PushLog2841", "call ChannelMgr.getPushChannel().send cause:" + e2.toString(), e2);
            }
            bq.b(context, new Intent("com.huawei.action.CONNECT_PUSHSRV_POLLINGSRV").setPackage(context.getPackageName()), ae.l(context).T() * 1000);
            return;
        }
        PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV_PUSHSRV").setPackage(context.getPackageName()));
    }

    private void h(Context context, String str, String str2) {
        aw.i("PushLog2841", "sendHiAnalytics when black screen, context=" + context + ",msgIdStr =" + str2);
        if (context != null && !TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            String str3 = "";
            try {
                str3 = str.substring(16, str.length());
            } catch (IndexOutOfBoundsException e) {
                aw.e("PushLog2841", e.toString());
            }
            String str4 = "|";
            au.a(context, 23101, new StringBuffer(str2).append("|").append(au.getVersion(context)).append("|").append(str3).append("|").append(MSG_REACH_ERROR_CODE.BLACK_SCREEN.ordinal()).append("|").append(0).toString());
        }
    }

    private void j(Context context, String str) {
        aw.d("PushLog2841", str + " will be unregister.");
        Object h = ao.h(context, str);
        if (!TextUtils.isEmpty(h)) {
            try {
                ChannelMgr.aX().a(new UnRegisterReqMessage(h));
            } catch (Throwable e) {
                aw.d("PushLog2841", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
            }
        }
    }

    private NewDeviceRegisterReqMessage k(Context context, String str) {
        bt btVar = new bt(context, "pushConfig");
        int parseInt = Integer.parseInt(au.getVersion(context));
        long j = btVar.getLong("cloudpush_off");
        long j2 = btVar.getLong("cloudpush_on");
        long currentTimeMillis = System.currentTimeMillis();
        int i = 0;
        try {
            int i2;
            String string = btVar.getString("cloudpush_arrayOfNetEventTime");
            if (!TextUtils.isEmpty(string) || string.equals("null")) {
                i = string.split("\\,").length;
            }
            aw.d("PushLog2841", "mDeviceTokenMgr.tokenMap.size:" + ao.z(context).bw.size());
            if (ao.z(context).bw.size() == 0) {
                i++;
            }
            if (i > 16) {
                i = 16;
            }
            aw.d("PushLog2841", "netEventAccount is: " + i);
            NetEventInfo[] netEventInfoArr = new NetEventInfo[i];
            if (!TextUtils.isEmpty(string) || string.equals("null")) {
                String[] split = string.split("\\,");
                for (i2 = 0; i2 < split.length; i2++) {
                    String[] split2 = split[i2].split("\\-");
                    netEventInfoArr[i2] = new NetEventInfo();
                    netEventInfoArr[i2].b((byte) Integer.parseInt(split2[0]));
                    netEventInfoArr[i2].c((byte) Integer.parseInt(split2[1]));
                    netEventInfoArr[i2].f(Long.parseLong(split2[2]));
                }
            }
            if (ao.z(context).bw.size() == 0) {
                i2 = i - 1;
                aw.d("PushLog2841", "syncPos is: " + i2);
                if (netEventInfoArr[i2] == null) {
                    netEventInfoArr[i2] = new NetEventInfo();
                }
                netEventInfoArr[i2].b((byte) -1);
                netEventInfoArr[i2].c((byte) 0);
                netEventInfoArr[i2].f(System.currentTimeMillis());
            }
            return new NewDeviceRegisterReqMessage(str, (byte) au.G(context), parseInt, j, j2, currentTimeMillis, i, netEventInfoArr);
        } catch (NumberFormatException e) {
            return new NewDeviceRegisterReqMessage(str, (byte) au.G(context), parseInt, j, j2, currentTimeMillis, 0, null);
        } catch (Exception e2) {
            return new NewDeviceRegisterReqMessage(str, (byte) au.G(context), parseInt, j, j2, currentTimeMillis, 0, null);
        }
    }

    private void k(String str) {
        synchronized (LOCK) {
            if (bA.size() >= 10) {
                bA.remove(0);
            }
            bA.add(str);
        }
    }

    private boolean l(String str) {
        boolean contains;
        synchronized (LOCK) {
            contains = bA.contains(str);
        }
        return contains;
    }

    public void onReceive(Context context, Intent intent) {
        aw.d("PushLog2841", "enter PushCommandProcessor:onReceive(intent:" + intent + " context:" + context);
        String action = intent.getAction();
        if ("com.huawei.push.action.NET_CHANGED".equals(action)) {
            bt btVar = new bt(context, "pushConfig");
            int intExtra = intent.getIntExtra("networkType", -1);
            boolean booleanExtra = intent.getBooleanExtra("networkState", false);
            if (booleanExtra) {
                btVar.a("cloudpush_net_on", Long.valueOf(System.currentTimeMillis()));
            } else {
                btVar.a("cloudpush_net_off", Long.valueOf(System.currentTimeMillis()));
            }
            a(btVar, intExtra, booleanExtra);
        } else if ("com.huawei.android.push.intent.CONNECTED".equals(action)) {
            a(context, "cloudpush_on", Boolean.valueOf(true));
            action = au.E(context);
            if (action == null) {
                aw.e("PushLog2841", "cannot get imei when receviced ACTION_CONNECTED");
                return;
            }
            try {
                ChannelMgr.aX().a(k(context, action));
            } catch (Throwable e) {
                aw.d("PushLog2841", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
            }
        } else if ("com.huawei.android.push.intent.CHANNEL_CLOSED".equals(action)) {
            a(context, "cloudpush_off", Boolean.valueOf(false));
        } else if ("com.huawei.android.push.intent.MSG_RECEIVED".equals(action)) {
            a(context, intent);
        } else if ("com.huawei.android.push.intent.REGISTER".equals(action)) {
            b(context, intent);
        } else if ("com.huawei.android.push.intent.REGISTER_SPECIAL".equals(action)) {
            ChannelMgr.aX().b(true);
            b(context, intent);
        } else if ("com.huawei.android.push.intent.DEREGISTER".equals(action)) {
            c(context, intent);
        } else if ("com.huawei.intent.action.SELF_SHOW_FLAG".equals(action)) {
            g(context, intent);
        } else if ("com.huawei.android.push.intent.MSG_RESPONSE".equals(action)) {
            f(context, intent);
        } else if ("com.huawei.android.push.intent.MSG_RSP_TIMEOUT".equals(action)) {
            D(context);
        } else if ("com.huawei.android.push.intent.RESET_BASTET".equals(action)) {
            aw.i("PushLog2841", "reset bastet alarm reach, and reconnect pushserver");
            PushBastet.ac(context).cb();
            PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(context.getPackageName()));
        } else if ("com.huawei.android.push.intent.RESPONSE_FAIL".equals(action)) {
            aw.i("PushLog2841", "srv response fail, close channel and set alarm to reconnect!");
            try {
                ChannelMgr.g(context).aU();
                ChannelMgr.g(context).g(ReconnectMgr.s(context).v(context));
            } catch (Throwable e2) {
                aw.d("PushLog2841", "call channel.close cause exceptino:" + e2.toString(), e2);
            }
        } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
            d(context, intent);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
            e(context, intent);
        } else if ("android.ctrlsocket.all.allowed".equals(action)) {
            this.bC = false;
            this.bB = new String[0];
            aw.i("PushLog2841", "all packages allow to use push, send cached messages to apps");
            for (m mVar : this.bD) {
                if (!(mVar.aD() == null || mVar.aE() == null)) {
                    a(context, mVar.getPackageName(), mVar.aD(), mVar.aE(), mVar.aF(), mVar.l());
                }
            }
            this.bD.clear();
        } else if ("android.scroff.ctrlsocket.status".equals(action)) {
            boolean booleanExtra2 = intent.getBooleanExtra("ctrl_socket_status", false);
            this.bC = booleanExtra2;
            if (booleanExtra2) {
                Object stringExtra = intent.getStringExtra("ctrl_socket_list");
                aw.i("PushLog2841", "only whitepackages can use push:" + stringExtra);
                if (!TextUtils.isEmpty(stringExtra)) {
                    this.bB = stringExtra.split("\t");
                    return;
                }
                return;
            }
            this.bB = new String[0];
            aw.i("PushLog2841", "not support push in sleep model");
        }
    }
}
