package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class PasswordProtectResetFragment extends PasswordProtectSetFragmentBase {
    protected int getProtectFragmentTitle() {
        return R.string.applock_protect_set_title;
    }

    protected int getStartButtonText() {
        return R.string.common_cancel;
    }

    protected void startButtonClick() {
        getActivity().finish();
    }

    protected void endButtonClick() {
        HsmStat.statE(Events.E_APPLOCK_SET_PROTECT_QUESTION_FINISH);
        setQuestionAndAnswer();
        getActivity().finish();
    }
}
