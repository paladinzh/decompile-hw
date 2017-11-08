package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
final class a implements DataInterceptor<TelephonyEntity> {
    static final FilterConfig xd = new FilterConfig();
    static final FilterResult xe = new FilterResult();
    a xf = new a();
    DataFilter<? extends TelephonyEntity> xg;
    DataHandler xh = new DataHandler();

    /* compiled from: Unknown */
    static final class a extends DataMonitor<TelephonyEntity> {
        a() {
        }
    }

    /* compiled from: Unknown */
    static final class b extends IncomingCallFilter {
        b() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }
    }

    /* compiled from: Unknown */
    static final class c extends IncomingSmsFilter {
        c() {
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }

        public void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler) {
        }

        public void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker) {
        }
    }

    /* compiled from: Unknown */
    static final class d extends OutgoingSmsFilter {
        d() {
        }

        protected /* synthetic */ FilterResult a(TelephonyEntity telephonyEntity, Object[] objArr) {
            return b((SmsEntity) telephonyEntity, objArr);
        }

        protected FilterResult b(SmsEntity smsEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }
    }

    /* compiled from: Unknown */
    static final class e extends SystemCallLogFilter {
        e() {
        }

        protected FilterResult a(CallLogEntity callLogEntity, Object... objArr) {
            return a.xe;
        }

        public FilterConfig defalutFilterConfig() {
            return a.xd;
        }

        public void setShortCallChecker(IShortCallChecker iShortCallChecker) {
        }
    }

    public a(String str) {
        DataFilter bVar;
        if (str.equals(DataInterceptorBuilder.TYPE_INCOMING_CALL)) {
            bVar = new b();
        } else if (str.equals(DataInterceptorBuilder.TYPE_INCOMING_SMS)) {
            bVar = new c();
        } else if (str.equals(DataInterceptorBuilder.TYPE_OUTGOING_SMS)) {
            bVar = new d();
        } else if (str.equals(DataInterceptorBuilder.TYPE_SYSTEM_CALL)) {
            bVar = new e();
        } else {
            return;
        }
        this.xg = bVar;
    }

    public DataFilter<TelephonyEntity> dataFilter() {
        return this.xg;
    }

    public DataHandler dataHandler() {
        return this.xh;
    }

    public DataMonitor<TelephonyEntity> dataMonitor() {
        return this.xf;
    }
}
