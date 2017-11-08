package com.android.settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

public class FontsizeAdapter extends ArrayAdapter<String> {
    private String[] entryvalues_font_size;
    private float[] entryvalues_font_size_float;
    private Context mContext;
    private int mFieldId = 0;
    private LayoutInflater mInflater;
    private List<String> mObjects;
    private int mResource;

    public FontsizeAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        init(context, resource, 0, Arrays.asList(objects));
    }

    private void init(Context context, int resource, int textViewResourceId, List<String> objects) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mResource = resource;
        this.mObjects = objects;
        this.mFieldId = textViewResourceId;
        this.entryvalues_font_size = this.mContext.getResources().getStringArray(2131361839);
        this.entryvalues_font_size_float = new float[this.entryvalues_font_size.length];
        for (int i = 0; i < this.entryvalues_font_size.length; i++) {
            this.entryvalues_font_size_float[i] = Float.parseFloat(this.entryvalues_font_size[i]);
        }
    }

    public int getCount() {
        return this.mObjects.size();
    }

    public String getItem(int position) {
        return (String) this.mObjects.get(position);
    }

    public Context getContext() {
        return this.mContext;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, this.mResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view;
        if (convertView == null) {
            view = this.mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        try {
            TextView text;
            if (this.mFieldId == 0) {
                text = (TextView) view;
            } else {
                text = (TextView) view.findViewById(this.mFieldId);
            }
            float size = 14.0f * this.entryvalues_font_size_float[position];
            if (position == 4) {
                size = 20.3f;
            }
            text.setTextSize(1, size);
            text.setText(getItem(position));
            return view;
        } catch (ClassCastException e) {
            Log.e("FontsizeAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException("FontsizeAdapter requires the resource ID to be a TextView", e);
        }
    }
}
