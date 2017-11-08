package com.huawei.systemmanager.antivirus.engine;

import com.huawei.systemmanager.antivirus.ScanResultEntity;
import java.util.List;
import java.util.Map;

public interface IScanPackageMgr {
    ScanResultEntity scanPackage(ScanResultEntity scanResultEntity);

    ScanResultEntity scanPackage(ScanResultEntity scanResultEntity, String str, String str2);

    List<ScanResultEntity> scanPackage(Map<String, ScanResultEntity> map);
}
