package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.bg.module.aresengine.AresEngineManager;
import tmsdk.bg.module.aresengine.DataFilter;
import tmsdk.bg.module.aresengine.DataHandler;
import tmsdk.bg.module.aresengine.DataInterceptorBuilder;
import tmsdk.bg.module.aresengine.DataMonitor;
import tmsdk.bg.module.aresengine.IShortCallChecker;
import tmsdk.bg.module.aresengine.SystemCallLogFilter;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public final class jd extends DataInterceptorBuilder<CallLogEntity> {
    private Context mContext;
    private b ty;
    private c tz;

    /* compiled from: Unknown */
    private static class a {
        static jd tA = new jd();
    }

    /* compiled from: Unknown */
    public static final class b extends DataMonitor<CallLogEntity> {
        private static final boolean tD = Build.BRAND.contains("Xiaomi");
        private static CallLogEntity tE;
        private static long tF = 0;
        private Context mContext;
        private ContentObserver tB;
        private BroadcastReceiver tC;
        private final long tG = 10000;
        private final ConcurrentLinkedQueue<String> tH = new ConcurrentLinkedQueue();
        private final ConcurrentLinkedQueue<String> tI = new ConcurrentLinkedQueue();
        private PhoneStateListener tJ;

        public b(Context context) {
            this.mContext = context;
            register();
        }

        private void a(ContentObserver contentObserver, CallLogEntity callLogEntity, ConcurrentLinkedQueue<String> concurrentLinkedQueue) {
            d.d("MMM", "recoreds.size: " + concurrentLinkedQueue.size() + " lastcalllog.phonenum:" + callLogEntity.phonenum);
            if (!concurrentLinkedQueue.isEmpty() && concurrentLinkedQueue.contains(callLogEntity.phonenum)) {
                d.d("MMM", "match =" + callLogEntity.phonenum);
                long currentTimeMillis = System.currentTimeMillis();
                callLogEntity.phonenum = PhoneNumberUtils.stripSeparators(callLogEntity.phonenum);
                notifyDataReached(callLogEntity, Long.valueOf(currentTimeMillis));
                concurrentLinkedQueue.clear();
                d.d("MMM", "clear ");
            }
        }

        private void register() {
            this.tC = new jj(this) {
                final /* synthetic */ b tK;

                {
                    this.tK = r1;
                }

                private String c(Intent intent) {
                    String stringExtra = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
                    return stringExtra == null ? getResultData() : stringExtra;
                }

                private String d(Intent intent) {
                    String stringExtra = intent.getStringExtra("incoming_number");
                    if (stringExtra == null) {
                        stringExtra = getResultData();
                    }
                    return PhoneNumberUtils.stripSeparators(stringExtra);
                }

                public void doOnRecv(Context context, Intent intent) {
                    Object c;
                    ConcurrentLinkedQueue a;
                    if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                        c = c(intent);
                        a = this.tK.tI;
                        if (c == null) {
                            c = "null";
                        }
                        a.add(c);
                    } else if (jy.a(context, intent) == 1 && !b.tD) {
                        c = d(intent);
                        a = this.tK.tH;
                        if (c == null) {
                            c = "null";
                        }
                        a.add(c);
                    }
                }
            };
            jy.a(this.mContext, this.tC);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
            intentFilter.setPriority(Integer.MAX_VALUE);
            intentFilter.addCategory("android.intent.category.DEFAULT");
            this.mContext.registerReceiver(this.tC, intentFilter);
            if (tD) {
                this.tJ = new PhoneStateListener(this) {
                    final /* synthetic */ b tK;

                    {
                        this.tK = r1;
                    }

                    public void onCallStateChanged(int i, String str) {
                        if (i == 1) {
                            ConcurrentLinkedQueue b = this.tK.tH;
                            if (TextUtils.isEmpty(str)) {
                                str = "null";
                            }
                            b.add(str);
                        }
                    }
                };
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.tJ, 32);
                instance.listenPhonesState(1, this.tJ, 32);
            }
            final Handler handler = new Handler();
            this.tB = new ContentObserver(this, handler) {
                final /* synthetic */ b tK;

                public synchronized void onChange(boolean z) {
                    super.onChange(z);
                    final AbsSysDao sysDao = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao();
                    final CallLogEntity lastCallLog = sysDao.getLastCallLog();
                    if (lastCallLog != null) {
                        handler.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass3 tO;

                            /* JADX WARNING: inconsistent code. */
                            /* Code decompiled incorrectly, please refer to instructions dump. */
                            public void run() {
                                boolean z = false;
                                if (lastCallLog.type != 2) {
                                    long currentTimeMillis = System.currentTimeMillis();
                                    if (b.tE != null) {
                                        if (!(currentTimeMillis - b.tF >= 10000)) {
                                            if (TextUtils.isEmpty(b.tE.phonenum)) {
                                                if (!"null".endsWith(lastCallLog.phonenum)) {
                                                }
                                            }
                                            z = true;
                                        }
                                    }
                                    d.d("SystemCallLogInterceptorBuilder", "needDel" + z);
                                    if (z) {
                                        sysDao.remove(lastCallLog);
                                        b.tE = null;
                                        b.tF = 0;
                                        this.tO.tK.tH.clear();
                                    } else {
                                        this.tO.tK.a(this.tO.tK.tB, lastCallLog, this.tO.tK.tH);
                                    }
                                    this.tO.tK.tI.clear();
                                    return;
                                }
                                this.tO.tK.a(this.tO.tK.tB, lastCallLog, this.tO.tK.tI);
                                this.tO.tK.tH.clear();
                            }
                        });
                    }
                }
            };
            this.mContext.getContentResolver().registerContentObserver(CallLog.CONTENT_URI, true, this.tB);
        }

        private void unregister() {
            this.mContext.getContentResolver().unregisterContentObserver(this.tB);
            if (this.tJ != null) {
                DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
                instance.listenPhonesState(0, this.tJ, 0);
                instance.listenPhonesState(1, this.tJ, 0);
            }
            this.tB = null;
            this.mContext.unregisterReceiver(this.tC);
            this.tC = null;
        }

        protected void finalize() throws Throwable {
            unregister();
            super.finalize();
        }
    }

    /* compiled from: Unknown */
    private static final class c extends SystemCallLogFilter {
        private Context mContext;
        private iq sC;
        private AresEngineManager sD = ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class));
        private IShortCallChecker tP;
        private boolean tQ;

        public c(Context context) {
            this.mContext = context;
            this.tQ = ck();
            this.sC = new iq();
            this.sC.b(512, 1, 2, 4, 8, 16, 32, 128, 64, 256);
            this.sC.a(512, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    return (bU() == 0 || bU() == 1) ? l.dn(bT().phonenum) : false;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(1, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    return bU() == 2 && this.tR.sD.getAresEngineFactor().getPrivateListDao().contains(((CallLogEntity) bT()).phonenum, 0);
                }

                void bZ() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    if (callLogEntity.type == 3) {
                        callLogEntity.duration = ((Long) bV()[0]).longValue() - callLogEntity.date;
                    }
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getPrivateCallLogDao(), true, false);
                }
            });
            this.sC.a(2, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getWhiteListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(4, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getBlackListDao().contains(callLogEntity.phonenum, 0)) ? false : true;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(8, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getSysDao().contains(callLogEntity.phonenum)) ? false : true;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(16, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    return (bU() == 3 || callLogEntity.type == 2 || !this.tR.sD.getAresEngineFactor().getLastCallLogDao().contains(callLogEntity.phonenum)) ? false : true;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(32, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    return (((CallLogEntity) bT()).type == 2 || bU() == 3) ? false : true;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), bU() == 1, true);
                }
            });
            this.sC.a(64, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    int i = 1;
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    String str = callLogEntity.phonenum;
                    if (str == null || str.length() <= 2) {
                        return false;
                    }
                    int i2 = (this.tR.tQ || callLogEntity.type != 1) ? 0 : 1;
                    if (callLogEntity.duration > 5) {
                        int i3 = 1;
                    } else {
                        boolean z = false;
                    }
                    if (i3 != 0) {
                        i = 0;
                    }
                    return i2 & i;
                }

                void bZ() {
                    this.tR.a(this, null, false, false);
                }
            });
            this.sC.a(128, new a(this) {
                final /* synthetic */ c tR;
                private final int tS = 8000;

                {
                    this.tR = r2;
                }

                boolean bY() {
                    long longValue = ((Long) bV()[0]).longValue();
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    long j = longValue - callLogEntity.date;
                    if (this.tR.tP != null) {
                        return this.tR.tP.isShortCall(callLogEntity, j);
                    }
                    boolean z;
                    if (!this.tR.tQ && bU() == 2 && callLogEntity.type == 3) {
                        if ((callLogEntity.duration > 8000 ? 1 : 0) == 0) {
                            if ((longValue - callLogEntity.date > 8000 ? 1 : 0) == 0) {
                                z = true;
                                return z;
                            }
                        }
                    }
                    z = false;
                    return z;
                }

                void bZ() {
                    CallLogEntity callLogEntity = (CallLogEntity) bT();
                    callLogEntity.duration = ((Long) bV()[0]).longValue() - callLogEntity.date;
                    AresEngineFactor aresEngineFactor = this.tR.sD.getAresEngineFactor();
                    aresEngineFactor.getPhoneDeviceController().cancelMissCall();
                    this.tR.a(this, aresEngineFactor.getCallLogDao(), true, false);
                }
            });
            this.sC.a(256, new a(this) {
                final /* synthetic */ c tR;

                {
                    this.tR = r1;
                }

                boolean bY() {
                    return ((CallLogEntity) bT()).type != 2 && bU() == 2;
                }

                void bZ() {
                    this.tR.a(this, this.tR.sD.getAresEngineFactor().getCallLogDao(), false, true);
                }
            });
        }

        private void a(a aVar, ICallLogDao<? extends CallLogEntity> iCallLogDao, boolean z, boolean z2) {
            FilterResult filterResult = new FilterResult();
            filterResult.mParams = aVar.bV();
            filterResult.mData = aVar.bT();
            filterResult.mFilterfiled = aVar.bW();
            filterResult.mState = aVar.bU();
            filterResult.isBlocked = z;
            aVar.a(filterResult);
            if (iCallLogDao != null && z) {
                CallLogEntity callLogEntity = (CallLogEntity) aVar.bT();
                if (z2) {
                    callLogEntity.type = 1;
                }
                AresEngineFactor aresEngineFactor = this.sD.getAresEngineFactor();
                IEntityConverter entityConverter = aresEngineFactor.getEntityConverter();
                if (iCallLogDao.insert(entityConverter == null ? callLogEntity : entityConverter.convert(callLogEntity), filterResult) != -1) {
                    aresEngineFactor.getSysDao().remove(callLogEntity);
                }
            }
        }

        private boolean ck() {
            return TMServiceFactory.getSystemInfoService().aC("com.htc.launcher");
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return this.sC.a(callLogEntity, getConfig(), objArr);
        }

        protected void a(CallLogEntity callLogEntity, FilterResult filterResult, Object... objArr) {
            super.a(callLogEntity, filterResult, new Object[0]);
            if (callLogEntity.type == 2) {
                this.sD.getAresEngineFactor().getLastCallLogDao().update(callLogEntity);
            }
        }

        public FilterConfig defalutFilterConfig() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.set(512, 0);
            filterConfig.set(1, 2);
            filterConfig.set(2, 0);
            filterConfig.set(4, 1);
            filterConfig.set(8, 0);
            filterConfig.set(16, 0);
            filterConfig.set(32, 3);
            filterConfig.set(128, 2);
            filterConfig.set(64, 2);
            filterConfig.set(256, 2);
            return filterConfig;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
            this.tP = iShortCallChecker;
        }
    }

    private jd() {
        this.mContext = TMSDKContext.getApplicaionContext();
    }

    public static jd cg() {
        return a.tA;
    }

    public DataFilter<CallLogEntity> getDataFilter() {
        if (this.tz == null) {
            this.tz = new c(this.mContext);
        }
        return this.tz;
    }

    public DataHandler getDataHandler() {
        return new DataHandler();
    }

    public DataMonitor<CallLogEntity> getDataMonitor() {
        if (this.ty == null) {
            this.ty = new b(this.mContext);
        }
        return this.ty;
    }

    public String getName() {
        return DataInterceptorBuilder.TYPE_SYSTEM_CALL;
    }
}
