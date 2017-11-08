package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.OtherTrafficSettingsActivity;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.TrafficPackageModel;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter.FstPackageSetPresenter;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter.FstPackageSetPresenterImp;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel.FstPackageSetView;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class FstPackageSetActivity extends HsmActivity implements FstPackageSetView, OnClickListener, TextWatcher, MessageHandler {
    public static final int CODE_PACKAGE_SET = 200;
    public static final int CODE_PACKAGE_SET_FINISH = 201;
    private static final int DECIMAL_PART_MAXLEN = 2;
    private static final int INTEGER_PART_MAXLEN = 6;
    private static final String TAG = FstPackageSetActivity.class.getSimpleName();
    private boolean isNetAssistantEnable;
    private Button mFinishButton;
    private GenericHandler mGenericHandler;
    private String mImsi;
    private boolean mLockState = false;
    private ViewGroup mOtherTrafficViewGroup;
    private EditText mPackageNumText;
    private Spinner mPackageUnitView;
    private FstPackageSetPresenter mPresenter;
    private SimProfileDes mSimProfileDes;
    private Bundle mSimProfileDesMap = new Bundle();
    private Spinner mStartDayView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fst_package_set);
        this.isNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        try {
            this.mSimProfileDesMap = Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT)).getSimProfileDes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        if (intent != null) {
            this.mImsi = intent.getStringExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI);
            this.mSimProfileDes = (SimProfileDes) getIntent().getParcelableExtra(SimProfileDes.SIM_PROFILE_DES);
            if (TextUtils.isEmpty(this.mImsi)) {
                HwLog.d(TAG, "this is no imsi");
                finish();
            }
            this.mPackageNumText = (EditText) findViewById(R.id.package_num);
            this.mPackageUnitView = (Spinner) findViewById(R.id.package_unit);
            this.mStartDayView = (Spinner) findViewById(R.id.start_day);
            this.mFinishButton = (Button) findViewById(R.id.finish_button);
            this.mOtherTrafficViewGroup = (ViewGroup) findViewById(R.id.ll_other_pkg_wrap);
            this.mFinishButton.setOnClickListener(this);
            this.mPackageNumText.addTextChangedListener(this);
            this.mOtherTrafficViewGroup.setOnClickListener(this);
            if (!this.isNetAssistantEnable) {
                initGlobalView();
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        TrafficPackageModel.destoryInstance();
    }

    protected void onResume() {
        super.onResume();
        if (this.mPresenter == null) {
            this.mGenericHandler = new GenericHandler(this);
            this.mGenericHandler.postDelayed(new Runnable() {
                public void run() {
                    FstPackageSetActivity.this.mPresenter = new FstPackageSetPresenterImp(FstPackageSetActivity.this, FstPackageSetActivity.this.mImsi);
                    FstPackageSetActivity.this.mPresenter.init();
                }
            }, 400);
        }
    }

    public void setTrafficUnitEntries(List<String> entries) {
        this.mPackageUnitView.setAdapter(new ArrayAdapter(this, 17367049, entries));
    }

    public void setStartDayEntries(List<String> entries) {
        this.mStartDayView.setAdapter(new ArrayAdapter(this, 17367049, entries));
    }

    public void showDefaultView(int unitPos, int StartDayPos, boolean notifyChecked) {
        this.mPackageUnitView.setSelection(unitPos);
        this.mStartDayView.setSelection(StartDayPos);
        this.mLockState = notifyChecked;
        this.mFinishButton.setEnabled(false);
    }

    public void showOtherPackageSet() {
    }

    public void finishPackageSet() {
        HsmStat.statE(Events.E_NETASSISTANT_FST_PACKAGE_SET_NEXT);
        Intent intent = new Intent();
        intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mImsi);
        intent.setClass(this, FstOperatorSetActivity.class);
        startActivityForResult(intent, 200);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HwLog.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
        if (requestCode == 200 && resultCode == 201) {
            finish();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_other_pkg_wrap:
                Intent i = new Intent();
                i.setClass(this, OtherTrafficSettingsActivity.class);
                i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mImsi);
                i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_FIRST_SETTING, true);
                startActivity(i);
                return;
            case R.id.finish_button:
                float pkgNum = 0.0f;
                try {
                    pkgNum = Float.valueOf(this.mPackageNumText.getText().toString()).floatValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int pkgUnit = this.mPackageUnitView.getSelectedItemPosition();
                int startDay = this.mStartDayView.getSelectedItemPosition();
                boolean lockSwitch = this.mLockState;
                if (this.mSimProfileDes == null) {
                    HwLog.i(TAG, "onClick , mSimProfileDes is null, get again");
                    if (!TextUtils.isEmpty(this.mImsi)) {
                        this.mSimProfileDesMap.setClassLoader(SimProfileDes.class.getClassLoader());
                        this.mSimProfileDes = (SimProfileDes) this.mSimProfileDesMap.getParcelable(this.mImsi);
                    }
                }
                if (!this.isNetAssistantEnable) {
                    if (this.mPresenter != null) {
                        this.mPresenter.save(pkgNum, pkgUnit, startDay, lockSwitch, this.mSimProfileDes);
                    }
                    finish();
                    return;
                } else if (this.mPresenter != null) {
                    this.mPresenter.finishPackageSet(pkgNum, pkgUnit, startDay, lockSwitch, this.mSimProfileDes);
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() == 0) {
            this.mFinishButton.setEnabled(false);
        } else {
            this.mFinishButton.setEnabled(true);
        }
    }

    public void afterTextChanged(Editable edit) {
        String temp = edit.toString();
        int posDot = temp.indexOf(".");
        if (posDot < 0) {
            if (temp.length() > 6) {
                edit.delete(6, 7);
            }
            return;
        }
        if ((temp.length() - posDot) - 1 > 2) {
            edit.delete((posDot + 2) + 1, (posDot + 2) + 2);
        }
    }

    public void onHandleMessage(Message msg) {
    }

    private void initGlobalView() {
        View startDayWrap = findViewById(R.id.start_day_wrap);
        TextView textView = (TextView) findViewById(R.id.size_title);
        this.mFinishButton.setText(R.string.common_finish);
        this.mPackageNumText.setHint("");
        textView.setText(R.string.net_assistant_setting_traffic_set_title);
        getActionBar().setTitle(R.string.title_flow_notify_settings);
        ViewUtils.setVisibility(this.mOtherTrafficViewGroup, 8);
        ViewUtils.setVisibility(startDayWrap, 8);
    }
}
