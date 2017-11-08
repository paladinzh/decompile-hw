package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Parcelable;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

/* compiled from: DummyActivity */
class c implements OnClickListener {
    final /* synthetic */ DummyActivity a;

    c(DummyActivity dummyActivity) {
        this.a = dummyActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        a.d("DummyActivity", "READ_PHONE_STATE PermissionName is null!");
        Parcelable errorStatus = new ErrorStatus(28, "READ_PHONE_STATE  Permission is not allowed");
        Intent intent = new Intent();
        intent.setPackage(this.a.getPackageName());
        intent.putExtra("isUseSDK", false);
        intent.putExtra("parce", errorStatus);
        e.c(this.a, intent);
        this.a.finish();
    }
}
