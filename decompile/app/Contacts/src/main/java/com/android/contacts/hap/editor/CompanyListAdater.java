package com.android.contacts.hap.editor;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.Locale;

public class CompanyListAdater extends BaseAdapter {
    private final Context mContext;
    private Cursor mCursor;
    private final int mHighLightColor;
    private String mHighLightString;
    private final LayoutInflater mLayoutInflater = LayoutInflater.from(this.mContext);

    public static class CompanyListItemCache {
        public TextView mCompanyName;

        public CompanyListItemCache(View view) {
            this.mCompanyName = (TextView) view.findViewById(R.id.company_name);
        }

        public String getCompanyName() {
            return this.mCompanyName.getText().toString();
        }
    }

    public CompanyListAdater(Context context, String inputString) {
        this.mContext = context;
        this.mHighLightString = inputString.toLowerCase(Locale.getDefault());
        int color = ImmersionUtils.getControlColor(this.mContext.getResources());
        if (color != 0) {
            this.mHighLightColor = color;
        } else {
            this.mHighLightColor = this.mContext.getResources().getColor(R.color.people_app_theme_color);
        }
    }

    public int getCount() {
        if (this.mCursor != null) {
            return this.mCursor.getCount();
        }
        return 0;
    }

    public String getItem(int position) {
        String company = null;
        if (this.mCursor != null) {
            try {
                this.mCursor.moveToPosition(position);
                company = this.mCursor.getString(0);
            } catch (Exception e) {
                HwLog.e("CompanyListAdapter", e.getMessage());
                this.mCursor.close();
                this.mCursor = null;
            }
        }
        return company;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.company_list_item, parent, false);
            convertView.setTag(new CompanyListItemCache(convertView));
        }
        View view = convertView;
        CompanyListItemCache viewCache = (CompanyListItemCache) convertView.getTag();
        String companyName = getItem(position);
        if (companyName != null) {
            String lowerCaseCompanyName = companyName.toLowerCase(Locale.getDefault());
            SpannableStringBuilder sb = new SpannableStringBuilder(companyName);
            int start = lowerCaseCompanyName.indexOf(this.mHighLightString);
            int end = start + this.mHighLightString.length();
            if (start >= 0 && end <= sb.length()) {
                sb.setSpan(new ForegroundColorSpan(this.mHighLightColor), start, end, 33);
            }
            viewCache.mCompanyName.setText(sb);
        }
        return view;
    }

    public void setCursor(Cursor cursor) {
        Cursor oldCursor = this.mCursor;
        if (cursor == null || cursor.isClosed()) {
            this.mCursor = null;
        } else {
            this.mCursor = cursor;
        }
        notifyDataSetChanged();
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    public void setInputString(String inputString) {
        this.mHighLightString = inputString.toLowerCase(Locale.getDefault());
    }
}
