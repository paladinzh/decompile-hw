package cn.com.xy.sms.sdk.ui.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.google.android.gms.R;

public class XyPreference extends Preference {
    private Context mContext;
    private TextView mReportState;
    private int mReportStateTextPaddingStart = 0;
    private int mReportStateTextWidth = 0;
    private String[] mUndateTypes;
    private ViewGroup mView;

    public XyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public XyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XyPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mUndateTypes = this.mContext.getResources().getStringArray(R.array.duoqu_update_type_arr);
        this.mReportStateTextWidth = (int) getContext().getResources().getDimension(R.dimen.duoqu_report_state_text_width);
        this.mReportStateTextPaddingStart = (int) getContext().getResources().getDimension(R.dimen.duoqu_report_state_text_padding_start);
    }

    public View getView(View convertView, ViewGroup parent) {
        if (this.mView != null) {
            return this.mView;
        }
        String updateTypeStr;
        View view = onCreateView(parent);
        this.mView = (ViewGroup) view;
        this.mReportState = (TextView) this.mView.findViewById(R.id.report_state);
        this.mReportState.setWidth(this.mReportStateTextWidth);
        this.mReportState.setMaxLines(1);
        this.mReportState.setGravity(8388613);
        this.mReportState.setPaddingRelative(this.mReportStateTextPaddingStart, 0, 0, 0);
        if (SmartSmsSdkUtil.SMARTSMS_UPDATE_TYPE.equals(getKey())) {
            String updateTypeSummary = "";
            switch (SmartSmsSdkUtil.getUpdateType(this.mContext)) {
                case 0:
                    updateTypeSummary = SmartSmsSettingActivity.getSummary(this.mUndateTypes[2]);
                    break;
                case 1:
                    updateTypeSummary = SmartSmsSettingActivity.getSummary(this.mUndateTypes[1]);
                    break;
                case 2:
                    updateTypeSummary = SmartSmsSettingActivity.getSummary(this.mUndateTypes[0]);
                    break;
            }
            updateTypeStr = updateTypeSummary;
        } else {
            updateTypeStr = getContext().getString(R.string.smart_sms_setting_status_close);
        }
        setState(updateTypeStr);
        onBindView(view);
        return view;
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

    public void setState(String resStr) {
        if (this.mReportState != null) {
            this.mReportState.setText(resStr);
        }
    }

    public void setState(int resId) {
        if (this.mReportState != null) {
            this.mReportState.setText(resId);
        }
    }
}
