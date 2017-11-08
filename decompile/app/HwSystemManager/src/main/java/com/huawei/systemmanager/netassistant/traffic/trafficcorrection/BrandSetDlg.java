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
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import java.util.ArrayList;
import tmsdk.bg.module.network.CodeName;

public class BrandSetDlg implements OnClickListener {
    private static final String TAG = BrandSetDlg.class.getSimpleName();
    private AlertDialog mBrandDlg;
    String mBrandId;
    private ArrayList<CodeName> mBrands;
    private AlertDialog mCarriesDlg;
    String mCarryId;
    private ArrayList<CodeName> mCarrys;
    private Context mContext;
    private String mImsi = SimCardManager.getInstance().getSimcardByIndex(this.mSimIndex);
    private int mSimIndex;

    public BrandSetDlg(Context context, int simIndex) {
        this.mContext = context;
        this.mSimIndex = simIndex;
    }

    public void startDlg() {
        this.mCarrys = TrafficCorrectionWrapper.getInstance().getCarries();
        if (this.mCarrys != null) {
            String[] nItems = new String[this.mCarrys.size()];
            String carryId = NetAssistantDBManager.getInstance().getAdjustProvider(this.mImsi);
            int defIndex = 0;
            for (int i = 0; i < nItems.length; i++) {
                if (TextUtils.equals(((CodeName) this.mCarrys.get(i)).mCode, carryId)) {
                    defIndex = i;
                }
                nItems[i] = ((CodeName) this.mCarrys.get(i)).mName;
            }
            Builder ab = new Builder(this.mContext);
            ab.setTitle(R.string.content_operator_settings);
            ab.setSingleChoiceItems(nItems, defIndex, this);
            this.mCarriesDlg = ab.create();
            this.mCarriesDlg.show();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCarriesDlg == dialog) {
            this.mCarriesDlg.cancel();
            this.mBrands = TrafficCorrectionWrapper.getInstance().getBrands(((CodeName) this.mCarrys.get(which)).mCode);
            if (this.mBrands != null) {
                this.mCarryId = ((CodeName) this.mCarrys.get(which)).mCode;
                String[] nItems = new String[this.mBrands.size()];
                String brandId = NetAssistantDBManager.getInstance().getAdjustBrand(this.mImsi);
                int defIndex = 0;
                for (int i = 0; i < nItems.length; i++) {
                    if (TextUtils.equals(brandId, ((CodeName) this.mBrands.get(i)).mCode)) {
                        defIndex = i;
                    }
                    nItems[i] = ((CodeName) this.mBrands.get(i)).mName;
                }
                Builder ab = new Builder(this.mContext);
                ab.setTitle(R.string.content_operator_settings);
                ab.setSingleChoiceItems(nItems, defIndex, this);
                this.mBrandDlg = ab.create();
                this.mBrandDlg.show();
            }
        } else if (dialog == this.mBrandDlg) {
            this.mBrandId = ((CodeName) this.mBrands.get(which)).mCode;
            NetAssistantDBManager.getInstance().setOperatorInfo(this.mImsi, this.mCarryId, this.mBrandId);
            this.mBrandDlg.cancel();
        }
    }
}
