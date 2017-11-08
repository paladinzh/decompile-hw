package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import java.util.ArrayList;
import tmsdk.bg.module.network.CodeName;

public class ProvinceSetDlg implements OnClickListener {
    private static final String TAG = ProvinceSetDlg.class.getSimpleName();
    String mCityId;
    private String mCityName;
    private AlertDialog mCitysDlg;
    private ArrayList<CodeName> mCitysList;
    private Context mContext;
    private String mImsi = SimCardManager.getInstance().getSimcardByIndex(this.mSimIndex);
    private AlertDialog mProvinceDlg;
    String mProvinceId;
    private String mProvinceName;
    private ArrayList<CodeName> mProvinces;
    private int mSimIndex;

    public ProvinceSetDlg(Context context, int simIndex) {
        this.mContext = context;
        this.mSimIndex = simIndex;
    }

    public void startDlg() {
        this.mProvinces = TrafficCorrectionWrapper.getInstance().getAllProvinces();
        if (this.mProvinces != null) {
            String[] nItems = new String[this.mProvinces.size()];
            String provinceId = NetAssistantDBManager.getInstance().getAdjustProvince(this.mImsi);
            int defIndex = 0;
            for (int i = 0; i < nItems.length; i++) {
                if (TextUtils.equals(provinceId, ((CodeName) this.mProvinces.get(i)).mCode)) {
                    defIndex = i;
                }
                nItems[i] = ((CodeName) this.mProvinces.get(i)).mName;
            }
            Builder ab = new Builder(this.mContext);
            ab.setTitle(R.string.content_city_settings);
            ab.setSingleChoiceItems(nItems, defIndex, this);
            this.mProvinceDlg = ab.create();
            this.mProvinceDlg.show();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mProvinceDlg == dialog) {
            this.mProvinceDlg.cancel();
            this.mCitysList = TrafficCorrectionWrapper.getInstance().getCities(((CodeName) this.mProvinces.get(which)).mCode);
            if (this.mCitysList != null) {
                this.mProvinceId = ((CodeName) this.mProvinces.get(which)).mCode;
                this.mProvinceName = ((CodeName) this.mProvinces.get(which)).mName;
                String[] nItems = new String[this.mCitysList.size()];
                String cityId = NetAssistantDBManager.getInstance().getAdjustCity(this.mImsi);
                int defIndex = 0;
                for (int i = 0; i < nItems.length; i++) {
                    if (TextUtils.equals(cityId, ((CodeName) this.mCitysList.get(i)).mCode)) {
                        defIndex = i;
                    }
                    nItems[i] = ((CodeName) this.mCitysList.get(i)).mName;
                }
                Builder ab = new Builder(this.mContext);
                ab.setTitle(R.string.content_city_settings);
                ab.setSingleChoiceItems(nItems, defIndex, this);
                this.mCitysDlg = ab.create();
                this.mCitysDlg.show();
            }
        } else if (dialog == this.mCitysDlg) {
            this.mCityId = ((CodeName) this.mCitysList.get(which)).mCode;
            this.mCityName = ((CodeName) this.mCitysList.get(which)).mName;
            NetAssistantDBManager.getInstance().setProvinceInfo(this.mImsi, this.mProvinceId, this.mCityId);
            String info = String.format("%s,%s", new Object[]{this.mProvinceName, this.mCityName});
            String statParam = HsmStatConst.constructJsonParams("LOC", info);
            HsmStat.statE(92, statParam);
            this.mCitysDlg.cancel();
        }
    }
}
