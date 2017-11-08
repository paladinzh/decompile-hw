package com.huawei.cloudservice;

import android.os.Bundle;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

public interface CloudRequestHandler {
    void onError(ErrorStatus errorStatus);

    void onFinish(Bundle bundle);
}
