package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import tmsdk.bg.module.network.CodeName;

public class OperaterSetPreference extends AbsPreference implements OnClickListener {
    public static final String TAG = "OperaterSettingPreference";
    private AlertDialog mBrandDlg = null;
    String mBrandId;
    private ArrayList<CodeName> mBrands = null;
    private CardItem mCard;
    private AlertDialog mCarriesDlg = null;
    String mCarryId;
    private ArrayList<CodeName> mCarrys = null;
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (OperaterSetPreference.this.mCard == null) {
                HwLog.e(OperaterSetPreference.TAG, "mLoadSummaryTask card is null!");
                return;
            }
            String[] operator = OperaterSetPreference.this.mCard.getOperator(OperaterSetPreference.this.getContext());
            String carrierId = operator[0];
            String brandId = operator[1];
            String carrierName = TrafficCorrectionWrapper.getInstance().getCarrierNameById(carrierId);
            if (TextUtils.isEmpty(carrierName)) {
                carrierName = OperaterSetPreference.this.getContext().getString(R.string.pref_not_set);
            }
            String brandName = TrafficCorrectionWrapper.getInstance().getBrandNameById(carrierId, brandId);
            if (TextUtils.isEmpty(brandId)) {
                brandName = OperaterSetPreference.this.getContext().getString(R.string.pref_not_set);
            }
            OperaterSetPreference.this.postSetSummary(OperaterSetPreference.this.getContext().getString(R.string.operator_pref_summary, new Object[]{carrierName, brandName}));
        }
    };

    public OperaterSetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OperaterSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.content_operator_settings);
    }

    protected void onClick() {
        startDlg();
    }

    public void setCard(CardItem card) {
        this.mCard = card;
        refreshPreferShow();
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    public void startDlg() {
        if (this.mCard == null) {
            HwLog.e(TAG, "startDlg card is null!");
            return;
        }
        this.mCarrys = TrafficCorrectionWrapper.getInstance().getCarries();
        if (this.mCarrys != null) {
            String[] nItems = new String[this.mCarrys.size()];
            String carryId = this.mCard.getOperator(getContext())[0];
            int defIndex = 0;
            for (int i = 0; i < nItems.length; i++) {
                if (TextUtils.equals(((CodeName) this.mCarrys.get(i)).mCode, carryId)) {
                    defIndex = i;
                }
                nItems[i] = ((CodeName) this.mCarrys.get(i)).mName;
            }
            Builder ab = new Builder(getContext());
            ab.setTitle(R.string.content_operator_settings);
            ab.setSingleChoiceItems(nItems, defIndex, this);
            ab.setNegativeButton(R.string.common_cancel, null);
            this.mCarriesDlg = ab.create();
            this.mCarriesDlg.show();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCard == null) {
            HwLog.e(TAG, "onClick card is null!");
            return;
        }
        if (this.mCarriesDlg == dialog) {
            this.mCarriesDlg.cancel();
            this.mBrands = TrafficCorrectionWrapper.getInstance().getBrands(((CodeName) this.mCarrys.get(which)).mCode);
            if (this.mBrands != null) {
                this.mCarryId = ((CodeName) this.mCarrys.get(which)).mCode;
                String[] nItems = new String[this.mBrands.size()];
                String brandId = this.mCard.getOperator(getContext())[1];
                int defIndex = 0;
                for (int i = 0; i < nItems.length; i++) {
                    if (TextUtils.equals(brandId, ((CodeName) this.mBrands.get(i)).mCode)) {
                        defIndex = i;
                    }
                    nItems[i] = ((CodeName) this.mBrands.get(i)).mName;
                }
                Builder ab = new Builder(getContext());
                ab.setTitle(R.string.content_operator_settings);
                ab.setSingleChoiceItems(nItems, defIndex, this);
                ab.setNegativeButton(R.string.common_cancel, null);
                this.mBrandDlg = ab.create();
                this.mBrandDlg.show();
            }
        } else if (dialog == this.mBrandDlg) {
            this.mBrandId = ((CodeName) this.mBrands.get(which)).mCode;
            this.mCard.setOperator(getContext(), this.mCarryId, this.mBrandId);
            this.mBrandDlg.cancel();
            refreshPreferShow();
        }
    }
}
