package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class PasswordProtectInitFragment extends PasswordProtectSetFragmentBase {
    protected int getProtectFragmentTitle() {
        return R.string.applock_protect_set_title;
    }

    protected int getStartButtonText() {
        return R.string.applock_protect_set_skip;
    }

    protected int getEndButtonText() {
        return R.string.common_finish;
    }

    protected void startButtonClick() {
        HsmStat.statE(Events.E_APPLOCK_INIT_SKIP_PROTECT_QUESTION);
        startActivity(ActivityIntentUtils.getApplicationListActivityIntent(this.mAppContext));
        getActivity().finish();
    }

    protected void endButtonClick() {
        HsmStat.statE(Events.E_APPLOCK_INIT_FINISH_PROTECT_QUESTION);
        setQuestionAndAnswer();
        startActivity(ActivityIntentUtils.getApplicationListActivityIntent(this.mAppContext));
        getActivity().finish();
    }
}
