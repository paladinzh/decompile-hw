package com.android.contacts.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.android.gms.R;
import java.util.ArrayList;

public class AlphaIndexFamilynameAdapter extends BaseAdapter {
    private ArrayList<FamilynameInfo> mArrayList = new ArrayList();
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public static class FamilynameInfo {
        private String mFamilyname;
        private int mPosition;

        public FamilynameInfo(String familyname, int position) {
            this.mFamilyname = familyname;
            this.mPosition = position;
        }

        public String getFamilynameText() {
            return this.mFamilyname;
        }

        public int getPosition() {
            return this.mPosition;
        }
    }

    public static class FamilynameViewCache {
        public final TextView mFamilynameTextView;
        public int mPosition;

        public FamilynameViewCache(View view) {
            this.mFamilynameTextView = (TextView) view.findViewById(R.id.familyname_list_item);
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public int getPosition() {
            return this.mPosition;
        }
    }

    public AlphaIndexFamilynameAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
    }

    public int getCount() {
        if (this.mArrayList != null) {
            return this.mArrayList.size();
        }
        return 0;
    }

    public FamilynameInfo getItem(int position) {
        if (this.mArrayList != null) {
            return (FamilynameInfo) this.mArrayList.get(position);
        }
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.familyname_list_item, parent, false);
            convertView.setTag(new FamilynameViewCache(convertView));
        }
        View view = convertView;
        FamilynameViewCache viewCache = (FamilynameViewCache) convertView.getTag();
        FamilynameInfo entry = getItem(position);
        if (entry != null) {
            viewCache.mFamilynameTextView.setText(entry.getFamilynameText());
            viewCache.setPosition(entry.getPosition());
        }
        return view;
    }

    public void setArrayList(ArrayList<FamilynameInfo> arrayList) {
        this.mArrayList.clear();
        if (arrayList != null) {
            this.mArrayList.addAll(arrayList);
        }
        notifyDataSetChanged();
    }
}
