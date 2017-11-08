package com.android.deskclock.alarmclock;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.deskclock.R;

public class ButtonPreference extends Preference {
    private Callback mCallBack = null;

    public interface Callback {
        void onClick(View view);
    }

    public ButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonPreference(Context context) {
        super(context, null);
    }

    protected void onBindView(View view) {
        ((Button) view.findViewById(R.id.delete_alarm)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ButtonPreference.this.mCallBack != null) {
                    ButtonPreference.this.mCallBack.onClick(v);
                }
            }
        });
        super.onBindView(view);
    }

    public void setCallback(Callback callBack) {
        this.mCallBack = callBack;
        notifyChanged();
    }
}
