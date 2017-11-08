package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import java.util.ArrayList;
import tmsdk.bg.module.network.CodeName;

public class LocationSetDlg implements OnClickListener {
    private static final String TAG = LocationSetDlg.class.getSimpleName();
    String mBrandId;
    private ArrayList<CodeName> mBrands;
    private AlertDialog mBrandsDlg;
    String mCarryId;
    private ArrayList<CodeName> mCarrys;
    private AlertDialog mCarrysDlg;
    String mCityId;
    private AlertDialog mCitysDlg;
    private ArrayList<CodeName> mCitysList;
    private Context mContext;
    private AlertDialog mProvinceDlg;
    String mProvinceId;
    private ArrayList<CodeName> mProvinces;
    private int mSimIndex;

    public LocationSetDlg(Context context, int simIndex) {
        this.mContext = context;
        this.mSimIndex = simIndex;
    }

    public void startDlg() {
        this.mProvinces = TrafficCorrectionWrapper.getInstance().getAllProvinces();
        if (this.mProvinces != null) {
            String[] nItems = new String[this.mProvinces.size()];
            for (int i = 0; i < nItems.length; i++) {
                nItems[i] = ((CodeName) this.mProvinces.get(i)).mName;
            }
            Builder ab = new Builder(this.mContext);
            ab.setTitle(R.string.content_city_settings);
            ab.setSingleChoiceItems(nItems, 0, this);
            this.mProvinceDlg = ab.create();
            this.mProvinceDlg.show();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        String[] nItems;
        int i;
        Builder ab;
        if (this.mProvinceDlg == dialog) {
            this.mProvinceDlg.cancel();
            this.mCitysList = TrafficCorrectionWrapper.getInstance().getCities(((CodeName) this.mProvinces.get(which)).mCode);
            if (this.mCitysList != null) {
                this.mProvinceId = ((CodeName) this.mProvinces.get(which)).mCode;
                nItems = new String[this.mCitysList.size()];
                for (i = 0; i < nItems.length; i++) {
                    nItems[i] = ((CodeName) this.mCitysList.get(i)).mName;
                }
                ab = new Builder(this.mContext);
                ab.setTitle(R.string.content_city_settings);
                ab.setSingleChoiceItems(nItems, 0, this);
                this.mCitysDlg = ab.create();
                this.mCitysDlg.show();
            }
        } else if (dialog == this.mCitysDlg) {
            this.mCitysDlg.cancel();
            this.mCityId = ((CodeName) this.mCitysList.get(which)).mCode;
            this.mCarrys = TrafficCorrectionWrapper.getInstance().getCarries();
            if (this.mCarrys != null) {
                nItems = new String[this.mCarrys.size()];
                for (i = 0; i < nItems.length; i++) {
                    nItems[i] = ((CodeName) this.mCarrys.get(i)).mName;
                }
                ab = new Builder(this.mContext);
                ab.setTitle(R.string.content_operator_settings);
                ab.setSingleChoiceItems(nItems, 0, this);
                this.mCarrysDlg = ab.create();
                this.mCarrysDlg.show();
            }
        } else if (dialog == this.mCarrysDlg) {
            this.mCarrysDlg.cancel();
            this.mCarryId = ((CodeName) this.mCarrys.get(which)).mCode;
            this.mBrands = TrafficCorrectionWrapper.getInstance().getBrands(((CodeName) this.mCarrys.get(which)).mCode);
            if (this.mBrands != null) {
                nItems = new String[this.mBrands.size()];
                for (i = 0; i < nItems.length; i++) {
                    nItems[i] = ((CodeName) this.mBrands.get(i)).mName;
                }
                ab = new Builder(this.mContext);
                ab.setTitle(R.string.content_operator_settings);
                ab.setSingleChoiceItems(nItems, 0, this);
                this.mBrandsDlg = ab.create();
                this.mBrandsDlg.show();
            }
        } else if (dialog == this.mBrandsDlg) {
            this.mBrandId = ((CodeName) this.mBrands.get(which)).mCode;
            String imsi = SimCardManager.getInstance().getSimcardByIndex(this.mSimIndex);
            NetAssistantDBManager.getInstance().setProvinceInfo(imsi, this.mProvinceId, this.mCityId);
            NetAssistantDBManager.getInstance().setOperatorInfo(imsi, this.mCarryId, this.mBrandId);
            this.mBrandsDlg.cancel();
        }
    }
}
