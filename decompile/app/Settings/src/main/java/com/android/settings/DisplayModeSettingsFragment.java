package com.android.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;

public class DisplayModeSettingsFragment extends Fragment {
    static final int[] LAYOUT_ARRAY = new int[]{2130968754, 2130968753, 2130968751};
    private int mPageIndex = 0;

    public void setPageIndex(int pageIndex) {
        this.mPageIndex = pageIndex;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ARRAY[this.mPageIndex], container, false);
        ((TextView) view.findViewById(2131886524)).setText("08:08");
        ((TextView) view.findViewById(2131886527)).setText(getString(2131627950, new Object[]{getString(2131627949)}));
        ((TextView) view.findViewById(2131886528)).setText(getString(2131627951, new Object[]{getString(2131627952)}));
        setSentDate((TextView) view.findViewById(2131886529));
        if (Utils.isWifiOnly(getContext())) {
            ((ImageView) view.findViewById(2131886523)).setVisibility(8);
        }
        return view;
    }

    private void setSentDate(TextView textView) {
        Calendar c = Calendar.getInstance();
        c.set(2015, 5, 5, 9, 5);
        textView.setText(formatLongDateAndTime(c.getTimeInMillis()).toString());
    }

    private CharSequence formatLongDateAndTime(long when) {
        return DateUtils.formatDateTime(getActivity(), when, 524311);
    }
}
