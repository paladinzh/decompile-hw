package com.android.dialer.voicemail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.android.gms.R;

public class ActiveView extends LinearLayout implements OnClickListener {
    private Button mActiveButton;
    private ButtonListener mListener;
    private Button mlaterButton;

    public interface ButtonListener {
        void onActiveButtonClick();

        void onLaterButtonClick();
    }

    public void setButtonListener(ButtonListener listener) {
        this.mListener = listener;
    }

    public ActiveView(Context context) {
        this(context, null);
    }

    public ActiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mActiveButton = (Button) findViewById(R.id.active);
        this.mlaterButton = (Button) findViewById(R.id.later);
        this.mActiveButton.setOnClickListener(this);
        this.mlaterButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (this.mListener == null) {
            return;
        }
        if (v.getId() == R.id.active) {
            this.mListener.onActiveButtonClick();
        } else if (v.getId() == R.id.later) {
            this.mListener.onLaterButtonClick();
        }
    }
}
