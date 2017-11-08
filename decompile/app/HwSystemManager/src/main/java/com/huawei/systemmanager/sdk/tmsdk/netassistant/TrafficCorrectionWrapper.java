package com.huawei.systemmanager.sdk.tmsdk.netassistant;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.bg.module.network.ProfileInfo;
import tmsdk.bg.module.network.TrafficCorrectionManager;

public class TrafficCorrectionWrapper {
    static final String TAG = "TrafficCorrectionWrapper";
    private static TrafficCorrectionWrapper sInstance = new TrafficCorrectionWrapper();
    private boolean mHasInit;
    private TrafficCorrectionManager mTcMgr;
    IHwTrafficCorrectionListener mTrafficCorrectionListener;

    public interface IHwTrafficCorrectionListener {
        void onError(int i, int i2);

        void onNeedSmsCorrection(int i, String str, String str2);

        void onProfileNotify(int i, ProfileInfo profileInfo);

        void onTrafficInfoNotify(int i, int i2, int i3, int i4);
    }

    private TrafficCorrectionWrapper() {
    }

    public static synchronized TrafficCorrectionWrapper getInstance() {
        TrafficCorrectionWrapper trafficCorrectionWrapper;
        synchronized (TrafficCorrectionWrapper.class) {
            trafficCorrectionWrapper = sInstance;
        }
        return trafficCorrectionWrapper;
    }

    public void init(Context context) {
        Utility.initSDK(context);
        if (context != null) {
            if (TMSEngineFeature.isSupportTMS()) {
                try {
                    this.mTcMgr = (TrafficCorrectionManager) ManagerCreatorB.getManager(TrafficCorrectionManager.class);
                    this.mTcMgr.setTrafficCorrectionListener(new ITrafficCorrectionListener() {
                        public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
                            if (TrafficCorrectionWrapper.this.mTrafficCorrectionListener != null) {
                                TrafficCorrectionWrapper.this.mTrafficCorrectionListener.onNeedSmsCorrection(simIndex, queryCode, queryPort);
                            }
                        }

                        public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
                            TrafficCorrectionWrapper.this.saveTrafficInfo(simIndex, trafficClass, subClass, kBytes);
                            if (TrafficCorrectionWrapper.this.mTrafficCorrectionListener != null) {
                                TrafficCorrectionWrapper.this.mTrafficCorrectionListener.onTrafficInfoNotify(simIndex, trafficClass, subClass, kBytes);
                            }
                        }

                        public void onError(int simIndex, int errorCode) {
                            if (TrafficCorrectionWrapper.this.mTrafficCorrectionListener != null) {
                                TrafficCorrectionWrapper.this.mTrafficCorrectionListener.onError(simIndex, errorCode);
                            }
                        }

                        public void onProfileNotify(int simIndex, ProfileInfo info) {
                            if (TrafficCorrectionWrapper.this.mTrafficCorrectionListener != null) {
                                TrafficCorrectionWrapper.this.mTrafficCorrectionListener.onProfileNotify(simIndex, info);
                            }
                        }
                    });
                    this.mHasInit = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            HwLog.w(TAG, "init: TMS is not supported");
        }
    }

    public void startCorrection(int simIndex) {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        this.mTcMgr.startCorrection(simIndex);
    }

    public void requestProfile(int simIndex) {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        this.mTcMgr.requestProfile(simIndex);
    }

    public void analysisSMS(int simIndex, String queryCode, String queryPort, String smsBody) {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        this.mTcMgr.analysisSMS(simIndex, queryCode, queryPort, smsBody);
    }

    public ArrayList<CodeName> getAllProvinces() {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        return this.mTcMgr.getAllProvinces();
    }

    public String getProvinceNameById(String provinceId) {
        if (TextUtils.isEmpty(provinceId)) {
            return "";
        }
        ArrayList<CodeName> provinces = getAllProvinces();
        String provinceName = "";
        if (provinces != null) {
            for (CodeName codeName : provinces) {
                if (TextUtils.equals(codeName.mCode, provinceId)) {
                    provinceName = codeName.mName;
                    break;
                }
            }
        }
        return provinceName;
    }

    public String getCityNameById(String provinceId, String cityId) {
        if (TextUtils.isEmpty(provinceId) || TextUtils.isEmpty(cityId)) {
            return "";
        }
        ArrayList<CodeName> citys = getCities(provinceId);
        String cityName = "";
        if (citys != null) {
            for (CodeName codeName : citys) {
                if (TextUtils.equals(codeName.mCode, cityId)) {
                    cityName = codeName.mName;
                    break;
                }
            }
        }
        return cityName;
    }

    public String getCarrierNameById(String carrierId) {
        if (TextUtils.isEmpty(carrierId)) {
            return "";
        }
        ArrayList<CodeName> providers = getCarries();
        String providerString = null;
        if (providers != null) {
            for (CodeName codeName : providers) {
                if (TextUtils.equals(codeName.mCode, carrierId)) {
                    providerString = codeName.mName;
                    break;
                }
            }
        }
        return providerString;
    }

    public String getBrandNameById(String carrierId, String brandId) {
        if (TextUtils.isEmpty(brandId)) {
            return "";
        }
        ArrayList<CodeName> brands = getBrands(carrierId);
        String brandString = null;
        if (brands != null) {
            for (CodeName codeName : brands) {
                if (TextUtils.equals(codeName.mCode, brandId)) {
                    brandString = codeName.mName;
                    break;
                }
            }
        }
        return brandString;
    }

    public ArrayList<CodeName> getCities(String provinceCode) {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        return this.mTcMgr.getCities(provinceCode);
    }

    public ArrayList<CodeName> getCarries() {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        return this.mTcMgr.getCarries();
    }

    public ArrayList<CodeName> getBrands(String carryId) {
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        return this.mTcMgr.getBrands(carryId);
    }

    public void setConfig(int simIndex, String provinceId, String cityId, String carryId, String brandId, int closingDay) {
        HwLog.d(TAG, "simindex = " + simIndex + " provinceId = " + provinceId + " cityId = " + cityId + " carryId = " + carryId + " brandId = " + brandId);
        if (!this.mHasInit) {
            init(GlobalContext.getContext());
        }
        this.mTcMgr.setConfig(simIndex, provinceId, cityId, carryId, brandId, closingDay);
    }

    public void setTrafficCorrectionListener(IHwTrafficCorrectionListener listener) {
        this.mTrafficCorrectionListener = listener;
    }

    private void saveTrafficInfo(int simIndex, int trafficClass, int subClass, int kBytes) {
    }
}
