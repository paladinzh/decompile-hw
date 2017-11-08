package com.android.contacts.hap.numbermark.base;

import com.android.contacts.hap.numbermark.CapabilityInfo;
import com.android.contacts.hap.service.NumberMarkInfo;
import java.util.List;

public interface ISDKCallApi {
    List<CapabilityInfo> getExtraInfoByNum(String str);

    NumberMarkInfo getInfoByNum(String str, String str2);

    NumberMarkInfo getInfoFromPresetDB(String str);
}
