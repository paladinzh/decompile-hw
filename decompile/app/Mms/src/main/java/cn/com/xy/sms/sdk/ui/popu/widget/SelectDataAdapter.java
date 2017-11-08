package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.HashMap;
import org.json.JSONArray;

public class SelectDataAdapter extends BaseAdapter {
    private AdapterDataSource mAdapterDataSource = null;
    public HashMap<String, Boolean> mCheckedStates = null;
    private LayoutInflater mLayoutInflater = null;

    public SelectDataAdapter(Context context, AdapterDataSource adapterDataSource, DuoquDialogSelected selected) {
        if (adapterDataSource != null && adapterDataSource.getDataSrouce() != null) {
            this.mAdapterDataSource = adapterDataSource;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mCheckedStates = new HashMap();
            int selectedIndex = -1;
            int dataLen = adapterDataSource.getDataSrouce().length();
            int i = 0;
            while (i < dataLen) {
                if ((!StringUtils.isNull(selected.getSelectName()) && this.mAdapterDataSource.getDisplayValue(i).equals(selected.getSelectName())) || selected.getSelectIndex() == i) {
                    selectedIndex = i;
                }
                if (selectedIndex == i) {
                    this.mCheckedStates.put(String.valueOf(i), Boolean.valueOf(true));
                } else {
                    this.mCheckedStates.put(String.valueOf(i), Boolean.valueOf(false));
                }
                if (selectedIndex == -1 && i == dataLen - 1) {
                    this.mCheckedStates.put(String.valueOf(i), Boolean.valueOf(true));
                }
                i++;
            }
        }
    }

    public int getCount() {
        if (this.mAdapterDataSource == null || this.mAdapterDataSource.getDataSrouce() == null) {
            return 0;
        }
        return this.mAdapterDataSource.getDataSrouce().length();
    }

    public Object getItem(int arg0) {
        if (this.mAdapterDataSource == null || this.mAdapterDataSource.getDataSrouce() == null) {
            return null;
        }
        return this.mAdapterDataSource.getDataSrouce().opt(arg0);
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(final int index, View convertView, ViewGroup arg2) {
        ViewHolder viewHolder;
        boolean res;
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.duoqu_list_items_content, null);
            viewHolder = new ViewHolder();
            viewHolder.mItemRadioButton = (RadioButton) convertView.findViewById(R.id.item_rb);
            viewHolder.mItemTextView = (TextView) convertView.findViewById(R.id.item_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mItemTextView.setText(getDisplayValue(index));
        viewHolder.mItemRadioButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                for (String key : SelectDataAdapter.this.mCheckedStates.keySet()) {
                    SelectDataAdapter.this.mCheckedStates.put(key, Boolean.valueOf(false));
                }
                SelectDataAdapter.this.mCheckedStates.put(String.valueOf(index), Boolean.valueOf(((RadioButton) v).isChecked()));
                SelectDataAdapter.this.notifyDataSetChanged();
            }
        });
        convertView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                for (String key : SelectDataAdapter.this.mCheckedStates.keySet()) {
                    SelectDataAdapter.this.mCheckedStates.put(key, Boolean.valueOf(false));
                }
                SelectDataAdapter.this.mCheckedStates.put(String.valueOf(index), Boolean.valueOf(true));
                SelectDataAdapter.this.notifyDataSetChanged();
            }
        });
        if (this.mCheckedStates.get(String.valueOf(index)) == null || !((Boolean) this.mCheckedStates.get(String.valueOf(index))).booleanValue()) {
            res = false;
            this.mCheckedStates.put(String.valueOf(index), Boolean.valueOf(false));
        } else {
            res = true;
        }
        viewHolder.mItemRadioButton.setChecked(res);
        return convertView;
    }

    public JSONArray getDataSource() {
        return this.mAdapterDataSource == null ? null : this.mAdapterDataSource.getDataSrouce();
    }

    public String getDisplayValue(int index) {
        return this.mAdapterDataSource == null ? null : this.mAdapterDataSource.getDisplayValue(index);
    }
}
