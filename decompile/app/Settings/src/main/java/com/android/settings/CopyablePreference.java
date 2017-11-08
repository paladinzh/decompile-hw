package com.android.settings;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class CopyablePreference extends Preference {
    public CopyablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CopyablePreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(true);
        holder.setDividerAllowedBelow(true);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                CopyablePreference.copyPreference(CopyablePreference.this.getContext(), CopyablePreference.this);
                return true;
            }
        });
    }

    public CharSequence getCopyableText() {
        return getSummary();
    }

    public static void copyPreference(Context context, CopyablePreference pref) {
        ((ClipboardManager) context.getSystemService("clipboard")).setText(pref.getCopyableText());
        Toast.makeText(context, 17040184, 0).show();
    }
}
