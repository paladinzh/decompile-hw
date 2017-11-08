package com.huawei.netassistant.ui.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.ServiceManager;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficStatisticsInfo;
import com.huawei.systemmanager.util.HwLog;

public class NetAssistantDialogManager {
    public static final String FLOW_NOTIFY_LIMITE_SETTINGS_PREF_KEY = "flow_limit_bytes";
    public static final String TAG = "NetAssistantDialogManager";

    public interface TrafficSetListener {
        void onSet(float f, String str);
    }

    public static void createManualAdjustSettingDialog(Context context, String imsi, OnDismissListener listener, ViewGroup container) {
        View view = LayoutInflater.from(context).inflate(R.layout.sub_settings_manual_adjust, container, false);
        AlertDialog dialog = createManualAdjustDialog(Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT)), imsi, context, view);
        ((EditText) view.findViewById(R.id.flow_limited_editText)).addTextChangedListener(createTextWatcher());
        final Button unitButton = (Button) view.findViewById(R.id.size_type);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.size_unit_no_kb, 17367048);
        unitButton.setText((CharSequence) adapter.getItem(0));
        unitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.equals(unitButton.getText(), (CharSequence) adapter.getItem(0))) {
                    unitButton.setText((CharSequence) adapter.getItem(1));
                } else {
                    unitButton.setText((CharSequence) adapter.getItem(0));
                }
            }
        });
        dialog.setView(view);
        dialog.setOnDismissListener(listener);
        dialog.show();
        final Button button = dialog.getButton(-1);
        button.setEnabled(false);
        ((Button) dialog.findViewById(R.id.content_flow_limited_title)).setVisibility(8);
        ((EditText) dialog.findViewById(R.id.flow_limited_editText)).addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        requestInputMethod(dialog);
    }

    private static AlertDialog createManualAdjustDialog(final INetAssistantService service, final String imsi, Context context, final View view) {
        return new Builder(context).setIconAttribute(16843605).setTitle(R.string.content_manual_adjust_settings).setPositiveButton(R.string.alert_dialog_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                CharSequence selected = ((Button) view.findViewById(R.id.size_type)).getText();
                String manualAdjustEdit = ((EditText) view.findViewById(R.id.flow_limited_editText)).getText().toString();
                float manualAdjust = -1.0f;
                try {
                    if (!TextUtils.isEmpty(manualAdjustEdit)) {
                        manualAdjust = Float.valueOf(manualAdjustEdit).floatValue();
                    }
                } catch (Exception e) {
                    HwLog.e(NetAssistantDialogManager.TAG, "set manualAdjust failed");
                }
                if (manualAdjust >= 0.0f) {
                    try {
                        long curTime = DateUtil.getCurrentTimeMills();
                        TrafficStatisticsInfo tsInfo = new TrafficStatisticsInfo(imsi, DateUtil.getYearMonth(imsi), 301);
                        tsInfo.get();
                        tsInfo.setTraffic(CommonMethodUtil.unitConvert(manualAdjust, selected));
                        tsInfo.setRecordTime(curTime);
                        tsInfo.save(null);
                        service.clearMonthLimitPreference(imsi);
                        service.clearMonthWarnPreference(imsi);
                        service.clearDailyWarnPreference(imsi);
                    } catch (Exception e2) {
                        HwLog.e(NetAssistantDialogManager.TAG, "setAdjustItemInfo Exception !");
                        e2.printStackTrace();
                    }
                }
                dialog.dismiss();
                HsmStat.statE(89);
            }
        }).setNegativeButton(R.string.alert_dialog_cancel, null).create();
    }

    public static void createPromptMessageDialog(Context context, final String imsi, ViewGroup container) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences("traffic_sms_no_tip", 0);
        if (sharedPreferences.getBoolean("no_tip", false)) {
            sendAdjustSMS(imsi);
            return;
        }
        Builder builder = new Builder(context);
        builder.setMessage(R.string.net_assistant_manual_mms_message);
        LayoutInflater inflater = LayoutInflater.from(context);
        builder.setCustomTitle((RelativeLayout) inflater.inflate(R.layout.traffic_sms_dialog_title, container, false));
        RelativeLayout contentLayout = (RelativeLayout) inflater.inflate(R.layout.traffic_sms_no_tip, container, false);
        final CheckBox checkBox = (CheckBox) contentLayout.findViewById(R.id.checkbox);
        builder.setPositiveButton(R.string.traffic_sms_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    Editor editor = sharedPreferences.edit();
                    editor.putBoolean("no_tip", true);
                    editor.commit();
                }
                NetAssistantDialogManager.sendAdjustSMS(imsi);
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setView(contentLayout);
        builder.create().show();
    }

    private static void sendAdjustSMS(String imsi) {
        HwLog.i(TAG, "sendAdjustSMS.");
        try {
            Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT)).sendAdjustSMS(imsi);
            ToastUtils.toastShortMsg((int) R.string.net_assistant_toast_manul_send_sms_Toast);
        } catch (Exception e) {
            HwLog.e(TAG, "setAdjustItemInfo Exception !");
            e.printStackTrace();
        }
    }

    private static void requestInputMethod(AlertDialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }

    public static TextWatcher createTextWatcher() {
        return new TextWatcher() {
            private static final int DECIMAL_PART_MAXLEN = 2;
            private static final int INTEGER_PART_MAXLEN = 6;

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

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        };
    }

    public static void createTrafficSettingsDialog(Context context, final Preference mNotifyLimitBytePref, final TrafficSetListener listener, String title, ViewGroup container) {
        String t;
        Builder builder = new Builder(context);
        if (TextUtils.isEmpty(title)) {
            t = context.getResources().getString(R.string.net_assistant_setting_traffic_set_title);
        } else {
            t = title;
        }
        builder.setTitle(t);
        View view = LayoutInflater.from(context).inflate(R.layout.traffic_num_setting_layout, container, false);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.flow_limited_editText);
        editText.addTextChangedListener(createTextWatcher());
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.size_unit_no_kb, 17367048);
        final Button unitButton = (Button) view.findViewById(R.id.size_type);
        unitButton.setText((CharSequence) adapter.getItem(0));
        unitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.equals(unitButton.getText(), (CharSequence) adapter.getItem(0))) {
                    unitButton.setText((CharSequence) adapter.getItem(1));
                } else {
                    unitButton.setText((CharSequence) adapter.getItem(0));
                }
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String edit = editText.getText().toString();
                if (!TextUtils.isEmpty(edit)) {
                    float editNum = 0.0f;
                    try {
                        editNum = Float.valueOf(edit).floatValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (listener != null) {
                        listener.onSet(editNum, unitButton.getText().toString());
                        return;
                    }
                    long setByte = CommonMethodUtil.unitConvert(editNum, unitButton.getText());
                    Editor editor = mNotifyLimitBytePref.getEditor();
                    if (editor != null) {
                        editor.putLong(NetAssistantDialogManager.FLOW_NOTIFY_LIMITE_SETTINGS_PREF_KEY, setByte).commit();
                    }
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(setByte));
                    HsmStat.statE(95, statParam);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        final Button button = dialog.getButton(-1);
        button.setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        requestInputMethod(dialog);
    }

    public static void createTimePickDialog(Context context, int titleId, int hour, int minute, OnTimeSetListener callBack) {
        TimePickerDialog dialog = new TimePickerDialog(context, callBack, hour, minute, true);
        dialog.setTitle(titleId);
        dialog.show();
    }
}
