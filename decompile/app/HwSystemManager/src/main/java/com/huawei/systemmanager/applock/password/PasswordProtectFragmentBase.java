package com.huawei.systemmanager.applock.password;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.huawei.systemmanager.R;

public abstract class PasswordProtectFragmentBase extends Fragment {
    private static final String TAG = "PasswordProtectFragmentBase";
    protected Context mAppContext = null;
    protected Button mEndButton = null;
    private Button mStartButton = null;

    protected abstract void endButtonClick();

    protected abstract int getEndButtonText();

    protected abstract int getProtectFragmentLayoutID();

    protected abstract int getProtectFragmentTitle();

    protected abstract int getStartButtonText();

    protected abstract void initSubViews(View view);

    protected abstract void startButtonClick();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppContext = getActivity().getApplicationContext();
        getActivity().getActionBar().setTitle(getProtectFragmentTitle());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getProtectFragmentLayoutID(), container, false);
        initSharedViews(view);
        initSubViews(view);
        return view;
    }

    private void initSharedViews(View view) {
        this.mStartButton = (Button) view.findViewById(R.id.app_lock_protect_button_start);
        this.mStartButton.setText(getStartButtonText());
        this.mEndButton = (Button) view.findViewById(R.id.app_lock_protect_button_end);
        this.mEndButton.setText(getEndButtonText());
        this.mStartButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PasswordProtectFragmentBase.this.startButtonClick();
            }
        });
        this.mEndButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PasswordProtectFragmentBase.this.endButtonClick();
            }
        });
    }
}
