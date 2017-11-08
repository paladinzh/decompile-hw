package com.huawei.systemmanager.adblock.ui.presenter;

import android.os.Bundle;

public interface IAdPresenter {
    void onCreate(Bundle bundle);

    void onDestroy();

    void onPause();

    void onResume();
}
