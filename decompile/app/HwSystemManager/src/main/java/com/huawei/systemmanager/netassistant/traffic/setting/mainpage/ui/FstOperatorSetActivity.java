package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.ICodeName;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter.FstOperatorSetPresenter;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter.FstOperatorSetPresenterImp;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel.FstOperatorSetView;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class FstOperatorSetActivity extends HsmActivity implements FstOperatorSetView, OnClickListener {
    private static final String TAG = FstOperatorSetActivity.class.getSimpleName();
    View mBrandView;
    Button mButton;
    View mCityView;
    OperatorDialog mDialog;
    String mImsi;
    View mOperatorView;
    FstOperatorSetPresenter mPresenter;
    View mProvinceView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fst_operator_set);
        Intent intent = getIntent();
        if (intent != null) {
            this.mImsi = intent.getStringExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI);
            if (TextUtils.isEmpty(this.mImsi)) {
                HwLog.d(TAG, "this is no imsi");
                finish();
            }
            this.mPresenter = new FstOperatorSetPresenterImp(this, this.mImsi);
            this.mDialog = new OperatorDialog(this);
            this.mProvinceView = findViewById(R.id.province_view);
            ((TextView) this.mProvinceView.findViewById(R.id.title)).setText(R.string.simcard_province);
            this.mCityView = findViewById(R.id.city_view);
            ((TextView) this.mCityView.findViewById(R.id.title)).setText(R.string.simcard_city);
            this.mOperatorView = findViewById(R.id.operator_view);
            ((TextView) this.mOperatorView.findViewById(R.id.title)).setText(R.string.operator_info_title);
            this.mBrandView = findViewById(R.id.brand_view);
            ((TextView) this.mBrandView.findViewById(R.id.title)).setText(R.string.brand_info_title);
            this.mButton = (Button) findViewById(R.id.button);
            this.mProvinceView.setOnClickListener(this);
            this.mCityView.setOnClickListener(this);
            this.mOperatorView.setOnClickListener(this);
            this.mBrandView.setOnClickListener(this);
            this.mButton.setOnClickListener(this);
            this.mPresenter.showDefaultView();
        }
    }

    public void showProvinceDialog(final List<ICodeName> list, int select) {
        this.mDialog.showDialog(list, R.string.content_province, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FstOperatorSetActivity.this.mPresenter.onProvinceSet((ICodeName) list.get(which));
                dialog.dismiss();
            }
        }, select);
    }

    public void showCityDialog(final List<ICodeName> list, int select) {
        this.mDialog.showDialog(list, R.string.content_city, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FstOperatorSetActivity.this.mPresenter.onCitySet((ICodeName) list.get(which));
                dialog.dismiss();
            }
        }, select);
    }

    public void showOperatorDialog(final List<ICodeName> list, int select) {
        this.mDialog.showDialog(list, R.string.content_operator_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FstOperatorSetActivity.this.mPresenter.onOperatorSet((ICodeName) list.get(which));
                dialog.dismiss();
            }
        }, select);
    }

    public void showBrandDialog(final List<ICodeName> list, int select) {
        this.mDialog.showDialog(list, R.string.content_operator_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FstOperatorSetActivity.this.mPresenter.onBrandSet((ICodeName) list.get(which));
                dialog.dismiss();
            }
        }, select);
    }

    public void finishOperatorSet() {
        setResult(201);
        finish();
    }

    public void setProvince(String provider) {
        ((TextView) this.mProvinceView.findViewById(R.id.txt)).setText(provider);
    }

    public void setCity(String city) {
        ((TextView) this.mCityView.findViewById(R.id.txt)).setText(city);
    }

    public void setOperator(String operator) {
        ((TextView) this.mOperatorView.findViewById(R.id.txt)).setText(operator);
    }

    public void setBrand(String brand) {
        ((TextView) this.mBrandView.findViewById(R.id.txt)).setText(brand);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.province_view:
                this.mPresenter.changeProvince();
                return;
            case R.id.city_view:
                this.mPresenter.changeCity();
                return;
            case R.id.operator_view:
                HsmStat.statE(Events.E_NETASSISTANT_CHANGE_OPERATOR);
                this.mPresenter.changeOperator();
                return;
            case R.id.brand_view:
                HsmStat.statE(Events.E_NETASSISTANT_CHANGE_BRAND);
                this.mPresenter.changeBrand();
                return;
            case R.id.button:
                HsmStat.statE(Events.E_NETASSISTANT_FINISH_PACKAGE_SET);
                this.mPresenter.finishOperatorSet();
                return;
            default:
                return;
        }
    }
}
