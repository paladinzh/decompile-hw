package com.huawei.harassmentinterception.callback;

import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import java.util.List;

public interface HandleListCallBack {
    void onAfterCheckListExist(int i, boolean z, List<ContactInfo> list);

    void onCompleteHandleList(int i);

    void onProcessHandleList(Object obj);
}
