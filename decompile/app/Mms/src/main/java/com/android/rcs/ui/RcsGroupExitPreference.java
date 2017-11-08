package com.android.rcs.ui;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.gms.R;

public class RcsGroupExitPreference extends Preference {
    private Button mExitButton;
    private int mExitTextColor;
    private OnPreferenceClickListener onPreferenceClickListener;

    public RcsGroupExitPreference(Context context) {
        super(context);
    }

    public RcsGroupExitPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RcsGroupExitPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.rcs_groupchat_detail_setting_exiting, parent, false);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mExitButton = (Button) view.findViewById(R.id.exit_group);
        this.mExitButton.setTextColor(this.mExitTextColor);
        this.mExitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RcsGroupExitPreference.this.onPreferenceClickListener != null) {
                    RcsGroupExitPreference.this.onPreferenceClickListener.onPreferenceClick(RcsGroupExitPreference.this);
                }
            }
        });
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener listener) {
        this.onPreferenceClickListener = listener;
    }

    public void setExitTextColor(int color) {
        this.mExitTextColor = color;
    }
}
