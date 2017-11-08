package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class ThresholdDialog extends DialogFragment {
    private static final String KEY_HARASS_TYPE = "key_harasstype";
    private static final int MAX_VALUE = 200;
    private static final int MIN_VALUE = 1;
    private String TAG = "ThresholdDialog";
    private String mKey;
    private int mOpcard = -1;
    private int mProgressValue;
    private SeekBar mSeekBar;
    private OnClickListener onClicker = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                Context ctx = GlobalContext.getContext();
                String key = ThresholdDialog.this.mKey;
                int opCard = ThresholdDialog.this.mOpcard;
                HwLog.i(ThresholdDialog.this.TAG, "user setSingleBlockValue, key:" + key + ",value:" + ThresholdDialog.this.mProgressValue + ", opCard:" + opCard + ", res:" + RulesOps.setSingleBlockValue(ctx, key, ThresholdDialog.this.mProgressValue, opCard));
                int eventId = ThresholdDialog.this.getEventIdByKey(key);
                if (eventId > 0) {
                    HsmStat.statE(eventId, HsmStatConst.PARAM_VAL, String.valueOf(ThresholdDialog.this.mProgressValue), HsmStatConst.PARAM_SUB, String.valueOf(ThresholdDialog.this.mOpcard));
                }
                Activity ac = ThresholdDialog.this.getActivity();
                if (ac != null && (ac instanceof SingleFragmentActivity)) {
                    Fragment contentFrg = ((SingleFragmentActivity) ac).getContainedFragment();
                    if (contentFrg instanceof CallIntellThresholdFragment) {
                        ((CallIntellThresholdFragment) contentFrg).refreshData();
                    }
                }
            }
        }
    };

    public void setParams(String key, int opCard) {
        this.mKey = key;
        this.mOpcard = opCard;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TextUtils.isEmpty(this.mKey) && savedInstanceState != null) {
            this.mKey = savedInstanceState.getString(KEY_HARASS_TYPE);
        }
        if (this.mOpcard == -1 && savedInstanceState != null) {
            this.mOpcard = savedInstanceState.getInt(ConstValues.KEY_OP_CARD, -1);
        }
        if (TextUtils.isEmpty(this.mKey) || this.mOpcard == -1) {
            HwLog.e(this.TAG, "ThresholdDialog create, but key is null!, mKey:" + this.mKey + ", opcard:" + this.mOpcard);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_HARASS_TYPE, this.mKey);
        outState.putInt(ConstValues.KEY_OP_CARD, this.mOpcard);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity ac = getActivity();
        this.mProgressValue = RulesOps.getBlockIntValue(RulesOps.getAllRules(ac), this.mKey, this.mOpcard);
        String dialogTitle = getDialogTilteByKey(this.mKey);
        Builder builder = new Builder(ac);
        View contenView = ac.getLayoutInflater().inflate(R.layout.preference_dialog_seekbar, null);
        TextView minValueTxt = (TextView) contenView.findViewById(R.id.min_value);
        if (minValueTxt != null) {
            minValueTxt.setText(String.valueOf(1));
        }
        TextView maxValueTxt = (TextView) contenView.findViewById(R.id.max_value);
        if (maxValueTxt != null) {
            maxValueTxt.setText(String.valueOf(200));
        }
        TextView desctipTxt = (TextView) contenView.findViewById(R.id.description);
        if (desctipTxt != null) {
            desctipTxt.setText(R.string.harassment_threshold_tip);
        }
        final TextView progressTxt = (TextView) contenView.findViewById(R.id.select_value);
        progressTxt.setText(String.valueOf(this.mProgressValue));
        this.mSeekBar = (SeekBar) contenView.findViewById(R.id.seek_bar);
        this.mSeekBar.setMax(199);
        this.mSeekBar.setProgress(this.mProgressValue - 1);
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ThresholdDialog.this.setPositivieButtonEnable(true);
                }
                ThresholdDialog.this.mProgressValue = progress + 1;
                if (progressTxt != null) {
                    progressTxt.setText(String.valueOf(ThresholdDialog.this.mProgressValue));
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        builder.setTitle(dialogTitle).setNegativeButton(R.string.harassment_threshold_cancel, null).setPositiveButton(R.string.harassment_threshold_save, this.onClicker);
        builder.setView(contenView);
        Dialog dialog = builder.create();
        setPositivieButtonEnable(dialog, false);
        return dialog;
    }

    private String getDialogTilteByKey(String key) {
        String dialogTitle = "";
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.e(this.TAG, "showSeekbarDialog but activity is null!");
            return dialogTitle;
        }
        if (RulesOps.KEY_INTELL_SCAM_VALUE.equals(key)) {
            dialogTitle = ac.getString(R.string.harassment_scam_threshold_dialog_tilte);
        } else if (RulesOps.KEY_INTELL_HARASS_VALUE.equals(key)) {
            dialogTitle = ac.getString(R.string.harassment_haras_threshold_dialog_tilte);
        } else if (RulesOps.KEY_INTELL_ADVER_VALUE.equals(key)) {
            dialogTitle = ac.getString(R.string.harassment_advertise_threshold_dialog_tilte);
        } else if (RulesOps.KEY_INTELL_ESTATE_VALUE.equals(key)) {
            dialogTitle = ac.getString(R.string.harassment_estate_threshold_dialog_tilte);
        } else {
            HwLog.i(this.TAG, "showSeekbarDialog, unknow key:" + key);
        }
        return dialogTitle;
    }

    private void setPositivieButtonEnable(boolean enable) {
        setPositivieButtonEnable(getDialog(), enable);
    }

    private void setPositivieButtonEnable(Dialog dialog, boolean enable) {
        if (dialog instanceof AlertDialog) {
            Button btn = ((AlertDialog) dialog).getButton(-1);
            if (btn != null) {
                btn.setEnabled(enable);
            }
        }
    }

    private int getEventIdByKey(String key) {
        if (RulesOps.KEY_INTELL_SCAM_VALUE.equals(key)) {
            return Events.E_HARASSMENT_SET_INTELL_SCAM_CALL_THRESHOLD;
        }
        if (RulesOps.KEY_INTELL_HARASS_VALUE.equals(key)) {
            return Events.E_HARASSMENT_SET_INTELL_HARASS_CALL_THRESHOLD;
        }
        if (RulesOps.KEY_INTELL_ADVER_VALUE.equals(key)) {
            return Events.E_HARASSMENT_SET_INTELL_ADVER_CALL_THRESHOLD;
        }
        if (RulesOps.KEY_INTELL_ESTATE_VALUE.equals(key)) {
            return Events.E_HARASSMENT_SET_INTELL_ESATE_CALL_THRESHOLD;
        }
        HwLog.i(this.TAG, "getEventIdByKey unknow key:" + key);
        return -1;
    }
}
