package com.android.mms.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;

public class HwPreference extends Preference {
    private ViewGroup mConvertView;
    private TextView mReportState;

    public HwPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HwPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwPreference(Context context) {
        super(context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        this.mConvertView = (ViewGroup) convertView;
        this.mReportState = (TextView) this.mConvertView.findViewById(R.id.report_state);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        int deliveryReportState = R.string.smart_sms_setting_status_close;
        boolean strStateFlag = false;
        String preferenceState = getContext().getString(R.string.smart_sms_setting_status_close);
        if ("pref_key_signature".equals(getKey())) {
            if (sp.getBoolean(getKey(), false)) {
                deliveryReportState = R.string.smart_sms_setting_status_open;
            } else {
                deliveryReportState = R.string.smart_sms_setting_status_close;
            }
        } else if ("pref_key_always_receive_and_send_mms".equals(getKey())) {
            preferenceState = getContext().getResources().getStringArray(R.array.prefEntries_mms_always_receive_and_send_mms)[sp.getInt("alwaysAllowMms", 1)];
            strStateFlag = true;
        } else {
            int defaultDeliveryReport = MmsConfig.getDefaultDeliveryReportState();
            try {
                defaultDeliveryReport = sp.getInt(getKey(), defaultDeliveryReport);
                if (defaultDeliveryReport != 1) {
                    if (defaultDeliveryReport != 2) {
                    }
                    deliveryReportState = R.string.multimedia_message;
                }
            } catch (ClassCastException e) {
                String deliveryReportStr = sp.getString("pref_key_delivery_reports", getContext().getString(GeneralPreferenceFragment.getDeliveryReportResId(R.string.smart_sms_setting_status_close)));
                if (defaultDeliveryReport != 1) {
                    if (defaultDeliveryReport != 2) {
                        if (defaultDeliveryReport == 3) {
                            deliveryReportState = R.string.text_multimedia_message;
                            if (strStateFlag) {
                                setState(deliveryReportState);
                            } else {
                                setState(preferenceState);
                            }
                            onBindView(convertView);
                            return convertView;
                        }
                        deliveryReportState = R.string.smart_sms_setting_status_close;
                        if (strStateFlag) {
                            setState(preferenceState);
                        } else {
                            setState(deliveryReportState);
                        }
                        onBindView(convertView);
                        return convertView;
                    }
                }
            } catch (Throwable th) {
                if (defaultDeliveryReport != 1) {
                    if (defaultDeliveryReport != 2) {
                        if (defaultDeliveryReport == 3) {
                        }
                    }
                }
            }
            deliveryReportState = R.string.text_message;
        }
        if (strStateFlag) {
            setState(preferenceState);
        } else {
            setState(deliveryReportState);
        }
        onBindView(convertView);
        return convertView;
    }

    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(getLayoutResource(), parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            if (getWidgetLayoutResource() != 0) {
                layoutInflater.inflate(getWidgetLayoutResource(), widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    public void setState(int resId) {
        if (this.mReportState != null) {
            this.mReportState.setText(resId);
        }
    }

    public void setState(String resStr) {
        if (this.mReportState != null) {
            this.mReportState.setText(resStr);
        }
    }
}
