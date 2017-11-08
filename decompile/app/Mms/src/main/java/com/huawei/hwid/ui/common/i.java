package com.huawei.hwid.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

/* compiled from: HistoryAccount */
public class i extends ArrayAdapter {
    private Context a;
    private int b;
    private int c;

    public i(Context context, int i, int i2, String[] strArr) {
        super(context, i, i2, strArr);
        this.a = context;
        this.b = i;
        this.c = i2;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = ((LayoutInflater) this.a.getSystemService("layout_inflater")).inflate(this.b, viewGroup, false);
        CheckedTextView checkedTextView = (CheckedTextView) inflate.findViewById(this.c);
        if (i == getCount() - 1) {
            checkedTextView.setCheckMarkDrawable(0);
        }
        checkedTextView.setText((CharSequence) getItem(i));
        return inflate;
    }
}
