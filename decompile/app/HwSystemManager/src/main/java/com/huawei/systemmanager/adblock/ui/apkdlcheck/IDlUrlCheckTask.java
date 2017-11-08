package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlUrlCheckOnlineTask.Callback;
import com.huawei.systemmanager.adblock.ui.view.DlChoiceDialog;
import com.huawei.systemmanager.adblock.ui.view.InstallAppmarketDialog;

public interface IDlUrlCheckTask extends Callback, DlChoiceDialog.Callback, InstallAppmarketDialog.Callback, InstallAppmarketTask.Callback {
    void execute();

    String getDownloadId();

    boolean isValid();

    void setResult(boolean z);

    void statAdUrlBlocked();
}
