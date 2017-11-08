package com.android.settings.pressure;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PressureExperienceActivity extends Activity implements OnClickListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969020);
        ((Button) findViewById(2131886991)).setOnClickListener(this);
    }

    public void onClick(View view) {
        finish();
    }
}
