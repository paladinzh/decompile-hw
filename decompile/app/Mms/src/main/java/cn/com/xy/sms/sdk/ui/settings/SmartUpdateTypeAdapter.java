package cn.com.xy.sms.sdk.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.R;

public class SmartUpdateTypeAdapter extends BaseAdapter {
    private static final int UPDATE_TYPE_ALL = 2;
    private static final int UPDATE_TYPE_CLOSE = 0;
    private static final int UPDATE_TYPE_WALAN = 1;
    private String[] mArr;
    private Context mContext;
    private int mItemId = -1;
    private MyHolder mMyHolder;

    private class MyHolder {
        RadioButton radioButton;
        TextView summaryText;
        TextView titleText;

        private MyHolder() {
        }

        public void bindText(String str, int pos) {
            if (str.indexOf("\n") != -1) {
                String[] texts = str.split("\n");
                if (this.titleText != null) {
                    this.titleText.setText(texts[0]);
                }
                if (this.summaryText != null) {
                    this.summaryText.setVisibility(0);
                    this.summaryText.setText(texts[1]);
                }
            } else {
                this.summaryText.setVisibility(8);
                if (this.titleText != null) {
                    this.titleText.setText(str);
                }
            }
            if (this.radioButton == null) {
                return;
            }
            if (SmartUpdateTypeAdapter.this.mItemId == 2) {
                if (pos == 0) {
                    this.radioButton.setChecked(true);
                } else {
                    this.radioButton.setChecked(false);
                }
            } else if (SmartUpdateTypeAdapter.this.mItemId == 1) {
                if (pos == 1) {
                    this.radioButton.setChecked(true);
                } else {
                    this.radioButton.setChecked(false);
                }
            } else if (SmartUpdateTypeAdapter.this.mItemId != 0) {
            } else {
                if (pos == 2) {
                    this.radioButton.setChecked(true);
                } else {
                    this.radioButton.setChecked(false);
                }
            }
        }
    }

    public SmartUpdateTypeAdapter(Context context, int arrId, int selectId) {
        this.mContext = context;
        this.mArr = context.getResources().getStringArray(arrId);
        this.mItemId = selectId;
    }

    public int getCount() {
        return this.mArr.length;
    }

    public Object getItem(int position) {
        return this.mArr[position];
    }

    public long getItemId(int id) {
        return (long) id;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        this.mMyHolder = null;
        if (convertView == null) {
            this.mMyHolder = new MyHolder();
            convertView = (RelativeLayout) LayoutInflater.from(this.mContext).inflate(R.layout.duoqu_update_type_item, null);
            this.mMyHolder.titleText = (TextView) convertView.findViewById(R.id.duoqu_title);
            this.mMyHolder.summaryText = (TextView) convertView.findViewById(R.id.duoqu_summary);
            this.mMyHolder.radioButton = (RadioButton) convertView.findViewById(R.id.duoqu_radio);
            this.mMyHolder.radioButton.setFocusable(false);
            convertView.setTag(this.mMyHolder);
        } else {
            this.mMyHolder = (MyHolder) convertView.getTag();
        }
        this.mMyHolder.bindText(this.mArr[position], position);
        return convertView;
    }
}
