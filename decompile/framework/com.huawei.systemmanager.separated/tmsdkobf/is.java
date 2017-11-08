package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.android.internal.telephony.ITelephony;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.IncomingCallFilter;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.IContactDao;
import tmsdk.common.module.aresengine.ILastCallLogDao;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class is extends DataInterceptorBuilder<CallLogEntity> {
    public static Long sA = Long.valueOf(0);
    public static long sy = 0;
    public static String sz = null;
    private Context mContext;

    /* compiled from: Unknown */
    private static class a {
        static is sB = new is();
    }

    /* compiled from: Unknown */
    private static final class b extends IncomingCallFilter {
        private iq sC = new iq();
        private AresEngineManager sD = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class));

        b(Context context) {
            this.sC.b(64, 1, 2, 4, 8, 16, 32);
            this.sC.a(64, aV(64));
            this.sC.a(1, aV(1));
            this.sC.a(2, aV(2));
            this.sC.a(4, aV(4));
            this.sC.a(8, aV(8));
            this.sC.a(16, aV(16));
            this.sC.a(32, aV(32));
        }

        private a aV(final int i) {
            return new a(this) {
                final /* synthetic */ b sF;

                boolean bY() {
                    IContactDao iContactDao = null;
                    if (bU() != 0 && bU() != 1) {
                        return false;
                    }
                    switch (i) {
                        case 1:
                            iContactDao = this.sF.sD.getAresEngineFactor().getPrivateListDao();
                            break;
                        case 2:
                            iContactDao = this.sF.sD.getAresEngineFactor().getWhiteListDao();
                            break;
                        case 4:
                            iContactDao = this.sF.sD.getAresEngineFactor().getBlackListDao();
                            break;
                        case 8:
                            iContactDao = this.sF.sD.getAresEngineFactor().getSysDao();
                            break;
                        case 16:
                            iContactDao = this.sF.sD.getAresEngineFactor().getLastCallLogDao();
                            break;
                    }
                    return i != 64 ? i != 32 ? !(iContactDao instanceof IContactDao) ? !(iContactDao instanceof ILastCallLogDao) ? !(iContactDao instanceof AbsSysDao) ? false : ((AbsSysDao) iContactDao).contains(bT().phonenum) : ((ILastCallLogDao) iContactDao).contains(bT().phonenum) : iContactDao.contains(bT().phonenum, 0) : true : TextUtils.isEmpty(bT().phonenum);
                }

                void bZ() {
                    int i = 0;
                    FilterResult filterResult = new FilterResult();
                    filterResult.mData = bT();
                    filterResult.mParams = bV();
                    filterResult.mState = bU();
                    filterResult.mFilterfiled = bW();
                    if (bU() != 0 && bU() == 1) {
                        ITelephony defaultTelephony;
                        boolean endCall;
                        long currentTimeMillis;
                        filterResult.isBlocked = true;
                        CallLogEntity callLogEntity = (CallLogEntity) filterResult.mData;
                        qz qzVar = jq.uh;
                        if (qzVar != null) {
                            if (callLogEntity.fromCard == null || callLogEntity.fromCard.equals(qzVar.cA(0))) {
                                defaultTelephony = DualSimTelephonyManager.getDefaultTelephony();
                                if (defaultTelephony != null) {
                                    endCall = defaultTelephony.endCall();
                                    d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                    if (!endCall) {
                                        endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                        d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                    }
                                    if (!endCall) {
                                        currentTimeMillis = System.currentTimeMillis();
                                        d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                        d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                        d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                        if (is.sy > 0) {
                                        }
                                        if (!(is.sy > 0)) {
                                            if (currentTimeMillis <= is.sy) {
                                                i = 1;
                                            }
                                            if (i == 0) {
                                            }
                                        }
                                    }
                                }
                                endCall = false;
                                d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                if (endCall) {
                                    endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                    d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                }
                                if (endCall) {
                                    currentTimeMillis = System.currentTimeMillis();
                                    d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                    d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                    d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                    if (is.sy > 0) {
                                    }
                                    if (is.sy > 0) {
                                        if (currentTimeMillis <= is.sy) {
                                            i = 1;
                                        }
                                        if (i == 0) {
                                        }
                                    }
                                }
                            } else if (callLogEntity.fromCard.equals(qzVar.cA(1))) {
                                defaultTelephony = DualSimTelephonyManager.getSecondTelephony();
                                if (defaultTelephony != null) {
                                    try {
                                        endCall = defaultTelephony.endCall();
                                    } catch (Throwable e) {
                                        d.a("IncomingCallInterceptorBuilder", "endCall", e);
                                    }
                                    d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                    if (endCall) {
                                        endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                        d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                    }
                                    if (endCall) {
                                        currentTimeMillis = System.currentTimeMillis();
                                        d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                        d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                        d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                        if (is.sy > 0) {
                                            if (currentTimeMillis <= is.sy) {
                                                i = 1;
                                            }
                                            if (i == 0) {
                                            }
                                        }
                                    }
                                }
                                endCall = false;
                                d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                                if (endCall) {
                                    endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                    d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                                }
                                if (endCall) {
                                    currentTimeMillis = System.currentTimeMillis();
                                    d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                    d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                    d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                    if (is.sy > 0) {
                                    }
                                    if (is.sy > 0) {
                                        if (currentTimeMillis <= is.sy) {
                                            i = 1;
                                        }
                                        if (i == 0) {
                                        }
                                    }
                                }
                            }
                        }
                        defaultTelephony = null;
                        if (defaultTelephony != null) {
                            endCall = defaultTelephony.endCall();
                            d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                            if (endCall) {
                                endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                                d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                            }
                            if (endCall) {
                                currentTimeMillis = System.currentTimeMillis();
                                d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                                d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                                d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                                if (is.sy > 0) {
                                }
                                if (is.sy > 0) {
                                    if (currentTimeMillis <= is.sy) {
                                        i = 1;
                                    }
                                    if (i == 0) {
                                    }
                                }
                            }
                        }
                        endCall = false;
                        d.d("IncomingCallInterceptorBuilder", "endCall1 " + endCall);
                        if (endCall) {
                            endCall = this.sF.sD.getAresEngineFactor().getPhoneDeviceController().hangup();
                            d.d("IncomingCallInterceptorBuilder", "endCall2 " + endCall);
                        }
                        if (endCall) {
                            currentTimeMillis = System.currentTimeMillis();
                            d.d("IncomingCallInterceptorBuilder", "now-lastCallEndTime" + (currentTimeMillis - is.sy));
                            d.d("IncomingCallInterceptorBuilder", "now" + currentTimeMillis);
                            d.d("IncomingCallInterceptorBuilder", "lastCallEndTime" + is.sy);
                            if (is.sy > 0) {
                            }
                            if (is.sy > 0) {
                                if (currentTimeMillis <= is.sy) {
                                    i = 1;
                                }
                                if (i == 0) {
                                }
                            }
                        }
                    }
                    a(filterResult);
                }
            };
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.sC.a(callLogEntity, getConfig(), objArr);
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(1, 0);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 0);
            filterConfig.set(64, 0);
            return filterConfig;
        }
    }

    /* compiled from: Unknown */
    private static final class c extends DataMonitor<CallLogEntity> {
        private tmsdkobf.ja.b sG = new tmsdkobf.ja.b(this) {
            final /* synthetic */ c sH;

            {
                this.sH = r1;
            }

            public void bx(String str) {
            }

            public void by(String str) {
                is.sy = System.currentTimeMillis();
            }

            public void bz(String str) {
                is.sy = 0;
            }

            public void i(String str, String str2) {
                is.sy = 0;
                TelephonyEntity callLogEntity = new CallLogEntity();
                callLogEntity.phonenum = str;
                callLogEntity.type = 1;
                callLogEntity.date = System.currentTimeMillis();
                callLogEntity.fromCard = str2;
                this.sH.notifyDataReached(callLogEntity, new Object[0]);
            }
        };

        public c(Context context) {
            ja.ce().a(this.sG);
        }

        protected void finalize() throws Throwable {
            ja.ce().b(this.sG);
            super.finalize();
        }
    }

    private is() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    public static is cb() {
        return a.sB;
    }

    public DataFilter<CallLogEntity> getDataFilter() {
        return new b(this.mContext);
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<CallLogEntity> getDataMonitor() {
        return new c(this.mContext);
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_INCOMING_CALL;
    }
}
