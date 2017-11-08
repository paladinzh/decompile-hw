package com.huawei.harassmentinterception.engine.tencent;

import android.content.Context;
import tmsdk.bg.module.aresengine.AresEngineFactor;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IContactDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.IKeyWordDao;
import tmsdk.common.module.aresengine.ILastCallLogDao;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.SmsEntity;

public class TmAresEngineFactor extends AresEngineFactor {
    public TmAresEngineFactor(Context context) {
    }

    public IContactDao<? extends ContactEntity> getBlackListDao() {
        return null;
    }

    public ICallLogDao<? extends CallLogEntity> getCallLogDao() {
        return null;
    }

    public IEntityConverter getEntityConverter() {
        return null;
    }

    public IKeyWordDao getKeyWordDao() {
        return null;
    }

    public ILastCallLogDao getLastCallLogDao() {
        return null;
    }

    public ICallLogDao<? extends CallLogEntity> getPrivateCallLogDao() {
        return null;
    }

    public IContactDao<? extends ContactEntity> getPrivateListDao() {
        return null;
    }

    public ISmsDao<? extends SmsEntity> getPrivateSmsDao() {
        return null;
    }

    public ISmsDao<? extends SmsEntity> getSmsDao() {
        return null;
    }

    public IContactDao<? extends ContactEntity> getWhiteListDao() {
        return null;
    }
}
