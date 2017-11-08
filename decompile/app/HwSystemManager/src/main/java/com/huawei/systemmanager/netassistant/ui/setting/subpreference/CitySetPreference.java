package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import tmsdk.bg.module.network.CodeName;

public class CitySetPreference extends AbsPreference implements OnClickListener {
    public static final String TAG = "CitySettingPreference";
    private CardItem mCard = null;
    String mCityId;
    private String mCityName = null;
    private AlertDialog mCitysDlg = null;
    private ArrayList<CodeName> mCitysList = null;
    private AlertDialog mProvinceDlg = null;
    String mProvinceId;
    private String mProvinceName = null;
    private ArrayList<CodeName> mProvinces = null;
    private Runnable mRefreshShowTask = new Runnable() {
        public void run() {
            String[] provinceAndCity = CitySetPreference.this.mCard.getProvinceAndCityId(CitySetPreference.this.getContext());
            String provinceId = provinceAndCity[0];
            String cityId = provinceAndCity[1];
            String province = TrafficCorrectionWrapper.getInstance().getProvinceNameById(provinceId);
            if (TextUtils.isEmpty(province)) {
                province = CitySetPreference.this.getContext().getString(R.string.pref_not_set);
            }
            String city = TrafficCorrectionWrapper.getInstance().getCityNameById(provinceId, cityId);
            if (TextUtils.isEmpty(city)) {
                city = CitySetPreference.this.getContext().getString(R.string.pref_not_set);
            }
            CitySetPreference.this.postSetSummary(CitySetPreference.this.getContext().getString(R.string.city_pref_summary, new Object[]{province, city}));
        }
    };

    public CitySetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CitySetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.content_city_settings);
    }

    public void setCard(CardItem card) {
        this.mCard = card;
        refreshPreferShow();
    }

    public void refreshPreferShow() {
        if (this.mCard == null) {
            HwLog.e(TAG, "refreshPreferShow card is null!");
        } else {
            postRunnableAsync(this.mRefreshShowTask);
        }
    }

    protected void onClick() {
        startDlg();
    }

    public void startDlg() {
        if (this.mCard == null) {
            HwLog.e(TAG, "startDlg card is null!");
            return;
        }
        this.mProvinces = TrafficCorrectionWrapper.getInstance().getAllProvinces();
        if (this.mProvinces != null) {
            String[] nItems = new String[this.mProvinces.size()];
            String provinceId = this.mCard.getProvinceAndCityId(getContext())[0];
            int defIndex = 0;
            for (int i = 0; i < nItems.length; i++) {
                if (TextUtils.equals(provinceId, ((CodeName) this.mProvinces.get(i)).mCode)) {
                    defIndex = i;
                }
                nItems[i] = ((CodeName) this.mProvinces.get(i)).mName;
            }
            Builder ab = new Builder(getContext());
            ab.setTitle(R.string.content_province);
            ab.setSingleChoiceItems(nItems, defIndex, this);
            ab.setNegativeButton(R.string.common_cancel, null);
            this.mProvinceDlg = ab.create();
            this.mProvinceDlg.show();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCard == null) {
            HwLog.e(TAG, "startDlg card is null!");
            return;
        }
        if (this.mProvinceDlg == dialog) {
            this.mProvinceDlg.cancel();
            this.mCitysList = TrafficCorrectionWrapper.getInstance().getCities(((CodeName) this.mProvinces.get(which)).mCode);
            if (this.mCitysList != null) {
                this.mProvinceId = ((CodeName) this.mProvinces.get(which)).mCode;
                this.mProvinceName = ((CodeName) this.mProvinces.get(which)).mName;
                String[] nItems = new String[this.mCitysList.size()];
                String cityId = this.mCard.getProvinceAndCityId(getContext())[1];
                int defIndex = 0;
                for (int i = 0; i < nItems.length; i++) {
                    if (TextUtils.equals(cityId, ((CodeName) this.mCitysList.get(i)).mCode)) {
                        defIndex = i;
                    }
                    nItems[i] = ((CodeName) this.mCitysList.get(i)).mName;
                }
                Builder ab = new Builder(getContext());
                ab.setTitle(R.string.content_city);
                ab.setSingleChoiceItems(nItems, defIndex, this);
                ab.setNegativeButton(R.string.common_cancel, null);
                this.mCitysDlg = ab.create();
                this.mCitysDlg.show();
            }
        } else if (dialog == this.mCitysDlg) {
            this.mCityId = ((CodeName) this.mCitysList.get(which)).mCode;
            this.mCityName = ((CodeName) this.mCitysList.get(which)).mName;
            this.mCard.setProvinceAndCity(getContext(), this.mProvinceId, this.mCityId);
            String[] strArr = new String[2];
            strArr[0] = "LOC";
            strArr[1] = String.format("%s,%s", new Object[]{this.mProvinceName, this.mCityName});
            String statParam = HsmStatConst.constructJsonParams(strArr);
            HsmStat.statE(92, statParam);
            this.mCitysDlg.cancel();
            refreshPreferShow();
        }
    }
}
