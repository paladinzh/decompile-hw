package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;
import tmsdk.common.module.intelli_sms.MMatchSysResult;

/* compiled from: Unknown */
final class b extends IntelliSmsChecker {
    IntelliSmsCheckResult xi = new IntelliSmsCheckResult(4, new MMatchSysResult(0, 0, 0, 0, 0, null));

    b() {
    }

    public IntelliSmsCheckResult check(SmsEntity smsEntity) {
        return this.xi;
    }

    public IntelliSmsCheckResult check(SmsEntity smsEntity, Boolean bool) {
        return this.xi;
    }

    public boolean isChargingSms(SmsEntity smsEntity) {
        return false;
    }
}
