package com.android.contacts.hap.numbermark;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import java.util.Timer;
import java.util.TimerTask;

public class NumberMarkActivity extends Activity {
    private static final int[] HAS_USER_MARK_DIALOG_ARRAY = new int[]{R.string.number_mark_remove, R.string.number_mark_crank, R.string.number_mark_fraud, R.string.number_mark_express, R.string.number_mark_promote_sales, R.string.number_mark_house_agent, R.string.number_mark_custom};
    private static final int[] NO_USER_MARK_DIALOG_ARRAY = new int[]{R.string.number_mark_crank, R.string.number_mark_fraud, R.string.number_mark_express, R.string.number_mark_promote_sales, R.string.number_mark_house_agent, R.string.number_mark_custom};
    private OnClickListener cancel_onClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NumberMarkActivity.this.setResultForCaller("");
        }
    };
    private boolean isCoustomDialogShow = false;
    private Context mContext = null;
    private AlertDialog mCustomDialog = null;
    private EditText mEditText = null;
    private int mHighlightColor;
    private NumberMarkManager mMarkManager;
    private String mMarkPhoneNumber = "";
    private int mMarkPhoneType = -1;
    private int mNormalColor;
    private OnClickListener ok_onClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String customMark = NumberMarkActivity.this.mEditText.getText().toString().trim();
            NumberMarkActivity.this.mMarkPhoneType = 5;
            NumberMarkActivity.this.setResultForCaller(customMark);
            StatisticalHelper.report(5001);
        }
    };

    private class MarkDialogAdapter extends BaseAdapter {
        private MarkDialogAdapter() {
        }

        public int getCount() {
            return NumberMarkActivity.this.mMarkPhoneType != -1 ? NumberMarkActivity.HAS_USER_MARK_DIALOG_ARRAY.length : NumberMarkActivity.NO_USER_MARK_DIALOG_ARRAY.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            UserMarkDialogItemViewHolder holder;
            int[] targetArray;
            if (convertView == null) {
                convertView = LayoutInflater.from(NumberMarkActivity.this.mContext).inflate(R.layout.user_mark_dialog_item, parent, false);
                holder = new UserMarkDialogItemViewHolder();
                holder.nameTV = (TextView) convertView.findViewById(R.id.user_mark_name);
                holder.alertTV = (TextView) convertView.findViewById(R.id.user_mark_alert);
                convertView.setTag(holder);
            }
            holder = (UserMarkDialogItemViewHolder) convertView.getTag();
            if (NumberMarkActivity.this.mMarkPhoneType != -1) {
                holder.nameTV.setText(NumberMarkActivity.this.mContext.getResources().getString(NumberMarkActivity.HAS_USER_MARK_DIALOG_ARRAY[position]));
                if (NumberMarkActivity.this.mMarkPhoneType == position - 1) {
                    holder.nameTV.setTextColor(NumberMarkActivity.this.mHighlightColor);
                } else {
                    holder.nameTV.setTextColor(NumberMarkActivity.this.mNormalColor);
                }
                targetArray = NumberMarkActivity.HAS_USER_MARK_DIALOG_ARRAY;
            } else {
                holder.nameTV.setText(NumberMarkActivity.this.mContext.getResources().getString(NumberMarkActivity.NO_USER_MARK_DIALOG_ARRAY[position]));
                targetArray = NumberMarkActivity.NO_USER_MARK_DIALOG_ARRAY;
            }
            switch (targetArray[position]) {
                case R.string.number_mark_crank:
                case R.string.number_mark_fraud:
                    holder.alertTV.setVisibility(0);
                    holder.alertTV.setText(NumberMarkActivity.this.mContext.getResources().getString(R.string.number_mark_report_to_server_item_alert));
                    convertView.setMinimumHeight(NumberMarkActivity.this.mContext.getResources().getDimensionPixelOffset(R.dimen.user_mark_dialog_list_item_double_line_min_height));
                    break;
                default:
                    holder.alertTV.setVisibility(8);
                    holder.alertTV.setText("");
                    convertView.setMinimumHeight(NumberMarkActivity.this.mContext.getResources().getDimensionPixelOffset(R.dimen.user_mark_dialog_list_item_single_line_min_height));
                    break;
            }
            return convertView;
        }
    }

    class MarkTextViewClickListener implements OnClickListener {
        MarkTextViewClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            String phoneMark = "";
            int phoneType = -1;
            int caseValue = which;
            if (NumberMarkActivity.this.mMarkPhoneType != -1) {
                caseValue = which - 1;
            }
            switch (caseValue) {
                case -1:
                    dialog.cancel();
                    NumberMarkActivity.this.mMarkManager.unmark(NumberMarkActivity.this.mMarkPhoneNumber);
                    Intent intent = new Intent();
                    intent.putExtra("PHONE_NUMBER", NumberMarkActivity.this.mMarkPhoneNumber);
                    intent.putExtra("MARK_TYPE", -1);
                    NumberMarkActivity.this.setResult(-1, intent);
                    return;
                case 0:
                    phoneMark = NumberMarkActivity.this.getString(R.string.number_mark_crank);
                    phoneType = 0;
                    break;
                case 1:
                    phoneMark = NumberMarkActivity.this.getString(R.string.number_mark_fraud);
                    phoneType = 1;
                    break;
                case 2:
                    phoneMark = NumberMarkActivity.this.getString(R.string.number_mark_express);
                    phoneType = 2;
                    break;
                case 3:
                    phoneMark = NumberMarkActivity.this.getString(R.string.number_mark_promote_sales);
                    phoneType = 3;
                    break;
                case 4:
                    phoneMark = NumberMarkActivity.this.getString(R.string.number_mark_house_agent);
                    phoneType = 4;
                    break;
                case 5:
                    dialog.cancel();
                    NumberMarkActivity.this.showCustomDialog();
                    return;
            }
            dialog.cancel();
            NumberMarkActivity.this.mMarkPhoneType = phoneType;
            NumberMarkActivity.this.setResultForCaller(phoneMark);
            StatisticalHelper.report(5001);
        }
    }

    private static class UserMarkDialogItemViewHolder {
        private TextView alertTV;
        private TextView nameTV;

        private UserMarkDialogItemViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        requestWindowFeature(1);
        if (((KeyguardManager) getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(524288);
        }
        if (EmuiFeatureManager.isChinaArea()) {
            this.mContext = this;
            int color = ImmersionUtils.getControlColor(getResources());
            if (color != 0) {
                this.mHighlightColor = color;
            } else {
                this.mHighlightColor = getResources().getColor(R.color.contact_highlight_color);
            }
            this.mNormalColor = getResources().getColor(R.color.shortcut_item_type_textcolor);
            Intent intent = getIntent();
            this.mMarkPhoneNumber = intent.getStringExtra("PHONE_NUMBER");
            this.mMarkPhoneType = intent.getIntExtra("MARK_TYPE", -1);
            this.mMarkManager = new NumberMarkManager(getApplicationContext(), null);
            NumberMarkInfo info = this.mMarkManager.getLocalMark(this.mMarkPhoneNumber);
            if (!(info == null || info.isCloudMark())) {
                this.mMarkPhoneType = NumberMarkUtil.convertClassifyToType(info.getClassify());
            }
            showDialog(100);
            return;
        }
        finish();
    }

    protected void onDestroy() {
        if (this.mMarkManager != null) {
            this.mMarkManager.destory();
        }
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void setResultForCaller(String phoneMark) {
        if (phoneMark == null || phoneMark.equals("")) {
            Intent intent = new Intent();
            intent.putExtra("PHONE_NUMBER", this.mMarkPhoneNumber);
            setResult(0, intent);
            return;
        }
        this.mMarkManager.mark(this.mMarkPhoneNumber, this.mMarkPhoneType, phoneMark);
        intent = new Intent();
        intent.putExtra("MARK_SUMMERY", phoneMark);
        intent.putExtra("PHONE_NUMBER", this.mMarkPhoneNumber);
        intent.putExtra("MARK_TYPE", this.mMarkPhoneType);
        setResult(-1, intent);
    }

    private void showCustomDialog() {
        this.isCoustomDialogShow = true;
        View customView = LayoutInflater.from(this).inflate(R.layout.number_custom_dialog, null);
        this.mCustomDialog = new Builder(this.mContext).setTitle(R.string.number_mark_custom).setView(customView).setPositiveButton(17039370, this.ok_onClickListener).setNegativeButton(17039360, this.cancel_onClickListener).show();
        this.mEditText = (EditText) customView.findViewById(R.id.customedittext);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                ((InputMethodManager) NumberMarkActivity.this.mContext.getSystemService("input_method")).showSoftInput(NumberMarkActivity.this.mEditText, 0);
            }
        }, 300);
        this.mCustomDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                NumberMarkActivity.this.isCoustomDialogShow = false;
                timer.cancel();
                NumberMarkActivity.this.finish();
            }
        });
        this.mCustomDialog.getButton(-1).setEnabled(!this.mEditText.getText().toString().equalsIgnoreCase(""));
        this.mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button buttonOk = NumberMarkActivity.this.mCustomDialog.getButton(-1);
                if (s.toString().trim().length() == 0) {
                    buttonOk.setEnabled(false);
                } else {
                    buttonOk.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                Dialog markPhoneDialog = new Builder(this.mContext).setCustomTitle(LayoutInflater.from(this).inflate(R.layout.user_mark_dialog_title, null)).setAdapter(new MarkDialogAdapter(), new MarkTextViewClickListener()).create();
                markPhoneDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (!NumberMarkActivity.this.isCoustomDialogShow) {
                            NumberMarkActivity.this.finish();
                        }
                    }
                });
                return markPhoneDialog;
            default:
                return null;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        removeDialog(100);
        if (this.mCustomDialog != null && this.mCustomDialog.isShowing()) {
            this.mCustomDialog.dismiss();
            this.mCustomDialog = null;
        }
        finish();
    }
}
