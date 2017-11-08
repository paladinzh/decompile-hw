package defpackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatReqMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.bd.Reporter;
import java.net.Socket;
import java.util.Set;

/* renamed from: y */
public class y extends q {
    private boolean as = false;
    private long at = 7200000;
    private long au = this.at;
    private long av = this.at;
    private int aw = 0;
    private String ax = "";
    private String ay = "";
    private String az = null;

    public y(Context context) {
        super(context);
    }

    private void a(k kVar, String str) {
        Object obj = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                Object obj2;
                Set<String> keySet = kVar.aj().keySet();
                if (keySet != null && keySet.size() > 0) {
                    for (String str2 : keySet) {
                        if (str2.contains(str)) {
                            String str3 = (String) kVar.aj().get(str2);
                            aw.d("PushLog2841", "apnName is:" + str2 + ",apnHeartBeat is:" + str3);
                            String[] split = str3.split("_");
                            this.au = Long.parseLong(split[0]) * 1000;
                            this.av = Long.parseLong(split[1]) * 1000;
                            obj2 = 1;
                            break;
                        }
                    }
                }
                obj2 = null;
                obj = obj2;
            } catch (Throwable e) {
                aw.d("PushLog2841", e.toString(), e);
            }
        }
        if (obj == null) {
            this.au = kVar.w() * 1000;
            this.av = kVar.x() * 1000;
        }
        aw.d("PushLog2841", "after all, minHeartBeat is :" + this.au + ",maxHeartBeat is:" + this.av);
    }

    private String bn() {
        String str = "";
        try {
            if (ChannelMgr.aX() != null) {
                Socket socket = ChannelMgr.aX().getSocket();
                if (socket != null) {
                    str = socket.getLocalAddress().getHostAddress();
                }
            }
        } catch (Exception e) {
            aw.d("PushLog2841", e.toString());
        }
        return str == null ? "" : str;
    }

    private Long bo() {
        String a = ag.a(this.context, "cloudpush_fixHeatBeat", "");
        try {
            long parseLong = 1000 * Long.parseLong(a.trim());
            aw.d("PushLog2841", "get heart beat from config, value:" + parseLong + " so neednot ajust");
            return Long.valueOf(parseLong);
        } catch (NumberFormatException e) {
            if ((2 == this.batteryStatus && 5 != this.batteryStatus) || 1 != au.G(this.context)) {
                return null;
            }
            aw.d("PushLog2841", "in wifi and in charging, cannot ajust heartBeat");
            return Long.valueOf(60000);
        } catch (Throwable e2) {
            aw.d("PushLog2841", "get cloudpush_fixHeatBeat:" + a + " cause:" + e2.toString(), e2);
            if (2 == this.batteryStatus) {
            }
            aw.d("PushLog2841", "in wifi and in charging, cannot ajust heartBeat");
            return Long.valueOf(60000);
        }
    }

    private boolean bq() {
        int G = au.G(this.context);
        String F = au.F(this.context);
        String I = au.I(this.context);
        if (1 == G) {
            I = "wifi";
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("HasFindHeartBeat_" + F + "_" + G + "_" + I, Boolean.valueOf(this.as));
        contentValues.put("HearBeatInterval_" + F + "_" + G + "_" + I, Long.valueOf(this.at));
        contentValues.put("ClientIP_" + F + "_" + G, this.az);
        if (this.as) {
            I = au.a(System.currentTimeMillis() + ae.l(this.context).as(), "yyyy-MM-dd HH:mm:ss SSS");
            aw.d("PushLog2841", "when find best heart beat,save the valid end time " + I + " to xml.");
            contentValues.put("HeartBeatValid", I);
        }
        return new bt(this.context, be()).a(contentValues);
    }

    public String be() {
        return "PushHearBeat";
    }

    public /* synthetic */ q bh() {
        return bp();
    }

    public void bi() {
        try {
            long e = e(false);
            if (this.ag.bT()) {
                aw.d("PushLog2841", "bastet started, do not need to check heartbeat timeout");
                e = this.ag.cd();
            } else {
                bq.b(PushService.c().getContext(), new Intent("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT").putExtra("timer_reason", "timeOutWaitPushSrvRsp").putExtra("connect_mode", ConnectEntityMode.ConnectEntity_Push.ordinal()).setPackage(this.context.getPackageName()), ae.l(this.context).ae());
            }
            h(System.currentTimeMillis());
            IPushMessage newHeartBeatReqMessage = new NewHeartBeatReqMessage();
            newHeartBeatReqMessage.d((byte) ((int) Math.ceil((((double) e) * 1.0d) / 60000.0d)));
            ChannelMgr.aX().a(newHeartBeatReqMessage);
        } catch (Throwable e2) {
            aw.d("PushLog2841", "call pushChannel.send cause Exception:" + e2.toString(), e2);
        }
    }

    protected boolean bj() {
        int G = au.G(this.context);
        String F = au.F(this.context);
        switch (G) {
            case 0:
                return (G == this.aw && F.equals(this.ax) && au.I(this.context).equals(this.ay)) ? false : true;
            case Reporter.ACTIVITY_CREATE /*1*/:
                return (G == this.aw && F.equals(this.ax) && bn().equals(this.az)) ? false : true;
            default:
                aw.d("PushLog2841", "isEnvChange:netType:" + G + false);
                return false;
        }
    }

    public y bp() {
        try {
            if (ChannelMgr.aX() == null) {
                aw.d("PushLog2841", "system is in start, wait net for heartBeat");
                return null;
            }
            String asString;
            this.az = bn();
            ContentValues co = new bt(this.context, be()).co();
            if (co != null) {
                asString = co.getAsString("HeartBeatValid");
                aw.d("PushLog2841", "hear beat valid from xml is " + asString);
                if (!TextUtils.isEmpty(asString) && (System.currentTimeMillis() >= au.n(asString) || System.currentTimeMillis() + ae.l(this.context).as() < au.n(asString))) {
                    PushService.a(new Intent("com.huawei.android.push.intent.HEARTBEAT_VALID_ARRIVED"));
                }
            } else {
                aw.d("PushLog2841", "PushHearBeat preferences is null");
            }
            this.aw = au.G(this.context);
            this.ax = au.F(this.context);
            k l = ae.l(this.context);
            this.au = l.w() * 1000;
            this.av = l.x() * 1000;
            this.as = false;
            aw.d("PushLog2841", "in loadHeartBeat netType:" + this.aw + " mccMnc:" + this.ax);
            ContentValues co2 = new bt(this.context, be()).co();
            switch (this.aw) {
                case -1:
                    this.at = l.D() * 1000;
                    return this;
                case 0:
                    this.ay = au.I(this.context);
                    aw.d("PushLog2841", "in loadHeartBeat apnName:" + this.ay);
                    a(l, this.ay);
                    break;
                case Reporter.ACTIVITY_CREATE /*1*/:
                    this.au = l.u() * 1000;
                    this.av = l.v() * 1000;
                    this.ay = "wifi";
                    this.at = this.au;
                    if (co2 != null) {
                        asString = co2.getAsString("ClientIP_" + this.ax + "_" + this.aw);
                        if (this.az == null || !this.az.equals(asString)) {
                            aw.d("PushLog2841", "curIP:" + this.az + " oldIP:" + asString + ", there are diff, so need find heartBeat again");
                            return this;
                        }
                    }
                    break;
                default:
                    aw.e("PushLog2841", "unKnow net type");
                    return this;
            }
            this.at = this.au;
            if (co2 == null) {
                return this;
            }
            if (co2.containsKey("HasFindHeartBeat_" + this.ax + "_" + this.aw + "_" + this.ay) && co2.containsKey("HearBeatInterval_" + this.ax + "_" + this.aw + "_" + this.ay)) {
                this.as = co2.getAsBoolean("HasFindHeartBeat_" + this.ax + "_" + this.aw + "_" + this.ay).booleanValue();
                Integer asInteger = co2.getAsInteger("HearBeatInterval_" + this.ax + "_" + this.aw + "_" + this.ay);
                int intValue = asInteger != null ? asInteger.intValue() : 0;
                if (((long) intValue) < 180000) {
                    return this;
                }
                this.at = (long) intValue;
                return this;
            }
            aw.d("PushLog2841", "have no this heartbeat config, use default");
            return this;
        } catch (Throwable e) {
            aw.d("PushLog2841", "call loadHeartBeat cause:" + e.toString(), e);
            return this;
        }
    }

    public long e(boolean z) {
        if (-1 == au.G(this.context)) {
            aw.i("PushLog2841", "no network, use no network heartbeat");
            return ae.l(this.context).D() * 1000;
        }
        Long bo = bo();
        if (bo != null) {
            return bo.longValue();
        }
        if (bj()) {
            bp();
        }
        long j = this.at;
        if (this.as) {
            return j;
        }
        j = z ? this.at : this.at + 30000;
        return j <= this.au ? this.au : j >= this.av ? this.av : j;
    }

    public void f(boolean z) {
        aw.d("PushLog2841", "enter adjustHeartBeat:(findHeartBeat:" + this.as + " RspTimeOut:" + z + " beatInterval:" + this.at + " range:[" + this.au + "," + this.av + "]," + "isHearBeatTimeReq:" + this.af + " batteryStatus:" + this.batteryStatus + ")");
        if (bo() != null || this.as) {
            return;
        }
        if (this.af) {
            d(false);
            this.at = e(z);
            if (z || this.at <= this.au || this.at >= this.av) {
                this.as = true;
                aw.i("PushLog2841", "after all the best heartBeat Interval:" + this.at + "ms");
            } else {
                aw.d("PushLog2841", "set current heartBeatInterval " + this.at + "ms");
            }
            bq();
            return;
        }
        aw.d("PushLog2841", "It is not hearBeatTimeReq");
    }

    public boolean i(long j) {
        return true;
    }

    public String toString() {
        String str = "=";
        String str2 = " ";
        return new StringBuffer().append("HasFindHeartBeat").append(str).append(this.as).append(str2).append("HearBeatInterval").append(str).append(this.at).append(str2).append("minHeartBeat").append(str).append(this.au).append(str2).append("maxHeartBeat").append(str).append(this.av).toString();
    }
}
